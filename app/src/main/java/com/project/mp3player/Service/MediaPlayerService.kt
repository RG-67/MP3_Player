package com.project.mp3player.Service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import java.lang.Exception

class MediaPlayerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    fun playAudio(audioUri: Uri) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, audioUri)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun setMediaPlayingLength(length: Int) {
        mediaPlayer.seekTo(length)
    }

    fun setMediaPlayerPlayOrPause(bl: Boolean) {
        if (bl) {
            mediaPlayer.start()
        }
        else {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

}