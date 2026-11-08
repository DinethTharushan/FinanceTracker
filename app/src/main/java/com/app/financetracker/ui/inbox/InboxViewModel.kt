package com.app.financetracker.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.financetracker.data.sms.InboxReadResult
import com.app.financetracker.data.sms.SmsInboxReader
import com.app.financetracker.data.sms.SmsPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InboxUiState {
    data object Loading : InboxUiState
    data class Loaded(val messages: List<SmsPreview>) : InboxUiState
    data object PermissionMissing : InboxUiState
}

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val reader: SmsInboxReader,
) : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** Reads the allowlisted inbox again. Cancels any read already in progress. */
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value !is InboxUiState.Loaded) _uiState.value = InboxUiState.Loading
            _uiState.value = when (val result = reader.readAllowlisted()) {
                is InboxReadResult.Success -> InboxUiState.Loaded(result.messages)
                InboxReadResult.PermissionMissing -> InboxUiState.PermissionMissing
            }
        }
    }
}
