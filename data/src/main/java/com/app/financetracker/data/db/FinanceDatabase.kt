package com.app.financetracker.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetaEntity::class, TransactionEntity::class],
    version = 2,
    autoMigrations = [
        // 1 -> 2: adds the transactions table.
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun appMetaDao(): AppMetaDao
}
