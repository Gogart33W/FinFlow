package com.gogart.finflow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.BudgetWithSpent
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.repository.BudgetRepository
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.preferences.SecurityManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val currencySymbol: StateFlow<String> = securityManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "₴"
        )

    private val currentYearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val selectedYearMonth = MutableStateFlow(currentYearMonthFormat.format(Date()))

    val budgetsWithSpent: StateFlow<List<BudgetWithSpent>> = selectedYearMonth.flatMapLatest { yearMonth ->
        budgetRepository.getBudgetsWithSpent(yearMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenseCategories: StateFlow<List<CategoryEntity>> = categoryRepository.getCategoriesByType(false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setBudget(categoryId: Long, monthlyLimit: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = categoryId,
                monthlyLimit = monthlyLimit,
                yearMonth = selectedYearMonth.value
            )
            budgetRepository.saveBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}

class BudgetViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(budgetRepository, categoryRepository, securityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
