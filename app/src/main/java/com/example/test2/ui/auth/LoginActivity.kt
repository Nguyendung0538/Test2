package com.example.test2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test2.PayrollApplication
import com.example.test2.databinding.ActivityLoginBinding
import com.example.test2.ui.admin.AdminMainActivity
import com.example.test2.ui.employee.EmployeeMainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory((application as PayrollApplication).container.authRepository)
    }
    private val app get() = application as PayrollApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etUsername.setText(getPreferences(MODE_PRIVATE).getString(KEY_LAST_USERNAME, ""))
        binding.btnLogin.setOnClickListener {
            binding.tilUsername.error = null
            binding.tilPassword.error = null
            viewModel.login(
                binding.etUsername.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnLogin.isEnabled = !state.isLoading
                    state.error?.let { message ->
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    }
                    state.user?.let { user ->
                        app.container.sessionManager.saveSession(user, binding.cbRememberMe.isChecked)
                        getPreferences(MODE_PRIVATE).edit()
                            .putString(KEY_LAST_USERNAME, user.username).apply()
                        viewModel.consumeUser()
                        openHome(user.role)
                    }
                }
            }
        }
    }

    private fun openHome(role: String) {
        val target = when (role) {
            "ADMIN" -> AdminMainActivity::class.java
            "EMPLOYEE" -> com.example.test2.EmployeeDashboardActivity::class.java
            else -> {
                Toast.makeText(this, "Vai trò không hợp lệ.", Toast.LENGTH_LONG).show()
                return
            }
        }
        startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    companion object {
        private const val KEY_LAST_USERNAME = "lastUsername"
    }
}
