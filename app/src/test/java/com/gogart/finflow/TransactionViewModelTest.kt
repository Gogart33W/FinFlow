package com.gogart.finflow

import com.gogart.finflow.data.local.dao.CategoryDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.repository.TransactionRepository
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
    fun totalsCalculationWorksCorrectly() = runTest {
        val categoryIncome = CategoryEntity(id = 1, name = "Зарплата", iconName = "Work", colorHex = "#4CAF50", isIncome = true)
        val categoryExpense = CategoryEntity(id = 2, name = "Продукти", iconName = "ShoppingCart", colorHex = "#FF5722", isIncome = false)

        val tx1 = TransactionWithCategory(
            transaction = TransactionEntity(id = 1, title = "Зарплата", amount = 1000.0, timestamp = 100, isIncome = true, categoryId = 1),
            category = categoryIncome
        )
        val tx2 = TransactionWithCategory(
            transaction = TransactionEntity(id = 2, title = "Сільпо", amount = 300.0, timestamp = 200, isIncome = false, categoryId = 2),
            category = categoryExpense
        )

        val fakeTransactionDao = object : TransactionDao {
            override suspend fun insertTransaction(transaction: TransactionEntity) {}
            override suspend fun deleteTransaction(transaction: TransactionEntity) {}
            override fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> = MutableStateFlow(listOf(tx1, tx2))
            override suspend fun getTransactionCountByCategoryId(categoryId: Long): Int = 0
        }

        val fakeCategoryDao = object : CategoryDao {
            override fun getAllCategories(): Flow<List<CategoryEntity>> = MutableStateFlow(listOf(categoryIncome, categoryExpense))
            override fun getCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>> = MutableStateFlow(emptyList())
            override suspend fun getCategoryById(id: Long): CategoryEntity? = null
            override suspend fun insertCategory(category: CategoryEntity): Long = 0
            override suspend fun insertCategories(categories: List<CategoryEntity>) {}
            override suspend fun updateCategory(category: CategoryEntity) {}
            override suspend fun deleteCategory(category: CategoryEntity) {}
        }

        val transactionRepository = TransactionRepository(fakeTransactionDao)
        val categoryRepository = CategoryRepository(fakeCategoryDao)

        val viewModel = TransactionViewModel(transactionRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, viewModel.totalIncome.value, 0.01)
        assertEquals(300.0, viewModel.totalExpense.value, 0.01)
        assertEquals(700.0, viewModel.totalBalance.value, 0.01)
    }
}
