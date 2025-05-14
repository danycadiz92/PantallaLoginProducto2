package com.example.pantallalogin

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private lateinit var txtPremio: TextView
    private lateinit var btnGirar: Button

    private var monedas = 100
    private lateinit var engine: GameEngine
    private lateinit var dbHelper: HistorialDBHelper
    private var spinSound: MediaPlayer? = null
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var prizeRef: DatabaseReference

    companion object {
        private const val CHANNEL_ID      = "canal_victoria"
        private const val NOTIFICATION_ID = 1001
        private const val LOC_REQ         = 3001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruleta)

        // Referencia al bote común
        prizeRef = FirebaseDatabase.getInstance().getReference("commonPrize")

        // Mostrar premio común en vivo
        txtPremio = findViewById(R.id.txtPremio)
        prizeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valor = snapshot.getValue(Int::class.java) ?: 0
                txtPremio.text = getString(R.string.common_prize_label, valor)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Canal de notificación
        crearCanalNotificacion()

        // Lógica, BD y ubicación
        engine      = GameEngine()
        dbHelper    = HistorialDBHelper(this)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        // Permiso ubicación
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
            var ganancia = engine.calcularGanancia(numero)
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
                        // Actualizar UI y notificación
                        actualizarMonedas()
                        tvResultado.text = if (ganancia >= 0)
                            getString(R.string.resultado_ganar, ganancia)
                        else
                            getString(R.string.resultado_perder, -ganancia)
                        if (ganancia > 0) mostrarNotificacionVictoria(ganancia)

                        val fecha = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                        ).format(Date())

                        // Guardar en BD local con ubicación
                        if (ActivityCompat.checkSelfPermission(
                                this@RuletaActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED) {
                            fusedClient.lastLocation.addOnSuccessListener { loc ->
                                guardarPartidaConUbicacion(
                                    numero, ganancia, monedas, fecha,
                                    loc?.latitude, loc?.longitude
                                )
                            }
                        } else {
                            guardarPartidaConUbicacion(numero, ganancia, monedas, fecha, null, null)
                        }

                        // Subir a Firebase y actualizar bote común
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            // Subir score
                            val entry = ScoreEntry(user.displayName ?: "Anon", ganancia)
                            FirebaseDatabase.getInstance()
                                .getReference("scores")
                                .push()
                                .setValue(entry)
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@RuletaActivity,
                                        "Error guardando en Firebase: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            // Incrementar bote común en 10
                            prizeRef.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(current: MutableData): Transaction.Result {
                                    val updated = (current.getValue(Int::class.java) ?: 0) + 10
                                    current.value = updated
                                    return Transaction.success(current)
                                }
                                override fun onComplete(
                                    error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?
                                ) { /* opcional */ }
                            })
                            // Si sale 0, repartir bote como jackpot
                            if (numero == 0) {
                                prizeRef.get().addOnSuccessListener { snap ->
                                    val premio = snap.getValue(Int::class.java) ?: 0
                                    ganancia += premio
                                    monedas    += premio
                                    Toast.makeText(
                                        this@RuletaActivity,
                                        getString(R.string.jackpot_msg, premio),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    prizeRef.setValue(0)
                                    // Actualizar UI tras jackpot
                                    actualizarMonedas()
                                    tvResultado.text = getString(
                                        R.string.resultado_ganar, ganancia)
                                }
                            }
                        }

                        // Captura y evento calendario si hay ganancia
                        if (ganancia > 0) {
                            val shot = capturarVista(window.decorView.rootView)
                            guardarCapturaEnGaleria(shot)
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
        dbHelper.insertarPartidaRuletaRx(numero, ganancia, saldo, fecha, lat, lon)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ /* OK */ }, { err ->
                Toast.makeText(
                    this@RuletaActivity,
                    "Error guardando partida: ${err.message}",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    private fun actualizarMonedas() {
        tvMonedas.text = getString(R.string.monedas, monedas)
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.victoria_notif_titulo)
            val channel = NotificationChannel(
                CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Alertas cuando ganas en la ruleta" }
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
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    private fun capturarVista(view: View): Bitmap {
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        Canvas(bmp).also { view.draw(it) }
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
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals)?.let { uri ->
            resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            vals.clear(); vals.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, vals, null, null)
            Toast.makeText(this, "Captura guardada en galería", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Error al guardar captura", Toast.LENGTH_SHORT).show()
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
        contentResolver.insert(CalendarContract.Events.CONTENT_URI, vals)?.let { uri ->
            val eventId = ContentUris.parseId(uri)
            Toast.makeText(this, "Evento guardado (ID $eventId)", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Error al guardar evento", Toast.LENGTH_SHORT).show()
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
    }

    override fun onDestroy() {
        super.onDestroy()
        spinSound?.release()
        spinSound = null
    }
}