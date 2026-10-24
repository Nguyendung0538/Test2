package com.example.test2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.databinding.ActivityEmployeeDashboardBinding
import com.example.test2.domain.WorkHoursCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmployeeDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeDashboardBinding
    private lateinit var prefs: SharedPreferences

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val PREFS_NAME = "attendance_prefs"
        private const val KEY_WORK_DATE = "work_date"
        private const val KEY_CHECK_IN_TIME = "check_in_time"
        private const val KEY_STATUS = "status"

        const val STATUS_IDLE = "IDLE"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupUI()
        setupEvents()
        syncTodayStatus()
    }

    private fun setupUI() {
        val todayStr = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date())
        binding.tvWelcomeEmployee.text = "Xin chào, Nhân viên!\n$todayStr"
    }

    private fun setupEvents() {
        // 1. Check-In
        binding.btnCheckIn.setOnClickListener {
            handleCheckIn()
        }

        // 2. Check-Out
        binding.btnCheckOut.setOnClickListener {
            handleCheckOut()
        }

        // 3. Xem Lịch sử điểm danh
        binding.btnViewHistory.setOnClickListener {
            val intent = Intent(this, AttendanceHistoryActivity::class.java)
            startActivity(intent)
        }

        // 4. Xem Chi tiết lương
        binding.btnViewSalary.setOnClickListener {
            val intent = Intent(this, SalaryDetailActivity::class.java)
            startActivity(intent)
        }

        // 5. Đăng xuất
        binding.btnLogoutEmployee.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đồng ý") { _, _ ->
                    Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun syncTodayStatus() {
        val today = dateFormat.format(Date())
        val savedDate = prefs.getString(KEY_WORK_DATE, null)
        val status = prefs.getString(KEY_STATUS, STATUS_IDLE) ?: STATUS_IDLE

        // Nếu qua ngày mới, reset trạng thái
        if (savedDate != today) {
            updateButtonState(STATUS_IDLE)
            return
        }

        updateButtonState(status)
    }

    private fun updateButtonState(status: String) {
        when (status) {
            STATUS_IN_PROGRESS -> {
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckIn.alpha = 0.5f

                binding.btnCheckOut.isEnabled = true
                binding.btnCheckOut.alpha = 1.0f
            }
            STATUS_COMPLETED -> {
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckIn.alpha = 0.5f

                binding.btnCheckOut.isEnabled = false
                binding.btnCheckOut.alpha = 0.5f
                binding.btnCheckOut.text = "Đã Check-Out Hôm Nay"
            }
            else -> { // STATUS_IDLE
                binding.btnCheckIn.isEnabled = true
                binding.btnCheckIn.alpha = 1.0f

                binding.btnCheckOut.isEnabled = false
                binding.btnCheckOut.alpha = 0.5f
            }
        }
    }

    private fun handleCheckIn() {
        val now = System.currentTimeMillis()
        val today = dateFormat.format(Date(now))
        val timeStr = timeFormat.format(Date(now))

        prefs.edit()
            .putString(KEY_WORK_DATE, today)
            .putLong(KEY_CHECK_IN_TIME, now)
            .putString(KEY_STATUS, STATUS_IN_PROGRESS)
            .apply()

        updateButtonState(STATUS_IN_PROGRESS)

        Toast.makeText(this, "✅ Check-In thành công lúc $timeStr", Toast.LENGTH_LONG).show()
    }

    private fun handleCheckOut() {
        val checkInTime = prefs.getLong(KEY_CHECK_IN_TIME, 0L)
        val now = System.currentTimeMillis()

        if (checkInTime == 0L || now <= checkInTime) {
            Toast.makeText(this, "⚠️ Thời gian không hợp lệ!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val result = WorkHoursCalculator.calculate(checkInTime, now)
            val timeStr = timeFormat.format(Date(now))

            prefs.edit()
                .putString(KEY_STATUS, STATUS_COMPLETED)
                .apply()

            updateButtonState(STATUS_COMPLETED)

            val message = if (result.overtimeHours > 0) {
                "✅ Check-Out lúc $timeStr\nTổng giờ: ${result.totalHours}h (Thường: ${result.regularHours}h, OT: ${result.overtimeHours}h)"
            } else {
                "✅ Check-Out lúc $timeStr\nTổng giờ làm: ${result.totalHours}h"
            }

            AlertDialog.Builder(this)
                .setTitle("Chấm Công Hoàn Tất")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
