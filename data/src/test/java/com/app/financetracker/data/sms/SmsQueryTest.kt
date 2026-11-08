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
}
