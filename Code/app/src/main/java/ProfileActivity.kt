package com.example.expensetrackerfinalfull

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityProfileBinding
import kotlin.concurrent.thread

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater); setContentView(binding.root)
        db = DatabaseHelper(this); session = SessionManager(this)

        // For demonstration, we show username and allow password change via DB operations
        // (In production, hash passwords; for college project plaintext is acceptable)
        val uid = session.currentUserId
        if (uid == -1) { finish(); return }

        binding.btnLogout.setOnClickListener {
            session.logout(); startActivity(android.content.Intent(this, LoginActivity::class.java)); finishAffinity()
        }

        binding.btnMasterReset.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Master reset").setMessage("This will DELETE all users and expenses. Continue?")
                .setPositiveButton("Yes") { _, _ ->
                    val dbFile = getDatabasePath("expenses_db")
                    db.close()
                    dbFile.delete()
                    Toast.makeText(this, "App reset. Restarting...", Toast.LENGTH_SHORT).show()
                    session.logout(); startActivity(android.content.Intent(this, LoginActivity::class.java)); finishAffinity()
                }.setNegativeButton("No", null).show()
        }
    }
}
