package com.app.financetracker.parser

import com.app.financetracker.parser.banks.BocParser
import com.app.financetracker.parser.senders.SenderAllowlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankParsersTest {

    @Test
    fun `every parser sender is allowlisted`() {
        BankParsers.all.flatMap { it.senderIds }.forEach { sender ->
            assertTrue("$sender is not in SenderAllowlist", SenderAllowlist.isAllowed(sender))
        }
    }

    @Test
    fun `no sender is claimed by two parsers`() {
        val senders = BankParsers.all.flatMap { it.senderIds }
        assertEquals(senders.size, senders.toSet().size)
    }

    @Test
    fun `BOC sender maps to the BOC parser`() {
        assertTrue(BankParsers.forSender("BOC") is BocParser)
    }

    @Test
    fun `unknown or differently cased sender has no parser`() {
        assertNull(BankParsers.forSender("boc"))
        assertNull(BankParsers.forSender("Dialog"))
    }
}
