package com.gogart.finflow.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.gogart.finflow.data.local.AppDataBase
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(private val context: Context, private val database: AppDataBase) {

    private val gson = Gson()

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val accounts = database.accountDao.getAllAccounts().first()
            val categories = database.categoryDao.getAllCategories().first()
            val transactions = database.transactionDao.getAllTransactionsWithCategory().first().map { it.transaction }
            val budgets = database.budgetDao.getAllBudgets()

            val backupData = BackupData(
                formatVersion = 1,
                accounts = accounts,
                categories = categories,
                transactions = transactions,
                budgets = budgets
            )

            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun parseBackupData(uri: Uri): BackupData? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val backupData = gson.fromJson(reader, BackupData::class.java)
                    if (backupData != null && backupData.formatVersion == 1) {
                        return@withContext backupData
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun executeImport(backupData: BackupData): Boolean = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val categoryIdMap = mutableMapOf<Long, Long>()
                val accountIdMap = mutableMapOf<Long, Long>()

                // 1. Import Categories and map old ID to new ID
                backupData.categories.forEach { oldCategory ->
                    val newCategory = oldCategory.copy(id = 0) // Reset ID to auto-generate
                    val newId = database.categoryDao.insertCategory(newCategory)
                    categoryIdMap[oldCategory.id] = newId
                }

                // 2. Import Accounts and map old ID to new ID
                backupData.accounts.forEach { oldAccount ->
                    val newAccount = oldAccount.copy(id = 0) // Reset ID to auto-generate
                    val newId = database.accountDao.insertAccount(newAccount)
                    accountIdMap[oldAccount.id] = newId
                }

                // 3. Import Transactions, mapping Foreign Keys to the new IDs
                backupData.transactions.forEach { oldTx ->
                    val newCategoryId = categoryIdMap[oldTx.categoryId]
                    val newAccountId = accountIdMap[oldTx.accountId]

                    if (newCategoryId != null && newAccountId != null) {
                        val newTx = oldTx.copy(
                            id = 0, // Reset ID
                            categoryId = newCategoryId,
                            accountId = newAccountId
                        )
                        database.transactionDao.insertTransaction(newTx)
                    }
                }

                // 4. Import Budgets, mapping Foreign Keys
                backupData.budgets.forEach { oldBudget ->
                    val newCategoryId = categoryIdMap[oldBudget.categoryId]
                    if (newCategoryId != null) {
                        val newBudget = oldBudget.copy(
                            id = 0, // Reset ID
                            categoryId = newCategoryId
                        )
                        database.budgetDao.insertOrUpdateBudget(newBudget)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
