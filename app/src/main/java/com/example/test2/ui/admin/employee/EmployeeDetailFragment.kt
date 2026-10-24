/**
 * Fragment hiển thị thông tin chi tiết nhân viên dành cho Admin.
 * Cho phép xem toàn bộ hồ sơ, tên tài khoản đăng nhập, mở form Chỉnh sửa hoặc Xóa nhân viên có Dialog xác nhận.
 */
package com.example.test2.ui.admin.employee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test2.PayrollApplication
import com.example.test2.R
import com.example.test2.databinding.FragmentEmployeeDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.launch

class EmployeeDetailFragment : Fragment() {

    private var _binding: FragmentEmployeeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EmployeeDetailViewModel by viewModels {
        val app = requireActivity().application as PayrollApplication
        EmployeeDetailViewModel.Factory(app.container.employeeRepository)
    }

    private var employeeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        employeeId = arguments?.getInt(ARG_EMPLOYEE_ID) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (employeeId > 0) {
            viewModel.loadDetail(employeeId)
        }

        binding.btnEdit.setOnClickListener {
            // Mở màn hình Chỉnh sửa thông tin nhân viên
            val fragment = EmployeeFormFragment.newInstance(employeeId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnDelete.setOnClickListener {
            // Hiển thị Dialog cảnh báo xóa an toàn
            showDeleteConfirmationDialog()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.employee?.let { emp ->
                        binding.tvFullName.text = emp.fullname
                        binding.tvEmployeeCode.text = "Mã NV: ${emp.code}"
                        binding.tvEmail.text = emp.email
                        binding.tvPhone.text = emp.phone

                        val firstLetter = emp.fullname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "N"
                        binding.tvAvatarLetter.text = firstLetter

                        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
                        binding.tvHourlyRate.text = "${formatter.format(emp.hourlyRate)} VNĐ / giờ"
                    }

                    state.username?.let { username ->
                        binding.tvUsername.text = username
                    }

                    if (state.isDeleteSuccess) {
                        Toast.makeText(requireContext(), "Đã xóa nhân viên thành công!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }

                    state.errorMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_employee_title)
            .setMessage(R.string.dialog_delete_employee_msg)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                viewModel.deleteEmployee()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EMPLOYEE_ID = "arg_employee_id"

        fun newInstance(employeeId: Int): EmployeeDetailFragment {
            val fragment = EmployeeDetailFragment()
            val args = Bundle()
            args.putInt(ARG_EMPLOYEE_ID, employeeId)
            fragment.arguments = args
            return fragment
        }
    }
}