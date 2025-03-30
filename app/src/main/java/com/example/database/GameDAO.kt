package com.example.database;

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class GameDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun saveGame(usuarioId: Int, resultado: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("resultado", resultado)
        }
        db.insert("juegos", null, values)
        db.close()
    }
}