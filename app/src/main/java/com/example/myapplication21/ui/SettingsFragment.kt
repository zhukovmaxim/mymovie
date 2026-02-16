package com.example.myapplication21.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication21.databinding.FragmentSettingsBinding
import com.example.myapplication21.utils.ExportUtils
import com.example.myapplication21.utils.ReminderUtils
import com.example.myapplication21.viewmodel.MovieViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: MovieViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        
        setupThemeToggle()
        setupExportButtons()
        setupClearCache()
        setupReminders()
    }
    
    private fun setupThemeToggle() {
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.lightThemeButton.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.darkThemeButton.isChecked = true
            else -> binding.systemThemeButton.isChecked = true
        }
        
        binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val themeMode = when (checkedId) {
                    binding.lightThemeButton.id -> AppCompatDelegate.MODE_NIGHT_NO
                    binding.darkThemeButton.id -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(themeMode)
                sharedPreferences.edit().putInt("theme_mode", themeMode).apply()
            }
        }
    }
    
    private fun setupExportButtons() {
        binding.exportJsonButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val movies = viewModel.allMovies.value
                    val filePath = ExportUtils.exportToJson(requireContext(), movies)
                    if (filePath != null) {
                        Toast.makeText(requireContext(), "Экспорт завершен: $filePath", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка экспорта", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        binding.exportCsvButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val movies = viewModel.allMovies.value
                    val filePath = ExportUtils.exportToCsv(requireContext(), movies)
                    if (filePath != null) {
                        Toast.makeText(requireContext(), "Экспорт завершен: $filePath", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка экспорта", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupClearCache() {
        binding.clearCacheButton.setOnClickListener {
            // Clear cache logic
            Toast.makeText(requireContext(), "Кэш очищен", Toast.LENGTH_SHORT).show()
        }
        
        // Add logout button functionality
        binding.logoutButton.setOnClickListener {
            sharedPreferences.edit().remove("current_user_id").apply()
            val intent = android.content.Intent(requireContext(), com.example.myapplication21.ui.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
    
    private fun setupReminders() {
        val reminderEnabled = sharedPreferences.getBoolean("daily_reminder_enabled", false)
        val reminderHour = sharedPreferences.getInt("daily_reminder_hour", 20)
        val reminderMinute = sharedPreferences.getInt("daily_reminder_minute", 0)
        
        binding.dailyReminderSwitch.isChecked = reminderEnabled
        updateReminderTimeButton(reminderHour, reminderMinute)
        
        binding.dailyReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("daily_reminder_enabled", isChecked).apply()
            if (isChecked) {
                ReminderUtils.scheduleDailyReminder(requireContext(), reminderHour, reminderMinute)
                Toast.makeText(requireContext(), "Ежедневные напоминания включены", Toast.LENGTH_SHORT).show()
            } else {
                ReminderUtils.cancelDailyReminder(requireContext())
                Toast.makeText(requireContext(), "Ежедневные напоминания выключены", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.reminderTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    sharedPreferences.edit()
                        .putInt("daily_reminder_hour", hourOfDay)
                        .putInt("daily_reminder_minute", minute)
                        .apply()
                    updateReminderTimeButton(hourOfDay, minute)
                    
                    if (binding.dailyReminderSwitch.isChecked) {
                        ReminderUtils.cancelDailyReminder(requireContext())
                        ReminderUtils.scheduleDailyReminder(requireContext(), hourOfDay, minute)
                    }
                },
                reminderHour,
                reminderMinute,
                true
            ).show()
        }
    }
    
    private fun updateReminderTimeButton(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        binding.reminderTimeButton.text = "Время: $timeString"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

