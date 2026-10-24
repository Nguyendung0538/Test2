/**
 * ViewModel quản lý dữ liệu cho Màn hình Danh sách Nhân viên (EmployeeListFragment).
 * Cung cấp StateFlow danh sách nhân viên và xử lý lọc tìm kiếm realtime theo tên, mã NV, email.
 */
package com.example.test2.ui.admin.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test2.data.entity.Employee
import com.example.test2.data.repository.EmployeeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class EmployeeListViewModel(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val employees: StateFlow<List<Employee>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchEmployees(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    class Factory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmployeeListViewModel::class.java)) {
                return EmployeeListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}