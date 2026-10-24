package com.example.test2.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(dateFormatter)
    fun currentMonth(): Int = LocalDate.now().monthValue
    fun currentYear(): Int = LocalDate.now().year
    fun monthStart(month: Int, year: Int): String =
        YearMonth.of(year, month).atDay(1).format(dateFormatter)
    fun monthEnd(month: Int, year: Int): String =
        YearMonth.of(year, month).atEndOfMonth().format(dateFormatter)
}