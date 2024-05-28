package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.graphics.PreviewGLSurfaceView
import asterhaven.vega.arboretum.lsystems.DerivationSteps
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.components.ParamTextField
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

@Composable
fun ParamsScreen(
    params : List<ArboretumViewModel.ViewModelParamWrapper>,
    system : TreeLSystem,
    populateAction : (Int) -> Unit,
    collapsed : Boolean = true
) {
    val stepsParam by remember { mutableStateOf(params.first { it.p.type is DerivationSteps }) }
    val steps = stepsParam.valueSF.map { it.roundToInt() }.collectAsState(stepsParam.p.initialValue.roundToInt())
    val populate = {
        populateAction(steps.value)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(
            ::PreviewGLSurfaceView,
            Modifier.aspectRatio(if(collapsed) 3f else 1f)
        ) {
            it.showPreview(system, steps.value)
        }
        Button(onClick = populate){
            Text(LocalContext.current.getString(R.string.btn_populate))
        }
        LazyColumn {
            items(params.size, { i -> params[i].p.symbol }) { i ->
                ParameterSetter(params[i].p, params[i].valueSF, params[i]::onValueChange)
            }
        }
    }
}

@Composable
fun ParameterSetter (p : LParameter, flow : StateFlow<Float>, update : (Float) -> Unit) {
    val value by flow.collectAsState()
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(p.symbol, Modifier.weight(.5f), textAlign = TextAlign.Center)
            Text(p.name, Modifier.weight(1.5f))
            ParamTextField(value, p.type, true, update)
        }
        Slider(
            value = value,
            onValueChange = update,
            valueRange = p.type.range,
            steps = (if (p.type is IntParameterType) p.type.rungsCount() else 0)
        )
    }
}
