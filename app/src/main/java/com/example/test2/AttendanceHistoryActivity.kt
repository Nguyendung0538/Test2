package com.example.test2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test2.databinding.ActivityAttendanceHistoryBinding
import java.util.Locale
import kotlin.math.max

class AttendanceHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceHistoryBinding
    private lateinit var adapter: AttendanceHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        loadAttendanceHistory()
    }

    private fun setupRecyclerView() {
        adapter = AttendanceHistoryAdapter(emptyList())
        binding.rvAttendanceHistory.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceHistory.adapter = adapter
    }

    private fun loadAttendanceHistory() {
        // Danh sách mẫu ban đầu để demo và test giao diện
        // Sẽ được thay thế bằng Room DAO / Repository khi tích hợp
        val sampleList = listOf(
            AttendanceItem(1, "2026-10-24", "08:00", "18:00", 10.0, 2.0, "COMPLETED"),
            AttendanceItem(2, "2026-10-23", "08:00", "17:00", 9.0, 1.0, "COMPLETED"),
            AttendanceItem(3, "2026-10-22", "08:15", "16:45", 8.5, 0.5, "COMPLETED"),
            AttendanceItem(4, "2026-10-21", "08:00", "--:--", 0.0, 0.0, "INCOMPLETE"),
            AttendanceItem(5, "2026-10-20", "08:00", "16:00", 8.0, 0.0, "COMPLETED")
        )

        adapter.updateData(sampleList)

        // Tính toán tóm tắt
        val completedDays = sampleList.count { it.status == "COMPLETED" }
        val totalHours = sampleList.filter { it.status == "COMPLETED" }.sumOf { it.totalHours }
        val totalOt = sampleList.filter { it.status == "COMPLETED" }.sumOf { it.otHours }

        binding.tvSummaryDays.text = "$completedDays ngày"
        binding.tvSummaryHours.text = String.format(Locale.US, "%.1fh", totalHours)
        binding.tvSummaryOt.text = String.format(Locale.US, "%.1fh", totalOt)
    }
}
