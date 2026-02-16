package com.example.myapplication21.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieDatabase
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.data.SortType
import com.example.myapplication21.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MovieRepository
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
    private val currentUserId: Long = sharedPreferences.getLong("current_user_id", 0)
    
    val allMovies: StateFlow<List<Movie>>
    
    val totalCount: LiveData<Int>
    val watchedCount: LiveData<Int>
    val averageRating: LiveData<Float?>
    
    private val searchQuery = MutableStateFlow<String>("")
    private val sortType = MutableStateFlow<SortType>(SortType.DATE_DESC)
    private val filterStatus = MutableStateFlow<MovieStatus?>(null)
    private val filterYearMin = MutableStateFlow<Int?>(null)
    private val filterYearMax = MutableStateFlow<Int?>(null)
    private val filterRatingMin = MutableStateFlow<Float?>(null)
    private val filterRatingMax = MutableStateFlow<Float?>(null)
    
    init {
        val movieDao = MovieDatabase.getDatabase(application).movieDao()
        repository = MovieRepository(movieDao)
        
        totalCount = repository.getTotalCount(currentUserId).asLiveData()
        watchedCount = repository.getWatchedCount(currentUserId).asLiveData()
        averageRating = repository.getAverageRating(currentUserId).asLiveData()
        
        // Initialize allMovies with search, sort, and filter
        allMovies = try {
            // Combine filters first (max 6 params in combine)
            val filtersCombined = kotlinx.coroutines.flow.combine(
                filterStatus,
                filterYearMin,
                filterYearMax,
                filterRatingMin,
                filterRatingMax
            ) { status, yearMin, yearMax, ratingMin, ratingMax ->
                FilterParamsData(status, yearMin, yearMax, ratingMin, ratingMax)
            }
            
            // Then combine with search and sort
            kotlinx.coroutines.flow.combine(
                searchQuery,
                sortType,
                filtersCombined
            ) { query, sort, filterData ->
                FilterParams(query, sort, filterData.status, filterData.yearMin, filterData.yearMax, filterData.ratingMin, filterData.ratingMax)
            }.flatMapLatest { params ->
                // Priority: search > filters > sort
                when {
                    !params.query.isBlank() -> {
                        repository.searchMovies(currentUserId, params.query)
                    }
                    params.status != null || params.yearMin != null || params.ratingMin != null -> {
                        // Apply filters
                        var flow = repository.getAllMovies(currentUserId)
                        // Apply status filter
                        if (params.status != null) {
                            flow = repository.getMoviesByStatus(currentUserId, params.status)
                        }
                        // Apply year range filter
                        if (params.yearMin != null && params.yearMax != null) {
                            flow = repository.getMoviesByYearRange(currentUserId, params.yearMin, params.yearMax)
                        }
                        // Apply rating range filter
                        if (params.ratingMin != null && params.ratingMax != null) {
                            flow = repository.getMoviesByRatingRange(currentUserId, params.ratingMin, params.ratingMax)
                        }
                        flow
                    }
                    else -> {
                        repository.getMoviesSorted(currentUserId, params.sort)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        } catch (e: Exception) {
            MutableStateFlow<List<Movie>>(emptyList()).asStateFlow()
        }
    }
    
    private data class FilterParamsData(
        val status: MovieStatus?,
        val yearMin: Int?,
        val yearMax: Int?,
        val ratingMin: Float?,
        val ratingMax: Float?
    )
    
    private data class FilterParams(
        val query: String,
        val sort: SortType,
        val status: MovieStatus?,
        val yearMin: Int?,
        val yearMax: Int?,
        val ratingMin: Float?,
        val ratingMax: Float?
    )
    
    fun loadAllMovies() {
        searchQuery.value = ""
        filterStatus.value = null
    }
    
    fun searchMovies(query: String) {
        searchQuery.value = query
    }
    
    fun setSortType(sort: SortType) {
        sortType.value = sort
    }
    
    fun filterByStatus(status: MovieStatus?) {
        filterStatus.value = status
    }
    
    fun getMoviesByStatus(status: MovieStatus) {
        filterStatus.value = status
    }
    
    fun filterByYearRange(minYear: Int?, maxYear: Int?) {
        filterYearMin.value = minYear
        filterYearMax.value = maxYear
    }
    
    fun filterByRatingRange(minRating: Float?, maxRating: Float?) {
        filterRatingMin.value = minRating
        filterRatingMax.value = maxRating
    }
    
    fun clearAllFilters() {
        filterStatus.value = null
        filterYearMin.value = null
        filterYearMax.value = null
        filterRatingMin.value = null
        filterRatingMax.value = null
        searchQuery.value = ""
    }
    
    suspend fun insertMovie(movie: Movie): Long {
        val movieWithUser = movie.copy(userId = currentUserId)
        return repository.insertMovie(movieWithUser)
    }
    
    fun insertMovieAsync(movie: Movie) {
        viewModelScope.launch {
            insertMovie(movie)
        }
    }
    
    fun updateMovie(movie: Movie) {
        viewModelScope.launch {
            val movieWithUser = movie.copy(userId = currentUserId)
            repository.updateMovie(movieWithUser)
        }
    }
    
    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.deleteMovie(movie)
        }
    }
    
    suspend fun getMovieById(id: Long): Movie? {
        return repository.getMovieById(id, currentUserId)
    }
    
    fun getMovieByIdAsync(id: Long, callback: (Movie?) -> Unit) {
        viewModelScope.launch {
            val movie = repository.getMovieById(id, currentUserId)
            callback(movie)
        }
    }
    
    // Extended statistics
    suspend fun getFavoriteDirector(): String? {
        return repository.getFavoriteDirector(currentUserId)
    }
    
    suspend fun getMostWatchedYear(): Int? {
        return repository.getMostWatchedYear(currentUserId)
    }
    
    suspend fun getBestRatedYear(): Int? {
        return repository.getBestRatedYear(currentUserId)
    }
}

