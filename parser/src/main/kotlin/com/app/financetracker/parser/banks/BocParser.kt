package com.app.financetracker.parser.banks

import com.app.financetracker.parser.BankSmsParser
import com.app.financetracker.parser.ParseResult
import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

/**
 * Bank of Ceylon account alerts, e.g.
 * `CEFT Transfer Debit Rs 4307.00 From A/C No XXXXXXXXXX533. Balance available Rs 101032.59 - Thank you for banking with BOC`
 *
 * The body carries no date/time or merchant: the SMS received time is used for
 * [Transaction.dateTime] and [Transaction.merchant] is always null.
 */
class BocParser : BankSmsParser {

    override val senderIds: Set<String> = setOf("BOC")

    override fun parse(body: String, receivedAt: Instant): ParseResult =
        try {
            parseOrReject(body, receivedAt)
        } catch (e: Exception) {
            // Last resort: the contract is never to throw. Don't include the message.
            ParseResult.Unrecognized("unexpected parse failure")
        }

    private fun parseOrReject(body: String, receivedAt: Instant): ParseResult {
        val text = body.trim().replace(WHITESPACE, " ")
        if (text.isEmpty()) return ParseResult.Unrecognized("empty message")

        val m = ALERT.matchEntire(text)
            ?: return ParseResult.Unrecognized("not a BOC account alert")
        val (channel, amountText, direction, suffix, balanceText) = m.destructured

        val amount = toMinor(amountText)
        if (amount <= 0L) return ParseResult.Unrecognized("non-positive amount")

        val directionType = if (direction == "From") TransactionType.DEBIT else TransactionType.CREDIT
        val keywordType = when {
            DEBIT_WORD.containsMatchIn(channel) -> TransactionType.DEBIT
            CREDIT_WORD.containsMatchIn(channel) -> TransactionType.CREDIT
            else -> null
        }
        if (keywordType != null && keywordType != directionType) {
            return ParseResult.Unrecognized("debit/credit direction conflict")
        }

        return ParseResult.Success(
            Transaction(
                amountMinor = amount,
                currency = "LKR",
                type = directionType,
                merchant = null,
                accountSuffix = suffix,
                dateTime = receivedAt,
                balanceAfterMinor = toMinor(balanceText),
                description = channel,
            ),
        )
    }

    /** "50,025.00" -> 5002500. The regex has already validated the shape. */
    private fun toMinor(text: String): Long =
        BigDecimal(text.replace(",", "")).movePointRight(2).longValueExact()

    private companion object {
        val WHITESPACE = Regex("\\s+")

        // Either plain digits or properly grouped thousands, always with 2 decimals.
        const val MONEY = """(?:\d{1,3}(?:,\d{3})+|\d+)\.\d{2}"""

        val ALERT = Regex(
            """(.+?) Rs ($MONEY) (From|To) A/C No X*(\d{3,4})\. """ +
                """Balance available Rs ($MONEY) - Thank you for banking with BOC\.?""",
        )

        val DEBIT_WORD = Regex("""\bDebit\b""", RegexOption.IGNORE_CASE)
        val CREDIT_WORD = Regex("""\bCredit\b""", RegexOption.IGNORE_CASE)
    }
}
