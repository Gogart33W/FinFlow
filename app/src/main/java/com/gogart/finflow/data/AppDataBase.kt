package com.gogart.finflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gogart.finflow.data.local.dao.CategoryDao
import com.gogart.finflow.data.local.dao.TransactionDao
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create categories table
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

                // 2. Insert default categories
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

                // 3. Create new transactions table with categoryId FK
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

                // 4. Copy old transactions into new table
                db.execSQL(
                    """
                    INSERT INTO `transactions_new` (`id`, `title`, `amount`, `timestamp`, `isIncome`, `categoryId`)
                    SELECT `id`, `title`, `amount`, `timestamp`, `isIncome`, 
                    CASE WHEN `isIncome` = 1 THEN 6 ELSE 13 END
                    FROM `transactions`
                    """.trimIndent()
                )

                // 5. Drop old table and rename new table
                db.execSQL("DROP TABLE IF EXISTS `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                // 6. Create index
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
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

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "finflow_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.categoryDao?.insertCategories(DEFAULT_CATEGORIES)
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
