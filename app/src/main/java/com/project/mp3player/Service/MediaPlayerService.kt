package com.project.mp3player.Service

import android.app.Service
import android.content.Context
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

    private var currentTrackUri: Uri? = null

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun playAudio(audioUri: Uri) {
        try {
            currentTrackUri = audioUri
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, audioUri)
            mediaPlayer.prepare()
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                clearPlaybackState()
            }

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

    fun setMediaPlayerPlayOrPause(play: Boolean) {
        if (play) {
            mediaPlayer.start()
        } else {
            mediaPlayer.pause()
        }
    }

    fun getCurrentTrackUri(): Uri? {
        return currentTrackUri
    }

    private fun clearPlaybackState() {
        val prefs = getSharedPreferences("PlaybackPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun prepareAudioAtPosition(audioUri: Uri, position: Int) {
        try {
            currentTrackUri = audioUri
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, audioUri)
            mediaPlayer.prepare()
            mediaPlayer.seekTo(position)

            mediaPlayer.setOnCompletionListener {
                clearPlaybackState()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error preparing audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDuration(): Int {
        return mediaPlayer.duration ?: 0
    }


    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

}