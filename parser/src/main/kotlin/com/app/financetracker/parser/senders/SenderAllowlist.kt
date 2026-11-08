package com.app.financetracker.parser.senders

/**
 * The only SMS senders the app is allowed to read. Messages from any other sender
 * must never be read, logged or stored.
 *
 * Matching is exact and case-sensitive: add each sender ID exactly as it appears on
 * the phone, including any casing variants a bank uses.
 */
object SenderAllowlist {

    val senderIds: Set<String> = setOf(
        "BOC", // Bank of Ceylon
    )

    fun isAllowed(address: String?): Boolean = address != null && address in senderIds
}
