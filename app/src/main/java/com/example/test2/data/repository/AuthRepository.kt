package com.example.test2.data.repository

import androidx.room.withTransaction
import com.example.test2.data.local.AppDatabase
import com.example.test2.data.entity.Account
import com.example.test2.utils.AppResult
import com.example.test2.utils.HashUtils

data class AuthUser(
    val accountId: Int,
    val username: String,
    val role: String,
    val employeeId: Int?
)

class AuthRepository(private val database: AppDatabase) {
    suspend fun login(usernameInput: String, passwordInput: String): AppResult<AuthUser> {
        val username = usernameInput.trim()
        if (username.isBlank() || passwordInput.isBlank()) {
            return AppResult.Error("Vui lòng nhập tên đăng nhập và mật khẩu.")
        }

        return try {
            ensureSeeded()
            val account = database.accountDao().findByUsername(username)
                ?: return AppResult.Error("Tên đăng nhập hoặc mật khẩu không đúng.")
            val passwordHash = HashUtils.sha256(passwordInput)
            if (!constantTimeEquals(account.password, passwordHash)) {
                return AppResult.Error("Tên đăng nhập hoặc mật khẩu không đúng.")
            }

            val employeeId = if (account.role == "EMPLOYEE") {
                database.employeeDao().findByAccountId(account.id)?.id
                    ?: return AppResult.Error("Tài khoản nhân viên chưa được liên kết hồ sơ. Vui lòng liên hệ Admin.")
            } else null

            if (account.role != "ADMIN" && account.role != "EMPLOYEE") {
                return AppResult.Error("Vai trò tài khoản không hợp lệ.")
            }

            AppResult.Success(
                AuthUser(
                    accountId = account.id,
                    username = account.username,
                    role = account.role,
                    employeeId = employeeId
                )
            )
        } catch (e: Exception) {
            AppResult.Error("Không thể đăng nhập. Vui lòng thử lại.", e)
        }
    }

    suspend fun ensureSeeded() {
        database.withTransaction {
            val accountDao = database.accountDao()
            if (accountDao.findByUsername("admin") == null) {
                accountDao.insert(
                    Account(username = "admin", password = HashUtils.sha256("admin123"), role = "ADMIN")
                )
            }
        }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }
}