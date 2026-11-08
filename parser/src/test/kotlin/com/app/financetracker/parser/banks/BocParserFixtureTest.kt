package com.app.financetracker.parser.banks

import com.app.financetracker.parser.ParseResult
import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.time.Instant

/**
 * Runs every `samples/BOC/<name>.txt` through [BocParser] and compares the result
 * with the matching `<name>.json`.
 */
@RunWith(Parameterized::class)
class BocParserFixtureTest(private val name: String) {

    companion object {
        private val dir: File by lazy {
            val url = checkNotNull(BocParserFixtureTest::class.java.classLoader.getResource("samples/BOC")) {
                "samples/BOC not found on the test classpath"
            }
            File(url.toURI())
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun fixtures(): List<String> {
            fun names(ext: String) =
                dir.listFiles { f -> f.extension == ext }.orEmpty().map { it.nameWithoutExtension }.toSet()

            val txt = names("txt")
            val json = names("json")
            check(txt.isNotEmpty()) { "No .txt fixtures in samples/BOC" }
            check(txt == json) {
                "Every .txt needs a matching .json and vice versa. " +
                    "Missing .json: ${txt - json}, missing .txt: ${json - txt}"
            }
            return txt.sorted()
        }
    }

    @Test
    fun `fixture parses to expected result`() {
        val body = File(dir, "$name.txt").readText(Charsets.UTF_8)
        val spec = JSONObject(File(dir, "$name.json").readText(Charsets.UTF_8))

        val parser = BocParser()
        assertTrue("sender not handled by BocParser", spec.getString("sender") in parser.senderIds)

        val actual = parser.parse(body, Instant.parse(spec.getString("receivedAt")))
        val expected = spec.getJSONObject("expected")

        when (expected.getString("result")) {
            "SUCCESS" -> {
                if (actual !is ParseResult.Success) fail("expected SUCCESS but got $actual")
                assertEquals(
                    expected.getJSONObject("transaction").toTransaction(),
                    (actual as ParseResult.Success).transaction,
                )
            }
            "UNRECOGNIZED" -> {
                if (actual !is ParseResult.Unrecognized) fail("expected UNRECOGNIZED but got $actual")
                if (expected.has("reason")) {
                    assertEquals(expected.getString("reason"), (actual as ParseResult.Unrecognized).reason)
                }
            }
            else -> fail("unknown expected.result in $name.json")
        }
    }

    private fun JSONObject.toTransaction() = Transaction(
        amountMinor = getLong("amountMinor"),
        currency = getString("currency"),
        type = TransactionType.valueOf(getString("type")),
        merchant = nullableString("merchant"),
        accountSuffix = nullableString("accountSuffix"),
        dateTime = Instant.parse(getString("dateTime")),
        balanceAfterMinor = if (isNull("balanceAfterMinor")) null else getLong("balanceAfterMinor"),
        description = nullableString("description"),
    )

    private fun JSONObject.nullableString(key: String): String? = if (isNull(key)) null else getString(key)
}
