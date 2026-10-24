package com.example.test2.session

import android.content.Context
import com.example.test2.data.repository.AuthUser

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("payroll_session", Context.MODE_PRIVATE)

    fun saveSession(user: AuthUser, rememberMe: Boolean) {
        prefs.edit()
            .putInt(KEY_ACCOUNT_ID, user.accountId)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_ROLE, user.role)
            .putInt(KEY_EMPLOYEE_ID, user.employeeId ?: -1)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun shouldRememberLogin(): Boolean = isLoggedIn() && prefs.getBoolean(KEY_REMEMBER_ME, false)
    fun accountId(): Int = prefs.getInt(KEY_ACCOUNT_ID, -1)
    fun username(): String? = prefs.getString(KEY_USERNAME, null)
    fun role(): String? = prefs.getString(KEY_ROLE, null)
    fun employeeId(): Int? = prefs.getInt(KEY_EMPLOYEE_ID, -1).takeIf { it > 0 }
    fun rememberMe(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCOUNT_ID = "accountId"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_EMPLOYEE_ID = "employeeId"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_REMEMBER_ME = "rememberMe"
    }
}
