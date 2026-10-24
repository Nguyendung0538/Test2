package com.example.test2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Account", indices = [Index(value = ["username"], unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    /** SHA-256 hex digest; never store a plain-text password. */
    val password: String,
    /** ADMIN or EMPLOYEE */
    val role: String
)