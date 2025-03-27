package com.example.pantallalogin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecuperarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar)

        val correo = findViewById<EditText>(R.id.etCorreoRecuperar)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarCorreo)

        btnEnviar.setOnClickListener {
            val email = correo.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Correo enviado a $email 📩", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
