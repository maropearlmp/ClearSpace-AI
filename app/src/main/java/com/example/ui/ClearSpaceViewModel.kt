package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppSetting
import com.example.data.StorageItem
import com.example.data.StorageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClearSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StorageRepository
    
    // UI Navigation & Flows
    private val _onboardingStep = MutableStateFlow(0) // 0: Welcome, 1: Scanning, 2: Scan Results, 3: Vault Setup, 4: Finished, -1: Main App
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _activeTab = MutableStateFlow("home") // "home", "files", "vault", "settings"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Interactive Scan state
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _currentScanningCategory = MutableStateFlow("Scanning Duplicate Photos...")
    val currentScanningCategory: StateFlow<String> = _currentScanningCategory.asStateFlow()

    private val _isMainScanning = MutableStateFlow(false)
    val isMainScanning: StateFlow<Boolean> = _isMainScanning.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StorageRepository(database.storageDao())
        
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Observe Database Items
    val allActiveItems: StateFlow<List<StorageItem>> = repository.allActiveItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsFlow: StateFlow<List<AppSetting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultedItems: StateFlow<List<StorageItem>> = repository.offloadedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Convenience Getters for calculations
    fun getSettingValue(key: String, defaultValue: Boolean): Boolean {
        return settingsFlow.value.find { it.key == key }?.value ?: defaultValue
    }

    // Onboarding control
    fun startOnboardingScan() {
        _onboardingStep.value = 1
        viewModelScope.launch {
            _scanProgress.value = 0f
            _currentScanningCategory.value = "Scanning Duplicate Photos"
            delay(1500)
            _scanProgress.value = 0.35f
            _currentScanningCategory.value = "Analyzing Large Videos"
            delay(1500)
            _scanProgress.value = 0.65f
            _currentScanningCategory.value = "Searching Junk Files"
            delay(1500)
            _scanProgress.value = 0.85f
            _currentScanningCategory.value = "Optimizing System Cache"
            delay(1000)
            _scanProgress.value = 1.0f
            delay(500)
            // Move to Step 2: Scan Results
            _onboardingStep.value = 2
        }
    }

    fun cancelOnboardingScan() {
        _onboardingStep.value = 0
        _scanProgress.value = 0f
    }

    fun proceedToVaultSetup() {
        _onboardingStep.value = 3
    }

    fun proceedToFinished() {
        _onboardingStep.value = 4
    }

    fun completeOnboarding() {
        _onboardingStep.value = -1 // Enter main app
        _activeTab.value = "home"
    }

    fun triggerMainScan() {
        _isMainScanning.value = true
        _scanProgress.value = 0f
        viewModelScope.launch {
            _currentScanningCategory.value = "Scanning Duplicate Photos..."
            delay(1000)
            _scanProgress.value = 0.3f
            _currentScanningCategory.value = "Analyzing Large Videos..."
            delay(1000)
            _scanProgress.value = 0.6f
            _currentScanningCategory.value = "Searching Junk Files..."
            delay(1000)
            _scanProgress.value = 0.9f
            delay(500)
            _scanProgress.value = 1.0f
            _isMainScanning.value = false
            // Switch to the 'files' tab so the user sees results!
            _activeTab.value = "files"
        }
    }

    fun cancelMainScan() {
        _isMainScanning.value = false
        _scanProgress.value = 0f
    }

    // Toggle Settings
    fun toggleSetting(key: String, currentValue: Boolean) {
        viewModelScope.launch {
            repository.saveSetting(key, !currentValue)
        }
    }

    // Tab Navigation
    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    // Deletion and Cleaning Operations (Flow driven)
    fun cleanDuplicatePhotos() {
        viewModelScope.launch {
            repository.deleteItemsByType("PHOTO")
        }
    }

    fun cleanLargeVideos() {
        viewModelScope.launch {
            repository.deleteItemsByType("VIDEO")
        }
    }

    fun cleanJunkFiles() {
        viewModelScope.launch {
            repository.deleteItemsByType("JUNK")
        }
    }

    fun cleanDownloads() {
        viewModelScope.launch {
            repository.deleteItemsByType("DOWNLOAD")
        }
    }

    fun cleanAll() {
        viewModelScope.launch {
            repository.deleteItemsByType("PHOTO")
            repository.deleteItemsByType("VIDEO")
            repository.deleteItemsByType("JUNK")
            repository.deleteItemsByType("DOWNLOAD")
        }
    }

    // Vault Interactions
    fun uploadFileToVault(name: String, sizeBytes: Long) {
        viewModelScope.launch {
            val item = StorageItem(
                name = name,
                type = if (name.endsWith(".mp4") || name.endsWith(".mov")) "VIDEO" else "DOWNLOAD",
                sizeBytes = sizeBytes,
                isOffloaded = true
            )
            repository.insertItems(listOf(item))
        }
    }

    fun restoreItemFromVault(item: StorageItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isOffloaded = false))
        }
    }

    fun restoreAllVaultItems() {
        viewModelScope.launch {
            val items = vaultedItems.value
            val restored = items.map { it.copy(isOffloaded = false) }
            repository.updateItems(restored)
        }
    }

    fun removeVaultItemPermanently(id: Int) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    // Direct helper exposures for UI file lists
    fun deleteItemById(id: Int) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    fun updateItem(item: StorageItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItemsByType(type: String) {
        viewModelScope.launch {
            repository.deleteItemsByType(type)
        }
    }

    fun resetMainApp() {
        // Resets the database to initial state and goes back to Onboarding Welcome
        viewModelScope.launch {
            repository.clearDatabase()
            _onboardingStep.value = 0
            _activeTab.value = "home"
            repository.prepopulateIfEmpty()
        }
    }
}
