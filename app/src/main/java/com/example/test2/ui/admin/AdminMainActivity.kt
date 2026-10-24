/**
 * Activity chính dành cho vai trò Quản trị viên (Admin).
 * Chứa container nạp các Fragment quản lý nhân viên và bảng lương của công ty.
 */
package com.example.test2.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.PayrollApplication
import com.example.test2.R
import com.example.test2.databinding.ActivityAdminMainBinding
import com.example.test2.ui.admin.employee.EmployeeListFragment
import com.example.test2.ui.auth.LoginActivity

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            (application as PayrollApplication).container.sessionManager.logout()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EmployeeListFragment.newInstance())
                .commit()
        }
    }
}