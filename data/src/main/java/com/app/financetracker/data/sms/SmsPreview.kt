package com.app.financetracker.data.sms

import java.time.Instant

/** A short, display-only view of an allowlisted SMS. Never holds the full message body. */
data class SmsPreview(
    val id: Long,
    val sender: String,
    val receivedAt: Instant,
    val preview: String,
)

sealed interface InboxReadResult {
    data class Success(val messages: List<SmsPreview>) : InboxReadResult
    data object PermissionMissing : InboxReadResult
}
