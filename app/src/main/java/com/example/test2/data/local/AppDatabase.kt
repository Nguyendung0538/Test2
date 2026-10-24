package com.example.test2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test2.data.dao.AccountDao
import com.example.test2.data.dao.EmployeeDao
import com.example.test2.data.dao.SalaryDao
import com.example.test2.data.dao.WorkLogDao
import com.example.test2.data.entity.Account
import com.example.test2.data.entity.Employee
import com.example.test2.data.entity.Salary
import com.example.test2.data.entity.WorkLog

@Database(
    entities = [Account::class, Employee::class, WorkLog::class, Salary::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun workLogDao(): WorkLogDao
    abstract fun salaryDao(): SalaryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payroll_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}