package com.app.financetracker.data.sms

import android.provider.Telephony

internal const val PREVIEW_LENGTH = 40

/** SQL selection matching `address` against [senderCount] bound arguments. */
internal fun senderSelection(senderCount: Int): String {
    require(senderCount > 0) { "senderCount must be positive" }
    return "${Telephony.Sms.ADDRESS} IN (${List(senderCount) { "?" }.joinToString(",")})"
}

/**
 * First [maxChars] characters of [body] on a single line. Counts Unicode code points,
 * so a cut never splits a character such as an emoji in half.
 */
internal fun previewOf(body: String?, maxChars: Int = PREVIEW_LENGTH): String {
    val singleLine = body.orEmpty().replace(WHITESPACE, " ").trim()
    val codePoints = singleLine.codePointCount(0, singleLine.length)
    if (codePoints <= maxChars) return singleLine
    return singleLine.substring(0, singleLine.offsetByCodePoints(0, maxChars))
}

private val WHITESPACE = Regex("\\s+")
