package com.example.category3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.category3.auth.ui.AppNavigation
import com.example.category3.auth.ui.SplashVideoScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Configure the window architecture to span edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            // Hide both the Status Bar (top clock/battery) and Navigation Bar (bottom buttons/line)
            hide(WindowInsetsCompat.Type.systemBars())

            // If the user swipes from an edge, bars show up temporarily as translucent overlays, then auto-vanish
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 🔒 SECURE LOCKDOWN: Intercept system back gestures natively
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do absolutely nothing inside this block.
                // This permanently intercepts and drops all system back swipes/buttons just like an exam kiosk.
            }
        })

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigationCoreFramework()
            }
        }
    }
}

// 🎬 SPLASH SCREEN STATE TRANSITION LAYER
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigationCoreFramework() {
    var isSplashActive by remember { mutableStateOf(true) }

    if (isSplashActive) {
        SplashVideoScreen(
            onVideoFinished = {
                isSplashActive = false // Latch passes over tracking controls to our navigation engine handler
            }
        )
    } else {
        // 🚀 ROUTER INJECTED SUCCESSFULLY: Switched from a single screen to the master application navigation layout
        AppNavigation()
    }
}