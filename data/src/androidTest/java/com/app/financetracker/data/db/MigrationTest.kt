package com.app.financetracker.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FinanceDatabase::class.java,
    )

    @Test
    fun migrate1To2KeepsMetaAndAddsTransactions() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL("INSERT INTO app_meta (`key`, value) VALUES ('k', 'v')")
        }

        // Validates the migrated schema against schemas/.../2.json.
        helper.runMigrationsAndValidate(DB_NAME, 2, true).use { db ->
            db.query("SELECT value FROM app_meta WHERE `key` = 'k'").use { c ->
                c.moveToFirst()
                assertEquals("v", c.getString(0))
            }
            db.query("SELECT COUNT(*) FROM transactions").use { c ->
                c.moveToFirst()
                assertEquals(0, c.getInt(0))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
