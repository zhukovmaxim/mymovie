package com.example.myapplication21.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication21.R
import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.utils.ImageUtils

class MovieAdapter(
    private val onItemClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        private val directorTextView: TextView = itemView.findViewById(R.id.directorTextView)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)

        fun bind(movie: Movie) {
            titleTextView.text = movie.title
            yearTextView.text = movie.year?.toString() ?: "Год не указан"
            directorTextView.text = movie.director ?: "Режиссер не указан"
            
            ratingBar.rating = movie.rating ?: 0f
            
            val (statusText, statusColor) = when (movie.status) {
                MovieStatus.WATCHED -> Pair("✓ Просмотрено", ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                MovieStatus.WANT_TO_WATCH -> Pair("★ Хочу посмотреть", ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
                MovieStatus.IN_PROGRESS -> Pair("▶ В процессе", ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark))
            }
            statusTextView.text = statusText
            statusTextView.setTextColor(statusColor)
            
            // Load poster
            movie.posterPath?.let { path ->
                val bitmap = ImageUtils.loadImageFromPath(path)
                if (bitmap != null) {
                    Glide.with(itemView.context)
                        .load(bitmap)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(posterImageView)
                } else {
                    Glide.with(itemView.context)
                        .load(android.R.drawable.ic_menu_gallery)
                        .into(posterImageView)
                }
            } ?: run {
                Glide.with(itemView.context)
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(posterImageView)
            }
            
            itemView.setOnClickListener {
                onItemClick(movie)
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}


