/**
 * ViewModel quản lý Bảng lương công ty (SalaryManagementFragment).
 * Cung cấp chức năng chọn lọc Tháng/Năm, tính toán tổng quỹ lương công ty,
 * tính lại toàn bộ bảng lương PENDING và đánh dấu PAID.
 */
package com.example.test2.ui.admin.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test2.data.entity.SalaryWithEmployee
import com.example.test2.data.repository.SalaryRepository
import com.example.test2.utils.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompanySalarySummary(
    val totalPayroll: Double = 0.0,
    val totalRegularHours: Double = 0.0,
    val totalOtHours: Double = 0.0
)

data class SalaryUiState(
    val month: Int = DateTimeUtils.currentMonth(),
    val year: Int = DateTimeUtils.currentYear(),
    val salaries: List<SalaryWithEmployee> = emptyList(),
    val summary: CompanySalarySummary = CompanySalarySummary(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SalaryManagementViewModel(
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    private val _month = MutableStateFlow(DateTimeUtils.currentMonth())
    private val _year = MutableStateFlow(DateTimeUtils.currentYear())
    private val _isLoading = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SalaryUiState> = combine(_month, _year) { m, y -> Pair(m, y) }
        .flatMapLatest { (m, y) ->
            combine(
                salaryRepository.getSalariesByMonth(m, y),
                _isLoading,
                _message
            ) { salaries, loading, msg ->
                val totalPayroll = salaries.sumOf { it.salary.totalSalary }
                val totalOt = salaries.sumOf { it.salary.overtimeHours }
                val totalHours = salaries.sumOf { it.salary.totalHours }
                val totalRegular = totalHours - totalOt

                SalaryUiState(
                    month = m,
                    year = y,
                    salaries = salaries,
                    summary = CompanySalarySummary(
                        totalPayroll = totalPayroll,
                        totalRegularHours = totalRegular,
                        totalOtHours = totalOt
                    ),
                    isLoading = loading,
                    message = msg
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SalaryUiState()
        )

    fun setMonth(month: Int) {
        _month.value = month
    }

    fun setYear(year: Int) {
        _year.value = year
    }

    fun recalculateAll() {
        val m = _month.value
        val y = _year.value
        viewModelScope.launch {
            _isLoading.value = true
            try {
                salaryRepository.recalculateAllSalaries(m, y)
                _message.value = "Đã tính lại bảng lương Tháng $m/$y!"
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Lỗi khi tính lại bảng lương"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsPaid(salaryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = salaryRepository.markAsPaid(salaryId)
                if (success) {
                    _message.value = "Đã chốt thanh toán (PAID) thành công!"
                } else {
                    _message.value = "Bản ghi lương này đã được thanh toán trước đó"
                }
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Lỗi khi chốt thanh toán"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    class Factory(private val repository: SalaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalaryManagementViewModel::class.java)) {
                return SalaryManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}