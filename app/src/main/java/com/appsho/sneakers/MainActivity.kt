package com.appsho.sneakers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.appsho.sneakers.ui.RunGearNavHost
import com.appsho.sneakers.ui.AppTab
import com.appsho.sneakers.ui.theme.RunGearTheme
import com.appsho.sneakers.ui.theme.CanvasBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = CanvasBg.toArgb(),
                darkScrim = CanvasBg.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.WHITE
            )
        )

        val app = application as RunGearApplication

        setContent {
            RunGearTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentTab by rememberSaveable { mutableStateOf(AppTab.SNEAKERS) }
                    RunGearNavHost(
                        app = app,
                        currentTab = currentTab,
                        onTabChange = { currentTab = it }
                    )
                }
            }
        }
    }
}
