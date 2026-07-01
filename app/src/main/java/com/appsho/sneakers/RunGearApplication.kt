package com.appsho.sneakers

import android.app.Application
import com.appsho.sneakers.data.AppDatabase
import com.appsho.sneakers.data.SneakerRepository
import com.appsho.sneakers.data.ThemePreferences

class RunGearApplication : Application() {
    val repository: SneakerRepository by lazy {
        SneakerRepository(
            dao = AppDatabase.getInstance(this).sneakerDao(),
            context = this
        )
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this)
    }
}
