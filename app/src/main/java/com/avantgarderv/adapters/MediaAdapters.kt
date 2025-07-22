package com.avantgarderv.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.bumptech.glide.Glide

class ImageAdapter(private val context: Context, private val imageUris: List<String>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.galleryImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = Uri.parse(imageUris[position])
        Glide.with(context)
            .load(uri)
            .placeholder(android.R.drawable.stat_sys_download)
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUris.size
}

class VideoAdapter(private val context: Context, private val videoUris: List<String>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnailView: ImageView = view.findViewById(R.id.videoThumbnailView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val uri = Uri.parse(videoUris[position])

        Glide.with(context)
            .asBitmap()
            .load(uri)
            .placeholder(android.R.drawable.stat_sys_download)
            .error(R.drawable.ic_play_circle)
            .centerCrop()
            .into(holder.thumbnailView)

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setDataAndType(uri, "video/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No app found to play video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = videoUris.size
}