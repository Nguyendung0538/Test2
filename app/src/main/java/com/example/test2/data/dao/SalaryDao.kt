package com.example.test2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.entity.Salary
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(salary: Salary): Long

    @Update
    suspend fun update(salary: Salary): Int

    @Query("SELECT * FROM Salary WHERE employeeId = :employeeId AND month = :month AND year = :year LIMIT 1")
    suspend fun findForMonth(employeeId: Int, month: Int, year: Int): Salary?

    @Query("SELECT * FROM Salary WHERE employeeId = :employeeId ORDER BY year DESC, month DESC")
    fun observeForEmployee(employeeId: Int): Flow<List<Salary>>

    @Query("SELECT * FROM Salary WHERE month = :month AND year = :year ORDER BY employeeId")
    fun observeForMonth(month: Int, year: Int): Flow<List<Salary>>

    @Query("SELECT * FROM Salary WHERE employeeId = :employeeId AND month = :month AND year = :year LIMIT 1")
    fun observeOne(employeeId: Int, month: Int, year: Int): Flow<Salary?>

    @Query("UPDATE Salary SET status = 'PAID' WHERE id = :salaryId AND status = 'PENDING'")
    suspend fun markPaid(salaryId: Int): Int
}