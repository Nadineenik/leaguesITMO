package com.example.leaguesitmo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val query: String,

    val timestamp: Long   // System.currentTimeMillis()
)