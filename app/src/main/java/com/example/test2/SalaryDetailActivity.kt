package com.example.test2

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test2.databinding.ActivitySalaryDetailBinding
import com.example.test2.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

class SalaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaryDetailBinding
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    private val app get() = application as PayrollApplication
    private val database get() = app.container.database
    private val sessionManager get() = app.container.sessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        observeSalaryData()
    }

    private fun observeSalaryData() {
        val employeeId = sessionManager.employeeId() ?: 1
        val month = DateTimeUtils.currentMonth()
        val year = DateTimeUtils.currentYear()

        binding.tvSalaryPeriod.text = "Tháng $month / $year"

        lifecycleScope.launch {
            // Lấy thông tin mức lương của nhân viên
            val employee = withContext(Dispatchers.IO) {
                database.employeeDao().findById(employeeId)
            }
            val hourlyRate = employee?.hourlyRate ?: 50000.0
            binding.tvHourlyRate.text = "${currencyFormatter.format(hourlyRate)} / h"

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.salaryDao().observeOne(employeeId, month, year).collect { salary ->
                    if (salary != null) {
                        val regularHours = max(salary.totalHours - salary.overtimeHours, 0.0)
                        binding.tvRegularHours.text = String.format(Locale.US, "%.1f giờ", regularHours)
                        binding.tvOvertimeHours.text = String.format(Locale.US, "%.1f giờ", salary.overtimeHours)
                        binding.tvTotalSalary.text = currencyFormatter.format(salary.totalSalary)

                        updateStatusBadge(salary.status)
                    } else {
                        binding.tvRegularHours.text = "0.0 giờ"
                        binding.tvOvertimeHours.text = "0.0 giờ"
                        binding.tvTotalSalary.text = "Chưa chốt lương"

                        updateStatusBadge("CHƯA CẬP NHẬT")
                    }
                }
            }
        }
    }

    private fun updateStatusBadge(status: String) {
        binding.tvSalaryStatus.text = status
        when (status) {
            "PAID" -> {
                binding.tvSalaryStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvSalaryStatus.setTextColor(Color.parseColor("#2E7D32"))
            }
            "PENDING" -> {
                binding.tvSalaryStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                binding.tvSalaryStatus.setTextColor(Color.parseColor("#E65100"))
            }
            else -> {
                binding.tvSalaryStatus.setBackgroundColor(Color.parseColor("#EEEEEE"))
                binding.tvSalaryStatus.setTextColor(Color.parseColor("#757575"))
            }
        }
    }
}
