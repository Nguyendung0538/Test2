package com.example.test2.data.local

import androidx.room.withTransaction
import com.example.test2.data.entity.Account
import com.example.test2.data.entity.Employee
import com.example.test2.data.entity.Salary
import com.example.test2.data.entity.WorkLog
import com.example.test2.domain.SalaryCalculator
import com.example.test2.utils.DateTimeUtils
import com.example.test2.utils.HashUtils
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

object DatabaseSeeder {
    /** Called for database seeding. Inserts 4 demo employees, work logs, and monthly salary records. */
    fun seed(database: AppDatabase) = runBlocking {
        database.withTransaction {
            val accountDao = database.accountDao()
            val employeeDao = database.employeeDao()
            val workLogDao = database.workLogDao()
            val salaryDao = database.salaryDao()

            if (accountDao.findByUsername("admin") == null) {
                accountDao.insert(Account(username = "admin", password = HashUtils.sha256("admin123"), role = "ADMIN"))
            }

            val currentMonth = DateTimeUtils.currentMonth()
            val currentYear = DateTimeUtils.currentYear()

            val demos = listOf(
                Demo("NV001", "Nguyen Van An", "an@example.com", "0901234567", 50000.0, "employee1", "123456", "PENDING", otDays = 1),
                Demo("NV002", "Tran Thi Binh", "binh@example.com", "0912345678", 55000.0, "employee2", "123456", "PENDING", otDays = 0),
                Demo("NV003", "Le Van Cuong", "cuong@example.com", "0923456789", 60000.0, "employee3", "123456", "PAID", otDays = 2),
                Demo("NV004", "Pham Thi Dung", "dung@example.com", "0934567890", 52000.0, "employee4", "123456", "PENDING", otDays = 0)
            )

            demos.forEach { d ->
                var account = accountDao.findByUsername(d.username)
                if (account == null) {
                    val accountId = accountDao.insert(
                        Account(username = d.username, password = HashUtils.sha256(d.password), role = "EMPLOYEE")
                    ).toInt()
                    account = accountDao.findById(accountId)
                }

                val accountId = account!!.id
                var employee = employeeDao.findByAccountId(accountId)
                if (employee == null) {
                    val employeeId = employeeDao.insert(
                        Employee(
                            code = d.code, fullname = d.fullname, email = d.email,
                            phone = d.phone, hourlyRate = d.hourlyRate, accountId = accountId
                        )
                    ).toInt()
                    employee = employeeDao.findById(employeeId)
                }

                val employeeId = employee!!.id

                // Five completed demo shifts per employee in the current month.
                val today = LocalDate.now()
                val days = (1..today.dayOfMonth).filter { day ->
                    LocalDate.of(today.year, today.monthValue, day).dayOfWeek.value <= 5
                }.takeLast(5)

                days.forEachIndexed { index, day ->
                    val date = LocalDate.of(today.year, today.monthValue, day)
                    val dateStr = date.toString()
                    if (workLogDao.findForDate(employeeId, dateStr) == null) {
                        val start = date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val hours = if (index < d.otDays) 10.0 else 8.0
                        val end = start + (hours * 3_600_000L).toLong()
                        workLogDao.insert(
                            WorkLog(
                                employeeId = employeeId,
                                workDate = dateStr,
                                checkInTime = start,
                                checkOutTime = end,
                                totalHours = hours,
                                status = "COMPLETED"
                            )
                        )
                    }
                }

                // Seed monthly salary record if not already present
                if (salaryDao.findForMonth(employeeId, currentMonth, currentYear) == null) {
                    val fromDate = DateTimeUtils.monthStart(currentMonth, currentYear)
                    val toDate = DateTimeUtils.monthEnd(currentMonth, currentYear)
                    val completedLogs = workLogDao.completedForPeriod(employeeId, fromDate, toDate)
                    val salaryResult = SalaryCalculator.calculate(completedLogs, d.hourlyRate)

                    salaryDao.insert(
                        Salary(
                            employeeId = employeeId,
                            month = currentMonth,
                            year = currentYear,
                            totalHours = salaryResult.totalHours,
                            overtimeHours = salaryResult.overtimeHours,
                            totalSalary = salaryResult.totalSalary,
                            status = d.status
                        )
                    )
                }
            }
        }
    }

    private data class Demo(
        val code: String,
        val fullname: String,
        val email: String,
        val phone: String,
        val hourlyRate: Double,
        val username: String,
        val password: String,
        val status: String,
        val otDays: Int
    )
}