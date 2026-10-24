package com.example.test2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Salary",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId", "month", "year"], unique = true)]
)
data class Salary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val month: Int,
    val year: Int,
    val totalHours: Double,
    val overtimeHours: Double,
    val totalSalary: Double,
    /** PENDING or PAID */
    val status: String = "PENDING"
)