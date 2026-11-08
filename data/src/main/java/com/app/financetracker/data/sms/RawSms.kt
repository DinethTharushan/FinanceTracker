package com.app.financetracker.data.sms

import java.time.Instant

/**
 * A full allowlisted SMS, held in memory only while it is being imported.
 * Never store, log or send it. [toString] leaves the body out.
 */
class RawSms(
    val sender: String,
    val receivedAt: Instant,
    val body: String,
) {
    override fun toString(): String = "RawSms(sender=$sender, receivedAt=$receivedAt, body=<${body.length} chars>)"
}

sealed interface InboxReadResult {
    data class Success(val messages: List<RawSms>) : InboxReadResult
    data object PermissionMissing : InboxReadResult
}
