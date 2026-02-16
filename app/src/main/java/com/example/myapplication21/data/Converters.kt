package com.example.myapplication21.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMovieStatus(status: MovieStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMovieStatus(status: String): MovieStatus {
        return MovieStatus.valueOf(status)
    }
}


