package com.gogart.finflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gogart.finflow.data.local.dao.AccountDao
import com.gogart.finflow.data.local.dao.BudgetDao
import com.gogart.finflow.data.local.dao.CategoryDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.AccountType
import com.gogart.finflow.data.local.entity.BudgetEntity
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class, BudgetEntity::class],
    version = 5,
    exportSchema = true
)
abstract class AppDataBase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val accountDao: AccountDao
    abstract val budgetDao: BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `isIncome` INTEGER NOT NULL,
                        `isDefault` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (1, 'Зарплата', 'Work', '#4CAF50', 1, 1)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (2, 'Фріланс', 'Laptop', '#2196F3', 1, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (3, 'Інвестиції', 'TrendingUp', '#009688', 1, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (4, 'Подарунок', 'CardGiftcard', '#E91E63', 1, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (5, 'Премія', 'MonetizationOn', '#FF9800', 1, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (6, 'Інший дохід', 'AttachMoney', '#8BC34A', 1, 0)")

                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (7, 'Їжа та продукти', 'ShoppingCart', '#FF5722', 0, 1)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (8, 'Транспорт', 'DirectionsCar', '#3F51B5', 0, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (9, 'Житло та комунальні', 'Home', '#795548', 0, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (10, 'Кафе та ресторани', 'Restaurant', '#FF9800', 0, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (11, 'Розваги', 'SportsEsports', '#9C27B0', 0, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (12, 'Здоров''я', 'MedicalServices', '#F44336', 0, 0)")
                db.execSQL("INSERT INTO `categories` (`id`, `name`, `iconName`, `colorHex`, `isIncome`, `isDefault`) VALUES (13, 'Інші витрати', 'MoreHoriz', '#607D8B', 0, 0)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isIncome` INTEGER NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `transactions_new` (`id`, `title`, `amount`, `timestamp`, `isIncome`, `categoryId`)
                    SELECT `id`, `title`, `amount`, `timestamp`, `isIncome`, 
                    CASE WHEN `isIncome` = 1 THEN 6 ELSE 13 END
                    FROM `transactions`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE IF EXISTS `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `accounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `initialBalance` REAL NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "INSERT INTO `accounts` (`id`, `name`, `type`, `initialBalance`, `colorHex`, `isDefault`) VALUES (1, 'Готівка', 'CASH', 0.0, '#4CAF50', 1)"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isIncome` INTEGER NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `accountId` INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `transactions_new` (`id`, `title`, `amount`, `timestamp`, `isIncome`, `categoryId`, `accountId`)
                    SELECT `id`, `title`, `amount`, `timestamp`, `isIncome`, `categoryId`, 1
                    FROM `transactions`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE IF EXISTS `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `monthlyLimit` REAL NOT NULL,
                        `yearMonth` TEXT NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryId_yearMonth` ON `budgets` (`categoryId`, `yearMonth`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `accounts` ADD COLUMN `currency` TEXT NOT NULL DEFAULT 'UAH'")
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Зарплата", iconName = "Work", colorHex = "#4CAF50", isIncome = true, isDefault = true),
            CategoryEntity(name = "Фріланс", iconName = "Laptop", colorHex = "#2196F3", isIncome = true),
            CategoryEntity(name = "Інвестиції", iconName = "TrendingUp", colorHex = "#009688", isIncome = true),
            CategoryEntity(name = "Подарунок", iconName = "CardGiftcard", colorHex = "#E91E63", isIncome = true),
            CategoryEntity(name = "Премія", iconName = "MonetizationOn", colorHex = "#FF9800", isIncome = true),
            CategoryEntity(name = "Інший дохід", iconName = "AttachMoney", colorHex = "#8BC34A", isIncome = true),

            CategoryEntity(name = "Їжа та продукти", iconName = "ShoppingCart", colorHex = "#FF5722", isIncome = false, isDefault = true),
            CategoryEntity(name = "Транспорт", iconName = "DirectionsCar", colorHex = "#3F51B5", isIncome = false),
            CategoryEntity(name = "Житло та комунальні", iconName = "Home", colorHex = "#795548", isIncome = false),
            CategoryEntity(name = "Кафе та ресторани", iconName = "Restaurant", colorHex = "#FF9800", isIncome = false),
            CategoryEntity(name = "Розваги", iconName = "SportsEsports", colorHex = "#9C27B0", isIncome = false),
            CategoryEntity(name = "Здоров'я", iconName = "MedicalServices", colorHex = "#F44336", isIncome = false),
            CategoryEntity(name = "Інші витрати", iconName = "MoreHoriz", colorHex = "#607D8B", isIncome = false)
        )

        val DEFAULT_ACCOUNT = AccountEntity(
            id = 1,
            name = "Готівка",
            type = AccountType.CASH,
            initialBalance = 0.0,
            colorHex = "#4CAF50",
            isDefault = true,
            currency = "UAH"
        )

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "finflow_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.categoryDao?.insertCategories(DEFAULT_CATEGORIES)
                                INSTANCE?.accountDao?.insertAccount(DEFAULT_ACCOUNT)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
