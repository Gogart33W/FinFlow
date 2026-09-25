package com.gogart.finflow

import android.content.Context
import app.cash.turbine.test
import com.gogart.finflow.data.local.dao.AccountDao
import com.gogart.finflow.data.local.dao.BudgetDao
import com.gogart.finflow.data.local.dao.CategoryDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountWithBalance
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.BudgetWithSpent
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.data.repository.BudgetRepository
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.presentation.util.NotificationHelper
import com.gogart.finflow.presentation.viewmodel.TransactionViewModel
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
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

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
    fun emptyListReturnsZeroTotals() = runTest {
        val fakeTransactionDao = createFakeTransactionDao(emptyList())
        val viewModel = createViewModel(fakeTransactionDao)

        viewModel.totalBalance.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
        }
        viewModel.totalIncome.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
        }
        viewModel.totalExpense.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
        }
    }

    @Test
    fun incomeOnlyTransactionsReturnCorrectBalanceAndExpenseZero() = runTest {
        val categoryIncome = CategoryEntity(id = 1, name = "Зарплата", iconName = "Work", colorHex = "#4CAF50", isIncome = true)
        val tx1 = TransactionWithCategory(
            transaction = TransactionEntity(id = 1, title = "Зарплата", amount = 1500.0, timestamp = 100, isIncome = true, categoryId = 1, accountId = 1),
            category = categoryIncome
        )

        val fakeTransactionDao = createFakeTransactionDao(listOf(tx1))
        val viewModel = createViewModel(fakeTransactionDao)

        viewModel.totalIncome.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
            assertEquals(1500.0, awaitItem(), 0.01)
        }
        viewModel.totalExpense.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
        }
        viewModel.totalBalance.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
            assertEquals(1500.0, awaitItem(), 0.01)
        }
    }

    @Test
    fun totalsCalculationWorksCorrectlyForIncomeAndExpenses() = runTest {
        val categoryIncome = CategoryEntity(id = 1, name = "Зарплата", iconName = "Work", colorHex = "#4CAF50", isIncome = true)
        val categoryExpense = CategoryEntity(id = 2, name = "Продукти", iconName = "ShoppingCart", colorHex = "#FF5722", isIncome = false)

        val tx1 = TransactionWithCategory(
            transaction = TransactionEntity(id = 1, title = "Зарплата", amount = 1000.0, timestamp = 100, isIncome = true, categoryId = 1, accountId = 1),
            category = categoryIncome
        )
        val tx2 = TransactionWithCategory(
            transaction = TransactionEntity(id = 2, title = "Сільпо", amount = 300.0, timestamp = 200, isIncome = false, categoryId = 2, accountId = 1),
            category = categoryExpense
        )

        val fakeTransactionDao = createFakeTransactionDao(listOf(tx1, tx2))
        val viewModel = createViewModel(fakeTransactionDao)

        viewModel.totalIncome.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
            assertEquals(1000.0, awaitItem(), 0.01)
        }
        viewModel.totalExpense.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
            assertEquals(300.0, awaitItem(), 0.01)
        }
        viewModel.totalBalance.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(0.0, awaitItem(), 0.01)
            assertEquals(700.0, awaitItem(), 0.01)
        }
    }

    private fun createFakeTransactionDao(list: List<TransactionWithCategory>): TransactionDao {
        return object : TransactionDao {
            override suspend fun insertTransaction(transaction: TransactionEntity) {}
            override suspend fun deleteTransaction(transaction: TransactionEntity) {}
            override fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> = MutableStateFlow(list)
            override suspend fun getTransactionCountByCategoryId(categoryId: Long): Int = 0
            override suspend fun getTransactionCountByAccountId(accountId: Long): Int = 0
            override fun getExpenseSummaryByCategory(startTime: Long, endTime: Long) = MutableStateFlow(emptyList<com.gogart.finflow.data.local.entity.CategoryExpenseSummary>())
            override fun getPeriodSummary(startTime: Long, endTime: Long) = MutableStateFlow(com.gogart.finflow.data.local.entity.PeriodSummary(0.0, 0.0))
        }
    }

    private fun createViewModel(transactionDao: TransactionDao): TransactionViewModel {
        val fakeCategoryDao = object : CategoryDao {
            override fun getAllCategories(): Flow<List<CategoryEntity>> = MutableStateFlow(emptyList())
            override fun getCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>> = MutableStateFlow(emptyList())
            override suspend fun getCategoryById(id: Long): CategoryEntity? = null
            override suspend fun insertCategory(category: CategoryEntity): Long = 0
            override suspend fun insertCategories(categories: List<CategoryEntity>) {}
            override suspend fun updateCategory(category: CategoryEntity) {}
            override suspend fun deleteCategory(category: CategoryEntity) {}
        }

        val fakeAccountDao = object : AccountDao {
            override fun getAllAccounts(): Flow<List<AccountEntity>> = MutableStateFlow(emptyList())
            override fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>> = MutableStateFlow(emptyList())
            override suspend fun getDefaultAccount(): AccountEntity? = null
            override suspend fun getAccountById(id: Long): AccountEntity? = null
            override suspend fun insertAccount(account: AccountEntity): Long = 0
            override suspend fun insertAccounts(accounts: List<AccountEntity>) {}
            override suspend fun updateAccount(account: AccountEntity) {}
            override suspend fun deleteAccount(account: AccountEntity) {}
        }
        
        val fakeBudgetDao = object : BudgetDao {
            override fun getBudgetsWithSpent(yearMonth: String): Flow<List<BudgetWithSpent>> = MutableStateFlow(emptyList())
            override suspend fun getAllBudgets(): List<BudgetEntity> = emptyList()
            override suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long = 0
            override suspend fun deleteBudget(budget: BudgetEntity) {}
        }

        val transactionRepository = TransactionRepository(transactionDao)
        val categoryRepository = CategoryRepository(fakeCategoryDao)
        val accountRepository = AccountRepository(fakeAccountDao, transactionDao)
        val budgetRepository = BudgetRepository(fakeBudgetDao)
        
        // Custom fake to avoid mockito dependency in a simple unit test
        val fakeNotificationHelper = object : NotificationHelper(org.mockito.Mockito.mock(Context::class.java)) {
            override fun showBudgetWarningNotification(categoryId: Long, categoryName: String, percentage: Int) {}
        }
        val mockSecurityManager = org.mockito.Mockito.mock(com.gogart.finflow.data.preferences.SecurityManager::class.java)
        org.mockito.Mockito.`when`(mockSecurityManager.currencySymbol).thenReturn(MutableStateFlow("₴"))

        return TransactionViewModel(transactionRepository, categoryRepository, accountRepository, budgetRepository, fakeNotificationHelper, mockSecurityManager)
    }
}
