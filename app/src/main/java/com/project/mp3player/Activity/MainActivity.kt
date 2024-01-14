package com.project.mp3player.Activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.mp3player.Adapter.MusicAdapter
import com.project.mp3player.ClickListener.MusicListener
import com.project.mp3player.Modal.MusicModal
import com.project.mp3player.Service.MediaPlayerService
import com.project.mp3player.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity(), MusicListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaPlayerService: MediaPlayerService
    private var serviceBound = false
    private val musicList: ArrayList<MusicModal> = ArrayList()
    private var musicAdapter: MusicAdapter? = null
    private var isUserSeeking = false

    companion object {
        private const val MUSIC_REQUEST_CODE = 100
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MediaPlayerService.LocalBinder
            mediaPlayerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()
        setDefault()
        clickMethod()

    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), MUSIC_REQUEST_CODE
            )
        } else {
            extractDataFromStorage()
        }
    }

    private fun setDefault() {
        val intent = Intent(this, MediaPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        musicAdapter = MusicAdapter(this, musicList) { clickedUri ->
            playAudio(clickedUri)
        }
        binding.musicRecycler.setHasFixedSize(true)
        binding.musicRecycler.adapter = musicAdapter
        musicAdapter?.setClickListener(this)
    }

    private fun clickMethod() {
        binding.closeBtn.setOnClickListener {
            hideLayoutAnimation()
        }
        binding.playBtn.setOnClickListener {
            binding.playBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE
            mediaPlayerService.setMediaPlayerPlayOrPause(true)
        }
        binding.pauseBtn.setOnClickListener {
            binding.pauseBtn.visibility = View.GONE
            binding.playBtn.visibility = View.VISIBLE
            mediaPlayerService.setMediaPlayerPlayOrPause(false)
        }
    }

    private fun hideLayoutAnimation() {
        val fromYDelta = 0f
        val toYDelta = binding.playLin.height.toFloat()
        val animation = TranslateAnimation(0f, 0f, fromYDelta, toYDelta)
        animation.duration = 500
        animation.fillAfter = true
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.playLin.visibility = View.GONE
                binding.musicRel.visibility = View.VISIBLE
                setWindow(false)
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
        binding.playLin.startAnimation(animation)
    }

    private fun playAudio(uri: Uri) {
        if (serviceBound) {
            binding.musicRel.visibility = View.GONE
            binding.playLin.visibility = View.VISIBLE
            setWindow(true)
            mediaPlayerService.playAudio(uri)
        }
    }

    private fun setWindow(bl: Boolean) {
        if (bl) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    override fun onDestroy() {
        if (serviceBound) {
            unbindService(connection)
            serviceBound = false
        }
        super.onDestroy()
    }

    private fun extractDataFromStorage() {
        val audioFiles = getAudioFiles()
        for (audioFilesUri in audioFiles) {
            extractMetaData(audioFilesUri)
        }
    }

    private fun getAudioFiles(): List<Uri> {
        val audioFiles = mutableListOf<Uri>()
        val externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(externalContentUri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val audioUri = Uri.parse("content://media/external/audio/media/${it.getLong(0)}")
                audioFiles.add(audioUri)
            }
        }
        return audioFiles
    }

    private fun extractMetaData(audioUri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, audioUri)
            val musicFileName = getFileNameFromUri(audioUri)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val thumbnailBytes = retriever.embeddedPicture

            val runningTime = convertDuration(duration?.toLong() ?: 0)
            if (thumbnailBytes != null) {
//                val bitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
                duration?.let {
                    MusicModal(
                        audioUri, musicFileName, artist, it, runningTime, thumbnailBytes
                    )
                }?.let { musicList.add(it) }
            } else {
                duration?.let {
                    MusicModal(
                        audioUri, musicFileName, artist, it, runningTime, thumbnailBytes
                    )
                }?.let { musicList.add(it) }
            }
            musicAdapter?.notifyDataSetChanged()
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return try {
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    displayName ?: uri.lastPathSegment ?: "UnknownFileName"
                } else {
                    "UnknownFileName"
                }
            } ?: "UnknownFileName"
        } finally {
            cursor?.close()
        }
    }

    private fun convertDuration(duration: Long): String {
        var finalTimeString = ""
        val secondString: String
        val hours = (duration / (1000 * 60 * 60)).toInt()
        val minutes = ((duration % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((duration % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()
        if (hours > 0) {
            finalTimeString = "$hours:"
        }
        secondString = if (seconds < 10) {
            "0$seconds"
        } else {
            "$seconds"
        }
        finalTimeString = "$finalTimeString$minutes:$secondString"
        return finalTimeString
    }

    private fun setProgressSeekbar(duration: Long) {
        binding.progressSeekBar.max = duration.toInt()
        binding.progressSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    isUserSeeking = true
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isUserSeeking = false
                val newPosition = p0?.progress ?: 0
                mediaPlayerService.setMediaPlayingLength(newPosition)
            }
        })
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    try {
                        if (!isUserSeeking) {
                            binding.progressSeekBar.progress =
                                mediaPlayerService.getCurrentPosition()
                        }
                        binding.currentLength.text =
                            convertDuration(
                                calculateRemainingTime(
                                    duration,
                                    mediaPlayerService.getCurrentPosition().toLong()
                                )
                            )
                    } catch (e: Exception) {

                    }
                }
            }
        }, 0, 1000)
    }

    private fun setForAndBackClick(duration: Int) {
        binding.backwardBtn.setOnClickListener {
            val backward = 15000
            val newPosition = mediaPlayerService.getCurrentPosition() - backward
            if (newPosition > 0) {
                mediaPlayerService.setMediaPlayingLength(newPosition)
                if (!isUserSeeking) {
                    binding.progressSeekBar.progress = newPosition
                }
            } else {
                mediaPlayerService.setMediaPlayingLength(0)
                if (!isUserSeeking) {
                    binding.progressSeekBar.progress = 0
                }
            }
        }
        binding.forwardBtn.setOnClickListener {
            val forwardTime = 15000
            val newPosition = mediaPlayerService.getCurrentPosition() + forwardTime
            if (newPosition < duration) {
                mediaPlayerService.setMediaPlayingLength(newPosition)
                if (!isUserSeeking) {
                    binding.progressSeekBar.progress = newPosition
                }
            } else {
                mediaPlayerService.setMediaPlayingLength(duration)
                if (!isUserSeeking) {
                    binding.progressSeekBar.progress = duration
                }
            }
        }
    }

    private fun calculateRemainingTime(totalDuration: Long, currentTime: Long): Long {
        return totalDuration - currentTime
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MUSIC_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                extractDataFromStorage()
            } else {
                checkPermission()
            }
        }
    }

    override fun onClick(position: Int, uri: Uri, duration: Int, musicTime: String?) {
        binding.originalLength.text = musicTime
        binding.currentLength.text = musicTime
        setProgressSeekbar(duration.toLong())
        setForAndBackClick(duration)
    }


}