package asterhaven.vega.arboretum.scene

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import asterhaven.vega.arboretum.graphics.draw.shapes.Drawing
import asterhaven.vega.arboretum.graphics.draw.shapes.Globe

class SceneViewModel : ViewModel() {
    val drawingsState = mutableStateListOf<Drawing>()
    init {
        drawingsState.add(Globe())
        //val v = Vector.build(Icosahedron.vdata, 0)
        //drawingsState.add(Tree(v, UnitVector.normalize(v), page60))
    }
}