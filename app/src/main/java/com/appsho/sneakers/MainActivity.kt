package com.appsho.sneakers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appsho.sneakers.ui.MainNavigationViewModel
import com.appsho.sneakers.ui.RunGearNavHost
import com.appsho.sneakers.ui.ThemeViewModel
import com.appsho.sneakers.ui.theme.CanvasBg
import com.appsho.sneakers.ui.theme.DarkBg
import com.appsho.sneakers.ui.theme.DarkSurface
import com.appsho.sneakers.ui.theme.RunGearTheme
import com.appsho.sneakers.ui.theme.SurfaceWhite
import com.appsho.sneakers.util.ShareIntentParser

class MainActivity : ComponentActivity() {

    private var incomingShareUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queueShareFromIntent(intent)

        val app = application as RunGearApplication

        setContent {
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModel.Factory(app.themePreferences)
            )
            val darkMode by themeViewModel.darkMode.collectAsState()
            val navViewModel: MainNavigationViewModel = viewModel()
            val shareUri = incomingShareUri

            LaunchedEffect(shareUri) {
                shareUri?.let { uri ->
                    navViewModel.acceptSharedImage(uri)
                    incomingShareUri = null
                }
            }

            SideEffect {
                val statusScrim = if (darkMode) DarkBg.toArgb() else CanvasBg.toArgb()
                val navScrim = if (darkMode) DarkSurface.toArgb() else SurfaceWhite.toArgb()
                enableEdgeToEdge(
                    statusBarStyle = if (darkMode) {
                        SystemBarStyle.dark(statusScrim)
                    } else {
                        SystemBarStyle.light(statusScrim, statusScrim)
                    },
                    navigationBarStyle = if (darkMode) {
                        SystemBarStyle.dark(navScrim)
                    } else {
                        SystemBarStyle.light(navScrim, navScrim)
                    }
                )
            }

            RunGearTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RunGearNavHost(
                        app = app,
                        themeViewModel = themeViewModel,
                        navViewModel = navViewModel,
                        currentTab = navViewModel.currentTab,
                        onTabChange = navViewModel::setTab
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        queueShareFromIntent(intent)
    }

    private fun queueShareFromIntent(intent: Intent?) {
        ShareIntentParser.parseImageUri(intent)?.let { uri ->
            incomingShareUri = uri
        }
    }
}
