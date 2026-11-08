package com.app.financetracker.data.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsDedupKeyTest {

    private val body = "CEFT Transfer Debit Rs 4307.00 From A/C No XXXXXXXXXX533."

    @Test
    fun `same input gives same key`() {
        assertEquals(smsDedupKey("BOC", 1_000L, body), smsDedupKey("BOC", 1_000L, body))
    }

    @Test
    fun `key is 64 lowercase hex chars`() {
        assertTrue(smsDedupKey("BOC", 1_000L, body).matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun `key matches reference SHA-256`() {
        // printf 'BOC\0001\000x' | sha256sum
        assertEquals(
            "8ec3ccb63543a1ed60a6eb1af1aa7304ecd0fe4ef15609f5f3563cb72558422d",
            smsDedupKey("BOC", 1L, "x"),
        )
    }

    @Test
    fun `changing any field changes the key`() {
        val base = smsDedupKey("BOC", 1_000L, body)
        assertNotEquals(base, smsDedupKey("BOC2", 1_000L, body))
        assertNotEquals(base, smsDedupKey("BOC", 1_001L, body))
        assertNotEquals(base, smsDedupKey("BOC", 1_000L, "$body "))
    }

    @Test
    fun `field boundaries cannot be shifted`() {
        assertNotEquals(smsDedupKey("AB", 1L, "C"), smsDedupKey("A", 1L, "BC"))
        assertNotEquals(smsDedupKey("A", 12L, "3"), smsDedupKey("A", 1L, "23"))
    }
}
