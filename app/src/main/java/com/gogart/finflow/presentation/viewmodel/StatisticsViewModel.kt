package com.gogart.finflow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gogart.finflow.data.local.entity.CategoryExpenseSummary
import com.gogart.finflow.data.local.entity.PeriodSummary
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.data.preferences.SecurityManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class TimePeriod(val title: String) {
    WEEK("Тиждень"),
    MONTH("Місяць"),
    YEAR("Рік"),
    ALL("Весь час")
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val repository: TransactionRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val currencySymbol: StateFlow<String> = securityManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "₴"
        )

    val selectedPeriod = MutableStateFlow(TimePeriod.MONTH)

    private val periodTimestamps = selectedPeriod.flatMapLatest { period ->
        val (start, end) = calculateRange(period)
        MutableStateFlow(Pair(start, end))
    }

    val periodSummary: StateFlow<PeriodSummary> = periodTimestamps.flatMapLatest { (start, end) ->
        repository.getPeriodSummary(start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PeriodSummary(0.0, 0.0)
    )

    val expenseCategories: StateFlow<List<CategoryExpenseSummary>> = periodTimestamps.flatMapLatest { (start, end) ->
        repository.getExpenseSummaryByCategory(start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectPeriod(period: TimePeriod) {
        selectedPeriod.value = period
    }

    private fun calculateRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        val startTime = when (period) {
            TimePeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            TimePeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            TimePeriod.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            TimePeriod.ALL -> 0L
        }
        return Pair(startTime, endTime)
    }
}

class StatisticsViewModelFactory(
    private val repository: TransactionRepository,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(repository, securityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
