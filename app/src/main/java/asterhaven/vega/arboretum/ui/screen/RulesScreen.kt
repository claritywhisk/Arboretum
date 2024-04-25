package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.LWord
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.ui.components.LabeledSection

private const val NOT_EDITING = -1
private const val EDITING_AXIOM = Int.MAX_VALUE

@Composable
fun RulesScreen(
    baseSpecification : Specification //the selected spec which this screen shows/modifies
) {
    val axiom by remember { mutableStateOf(baseSpecification.initialRaw) }
    val productionRules =
        remember { mutableStateListOf(*baseSpecification.productionsRaw.toTypedArray()) }
    val editingRow = remember { mutableStateOf(NOT_EDITING) }
    val cursorPos = remember { mutableStateOf(NOT_EDITING) }
    var reorderDeleteButtonsVisible by remember { mutableStateOf(false) }
    @Composable
    fun AccursedText(text: String, modifier: Modifier = Modifier) {
        Text(buildAnnotatedString {
            append(text)
            if (cursorPos.value != NOT_EDITING)
                addStyle(SpanStyle(background = Color.Cyan), cursorPos.value, cursorPos.value + 1)
        }, modifier)
    }
    @Composable
    fun EditRow() {
        Row(Modifier.fillMaxWidth()) {
            DropdownMenu(expanded = true, onDismissRequest = { editingRow.value = NOT_EDITING }) {
                LWord.standardSymbols.forEach {
                    DropdownMenuItem(text = {
                        Text(it.symbol.toString())
                        Text(it.desc)
                    }, onClick = { })
                }
            }
        }
    }

    @Composable
    fun ReorderDeleteButtons(i : Int){
        IconButton(onClick = {
            productionRules.removeAt(i)
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
        }
        IconButton(enabled = i > 0, onClick = {
            productionRules.add(i - 1, productionRules.removeAt(i))
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Rule Up")
        }
        IconButton(enabled = i != productionRules.lastIndex, onClick = {
            productionRules.add(i + 1, productionRules.removeAt(i))
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Rule Down")
        }
    }
    //BEGIN Content of RulesScreen Composable
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LabeledSection(LocalContext.current.getString(R.string.rules_label_axiom)) {
            Row(Modifier.fillMaxWidth()) {
                AccursedText(axiom)
            }
            if (editingRow.value == EDITING_AXIOM) EditRow()
        }
        LabeledSection(LocalContext.current.getString(R.string.rules_label_productions)) {
            productionRules.forEachIndexed { iRule, pr ->
                Row(Modifier.fillMaxWidth()) {
                    AccursedText(pr.before)
                    Icon(Icons.Default.ArrowForward, contentDescription = "Arrow")
                    AccursedText(
                        pr.after, Modifier
                            .weight(1f)
                            .width(IntrinsicSize.Max)
                    )
                    if (reorderDeleteButtonsVisible) {
                        Spacer(Modifier.weight(.01f))
                        Row(Modifier.align(Alignment.CenterVertically)) {
                            ReorderDeleteButtons(iRule)
                        }
                    }
                }
                if(editingRow.value == iRule) EditRow()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { productionRules.add(Specification.Production("", "")) }
                ) {
                    Text(LocalContext.current.getString(R.string.rules_btn_add))
                }
                Button(
                    onClick = { reorderDeleteButtonsVisible = !reorderDeleteButtonsVisible }
                ) {
                    Text(LocalContext.current.getString(R.string.rules_btn_alter))
                }
                Button(
                    onClick = {  /*TODO: Handle */ }
                ) {
                    Text(
                        LocalContext.current.getString(R.string.rules_btn_compile),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
