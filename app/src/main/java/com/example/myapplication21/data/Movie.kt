package com.example.myapplication21.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0, // ID пользователя-владельца
    val title: String,
    val year: Int? = null,
    val director: String? = null,
    val actors: String? = null, // Разделенные запятой
    val description: String? = null,
    val posterPath: String? = null, // Путь к локальному файлу
    val rating: Float? = null, // 1-5 звезд
    val watchDate: Long? = null, // Timestamp
    val status: MovieStatus = MovieStatus.WANT_TO_WATCH,
    val review: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MovieStatus {
    WATCHED,        // Просмотрено
    WANT_TO_WATCH,  // Хочу посмотреть
    IN_PROGRESS     // В процессе
}

