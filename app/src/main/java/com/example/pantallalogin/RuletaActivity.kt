package com.example.pantallalogin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.database.HistorialDBHelper              // ① Importa el helper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RuletaActivity : AppCompatActivity() {

    private lateinit var tvMonedas: TextView
    private lateinit var tvResultado: TextView
    private lateinit var btnGirar: Button

    private var monedas = 100   // Cantidad inicial
    private lateinit var engine: GameEngine

    // ② Añade la propiedad del helper
    private lateinit var dbHelper: HistorialDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruleta)

        // Instancio mi lógica de juego
        engine = GameEngine()

        // Instancio el helper de SQLite
        dbHelper = HistorialDBHelper(this)

        // Enlazo vistas
        tvMonedas   = findViewById(R.id.tvMonedas)
        tvResultado = findViewById(R.id.tvResultado)
        btnGirar    = findViewById(R.id.btnGirar)

        // Muestro las monedas iniciales
        actualizarMonedas()

        // Al pulsar "Girar"
        btnGirar.setOnClickListener {
            val numero      = engine.girarRuleta()
            val ganancia    = engine.calcularGanancia(numero)
            monedas       += ganancia

            // Actualizo UI
            actualizarMonedas()

            // Muestro un mensaje con el resultado
            tvResultado.text = if (ganancia >= 0) {
                "¡Número $numero! Ganaste $ganancia monedas"
            } else {
                "¡Número $numero! Perdiste ${-ganancia} monedas"
            }

            // ③ Preparo la fecha en formato texto
            val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            // ④ Inserto el registro de esta partida en SQLite
            dbHelper.insertarPartida(
                numero,
                ganancia,
                monedas,
                fecha
            )
        }
    }

    private fun actualizarMonedas() {
        tvMonedas.text = "Monedas: $monedas"
    }
}
