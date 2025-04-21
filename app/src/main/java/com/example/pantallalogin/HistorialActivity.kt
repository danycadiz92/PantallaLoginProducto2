package com.example.pantallalogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database.HistorialDBHelper

class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // Referencia al RecyclerView
        val rvHistorial = findViewById<RecyclerView>(R.id.rvHistorial).apply {
            layoutManager = LinearLayoutManager(this@HistorialActivity)
            // Consultamos los datos y asignamos el adapter
            adapter = PartidaAdapter(
                HistorialDBHelper(this@HistorialActivity).obtenerHistorial()
            )
        }
    }
}
