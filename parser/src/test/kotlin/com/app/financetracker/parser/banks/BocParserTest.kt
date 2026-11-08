package com.app.financetracker.parser.banks

import com.app.financetracker.parser.ParseResult
import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BocParserTest {

    private val parser = BocParser()
    private val at = Instant.parse("2026-11-01T09:00:00Z")

    private fun boc(channel: String, amount: String, dir: String, footer: String = "BOC") =
        "$channel Rs $amount $dir A/C No XXXXXXXXXX533. " +
            "Balance available Rs 155364.59 - Thank you for banking with $footer"

    private fun success(body: String): Transaction {
        val result = parser.parse(body, at)
        assertTrue("expected Success, got $result", result is ParseResult.Success)
        return (result as ParseResult.Success).transaction
    }

    private fun assertUnrecognized(body: String) {
        val result = parser.parse(body, at)
        assertTrue("expected Unrecognized, got $result", result is ParseResult.Unrecognized)
        val reason = (result as ParseResult.Unrecognized).reason
        assertTrue(reason.isNotBlank())
        // Reasons are fixed strings; they must never echo account digits from the message.
        assertFalse("reason leaks account digits", reason.contains("533"))
    }

    @Test
    fun `thousands separators are parsed`() {
        val t = success(
            "CEFT Transfer Debit Rs 50,025.00 From A/C No XXXXXXXXXX533. " +
                "Balance available Rs 1,105,339.59 - Thank you for banking with BOC",
        )
        assertEquals(5_002_500L, t.amountMinor)
        assertEquals(110_533_959L, t.balanceAfterMinor)
    }

    @Test
    fun `extra whitespace and line breaks are tolerated`() {
        val t = success(
            "  Transfer Credit Rs 1200.00 To A/C No XXXXXXXXXX533.\n" +
                "Balance available Rs 154364.59 -  Thank you for banking with BOC\n",
        )
        assertEquals(TransactionType.CREDIT, t.type)
        assertEquals("Transfer Credit", t.description)
        assertEquals(15_436_459L, t.balanceAfterMinor)
    }

    @Test
    fun `received time becomes the transaction time`() {
        assertEquals(at, success(boc("Online Transfer Debit", "200.00", "From")).dateTime)
    }

    @Test
    fun `toString masks account suffix`() {
        val text = success(boc("Online Transfer Debit", "200.00", "From")).toString()
        assertTrue(text.contains("accountSuffix=***533"))
    }

    @Test
    fun `otp message is unrecognized`() =
        assertUnrecognized("Your BOC Smart OTP is 482913. Do not share this code with anyone.")

    @Test
    fun `promo message is unrecognized`() =
        assertUnrecognized("Enjoy 20% off at selected merchants with your BOC Credit Card. T&C apply.")

    @Test
    fun `empty body is unrecognized`() = assertUnrecognized("")

    @Test
    fun `malformed amount is unrecognized`() =
        assertUnrecognized(boc("Online Transfer Debit", "2,,00.00", "From"))

    @Test
    fun `zero amount is unrecognized`() =
        assertUnrecognized(boc("Online Transfer Debit", "0.00", "From"))

    @Test
    fun `debit keyword with To direction is unrecognized`() =
        assertUnrecognized(boc("Online Transfer Debit", "200.00", "To"))

    @Test
    fun `credit keyword with From direction is unrecognized`() =
        assertUnrecognized(boc("Transfer Credit", "200.00", "From"))

    @Test
    fun `different footer is unrecognized`() =
        assertUnrecognized(boc("Online Transfer Debit", "200.00", "From", footer = "XYZ"))
}
