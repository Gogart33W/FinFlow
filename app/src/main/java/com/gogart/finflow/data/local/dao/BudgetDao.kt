package com.gogart.finflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Transaction
    @Query(
        """
        SELECT 
            b.*,
            COALESCE((
                SELECT SUM(t.amount) 
                FROM transactions t 
                WHERE t.categoryId = b.categoryId 
                  AND t.isIncome = 0 
                  AND strftime('%Y-%m', t.timestamp / 1000, 'unixepoch') = b.yearMonth
            ), 0.0) AS spentAmount
        FROM budgets b
        WHERE b.yearMonth = :yearMonth
        ORDER BY b.monthlyLimit DESC
        """
    )
    fun getBudgetsWithSpent(yearMonth: String): Flow<List<BudgetWithSpent>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}
