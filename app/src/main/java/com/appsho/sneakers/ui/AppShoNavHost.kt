package com.appsho.sneakers.ui

import androidx.compose.runtime.Composable
import com.appsho.sneakers.RunGearApplication

@Deprecated("Renomeado para RunGearNavHost")
@Composable
fun AppShoNavHost(
    app: RunGearApplication,
    currentTab: AppTab,
    onTabChange: (AppTab) -> Unit
) = RunGearNavHost(app, currentTab, onTabChange)
