package com.gogart.finflow.data.repository

import com.gogart.finflow.data.local.dao.BudgetDao
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {
    fun getBudgetsWithSpent(yearMonth: String): Flow<List<BudgetWithSpent>> =
        dao.getBudgetsWithSpent(yearMonth)

    suspend fun saveBudget(budget: BudgetEntity): Long =
        dao.insertOrUpdateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        dao.deleteBudget(budget)
}
