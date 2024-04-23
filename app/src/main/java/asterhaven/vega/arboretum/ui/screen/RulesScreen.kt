package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.ui.components.LabeledSection

@Composable
fun RulesScreen(
    baseSpecification : Specification //the selected spec which this screen shows/modifies
){
    var areButtonsVisible by remember { mutableStateOf(false) }
    var axiom by remember { mutableStateOf(baseSpecification.initialRaw) }
    var productionRules = remember { mutableStateListOf(*baseSpecification.productionsRaw.toTypedArray()) }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LabeledSection(LocalContext.current.getString(R.string.rules_label_axiom)){
            Text(axiom)
        }
        LabeledSection(LocalContext.current.getString(R.string.rules_label_productions)){
            productionRules.forEachIndexed { iRule, pr ->
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(pr.before)
                        Icon(Icons.Default.ArrowForward, contentDescription = "Arrow")
                        Text(pr.after)
                        if (areButtonsVisible) ReorderDeleteButtons(productionRules, iRule)
                    }
                }
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
                    onClick = { areButtonsVisible = !areButtonsVisible }
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

@Composable
fun ReorderDeleteButtons(rules : SnapshotStateList<Specification.Production>, i : Int){
    IconButton(onClick = { rules.removeAt(i) }) {
        Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
    }
    if(i > 0) IconButton(onClick = { rules.add(i - 1, rules.removeAt(i)) }) {
        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Rule Up")
    }
    if(i != rules.lastIndex) IconButton(onClick = { rules.add(i + 1, rules.removeAt(i)) }) {
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Rule Down")
    }
}