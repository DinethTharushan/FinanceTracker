package com.app.financetracker.data.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: FinanceDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FinanceDatabase::class.java).build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() = database.close()

    private fun row(
        id: String,
        at: Long,
        amount: Long = 100,
        type: String = "DEBIT",
        currency: String = "LKR",
    ) = TransactionEntity(
        id = id, sender = "BOC", amountMinor = amount, currency = currency, type = type,
        merchant = null, accountSuffix = "533", occurredAtMillis = at, balanceAfterMinor = null,
        description = "CEFT Transfer Debit",
    )

    @Test
    fun duplicateIdIsIgnored() = runBlocking {
        val first = dao.insertAll(listOf(row("a", 1), row("b", 2)))
        val again = dao.insertAll(listOf(row("a", 1, amount = 999), row("c", 3)))

        assertEquals(2, first.count { it != -1L })
        assertEquals(listOf(-1L), again.filter { it == -1L })
        val all = dao.observeAll().first()
        assertEquals(3, all.size)
        assertEquals(100L, all.single { it.id == "a" }.amountMinor)
    }

    @Test
    fun newestFirst() = runBlocking {
        dao.insertAll(listOf(row("old", 10), row("new", 30), row("mid", 20)))
        assertEquals(listOf("new", "mid", "old"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun spentSumsOnlyDebitsInRangeByCurrency() = runBlocking {
        dao.insertAll(
            listOf(
                row("before", at = 99, amount = 1),
                row("start", at = 100, amount = 10),
                row("inside", at = 150, amount = 20),
                row("credit", at = 150, amount = 1_000, type = "CREDIT"),
                row("usd", at = 150, amount = 5, currency = "USD"),
                row("end", at = 200, amount = 100),
            ),
        )
        val totals = dao.observeSpentByCurrency(fromMillis = 100, toMillis = 200).first()
        assertEquals(listOf(CurrencyTotal("LKR", 30), CurrencyTotal("USD", 5)), totals)
    }

    @Test
    fun spentIsEmptyWithNoDebits() = runBlocking {
        assertEquals(emptyList<CurrencyTotal>(), dao.observeSpentByCurrency(0, Long.MAX_VALUE).first())
    }
}
