package com.example.collectionsapp.data.database.dao

import androidx.room.*
import com.example.collectionsapp.data.database.entities.CollectionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionItemDao {
    @Query("SELECT * FROM collection_items WHERE collectionId = :collectionId ORDER BY createdAt DESC")
    fun getItemsByCollectionId(collectionId: Long): Flow<List<CollectionItem>>

    // ✅ هذه الدالة ضرورية للتصدير
    @Query("SELECT * FROM collection_items WHERE collectionId = :collectionId ORDER BY createdAt DESC")
    suspend fun getItemsByCollectionIdOnce(collectionId: Long): List<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    suspend fun getItemById(id: Long): CollectionItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CollectionItem): Long

    @Update
    suspend fun updateItem(item: CollectionItem)

    @Delete
    suspend fun deleteItem(item: CollectionItem)

    @Query("DELETE FROM collection_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
}