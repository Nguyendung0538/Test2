package com.example.test2

import com.example.test2.domain.WorkHoursCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WorkHoursCalculatorTest {

    @Test
    fun testRegularHoursUnder8() {
        val checkIn = 1000L
        val checkOut = checkIn + (5 * 3600 * 1000L) // 5 hours
        val result = WorkHoursCalculator.calculate(checkIn, checkOut)

        assertEquals(5.0, result.totalHours, 0.001)
        assertEquals(5.0, result.regularHours, 0.001)
        assertEquals(0.0, result.overtimeHours, 0.001)
    }

    @Test
    fun testOvertimeHoursOver8() {
        val checkIn = 1000L
        val checkOut = checkIn + (10 * 3600 * 1000L) // 10 hours
        val result = WorkHoursCalculator.calculate(checkIn, checkOut)

        assertEquals(10.0, result.totalHours, 0.001)
        assertEquals(8.0, result.regularHours, 0.001)
        assertEquals(2.0, result.overtimeHours, 0.001)
    }

    @Test
    fun testInvalidCheckOutTimeThrowsException() {
        val checkIn = 5000L
        val checkOut = 3000L

        assertThrows(IllegalArgumentException::class.java) {
            WorkHoursCalculator.calculate(checkIn, checkOut)
        }
    }
}
