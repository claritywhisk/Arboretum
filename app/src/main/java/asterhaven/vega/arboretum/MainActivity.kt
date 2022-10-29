package asterhaven.vega.arboretum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import asterhaven.vega.arboretum.scene.SceneViewModel
import asterhaven.vega.arboretum.ui.theme.ArboretumTheme
import asterhaven.vega.graphics.ArboretumGLSurfaceView

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
fun MainScreen(model: SceneViewModel = SceneViewModel) {
    AndroidView(factory = ArboretumGLSurfaceView.Companion::manufacture,
        modifier = Modifier.fillMaxSize(),
        update = {
            it.updateState(model.drawingsState)
        }
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Greeting("Android")
    }
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