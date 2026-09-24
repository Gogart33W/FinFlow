package com.gogart.finflow.data.local.entity

import androidx.room.Embedded

data class AccountWithBalance(
    @Embedded
    val account: AccountEntity,
    val currentBalance: Double
)
