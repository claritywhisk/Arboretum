package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.components.ParameterSetter

@Composable
fun ParamsScreen(
    params : ArrayList<ArboretumViewModel.Param>
) {
    LazyColumn {
        items(params.size, { i -> params[i].symbol }) { i ->
            ParameterSetter(params[i])
        }
    }
}
