package com.gogart.finflow.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.backup.BackupData
import com.gogart.finflow.data.backup.BackupManager
import com.gogart.finflow.data.preferences.SecurityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsViewModel(
    private val securityManager: SecurityManager,
    private val backupManager: BackupManager
) : ViewModel() {

    val isPinSet: StateFlow<Boolean> = securityManager.isPinSet
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isOnboardingCompleted: StateFlow<Boolean> = securityManager.isOnboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val themeMode: StateFlow<ThemeMode> = securityManager.themeMode
        .map { ThemeMode.valueOf(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val isDynamicColorEnabled: StateFlow<Boolean> = securityManager.isDynamicColorEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _backupDataPreview = MutableStateFlow<BackupData?>(null)
    val backupDataPreview: StateFlow<BackupData?> = _backupDataPreview

    init {
        viewModelScope.launch {
            securityManager.isPinSet.collect { hasPin ->
                if (!hasPin) {
                    _isAuthenticated.value = true
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            securityManager.setThemeMode(mode.name)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityManager.setDynamicColorEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            securityManager.setOnboardingCompleted()
        }
    }

    fun setPin(pin: String?) {
        viewModelScope.launch {
            securityManager.setPin(pin)
            if (pin == null) {
                _isAuthenticated.value = true
                _messageEvent.emit("pin_removed")
            } else {
                _messageEvent.emit("pin_saved")
            }
        }
    }

    fun authenticate(pin: String) {
        viewModelScope.launch {
            securityManager.validatePin(
                pin = pin,
                onSuccess = {
                    _isAuthenticated.value = true
                },
                onError = {
                    viewModelScope.launch {
                        _messageEvent.emit("invalid_pin")
                    }
                }
            )
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val success = backupManager.exportData(uri)
            val msg = if (success) "export_success" else "export_error"
            _messageEvent.emit(msg)
        }
    }

    fun parseImportData(uri: Uri) {
        viewModelScope.launch {
            val data = backupManager.parseBackupData(uri)
            if (data != null) {
                _backupDataPreview.value = data
            } else {
                _messageEvent.emit("import_read_error")
            }
        }
    }

    fun confirmImport() {
        val data = _backupDataPreview.value ?: return
        viewModelScope.launch {
            val success = backupManager.executeImport(data)
            _backupDataPreview.value = null
            val msg = if (success) "import_success" else "import_error"
            _messageEvent.emit(msg)
        }
    }

    fun cancelImport() {
        _backupDataPreview.value = null
    }
}

class SettingsViewModelFactory(
    private val securityManager: SecurityManager,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(securityManager, backupManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
