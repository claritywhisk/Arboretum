package asterhaven.vega.arboretum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import asterhaven.vega.arboretum.ui.theme.ArboretumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArboretumTheme {
                AApp()
            }
        }
    }
}
