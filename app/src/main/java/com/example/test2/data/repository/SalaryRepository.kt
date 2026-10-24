/**
 * Repository quản lý dữ liệu Bảng lương toàn công ty.
 * Tích hợp thuật toán SalaryCalculator để tự động tính toán lương và cập nhật trạng thái thanh toán.
 */
package com.example.test2.data.repository

import androidx.room.withTransaction
import com.example.test2.data.entity.Salary
import com.example.test2.data.entity.SalaryWithEmployee
import com.example.test2.data.local.AppDatabase
import com.example.test2.domain.SalaryCalculator
import com.example.test2.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SalaryRepository(private val database: AppDatabase) {

    private val salaryDao = database.salaryDao()
    private val employeeDao = database.employeeDao()
    private val workLogDao = database.workLogDao()

    /** Tra cứu danh sách bảng lương kèm thông tin nhân viên theo Tháng/Năm. */
    fun getSalariesByMonth(month: Int, year: Int): Flow<List<SalaryWithEmployee>> {
        return salaryDao.observeSalariesWithEmployee(month, year)
    }

    /**
     * Tính toán hoặc cập nhật lại bản ghi bảng lương tháng của 1 nhân viên.
     * Quy tắc: Bản ghi đã chuyển sang trạng thái "PAID" sẽ không bị ghi đè.
     */
    suspend fun upsertMonthlySalary(employeeId: Int, month: Int, year: Int) {
        database.withTransaction {
            val employee = employeeDao.findById(employeeId) ?: return@withTransaction
            val existingSalary = salaryDao.findForMonth(employeeId, month, year)

            // Khóa bản ghi nếu đã thanh toán (PAID)
            if (existingSalary?.status == "PAID") return@withTransaction

            // Lấy toàn bộ WorkLog COMPLETED trong tháng
            val fromDate = DateTimeUtils.monthStart(month, year)
            val toDate = DateTimeUtils.monthEnd(month, year)
            val completedLogs = workLogDao.completedForPeriod(employeeId, fromDate, toDate)

            // Gọi thuật toán tính toán lương chuẩn
            val result = SalaryCalculator.calculate(completedLogs, employee.hourlyRate)

            if (existingSalary == null) {
                salaryDao.insert(
                    Salary(
                        employeeId = employeeId,
                        month = month,
                        year = year,
                        totalHours = result.totalHours,
                        overtimeHours = result.overtimeHours,
                        totalSalary = result.totalSalary,
                        status = "PENDING"
                    )
                )
            } else {
                salaryDao.update(
                    existingSalary.copy(
                        totalHours = result.totalHours,
                        overtimeHours = result.overtimeHours,
                        totalSalary = result.totalSalary
                    )
                )
            }
        }
    }

    /**
     * Quét và tính toán lại bảng lương tháng cho toàn bộ nhân viên trong công ty.
     * Chỉ tính lại các bản ghi đang ở trạng thái PENDING.
     */
    suspend fun recalculateAllSalaries(month: Int, year: Int) {
        val allEmployees = employeeDao.observeAll().first()
        for (emp in allEmployees) {
            upsertMonthlySalary(emp.id, month, year)
        }
    }

    /** Đánh dấu bản ghi lương là đã thanh toán (PAID) và khóa dữ liệu. */
    suspend fun markAsPaid(salaryId: Int): Boolean {
        return salaryDao.markPaid(salaryId) > 0
    }
}