package com.gogart.finflow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalIncome: StateFlow<Double> = transactions.map { list ->
        list.filter { it.isIncome }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalExpense: StateFlow<Double> = transactions.map { list ->
        list.filter { !it.isIncome }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalBalance: StateFlow<Double> = transactions.map { list ->
        val income = list.filter { it.isIncome }.sumOf { it.amount }
        val expense = list.filter { !it.isIncome }.sumOf { it.amount }
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addTransaction(
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                isIncome = isIncome,
                category = category
            )
            repository.insert(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
