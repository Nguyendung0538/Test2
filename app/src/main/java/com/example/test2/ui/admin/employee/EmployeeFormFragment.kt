/**
 * Fragment giao diện Form Thêm mới / Chỉnh sửa thông tin Nhân viên.
 * Hiển thị các trường nhập liệu TextInputLayout và báo lỗi trực tiếp khi dữ liệu không hợp lệ.
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
import com.example.test2.data.entity.Employee
import com.example.test2.databinding.FragmentEmployeeFormBinding
import kotlinx.coroutines.launch

class EmployeeFormFragment : Fragment() {

    private var _binding: FragmentEmployeeFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EmployeeFormViewModel by viewModels {
        val app = requireActivity().application as PayrollApplication
        EmployeeFormViewModel.Factory(app.container.employeeRepository)
    }

    private var employeeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        employeeId = arguments?.getInt(ARG_EMPLOYEE_ID)?.takeIf { it > 0 }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadEmployee(employeeId)

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveEmployee(
                code = binding.etEmployeeCode.text?.toString().orEmpty(),
                username = binding.etUsername.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty(),
                fullname = binding.etFullName.text?.toString().orEmpty(),
                email = binding.etEmail.text?.toString().orEmpty(),
                phone = binding.etPhone.text?.toString().orEmpty(),
                hourlyRateText = binding.etHourlyRate.text?.toString().orEmpty()
            )
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvFormTitle.text = getString(
                        if (state.isEditMode) R.string.title_edit_employee else R.string.title_add_employee
                    )

                    // Ở chế độ Sửa: Ẩn các trường Username & Password
                    binding.tilUsername.visibility = if (state.isEditMode) View.GONE else View.VISIBLE
                    binding.tilPassword.visibility = if (state.isEditMode) View.GONE else View.VISIBLE

                    // Điền dữ liệu ban đầu nếu là chế độ Sửa
                    state.initialEmployee?.let { emp ->
                        prefillDataOnce(emp)
                    }

                    // Hiển thị lỗi validation trên từng TextInputLayout
                    val err = state.validationErrors
                    binding.tilEmployeeCode.error = err.codeError?.let { getString(it) }
                    binding.tilUsername.error = err.usernameError?.let { getString(it) }
                    binding.tilPassword.error = err.passwordError?.let { getString(it) }
                    binding.tilFullName.error = err.fullnameError?.let { getString(it) }
                    binding.tilEmail.error = err.emailError?.let { getString(it) }
                    binding.tilPhone.error = err.phoneError?.let { getString(it) }
                    binding.tilHourlyRate.error = err.hourlyRateError?.let { getString(it) }

                    // Thông báo thành công
                    if (state.isSaveSuccess) {
                        Toast.makeText(
                            requireContext(),
                            if (state.isEditMode) "Cập nhật nhân viên thành công!" else "Thêm nhân viên thành công!",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }

                    state.errorMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private var isFilled = false
    private fun prefillDataOnce(emp: Employee) {
        if (isFilled) return
        isFilled = true
        binding.etEmployeeCode.setText(emp.code)
        binding.etFullName.setText(emp.fullname)
        binding.etEmail.setText(emp.email)
        binding.etPhone.setText(emp.phone)
        binding.etHourlyRate.setText(emp.hourlyRate.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EMPLOYEE_ID = "arg_employee_id"

        fun newInstance(employeeId: Int? = null): EmployeeFormFragment {
            val fragment = EmployeeFormFragment()
            val args = Bundle()
            if (employeeId != null) {
                args.putInt(ARG_EMPLOYEE_ID, employeeId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}