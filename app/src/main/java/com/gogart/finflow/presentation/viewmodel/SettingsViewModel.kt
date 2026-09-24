package com.gogart.finflow.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.backup.BackupManager
import com.gogart.finflow.data.preferences.SecurityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
            initialValue = true // assume true until loaded to avoid flash
        )

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        viewModelScope.launch {
            securityManager.isPinSet.collect { hasPin ->
                if (!hasPin) {
                    _isAuthenticated.value = true
                }
            }
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
                _isAuthenticated.value = true // Unlocked if PIN is removed
                _messageEvent.emit("PIN-код успішно видалено")
            } else {
                _messageEvent.emit("PIN-код успішно встановлено")
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
                        _messageEvent.emit("Невірний PIN-код")
                    }
                }
            )
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val success = backupManager.exportData(uri)
            val msg = if (success) "Дані успішно експортовано" else "Помилка при експорті"
            _messageEvent.emit(msg)
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            val success = backupManager.importData(uri)
            val msg = if (success) "Дані успішно імпортовано" else "Помилка при імпорті"
            _messageEvent.emit(msg)
        }
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
