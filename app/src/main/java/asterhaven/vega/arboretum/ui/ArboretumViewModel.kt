package asterhaven.vega.arboretum.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
import asterhaven.vega.arboretum.graphics.draw.Tree
import asterhaven.vega.arboretum.lsystems.DerivationSteps
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.Specification
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
    val worldDrawings: State<List<Drawing>> get() = _worldDrawings

    private val _specification : MutableState<Specification?> = mutableStateOf(null)
    val specification : State<Specification?> = _specification
    fun updateSpecification(newSpecification : Specification){
        _lSystem.value = newSpecification.compile()
        _params.value.forEach { it.job.cancel() }
        _params.value = arrayListOf<ArboretumViewModel.ViewModelParamWrapper>()
            .apply {
                addAll(newSpecification.params.filter { p ->
                    p.type !is TrueConstant
                }.map { sp -> ViewModelParamWrapper(sp) })
            }
        _specification.value = newSpecification
    }

    private val _lSystem : MutableState<TreeLSystem?> = mutableStateOf(null)
    val lSystem : State<TreeLSystem?> = _lSystem

    private var _params : MutableState<List<ViewModelParamWrapper>> = mutableStateOf(listOf())
    val params : State<List<ViewModelParamWrapper>> = _params

    init {
        updateSpecification(Systems.list[Systems.list.lastIndex])
    }

    private val _leavingScreen : MutableState<ArboretumScreen?> = mutableStateOf(null)
    val leavingScreen : State<ArboretumScreen?> = _leavingScreen
    fun setLeavingScreen(s : ArboretumScreen) { _leavingScreen.value = s }

    fun populateAction(steps : Int){
        val trees = Icosahedron.stems.map { Tree(it, lSystem.value!!, steps) }
        val newList = mutableListOf<Drawing>()
        newList.addAll(_worldDrawings.value.filter { it !is Tree })
        newList.addAll(trees)
        _worldDrawings.value = newList
    }

    inner class ViewModelParamWrapper(val p : LParameter){
        private val _valueMSF = MutableStateFlow(p.initialValue)
        val valueSF : StateFlow<Float> = _valueMSF
        fun onValueChange(f : Float){
            _valueMSF.value = f
        }
        val job = viewModelScope.launch {
            _valueMSF.debounce(30).collectLatest {
                synchronized(this@ArboretumViewModel) {
                    if(p.type !is DerivationSteps){//preview should take action if steps alters
                        // Update the tree math upon debounced parameter input
                        if(specification.value!!.updateConstant(p.symbol, it)) {
                            _lSystem.value = specification.value!!.compile()
                        }
                    }
                }
            }
        }
    }
}