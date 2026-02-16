package com.example.myapplication21.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieListDao {
    @Query("SELECT * FROM movie_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserLists(userId: Long): Flow<List<MovieList>>
    
    @Query("SELECT * FROM movie_lists WHERE id = :listId")
    suspend fun getListById(listId: Long): MovieList?
    
    @Query("SELECT m.* FROM movies m INNER JOIN movie_list_items mli ON m.id = mli.movieId WHERE mli.listId = :listId ORDER BY mli.addedAt DESC")
    fun getMoviesInList(listId: Long): Flow<List<Movie>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: MovieList): Long
    
    @Update
    suspend fun updateList(list: MovieList)
    
    @Delete
    suspend fun deleteList(list: MovieList)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovieToList(item: MovieListItem)
    
    @Query("DELETE FROM movie_list_items WHERE listId = :listId AND movieId = :movieId")
    suspend fun removeMovieFromList(listId: Long, movieId: Long)
    
    @Query("DELETE FROM movie_list_items WHERE listId = :listId")
    suspend fun clearList(listId: Long)
}

