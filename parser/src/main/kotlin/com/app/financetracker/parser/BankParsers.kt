package com.app.financetracker.parser

import com.app.financetracker.parser.banks.BocParser

/** Every bank parser the app knows, looked up by SMS sender ID. */
object BankParsers {

    val all: List<BankSmsParser> = listOf(
        BocParser(),
    )

    private val bySender: Map<String, BankSmsParser> =
        all.flatMap { parser -> parser.senderIds.map { it to parser } }.toMap()

    /** The parser for [senderId] (exact, case-sensitive match), or null if none. */
    fun forSender(senderId: String): BankSmsParser? = bySender[senderId]
}
