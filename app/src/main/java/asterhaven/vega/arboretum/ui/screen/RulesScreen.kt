package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.TreeLSystem
import asterhaven.vega.arboretum.ui.components.LabeledTextField

@Composable
fun RulesScreen(){
    // TODO: Add ViewModel logic
    
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LabeledTextField(
            label = LocalContext.current.getString(R.string.label_axiom),
            text = "todo"){
            //update
        }
    }
}