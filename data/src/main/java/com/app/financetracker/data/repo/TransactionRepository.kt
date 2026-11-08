package com.app.financetracker.data.repo

import com.app.financetracker.data.db.AppMetaDao
import com.app.financetracker.data.db.AppMetaEntity
import com.app.financetracker.data.db.TransactionDao
import com.app.financetracker.data.db.TransactionEntity
import com.app.financetracker.data.db.toEntity
import com.app.financetracker.data.db.toTransaction
import com.app.financetracker.data.sms.InboxReadResult
import com.app.financetracker.data.sms.SmsInboxReader
import com.app.financetracker.data.sms.smsDedupKey
import com.app.financetracker.parser.BankParsers
import com.app.financetracker.parser.ParseResult
import com.app.financetracker.parser.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** A saved transaction plus its stable row key. */
data class StoredTransaction(val id: String, val transaction: Transaction)

sealed interface ImportResult {
    /** [unrecognized] messages came from allowlisted senders but did not parse; they are not stored. */
    data class Imported(val added: Int, val duplicates: Int, val unrecognized: Int) : ImportResult
    data object PermissionMissing : ImportResult
}

@Singleton
class TransactionRepository @Inject constructor(
    private val reader: SmsInboxReader,
    private val transactionDao: TransactionDao,
    private val appMetaDao: AppMetaDao,
) {

    private val importLock = Mutex()

    /**
     * Reads the allowlisted inbox, parses every message and saves the successes.
     * Safe to repeat: rows are keyed by [smsDedupKey], so a message is saved once.
     * Raw bodies are only held in memory here and are never stored or logged.
     */
    suspend fun importFromInbox(): ImportResult = importLock.withLock {
        val messages = when (val read = reader.readAllowlistedForImport()) {
            InboxReadResult.PermissionMissing -> return ImportResult.PermissionMissing
            is InboxReadResult.Success -> read.messages
        }

        val (entities, unrecognized) = withContext(Dispatchers.Default) {
            var unrecognized = 0
            val entities = mutableListOf<TransactionEntity>()
            for (sms in messages) {
                val parser = BankParsers.forSender(sms.sender)
                if (parser == null) {
                    unrecognized++
                    continue
                }
                when (val result = parser.parse(sms.body, sms.receivedAt)) {
                    is ParseResult.Success -> entities += result.transaction.toEntity(
                        id = smsDedupKey(sms.sender, sms.receivedAt.toEpochMilli(), sms.body),
                        sender = sms.sender,
                    )
                    is ParseResult.Unrecognized -> unrecognized++
                }
            }
            entities to unrecognized
        }

        val added = if (entities.isEmpty()) 0 else transactionDao.insertAll(entities).count { it != -1L }
        appMetaDao.put(AppMetaEntity(KEY_INITIAL_IMPORT_DONE, "true"))
        ImportResult.Imported(added = added, duplicates = entities.size - added, unrecognized = unrecognized)
    }

    suspend fun isInitialImportDone(): Boolean = appMetaDao.get(KEY_INITIAL_IMPORT_DONE) == "true"

    /** All saved transactions, newest first. */
    fun observeTransactions(): Flow<List<StoredTransaction>> =
        transactionDao.observeAll().map { rows -> rows.map { StoredTransaction(it.id, it.toTransaction()) } }

    /** Debit totals in [from, to), in minor units, keyed by currency code. */
    fun observeSpent(from: Instant, to: Instant): Flow<Map<String, Long>> =
        transactionDao.observeSpentByCurrency(from.toEpochMilli(), to.toEpochMilli())
            .map { rows -> rows.associate { it.currency to it.totalMinor } }

    private companion object {
        const val KEY_INITIAL_IMPORT_DONE = "initial_import_done"
    }
}
