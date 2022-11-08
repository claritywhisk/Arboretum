package asterhaven.vega.arboretum.ui

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import asterhaven.vega.arboretum.graphics.draw.shapes.Drawing
import asterhaven.vega.arboretum.graphics.draw.shapes.Globe
import asterhaven.vega.arboretum.lsystems.Systems.page25
import asterhaven.vega.arboretum.lsystems.Systems.page60

class ArboretumViewModel : ViewModel() {
    val worldDrawings = ObservableArrayList<Drawing>()
    init {
        worldDrawings.add(Globe())
        //val v = Vector.build(Icosahedron.vdata, 0)
        //drawingsState.add(Tree(v, UnitVector.normalize(v), page60))
    }
    val fred : LiveData<Param> = MutableLiveData<Param>()


    class Param(symbol : String, value : Float, name : String = "", range : ClosedFloatingPointRange<Float>) {
        val symbol = symbol
        private val _value = MutableLiveData(value)
        val value : LiveData<Float> = _value
        fun onValueChange(f : Float){
            _value.value = f
        }
        val name = name
        val range = range
    }

    val params = arrayListOf<Param>()
    init {
        page60.constants.forEach {
            params.add(Param(
                symbol = it.key,
                value = it.value,
                name = page60.constantNames[it.key] ?: "",
                range = 0f..100f //todo
            ))
        }
    }
}