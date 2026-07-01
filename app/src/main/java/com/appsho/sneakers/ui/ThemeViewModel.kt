package com.appsho.sneakers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appsho.sneakers.data.ThemePreferences
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val darkMode: StateFlow<Boolean> = themePreferences.darkMode

    fun setDarkMode(enabled: Boolean) {
        themePreferences.setDarkMode(enabled)
    }

    class Factory(
        private val themePreferences: ThemePreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThemeViewModel(themePreferences) as T
        }
    }
}
