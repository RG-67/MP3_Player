package com.project.mp3player.ClickListener

import android.net.Uri

interface MusicListener {
    fun onClick(position: Int, uri: Uri, duration: Int, musicTime: String?)
}