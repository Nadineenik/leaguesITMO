package com.example.leaguesitmo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SavedSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: SavedSearch)

    @Query("SELECT * FROM saved_searches ORDER BY timestamp DESC LIMIT 10")
    suspend fun getLast10(): List<SavedSearch>

    // Опционально: очистка старых записей (если нужно)
    @Query("DELETE FROM saved_searches WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}