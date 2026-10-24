package com.example.test2

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.databinding.ActivitySalaryDetailBinding
import java.text.NumberFormat
import java.util.Locale

class SalaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaryDetailBinding
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        loadSalaryDetails()
    }

    private fun loadSalaryDetails() {
        // Dữ liệu mẫu ban đầu để demo và test giao diện
        // Sẽ được cập nhật từ Room DAO / SessionManager
        val month = 10
        val year = 2026
        val hourlyRate = 50000.0 // 50.000 đ/h
        val regularHours = 160.0
        val overtimeHours = 15.0
        val status = "PENDING"

        val regularPay = regularHours * hourlyRate
        val otPay = overtimeHours * hourlyRate * 1.5
        val totalSalary = regularPay + otPay

        binding.tvSalaryPeriod.text = "Tháng $month / $year"
        binding.tvHourlyRate.text = "${currencyFormatter.format(hourlyRate)} / h"
        binding.tvRegularHours.text = String.format(Locale.US, "%.1f giờ", regularHours)
        binding.tvOvertimeHours.text = String.format(Locale.US, "%.1f giờ", overtimeHours)
        binding.tvTotalSalary.text = currencyFormatter.format(totalSalary)

        binding.tvSalaryStatus.text = status
        if (status == "PAID") {
            binding.tvSalaryStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
            binding.tvSalaryStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            binding.tvSalaryStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
            binding.tvSalaryStatus.setTextColor(Color.parseColor("#E65100"))
        }
    }
}
