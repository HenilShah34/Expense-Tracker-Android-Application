package com.example.expensetrackerfinalfull


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Expense(val id:Int=0, val amount:Double, val category:String, val note:String, val timestamp:Long, val userId:Int)
data class User(val id:Int=0, val username:String, val password:String, val secQ:String, val secA:String, val mobile:String, val country:String)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "expenses_db", null, 3) {
    fun getSecurityQuestionByUsername(username: String): String? {
        val db = readableDatabase
        val c = db.rawQuery("SELECT sec_q FROM users WHERE username=?", arrayOf(username))
        c.use { if (it.moveToFirst()) return it.getString(0) }
        return null
    }
    fun getSecurityQuestionByMobile(mobile: String): String? {
        val db = readableDatabase
        val c = db.rawQuery("SELECT sec_q FROM users WHERE mobile=?", arrayOf(mobile))
        c.use { if (it.moveToFirst()) return it.getString(0) }
        return null
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS users(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            password TEXT,
            sec_q TEXT,
            sec_a TEXT,
            mobile TEXT,
            country TEXT
        )""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS expenses(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            amount REAL NOT NULL,
            category TEXT NOT NULL,
            note TEXT,
            ts INTEGER,
            user_id INTEGER
        )""")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            try { db.execSQL("ALTER TABLE users ADD COLUMN mobile TEXT"); } catch (_:Exception) {}
            try { db.execSQL("ALTER TABLE users ADD COLUMN country TEXT"); } catch (_:Exception) {}
        }
    }

    // ===== Users =====

    fun register(username:String, password:String, secQ:String, secA:String): Long {
        val cv = ContentValues()
        cv.put("username", username); cv.put("password", password); cv.put("sec_q", secQ); cv.put("sec_a", secA)
        return writableDatabase.insert("users", null, cv)
    }

    fun registerUser(username:String, password:String, secQ:String, secA:String, mobile:String, country:String): Long {
        val cv = ContentValues()
        cv.put("username", username); cv.put("password", password)
        cv.put("sec_q", secQ); cv.put("sec_a", secA)
        cv.put("mobile", mobile); cv.put("country", country)
        return writableDatabase.insert("users", null, cv)
    }

    fun login(username:String, password:String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM users WHERE username=? AND password=?", arrayOf(username, password))
        c.use { if (it.moveToFirst()) return it.getInt(0) }
        return -1
    }

    fun loginWithUsernameOrMobile(identifier:String, password:String): Int {
        val sql = if (identifier.all { it.isDigit() } || identifier.startsWith("+"))
            "SELECT id FROM users WHERE mobile=? AND password=?"
        else
            "SELECT id FROM users WHERE username=? AND password=?"
        val c = readableDatabase.rawQuery(sql, arrayOf(identifier, password))
        c.use { if (it.moveToFirst()) return it.getInt(0) }
        return -1
    }

    // Original resetPassword - consider removing or renaming if the one below is preferred
    fun resetPasswordByUsername(username:String, secA:String, newPass:String): Boolean { // Renamed for clarity
        val c = readableDatabase.rawQuery("SELECT id FROM users WHERE username=? AND sec_a=?", arrayOf(username, secA))
        c.use {
            if (it.moveToFirst()) {
                val id = it.getInt(0)
                val cv = ContentValues(); cv.put("password", newPass)
                writableDatabase.update("users", cv, "id=?", arrayOf(id.toString()))
                return true
            }
        }
        return false
    }

    fun getUsernameByMobile(mobile:String, secQ:String, secA:String): String? {
        val c = readableDatabase.rawQuery("SELECT username FROM users WHERE mobile=? AND sec_q=? AND sec_a=?", arrayOf(mobile, secQ, secA))
        c.use { if (it.moveToFirst()) return it.getString(0) }
        return null
    }

    // ===== Expenses =====

    fun addExpense(e: Expense): Long {
        val cv = ContentValues()
        cv.put("amount", e.amount); cv.put("category", e.category); cv.put("note", e.note); cv.put("ts", e.timestamp); cv.put("user_id", e.userId)
        return writableDatabase.insert("expenses", null, cv)
    }

    fun updateExpense(e: Expense): Int {
        val cv = ContentValues()
        cv.put("amount", e.amount); cv.put("category", e.category); cv.put("note", e.note); cv.put("ts", e.timestamp)
        return writableDatabase.update("expenses", cv, "id=? AND user_id=?", arrayOf(e.id.toString(), e.userId.toString()))
    }

    fun deleteExpense(id:Int, userId:Int): Int {
        return writableDatabase.delete("expenses", "id=? AND user_id=?", arrayOf(id.toString(), userId.toString()))
    }

    fun listExpenses(start:Long, end:Long, userId:Int): List<Expense> {
        val res = mutableListOf<Expense>()
        // Ensure note is handled for nulls correctly as it was in the original snippet
        val c = readableDatabase.rawQuery("SELECT id, amount, category, note, ts FROM expenses WHERE ts BETWEEN ? AND ? AND user_id=? ORDER BY ts DESC",
            arrayOf(start.toString(), end.toString(), userId.toString()))
        c.use {
            while (it.moveToNext()) {
                val noteValue = if (it.isNull(3)) "" else it.getString(3) // Ensure note is not null
                res.add(Expense(id=it.getInt(0), amount=it.getDouble(1), category=it.getString(2), note=noteValue, timestamp=it.getLong(4), userId=userId))
            }
        }
        return res
    }

    fun categoryTotals(start:Long, end:Long, userId:Int): Map<String, Double> {
        val map = linkedMapOf<String, Double>()
        val c = readableDatabase.rawQuery("SELECT category, SUM(amount) FROM expenses WHERE ts BETWEEN ? AND ? AND user_id=? GROUP BY category",
            arrayOf(start.toString(), end.toString(), userId.toString()))
        c.use {
            while (it.moveToNext()) map[it.getString(0)] = it.getDouble(1)
        }
        return map
    }

    fun totalInRange(start:Long, end:Long, userId:Int): Double {
        val c = readableDatabase.rawQuery("SELECT SUM(amount) FROM expenses WHERE ts BETWEEN ? AND ? AND user_id=?", arrayOf(start.toString(), end.toString(), userId.toString()))
        c.use { if (it.moveToFirst() && !it.isNull(0)) return it.getDouble(0) }
        return 0.0
    }

    // MOVED THIS FUNCTION INSIDE THE CLASS
    fun resetPassword(identifier: String, byMobile: Boolean, secQ: String, secA: String, newPass: String): Boolean {
        // Now 'writableDatabase' is accessible
        val db = writableDatabase
        val whereClause = if (byMobile) "mobile=?" else "username=?"
        // Corrected arguments for rawQuery: selection args for whereClause, secQ, secA
        val query = "SELECT id FROM users WHERE $whereClause AND sec_q=? AND sec_a=?"
        val selectionArgs = arrayOf(identifier, secQ, secA)

        // The cursor 'c' is correctly typed now as it's within the class context
        val c: Cursor = db.rawQuery(query, selectionArgs)
        c.use { cursor -> // Explicitly typed 'cursor' for clarity
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(0)
                // Use ContentValues for updating password, it's safer
                val cv = ContentValues()
                cv.put("password", newPass)
                db.update("users", cv, "id=?", arrayOf(id.toString()))
                return true // This return is now for the resetPassword function
            }
        }
        return false
    }
} // <<<<< CLASS ENDS HERE NOW
