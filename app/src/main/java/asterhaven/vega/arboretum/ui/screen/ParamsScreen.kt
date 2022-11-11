package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.graphics.ArboretumGLSurfaceView
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.components.ParameterSetter

@Composable
fun ParamsScreen(
    params : ArrayList<ArboretumViewModel.Param>,
    collapsed : Boolean = false
) {
    Column{
        LazyColumn {
            items(params.size, { i -> params[i].symbol }) { i ->
                ParameterSetter(params[i])
            }
        }
        AndroidView(
            ::ArboretumGLSurfaceView,
            Modifier.fillMaxWidth().aspectRatio(if(collapsed) 1f/3f else 1f)
        ) { it.updateState() }
    }
}
