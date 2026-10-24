/**
 * Unit Test kiểm thử thuật toán tính lương SalaryCalculator.
 */
package com.example.test2.domain

import com.example.test2.data.entity.WorkLog
import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryCalculatorTest {

    @Test
    fun calculate_standard8Hours_returnsZeroOvertime() {
        val logs = listOf(
            WorkLog(
                id = 1,
                employeeId = 100,
                workDate = "2025-03-01",
                checkInTime = 1000000L,
                checkOutTime = 1000000L + (8 * 3600000L),
                totalHours = 8.0,
                status = "COMPLETED"
            )
        )
        val hourlyRate = 50000.0

        val result = SalaryCalculator.calculate(logs, hourlyRate)

        assertEquals(8.0, result.regularHours, 0.001)
        assertEquals(0.0, result.overtimeHours, 0.001)
        assertEquals(8.0, result.totalHours, 0.001)
        assertEquals(400000.0, result.regularSalary, 0.001)
        assertEquals(0.0, result.overtimeSalary, 0.001)
        assertEquals(400000.0, result.totalSalary, 0.001)
    }

    @Test
    fun calculate_overtimeHours_appliesOtMultiplier() {
        val logs = listOf(
            WorkLog(
                id = 2,
                employeeId = 100,
                workDate = "2025-03-02",
                checkInTime = 1000000L,
                checkOutTime = 1000000L + (10.5 * 3600000L).toLong(),
                totalHours = 10.5,
                status = "COMPLETED"
            )
        )
        val hourlyRate = 50000.0

        val result = SalaryCalculator.calculate(logs, hourlyRate)

        // 8h chuẩn * 50,000 = 400,000
        // 2.5h OT * 50,000 * 1.5 = 187,500
        // Tổng = 587,500
        assertEquals(8.0, result.regularHours, 0.001)
        assertEquals(2.5, result.overtimeHours, 0.001)
        assertEquals(10.5, result.totalHours, 0.001)
        assertEquals(400000.0, result.regularSalary, 0.001)
        assertEquals(187500.0, result.overtimeSalary, 0.001)
        assertEquals(587500.0, result.totalSalary, 0.001)
    }

    @Test
    fun calculate_multipleDays_accumulatesHoursAndSalary() {
        val logs = listOf(
            WorkLog(id = 1, employeeId = 100, workDate = "2025-03-01", checkInTime = 0L, totalHours = 8.0, status = "COMPLETED"),
            WorkLog(id = 2, employeeId = 100, workDate = "2025-03-02", checkInTime = 0L, totalHours = 9.0, status = "COMPLETED"),
            WorkLog(id = 3, employeeId = 100, workDate = "2025-03-03", checkInTime = 0L, totalHours = 6.0, status = "COMPLETED")
        )
        val hourlyRate = 60000.0

        val result = SalaryCalculator.calculate(logs, hourlyRate)

        // Ngày 1: 8h chuẩn, 0h OT
        // Ngày 2: 8h chuẩn, 1h OT
        // Ngày 3: 6h chuẩn, 0h OT
        // Tổng chuẩn = 22h, OT = 1h
        // Lương chuẩn = 22 * 60,000 = 1,320,000
        // Lương OT = 1 * 60,000 * 1.5 = 90,000
        // Tổng lương = 1,410,000
        assertEquals(22.0, result.regularHours, 0.001)
        assertEquals(1.0, result.overtimeHours, 0.001)
        assertEquals(23.0, result.totalHours, 0.001)
        assertEquals(1320000.0, result.regularSalary, 0.001)
        assertEquals(90000.0, result.overtimeSalary, 0.001)
        assertEquals(1410000.0, result.totalSalary, 0.001)
    }

    @Test
    fun calculate_emptyLogs_returnsZeroes() {
        val result = SalaryCalculator.calculate(emptyList(), 50000.0)

        assertEquals(0.0, result.regularHours, 0.001)
        assertEquals(0.0, result.overtimeHours, 0.001)
        assertEquals(0.0, result.totalHours, 0.001)
        assertEquals(0.0, result.totalSalary, 0.001)
    }

    @Test
    fun calculate_filtersNonCompletedStatus_ignoresInProgressAndIncomplete() {
        val logs = listOf(
            WorkLog(id = 1, employeeId = 100, workDate = "2025-03-01", checkInTime = 0L, totalHours = 8.0, status = "COMPLETED"),
            WorkLog(id = 2, employeeId = 100, workDate = "2025-03-02", checkInTime = 0L, totalHours = 5.0, status = "IN_PROGRESS"),
            WorkLog(id = 3, employeeId = 100, workDate = "2025-03-03", checkInTime = 0L, totalHours = 0.0, status = "INCOMPLETE")
        )
        val hourlyRate = 50000.0

        val result = SalaryCalculator.calculate(logs, hourlyRate)

        // Chỉ tính log 1 (8h COMPLETED)
        assertEquals(8.0, result.regularHours, 0.001)
        assertEquals(0.0, result.overtimeHours, 0.001)
        assertEquals(400000.0, result.totalSalary, 0.001)
    }
}