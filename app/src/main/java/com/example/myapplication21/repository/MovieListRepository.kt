package com.example.myapplication21.repository

import com.example.myapplication21.data.Movie
import com.example.myapplication21.data.MovieList
import com.example.myapplication21.data.MovieListItem
import com.example.myapplication21.data.MovieListDao
import kotlinx.coroutines.flow.Flow

class MovieListRepository(private val movieListDao: MovieListDao) {
    fun getUserLists(userId: Long): Flow<List<MovieList>> = movieListDao.getUserLists(userId)
    
    suspend fun getListById(listId: Long): MovieList? = movieListDao.getListById(listId)
    
    fun getMoviesInList(listId: Long): Flow<List<Movie>> = movieListDao.getMoviesInList(listId)
    
    suspend fun insertList(list: MovieList): Long = movieListDao.insertList(list)
    
    suspend fun updateList(list: MovieList) = movieListDao.updateList(list)
    
    suspend fun deleteList(list: MovieList) = movieListDao.deleteList(list)
    
    suspend fun addMovieToList(listId: Long, movieId: Long) = movieListDao.addMovieToList(MovieListItem(listId, movieId))
    
    suspend fun removeMovieFromList(listId: Long, movieId: Long) = movieListDao.removeMovieFromList(listId, movieId)
}

