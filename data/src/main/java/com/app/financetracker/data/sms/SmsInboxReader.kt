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
 * reach the app. Bodies are cut to a short preview right away, and nothing here is
 * logged or stored.
 */
class SmsInboxReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun readAllowlisted(): InboxReadResult = withContext(Dispatchers.IO) {
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
                val idCol = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)

                buildList {
                    while (size < MAX_MESSAGES && cursor.moveToNext()) {
                        val address = cursor.getString(addressCol)
                        // Second safeguard in case the provider ever ignores the selection.
                        if (!SenderAllowlist.isAllowed(address)) continue
                        add(
                            SmsPreview(
                                id = cursor.getLong(idCol),
                                sender = address,
                                receivedAt = Instant.ofEpochMilli(cursor.getLong(dateCol)),
                                preview = previewOf(cursor.getString(bodyCol)),
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
        const val MAX_MESSAGES = 500

        val PROJECTION = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.DATE,
            Telephony.Sms.BODY,
        )
    }
}
