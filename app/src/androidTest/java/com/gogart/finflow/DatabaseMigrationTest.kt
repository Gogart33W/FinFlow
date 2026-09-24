package com.gogart.finflow

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gogart.finflow.data.local.AppDataBase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDataBase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO transactions (id, title, amount, timestamp, isIncome, category) VALUES (1, 'Зарплата', 1000.0, 100, 1, 'Зарплата')")
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDataBase.MIGRATION_1_2)

        val cursor = db.query("SELECT * FROM transactions WHERE id = 1")
        assert(cursor.moveToFirst())
        assert(cursor.getLong(cursor.getColumnIndexOrThrow("categoryId")) == 6L)
        cursor.close()
    }

    @Test
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO categories (id, name, iconName, colorHex, isIncome, isDefault) VALUES (1, 'Зарплата', 'Work', '#4CAF50', 1, 1)")
            execSQL("INSERT INTO transactions (id, title, amount, timestamp, isIncome, categoryId) VALUES (1, 'Зарплата', 1000.0, 100, 1, 1)")
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDataBase.MIGRATION_2_3)

        val cursorAccount = db.query("SELECT * FROM accounts WHERE id = 1")
        assert(cursorAccount.moveToFirst())
        assert(cursorAccount.getString(cursorAccount.getColumnIndexOrThrow("name")) == "Готівка")
        cursorAccount.close()

        val cursorTx = db.query("SELECT * FROM transactions WHERE id = 1")
        assert(cursorTx.moveToFirst())
        assert(cursorTx.getLong(cursorTx.getColumnIndexOrThrow("accountId")) == 1L)
        cursorTx.close()
    }
}
