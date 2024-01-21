package com.project.mp3player.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.mp3player.ClickListener.MusicListener
import com.project.mp3player.Modal.MusicModal
import com.project.mp3player.R
import com.project.mp3player.databinding.MusicListItemBinding
import java.util.Timer
import java.util.TimerTask

class MusicAdapter(
    private val context: Context,
    private val musicList: ArrayList<MusicModal> = ArrayList(),
    private val onItemClickListener: (Uri) -> Unit
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var clickListener: MusicListener? = null
    private var originalMusicList: List<MusicModal> = ArrayList()

    class ViewHolder(val binding: MusicListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal = musicList[position]
        modal.getMusicThumbnail().let {
            val bitmap = it?.let { it1 -> BitmapFactory.decodeByteArray(it, 0, it1.size) }
            holder.binding.musicImage.setImageBitmap(bitmap)
        }
        holder.binding.title.text = modal.getMusicTitle()
        holder.binding.musicBy.text = modal.getMusicBy()
        holder.binding.musicTime.text = modal.getMusicTime()

        holder.itemView.setOnClickListener {
            clickListener?.onClick(position, modal.getMusicMp3(), modal.getOriginalTime().toInt(), modal.getMusicTime())
            onItemClickListener.invoke(modal.getMusicMp3())
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun setClickListener(listener: MusicListener) {
        this.clickListener = listener
    }

    fun setMusicList(newList: List<MusicModal>) {
        originalMusicList = ArrayList(newList)
        updateDisplayedList("")
    }

    fun updateDisplayedList(query: String) {
        musicList.clear()
        if (query.isEmpty()) {
            musicList.addAll(originalMusicList)
        } else {
            val filteredList = originalMusicList.filter { music ->
                music.getMusicTitle()!!.contains(query, ignoreCase = true) == true
            }
            musicList.addAll(filteredList)
        }
        notifyDataSetChanged()
    }

}