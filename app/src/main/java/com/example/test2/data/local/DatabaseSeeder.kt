package com.example.test2.data.local

import androidx.room.withTransaction
import com.example.test2.data.entity.Account
import com.example.test2.data.entity.Employee
import com.example.test2.data.entity.WorkLog
import com.example.test2.utils.DateTimeUtils
import com.example.test2.utils.HashUtils
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import java.time.ZoneId

object DatabaseSeeder {
    /** Called only for a newly-created database. Inserts demo users and current-month logs. */
    fun seed(database: AppDatabase) = runBlocking {
        database.withTransaction {
            val accountDao = database.accountDao()
            val employeeDao = database.employeeDao()
            val workLogDao = database.workLogDao()

            if (accountDao.findByUsername("admin") == null) {
                accountDao.insert(Account(username = "admin", password = HashUtils.sha256("admin123"), role = "ADMIN"))
            }

            val demos = listOf(
                Demo("NV001", "Nguyen Van An", "an@example.com", "0901234567", 50000.0, "employee1", "123456"),
                Demo("NV002", "Tran Thi Binh", "binh@example.com", "0912345678", 55000.0, "employee2", "123456"),
                Demo("NV003", "Le Van Cuong", "cuong@example.com", "0923456789", 60000.0, "employee3", "123456")
            )

            demos.forEach { d ->
                if (accountDao.findByUsername(d.username) == null) {
                    val accountId = accountDao.insert(
                        Account(username = d.username, password = HashUtils.sha256(d.password), role = "EMPLOYEE")
                    ).toInt()
                    val employeeId = employeeDao.insert(
                        Employee(
                            code = d.code, fullname = d.fullname, email = d.email,
                            phone = d.phone, hourlyRate = d.hourlyRate, accountId = accountId
                        )
                    ).toInt()

                    // Five completed demo shifts per employee in the current month.
                    val today = LocalDate.now()
                    val days = (1..today.dayOfMonth).filter { day ->
                        LocalDate.of(today.year, today.monthValue, day).dayOfWeek.value <= 5
                    }.takeLast(5)
                    days.forEachIndexed { index, day ->
                        val date = LocalDate.of(today.year, today.monthValue, day)
                        val start = date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val hours = if (index == 0) 9.0 else 8.0
                        val end = start + (hours * 3_600_000L).toLong()
                        workLogDao.insert(
                            WorkLog(
                                employeeId = employeeId, workDate = date.toString(),
                                checkInTime = start, checkOutTime = end, totalHours = hours,
                                status = "COMPLETED"
                            )
                        )
                    }
                }
            }
        }
    }

    private data class Demo(
        val code: String, val fullname: String, val email: String, val phone: String,
        val hourlyRate: Double, val username: String, val password: String
    )
}