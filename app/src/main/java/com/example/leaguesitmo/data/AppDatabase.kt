package com.example.leaguesitmo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        SavedSearch::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun savedSearchDao(): SavedSearchDao
}