package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM saved_items ORDER BY isPinned DESC, timestamp DESC")
    fun getAllItems(): Flow<List<SavedItemEntity>>

    @Query("SELECT * FROM saved_items WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getItemsByCategory(category: String): Flow<List<SavedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SavedItemEntity): Long

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("UPDATE saved_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean)

    @Query("DELETE FROM saved_items WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Query("DELETE FROM saved_items")
    suspend fun clearAll()
}
