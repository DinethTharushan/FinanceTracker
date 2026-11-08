package com.app.financetracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /** Rows whose id already exists are skipped; their returned row ID is -1. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("SELECT * FROM transactions ORDER BY occurredAtMillis DESC, id")
    fun observeAll(): Flow<List<TransactionEntity>>

    /** Debit totals in [fromMillis, toMillis), one row per currency. */
    @Query(
        """
        SELECT currency, SUM(amountMinor) AS total FROM transactions
        WHERE type = 'DEBIT' AND occurredAtMillis >= :fromMillis AND occurredAtMillis < :toMillis
        GROUP BY currency ORDER BY currency
        """,
    )
    fun observeSpentByCurrency(fromMillis: Long, toMillis: Long): Flow<List<CurrencyTotal>>
}
