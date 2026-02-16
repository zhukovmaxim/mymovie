package com.example.myapplication21.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.databinding.ActivityAddEditMovieBinding
import com.example.myapplication21.utils.ImageUtils
import com.example.myapplication21.utils.ReminderUtils
import com.example.myapplication21.viewmodel.MovieViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class AddEditMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMovieBinding
    private lateinit var viewModel: MovieViewModel
    private var movieId: Long? = null
    private var currentPhotoPath: String? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MovieViewModel::class.java]

        setupToolbar()
        setupStatusToggle()
        setupImageButtons()
        setupReminder()
        loadMovieIfEditing()
        setupSaveButton()
    }
    
    private var reminderCalendar: Calendar? = null
    
    private fun setupReminder() {
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderDateButton.isEnabled = isChecked
            if (!isChecked) {
                reminderCalendar = null
            }
        }
        
        binding.reminderDateButton.setOnClickListener {
            showDateTimePicker()
        }
    }
    
    private fun showDateTimePicker() {
        val calendar = reminderCalendar ?: Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
        }
        
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val timePicker = android.app.TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        reminderCalendar = calendar
                        updateReminderButton()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }
    
    private fun updateReminderButton() {
        reminderCalendar?.let { cal ->
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            binding.reminderDateButton.text = dateFormat.format(cal.time)
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageUri(it)
        }
    }
    
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoPath != null) {
            val bitmap = ImageUtils.loadImageFromPath(currentPhotoPath!!)
            bitmap?.let {
                displayImage(it)
                saveImageToInternalStorage(it)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // Permissions granted, can proceed
        } else {
            Toast.makeText(this, "Необходимы разрешения для работы с фото", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupImageButtons() {
        binding.selectFromGalleryButton.setOnClickListener {
            requestPermissionsAndOpenGallery()
        }
        
        binding.takePhotoButton.setOnClickListener {
            requestPermissionsAndOpenCamera()
        }
    }
    
    private fun requestPermissionsAndOpenGallery() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (hasPermissions(permissions)) {
            galleryLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun requestPermissionsAndOpenCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        
        if (hasPermissions(permissions)) {
            openCamera()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun openCamera() {
        photoFile = ImageUtils.createImageFile(this)
        photoFile?.let { file ->
            currentPhotoPath = file.absolutePath
            val photoURI = ImageUtils.getImageUri(this, file)
            cameraLauncher.launch(photoURI)
        } ?: run {
            Toast.makeText(this, "Ошибка создания файла", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleImageUri(uri: Uri) {
        val bitmap = ImageUtils.getBitmapFromUri(this, uri)
        bitmap?.let {
            val resized = ImageUtils.resizeBitmap(it, 800, 800)
            displayImage(resized)
            saveImageToInternalStorage(resized)
        }
    }
    
    private fun displayImage(bitmap: Bitmap) {
        Glide.with(this)
            .load(bitmap)
            .into(binding.posterImageView)
    }
    
    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val filename = "poster_${movieId ?: System.currentTimeMillis()}.jpg"
        currentPhotoPath = ImageUtils.saveImageToInternalStorage(this, bitmap, filename)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupStatusToggle() {
        binding.statusToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                // Status is handled in saveMovie()
            }
        }
    }

    private fun loadMovieIfEditing() {
        movieId = intent.getLongExtra("movie_id", -1)
        if (movieId != null && movieId!! > 0) {
            binding.toolbar.title = "Редактировать фильм"
            lifecycleScope.launch {
                val movie = viewModel.getMovieById(movieId!!)
                movie?.let {
                    populateFields(it)
                }
            }
        }
    }

    private fun populateFields(movie: Movie) {
        binding.titleEditText.setText(movie.title)
        binding.yearEditText.setText(movie.year?.toString() ?: "")
        binding.directorEditText.setText(movie.director ?: "")
        binding.actorsEditText.setText(movie.actors ?: "")
        binding.descriptionEditText.setText(movie.description ?: "")
        binding.ratingBar.rating = movie.rating ?: 0f
        binding.reviewEditText.setText(movie.review ?: "")
        currentPhotoPath = movie.posterPath

        // Load poster if exists
        movie.posterPath?.let { path ->
            val bitmap = ImageUtils.loadImageFromPath(path)
            bitmap?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.posterImageView)
            } ?: run {
                Glide.with(this)
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(binding.posterImageView)
            }
        } ?: run {
            Glide.with(this)
                .load(android.R.drawable.ic_menu_gallery)
                .into(binding.posterImageView)
        }

        when (movie.status) {
            MovieStatus.WATCHED -> binding.watchedButton.isChecked = true
            MovieStatus.WANT_TO_WATCH -> binding.wantToWatchButton.isChecked = true
            MovieStatus.IN_PROGRESS -> binding.inProgressButton.isChecked = true
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                saveMovie()
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.titleEditText.text?.toString()?.trim()
        if (title.isNullOrBlank()) {
            Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveMovie() {
        val title = binding.titleEditText.text?.toString()?.trim() ?: ""
        val year = binding.yearEditText.text?.toString()?.toIntOrNull()
        val director = binding.directorEditText.text?.toString()?.trim()
        val actors = binding.actorsEditText.text?.toString()?.trim()
        val description = binding.descriptionEditText.text?.toString()?.trim()
        val rating = if (binding.ratingBar.rating > 0) binding.ratingBar.rating else null
        val review = binding.reviewEditText.text?.toString()?.trim()

        val status = when {
            binding.watchedButton.isChecked -> MovieStatus.WATCHED
            binding.inProgressButton.isChecked -> MovieStatus.IN_PROGRESS
            else -> MovieStatus.WANT_TO_WATCH
        }

        val watchDate = if (status == MovieStatus.WATCHED) {
            System.currentTimeMillis()
        } else {
            null
        }

        lifecycleScope.launch {
            val movie = if (movieId != null && movieId!! > 0) {
                // Editing existing movie
                val existingMovie = viewModel.getMovieById(movieId!!)
                existingMovie?.copy(
                    title = title,
                    year = year,
                    director = director,
                    actors = actors,
                    description = description,
                    rating = rating,
                    status = status,
                    watchDate = watchDate,
                    review = review,
                    posterPath = currentPhotoPath ?: existingMovie.posterPath
                ) ?: Movie(
                    id = movieId!!,
                    title = title,
                    year = year,
                    director = director,
                    actors = actors,
                    description = description,
                    rating = rating,
                    status = status,
                    watchDate = watchDate,
                    review = review,
                    posterPath = currentPhotoPath
                )
            } else {
                // New movie - userId will be added in ViewModel
                Movie(
                    title = title,
                    year = year,
                    director = director,
                    actors = actors,
                    description = description,
                    rating = rating,
                    status = status,
                    watchDate = watchDate,
                    review = review,
                    posterPath = currentPhotoPath
                )
            }

            lifecycleScope.launch {
                if (movieId != null && movieId!! > 0) {
                    viewModel.updateMovie(movie)
                    // Setup reminder if enabled
                    if (binding.reminderSwitch.isChecked && reminderCalendar != null) {
                        val movieForReminder = movie.copy(id = movieId!!)
                        ReminderUtils.scheduleReminder(this@AddEditMovieActivity, movieForReminder, reminderCalendar!!)
                    } else {
                        // Cancel existing reminder if disabled
                        ReminderUtils.cancelReminder(this@AddEditMovieActivity, movieId!!)
                    }
                } else {
                    // Get the inserted movie ID
                    val insertedId = viewModel.insertMovie(movie)
                    // Setup reminder if enabled
                    if (binding.reminderSwitch.isChecked && reminderCalendar != null) {
                        val movieForReminder = movie.copy(id = insertedId)
                        ReminderUtils.scheduleReminder(this@AddEditMovieActivity, movieForReminder, reminderCalendar!!)
                    }
                }

                Toast.makeText(this@AddEditMovieActivity, "Фильм сохранен", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}


