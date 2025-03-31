package com.example.database;

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ruleta_nativex.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        private const val TABLE_USUARIOS = "usuarios"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_CONTRASEÑA = "contraseña"
        private const val COLUMN_MONEDAS = "monedas"
        private const val COLUMN_PARTIDAS = "partidas"

        // Tabla Juegos
        private const val TABLE_JUEGOS = "juegos"
        private const val COLUMN_JUEGO_ID = "id_juego"
        private const val COLUMN_RESULTADO = "resultado"
        private const val COLUMN_USUARIO_ID = "usuario_id"

        // Tabla Historial
        private const val TABLE_HISTORIAL = "historial"
        private const val COLUMN_HISTORIAL_ID = "id_historial"
        private const val COLUMN_PARTIDAS_LIST = "partidas_list"
        private const val COLUMN_MONEDAS_HISTORIAL = "monedas"

        // Tabla Ruleta
        private const val TABLE_RULETA = "ruleta"
        private const val COLUMN_RULETA_ID = "id_ruleta"
        private const val COLUMN_NUMERO_GANADOR = "numero_ganador"
        private const val COLUMN_VALOR_SEGMENTO = "valor_segmento"
        private const val COLUMN_TIPO_SEGMENTO = "tipo_segmento"
        private const val COLUMN_USUARIO_ID = "usuario_id"
        private const val COLUMN_FECHA = "fecha"

        // Tabla Administrador
        private const val TABLE_ADMIN = "administrador"
        private const val COLUMN_ADMIN_ID = "id_admin"
        private const val COLUMN_ADMIN_CREDENCIALES = "credenciales"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsuariosTable = """
            CREATE TABLE $TABLE_USUARIOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_CONTRASEÑA TEXT NOT NULL,
                $COLUMN_MONEDAS INTEGER DEFAULT 1000,
                $COLUMN_PARTIDAS INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createJuegosTable = """
            CREATE TABLE $TABLE_JUEGOS (
                $COLUMN_JUEGO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RESULTADO BOOLEAN NOT NULL,
                $COLUMN_USUARIO_ID INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_USUARIO_ID) REFERENCES $TABLE_USUARIOS($COLUMN_ID)
            )
        """.trimIndent()

        val createHistorialTable = """
            CREATE TABLE $TABLE_HISTORIAL (
                $COLUMN_HISTORIAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PARTIDAS_LIST TEXT NOT NULL,
                $COLUMN_MONEDAS_HISTORIAL INTEGER NOT NULL
            )
        """.trimIndent()

        val createRuletaTable = """
             CREATE TABLE $TABLE_RULETA (
                $COLUMN_RULETA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NUMERO_GANADOR INTEGER NOT NULL,
                $COLUMN_VALOR_SEGMENTO INTEGER NOT NULL,
                $COLUMN_TIPO_SEGMENTO TEXT NOT NULL CHECK($COLUMN_TIPO_SEGMENTO IN ('positivo', 'negativo')),
                $COLUMN_USUARIO_ID INTEGER NOT NULL,
                $COLUMN_FECHA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_USUARIO_ID) REFERENCES usuarios(id)
             )
          """.trimIndent()

        val createAdminTable = """
            CREATE TABLE $TABLE_ADMIN (
                $COLUMN_ADMIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ADMIN_CREDENCIALES TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createUsuariosTable)
        db.execSQL(createJuegosTable)
        db.execSQL(createHistorialTable)
        db.execSQL(createRuletaTable)
        db.execSQL(createAdminTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_JUEGOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RULETA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADMIN")
        onCreate(db)
    }
}