package com.example.collectionsapp.data.database.dao

import androidx.room.*
import com.example.collectionsapp.data.database.entities.Collection
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): Collection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection): Long

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: Long)
}