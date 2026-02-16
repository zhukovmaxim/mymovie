package com.example.myapplication21.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication21.databinding.FragmentStatisticsBinding
import com.example.myapplication21.viewmodel.MovieViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MovieViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStatistics()
        loadExtendedStatistics()
    }
    
    private fun observeStatistics() {
        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.totalMoviesTextView.text = count.toString()
        }
        
        viewModel.watchedCount.observe(viewLifecycleOwner) { count ->
            binding.watchedMoviesTextView.text = count.toString()
        }
        
        viewModel.averageRating.observe(viewLifecycleOwner) { rating ->
            val formattedRating = if (rating != null && rating > 0) {
                DecimalFormat("#.#").format(rating)
            } else {
                "Нет оценок"
            }
            binding.averageRatingTextView.text = formattedRating
        }
    }
    
    private fun loadExtendedStatistics() {
        lifecycleScope.launch {
            try {
                val favoriteDirector = viewModel.getFavoriteDirector()
                binding.favoriteDirectorTextView.text = favoriteDirector ?: "Не указан"
                
                val mostWatchedYear = viewModel.getMostWatchedYear()
                binding.mostWatchedYearTextView.text = mostWatchedYear?.toString() ?: "-"
                
                val bestRatedYear = viewModel.getBestRatedYear()
                binding.bestRatedYearTextView.text = bestRatedYear?.toString() ?: "-"
            } catch (e: Exception) {
                // Handle error silently
                binding.favoriteDirectorTextView.text = "Не указан"
                binding.mostWatchedYearTextView.text = "-"
                binding.bestRatedYearTextView.text = "-"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

