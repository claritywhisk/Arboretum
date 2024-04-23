package asterhaven.vega.arboretum.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.ui.components.ArboretumTabRow
import asterhaven.vega.arboretum.ui.screen.CollectionScreen
import asterhaven.vega.arboretum.ui.screen.ParamsScreen
import asterhaven.vega.arboretum.ui.screen.RulesScreen
import asterhaven.vega.arboretum.ui.screen.WorldScreen

enum class ArboretumScreen(@StringRes val title: Int, @DrawableRes val icon: Int) {
    World(R.string.app_name, R.drawable.baseline_circle_24),
    Parameters(R.string.parameters_title, R.drawable.baseline_park_24),
    Rules(R.string.rules_title, R.drawable.baseline_edit_24),
    Collection(R.string.collection_title, R.drawable.baseline_forest_24)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArboretumApp(
    modifier: Modifier = Modifier,
    viewModel: ArboretumViewModel,
    navController: NavHostController = rememberNavController()
) {
    val startTab = ArboretumScreen.Parameters
    var currentScreen by remember { mutableStateOf(startTab) }

    fun navigateTo(newScreen : ArboretumScreen) {
        navController.navigate(newScreen.name)
        currentScreen = newScreen
    }

    Scaffold(
        topBar = {
            ArboretumTabRow(
                onTabSelected = ::navigateTo,
                currentScreen = currentScreen
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startTab.name,
            modifier = modifier
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            composable(route = ArboretumScreen.World.name){
                WorldScreen(viewModel.worldDrawings.value)
            }
            composable(route = ArboretumScreen.Parameters.name){
                ParamsScreen(viewModel.params.value, viewModel.lSystem.value!!, { steps ->
                    navigateTo(ArboretumScreen.World)
                    viewModel.populateAction(steps)
                })
            }
            composable(route = ArboretumScreen.Rules.name){
                RulesScreen(viewModel.specification.value!!)
            }
            composable(route = ArboretumScreen.Collection.name){
                CollectionScreen(viewModel.specification.value!!, viewModel::updateSpecification)
            }
        }
    }
}

