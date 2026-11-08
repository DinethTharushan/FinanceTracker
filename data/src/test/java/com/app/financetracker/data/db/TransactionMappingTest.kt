package com.app.financetracker.data.db

import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TransactionMappingTest {

    private val transaction = Transaction(
        amountMinor = 430_700,
        currency = "LKR",
        type = TransactionType.DEBIT,
        merchant = null,
        accountSuffix = "533",
        dateTime = Instant.parse("2026-11-07T09:15:30.123Z"),
        balanceAfterMinor = 10_103_259,
        description = "CEFT Transfer Debit",
    )

    @Test
    fun `entity holds parsed fields`() {
        val entity = transaction.toEntity(id = "abc", sender = "BOC")
        assertEquals("abc", entity.id)
        assertEquals("BOC", entity.sender)
        assertEquals("DEBIT", entity.type)
        assertEquals(Instant.parse("2026-11-07T09:15:30.123Z").toEpochMilli(), entity.occurredAtMillis)
    }

    @Test
    fun `round trip keeps every field`() {
        assertEquals(transaction, transaction.toEntity("abc", "BOC").toTransaction())
        val credit = transaction.copy(type = TransactionType.CREDIT, merchant = "Shop", accountSuffix = null, balanceAfterMinor = null)
        assertEquals(credit, credit.toEntity("def", "BOC").toTransaction())
    }
}
