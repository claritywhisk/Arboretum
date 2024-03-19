package asterhaven.vega.arboretum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
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
    val params by lazy { arrayListOf<ViewModelParam>().also {
        it.addAll(specification.parameters.map { sp -> ViewModelParam(sp) })
    }}
    inner class ViewModelParam(p : TreeLSystem.Specification.Parameter){
        private val _value = MutableStateFlow(p.initialValue)
        val value : StateFlow<Float> = _value
        val range = p.type.range
        val symbol = p.symbol
        val name = p.name
        fun onValueChange(f : Float){
            _value.value = f
        }
        init {
            viewModelScope.launch {
                _value.debounce(30).collectLatest {
                    synchronized(specification) {
                        // Update the tree math upon debounced parameter input
                        specification.updateConstant(p.symbol, it)
                        _lSystem.value = specification.compile()
                    }
                }
            }
        }
    }
}