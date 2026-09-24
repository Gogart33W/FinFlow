package com.gogart.finflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BudgetWithSpent(
    @Embedded
    val budget: BudgetEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,
    val spentAmount: Double
)
