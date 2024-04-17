package asterhaven.vega.arboretum.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
import asterhaven.vega.arboretum.graphics.draw.Tree
import asterhaven.vega.arboretum.lsystems.DerivationSteps
import asterhaven.vega.arboretum.lsystems.Systems
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.lsystems.TrueConstant
import asterhaven.vega.arboretum.utility.shapes.Icosahedron
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
class ArboretumViewModel : ViewModel() {
    private val _worldDrawings = mutableStateOf(mutableListOf<Drawing>(Globe()))
    val worldDrawings : State<List<Drawing>> get() = _worldDrawings

    private val _specification by lazy { mutableStateOf(Systems.list[Systems.list.lastIndex]) }
    var specification : TreeLSystem.Specification
        get() = _specification.value
        set(value){
            _specification.value = value
            _lSystem.value = value.compile()
        }

    private val _lSystem by lazy { MutableStateFlow(specification.compile()) }
    val lSystem : StateFlow<TreeLSystem> by lazy { _lSystem }

    fun populateAction(steps : Int){
        val trees = Icosahedron.stems.map { Tree(it, lSystem.value, steps) }
        val newList = mutableListOf<Drawing>()
        newList.addAll(_worldDrawings.value.filter { it !is Tree })
        newList.addAll(trees)
        _worldDrawings.value = newList
    }

    val params by lazy { arrayListOf<ViewModelParamWrapper>().apply {
        addAll(specification.parameters.filter { p ->
            p.type !is TrueConstant
        }.map { sp -> ViewModelParamWrapper(sp) })
    }}

    inner class ViewModelParamWrapper(val p : TreeLSystem.Specification.Parameter){
        private val _valueMSF = MutableStateFlow(p.initialValue)
        val valueSF : StateFlow<Float> = _valueMSF
        fun onValueChange(f : Float){
            _valueMSF.value = f
        }
        init {
            viewModelScope.launch {
                _valueMSF.debounce(30).collectLatest {
                    synchronized(this@ArboretumViewModel) {
                        if(p.type !is DerivationSteps){//preview should take action if steps alters
                            // Update the tree math upon debounced parameter input
                            if(specification.updateConstant(p.symbol, it)) {
                                _lSystem.value = specification.compile()
                            }
                        }
                    }
                }
            }
        }
    }
}