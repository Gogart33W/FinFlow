package com.gogart.finflow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountType
import com.gogart.finflow.data.local.entity.AccountWithBalance
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.data.preferences.SecurityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val currencySymbol: StateFlow<String> = securityManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "₴"
        )

    val accountsWithBalance: StateFlow<List<AccountWithBalance>> =
        repository.getAllAccountsWithBalance()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent

    fun addAccount(
        name: String,
        type: AccountType,
        initialBalance: Double,
        colorHex: String,
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name,
                type = type,
                initialBalance = initialBalance,
                colorHex = colorHex,
                isDefault = isDefault
            )
            repository.insertAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity, onCannotDelete: () -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteAccount(account)
            if (!deleted) {
                onCannotDelete()
            }
        }
    }
}

class AccountViewModelFactory(
    private val repository: AccountRepository,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(repository, securityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
