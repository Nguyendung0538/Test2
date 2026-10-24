/**
 * Repository quản lý toàn bộ các thao tác truy xuất dữ liệu Nhân viên và Tài khoản liên quan (CRUD).
 * Thực thi Room Transaction để đảm bảo tính toàn vẹn dữ liệu khi tạo/xóa nhân viên.
 */
package com.example.test2.data.repository

import androidx.room.withTransaction
import com.example.test2.data.entity.Account
import com.example.test2.data.entity.Employee
import com.example.test2.data.local.AppDatabase
import com.example.test2.utils.HashUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EmployeeRepository(private val database: AppDatabase) {

    private val employeeDao = database.employeeDao()
    private val accountDao = database.accountDao()

    /** Lấy danh sách toàn bộ nhân viên cập nhật thời gian thực. */
    fun getAllEmployees(): Flow<List<Employee>> {
        return employeeDao.observeAll()
    }

    /** Tìm kiếm nhân viên theo họ tên, mã NV hoặc email. */
    fun searchEmployees(query: String): Flow<List<Employee>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            employeeDao.observeAll()
        } else {
            employeeDao.search(trimmed)
        }
    }

    /** Lấy thông tin nhân viên theo ID. */
    fun getEmployeeById(id: Int): Flow<Employee?> {
        return flow {
            emit(employeeDao.findById(id))
        }
    }

    /** Kiểm tra mã nhân viên đã tồn tại trong CSDL chưa. */
    suspend fun isCodeExists(code: String): Boolean {
        return employeeDao.findByCode(code.trim()) != null
    }

    /** Kiểm tra tên đăng nhập đã tồn tại trong CSDL chưa. */
    suspend fun isUsernameExists(username: String): Boolean {
        return accountDao.findByUsername(username.trim()) != null
    }

    /**
     * Thêm mới nhân viên kèm tạo tài khoản đăng nhập trong cùng 1 Room Transaction.
     * Mật khẩu của tài khoản sẽ tự động được mã hóa SHA-256 trước khi lưu.
     */
    suspend fun insertEmployeeWithAccount(employee: Employee, account: Account): Long {
        return database.withTransaction {
            // 1. Mã hóa mật khẩu và chèn tài khoản mới
            val hashedPassword = HashUtils.sha256(account.password)
            val newAccount = account.copy(password = hashedPassword, role = "EMPLOYEE")
            val accountId = accountDao.insert(newAccount).toInt()

            // 2. Chèn nhân viên mới tham chiếu đến accountId vừa tạo
            val newEmployee = employee.copy(accountId = accountId)
            employeeDao.insert(newEmployee)
        }
    }

    /** Cập nhật thông tin cá nhân và mức lương giờ của nhân viên. */
    suspend fun updateEmployee(employee: Employee) {
        employeeDao.update(employee)
    }

    /** Xóa nhân viên (Room CASCADE sẽ tự động dọn dẹp Account, WorkLog và Salary tương ứng). */
    suspend fun deleteEmployee(employee: Employee) {
        database.withTransaction {
            employeeDao.deleteById(employee.id)
            accountDao.deleteById(employee.accountId)
        }
    }
}