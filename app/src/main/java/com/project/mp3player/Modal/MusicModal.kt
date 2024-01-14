package com.project.mp3player.Modal

import android.net.Uri

class MusicModal(
    private var musicMp3: Uri,
    private var musicTitle: String?,
    private var musicBy: String?,
    private var originalTime: String,
    private var musicTime: String?,
    private var musicThumbnail: ByteArray?
) {

    fun getMusicMp3(): Uri {
        return musicMp3
    }
    fun getMusicTitle(): String? {
        return musicTitle
    }
    fun getMusicBy(): String? {
        return musicBy
    }
    fun getOriginalTime(): String {
        return originalTime
    }
    fun getMusicTime(): String? {
        return musicTime
    }
    fun getMusicThumbnail(): ByteArray? {
        return musicThumbnail
    }

    fun setMusicMp3(msMp3: Uri) {
        musicMp3 = msMp3
    }
    fun setMusicTitle(mTitle: String?) {
        musicTitle = mTitle
    }
    fun setMusicBy(msBy: String?) {
        musicBy = msBy
    }
    fun setOriginalTime(orTime: String) {
        originalTime = orTime
    }
    fun setMusicTime(msTime: String?) {
        musicTime = msTime
    }
    fun setMusicThumbnail(msTNail: ByteArray?) {
        musicThumbnail = msTNail
    }

}