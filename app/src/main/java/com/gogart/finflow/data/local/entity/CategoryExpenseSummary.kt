package com.gogart.finflow.data.local.entity

data class CategoryExpenseSummary(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: String,
    val totalAmount: Double
)

data class PeriodSummary(
    val totalIncome: Double,
    val totalExpense: Double
)
