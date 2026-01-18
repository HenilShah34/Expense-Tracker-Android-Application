package com.example.expensetrackerfinalfull

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class BudgetSetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Uses res/layout/activity_budget_setup.xml
        setContentView(R.layout.activity_budget_setup)

        val inputBudget = findViewById<TextInputLayout>(R.id.inputBudget)
        val spinnerPeriod = findViewById<Spinner>(R.id.spinnerPeriod)
        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val periods = listOf("Daily", "Weekly", "Monthly")
        val currencies = listOf("INR", "USD", "EUR")

        spinnerPeriod.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periods)
        spinnerCurrency.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)

        btnSave.setOnClickListener {
            val amountStr = inputBudget.editText?.text?.toString()?.trim().orEmpty()
            if (amountStr.isEmpty()) {
                inputBudget.error = "Enter budget amount"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                inputBudget.error = "Enter valid amount"
                return@setOnClickListener
            } else {
                inputBudget.error = null
            }

            val period = spinnerPeriod.selectedItem?.toString() ?: "Monthly"
            val currency = spinnerCurrency.selectedItem?.toString() ?: "INR"

            val sm = SessionManager(this)
            sm.budgetAmount = amount.toDouble()
            sm.budgetPeriod = period
            sm.selectedCurrency = currency
            sm.setBudgetSetupDone(true)



            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
