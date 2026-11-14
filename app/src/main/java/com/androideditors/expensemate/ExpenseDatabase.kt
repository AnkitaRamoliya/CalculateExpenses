package com.androideditors.expensemate

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Amounts are stored as integer paise to avoid floating-point rounding errors.
data class Expense(
    val id: Long,
    val title: String,
    val amountPaise: Long,
    val category: String,
    val createdAt: Long
)

class ExpenseDatabase(context: Context) : SQLiteOpenHelper(context, "expenses.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, amount_paise INTEGER NOT NULL, category TEXT NOT NULL, created_at INTEGER NOT NULL)""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add explicit migrations here if the database schema changes.
    }

    fun list(): List<Expense> = readableDatabase.rawQuery(
        "SELECT id, title, amount_paise, category, created_at FROM expenses ORDER BY created_at DESC, id DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(
                Expense(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3), cursor.getLong(4))
            )
        }
    }

    fun save(title: String, amountPaise: Long, category: String, id: Long? = null) {
        val values = ContentValues().apply {
            put("title", title.trim())
            put("amount_paise", amountPaise)
            put("category", category)
            if (id == null) put("created_at", System.currentTimeMillis())
        }
        if (id == null) writableDatabase.insertOrThrow("expenses", null, values)
        else writableDatabase.update("expenses", values, "id = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("expenses", "id = ?", arrayOf(id.toString()))
    }
}
