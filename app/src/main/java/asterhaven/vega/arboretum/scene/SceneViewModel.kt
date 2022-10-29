package asterhaven.vega.arboretum.scene

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import asterhaven.vega.graphics.draw.Drawing

object SceneViewModel : ViewModel() {
    val drawingsState = mutableStateListOf<Drawing>()
}