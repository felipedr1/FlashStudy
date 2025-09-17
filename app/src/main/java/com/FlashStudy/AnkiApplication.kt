package com.FlashStudy

import android.app.Application
import com.FlashStudy.data.database.AppDatabase

class AnkiApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}