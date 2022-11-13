package asterhaven.vega.arboretum.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import asterhaven.vega.arboretum.ui.ArboretumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSetter (
    param : ArboretumViewModel.Param
) {
    val value = param.value.observeAsState(param.value.value)
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(param.symbol, Modifier.weight(.5f), textAlign = TextAlign.Center)
            Text(param.name, Modifier.weight(1.5f))
            TextField(
                "%.2f".format(param.value.value),
                { it.toFloatOrNull()?.also(param::onValueChange)  },
                Modifier.weight(1f),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Slider( value = value.value ?: param.range.start,
                onValueChange = param::onValueChange,
                valueRange = param.range)
    }
}

@Preview
@Composable
fun ParamPreview() = ParameterSetter(
    ArboretumViewModel().Param(
        symbol = "a",
        name = "name",
        value = .333f,
        range = 0f.rangeTo(1f)
    )
)
