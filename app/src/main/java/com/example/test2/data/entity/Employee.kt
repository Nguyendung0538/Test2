package com.example.test2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Employee",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["accountId"], unique = true)
    ]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val fullname: String,
    val email: String,
    val phone: String,
    val hourlyRate: Double,
    val accountId: Int
)