/**
 * ViewModel xử lý logic nghiệp vụ và kiểm tra tính hợp lệ (Validation)
 * cho Màn hình Thêm mới và Chỉnh sửa thông tin Nhân viên (EmployeeFormFragment).
 */
package com.example.test2.ui.admin.employee

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test2.R
import com.example.test2.data.entity.Account
import com.example.test2.data.entity.Employee
import com.example.test2.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class ValidationErrors(
    val codeError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val fullnameError: Int? = null,
    val emailError: Int? = null,
    val phoneError: Int? = null,
    val hourlyRateError: Int? = null
) {
    val hasErrors: Boolean
        get() = codeError != null || usernameError != null || passwordError != null ||
                fullnameError != null || emailError != null || phoneError != null || hourlyRateError != null
}

data class FormUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val initialEmployee: Employee? = null,
    val validationErrors: ValidationErrors = ValidationErrors(),
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EmployeeFormViewModel(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private var editingEmployeeId: Int? = null

    fun loadEmployee(employeeId: Int?) {
        editingEmployeeId = employeeId
        if (employeeId == null) {
            _uiState.value = FormUiState(isEditMode = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val emp = repository.getEmployeeById(employeeId).firstOrNull()
            if (emp != null) {
                _uiState.value = FormUiState(
                    isLoading = false,
                    isEditMode = true,
                    initialEmployee = emp
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không tìm thấy thông tin nhân viên"
                )
            }
        }
    }

    fun saveEmployee(
        code: String,
        username: String,
        password: String,
        fullname: String,
        email: String,
        phone: String,
        hourlyRateText: String
    ) {
        val isEditMode = _uiState.value.isEditMode
        val currentEmp = _uiState.value.initialEmployee

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 1. Validation Mã NV
            val codeTrimmed = code.trim()
            var codeErr: Int? = null
            if (codeTrimmed.isEmpty()) {
                codeErr = R.string.error_required_field
            } else if (!isEditMode || codeTrimmed != currentEmp?.code) {
                if (repository.isCodeExists(codeTrimmed)) {
                    codeErr = R.string.error_code_exists
                }
            }

            // 2. Validation Username & Password (chỉ bắt buộc khi tạo mới)
            val usernameTrimmed = username.trim()
            var usernameErr: Int? = null
            var passwordErr: Int? = null
            if (!isEditMode) {
                if (usernameTrimmed.isEmpty()) {
                    usernameErr = R.string.error_required_field
                } else if (usernameTrimmed.length < 6) {
                    usernameErr = R.string.error_username_exists
                } else if (repository.isUsernameExists(usernameTrimmed)) {
                    usernameErr = R.string.error_username_exists
                }

                if (password.trim().isEmpty()) {
                    passwordErr = R.string.error_required_field
                } else if (password.trim().length < 6) {
                    passwordErr = R.string.error_short_password
                }
            }

            // 3. Validation Họ tên
            val fullnameTrimmed = fullname.trim()
            val fullnameErr = if (fullnameTrimmed.isEmpty()) R.string.error_required_field else null

            // 4. Validation Email
            val emailTrimmed = email.trim()
            val emailErr = if (emailTrimmed.isEmpty()) {
                R.string.error_required_field
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                R.string.error_invalid_email
            } else null

            // 5. Validation Số điện thoại
            val phoneTrimmed = phone.trim()
            val phoneErr = if (phoneTrimmed.isEmpty()) {
                R.string.error_required_field
            } else if (!Regex("^0[0-9]{9}$").matches(phoneTrimmed)) {
                R.string.error_invalid_phone
            } else null

            // 6. Validation Lương theo giờ
            val hourlyRate = hourlyRateText.trim().toDoubleOrNull()
            val hourlyRateErr = if (hourlyRateText.trim().isEmpty()) {
                R.string.error_required_field
            } else if (hourlyRate == null || hourlyRate <= 0) {
                R.string.error_invalid_hourly_rate
            } else null

            val errors = ValidationErrors(
                codeError = codeErr,
                usernameError = usernameErr,
                passwordError = passwordErr,
                fullnameError = fullnameErr,
                emailError = emailErr,
                phoneError = phoneErr,
                hourlyRateError = hourlyRateErr
            )

            if (errors.hasErrors) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    validationErrors = errors
                )
                return@launch
            }

            // Lưu CSDL
            try {
                if (isEditMode && currentEmp != null) {
                    val updated = currentEmp.copy(
                        code = codeTrimmed,
                        fullname = fullnameTrimmed,
                        email = emailTrimmed,
                        phone = phoneTrimmed,
                        hourlyRate = hourlyRate!!
                    )
                    repository.updateEmployee(updated)
                } else {
                    val newAccount = Account(
                        username = usernameTrimmed,
                        password = password,
                        role = "EMPLOYEE"
                    )
                    val newEmployee = Employee(
                        code = codeTrimmed,
                        fullname = fullnameTrimmed,
                        email = emailTrimmed,
                        phone = phoneTrimmed,
                        hourlyRate = hourlyRate!!,
                        accountId = 0
                    )
                    repository.insertEmployeeWithAccount(newEmployee, newAccount)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isSaveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Lỗi khi lưu thông tin nhân viên"
                )
            }
        }
    }

    class Factory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmployeeFormViewModel::class.java)) {
                return EmployeeFormViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}