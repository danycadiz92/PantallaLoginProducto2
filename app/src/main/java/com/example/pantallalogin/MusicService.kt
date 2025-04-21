package com.example.tuproyecto.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import com.example.tuproyecto.R

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    private lateinit var player: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest

    override fun onCreate() {
        super.onCreate()
        // Configuro MediaPlayer con el recurso raw
        player = MediaPlayer.create(this, R.raw.musica_fondo).apply {
            isLooping = true
            setVolume(1f, 1f)
        }
        audioManager = getSystemService(AudioManager::class.java)
        // Preparar solicitud de Audio Focus
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            .setOnAudioFocusChangeListener(this)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Solicito Audio Focus
        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos
        player.stop()
        player.release()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                player.pause()
            AudioManager.AUDIOFOCUS_GAIN ->
                player.start()
            AudioManager.AUDIOFOCUS_LOSS -> {
                player.stop()
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
