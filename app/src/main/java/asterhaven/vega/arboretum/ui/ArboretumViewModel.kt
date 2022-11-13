package asterhaven.vega.arboretum.ui

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import asterhaven.vega.arboretum.graphics.draw.Drawing
import asterhaven.vega.arboretum.graphics.draw.Globe
import asterhaven.vega.arboretum.lsystems.Systems.page25
import asterhaven.vega.arboretum.lsystems.Systems.page60
import asterhaven.vega.arboretum.lsystems.Tree
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.lsystems.specify
import kotlinx.coroutines.*

class ArboretumViewModel : ViewModel() {
    val worldDrawings = ObservableArrayList<Drawing>()
    init {
        worldDrawings.add(Globe())
        //val v = Vector.build(Icosahedron.vdata, 0)
        //drawingsState.add(Tree(v, UnitVector.normalize(v), page60))
    }


    val specification = page60
    val params = arrayListOf<Param>()
    init {
        specification.constants.forEach {
            params.add(Param(
                symbol = it.key,
                value = it.value.first,
                name = it.value.second,
                range = 0f..100f //todo
            ))
        }
    }
    private val _lSystem = MutableLiveData(specification.compile())
    val lSystem : LiveData<TreeLSystem> = _lSystem

    inner class Param(symbol : String, value : Float, name : String = "", range : ClosedFloatingPointRange<Float>) {
        val symbol = symbol
        private val _value = MutableLiveData(value)
        val value : LiveData<Float> = _value
        private var vcJob : Job? = null
        fun onValueChange(f : Float){
            _value.value = f
            val last = vcJob
            vcJob = CoroutineScope(Dispatchers.Default).launch {
                last?.cancelAndJoin()
                specification.constant(symbol, f, name)
                val ls = specification.compile()
                ensureActive()
                CoroutineScope(Dispatchers.Main).launch { _lSystem.value = ls }
            }
        }
        val name = name
        val range = range
    }
}