package com.example.pantallalogin.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import com.example.pantallalogin.R

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    private lateinit var player: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest

    override fun onCreate() {
        super.onCreate()

        // 1️⃣ Cargo el MP3 desde res/raw/bg_music.mp3
        player = MediaPlayer.create(this, R.raw.bg_music).apply {
            isLooping = true
            setVolume(1f, 1f)
        }

        // 2️⃣ Consigo el AudioManager
        audioManager = getSystemService(AudioManager::class.java)

        // 3️⃣ Preparo la petición de Audio Focus
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            setOnAudioFocusChangeListener(this@MusicService)
        }.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Solicito focus de audio
        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.start()
        }
        return START_STICKY
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.pause()
            AudioManager.AUDIOFOCUS_GAIN                        -> player.start()
            AudioManager.AUDIOFOCUS_LOSS                        -> {
                player.stop()
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.release()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
