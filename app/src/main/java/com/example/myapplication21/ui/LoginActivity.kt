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
import com.example.myapplication21.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if user is already logged in
        val currentUserId = sharedPreferences.getLong("current_user_id", -1)
        if (currentUserId != -1L) {
            navigateToMain()
            return
        }

        setupButtons()
    }

    private fun setupButtons() {
        binding.loginButton.setOnClickListener {
            login()
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set underline for forgot password text
        val forgotPasswordText = android.text.SpannableString("Забыли пароль?")
        forgotPasswordText.setSpan(android.text.style.UnderlineSpan(), 0, forgotPasswordText.length, 0)
        binding.forgotPasswordTextView.text = forgotPasswordText
        
        binding.forgotPasswordTextView.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.guestButton.setOnClickListener {
            // Guest mode - use default user ID 0
            sharedPreferences.edit().putLong("current_user_id", 0).apply()
            navigateToMain()
        }
    }

    private fun login() {
        val username = binding.usernameEditText.text?.toString()?.trim() ?: ""
        val password = binding.passwordEditText.text?.toString()?.trim() ?: ""

        // Validation
        if (username.isBlank()) {
            binding.usernameInputLayout.error = "Введите имя пользователя"
            binding.usernameEditText.requestFocus()
            return
        } else {
            binding.usernameInputLayout.error = null
        }

        if (password.isBlank()) {
            binding.passwordInputLayout.error = "Введите пароль"
            binding.passwordEditText.requestFocus()
            return
        } else {
            binding.passwordInputLayout.error = null
        }

        // Disable button during login
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Вход..."

        lifecycleScope.launch {
            try {
                val userDao = MovieDatabase.getDatabase(application).userDao()
                val user = userDao.login(username, password)

                if (user != null) {
                    sharedPreferences.edit().putLong("current_user_id", user.id).apply()
                    Toast.makeText(this@LoginActivity, "Добро пожаловать, ${user.username}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    binding.passwordInputLayout.error = "Неверное имя пользователя или пароль"
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "Войти"
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "Войти"
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Введите email или имя пользователя"
        input.setPadding(50, 20, 50, 20)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Восстановление пароля")
            .setMessage("Введите email или имя пользователя для восстановления пароля")
            .setView(input)
            .setPositiveButton("Отправить") { _, _ ->
                val identifier = input.text.toString().trim()
                if (identifier.isNotBlank()) {
                    recoverPassword(identifier)
                } else {
                    Toast.makeText(this, "Введите email или имя пользователя", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun recoverPassword(identifier: String) {
        lifecycleScope.launch {
            try {
                val userDao = MovieDatabase.getDatabase(application).userDao()
                val user = userDao.getUserByUsername(identifier) ?: userDao.getUserByEmail(identifier)
                
                if (user != null) {
                    // В реальном приложении здесь должна быть отправка email с паролем
                    // Для демо просто показываем пароль
                    androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Пароль восстановлен")
                        .setMessage("Ваш пароль: ${user.password}\n\nВ реальном приложении пароль будет отправлен на email.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(this@LoginActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

