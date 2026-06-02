package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {
    @Query("SELECT * FROM storage_items WHERE isDeleted = 0")
    fun getAllActiveItems(): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE type = :type AND isDeleted = 0")
    fun getActiveItemsByType(type: String): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE isOffloaded = 1 AND isDeleted = 0")
    fun getOffloadedItems(): Flow<List<StorageItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<StorageItem>)

    @Update
    suspend fun updateItem(item: StorageItem)

    @Update
    suspend fun updateItems(items: List<StorageItem>)

    @Query("UPDATE storage_items SET isDeleted = 1 WHERE type = :type")
    suspend fun deleteItemsByType(type: String)

    @Query("UPDATE storage_items SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM storage_items")
    suspend fun clearDatabase()

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}
