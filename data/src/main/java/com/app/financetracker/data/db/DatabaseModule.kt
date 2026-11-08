package com.app.financetracker.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "finance_tracker.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideTransactionDao(database: FinanceDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideAppMetaDao(database: FinanceDatabase): AppMetaDao = database.appMetaDao()
}
