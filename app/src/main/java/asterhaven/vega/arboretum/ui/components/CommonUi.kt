package asterhaven.vega.arboretum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.ParameterType
import dev.nesk.akkurate.ValidationResult
import kotlin.math.roundToInt

@Composable
fun arbClickableTextStyle() = TextStyle(
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    background = MaterialTheme.colorScheme.surfaceVariant,
    textAlign = TextAlign.Center
)

@Composable
fun arbPlainTextStyle() = TextStyle(
    color = MaterialTheme.colorScheme.onSurface,
    background = MaterialTheme.colorScheme.surface,
    textAlign = TextAlign.Center
)

@Composable
fun ArbBasicTextField(
    value : String,
    onValueChange : (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled : Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions()
) {
    BasicTextField(
        value,
        onValueChange,
        modifier,
        enabled = isEnabled,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        textStyle = if(isEnabled) arbClickableTextStyle() else arbPlainTextStyle()
    )
}

@Composable
fun ParamTextField(value : Float, type : ParameterType, enabled : Boolean, update : (Float) -> Unit) {
    fun format(f : Float) = if (type is IntParameterType) f.roundToInt().toString() else "%.2f".format(f)
    var text by remember { mutableStateOf( format(value) )}
    var err by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        err = false
        val formattedValue = format(value)
        if(formattedValue != text) text = formattedValue
    }
    ArbBasicTextField(
        text,
        { newText ->
            err = false
            text = newText
            when(val f = newText.toFloatOrNull()) {
                null -> err = true
                else -> if(f in type.range) update(f)
                        else err = true
            }
        },
        if(err) Modifier.background(MaterialTheme.colorScheme.error) else Modifier,
        isEnabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
fun <T> ArbDropMenu(selection : T, //todo everything as LazyColumn
                    onSelect : (T) -> Unit,
                    name : @Composable (T) -> String, list : List<T>,
                    startsExpanded : Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = name(selection),
            modifier = Modifier.clickable {
                expanded = !expanded
            },
            style = arbClickableTextStyle()
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


