package com.example.test2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.test2.databinding.ItemAttendanceHistoryBinding

data class AttendanceItem(
    val id: Int = 0,
    val workDate: String,
    val checkInTimeStr: String,
    val checkOutTimeStr: String,
    val totalHours: Double,
    val otHours: Double,
    val status: String
)

class AttendanceHistoryAdapter(
    private var items: List<AttendanceItem>
) : RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAttendanceHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.tvWorkDate.text = "Ngày: ${item.workDate}"
        binding.tvCheckInTime.text = item.checkInTimeStr.ifBlank { "--:--" }
        binding.tvCheckOutTime.text = item.checkOutTimeStr.ifBlank { "--:--" }

        if (item.otHours > 0) {
            binding.tvTotalHours.text = "${item.totalHours}h (OT: ${item.otHours}h)"
        } else {
            binding.tvTotalHours.text = "${item.totalHours}h"
        }

        binding.tvStatusBadge.text = item.status
        when (item.status) {
            "COMPLETED" -> {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
            }
            "IN_PROGRESS" -> {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#E65100"))
            }
            "INCOMPLETE" -> {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
            }
            else -> {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#EEEEEE"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#616161"))
            }
        }
    }

    fun updateData(newItems: List<AttendanceItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
