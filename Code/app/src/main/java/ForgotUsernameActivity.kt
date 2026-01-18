package com.example.expensetrackerfinalfull

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityForgotUsernameBinding

class ForgotUsernameActivity : AppCompatActivity() {
    private lateinit var b: ActivityForgotUsernameBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityForgotUsernameBinding.inflate(layoutInflater); setContentView(b.root)
        db = DatabaseHelper(this)

        val questions = listOf("What's your pet name?","What's your favorite sport?","What's your birthplace?","What's your mother's maiden name?")
        b.spSecQ.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, questions)

        b.btnConfirm.setOnClickListener {
            val mobile = b.etMobile.text.toString().trim()
            val q = b.spSecQ.selectedItem.toString()
            val a = b.etSecA.text.toString().trim()
            if (mobile.isEmpty() || a.isEmpty()) { Toast.makeText(this, "Please fill the fields", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val username = db.getUsernameByMobile(mobile, q, a)
            if (username != null) {
                b.tvResult.text = "Your username is: " + username
            } else {
                b.tvResult.text = "Incorrect details"
            }
        }
    }
}