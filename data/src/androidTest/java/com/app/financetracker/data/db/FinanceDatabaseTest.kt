package com.app.financetracker.data.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceDatabaseTest {

    @Test
    fun databaseOpens() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, FinanceDatabase::class.java).build()

        try {
            assertTrue(database.openHelper.writableDatabase.isOpen)
        } finally {
            database.close()
        }
    }
}
