package com.example.test2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test2.databinding.ActivityAttendanceHistoryBinding
import com.example.test2.utils.DateTimeUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class AttendanceHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceHistoryBinding
    private lateinit var adapter: AttendanceHistoryAdapter

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val app get() = application as PayrollApplication
    private val database get() = app.container.database
    private val sessionManager get() = app.container.sessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        observeAttendanceHistory()
    }

    private fun setupRecyclerView() {
        adapter = AttendanceHistoryAdapter(emptyList())
        binding.rvAttendanceHistory.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceHistory.adapter = adapter
    }

    private fun observeAttendanceHistory() {
        val employeeId = sessionManager.employeeId() ?: 1
        val currentMonth = DateTimeUtils.currentMonth()
        val currentYear = DateTimeUtils.currentYear()

        val fromDate = DateTimeUtils.monthStart(currentMonth, currentYear)
        val toDate = DateTimeUtils.monthEnd(currentMonth, currentYear)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.workLogDao().observeForPeriod(employeeId, fromDate, toDate).collect { logs ->
                    val items = logs.map { log ->
                        val inStr = timeFormat.format(Date(log.checkInTime))
                        val outStr = log.checkOutTime?.let { timeFormat.format(Date(it)) } ?: "--:--"
                        val otHours = max(log.totalHours - 8.0, 0.0)

                        AttendanceItem(
                            id = log.id,
                            workDate = log.workDate,
                            checkInTimeStr = inStr,
                            checkOutTimeStr = outStr,
                            totalHours = log.totalHours,
                            otHours = otHours,
                            status = log.status
                        )
                    }

                    adapter.updateData(items)

                    // Cập nhật 3 thẻ tóm tắt trên đầu màn hình
                    val completedLogs = logs.filter { it.status == "COMPLETED" }
                    val completedDays = completedLogs.size
                    val totalHours = completedLogs.sumOf { it.totalHours }
                    val totalOt = completedLogs.sumOf { max(it.totalHours - 8.0, 0.0) }

                    binding.tvSummaryDays.text = "$completedDays ngày"
                    binding.tvSummaryHours.text = String.format(Locale.US, "%.1fh", totalHours)
                    binding.tvSummaryOt.text = String.format(Locale.US, "%.1fh", totalOt)
                }
            }
        }
    }
}
