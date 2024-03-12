package asterhaven.vega.arboretum

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.components.ArboretumTabRow
import asterhaven.vega.arboretum.ui.screen.ParamsScreen
import asterhaven.vega.arboretum.ui.screen.WorldScreen

enum class ArboretumScreen(@StringRes val title: Int, @DrawableRes val icon: Int) {
    World(R.string.app_name, R.drawable.baseline_forest_24),
    Parameters(R.string.parameters_title, R.drawable.baseline_edit_24)
    //Rules(title = R.string.rules_title),
    //Collection(title = R.string.collection_title)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArboretumApp(
    modifier: Modifier = Modifier,
    viewModel: ArboretumViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    var currentScreen by remember { mutableStateOf(ArboretumScreen.World) }
    val lSystem = viewModel.lSystem.collectAsState().value
    Scaffold(
        topBar = {
            ArboretumTabRow(
                onTabSelected = { newScreen ->
                    navController.navigate(newScreen.name)
                    currentScreen = newScreen
                },
                currentScreen = currentScreen
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ArboretumScreen.World.name,
            modifier = modifier
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            composable(route = ArboretumScreen.World.name){
                WorldScreen(viewModel.worldDrawings)
            }
            composable(route = ArboretumScreen.Parameters.name){
                ParamsScreen(viewModel.params, lSystem)
            }
        }
    }
}