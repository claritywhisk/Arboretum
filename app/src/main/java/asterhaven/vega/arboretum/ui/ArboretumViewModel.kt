package asterhaven.vega.arboretum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import asterhaven.vega.arboretum.lsystems.Systems.page60
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
class ArboretumViewModel : ViewModel() {
    private val specification = page60
    val params = arrayListOf<Param>()
    private val _lSystem = MutableStateFlow(specification.compile())
    val lSystem : StateFlow<TreeLSystem> = _lSystem
    init {
        specification.constants.forEach {
            params.add(Param(
                symbol = it.key,
                value = it.value.first,
                name = it.value.second,
                range = 0f..100f //todo
            ))
        }
        params.forEach { p ->
            viewModelScope.launch {
                p.value.debounce(50).collectLatest {
                    synchronized(specification) {
                        specification.constant(p.symbol, it, p.name)
                        _lSystem.value = specification.compile()
                    }
                }
            }
        }
    }

    inner class Param(symbol : String, value : Float, name : String = "", range : ClosedFloatingPointRange<Float>) {
        val symbol = symbol
        private val _value = MutableStateFlow(value)
        val value : StateFlow<Float> = _value
        fun onValueChange(f : Float){
            _value.value = f
        }
        val name = name
        val range = range
    }
}