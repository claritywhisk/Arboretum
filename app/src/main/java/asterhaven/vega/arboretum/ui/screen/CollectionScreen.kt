package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import asterhaven.vega.arboretum.lsystems.Systems
import asterhaven.vega.arboretum.lsystems.TreeLSystem

@Composable
fun CollectionScreen(
    selected : TreeLSystem.Specification,
    onSelectSpecification: (TreeLSystem.Specification) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = selected.name,
            modifier = Modifier.clickable {
                expanded = !expanded
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Systems.list.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = { onSelectSpecification(it) }
                )
            }
        }
    }
}