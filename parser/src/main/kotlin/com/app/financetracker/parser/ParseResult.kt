package com.app.financetracker.parser

import com.app.financetracker.parser.model.Transaction

sealed interface ParseResult {
    data class Success(val transaction: Transaction) : ParseResult

    /**
     * The message came from an allowlisted sender but could not be parsed.
     * [reason] is a fixed description and must never contain message text.
     */
    data class Unrecognized(val reason: String) : ParseResult
}
