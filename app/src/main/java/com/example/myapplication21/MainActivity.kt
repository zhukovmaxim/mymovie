package com.example.myapplication21

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication21.databinding.ActivityMainBinding
import com.example.myapplication21.ui.AddEditMovieActivity
import com.example.myapplication21.ui.LoginActivity
import com.example.myapplication21.ui.MovieDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // Check if user is logged in
        val currentUserId = sharedPreferences.getLong("current_user_id", -1)
        if (currentUserId == -1L) {
            navigateToLogin()
            return
        }
        
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Setup navigation after view is created
        binding.root.post {
            setupBottomNavigation()
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun setupBottomNavigation() {
        try {
            val navController = findNavController(R.id.fragmentContainer)
            binding.bottomNavigationView.setupWithNavController(navController)
        } catch (e: Exception) {
            // Navigation might not be ready yet, try again later
            binding.root.postDelayed({
                try {
                    val navController = findNavController(R.id.fragmentContainer)
                    binding.bottomNavigationView.setupWithNavController(navController)
                } catch (ex: Exception) {
                    // Ignore if still fails
                }
            }, 100)
        }
    }
    
    fun navigateToAddMovie() {
        val intent = Intent(this, AddEditMovieActivity::class.java)
        startActivity(intent)
    }
    
    fun navigateToMovieDetail(movieId: Long) {
        val intent = Intent(this, MovieDetailActivity::class.java)
        intent.putExtra("movie_id", movieId)
        startActivity(intent)
    }
}