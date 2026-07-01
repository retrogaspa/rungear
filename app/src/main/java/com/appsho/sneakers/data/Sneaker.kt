package com.appsho.sneakers.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sneakers")
data class Sneaker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
