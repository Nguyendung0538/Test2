package com.example.test2.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.PayrollApplication
import com.example.test2.databinding.ActivityMainPlaceholderBinding
import com.example.test2.ui.auth.LoginActivity

/** Navigation placeholder; TV2 replaces this with Admin bottom-navigation screens. */
class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainPlaceholderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvWelcome.text = "Xin chào Admin"
        binding.btnLogout.setOnClickListener {
            (application as PayrollApplication).container.sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }
}
