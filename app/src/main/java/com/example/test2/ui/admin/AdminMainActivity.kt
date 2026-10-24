/**
 * Activity chính dành cho vai trò Quản trị viên (Admin).
 * Chứa BottomNavigationView chuyển đổi giữa Màn hình Quản lý Nhân viên và Quản lý Bảng lương.
 */
package com.example.test2.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.test2.PayrollApplication
import com.example.test2.R
import com.example.test2.databinding.ActivityAdminMainBinding
import com.example.test2.ui.admin.employee.EmployeeListFragment
import com.example.test2.ui.admin.salary.SalaryManagementFragment
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

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_employees -> {
                    binding.toolbarAdmin.title = "Quản lý Nhân viên"
                    switchFragment(EmployeeListFragment.newInstance())
                    true
                }
                R.id.nav_salary -> {
                    binding.toolbarAdmin.title = "Quản lý Bảng lương"
                    switchFragment(SalaryManagementFragment.newInstance())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.toolbarAdmin.title = "Quản lý Nhân viên"
            switchFragment(EmployeeListFragment.newInstance())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}