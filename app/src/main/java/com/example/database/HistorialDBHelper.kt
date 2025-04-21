package com.example.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

/**
 * Helper para la base de datos de historial de partidas.
 */
class HistorialDBHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Creamos la tabla Historial
        val createTable = """
      CREATE TABLE $TABLE_NAME (
        $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_NUMERO INTEGER,
        $COL_RESULTADO INTEGER,
        $COL_SALDO INTEGER,
        $COL_FECHA TEXT
      )
    """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para esta versión, eliminamos y recreamos
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Inserta un registro de partida en la tabla Historial.
     */
    fun insertarPartida(
        numero: Int,
        gananciaPerdida: Int,
        saldoDespues: Int,
        fecha: String
    ) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NUMERO, numero)
            put(COL_RESULTADO, gananciaPerdida)
            put(COL_SALDO, saldoDespues)
            put(COL_FECHA, fecha)
        }
        db.insert(TABLE_NAME, null, valores)
        db.close()
    }

    companion object {
        private const val DATABASE_NAME = "NativexHistorial.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "Historial"
        const val COL_ID = "id"
        const val COL_NUMERO = "numero"
        const val COL_RESULTADO = "resultado"
        const val COL_SALDO = "saldo"
        const val COL_FECHA = "fecha"
    }
}
