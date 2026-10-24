package com.example.test2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test2.data.entity.WorkLog
import com.example.test2.databinding.ActivityEmployeeDashboardBinding
import com.example.test2.domain.WorkHoursCalculator
import com.example.test2.ui.auth.LoginActivity
import com.example.test2.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmployeeDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeDashboardBinding

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val app get() = application as PayrollApplication
    private val database get() = app.container.database
    private val sessionManager get() = app.container.sessionManager

    private var currentEmployeeId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentEmployeeId = sessionManager.employeeId() ?: 1

        setupUI()
        setupEvents()
    }

    override fun onResume() {
        super.onResume()
        syncTodayStatusFromDb()
    }

    private fun setupUI() {
        val todayStr = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date())

        lifecycleScope.launch {
            val employee = withContext(Dispatchers.IO) {
                database.employeeDao().findById(currentEmployeeId)
            }
            val displayName = employee?.fullname ?: sessionManager.username() ?: "Nhân viên"
            binding.tvWelcomeEmployee.text = "Xin chào, $displayName!\n$todayStr"
        }
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
                    sessionManager.logout()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun syncTodayStatusFromDb() {
        val today = DateTimeUtils.today()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Tự động quét và đánh dấu INCOMPLETE cho các log cũ chưa checkout
                val oldOpenLogs = database.workLogDao().findOldOpenLogs(today)
                oldOpenLogs.forEach { log ->
                    database.workLogDao().update(log.copy(status = "INCOMPLETE", totalHours = 0.0))
                }
            }

            val todayLog = withContext(Dispatchers.IO) {
                database.workLogDao().findForDate(currentEmployeeId, today)
            }

            when {
                todayLog == null -> updateButtonState("IDLE")
                todayLog.status == "IN_PROGRESS" -> updateButtonState("IN_PROGRESS")
                todayLog.status == "COMPLETED" -> updateButtonState("COMPLETED")
                else -> updateButtonState("IDLE")
            }
        }
    }

    private fun updateButtonState(status: String) {
        when (status) {
            "IN_PROGRESS" -> {
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckIn.alpha = 0.5f

                binding.btnCheckOut.isEnabled = true
                binding.btnCheckOut.alpha = 1.0f
                binding.btnCheckOut.text = "Check-Out"
            }
            "COMPLETED" -> {
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckIn.alpha = 0.5f

                binding.btnCheckOut.isEnabled = false
                binding.btnCheckOut.alpha = 0.5f
                binding.btnCheckOut.text = "Đã Check-Out Hôm Nay"
            }
            else -> { // IDLE
                binding.btnCheckIn.isEnabled = true
                binding.btnCheckIn.alpha = 1.0f

                binding.btnCheckOut.isEnabled = false
                binding.btnCheckOut.alpha = 0.5f
                binding.btnCheckOut.text = "Check-Out"
            }
        }
    }

    private fun handleCheckIn() {
        val now = System.currentTimeMillis()
        val today = DateTimeUtils.today()
        val timeStr = timeFormat.format(Date(now))

        lifecycleScope.launch {
            val existing = withContext(Dispatchers.IO) {
                database.workLogDao().findForDate(currentEmployeeId, today)
            }

            if (existing != null) {
                Toast.makeText(this@EmployeeDashboardActivity, "⚠️ Bạn đã check-in ngày hôm nay rồi!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val newLog = WorkLog(
                employeeId = currentEmployeeId,
                workDate = today,
                checkInTime = now,
                checkOutTime = null,
                totalHours = 0.0,
                status = "IN_PROGRESS"
            )

            withContext(Dispatchers.IO) {
                database.workLogDao().insert(newLog)
            }

            updateButtonState("IN_PROGRESS")
            Toast.makeText(this@EmployeeDashboardActivity, "✅ Check-In thành công lúc $timeStr", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleCheckOut() {
        val now = System.currentTimeMillis()
        val today = DateTimeUtils.today()
        val timeStr = timeFormat.format(Date(now))

        lifecycleScope.launch {
            val todayLog = withContext(Dispatchers.IO) {
                database.workLogDao().findForDate(currentEmployeeId, today)
            }

            if (todayLog == null) {
                Toast.makeText(this@EmployeeDashboardActivity, "⚠️ Chưa có bản ghi Check-In hôm nay!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (todayLog.status == "COMPLETED") {
                Toast.makeText(this@EmployeeDashboardActivity, "⚠️ Hôm nay bạn đã check-out rồi!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val result = WorkHoursCalculator.calculate(todayLog.checkInTime, now)

                val updatedLog = todayLog.copy(
                    checkOutTime = now,
                    totalHours = result.totalHours,
                    status = "COMPLETED"
                )

                withContext(Dispatchers.IO) {
                    database.workLogDao().update(updatedLog)
                }

                updateButtonState("COMPLETED")

                val message = if (result.overtimeHours > 0) {
                    "✅ Check-Out lúc $timeStr\nTổng giờ: ${result.totalHours}h (Thường: ${result.regularHours}h, OT: ${result.overtimeHours}h)"
                } else {
                    "✅ Check-Out lúc $timeStr\nTổng giờ làm: ${result.totalHours}h"
                }

                AlertDialog.Builder(this@EmployeeDashboardActivity)
                    .setTitle("Chấm Công Hoàn Tất")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@EmployeeDashboardActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
