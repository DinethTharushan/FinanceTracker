package com.app.financetracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.financetracker.data.repo.ImportResult
import com.app.financetracker.data.repo.StoredTransaction
import com.app.financetracker.data.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class TransactionsUiState(
    val month: YearMonth,
    /** Debit totals for [month] in minor units, by currency. Empty when nothing was spent. */
    val spentThisMonth: Map<String, Long> = emptyMap(),
    val transactions: List<StoredTransaction> = emptyList(),
    /** True until the first database read arrives. */
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    /** Outcome of the last import, shown once and then cleared via [TransactionsViewModel.onImportResultShown]. */
    val importResult: ImportResult? = null,
)

private data class ImportState(val running: Boolean = false, val result: ImportResult? = null)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val clock: Clock,
) : ViewModel() {

    private val zone: ZoneId = clock.zone
    private val month = MutableStateFlow(YearMonth.now(clock))
    private val importState = MutableStateFlow(ImportState())

    private val spent = month.flatMapLatest { m ->
        repository.observeSpent(
            from = m.atDay(1).atStartOfDay(zone).toInstant(),
            to = m.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant(),
        )
    }

    val uiState: StateFlow<TransactionsUiState> =
        combine(month, spent, repository.observeTransactions(), importState) { m, spentMap, list, import ->
            TransactionsUiState(
                month = m,
                spentThisMonth = spentMap,
                transactions = list,
                isLoading = false,
                isImporting = import.running,
                importResult = import.result,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TransactionsUiState(month = month.value),
        )

    /**
     * Called when SMS permission is granted and on every resume while it is. Runs the
     * first inbox import if it has not run yet, and moves to a new month if the date
     * has rolled over while the app was open.
     */
    fun onPermissionGranted() {
        month.value = YearMonth.now(clock)
        viewModelScope.launch {
            if (!repository.isInitialImportDone()) runImport(showResult = false)
        }
    }

    fun rescan() {
        viewModelScope.launch { runImport(showResult = true) }
    }

    fun onImportResultShown() {
        importState.update { it.copy(result = null) }
    }

    private suspend fun runImport(showResult: Boolean) {
        if (importState.value.running) return
        importState.update { it.copy(running = true) }
        val result = repository.importFromInbox()
        importState.value = ImportState(running = false, result = result.takeIf { showResult })
    }
}
