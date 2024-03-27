package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.graphics.ArboretumGLSurfaceView
import asterhaven.vega.arboretum.graphics.draw.Drawing

@Composable
fun WorldScreen(plantings : List<Drawing>) {
    var previousPlantings by remember { mutableStateOf<List<Drawing>>(emptyList()) }
    AndroidView(factory = ::ArboretumGLSurfaceView,
        modifier = Modifier.fillMaxSize(),
        update = {
            if(plantings != previousPlantings) {
                it.updateState(plantings)
                previousPlantings = plantings
            }
        }
    )
}