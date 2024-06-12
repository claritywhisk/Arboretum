package asterhaven.vega.arboretum.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import asterhaven.vega.arboretum.data.ArbRepository
import asterhaven.vega.arboretum.lsystems.Specification

@Composable
fun CollectionScreen(
    selected: Specification,
    onSelectSpecification: (Specification) -> Unit) {
    var systemsList by remember { mutableStateOf<List<Specification>>(listOf()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val success : (List<Specification>) -> Unit = {
            systemsList = it
            loading = false
        }
        val failure : (Exception) -> Unit = {
            error = "Error getting documents: $it"
            loading = false
        }
        ArbRepository.getSystemsFromDB(success, failure)
    }
    if(loading || error != null) Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if(loading) CircularProgressIndicator()
        error?.let { Text(it) }
    }
    else LazyColumn {
        items(systemsList) {
            collectionMenuItem(it) {
                onSelectSpecification(it)
            }
        }
    }
}

@Composable
private fun collectionMenuItem(cs : Specification, onClick : () -> Unit) {
    Row(
        Modifier.clickable { onClick() }
    ) {
        Text(cs.name)
        //todo metadata
        //todo preview glsurfaceview
    }
}