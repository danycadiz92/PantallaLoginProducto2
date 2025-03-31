package com.example.tuproyecto.ui.bienvenida

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tuproyecto.R
import android.widget.Button
import android.widget.TextView

class BienvenidaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        val btnOpciones = findViewById<Button>(R.id.btnIrOpciones)

        btnOpciones.setOnClickListener {
            // Ir a la pantalla de opciones del juego
            val intent = Intent(this, OpcionesActivity::class.java)
            startActivity(intent)
            // O finish() si no quieres volver atrás
        }
    }
}
