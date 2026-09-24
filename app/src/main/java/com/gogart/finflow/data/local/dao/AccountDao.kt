package com.gogart.finflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query(
        """
        SELECT 
            a.*,
            (a.initialBalance + 
                COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.isIncome = 1), 0.0) - 
                COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.isIncome = 0), 0.0)
            ) AS currentBalance
        FROM accounts a
        ORDER BY a.isDefault DESC, a.name ASC
        """
    )
    fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>>

    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}
