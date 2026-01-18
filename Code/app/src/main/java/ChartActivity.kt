package com.example.expensetrackerfinalfull

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetrackerfinalfull.databinding.ActivityChartBinding
import java.util.Calendar
import kotlin.concurrent.thread

class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    @Suppress("SetTextI18n") // we set simple strings directly for this screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        thread {
            // Use the SAME period as the dashboard
            val now = System.currentTimeMillis()
            val range = when (session.budgetPeriod) {
                "Weekly" -> rangeForWeek(now)
                "Monthly" -> rangeForMonth(now)
                else -> rangeForDay(now)
            }
            val start = range.first
            val end = range.second

            // Fetch totals by category for the selected period
            val totals: Map<String, Double> = db.categoryTotals(start, end, session.currentUserId)

            runOnUiThread {
                if (totals.isEmpty()) {
                    val periodLabel = when (session.budgetPeriod) {
                        "Weekly" -> "this week"
                        "Monthly" -> "this month"
                        else -> "today"
                    }
                    binding.tvChartInfo.text = "No data for $periodLabel"
                    binding.pieChart.visibility = View.INVISIBLE
                    binding.legendContainer.removeAllViews()
                    return@runOnUiThread
                }

                // Prepare entries for your custom pie chart view
                val entries: List<Pair<String, Double>> = totals.entries.map { it.key to it.value }
                binding.pieChart.visibility = View.VISIBLE
                binding.pieChart.setData(entries)

                // Build/update legend
                binding.legendContainer.removeAllViews()
                val colors = binding.pieChart.getColors()
                entries.forEachIndexed { idx, (category, amount) ->
                    val tv = TextView(this).apply {
                        text = "$category: ${"%.2f".format(amount)}"
                        setPadding(4, 8, 4, 8)
                        setTextColor(Color.DKGRAY)
                        compoundDrawablePadding = 16
                        setCompoundDrawablesWithIntrinsicBounds(
                            ColorSwatchDrawable(colors[idx % colors.size]),
                            null, null, null
                        )
                    }
                    binding.legendContainer.addView(tv)
                }
                binding.tvChartInfo.text = "" // clear any previous message
            }
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
