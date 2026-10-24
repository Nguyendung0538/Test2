package com.example.test2.ui.employee

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.EmployeeDashboardActivity

/**
 * TV3 Employee Activity: Chuyển hướng đến EmployeeDashboardActivity.
 */
class EmployeeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, EmployeeDashboardActivity::class.java))
        finish()
    }
}