package com.example.test2.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class CalculationResult(
    val totalHours: Double,
    val regularHours: Double,
    val overtimeHours: Double
)

object WorkHoursCalculator {

    /**
     * Tính tổng số giờ làm việc, giờ chuẩn và giờ OT từ timestamp check-in & check-out.
     * Làm tròn 2 chữ số thập phân.
     */
    fun calculate(checkInTime: Long, checkOutTime: Long): CalculationResult {
        if (checkOutTime <= checkInTime) {
            throw IllegalArgumentException("Thời gian Check-out phải lớn hơn thời gian Check-in!")
        }

        val diffMillis = checkOutTime - checkInTime
        val rawHours = diffMillis / 3_600_000.0
        val totalHours = round(rawHours * 100.0) / 100.0

        val regularHours = min(totalHours, 8.0)
        val overtimeHours = max(totalHours - 8.0, 0.0)

        return CalculationResult(
            totalHours = totalHours,
            regularHours = round(regularHours * 100.0) / 100.0,
            overtimeHours = round(overtimeHours * 100.0) / 100.0
        )
    }
}
