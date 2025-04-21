package com.example.tuproyecto.ui.opciones

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch                         // ← IMPORT DEL SWITCH
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.tuproyecto.R
import com.example.tuproyecto.service.MusicService  // ← IMPORT DEL SERVICE
import com.example.tuproyecto.ui.historial.HistorialActivity
import com.example.tuproyecto.ui.juego.RuletaActivity
import com.example.tuproyecto.ui.login.LoginActivity
import com.example.tuproyecto.ui.puntuacion.PuntuacionActivity

class OpcionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opciones)

        // ① Enlazo el Toolbar y lo convierto en ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // --- MUSIC: Control ON/OFF ---
        val switchMusica = findViewById<Switch>(R.id.switchMusica)
        // Arranca el servicio si el switch viene marcado
        if (switchMusica.isChecked) {
            startService(Intent(this, MusicService::class.java))
        }
        // Listener para activar/desactivar música
        switchMusica.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(Intent(this, MusicService::class.java))
            } else {
                stopService(Intent(this, MusicService::class.java))
            }
        }
        // --- FIN MUSIC ---

        // ② Enlazo los botones
        val btnJugar     = findViewById<Button>(R.id.btnJugarRuleta)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnConfig    = findViewById<Button>(R.id.btnConfig)
        val btnSalir     = findViewById<Button>(R.id.btnSalir)

        btnJugar.setOnClickListener {
            startActivity(Intent(this, RuletaActivity::class.java))
        }
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
        btnConfig.setOnClickListener {
            startActivity(Intent(this, PuntuacionActivity::class.java))
        }
        btnSalir.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ③ Inflo el menú de navegación
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // ④ Manejo los clicks del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_ruleta -> {
                startActivity(Intent(this, RuletaActivity::class.java))
                return true
            }
            R.id.menu_historial -> {
                startActivity(Intent(this, HistorialActivity::class.java))
                return true
            }
            R.id.menu_puntuacion -> {
                startActivity(Intent(this, PuntuacionActivity::class.java))
                return true
            }
            R.id.menu_salir -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
