package com.appsho.sneakers.ui



import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.AutoAwesome

import androidx.compose.material.icons.outlined.GridView

import androidx.compose.material.icons.outlined.Settings

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.NavigationBar

import androidx.compose.material3.NavigationBarItem

import androidx.compose.material3.NavigationBarItemDefaults

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Snackbar

import androidx.compose.material3.SnackbarHost

import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.appsho.sneakers.RunGearApplication



@Composable

fun RunGearNavHost(

    app: RunGearApplication,

    themeViewModel: ThemeViewModel,

    currentTab: AppTab,

    onTabChange: (AppTab) -> Unit

) {

    val snackbarHostState = remember { SnackbarHostState() }



    val listViewModel: SneakerListViewModel = viewModel(

        factory = SneakerListViewModel.Factory(app.repository)

    )

    val composeViewModel: ComposeImageViewModel = viewModel(
        factory = ComposeImageViewModel.Factory(app.repository, app.applicationContext)
    )
    val gridColumns by themeViewModel.collectionGridColumns.collectAsState()



    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        snackbarHost = {

            SnackbarHost(snackbarHostState) { data ->

                Snackbar(

                    snackbarData = data,

                    shape = RoundedCornerShape(12.dp),

                    containerColor = MaterialTheme.colorScheme.inverseSurface,

                    contentColor = MaterialTheme.colorScheme.inverseOnSurface

                )

            }

        },

        bottomBar = {

            NavigationBar(

                containerColor = MaterialTheme.colorScheme.surface,

                tonalElevation = 0.dp

            ) {

                NavigationBarItem(
                    selected = currentTab == AppTab.COMPOSE,
                    onClick = { onTabChange(AppTab.COMPOSE) },
                    icon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    },
                    label = { Text(AppTab.COMPOSE.label) },
                    colors = navItemColors()
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SNEAKERS,
                    onClick = { onTabChange(AppTab.SNEAKERS) },
                    icon = {
                        Icon(Icons.Outlined.GridView, contentDescription = null)
                    },
                    label = { Text(AppTab.SNEAKERS.label) },
                    colors = navItemColors()
                )

                NavigationBarItem(

                    selected = currentTab == AppTab.SETTINGS,

                    onClick = { onTabChange(AppTab.SETTINGS) },

                    icon = {

                        Icon(Icons.Outlined.Settings, contentDescription = null)

                    },

                    label = { Text(AppTab.SETTINGS.label) },

                    colors = navItemColors()

                )

            }

        }

    ) { padding ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

        ) {

            when (currentTab) {

                AppTab.SNEAKERS -> SneakerListScreen(
                    viewModel = listViewModel,
                    snackbarHostState = snackbarHostState,
                    gridColumns = gridColumns
                )

                AppTab.COMPOSE -> ComposeImageScreen(

                    viewModel = composeViewModel,

                    snackbarHostState = snackbarHostState,

                    onNavigateToCollection = { onTabChange(AppTab.SNEAKERS) }

                )

                AppTab.SETTINGS -> SettingsScreen(viewModel = themeViewModel)

            }

        }

    }

}



@Composable

private fun navItemColors() = NavigationBarItemDefaults.colors(

    selectedIconColor = MaterialTheme.colorScheme.primary,

    selectedTextColor = MaterialTheme.colorScheme.primary,

    indicatorColor = MaterialTheme.colorScheme.primaryContainer,

    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

)

