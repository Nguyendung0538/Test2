package com.example.test2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.entity.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee): Int

    @Query("SELECT * FROM Employee WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): Employee?

    @Query("SELECT * FROM Employee WHERE accountId = :accountId LIMIT 1")
    suspend fun findByAccountId(accountId: Int): Employee?

    @Query("SELECT * FROM Employee WHERE code = :code COLLATE NOCASE LIMIT 1")
    suspend fun findByCode(code: String): Employee?

    @Query("""
        SELECT * FROM Employee
        WHERE fullname LIKE '%' || :query || '%'
           OR code LIKE '%' || :query || '%'
           OR email LIKE '%' || :query || '%'
        ORDER BY fullname
    """)
    fun search(query: String): Flow<List<Employee>>

    @Query("SELECT * FROM Employee ORDER BY fullname")
    fun observeAll(): Flow<List<Employee>>

    @Query("DELETE FROM Employee WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}