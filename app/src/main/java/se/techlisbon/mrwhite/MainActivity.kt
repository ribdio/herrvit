package se.techlisbon.mrwhite

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val useDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val context = LocalContext.current

            val colorScheme = if (useDynamicColor) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                lightColorScheme()
            }

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography(),
                shapes = Shapes()
            ) {
                MrWhiteApp()
            }
        }
    }
}

