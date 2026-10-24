/**
 * ViewModel quản lý dữ liệu và logic cho Màn hình Chi tiết Nhân viên (EmployeeDetailFragment).
 * Cung cấp thông tin chi tiết nhân viên, tên tài khoản và xử lý yêu cầu xóa nhân viên khỏi hệ thống.
 */
package com.example.test2.ui.admin.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test2.data.entity.Employee
import com.example.test2.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class EmployeeDetailUiState(
    val isLoading: Boolean = false,
    val employee: Employee? = null,
    val username: String? = null,
    val isDeleteSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EmployeeDetailViewModel(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeDetailUiState())
    val uiState: StateFlow<EmployeeDetailUiState> = _uiState.asStateFlow()

    private var currentEmployeeId: Int? = null

    fun loadDetail(employeeId: Int) {
        currentEmployeeId = employeeId
        viewModelScope.launch {
            _uiState.value = EmployeeDetailUiState(isLoading = true)
            val emp = repository.getEmployeeById(employeeId).firstOrNull()
            if (emp != null) {
                val account = repository.getAccountById(emp.accountId)
                _uiState.value = EmployeeDetailUiState(
                    isLoading = false,
                    employee = emp,
                    username = account?.username ?: "Chưa liên kết"
                )
            } else {
                _uiState.value = EmployeeDetailUiState(
                    isLoading = false,
                    errorMessage = "Không tìm thấy thông tin nhân viên"
                )
            }
        }
    }

    fun deleteEmployee() {
        val emp = _uiState.value.employee ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.deleteEmployee(emp)
                _uiState.value = _uiState.value.copy(isLoading = false, isDeleteSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Lỗi khi xóa nhân viên"
                )
            }
        }
    }

    class Factory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmployeeDetailViewModel::class.java)) {
                return EmployeeDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}