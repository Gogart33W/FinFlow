package com.gogart.finflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract val transactionDao: TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "finflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
