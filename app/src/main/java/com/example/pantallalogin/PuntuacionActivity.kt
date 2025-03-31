package com.example.tuproyecto.ui.puntos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import com.example.tuproyecto.R
import com.example.tuproyecto.data.AppDatabase  // depende de cómo tengas organizado
import com.example.tuproyecto.data.Partida
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PuntuacionActivity : AppCompatActivity() {

    private lateinit var tvMonedas: TextView
    private lateinit var listPuntuaciones: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puntuacion)

        tvMonedas = findViewById(R.id.tvMonedasActuales)
        listPuntuaciones = findViewById(R.id.listPuntuaciones)

        // Supongamos que en tu DB ya guardas las monedas
        val db = AppDatabase.getInstance(this)
        // Cargamos data en un hilo secundario (con Coroutines como ejemplo)
        GlobalScope.launch {
            val partidas = db.partidaDao().obtenerHistorial()  // Devuelve List<Partida>

            // Cambiar al hilo principal para actualizar la UI
            runOnUiThread {
                // Muestra cuántas monedas (puedes tenerlo en la tabla user, o calcularlo)
                // Ejemplo: la última partida es la que define las monedas actuales
                if (partidas.isNotEmpty()) {
                    val ultima = partidas[0]
                    tvMonedas.text = "Monedas: ${ultima.monedas}"
                }

                // Convertimos las partidas en strings
                val listaStrings = partidas.map { p ->
                    "Fecha: ${p.fecha} - Monedas: ${p.monedas} - Tiradas: ${p.tiradas}"
                }

                // Creamos un ArrayAdapter básico
                val adapter = android.widget.ArrayAdapter<String>(
                    this@PuntuacionActivity,
                    android.R.layout.simple_list_item_1,
                    listaStrings
                )
                listPuntuaciones.adapter = adapter
            }
        }
    }
}
