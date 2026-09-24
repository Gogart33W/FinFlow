package com.gogart.finflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gogart.finflow.data.local.entity.CategoryExpenseSummary
import com.gogart.finflow.data.local.entity.PeriodSummary
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionCountByCategoryId(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionCountByAccountId(accountId: Long): Int

    @Query(
        """
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconName AS iconName,
            c.colorHex AS colorHex,
            SUM(t.amount) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.isIncome = 0 AND t.timestamp BETWEEN :startTime AND :endTime
        GROUP BY c.id, c.name, c.iconName, c.colorHex
        ORDER BY totalAmount DESC
        """
    )
    fun getExpenseSummaryByCategory(startTime: Long, endTime: Long): Flow<List<CategoryExpenseSummary>>

    @Query(
        """
        SELECT 
            COALESCE(SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END), 0.0) AS totalIncome,
            COALESCE(SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END), 0.0) AS totalExpense
        FROM transactions
        WHERE timestamp BETWEEN :startTime AND :endTime
        """
    )
    fun getPeriodSummary(startTime: Long, endTime: Long): Flow<PeriodSummary>
}
