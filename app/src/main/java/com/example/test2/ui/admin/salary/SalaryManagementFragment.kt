/**
 * Fragment Màn hình Quản lý Bảng lương dành cho Quản trị viên (Admin).
 * Cho phép tra cứu theo Tháng/Năm, xem tổng quan quỹ lương công ty,
 * bấm "Tính lại toàn bộ" và chốt thanh toán từng nhân viên.
 */
package com.example.test2.ui.admin.salary

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test2.PayrollApplication
import com.example.test2.databinding.FragmentSalaryManagementBinding
import com.example.test2.utils.DateTimeUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.launch

class SalaryManagementFragment : Fragment() {

    private var _binding: FragmentSalaryManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalaryManagementViewModel by viewModels {
        val app = requireActivity().application as PayrollApplication
        SalaryManagementViewModel.Factory(app.container.salaryRepository)
    }

    private lateinit var adapter: SalaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalaryManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupSpinners() {
        val months = (1..12).map { "Tháng $it" }
        val monthAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(DateTimeUtils.currentMonth() - 1)

        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setMonth(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val currentYear = DateTimeUtils.currentYear()
        val years = (currentYear - 2..currentYear + 2).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(2) // currentYear is index 2

        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = years[position].toIntOrNull() ?: currentYear
                viewModel.setYear(selectedYear)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = SalaryAdapter { item ->
            SalaryDetailDialog(requireContext(), item) { salaryId ->
                viewModel.markAsPaid(salaryId)
            }.show()
        }
        binding.rvSalaries.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnRecalculateAll.setOnClickListener {
            viewModel.recalculateAll()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
                    binding.tvTotalPayroll.text = "${formatter.format(state.summary.totalPayroll)} VNĐ"
                    binding.tvTotalRegularHours.text = "${state.summary.totalRegularHours}h"
                    binding.tvTotalOtHours.text = "${state.summary.totalOtHours}h"

                    adapter.submitList(state.salaries)
                    binding.layoutEmptyState.visibility = if (state.salaries.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvSalaries.visibility = if (state.salaries.isEmpty()) View.GONE else View.VISIBLE

                    state.message?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SalaryManagementFragment()
    }
}