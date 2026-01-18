package com.example.expensetrackerfinalfull

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var db: DatabaseHelper

    private var modeMobile = false
    private var cachedIdentifier: String? = null
    private var cachedQuestion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        // Default state comes from XML (rbUsername checked = true)
        modeMobile = binding.rbMobile.isChecked

        // Default input config for username mode
        if (!modeMobile) {
            binding.etIdentifier.inputType = InputType.TYPE_CLASS_TEXT
            binding.etIdentifier.filters = emptyArray()
        } else {
            binding.etIdentifier.inputType = InputType.TYPE_CLASS_PHONE
            binding.etIdentifier.filters = arrayOf(InputFilter.LengthFilter(10))
        }

        // Switch between Username / Mobile modes
        binding.rbUsername.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                modeMobile = false
                binding.etIdentifier.hint = "Enter username"
                binding.etIdentifier.inputType = InputType.TYPE_CLASS_TEXT
                binding.etIdentifier.filters = emptyArray()
                resetStep2()
            }
        }
        binding.rbMobile.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                modeMobile = true
                binding.etIdentifier.hint = "Enter mobile number"
                binding.etIdentifier.inputType = InputType.TYPE_CLASS_PHONE
                binding.etIdentifier.filters = arrayOf(InputFilter.LengthFilter(10))
                resetStep2()
            }
        }

        // Step 1: Fetch security question for given identifier
        binding.btnFetchQuestion.setOnClickListener {
            val id = binding.etIdentifier.text.toString().trim()
            if (!modeMobile && id.isEmpty()) {
                toast("Please enter username"); return@setOnClickListener
            }
            if (modeMobile && id.length != 10) {
                toast("Enter 10-digit mobile number"); return@setOnClickListener
            }

            val secQ = if (modeMobile) {
                db.getSecurityQuestionByMobile(id)
            } else {
                db.getSecurityQuestionByUsername(id)
            }

            if (secQ == null) {
                toast("Incorrect ${if (modeMobile) "Mobile No." else "Username"} entered")
                return@setOnClickListener
            }

            cachedIdentifier = id
            cachedQuestion = secQ

            binding.tvSecQ.text = secQ
            binding.tvSecQ.visibility = View.VISIBLE
            binding.etSecA.visibility = View.VISIBLE
            binding.etNewPin.visibility = View.VISIBLE
            binding.btnConfirmReset.visibility = View.VISIBLE
        }

        // Step 2: Validate answer + reset PIN
        binding.btnConfirmReset.setOnClickListener {
            val id = cachedIdentifier ?: run {
                toast("Please fetch the security question first")
                return@setOnClickListener
            }
            val q = cachedQuestion ?: run {
                toast("Please fetch the security question first")
                return@setOnClickListener
            }

            val a = binding.etSecA.text.toString().trim()
            val pin = binding.etNewPin.text.toString().trim()

            if (a.isEmpty()) {
                toast("Enter security answer"); return@setOnClickListener
            }
            if (pin.length !in 4..6 || !pin.all { it.isDigit() }) {
                toast("PIN must be 4-6 digits"); return@setOnClickListener
            }

            val ok = db.resetPassword(
                identifier = id,
                byMobile = modeMobile,
                secQ = q,
                secA = a,
                newPass = pin
            )
            if (ok) {
                toast("Password reset. Please login.")
                finish()
            } else {
                toast("Incorrect details")
            }
        }
    }

    private fun resetStep2() {
        cachedIdentifier = null
        cachedQuestion = null
        binding.tvSecQ.visibility = View.GONE
        binding.etSecA.visibility = View.GONE
        binding.etNewPin.visibility = View.GONE
        binding.btnConfirmReset.visibility = View.GONE
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
