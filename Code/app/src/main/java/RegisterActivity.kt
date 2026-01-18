package com.example.expensetrackerfinalfull

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import android.text.method.PasswordTransformationMethod
import com.example.expensetrackerfinalfull.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var b: ActivityRegisterBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    // separate flags for each field
    private var showPin = false
    private var showConfirm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        // --- Setup custom eye toggles (PIN hidden by default) ---
        initPasswordFieldToggle(
            showFlagGetter = { showPin },
            showFlagSetter = { showPin = it },
            layoutId = R.id.inputPin,
            editId = R.id.etPassword
        )
        initPasswordFieldToggle(
            showFlagGetter = { showConfirm },
            showFlagSetter = { showConfirm = it },
            layoutId = R.id.inputConfirmPin,
            editId = R.id.etConfirm
        )

        // Security question dropdown
        val questions = listOf(
            "What's your pet name?",
            "What's your favorite sport?",
            "What's your birthplace?",
            "What's your mother's maiden name?"
        )
        b.spSecQ.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, questions)

        // Country code dropdown
        val countries = listOf(
            "+91 India", "+1 United States", "+44 United Kingdom", "+61 Australia",
            "+81 Japan", "+49 Germany", "+33 France", "+971 UAE", "+92 Pakistan", "+880 Bangladesh"
        )
        b.spCountry.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)

        b.btnCreate.setOnClickListener {
            val username = b.etUsername.text.toString().trim()
            val pin = b.etPassword.text.toString().trim()
            val confirm = b.etConfirm.text.toString().trim()
            val secQ = b.spSecQ.selectedItem.toString()
            val secA = b.etSecA.text.toString().trim()
            val country = b.spCountry.selectedItem.toString()
            val mobile = b.etMobile.text.toString().trim()

            if (username.isEmpty() || pin.isEmpty() || confirm.isEmpty() || secA.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!pin.all { it.isDigit() }) {
                Toast.makeText(this, "PIN must contain digits only", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin != confirm) {
                Toast.makeText(this, "PIN and Confirm PIN do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
                Toast.makeText(this, "Enter 10-digit mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirm details")
                .setMessage("Create account with these details?\nUsername: $username\nMobile: $country $mobile\nSecurity Q: $secQ")
                .setPositiveButton("Yes") { _, _ ->
                    val id = db.registerUser(
                        username,
                        pin,
                        secQ,
                        secA,
                        mobile,
                        country.split(" ")[0]
                    )
                    if (id == -1L) {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        session.currentUserId = id.toInt()
                        // Always go to Budget Setup after registering
                        startActivity(Intent(this, BudgetSetupActivity::class.java))
                        finish()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun initPasswordFieldToggle(
        showFlagGetter: () -> Boolean,
        showFlagSetter: (Boolean) -> Unit,
        layoutId: Int,
        editId: Int
    ) {
        val til = findViewById<com.google.android.material.textfield.TextInputLayout>(layoutId)
        val et = findViewById<com.google.android.material.textfield.TextInputEditText>(editId)

        // Start hidden by default
        et.transformationMethod = PasswordTransformationMethod.getInstance()
        til.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_visibility_off_24)

        til.setEndIconOnClickListener {
            val newShow = !showFlagGetter()
            showFlagSetter(newShow)

            if (newShow) {
                // eye OPEN → show PIN
                et.transformationMethod = null
                til.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_visibility_24)
            } else {
                // eye CLOSED → hide PIN
                et.transformationMethod = PasswordTransformationMethod.getInstance()
                til.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_visibility_off_24)
            }
            // keep cursor at end
            et.setSelection(et.text?.length ?: 0)
        }
    }
}
