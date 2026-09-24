package com.gogart.finflow.data.backup

import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity

data class BackupData(
    val formatVersion: Int = 1,
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>
)
