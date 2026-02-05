package com.example.leaguesitmo


import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.leaguesitmo.data.AppDatabase

class App : Application() {
    companion object {
        val database: AppDatabase by lazy {
            // Это выполнится при первом обращении
            Room.databaseBuilder(
                instance.applicationContext,
                AppDatabase::class.java,
                "student_app.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Предзагрузка (опционально)
        GlobalScope.launch(Dispatchers.IO) {
            database // просто вызов, чтобы инициализировать
        }
    }
}