package com.example.myapplication21.utils

import android.content.Context
import android.os.Environment
import com.example.myapplication21.data.Movie
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    suspend fun exportToJson(context: Context, movies: List<Movie>): String? = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(movies)
            val fileName = "movies_export_${dateFormat.format(Date())}.json"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileWriter(file).use { writer ->
                writer.write(json)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToCsv(context: Context, movies: List<Movie>): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "movies_export_${dateFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileWriter(file).use { writer ->
                // Write header
                writer.append("ID,Название,Год,Режиссер,Актеры,Описание,Оценка,Статус,Дата просмотра,Рецензия,Дата создания\n")
                
                // Write data
                movies.forEach { movie ->
                    writer.append("${movie.id},")
                    writer.append("\"${movie.title.replace("\"", "\"\"")}\",")
                    writer.append("${movie.year ?: ""},")
                    writer.append("\"${(movie.director ?: "").replace("\"", "\"\"")}\",")
                    writer.append("\"${(movie.actors ?: "").replace("\"", "\"\"")}\",")
                    writer.append("\"${(movie.description ?: "").replace("\"", "\"\"")}\",")
                    writer.append("${movie.rating ?: ""},")
                    writer.append("${movie.status.name},")
                    writer.append("${movie.watchDate ?: ""},")
                    writer.append("\"${(movie.review ?: "").replace("\"", "\"\"")}\",")
                    writer.append("${movie.createdAt}\n")
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


