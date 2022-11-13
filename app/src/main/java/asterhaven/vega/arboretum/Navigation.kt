package asterhaven.vega.arboretum

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import asterhaven.vega.arboretum.ui.ArboretumViewModel
import asterhaven.vega.arboretum.ui.screen.ParamsScreen
import asterhaven.vega.arboretum.ui.screen.WorldScreen

enum class ArboretumScreen(@StringRes val title: Int) {
    World(title = R.string.app_name),
    Parameters(title = R.string.parameters_title),
    Rules(title = R.string.rules_title),
    Collection(title = R.string.collection_title)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArboretumAppBar(
    currentScreen: ArboretumScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AApp(
    modifier: Modifier = Modifier,
    viewModel: ArboretumViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = ArboretumScreen.valueOf(
        backStackEntry?.destination?.route ?: ArboretumScreen.World.name
    )

    Scaffold(
        topBar = {
            ArboretumAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        println("paddington" + innerPadding)
        NavHost(
            navController = navController,
            startDestination = ArboretumScreen.Parameters.name,
            modifier = modifier.padding(innerPadding).navigationBarsPadding()
        ) {
            /*composable(route = ArboretumScreen.World.name){
                WorldScreen(viewModel.worldDrawings)
            }*/
            composable(route = ArboretumScreen.Parameters.name){
                ParamsScreen(viewModel.params, viewModel.lSystem)
            }
        }
    }
}