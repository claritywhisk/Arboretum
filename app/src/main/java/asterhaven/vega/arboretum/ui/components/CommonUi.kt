package asterhaven.vega.arboretum.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.lsystems.UnitInterval
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSetter (
    paramWrapper : ArboretumViewModel.ViewModelParamWrapper
) {
    val value = paramWrapper.valueSF.collectAsState().value
    val isInt = paramWrapper.p.type is IntParameterType
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(paramWrapper.p.symbol, Modifier.weight(.5f), textAlign = TextAlign.Center)
            Text(paramWrapper.p.name, Modifier.weight(1.5f))
            TextField(
                if(isInt) value.roundToInt().toString() else "%.2f".format(value),
                { newVal -> newVal.toFloatOrNull()?.also(paramWrapper::onValueChange) },
                Modifier.weight(1f),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Slider( value = value,
                onValueChange = paramWrapper::onValueChange,
                valueRange = paramWrapper.p.type.range,
                steps = (if(paramWrapper.p.type is IntParameterType) paramWrapper.p.type.rungsCount() else 0)
        )
    }
}

@Preview
@Composable
fun ParamPreview() = ParameterSetter(
    ArboretumViewModel().ViewModelParamWrapper(TreeLSystem.Specification.Parameter(
        symbol = "a",
        name = "name",
        initialValue = .333f,
        type = UnitInterval
    ))
)

@Composable
fun LabeledTextField(label: String, text: String, onTextChanged: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label)
        TextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
