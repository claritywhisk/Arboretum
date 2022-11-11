package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.graphics.ArboretumGLSurfaceView
import asterhaven.vega.arboretum.graphics.draw.Drawing

@Composable
fun WorldScreen(plantings : List<Drawing>) {
    AndroidView(factory = ::ArboretumGLSurfaceView,
        modifier = Modifier.fillMaxSize(),
        update = {
            it.updateState(plantings)
        }
    )
}