package com.example.expensetrackerfinalfull

import java.util.*
import kotlin.math.round

object Utils {
    fun startOfDay(ts: Long): Long {
        val cal = Calendar.getInstance(); cal.timeInMillis = ts
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    object Utils {
        fun rangeForDay(now: Long): Pair<Long, Long> {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val end = cal.timeInMillis - 1
            return start to end
        }

        fun rangeForWeek(now: Long): Pair<Long, Long> {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek) // start of week
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            val end = cal.timeInMillis - 1
            return start to end
        }

        fun rangeForMonth(now: Long): Pair<Long, Long> {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.MONTH, 1)
            val end = cal.timeInMillis - 1
            return start to end
        }
    }

    // simple currency conversion stub - for display only
    fun convertAmount(amount: Double, currency: String): Pair<String, Double> {
        // Only change symbol for display; do not convert numeric amount.
        val sym = when(currency) {
            "USD" -> "$"
            "EUR" -> "€"
            else -> "₹"
        }
        return sym to (round(amount*100)/100.0)
    }
}
