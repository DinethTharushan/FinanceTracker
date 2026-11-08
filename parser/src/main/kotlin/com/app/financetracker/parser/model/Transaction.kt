package com.app.financetracker.parser.model

import java.time.Instant

/**
 * A transaction parsed from a bank SMS.
 *
 * Money is held in minor units (cents) as [Long]; never use floating point for amounts.
 */
data class Transaction(
    /** Amount in minor units, always > 0. Direction is given by [type]. */
    val amountMinor: Long,
    /** ISO 4217 code, e.g. "LKR". */
    val currency: String,
    val type: TransactionType,
    /** Null when the bank does not name a merchant. */
    val merchant: String?,
    /** The trailing account digits the bank exposes (e.g. "533"); never a full number. */
    val accountSuffix: String?,
    /** When the bank text has no date/time, this is the SMS received time. */
    val dateTime: Instant,
    /** Available balance after the transaction, in minor units, if the bank reports it. */
    val balanceAfterMinor: Long?,
    /** The bank's own label for the transaction, e.g. "CEFT Transfer Debit". */
    val description: String?,
) {
    // Masked so an accidental log line only ever shows the exposed suffix.
    override fun toString(): String =
        "Transaction(amountMinor=$amountMinor, currency=$currency, type=$type, " +
            "merchant=$merchant, accountSuffix=${accountSuffix?.let { "***$it" }}, " +
            "dateTime=$dateTime, balanceAfterMinor=$balanceAfterMinor, description=$description)"
}
