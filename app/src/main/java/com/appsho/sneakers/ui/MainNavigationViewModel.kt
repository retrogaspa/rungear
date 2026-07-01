package com.appsho.sneakers.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** Aba inicial sempre Criar; sobrevive rotação, não persiste entre aberturas do app. */
class MainNavigationViewModel : ViewModel() {
    var currentTab by mutableStateOf(AppTab.COMPOSE)
        private set

    fun setTab(tab: AppTab) {
        currentTab = tab
    }
}
