package com.example.leaguesitmo.data

import androidx.room.RoomDatabase
import androidx.room.Database
import com.example.leaguesitmo.navigation.Screen

@Database(entities = [User::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}