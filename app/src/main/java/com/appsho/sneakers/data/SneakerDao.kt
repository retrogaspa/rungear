package com.appsho.sneakers.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SneakerDao {
    @Query("SELECT * FROM sneakers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Sneaker>>

    @Query("SELECT * FROM sneakers WHERE id = :id")
    suspend fun getById(id: Long): Sneaker?

    @Query("SELECT EXISTS(SELECT 1 FROM sneakers WHERE iconPath = :iconPath)")
    suspend fun existsByIconPath(iconPath: String): Boolean

    @Insert
    suspend fun insert(sneaker: Sneaker): Long

    @Delete
    suspend fun delete(sneaker: Sneaker)
}
