/**
 * Adapter cho RecyclerView hiển thị danh sách nhân viên trong màn hình Admin.
 * Sử dụng DiffUtil để tối ưu hóa hiệu năng render dữ liệu và cập nhật giao diện mượt mà.
 */
package com.example.test2.ui.admin.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test2.data.entity.Employee
import com.example.test2.databinding.ItemEmployeeBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class EmployeeAdapter(
    private val onItemClick: (Employee) -> Unit
) : ListAdapter<Employee, EmployeeAdapter.EmployeeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmployeeViewHolder(
        private val binding: ItemEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(employee: Employee) {
            binding.tvEmployeeCode.text = employee.code
            binding.tvFullName.text = employee.fullname
            binding.tvEmail.text = employee.email

            // Chữ cái đầu tiên của Tên làm Avatar
            val firstLetter = employee.fullname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "N"
            binding.tvAvatarLetter.text = firstLetter

            // Định dạng tiền tệ Lương/giờ (VD: 50.000 đ/h)
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
            binding.tvHourlyRate.text = "${formatter.format(employee.hourlyRate)} đ/h"

            binding.cardEmployee.setOnClickListener {
                onItemClick(employee)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Employee>() {
            override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
                return oldItem == newItem
            }
        }
    }
}