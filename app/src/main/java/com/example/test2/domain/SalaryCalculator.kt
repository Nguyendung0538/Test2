/**
 * Thuật toán tính toán tiền lương theo công thức quy chuẩn.
 * Tách giờ làm việc chuẩn (tối đa 8h/ngày) và giờ tăng ca OT (nhân hệ số 1.5).
 * Chỉ tính toán dựa trên các bản ghi WorkLog có trạng thái COMPLETED.
 */
package com.example.test2.domain

import com.example.test2.data.entity.WorkLog
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object SalaryCalculator {

    const val STANDARD_DAILY_HOURS = 8.0
    const val OT_MULTIPLIER = 1.5

    data class SalaryResult(
        val regularHours: Double,
        val overtimeHours: Double,
        val totalHours: Double,
        val regularSalary: Double,
        val overtimeSalary: Double,
        val totalSalary: Double
    )

    fun calculate(workLogs: List<WorkLog>, hourlyRate: Double): SalaryResult {
        if (workLogs.isEmpty() || hourlyRate <= 0) {
            return SalaryResult(
                regularHours = 0.0,
                overtimeHours = 0.0,
                totalHours = 0.0,
                regularSalary = 0.0,
                overtimeSalary = 0.0,
                totalSalary = 0.0
            )
        }

        // Chỉ tính các ca đã hoàn thành (status == "COMPLETED")
        val completedLogs = workLogs.filter { it.status == "COMPLETED" }

        var totalRegularHours = 0.0
        var totalOvertimeHours = 0.0

        for (log in completedLogs) {
            val dailyHours = max(0.0, log.totalHours)
            val regular = min(dailyHours, STANDARD_DAILY_HOURS)
            val overtime = max(0.0, dailyHours - STANDARD_DAILY_HOURS)

            totalRegularHours += regular
            totalOvertimeHours += overtime
        }

        val totalHours = totalRegularHours + totalOvertimeHours
        val regularSalary = roundToTwoDecimals(totalRegularHours * hourlyRate)
        val overtimeSalary = roundToTwoDecimals(totalOvertimeHours * hourlyRate * OT_MULTIPLIER)
        val totalSalary = roundToTwoDecimals(regularSalary + overtimeSalary)

        return SalaryResult(
            regularHours = roundToTwoDecimals(totalRegularHours),
            overtimeHours = roundToTwoDecimals(totalOvertimeHours),
            totalHours = roundToTwoDecimals(totalHours),
            regularSalary = regularSalary,
            overtimeSalary = overtimeSalary,
            totalSalary = totalSalary
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}