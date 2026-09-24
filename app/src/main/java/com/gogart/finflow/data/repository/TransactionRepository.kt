package com.gogart.finflow.data.repository

import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> =
        dao.getAllTransactionsWithCategory()

    suspend fun getTransactionCountByCategoryId(categoryId: Long): Int =
        dao.getTransactionCountByCategoryId(categoryId)
}
