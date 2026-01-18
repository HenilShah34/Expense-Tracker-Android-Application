package com.example.expensetrackerfinalfull


import android.content.Context
import android.content.SharedPreferences

class SessionManager(ctx: Context) {

    private val sp: SharedPreferences =
        ctx.getSharedPreferences("session", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_CURRENT_USER_ID = "currentUserId"
        const val KEY_BUDGET_AMOUNT = "budgetAmount"          // float fallback
        const val KEY_BUDGET_AMOUNT_BITS = "budgetAmountBits" // double bits (more precise)
        const val KEY_BUDGET_PERIOD = "budgetPeriod"
        const val KEY_CURRENCY = "selectedCurrency"
        const val KEY_SETUP_DONE = "setup_done"
        const val KEY_THEME = "theme" // optional; used defensively
    }

    // ---------------------------
    // User session
    // ---------------------------
    var currentUserId: Int
        get() = sp.getInt(KEY_CURRENT_USER_ID, -1)
        set(v) { sp.edit().putInt(KEY_CURRENT_USER_ID, v).apply() }

    // ---------------------------
    // Budget amount (Double stored with Double-bits + Float fallback)
    // ---------------------------
    var budgetAmount: Double
        get() = java.lang.Double.longBitsToDouble(
            sp.getLong(KEY_BUDGET_AMOUNT_BITS, java.lang.Double.doubleToLongBits(0.0))
        )
        set(v) {
            sp.edit()
                .putLong(KEY_BUDGET_AMOUNT_BITS, java.lang.Double.doubleToLongBits(v))
                .putFloat(KEY_BUDGET_AMOUNT, v.toFloat()) // keep a float fallback
                .apply()
        }

    // ---------------------------
    // Period (use property directly)
    // ---------------------------
    var budgetPeriod: String
        get() = sp.getString(KEY_BUDGET_PERIOD, "Daily") ?: "Daily"
        set(v) { sp.edit().putString(KEY_BUDGET_PERIOD, v).apply() }

    // ---------------------------
    // Currency (property + optional helper methods for compatibility)
    // ---------------------------
    var selectedCurrency: String
        get() = sp.getString(KEY_CURRENCY, "INR") ?: "INR"
        set(v) { sp.edit().putString(KEY_CURRENCY, v).apply() }

    // Optional helpers (don’t clash with property names)
    fun setCurrency(v: String) { selectedCurrency = v }
    fun getCurrency(): String = selectedCurrency

    // Store setup flag per user
    fun setBudgetSetupDone(done: Boolean) {
        val uid = currentUserId
        if (uid <= 0) return
        sp.edit().putBoolean("setup_done_user_$uid", done).apply()
    }

    fun isBudgetSetupDone(): Boolean {
        val uid = currentUserId
        if (uid <= 0) return false
        return sp.getBoolean("setup_done_user_$uid", false)
    }

    fun setTheme(theme: String) { sp.edit().putString(KEY_THEME, theme).apply() }
    fun getTheme(): String = sp.getString(KEY_THEME, "") ?: ""

    // ---------------------------
    // Logout: clear only session stuff; KEEP currency/period/budget/setup/theme
    // ---------------------------
    fun logout() {
        val currency = selectedCurrency
        val period = budgetPeriod
        val amount = budgetAmount
        val setup = isBudgetSetupDone()
        val theme = try { getTheme() } catch (_: Exception) { "" }

        sp.edit().clear().apply()

        // Restore preserved values
        selectedCurrency = currency
        budgetPeriod = period
        budgetAmount = amount
        setBudgetSetupDone(setup)
        try { setTheme(theme) } catch (_: Exception) { }
    }
}
