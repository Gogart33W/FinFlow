package com.gogart.finflow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.data.repository.BudgetRepository
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.data.preferences.SecurityManager
import com.gogart.finflow.presentation.util.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationHelper: NotificationHelper,
    private val securityManager: SecurityManager
) : ViewModel() {

    val currencySymbol: StateFlow<String> = securityManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "₴"
        )

    val transactions: StateFlow<List<TransactionWithCategory>> =
        transactionRepository.getAllTransactionsWithCategory()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val categories: StateFlow<List<CategoryEntity>> =
        categoryRepository.getAllCategories()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val accounts: StateFlow<List<AccountEntity>> =
        accountRepository.getAllAccounts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val totalIncome: StateFlow<Double> = transactions.map { list ->
        list.filter { it.transaction.isIncome }.sumOf { it.transaction.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalExpense: StateFlow<Double> = transactions.map { list ->
        list.filter { !it.transaction.isIncome }.sumOf { it.transaction.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalBalance: StateFlow<Double> = transactions.map { list ->
        val income = list.filter { it.transaction.isIncome }.sumOf { it.transaction.amount }
        val expense = list.filter { !it.transaction.isIncome }.sumOf { it.transaction.amount }
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
        categoryId: Long,
        accountId: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                isIncome = isIncome,
                categoryId = categoryId,
                accountId = accountId
            )
            transactionRepository.insert(entity)

            if (!isIncome) {
                checkBudgetLimit(categoryId, amount)
            }
        }
    }

    private suspend fun checkBudgetLimit(categoryId: Long, newAmount: Double) {
        val currentYearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = currentYearMonthFormat.format(Date())

        val budgets = budgetRepository.getBudgetsWithSpent(currentMonth).firstOrNull()
        val budgetForCategory = budgets?.find { it.category.id == categoryId }

        if (budgetForCategory != null) {
            val budgetLimit = budgetForCategory.budget.monthlyLimit
            val newTotalSpent = budgetForCategory.spentAmount + newAmount

            if (budgetLimit > 0 && newTotalSpent >= 0.9 * budgetLimit) {
                // Trigger notification (basic 90% threshold for now)
                val percentage = ((newTotalSpent / budgetLimit) * 100).toInt()
                notificationHelper.showBudgetWarningNotification(
                    categoryId = categoryId,
                    categoryName = budgetForCategory.category.name,
                    percentage = percentage
                )
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.update(transaction)
        }
    }
}

class TransactionViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationHelper: NotificationHelper,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(
                transactionRepository,
                categoryRepository,
                accountRepository,
                budgetRepository,
                notificationHelper,
                securityManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
