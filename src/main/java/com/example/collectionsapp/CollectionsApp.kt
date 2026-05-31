package com.example.collectionsapp

import android.app.Application
import kotlin.getValue
import com.example.collectionsapp.data.database.AppDatabase

class CollectionsApp : Application() {
    val database by lazy {
        AppDatabase.getDatabase(this)
    }
}