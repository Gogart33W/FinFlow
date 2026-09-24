package com.gogart.finflow.data.backup

import android.content.Context
import android.net.Uri
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
            
            // To properly do this, we need a query that returns all budgets directly. 
            // For now, let's assume we can get them (we will add a query in BudgetDao)
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

    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val backupData = gson.fromJson(reader, BackupData::class.java)

                    if (backupData != null && backupData.formatVersion == 1) {
                        // Using REPLACE strategy in DAOs acts as merge (or overwrite on ID match)
                        if (backupData.accounts.isNotEmpty()) {
                            database.accountDao.insertAccounts(backupData.accounts)
                        }
                        if (backupData.categories.isNotEmpty()) {
                            database.categoryDao.insertCategories(backupData.categories)
                        }
                        if (backupData.transactions.isNotEmpty()) {
                            backupData.transactions.forEach {
                                database.transactionDao.insertTransaction(it)
                            }
                        }
                        if (backupData.budgets.isNotEmpty()) {
                            backupData.budgets.forEach {
                                database.budgetDao.insertOrUpdateBudget(it)
                            }
                        }
                        return@withContext true
                    }
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
