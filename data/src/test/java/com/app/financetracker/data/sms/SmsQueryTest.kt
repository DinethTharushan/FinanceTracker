package com.app.financetracker.data.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SmsQueryTest {

    @Test
    fun `selection has one placeholder per sender`() {
        assertEquals("address IN (?)", senderSelection(1))
        assertEquals("address IN (?,?,?)", senderSelection(3))
    }

    @Test
    fun `selection rejects zero senders`() {
        assertThrows(IllegalArgumentException::class.java) { senderSelection(0) }
    }

    @Test
    fun `short body is returned unchanged`() {
        assertEquals("Short message", previewOf("Short message"))
    }

    @Test
    fun `long body is cut to 40 characters`() {
        val body = "A".repeat(39) + "BCDEFGHIJ"
        val preview = previewOf(body)
        assertEquals(40, preview.length)
        assertEquals("A".repeat(39) + "B", preview)
    }

    @Test
    fun `body of exactly 40 characters is kept whole`() {
        val body = "x".repeat(40)
        assertEquals(body, previewOf(body))
    }

    @Test
    fun `line breaks and repeated spaces collapse to single spaces`() {
        assertEquals("Line one Line two", previewOf("  Line one\n\n  Line two \r\n"))
    }

    @Test
    fun `cut never splits an emoji`() {
        // Each emoji is one code point but two UTF-16 chars.
        val body = "a".repeat(39) + "💰💰"
        val preview = previewOf(body)
        assertEquals("a".repeat(39) + "💰", preview)
    }

    @Test
    fun `null body becomes empty preview`() {
        assertEquals("", previewOf(null))
    }
}
