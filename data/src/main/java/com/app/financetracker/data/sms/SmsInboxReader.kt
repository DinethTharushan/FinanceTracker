package com.app.financetracker.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import com.app.financetracker.parser.senders.SenderAllowlist
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Reads the SMS inbox, limited to allowlisted senders.
 *
 * Privacy: the sender filter is applied in the provider query, so other messages never
 * reach the app. Full bodies are returned for parsing only; nothing here is logged or
 * stored.
 */
class SmsInboxReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Every allowlisted inbox message, newest first. */
    suspend fun readAllowlistedForImport(): InboxReadResult = withContext(Dispatchers.IO) {
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext InboxReadResult.PermissionMissing
        }
        val senders = SenderAllowlist.senderIds.toTypedArray()
        if (senders.isEmpty()) return@withContext InboxReadResult.Success(emptyList())

        try {
            val messages = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                PROJECTION,
                senderSelection(senders.size),
                senders,
                "${Telephony.Sms.DATE} DESC",
            )?.use { cursor ->
                val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)

                buildList {
                    while (cursor.moveToNext()) {
                        val address = cursor.getString(addressCol)
                        // Second safeguard in case the provider ever ignores the selection.
                        if (address == null || !SenderAllowlist.isAllowed(address)) continue
                        add(
                            RawSms(
                                sender = address,
                                receivedAt = Instant.ofEpochMilli(cursor.getLong(dateCol)),
                                body = cursor.getString(bodyCol).orEmpty(),
                            ),
                        )
                    }
                }
            }.orEmpty()
            InboxReadResult.Success(messages)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the query. Not logged on purpose.
            InboxReadResult.PermissionMissing
        }
    }

    private companion object {
        val PROJECTION = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.DATE,
            Telephony.Sms.BODY,
        )
    }
}
