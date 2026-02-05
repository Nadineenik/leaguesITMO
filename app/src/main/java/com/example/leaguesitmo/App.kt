package com.example.leaguesitmo

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.leaguesitmo.data.AppDatabase


class App : Application() {
    companion object {
        val database: AppDatabase by lazy {
            Room.databaseBuilder(
                instance.applicationContext,
                AppDatabase::class.java,
                "league_app.db"
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }

        // Самые надёжные миграции
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN email TEXT")
            }
        }

            lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        GlobalScope.launch(Dispatchers.IO) {
            database // просто инициализируем
        }
    }
}