/**
 * Adapter cho RecyclerView hiển thị danh sách bảng lương từng nhân viên trong tháng.
 * Hiển thị họ tên, mã NV, tổng số giờ (chuẩn + OT), tổng số tiền và nhãn trạng thái (PENDING/PAID).
 */
package com.example.test2.ui.admin.salary

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test2.data.entity.SalaryWithEmployee
import com.example.test2.databinding.ItemSalaryBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SalaryAdapter(
    private val onItemClick: (SalaryWithEmployee) -> Unit
) : ListAdapter<SalaryWithEmployee, SalaryAdapter.SalaryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaryViewHolder {
        val binding = ItemSalaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SalaryViewHolder(
        private val binding: ItemSalaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SalaryWithEmployee) {
            val salary = item.salary
            val employee = item.employee

            binding.tvEmployeeCode.text = employee.code
            binding.tvFullName.text = employee.fullname

            // Hiển thị giờ làm
            val regularHours = salary.totalHours - salary.overtimeHours
            binding.tvTotalHours.text = "Tổng ${salary.totalHours}h (${salary.overtimeHours}h OT)"

            // Định dạng tiền tệ
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
            binding.tvTotalSalary.text = "${formatter.format(salary.totalSalary)} VNĐ"

            // Trạng thái thanh toán (PENDING/PAID)
            if (salary.status == "PAID") {
                binding.tvStatus.text = "ĐÃ THANH TOÁN"
                binding.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
            } else {
                binding.tvStatus.text = "CHỜ THANH TOÁN"
                binding.tvStatus.setTextColor(Color.parseColor("#E65100"))
                binding.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
            }

            binding.cardSalary.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SalaryWithEmployee>() {
            override fun areItemsTheSame(oldItem: SalaryWithEmployee, newItem: SalaryWithEmployee): Boolean {
                return oldItem.salary.id == newItem.salary.id
            }

            override fun areContentsTheSame(oldItem: SalaryWithEmployee, newItem: SalaryWithEmployee): Boolean {
                return oldItem == newItem
            }
        }
    }
}