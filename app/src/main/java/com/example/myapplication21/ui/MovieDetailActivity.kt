package com.example.myapplication21.ui

import android.content.Intent
import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.databinding.ActivityMovieDetailBinding
import com.example.myapplication21.utils.ImageUtils
import com.example.myapplication21.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var viewModel: MovieViewModel
    private var movie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MovieViewModel::class.java]

        val movieId = intent.getLongExtra("movie_id", -1)
        if (movieId == -1L) {
            Toast.makeText(this, "Ошибка загрузки фильма", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        loadMovie(movieId)
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadMovie(movieId: Long) {
        lifecycleScope.launch {
            val loadedMovie = viewModel.getMovieById(movieId)
            movie = loadedMovie
            loadedMovie?.let {
                populateFields(it)
            } ?: run {
                Toast.makeText(this@MovieDetailActivity, "Фильм не найден", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateFields(movie: Movie) {
        binding.titleTextView.text = movie.title
        binding.yearTextView.text = movie.year?.toString() ?: "Год не указан"
        binding.directorTextView.text = movie.director ?: "Режиссер не указан"
        binding.actorsTextView.text = movie.actors ?: "Актеры не указаны"
        binding.descriptionTextView.text = movie.description ?: "Описание отсутствует"
        binding.ratingBar.rating = movie.rating ?: 0f

        binding.statusTextView.text = when (movie.status) {
            MovieStatus.WATCHED -> "Просмотрено"
            MovieStatus.WANT_TO_WATCH -> "Хочу посмотреть"
            MovieStatus.IN_PROGRESS -> "В процессе"
        }

        if (!movie.review.isNullOrBlank()) {
            binding.reviewTextView.text = movie.review
            binding.reviewTextView.visibility = android.view.View.VISIBLE
        } else {
            binding.reviewTextView.visibility = android.view.View.GONE
        }

        // Load poster
        movie.posterPath?.let { path ->
            val bitmap = ImageUtils.loadImageFromPath(path)
            if (bitmap != null) {
                Glide.with(this)
                    .load(bitmap)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.posterImageView)
            } else {
                Glide.with(this)
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(binding.posterImageView)
            }
        } ?: run {
            Glide.with(this)
                .load(android.R.drawable.ic_menu_gallery)
                .into(binding.posterImageView)
        }
    }

    private fun setupButtons() {
        binding.editButton.setOnClickListener {
            movie?.let {
                val intent = Intent(this, AddEditMovieActivity::class.java)
                intent.putExtra("movie_id", it.id)
                startActivity(intent)
            }
        }

        binding.deleteButton.setOnClickListener {
            movie?.let {
                showDeleteConfirmation(it)
            }
        }
    }

    private fun showDeleteConfirmation(movie: Movie) {
        AlertDialog.Builder(this)
            .setTitle("Удалить фильм?")
            .setMessage("Вы уверены, что хотите удалить \"${movie.title}\" из коллекции?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteMovie(movie)
                Toast.makeText(this, "Фильм удален", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        movie?.let {
            loadMovie(it.id)
        }
    }
}

