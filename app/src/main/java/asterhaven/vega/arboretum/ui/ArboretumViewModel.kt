package asterhaven.vega.arboretum.ui

import androidx.compose.runtime.mutableStateOf
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import asterhaven.vega.arboretum.graphics.draw.shapes.Drawing
import asterhaven.vega.arboretum.graphics.draw.shapes.Globe
import asterhaven.vega.arboretum.lsystems.TreeLSystem

class ArboretumViewModel : ViewModel() {
    val worldDrawings = ObservableArrayList<Drawing>()
    init {
        worldDrawings.add(Globe())
        //val v = Vector.build(Icosahedron.vdata, 0)
        //drawingsState.add(Tree(v, UnitVector.normalize(v), page60))
    }

    val treeLSystemBuilder = mutableStateOf( TreeLSystem.Builder() )
}