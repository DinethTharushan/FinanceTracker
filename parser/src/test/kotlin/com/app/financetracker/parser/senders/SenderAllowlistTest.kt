package com.app.financetracker.parser.senders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SenderAllowlistTest {

    @Test
    fun `allowlisted sender is allowed`() {
        assertTrue(SenderAllowlist.isAllowed("BOC"))
    }

    @Test
    fun `unknown senders are rejected`() {
        assertFalse(SenderAllowlist.isAllowed("+94771234567"))
        assertFalse(SenderAllowlist.isAllowed("Dialog"))
    }

    @Test
    fun `near misses are rejected`() {
        assertFalse(SenderAllowlist.isAllowed("BOC1"))
        assertFalse(SenderAllowlist.isAllowed("XBOC"))
        assertFalse(SenderAllowlist.isAllowed(" BOC"))
        assertFalse(SenderAllowlist.isAllowed("BOC "))
    }

    @Test
    fun `matching is case-sensitive`() {
        assertFalse(SenderAllowlist.isAllowed("boc"))
        assertFalse(SenderAllowlist.isAllowed("Boc"))
    }

    @Test
    fun `null and blank senders are rejected`() {
        assertFalse(SenderAllowlist.isAllowed(null))
        assertFalse(SenderAllowlist.isAllowed(""))
        assertFalse(SenderAllowlist.isAllowed("   "))
    }
}
