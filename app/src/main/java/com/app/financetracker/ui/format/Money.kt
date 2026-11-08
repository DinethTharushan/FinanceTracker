package com.app.financetracker.ui.format

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * "LKR 4,307.00" from 430700 minor units. Amounts are stored with 2 decimal places
 * (see the parsers), so that is what is shown. No floating point is involved.
 */
fun formatMinor(amountMinor: Long, currency: String, locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true
    }
    return "$currency ${format.format(BigDecimal.valueOf(amountMinor, 2))}"
}
