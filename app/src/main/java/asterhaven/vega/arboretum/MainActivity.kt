package asterhaven.vega.arboretum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import asterhaven.vega.arboretum.scene.SceneViewModel
import asterhaven.vega.arboretum.ui.theme.ArboretumTheme
import asterhaven.vega.arboretum.graphics.ArboretumGLSurfaceView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArboretumTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(model: SceneViewModel = viewModel()) {
    AndroidView(factory = ::ArboretumGLSurfaceView,
        modifier = Modifier.fillMaxSize(),
        update = {
            it.updateState(model.drawingsState)
        }
    )
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArboretumTheme {
        Greeting("Planter")
    }
}