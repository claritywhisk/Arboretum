package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.graphics.PreviewGLSurfaceView
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.components.ParameterSetter

@Composable
fun ParamsScreen(
    params : ArrayList<ArboretumViewModel.Param>,
    system : TreeLSystem,
    collapsed : Boolean = true
) {
    Column {
        AndroidView(
            ::PreviewGLSurfaceView,
            Modifier.aspectRatio(if(collapsed) 3f else 1f)
        ) {
            it.beginPreview(system)
        }
        LazyColumn {
            items(params.size, { i -> params[i].symbol }) { i ->
                ParameterSetter(params[i])
            }
        }
    }
}
