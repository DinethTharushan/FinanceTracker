package com.app.financetracker.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyTest {

    @Test
    fun `formats with grouping and two decimals`() {
        assertEquals("LKR 4,307.00", formatMinor(430_700, "LKR", Locale.US))
        assertEquals("LKR 1,010,325.90", formatMinor(101_032_590, "LKR", Locale.US))
    }

    @Test
    fun `small amounts keep leading zero`() {
        assertEquals("LKR 0.05", formatMinor(5, "LKR", Locale.US))
        assertEquals("LKR 0.00", formatMinor(0, "LKR", Locale.US))
    }

    @Test
    fun `large amounts are exact`() {
        assertEquals("LKR 92,233,720,368,547,758.07", formatMinor(Long.MAX_VALUE, "LKR", Locale.US))
    }
}
