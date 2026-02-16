package com.example.myapplication21.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.myapplication21.MainActivity
import com.example.myapplication21.data.MovieDatabase
import com.example.myapplication21.data.User
import com.example.myapplication21.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRegisterButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val username = binding.usernameEditText.text?.toString()?.trim() ?: ""
        val email = binding.emailEditText.text?.toString()?.trim() ?: ""
        val password = binding.passwordEditText.text?.toString() ?: ""
        val confirmPassword = binding.confirmPasswordEditText.text?.toString() ?: ""

        // Clear previous errors
        binding.usernameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null

        // Validation
        var hasError = false

        if (username.isBlank()) {
            binding.usernameInputLayout.error = "Введите имя пользователя"
            hasError = true
        } else if (username.length < 3) {
            binding.usernameInputLayout.error = "Имя должно содержать минимум 3 символа"
            hasError = true
        }

        if (email.isBlank()) {
            binding.emailInputLayout.error = "Введите email"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Некорректный email"
            hasError = true
        }

        if (password.isBlank()) {
            binding.passwordInputLayout.error = "Введите пароль"
            hasError = true
        } else if (password.length < 4) {
            binding.passwordInputLayout.error = "Пароль должен содержать минимум 4 символа"
            hasError = true
        }

        if (confirmPassword.isBlank()) {
            binding.confirmPasswordInputLayout.error = "Подтвердите пароль"
            hasError = true
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Пароли не совпадают"
            hasError = true
        }

        if (hasError) {
            return
        }

        // Disable button during registration
        binding.registerButton.isEnabled = false
        binding.registerButton.text = "Регистрация..."

        lifecycleScope.launch {
            try {
                val userDao = MovieDatabase.getDatabase(application).userDao()
                
                // Check if username already exists
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check if email already exists
                val existingEmail = userDao.getUserByEmail(email)
                if (existingEmail != null) {
                    Toast.makeText(this@RegisterActivity, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = User(
                    username = username,
                    email = email,
                    password = password // В реальном приложении должен быть хеширован
                )

                val userId = userDao.insertUser(user)
                Toast.makeText(this@RegisterActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                
                // Auto login after registration
                getSharedPreferences("app_preferences", MODE_PRIVATE)
                    .edit()
                    .putLong("current_user_id", userId)
                    .apply()

                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.registerButton.isEnabled = true
                binding.registerButton.text = "Зарегистрироваться"
            }
        }
    }
}

