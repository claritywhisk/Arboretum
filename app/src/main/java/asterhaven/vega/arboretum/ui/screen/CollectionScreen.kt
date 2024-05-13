package asterhaven.vega.arboretum.ui.screen

import androidx.compose.runtime.Composable
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.Systems
import asterhaven.vega.arboretum.ui.components.ArbDropMenu

@Composable
fun CollectionScreen(
    selected : Specification,
    onSelectSpecification: (Specification) -> Unit) {
    ArbDropMenu(selected, onSelectSpecification, { spec -> spec.name }, Systems.list, true)
}