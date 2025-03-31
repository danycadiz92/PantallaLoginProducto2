package com.example.database;

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class UserDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun registerUser(nombre: String, email: String, contraseña: String): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("nombre", nombre)
        values.put("email", email)
        values.put("contraseña", contraseña)
        values.put("monedas", 1000)

        val result = db.insert("usuarios", null, values)
        db.close()
        return result != -1L
    }

    fun loginUser(email: String, password: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT * FROM usuarios WHERE email = ? AND contraseña = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isLoggedIn = cursor.count > 0
        cursor.close()
        db.close()
        return isLoggedIn
    }
}