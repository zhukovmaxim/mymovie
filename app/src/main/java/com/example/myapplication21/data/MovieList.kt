package com.example.myapplication21.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_lists")
data class MovieList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String? = null,
    val coverMovieId: Long? = null, // ID фильма для обложки
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "movie_list_items", primaryKeys = ["listId", "movieId"])
data class MovieListItem(
    val listId: Long,
    val movieId: Long,
    val addedAt: Long = System.currentTimeMillis()
)


