package com.example.test2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "WorkLog",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId", "workDate"], unique = true)]
)
data class WorkLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    /** yyyy-MM-dd */
    val workDate: String,
    /** Epoch milliseconds */
    val checkInTime: Long,
    /** Epoch milliseconds; null until checkout */
    val checkOutTime: Long? = null,
    val totalHours: Double = 0.0,
    /** IN_PROGRESS, COMPLETED, INCOMPLETE */
    val status: String
)
