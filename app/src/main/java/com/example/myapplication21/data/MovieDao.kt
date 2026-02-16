package com.example.myapplication21.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllMovies(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE id = :id AND userId = :userId")
    suspend fun getMovieById(id: Long, userId: Long): Movie?
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR director LIKE '%' || :query || '%' OR actors LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchMovies(userId: Long, query: String): Flow<List<Movie>>
    
    // Sorting queries with userId
    @Query("SELECT * FROM movies WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMoviesSortedByDateDesc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId ORDER BY createdAt ASC")
    fun getMoviesSortedByDateAsc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND rating IS NOT NULL ORDER BY rating DESC, createdAt DESC")
    fun getMoviesSortedByRatingDesc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND rating IS NOT NULL ORDER BY rating ASC, createdAt DESC")
    fun getMoviesSortedByRatingAsc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND year IS NOT NULL ORDER BY year DESC, createdAt DESC")
    fun getMoviesSortedByYearDesc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND year IS NOT NULL ORDER BY year ASC, createdAt DESC")
    fun getMoviesSortedByYearAsc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId ORDER BY title ASC")
    fun getMoviesSortedByTitleAsc(userId: Long): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId ORDER BY title DESC")
    fun getMoviesSortedByTitleDesc(userId: Long): Flow<List<Movie>>
    
    // Filtering queries with userId
    @Query("SELECT * FROM movies WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    fun getMoviesByStatus(userId: Long, status: String): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND rating >= :minRating AND rating <= :maxRating ORDER BY rating DESC, createdAt DESC")
    fun getMoviesByRatingRange(userId: Long, minRating: Float, maxRating: Float): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND year >= :minYear AND year <= :maxYear ORDER BY year DESC, createdAt DESC")
    fun getMoviesByYearRange(userId: Long, minYear: Int, maxYear: Int): Flow<List<Movie>>
    
    @Query("SELECT * FROM movies WHERE userId = :userId AND year = :year ORDER BY createdAt DESC")
    fun getMoviesByYear(userId: Long, year: Int): Flow<List<Movie>>
    
    // Statistics queries with userId
    @Query("SELECT COUNT(*) FROM movies WHERE userId = :userId")
    fun getTotalCount(userId: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM movies WHERE userId = :userId AND status = 'WATCHED'")
    fun getWatchedCount(userId: Long): Flow<Int>
    
    @Query("SELECT AVG(rating) FROM movies WHERE userId = :userId AND rating IS NOT NULL")
    fun getAverageRating(userId: Long): Flow<Float?>
    
    // Extended statistics - simplified queries
    @Query("SELECT director FROM movies WHERE userId = :userId AND director IS NOT NULL GROUP BY director ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getFavoriteDirector(userId: Long): String?
    
    @Query("SELECT year FROM movies WHERE userId = :userId AND year IS NOT NULL GROUP BY year ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostWatchedYear(userId: Long): Int?
    
    @Query("SELECT year FROM movies WHERE userId = :userId AND year IS NOT NULL AND rating IS NOT NULL GROUP BY year ORDER BY AVG(rating) DESC LIMIT 1")
    suspend fun getBestRatedYear(userId: Long): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie): Long
    
    @Update
    suspend fun updateMovie(movie: Movie)
    
    @Delete
    suspend fun deleteMovie(movie: Movie)
    
    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Long)
}

