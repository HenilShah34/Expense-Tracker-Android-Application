package com.example.expensetrackerfinalfull

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityAddEditBinding
import java.util.Calendar
import kotlin.concurrent.thread

class AddEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var editId: Int = 0

    @Suppress("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        val cats = listOf("Food", "Transport", "Shopping", "Utilities", "Other")
        binding.spCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, cats)

        // Default select "Others" if it exists; otherwise keep first
        selectDefaultCategoryOthers()

        editId = intent.getIntExtra("editId", 0)
        if (editId != 0) {
            val e = db.listExpenses(0, Long.MAX_VALUE, session.currentUserId)
                .firstOrNull { it.id == editId }
            e?.let {
                binding.etAmount.setText("%.2f".format(it.amount))
                binding.spCategory.setSelection(
                    cats.indexOf(it.category).coerceAtLeast(0)
                )
                binding.etNote.setText(it.note)
            }
        }

        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                android.widget.Toast.makeText(
                    this,
                    "Please enter the amount",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                android.widget.Toast.makeText(
                    this,
                    "Invalid amount",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val category = binding.spCategory.selectedItem.toString()
            val note = binding.etNote.text.toString()
            val ts = System.currentTimeMillis()

            thread {
                if (editId == 0) {
                    // INSERT path
                    db.addExpense(
                        Expense(
                            amount = amount,
                            category = category,
                            note = note,
                            timestamp = ts,
                            userId = session.currentUserId
                        )
                    )

                    // ---- Budget check ONLY after adding a new expense ----
                    val budget = session.budgetAmount
                    var crossed = false
                    if (budget > 0) {
                        val now = System.currentTimeMillis()
                        val range = when (session.budgetPeriod) {
                            "Daily" -> rangeForDay(now)
                            "Weekly" -> rangeForWeek(now)
                            else -> rangeForMonth(now)
                        }
                        val start = range.first
                        val end = range.second
                        val totalNow = db.totalInRange(start, end, session.currentUserId)
                        crossed = totalNow > budget
                    }

                    if (crossed) {
                        runOnUiThread {
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Budget Alert")
                                .setMessage("You are crossing your budget limit!!")
                                .setPositiveButton("OK") { _, _ -> finish() }
                                .setOnDismissListener { finish() } // safety
                                .show()
                        }
                    } else {
                        runOnUiThread { finish() }
                    }
                    // -----------------------------------------------------

                } else {
                    // UPDATE path (no popup here)
                    db.updateExpense(
                        Expense(
                            id = editId,
                            amount = amount,
                            category = category,
                            note = note,
                            timestamp = ts,
                            userId = session.currentUserId
                        )
                    )
                    runOnUiThread { finish() }
                }
            }
        }
    }

    private fun selectDefaultCategoryOthers() {
        try {
            val idx = (0 until binding.spCategory.count)
                .firstOrNull {
                    binding.spCategory.getItemAtPosition(it)
                        .toString()
                        .equals("Others", true)
                } ?: 0
            binding.spCategory.setSelection(idx)
        } catch (_: Exception) {
        }
    }

    // ---------------------------
    // Local date-range helpers (Pair<Long, Long>)
    // ---------------------------
    private fun rangeForDay(now: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }

    private fun rangeForWeek(now: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }

    private fun rangeForMonth(now: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }
}
