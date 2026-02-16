package com.example.myapplication21.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String, // В реальном приложении должен быть хеширован
    val createdAt: Long = System.currentTimeMillis()
)


