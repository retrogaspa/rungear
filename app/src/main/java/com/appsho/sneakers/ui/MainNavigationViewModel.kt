package com.appsho.sneakers.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainNavigationViewModel : ViewModel() {
    var currentTab by mutableStateOf(AppTab.COMPOSE)
        private set

    var pendingShareImageUri by mutableStateOf<Uri?>(null)
        private set

    fun setTab(tab: AppTab) {
        currentTab = tab
    }

    fun acceptSharedImage(uri: Uri) {
        pendingShareImageUri = uri
        currentTab = AppTab.COMPOSE
    }

    fun clearPendingShareImage() {
        pendingShareImageUri = null
    }
}
