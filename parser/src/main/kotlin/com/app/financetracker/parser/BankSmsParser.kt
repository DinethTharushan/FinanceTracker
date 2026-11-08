package com.app.financetracker.parser

import java.time.Instant

/** Parses SMS alerts for one bank. Implementations must never throw. */
interface BankSmsParser {
    /** Sender IDs this parser handles; each must also be in SenderAllowlist. */
    val senderIds: Set<String>

    /** [receivedAt] is the SMS timestamp, used when the body has no date/time. */
    fun parse(body: String, receivedAt: Instant): ParseResult
}
