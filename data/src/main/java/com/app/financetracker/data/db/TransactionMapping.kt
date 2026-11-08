package com.app.financetracker.data.db

import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import java.time.Instant

internal fun Transaction.toEntity(id: String, sender: String): TransactionEntity =
    TransactionEntity(
        id = id,
        sender = sender,
        amountMinor = amountMinor,
        currency = currency,
        type = type.name,
        merchant = merchant,
        accountSuffix = accountSuffix,
        occurredAtMillis = dateTime.toEpochMilli(),
        balanceAfterMinor = balanceAfterMinor,
        description = description,
    )

internal fun TransactionEntity.toTransaction(): Transaction =
    Transaction(
        amountMinor = amountMinor,
        currency = currency,
        type = TransactionType.valueOf(type),
        merchant = merchant,
        accountSuffix = accountSuffix,
        dateTime = Instant.ofEpochMilli(occurredAtMillis),
        balanceAfterMinor = balanceAfterMinor,
        description = description,
    )
