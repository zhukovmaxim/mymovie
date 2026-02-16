package com.example.myapplication21.repository

import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieDao
import com.example.myapplication21.data.MovieStatus
import com.example.myapplication21.data.SortType
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val movieDao: MovieDao) {
    fun getAllMovies(userId: Long): Flow<List<Movie>> = movieDao.getAllMovies(userId)
    
    suspend fun getMovieById(id: Long, userId: Long): Movie? = movieDao.getMovieById(id, userId)
    
    fun searchMovies(userId: Long, query: String): Flow<List<Movie>> = movieDao.searchMovies(userId, query)
    
    fun getMoviesByStatus(userId: Long, status: MovieStatus): Flow<List<Movie>> = movieDao.getMoviesByStatus(userId, status.name)
    
    fun getMoviesByRatingRange(userId: Long, minRating: Float, maxRating: Float): Flow<List<Movie>> = 
        movieDao.getMoviesByRatingRange(userId, minRating, maxRating)
    
    fun getMoviesByYearRange(userId: Long, minYear: Int, maxYear: Int): Flow<List<Movie>> = 
        movieDao.getMoviesByYearRange(userId, minYear, maxYear)
    
    fun getMoviesByYear(userId: Long, year: Int): Flow<List<Movie>> = movieDao.getMoviesByYear(userId, year)
    
    fun getMoviesSorted(userId: Long, sortType: SortType): Flow<List<Movie>> = when (sortType) {
        SortType.DATE_DESC -> movieDao.getMoviesSortedByDateDesc(userId)
        SortType.DATE_ASC -> movieDao.getMoviesSortedByDateAsc(userId)
        SortType.RATING_DESC -> movieDao.getMoviesSortedByRatingDesc(userId)
        SortType.RATING_ASC -> movieDao.getMoviesSortedByRatingAsc(userId)
        SortType.YEAR_DESC -> movieDao.getMoviesSortedByYearDesc(userId)
        SortType.YEAR_ASC -> movieDao.getMoviesSortedByYearAsc(userId)
        SortType.TITLE_ASC -> movieDao.getMoviesSortedByTitleAsc(userId)
        SortType.TITLE_DESC -> movieDao.getMoviesSortedByTitleDesc(userId)
    }
    
    fun getTotalCount(userId: Long): Flow<Int> = movieDao.getTotalCount(userId)
    
    fun getWatchedCount(userId: Long): Flow<Int> = movieDao.getWatchedCount(userId)
    
    fun getAverageRating(userId: Long): Flow<Float?> = movieDao.getAverageRating(userId)
    
    suspend fun insertMovie(movie: Movie): Long = movieDao.insertMovie(movie)
    
    suspend fun updateMovie(movie: Movie) = movieDao.updateMovie(movie)
    
    suspend fun deleteMovie(movie: Movie) = movieDao.deleteMovie(movie)
    
    suspend fun deleteMovieById(id: Long) = movieDao.deleteMovieById(id)
    
    suspend fun getAllMoviesList(): List<Movie> {
        // Helper method for export - will be implemented with coroutines
        return emptyList() // Placeholder
    }
    
    // Extended statistics
    suspend fun getFavoriteDirector(userId: Long): String? = movieDao.getFavoriteDirector(userId)
    
    suspend fun getMostWatchedYear(userId: Long): Int? = movieDao.getMostWatchedYear(userId)
    
    suspend fun getBestRatedYear(userId: Long): Int? = movieDao.getBestRatedYear(userId)
}

