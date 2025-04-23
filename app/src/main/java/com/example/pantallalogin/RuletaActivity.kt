package com.example.pantallalogin

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pantallalogin.database.HistorialDBHelper
import com.example.pantallalogin.engine.GameEngine
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RuletaActivity : AppCompatActivity() {

    private lateinit var ivRuleta: ImageView
    private lateinit var tvMonedas: TextView
    private lateinit var tvResultado: TextView
    private lateinit var btnGirar: Button

    private var monedas = 100
    private lateinit var engine: GameEngine
    private lateinit var dbHelper: HistorialDBHelper
    private var spinSound: MediaPlayer? = null
    private lateinit var fusedClient: FusedLocationProviderClient

    companion object {
        private const val CHANNEL_ID      = "canal_victoria"
        private const val NOTIFICATION_ID = 1001
        private const val CALENDAR_REQ    = 2001
        private const val LOC_REQ         = 3001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruleta)

        // Canal de notificación
        crearCanalNotificacion()

        // Lógica, BD y ubicación
        engine      = GameEngine()
        dbHelper    = HistorialDBHelper(this)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        // Pedir permiso de ubicación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOC_REQ
            )
        }

        // Vistas
        ivRuleta    = findViewById(R.id.ivRuleta)
        tvMonedas   = findViewById(R.id.tvMonedas)
        tvResultado = findViewById(R.id.tvResultado)
        btnGirar    = findViewById(R.id.btnGirar)

        // Sonido
        spinSound = MediaPlayer.create(this, R.raw.spin_sound).apply { isLooping = false }

        actualizarMonedas()

        btnGirar.setOnClickListener {
            val numero   = engine.girarRuleta()
            val ganancia = engine.calcularGanancia(numero)
            monedas    += ganancia

            spinSound?.start()

            val vueltas = (3..6).random()
            val desde   = ivRuleta.rotation % 360
            val hasta   = desde + vueltas * 360f + numero * (360f / 37f)

            ObjectAnimator.ofFloat(ivRuleta, "rotation", desde, hasta).apply {
                duration     = 2000L
                interpolator = DecelerateInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(a: Animator) {}
                    override fun onAnimationRepeat(a: Animator) {}
                    override fun onAnimationCancel(a: Animator) {}
                    override fun onAnimationEnd(a: Animator) {
                        actualizarMonedas()
                        tvResultado.text = if (ganancia >= 0)
                            getString(R.string.resultado_ganar, ganancia)
                        else
                            getString(R.string.resultado_perder, -ganancia)

                        if (ganancia > 0) {
                            mostrarNotificacionVictoria(ganancia)
                        }

                        val fecha = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())

                        // Obtenemos ubicación (si permiso dado)
                        if (ActivityCompat.checkSelfPermission(
                                this@RuletaActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED) {
                            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                guardarPartidaConUbicacion(
                                    numero, ganancia, monedas, fecha,
                                    loc?.latitude, loc?.longitude
                                )
                            }
                        } else {
                            // Sin ubicación
                            guardarPartidaConUbicacion(numero, ganancia, monedas, fecha, null, null)
                        }

                        // Captura y galería
                        if (ganancia > 0) {
                            val shot = capturarVista(window.decorView.rootView)
                            guardarCapturaEnGaleria(shot)
                            // Evento en calendario
                            guardarEventoEnCalendario(ganancia)
                        }
                    }
                })
                start()
            }
        }
    }

    private fun guardarPartidaConUbicacion(
        numero: Int, ganancia: Int, saldo: Int, fecha: String,
        lat: Double?, lon: Double?
    ) {
        dbHelper.insertarPartidaRuletaRx(
            numero, ganancia, saldo, fecha, lat, lon
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { /* OK */ },
                { err ->
                    Toast.makeText(
                        this@RuletaActivity,
                        "Error guardando partida: ${err.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
    }

    private fun actualizarMonedas() {
        tvMonedas.text = getString(R.string.monedas, monedas)
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name       = getString(R.string.victoria_notif_titulo)
            val description= "Alertas cuando ganas en la ruleta"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun mostrarNotificacionVictoria(ganancia: Int) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trofeo)
            .setContentTitle(getString(R.string.victoria_notif_titulo))
            .setContentText(getString(R.string.victoria_notif_texto, ganancia))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        NotificationManagerCompat.from(this)
            .notify(NOTIFICATION_ID, builder.build())
    }

    private fun capturarVista(view: View): Bitmap {
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        view.draw(canvas)
        return bmp
    }

    private fun guardarCapturaEnGaleria(bitmap: Bitmap) {
        val fn = "ruleta_${System.currentTimeMillis()}.png"
        val resolver = contentResolver
        val vals = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fn)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            vals.clear()
            vals.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, vals, null, null)
            Toast.makeText(this, "Captura guardada en galería", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar captura", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarEventoEnCalendario(ganancia: Int) {
        val calId = obtenerCalendarId() ?: run {
            Toast.makeText(this, "No se encontró calendario", Toast.LENGTH_SHORT).show()
            return
        }
        val ahora = System.currentTimeMillis()
        val vals = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, ahora)
            put(CalendarContract.Events.DTEND,   ahora + 60_000)
            put(CalendarContract.Events.TITLE,   getString(R.string.victoria_notif_titulo))
            put(CalendarContract.Events.DESCRIPTION,
                getString(R.string.victoria_notif_texto, ganancia))
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.EVENT_TIMEZONE,
                java.util.TimeZone.getDefault().id)
        }
        val uriEvento = contentResolver.insert(CalendarContract.Events.CONTENT_URI, vals)
        if (uriEvento != null) {
            val eventId = ContentUris.parseId(uriEvento)
            Toast.makeText(this, "Evento guardado (ID $eventId)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar evento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerCalendarId(): Long? {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val cols = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        contentResolver.query(uri, cols, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return null
    }

    // Gestionar respuesta permiso ubicación (solo confirmamos)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == LOC_REQ &&
            results.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            // permiso concedido, nada más que hacer
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spinSound?.release()
        spinSound = null
    }
}
