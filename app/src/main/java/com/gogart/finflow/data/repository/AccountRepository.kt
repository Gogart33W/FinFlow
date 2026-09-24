package com.gogart.finflow.data.repository

import com.gogart.finflow.data.local.dao.AccountDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountWithBalance
import kotlinx.coroutines.flow.Flow

class AccountRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>> =
        accountDao.getAllAccountsWithBalance()

    suspend fun getDefaultAccount(): AccountEntity? = accountDao.getDefaultAccount()

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity): Boolean {
        val transactionCount = transactionDao.getTransactionCountByAccountId(account.id)
        return if (transactionCount == 0) {
            accountDao.deleteAccount(account)
            true
        } else {
            false
        }
    }
}
