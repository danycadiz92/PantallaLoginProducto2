package com.example.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.rxjava3.core.Completable  // ← import RxJava

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ruleta_nativex.db"
        private const val DATABASE_VERSION = 2  // Versión 2 con lat/lon

        // Tabla Usuarios, Juegos, Historial, Admin… (igual que antes)

        // Tabla Ruleta
        private const val TABLE_RULETA = "ruleta"
        private const val COLUMN_RULETA_ID       = "id_ruleta"
        private const val COLUMN_NUMERO_GANADOR  = "numero_ganador"
        private const val COLUMN_VALOR_SEGMENTO  = "valor_segmento"
        private const val COLUMN_TIPO_SEGMENTO   = "tipo_segmento"
        private const val COLUMN_USUARIO_ID      = "usuario_id"
        private const val COLUMN_FECHA           = "fecha"
        private const val COLUMN_LATITUDE        = "latitude"
        private const val COLUMN_LONGITUDE       = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // … creación de usuarios, juegos, historial, admin …

        val createRuletaTable = """
            CREATE TABLE $TABLE_RULETA (
              $COLUMN_RULETA_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
              $COLUMN_NUMERO_GANADOR  INTEGER NOT NULL,
              $COLUMN_VALOR_SEGMENTO  INTEGER NOT NULL,
              $COLUMN_TIPO_SEGMENTO   TEXT NOT NULL CHECK($COLUMN_TIPO_SEGMENTO IN ('positivo','negativo')),
              $COLUMN_USUARIO_ID      INTEGER NOT NULL,
              $COLUMN_FECHA           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              $COLUMN_LATITUDE        REAL,
              $COLUMN_LONGITUDE       REAL,
              FOREIGN KEY($COLUMN_USUARIO_ID) REFERENCES $TABLE_USUARIOS(id)
            );
        """.trimIndent()
        db.execSQL(createRuletaTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_RULETA ADD COLUMN $COLUMN_LATITUDE REAL;")
            db.execSQL("ALTER TABLE $TABLE_RULETA ADD COLUMN $COLUMN_LONGITUDE REAL;")
        }
    }

    /**
     * Inserta una partida de ruleta, guardando opcionalmente latitud y longitud.
     */
    fun insertarPartidaRuleta(
        numero: Int,
        valorSegmento: Int,
        tipoSegmento: String,
        usuarioId: Int,
        fecha: String,
        latitude: Double?,
        longitude: Double?
    ) {
        writableDatabase.insert(
            TABLE_RULETA, null, ContentValues().apply {
                put(COLUMN_NUMERO_GANADOR, numero)
                put(COLUMN_VALOR_SEGMENTO, valorSegmento)
                put(COLUMN_TIPO_SEGMENTO, tipoSegmento)
                put(COLUMN_USUARIO_ID, usuarioId)
                put(COLUMN_FECHA, fecha)
                put(COLUMN_LATITUDE, latitude)
                put(COLUMN_LONGITUDE, longitude)
            }
        )
    }

    /**
     * Versión asíncrona con RxJava: inserta en background y notifica cuando termina.
     */
    fun insertarPartidaRuletaRx(
        numero: Int,
        valorSegmento: Int,
        tipoSegmento: String,
        usuarioId: Int,
        fecha: String,
        latitude: Double?,
        longitude: Double?
    ): Completable {
        return Completable.fromAction {
            insertarPartidaRuleta(
                numero, valorSegmento, tipoSegmento,
                usuarioId, fecha, latitude, longitude
            )
        }
    }

    // Resto de métodos (insertar usuario, consultar historial, etc.)...
}
