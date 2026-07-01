package com.appsho.sneakers.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _collectionGridColumns = MutableStateFlow(
        prefs.getInt(KEY_COLLECTION_GRID_COLUMNS, DEFAULT_GRID_COLUMNS).coerceIn(1, 4)
    )
    val collectionGridColumns: StateFlow<Int> = _collectionGridColumns.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _darkMode.value = enabled
    }

    fun setCollectionGridColumns(columns: Int) {
        val safe = columns.coerceIn(1, 4)
        prefs.edit().putInt(KEY_COLLECTION_GRID_COLUMNS, safe).apply()
        _collectionGridColumns.value = safe
    }

    companion object {
        private const val PREFS_NAME = "rungear_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_COLLECTION_GRID_COLUMNS = "collection_grid_columns"
        const val DEFAULT_GRID_COLUMNS = 2
    }
}
