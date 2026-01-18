package com.example.expensetrackerfinalfull

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.text.InputFilter
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        // Enforce 10-digit limit on mobile field
        b.etLoginMobile.filters = arrayOf(InputFilter.LengthFilter(10))

        // If already logged in, go forward with setup gate
        if (session.currentUserId != -1) {
            navigatePostLogin()
            finish()
            return
        }

        // Radio group → username vs mobile input
        b.rgLoginMode.setOnCheckedChangeListener { _, _ ->
            val useUsername = b.rbLoginUsername.isChecked
            b.etLoginUser.visibility = if (useUsername) View.VISIBLE else View.GONE
            b.etLoginMobile.visibility = if (useUsername) View.GONE else View.VISIBLE
        }

        b.btnLogin.setOnClickListener {
            val identifier = if (b.rbLoginUsername.isChecked)
                b.etLoginUser.text.toString().trim()
            else
                b.etLoginMobile.text.toString().trim()

            val pin = b.etPass.text.toString().trim()

            if (identifier.isBlank() || pin.isBlank()) {
                Toast.makeText(this, "Please enter required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!pin.all { it.isDigit() }) {
                Toast.makeText(this, "PIN must be digits only", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Extra guard: when logging in via mobile, require exactly 10 digits
            if (!b.rbLoginUsername.isChecked) {
                if (identifier.length != 10) {
                    Toast.makeText(this, "Enter 10-digit mobile number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val id = db.loginWithUsernameOrMobile(identifier, pin)
            if (id != -1) {
                session.currentUserId = id
                navigatePostLogin()
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        b.tvForgotPass.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        b.tvForgotUsername.setOnClickListener {
            startActivity(Intent(this, ForgotUsernameActivity::class.java))
        }
    }

    private fun navigatePostLogin() {
        val sm = SessionManager(this)
        if (sm.isBudgetSetupDone()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, BudgetSetupActivity::class.java))
        }
    }

}
