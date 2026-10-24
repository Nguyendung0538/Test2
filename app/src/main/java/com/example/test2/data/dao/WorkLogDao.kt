package com.example.test2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.entity.WorkLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: WorkLog): Long

    @Update
    suspend fun update(log: WorkLog): Int

    @Query("SELECT * FROM WorkLog WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): WorkLog?

    @Query("SELECT * FROM WorkLog WHERE employeeId = :employeeId AND workDate = :date LIMIT 1")
    suspend fun findForDate(employeeId: Int, date: String): WorkLog?

    @Query("""
        SELECT * FROM WorkLog
        WHERE employeeId = :employeeId AND workDate BETWEEN :fromDate AND :toDate
        ORDER BY workDate DESC
    """)
    fun observeForPeriod(employeeId: Int, fromDate: String, toDate: String): Flow<List<WorkLog>>

    @Query("""
        SELECT * FROM WorkLog
        WHERE employeeId = :employeeId AND workDate BETWEEN :fromDate AND :toDate
          AND status = 'COMPLETED'
        ORDER BY workDate
    """)
    suspend fun completedForPeriod(employeeId: Int, fromDate: String, toDate: String): List<WorkLog>

    @Query("""
        SELECT * FROM WorkLog
        WHERE workDate < :today AND status = 'IN_PROGRESS'
    """)
    suspend fun findOldOpenLogs(today: String): List<WorkLog>

    @Query("SELECT * FROM WorkLog ORDER BY workDate DESC")
    fun observeAll(): Flow<List<WorkLog>>
}
