package asterhaven.vega.arboretum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import asterhaven.vega.arboretum.ui.theme.ArboretumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            System.setProperty(
                kotlinx.coroutines.DEBUG_PROPERTY_NAME,
                kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
            )
        }
        setContent {
            ArboretumTheme {
                ArboretumApp()
            }
        }
    }
}
