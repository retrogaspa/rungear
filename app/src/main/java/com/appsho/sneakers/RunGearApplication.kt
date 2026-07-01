package com.appsho.sneakers

import android.app.Application
import com.appsho.sneakers.data.AppDatabase
import com.appsho.sneakers.data.SneakerRepository

class RunGearApplication : Application() {
    val repository: SneakerRepository by lazy {
        SneakerRepository(
            dao = AppDatabase.getInstance(this).sneakerDao(),
            context = this
        )
    }
}
