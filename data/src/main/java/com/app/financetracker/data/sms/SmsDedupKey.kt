package com.app.financetracker.data.sms

import java.security.MessageDigest

/**
 * Stable, one-way key for an SMS: lowercase hex SHA-256 of
 * `sender NUL receivedAtMillis NUL body` (UTF-8).
 *
 * The NUL separators stop different field splits from producing the same input
 * (e.g. sender "AB" + body "C" vs sender "A" + body "BC"). The body itself is
 * never stored; only this hash is.
 */
internal fun smsDedupKey(sender: String, receivedAtMillis: Long, body: String): String {
    val input = "$sender\u0000$receivedAtMillis\u0000$body"
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}
