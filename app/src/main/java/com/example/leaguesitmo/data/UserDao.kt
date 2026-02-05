package com.example.leaguesitmo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}
