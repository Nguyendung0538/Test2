package com.example.test2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test2.data.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: Account): Long

    @Query("SELECT * FROM Account WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun findByUsername(username: String): Account?

    @Query("SELECT * FROM Account WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): Account?

    @Query("SELECT * FROM Account ORDER BY username")
    fun observeAll(): Flow<List<Account>>

    @Query("DELETE FROM Account WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}