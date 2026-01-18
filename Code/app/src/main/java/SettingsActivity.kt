package com.example.expensetrackerfinalfull

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.expensetrackerfinalfull.databinding.ActivitySettingsBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    @Suppress("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        // Populate spinners
        val periods = listOf("Daily", "Weekly", "Monthly")
        binding.spBudgetPeriod.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periods)
        val currencies = listOf("INR", "USD", "EUR")
        binding.spCurrency.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)

        // Load existing
        binding.etBudget.setText(
            session.budgetAmount.takeIf { it > 0 }?.let {
                String.format(Locale.getDefault(), "%.2f", it)
            } ?: ""
        )
        binding.spBudgetPeriod.setSelection(periods.indexOf(session.budgetPeriod).coerceAtLeast(0))
        binding.spCurrency.setSelection(currencies.indexOf(session.selectedCurrency).coerceAtLeast(0))

        binding.btnSaveBudget.setOnClickListener {
            val amountStr = binding.etBudget.text.toString().trim()
            val period = binding.spBudgetPeriod.selectedItem.toString()
            val currency = binding.spCurrency.selectedItem.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            // Allow zero budget as "no budget"
            session.budgetAmount = amount
            session.budgetPeriod = period
            session.selectedCurrency = currency
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnExport.setOnClickListener { exportCsv() }
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun exportCsv() {
        thread {
            val now = System.currentTimeMillis()
            val range = when (session.budgetPeriod) {
                "Daily" -> rangeForDay(now)
                "Weekly" -> rangeForWeek(now)
                else -> rangeForMonth(now)
            }
            val start = range.first
            val end = range.second

            val list = db.listExpenses(start, end, session.currentUserId)
            val fmtDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sb = StringBuilder("Date,Category,Amount,Note\n")
            for (e in list) {
                val dateStr = fmtDate.format(Date(e.timestamp))
                val safeNote = (e.note ?: "").replace(",", " ")
                sb.append("$dateStr,${e.category},${e.amount},$safeNote\n")
            }

            val name = "expenses_${System.currentTimeMillis()}.csv"
            val file = File(cacheDir, name)
            file.writeText(sb.toString())

            runOnUiThread {
                val uri: Uri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    file
                )
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(share, "Export CSV"))
            }
        }
    }

    // ---------------------------
    // Local date-range helpers
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
