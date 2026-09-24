package com.gogart.finflow.data.repository

import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransaction()
}
