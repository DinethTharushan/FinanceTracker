package com.app.financetracker.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.financetracker.R
import com.app.financetracker.data.repo.ImportResult
import com.app.financetracker.data.repo.StoredTransaction
import com.app.financetracker.parser.model.Transaction
import com.app.financetracker.parser.model.TransactionType
import com.app.financetracker.ui.format.formatMinor
import com.app.financetracker.ui.permission.SmsPermissionGate
import com.app.financetracker.ui.theme.FinanceTrackerTheme
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
    SmsPermissionGate(onGranted = viewModel::onPermissionGranted) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        val result = state.importResult
        val message = result?.let { importMessage(it) }
        LaunchedEffect(result) {
            if (message != null) {
                snackbarHostState.showSnackbar(message)
                viewModel.onImportResultShown()
            }
        }

        TransactionsContent(state, onRescan = viewModel::rescan, snackbarHostState = snackbarHostState)
    }
}

@Composable
private fun importMessage(result: ImportResult): String = when (result) {
    is ImportResult.Imported -> stringResource(R.string.import_result, result.added, result.unrecognized)
    ImportResult.PermissionMissing -> stringResource(R.string.import_permission_missing)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsContent(
    state: TransactionsUiState,
    onRescan: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transactions_title)) },
                actions = {
                    if (state.isImporting) {
                        CircularProgressIndicator(Modifier.padding(end = 16.dp).size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = onRescan) { Text(stringResource(R.string.rescan)) }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            MonthTotal(state.month, state.spentThisMonth)
            when {
                state.isLoading || (state.isImporting && state.transactions.isEmpty()) ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                state.transactions.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                    Text(
                        stringResource(R.string.transactions_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                else -> TransactionList(state.transactions)
            }
        }
    }
}

@Composable
private fun MonthTotal(month: YearMonth, spent: Map<String, Long>) {
    val monthLabel = remember(month) { month.format(DateTimeFormatter.ofPattern("LLLL yyyy")) }
    ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.spent_in_month, monthLabel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // One line per currency: amounts in different currencies are never added together.
            val totals = spent.ifEmpty { mapOf(DEFAULT_CURRENCY to 0L) }
            totals.forEach { (currency, total) ->
                Text(formatMinor(total, currency), style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun TransactionList(transactions: List<StoredTransaction>) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(transactions, key = { it.id }) { stored ->
            TransactionRow(stored.transaction, formatter)
            HorizontalDivider()
        }
    }
}

@Composable
private fun TransactionRow(t: Transaction, formatter: DateTimeFormatter) {
    val isCredit = t.type == TransactionType.CREDIT
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                // BOC alerts name no merchant, so fall back to the bank's own label.
                t.merchant ?: t.description ?: stringResource(R.string.transaction_unknown),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatter.format(t.dateTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            (if (isCredit) "+" else "−") + formatMinor(t.amountMinor, t.currency),
            style = MaterialTheme.typography.titleMedium,
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private const val DEFAULT_CURRENCY = "LKR"

@Preview(showBackground = true)
@Composable
private fun TransactionsContentPreview() {
    fun sample(id: String, minor: Long, type: TransactionType, label: String, at: String) = StoredTransaction(
        id,
        Transaction(minor, "LKR", type, null, "533", Instant.parse(at), null, label),
    )
    FinanceTrackerTheme {
        TransactionsContent(
            state = TransactionsUiState(
                month = YearMonth.of(2026, 11),
                spentThisMonth = mapOf("LKR" to 1_230_700L),
                transactions = listOf(
                    sample("1", 430_700, TransactionType.DEBIT, "CEFT Transfer Debit", "2026-11-07T09:15:00Z"),
                    sample("2", 5_000_000, TransactionType.CREDIT, "Transfer Credit", "2026-11-05T04:00:00Z"),
                    sample("3", 800_000, TransactionType.DEBIT, "POS/ATM Transaction", "2026-11-02T12:30:00Z"),
                ),
                isLoading = false,
            ),
            onRescan = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsEmptyPreview() {
    FinanceTrackerTheme {
        TransactionsContent(
            state = TransactionsUiState(month = YearMonth.of(2026, 11), isLoading = false),
            onRescan = {},
        )
    }
}
