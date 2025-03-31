package com.example.pantallalogin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RuletaActivity : AppCompatActivity() {

    private lateinit var tvMonedas: TextView
    private lateinit var tvResultado: TextView
    private lateinit var btnGirar: Button

    private var monedas = 100   // Cantidad inicial
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruleta)

        // Instancio mi lógica de juego
        engine = GameEngine()

        // Enlazo vistas
        tvMonedas = findViewById(R.id.tvMonedas)
        tvResultado = findViewById(R.id.tvResultado)
        btnGirar = findViewById(R.id.btnGirar)

        // Muestro las monedas iniciales
        actualizarMonedas()

        // Al pulsar "Girar"
        btnGirar.setOnClickListener {
            val numero = engine.girarRuleta()
            val ganancia = engine.calcularGanancia(numero)
            monedas += ganancia

            // Actualizo UI
            actualizarMonedas()

            // Muestro un mensaje con el resultado
            if (ganancia >= 0) {
                tvResultado.text = "¡Número $numero! Ganaste $ganancia monedas"
            } else {
                tvResultado.text = "¡Número $numero! Perdiste ${-ganancia} monedas"
            }

            // Si quieres, aquí llamas a tu base de datos:
            // DataBaseHelper(this).insertarPartida(numero, ganancia, monedas, ... )
        }
    }

    private fun actualizarMonedas() {
        tvMonedas.text = "Monedas: $monedas"
    }
}
