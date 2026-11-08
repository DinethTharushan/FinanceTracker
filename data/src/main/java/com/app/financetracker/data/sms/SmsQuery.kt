package com.app.financetracker.data.sms

import android.provider.Telephony

/** SQL selection matching `address` against [senderCount] bound arguments. */
internal fun senderSelection(senderCount: Int): String {
    require(senderCount > 0) { "senderCount must be positive" }
    return "${Telephony.Sms.ADDRESS} IN (${List(senderCount) { "?" }.joinToString(",")})"
}
