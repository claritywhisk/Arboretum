package asterhaven.vega.arboretum.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.ParameterType
import asterhaven.vega.arboretum.lsystems.Systems
import asterhaven.vega.arboretum.lsystems.UnitInterval
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import dev.nesk.akkurate.ValidationResult
import kotlin.math.roundToInt

@Composable
fun ParameterSetter ( //todo back whence ye came
    paramWrapper : ArboretumViewModel.ViewModelParamWrapper
) {
    val value = paramWrapper.valueSF.collectAsState().value
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(paramWrapper.p.symbol, Modifier.weight(.5f), textAlign = TextAlign.Center)
            Text(paramWrapper.p.name, Modifier.weight(1.5f))
            ParamTextField(value, paramWrapper.p.type, paramWrapper::onValueChange)
        }
        Slider( value = value,
                onValueChange = paramWrapper::onValueChange,
                valueRange = paramWrapper.p.type.range,
                steps = (if(paramWrapper.p.type is IntParameterType) paramWrapper.p.type.rungsCount() else 0)
        )
    }
}

@Composable
fun ParamTextField(value : Float, type : ParameterType, update : (Float) -> Unit,) {
    TextField(
        if(type is IntParameterType) value.roundToInt().toString() else "%.2f".format(value),
        { newVal -> newVal.toFloatOrNull()?.let { update(it) } },
        Modifier.width(96.dp),
        singleLine = true,
        textStyle = TextStyle(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun <T> ArbDropMenu(selection : T,
                    onSelect : (T) -> Unit,
                    name : @Composable (T) -> String, list : List<T>,
                    startsExpanded : Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = name(selection),
            modifier = Modifier.clickable {
                expanded = !expanded
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            list.forEach {
                DropdownMenuItem(
                    text = { Text(name(it)) },
                    onClick = { onSelect(it) }
                )
            }
        }
    }
}

@Composable
fun LabeledSection(
    label: String,
    error: ValidationResult.Failure?,
    content: @Composable (ColumnScope.() -> Unit)
) {
    CanShowErrorBelow(error) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(label)
            content()
        }
    }
}

@Composable
fun CanShowErrorBelow(error: ValidationResult.Failure?, content: @Composable () -> Unit) {
    Row { content() }
    error?.violations?.forEach {
        Row {
            Text(it.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

object UniqueIdGenerator {
    private var idCounter = 0
    fun nextId(): Int {
        return idCounter++
    }
}

