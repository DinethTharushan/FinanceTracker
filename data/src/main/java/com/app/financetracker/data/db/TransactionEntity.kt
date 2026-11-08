package com.app.financetracker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A parsed transaction. Holds only parsed fields: the raw SMS body is never stored.
 *
 * [id] is the SMS dedup key (SHA-256 of sender + timestamp + body), so importing the
 * same message twice keeps one row.
 */
@Entity(
    tableName = "transactions",
    indices = [Index("occurredAtMillis")],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val amountMinor: Long,
    val currency: String,
    /** [com.app.financetracker.parser.model.TransactionType] name. */
    val type: String,
    val merchant: String?,
    /** Trailing account digits only (at most 4), never a full number. */
    val accountSuffix: String?,
    val occurredAtMillis: Long,
    val balanceAfterMinor: Long?,
    val description: String?,
)

/** One row of a per-currency sum. */
data class CurrencyTotal(
    val currency: String,
    @ColumnInfo(name = "total") val totalMinor: Long,
)
