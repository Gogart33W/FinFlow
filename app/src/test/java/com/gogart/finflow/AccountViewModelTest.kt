package com.gogart.finflow

import com.gogart.finflow.data.local.dao.AccountDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountType
import com.gogart.finflow.data.local.entity.AccountWithBalance
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.presentation.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun accountBalancesCalculatedCorrectly() = runTest {
        val accountCash = AccountEntity(id = 1, name = "Готівка", type = AccountType.CASH, initialBalance = 100.0, isDefault = true)
        val accountCard = AccountEntity(id = 2, name = "Monobank", type = AccountType.CARD, initialBalance = 500.0)

        val accountWithBal1 = AccountWithBalance(account = accountCash, currentBalance = 150.0)
        val accountWithBal2 = AccountWithBalance(account = accountCard, currentBalance = 400.0)

        val fakeAccountDao = object : AccountDao {
            override fun getAllAccounts(): Flow<List<AccountEntity>> = MutableStateFlow(listOf(accountCash, accountCard))
            override fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>> = MutableStateFlow(listOf(accountWithBal1, accountWithBal2))
            override suspend fun getDefaultAccount(): AccountEntity? = accountCash
            override suspend fun getAccountById(id: Long): AccountEntity? = if (id == 1L) accountCash else accountCard
            override suspend fun insertAccount(account: AccountEntity): Long = 1
            override suspend fun insertAccounts(accounts: List<AccountEntity>) {}
            override suspend fun updateAccount(account: AccountEntity) {}
            override suspend fun deleteAccount(account: AccountEntity) {}
        }

        val fakeTransactionDao = object : TransactionDao {
            override suspend fun insertTransaction(transaction: TransactionEntity) {}
            override suspend fun deleteTransaction(transaction: TransactionEntity) {}
            override fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> = MutableStateFlow(emptyList())
            override suspend fun getTransactionCountByCategoryId(categoryId: Long): Int = 0
            override suspend fun getTransactionCountByAccountId(accountId: Long): Int = 0
        }

        val repository = AccountRepository(fakeAccountDao, fakeTransactionDao)
        val viewModel = AccountViewModel(repository)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.accountsWithBalance.value.size)
        assertEquals(150.0, viewModel.accountsWithBalance.value[0].currentBalance, 0.01)
        assertEquals(400.0, viewModel.accountsWithBalance.value[1].currentBalance, 0.01)
    }
}
