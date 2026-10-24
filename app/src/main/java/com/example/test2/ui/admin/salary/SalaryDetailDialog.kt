/**
 * Dialog hiển thị chi tiết bảng lương từng nhân viên và cho phép Admin chốt thanh toán (PAID).
 */
package com.example.test2.ui.admin.salary

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.example.test2.R
import com.example.test2.data.entity.SalaryWithEmployee
import com.example.test2.databinding.DialogSalaryDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SalaryDetailDialog(
    private val context: Context,
    private val item: SalaryWithEmployee,
    private val onMarkPaid: (salaryId: Int) -> Unit
) {

    fun show() {
        val dialog = BottomSheetDialog(context)
        val binding = DialogSalaryDetailBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        val salary = item.salary
        val employee = item.employee
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))

        val regularHours = salary.totalHours - salary.overtimeHours
        val regularSalary = regularHours * employee.hourlyRate
        val otSalary = salary.overtimeHours * employee.hourlyRate * 1.5

        binding.tvDialogTitle.text = "Chi tiết Bảng lương Tháng ${salary.month}/${salary.year}"
        binding.tvEmployeeName.text = "Nhân viên: ${employee.fullname} (${employee.code})"
        binding.tvHourlyRateDetail.text = "${formatter.format(employee.hourlyRate)} VNĐ"
        binding.tvRegularHoursDetail.text = "${regularHours}h"
        binding.tvRegularSalaryDetail.text = "${formatter.format(regularSalary)} VNĐ"
        binding.tvOtHoursDetail.text = "${salary.overtimeHours}h"
        binding.tvOtSalaryDetail.text = "${formatter.format(otSalary)} VNĐ"
        binding.tvTotalSalaryDetail.text = "${formatter.format(salary.totalSalary)} VNĐ"

        if (salary.status == "PAID") {
            binding.tvStatusDetail.text = "ĐÃ THANH TOÁN"
            binding.tvStatusDetail.setTextColor(Color.parseColor("#2E7D32"))
            binding.btnMarkPaid.isEnabled = false
            binding.btnMarkPaid.text = "Đã thanh toán (Đã khóa)"
        } else {
            binding.tvStatusDetail.text = "CHỜ THANH TOÁN"
            binding.tvStatusDetail.setTextColor(Color.parseColor("#E65100"))
            binding.btnMarkPaid.isEnabled = true
            binding.btnMarkPaid.text = context.getString(R.string.btn_mark_paid)
        }

        binding.btnMarkPaid.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_mark_paid_title)
                .setMessage(R.string.dialog_mark_paid_msg)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save) { _, _ ->
                    onMarkPaid(salary.id)
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}