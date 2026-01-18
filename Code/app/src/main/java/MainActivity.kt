package com.example.expensetrackerfinalfull

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetrackerfinalfull.databinding.ActivityMainBinding
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ExpenseAdapter
    private lateinit var session: SessionManager

    @Suppress("SetTextI18n") // simple dynamic text is fine for this screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        // --------------------------
        // Toolbar + Drawer setup
        // --------------------------
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_export_csv -> {
                    try { exportCsv() } catch (_: Exception) {}
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_budget_setup -> {
                    startActivity(Intent(this, BudgetSetupActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // --------------------------
        // RecyclerView + Buttons
        // --------------------------
        adapter = ExpenseAdapter(
            listOf(),
            onEdit = { expense ->
                val i = Intent(this, AddEditActivity::class.java)
                i.putExtra("editId", expense.id)
                startActivity(i)
            },
            onDelete = { id ->
                thread {
                    db.deleteExpense(id, session.currentUserId)
                    runOnUiThread { loadForPeriod() }
                }
            }
        )

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
        binding.btnChart.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadForPeriod()
    }

    private fun loadForPeriod() {
        thread {
            val period = session.budgetPeriod
            val now = System.currentTimeMillis()
            val (start, end) = when (period) {
                "Weekly" -> rangeForWeek(now)
                "Monthly" -> rangeForMonth(now)
                else -> rangeForDay(now) // Daily default
            }
            val expenses = db.listExpenses(start, end, session.currentUserId)
            runOnUiThread {
                adapter.update(expenses)
                updateSummary(expenses)
            }
        }
    }

    @Suppress("SetTextI18n")
    private fun updateSummary(expenses: List<Expense>) {
        val totalAmount = expenses.sumOf { it.amount }
        val (currencySymbol, totalDisplay) = Utils.convertAmount(totalAmount, session.selectedCurrency)
        binding.tvTotal.text = "Total: ${currencySymbol}${"%.2f".format(totalDisplay)}"

        val budget = session.budgetAmount
        val periodName = session.budgetPeriod

        if (budget > 0) {
            val now = System.currentTimeMillis()
            val (start, end) = when (periodName) {
                "Daily" -> rangeForDay(now)
                "Weekly" -> rangeForWeek(now)
                else -> rangeForMonth(now)
            }
            val periodTotal = db.totalInRange(start, end, session.currentUserId)
            val remaining = (budget - periodTotal)
            val (symBudget, budgetDisplay) = Utils.convertAmount(budget, session.selectedCurrency)
            val (_, remainingDisplay) = Utils.convertAmount(remaining, session.selectedCurrency)

            binding.tvBudget.text = "Budget: ${symBudget}${"%.2f".format(budgetDisplay)} (${periodName})"
            binding.tvRemaining.text = "Remaining: ${currencySymbol}${"%.2f".format(remainingDisplay)}"
        } else {
            binding.tvBudget.text = "Budget: -"
            binding.tvRemaining.text = "Remaining: -"
        }
    }

    // ---------------------------
    // Remove 3-dots overflow menu
    // ---------------------------
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean = false
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean = false

    // ---------------------------
    // CSV Export (current period only)
    // ---------------------------
    private fun exportCsv() {
        try {
            val file = File(cacheDir, "expenses_export.csv")

            val now = System.currentTimeMillis()
            val (start, end) = when (session.budgetPeriod) {
                "Weekly" -> rangeForWeek(now)
                "Monthly" -> rangeForMonth(now)
                else -> rangeForDay(now)
            }

            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            FileWriter(file).use { fw ->
                fw.appendLine("amount,category,note,timestamp")
                val expenses = db.listExpenses(start, end, session.currentUserId)
                for (e in expenses) {
                    val safeNote = (e.note ?: "").replace(",", " ")
                    val ts = fmt.format(Date(e.timestamp))
                    fw.appendLine("${e.amount},${e.category},${safeNote},${ts}")
                }
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(share, "Export CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek) // start of week
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
