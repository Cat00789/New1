package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthSyncApplication : Application() {

    // Simple dependency injection container
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { HealthRepository(database.healthDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize default user profile if none exists
        CoroutineScope(Dispatchers.IO).launch {
            repository.initDefaultProfileIfNeeded()
            repository.initDefaultUsersIfNeeded()
        }
    }
}
