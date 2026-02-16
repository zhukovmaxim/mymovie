package com.example.myapplication21.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.example.myapplication21.MainActivity
import com.example.myapplication21.R
import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.data.SortType
import com.example.myapplication21.databinding.FragmentMovieListBinding
import com.example.myapplication21.viewmodel.MovieViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MovieListFragment : Fragment() {
    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MovieViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        setupFab()
        setupSortAndFilter()
        setupSwipeRefresh()
        observeMovies()
    }
    
    private fun setupRecyclerView() {
        adapter = MovieAdapter { movie ->
            // Navigate to detail screen
            (activity as? MainActivity)?.navigateToMovieDetail(movie.id)
        }
        
        binding.moviesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.moviesRecyclerView.adapter = adapter
        
        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = adapter.currentList[position]
                viewModel.deleteMovie(movie)
                Toast.makeText(requireContext(), "Фильм удален", Toast.LENGTH_SHORT).show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.moviesRecyclerView)
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    viewModel.searchMovies(s?.toString() ?: "")
                } catch (e: Exception) {
                    // Ignore search errors
                }
            }
        })
    }
    
    private fun setupFab() {
        binding.addMovieFab.setOnClickListener {
            try {
                (activity as? MainActivity)?.navigateToAddMovie()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupSortAndFilter() {
        binding.sortButton.setOnClickListener {
            showSortDialog()
        }
        
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
    }
    
    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "По дате (новые сначала)",
            "По дате (старые сначала)",
            "По оценке (высокие сначала)",
            "По оценке (низкие сначала)",
            "По году (новые сначала)",
            "По году (старые сначала)",
            "По алфавиту (А-Я)",
            "По алфавиту (Я-А)"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Сортировка")
            .setItems(sortOptions) { _, which ->
                val sortType = when (which) {
                    0 -> SortType.DATE_DESC
                    1 -> SortType.DATE_ASC
                    2 -> SortType.RATING_DESC
                    3 -> SortType.RATING_ASC
                    4 -> SortType.YEAR_DESC
                    5 -> SortType.YEAR_ASC
                    6 -> SortType.TITLE_ASC
                    7 -> SortType.TITLE_DESC
                    else -> SortType.DATE_DESC
                }
                viewModel.setSortType(sortType)
            }
            .show()
    }
    
    private fun showFilterDialog() {
        val filterOptions = arrayOf(
            "По статусу",
            "По году выпуска",
            "По оценке",
            "Сбросить все фильтры"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Фильтрация")
            .setItems(filterOptions) { _, which ->
                when (which) {
                    0 -> showStatusFilterDialog()
                    1 -> showYearFilterDialog()
                    2 -> showRatingFilterDialog()
                    3 -> viewModel.clearAllFilters()
                }
            }
            .show()
    }
    
    private fun showStatusFilterDialog() {
        val filterOptions = arrayOf(
            "Все фильмы",
            "Просмотренные",
            "Хочу посмотреть",
            "В процессе"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Фильтр по статусу")
            .setItems(filterOptions) { _, which ->
                val status = when (which) {
                    0 -> null
                    1 -> MovieStatus.WATCHED
                    2 -> MovieStatus.WANT_TO_WATCH
                    3 -> MovieStatus.IN_PROGRESS
                    else -> null
                }
                viewModel.filterByStatus(status)
            }
            .show()
    }
    
    private fun showYearFilterDialog() {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        // Simple implementation - can be enhanced with NumberPicker
        AlertDialog.Builder(requireContext())
            .setTitle("Фильтр по году")
            .setMessage("Введите диапазон годов (например: 2000-2020)")
            .setPositiveButton("Применить") { _, _ ->
                // TODO: Implement year range filter with NumberPicker
                Toast.makeText(requireContext(), "Фильтр по году будет реализован", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun showRatingFilterDialog() {
        val ratingOptions = arrayOf(
            "Все оценки",
            "1-2 звезды",
            "3-4 звезды",
            "4-5 звезд",
            "Только 5 звезд"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Фильтр по оценке")
            .setItems(ratingOptions) { _, which ->
                when (which) {
                    0 -> viewModel.filterByRatingRange(null, null)
                    1 -> viewModel.filterByRatingRange(1f, 2f)
                    2 -> viewModel.filterByRatingRange(3f, 4f)
                    3 -> viewModel.filterByRatingRange(4f, 5f)
                    4 -> viewModel.filterByRatingRange(5f, 5f)
                }
            }
            .show()
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAllMovies()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun observeMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.allMovies.collect { movies ->
                    if (::adapter.isInitialized) {
                        adapter.submitList(movies)
                        binding.emptyStateTextView.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                // Handle error
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.emptyStateTextView.text = "Ошибка загрузки данных"
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

