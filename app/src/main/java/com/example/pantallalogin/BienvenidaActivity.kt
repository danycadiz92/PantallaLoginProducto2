package com.example.pantallalogin.ui.bienvenida

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pantallalogin.R
import android.widget.Button

class BienvenidaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        val btnOpciones = findViewById<Button>(R.id.btnIrOpciones)

        btnOpciones.setOnClickListener {
            // Ir a la pantalla de la ruleta
            val intent = Intent(this, RuletaActivity::class.java)
            startActivity(intent)
        }
    }
}
