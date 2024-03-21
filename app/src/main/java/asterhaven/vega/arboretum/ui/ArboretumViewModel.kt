package asterhaven.vega.arboretum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
import asterhaven.vega.arboretum.lsystems.DerivationSteps
import asterhaven.vega.arboretum.lsystems.Systems.page60
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
class ArboretumViewModel : ViewModel() {
    val worldDrawings : List<Drawing> by lazy { arrayListOf(Globe()) }

    private val specification by lazy { page60 }
    private val _lSystem by lazy { MutableStateFlow(specification.compile()) }
    val lSystem : StateFlow<TreeLSystem> by lazy { _lSystem }

    val params by lazy { arrayListOf<ViewModelParamWrapper>().also {
        it.addAll(specification.parameters.map { sp -> ViewModelParamWrapper(sp) })
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