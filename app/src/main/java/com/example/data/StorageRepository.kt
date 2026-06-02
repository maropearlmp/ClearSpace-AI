package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StorageRepository(private val storageDao: StorageDao) {

    val allActiveItems: Flow<List<StorageItem>> = storageDao.getAllActiveItems()
    val allSettings: Flow<List<AppSetting>> = storageDao.getAllSettings()
    val offloadedItems: Flow<List<StorageItem>> = storageDao.getOffloadedItems()

    fun getActiveItemsByType(type: String): Flow<List<StorageItem>> {
        return storageDao.getActiveItemsByType(type)
    }

    suspend fun insertItems(items: List<StorageItem>) {
        storageDao.insertItems(items)
    }

    suspend fun updateItem(item: StorageItem) {
        storageDao.updateItem(item)
    }

    suspend fun updateItems(items: List<StorageItem>) {
        storageDao.updateItems(items)
    }

    suspend fun deleteItemsByType(type: String) {
        storageDao.deleteItemsByType(type)
    }

    suspend fun deleteItemById(id: Int) {
        storageDao.deleteItemById(id)
    }

    suspend fun saveSetting(key: String, value: Boolean) {
        storageDao.saveSetting(AppSetting(key, value))
    }

    suspend fun clearDatabase() {
        storageDao.clearDatabase()
    }

    suspend fun prepopulateIfEmpty() {
        // Check if settings are present or storage_items is empty
        val currentItems = allActiveItems.first()
        if (currentItems.isEmpty()) {
            val defaultItems = listOf(
                // 1. Duplicate Photos (Total target size in design: 32 GB)
                StorageItem(
                    name = "IMG_6421_Graduation_Sharp.jpg",
                    type = "PHOTO",
                    sizeBytes = 8_200_000_000L, // 8.2 GB
                    groupKey = "Graduation Duplicates",
                    fileCount = 3
                ),
                StorageItem(
                    name = "IMG_6422_Graduation_Similar_1.jpg",
                    type = "PHOTO",
                    sizeBytes = 8_100_000_000L, // 8.1 GB
                    groupKey = "Graduation Duplicates",
                    fileCount = 1
                ),
                StorageItem(
                    name = "IMG_6423_Graduation_Similar_2.jpg",
                    type = "PHOTO",
                    sizeBytes = 8_000_000_000L, // 8.0 GB
                    groupKey = "Graduation Duplicates",
                    fileCount = 1
                ),
                StorageItem(
                    name = "IMG_8849_Sunset_Beach.heic",
                    type = "PHOTO",
                    sizeBytes = 4_800_000_000L, // 4.8 GB
                    groupKey = "Sunset Beach Duplicates",
                    fileCount = 4
                ),
                StorageItem(
                    name = "IMG_8850_Sunset_Beach_Burst_1.heic",
                    type = "PHOTO",
                    sizeBytes = 4_700_000_000L, // 4.7 GB
                    groupKey = "Sunset Beach Duplicates",
                    fileCount = 1
                ),
                StorageItem(
                    name = "IMG_8851_Sunset_Beach_Burst_2.heic",
                    type = "PHOTO",
                    sizeBytes = 4_600_000_000L, // 4.6 GB
                    groupKey = "Sunset Beach Duplicates",
                    fileCount = 1
                ),
                StorageItem(
                    name = "IMG_4301_Food_Brunch.jpg",
                    type = "PHOTO",
                    sizeBytes = 2_100_000_000L, // 2.1 GB
                    groupKey = "Brunch Table",
                    fileCount = 2
                ),
                StorageItem(
                    name = "IMG_4302_Food_Brunch_Alt.jpg",
                    type = "PHOTO",
                    sizeBytes = 2_050_000_000L, // 2.05 GB
                    groupKey = "Brunch Table",
                    fileCount = 1
                ),

                // 2. Large Videos (Total target size: 14 GB)
                StorageItem(
                    name = "Concert_Encore_2025_4K.mp4",
                    type = "VIDEO",
                    sizeBytes = 5_200_000_000L, // 5.2 GB
                    addedTime = System.currentTimeMillis() - 12 * 24 * 3600 * 1000L
                ),
                StorageItem(
                    name = "Roadtrip_Scenic_Drive.mp4",
                    type = "VIDEO",
                    sizeBytes = 4_800_000_000L, // 4.8 GB
                    addedTime = System.currentTimeMillis() - 24 * 24 * 3600 * 1000L
                ),
                StorageItem(
                    name = "Cat_Pouncing_SlowMo.mp4",
                    type = "VIDEO",
                    sizeBytes = 2_100_000_000L, // 2.1 GB
                    addedTime = System.currentTimeMillis() - 4 * 24 * 3600 * 1000L
                ),
                StorageItem(
                    name = "Birthday_Cake_Wishes.mp4",
                    type = "VIDEO",
                    sizeBytes = 1_900_000_000L, // 1.9 GB
                    addedTime = System.currentTimeMillis() - 15 * 24 * 3600 * 1000L
                ),

                // 3. Junk Files (Total target size: 800 MB)
                StorageItem(
                    name = "Instagram Cached Feed Images",
                    type = "JUNK",
                    sizeBytes = 350_000_000L // 350 MB
                ),
                StorageItem(
                    name = "TikTok Video Temp Buffer",
                    type = "JUNK",
                    sizeBytes = 280_000_000L // 280 MB
                ),
                StorageItem(
                    name = "Chrome Web Cache",
                    type = "JUNK",
                    sizeBytes = 170_000_000L // 170 MB
                ),

                // 4. Unused Downloads (Total target size: 5 GB)
                StorageItem(
                    name = "User_Manual_Projector_Draft.pdf",
                    type = "DOWNLOAD",
                    sizeBytes = 1_200_000_000L, // 1.2 GB
                    addedTime = System.currentTimeMillis() - 190 * 24 * 3600 * 1000L
                ),
                StorageItem(
                    name = "Database_Backup_Oct_2025.tar.gz",
                    type = "DOWNLOAD",
                    sizeBytes = 2_300_000_000L, // 2.3 GB
                    addedTime = System.currentTimeMillis() - 210 * 24 * 3600 * 1000L
                ),
                StorageItem(
                    name = "Unused_Figma_Design_Export.zip",
                    type = "DOWNLOAD",
                    sizeBytes = 1_500_000_000L, // 1.5 GB
                    addedTime = System.currentTimeMillis() - 220 * 24 * 3600 * 1000L
                ),

                // 5. Pre-offloaded items in Secure Cloud Vault (For display in Vault screen, representing completed work)
                StorageItem(
                    name = "Graduation_Video.mp4",
                    type = "VIDEO",
                    sizeBytes = 2_400_000_000L, // 2.4 GB
                    addedTime = System.currentTimeMillis() - 2 * 3600 * 1000L, // 2 hours ago
                    isOffloaded = true
                ),
                StorageItem(
                    name = "Holiday_Photos.zip",
                    type = "DOWNLOAD",
                    sizeBytes = 840_000_000L, // 840 MB
                    addedTime = System.currentTimeMillis() - 24 * 3600 * 1000L, // Yesterday
                    isOffloaded = true
                ),
                StorageItem(
                    name = "Tax_Returns_2023.pdf",
                    type = "DOWNLOAD",
                    sizeBytes = 12_000_000L, // 12 MB
                    addedTime = System.currentTimeMillis() - 3 * 24 * 3600 * 1000L, // 3 days ago
                    isOffloaded = true
                )
            )
            storageDao.insertItems(defaultItems)

            // Setup default settings
            storageDao.saveSetting(AppSetting("auto_clean", true))
            storageDao.saveSetting(AppSetting("safe_delete", true))
            storageDao.saveSetting(AppSetting("notifications", false))
            storageDao.saveSetting(AppSetting("wifi_only", true))
        }
    }
}
