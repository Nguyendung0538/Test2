package com.example.test2.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.PayrollApplication
import com.example.test2.ui.admin.AdminMainActivity
import com.example.test2.ui.employee.EmployeeMainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = (application as PayrollApplication).container.sessionManager
        val target = if (session.shouldRememberLogin()) {
            when (session.role()) {
                "ADMIN" -> AdminMainActivity::class.java
                "EMPLOYEE" -> EmployeeMainActivity::class.java
                else -> LoginActivity::class.java
            }
        } else {
            session.logout()
            LoginActivity::class.java
        }
        startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}
```

### `app/src/main/java/com/example/test2/ui/employee/EmployeeMainActivity.kt`

```kotlin
package com.example.test2.ui.employee

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test2.PayrollApplication
import com.example.test2.databinding.ActivityMainPlaceholderBinding
import com.example.test2.ui.auth.LoginActivity

/** Navigation placeholder; TV3 replaces this with Employee bottom-navigation screens. */
class EmployeeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainPlaceholderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvWelcome.text = "Xin chào Nhân viên"
        binding.btnLogout.setOnClickListener {
            (application as PayrollApplication).container.sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }
}
