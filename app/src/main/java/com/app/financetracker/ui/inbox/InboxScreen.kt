package com.app.financetracker.ui.inbox

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.financetracker.R
import com.app.financetracker.data.sms.SmsPreview
import com.app.financetracker.ui.theme.FinanceTrackerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InboxScreen(viewModel: InboxViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var status by rememberSaveable {
        mutableStateOf(
            if (context.hasSmsPermission()) SmsPermissionStatus.Granted else SmsPermissionStatus.NotRequested,
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = activity?.statusAfterRequest(granted)
            ?: if (granted) SmsPermissionStatus.Granted else SmsPermissionStatus.Denied
        if (granted) viewModel.load()
    }

    // Re-check on every return to the app: the user may have changed it in Settings.
    LifecycleResumeEffect(Unit) {
        if (context.hasSmsPermission()) {
            status = SmsPermissionStatus.Granted
            viewModel.load()
        } else if (status == SmsPermissionStatus.Granted) {
            status = SmsPermissionStatus.NotRequested
        }
        onPauseOrDispose { }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (status) {
                SmsPermissionStatus.Granted -> InboxContent(uiState)
                SmsPermissionStatus.NotRequested -> PermissionMessage(
                    title = stringResource(R.string.sms_permission_title),
                    body = stringResource(R.string.sms_permission_rationale),
                    buttonLabel = stringResource(R.string.sms_permission_allow),
                    onClick = { launcher.launch(SMS_PERMISSION) },
                )
                SmsPermissionStatus.Denied -> PermissionMessage(
                    title = stringResource(R.string.sms_permission_title),
                    body = stringResource(R.string.sms_permission_denied),
                    buttonLabel = stringResource(R.string.sms_permission_try_again),
                    onClick = { launcher.launch(SMS_PERMISSION) },
                )
                SmsPermissionStatus.PermanentlyDenied -> PermissionMessage(
                    title = stringResource(R.string.sms_permission_off_title),
                    body = stringResource(R.string.sms_permission_permanently_denied),
                    buttonLabel = stringResource(R.string.sms_permission_open_settings),
                    onClick = { context.openAppSettings() },
                )
            }
        }
    }
}

@Composable
private fun InboxContent(uiState: InboxUiState) {
    when (uiState) {
        InboxUiState.Loading, InboxUiState.PermissionMissing -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is InboxUiState.Loaded -> if (uiState.messages.isEmpty()) {
            CenteredText(stringResource(R.string.inbox_empty))
        } else {
            MessageList(uiState.messages)
        }
    }
}

@Composable
private fun MessageList(messages: List<SmsPreview>) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(messages, key = { it.id }) { message ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(message.sender, style = MaterialTheme.typography.titleMedium)
                Text(
                    formatter.format(message.receivedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    message.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun PermissionMessage(title: String, body: String, buttonLabel: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.widthIn(max = 480.dp))
        Button(onClick = onClick) { Text(buttonLabel) }
    }
}

@Composable
private fun CenteredText(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageListPreview() {
    FinanceTrackerTheme {
        MessageList(
            listOf(
                SmsPreview(1, "BOC", Instant.parse("2026-11-07T09:15:00Z"), "Sample alert text for the preview only"),
                SmsPreview(2, "BOC", Instant.parse("2026-11-06T18:40:00Z"), "Another sample alert"),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionMessagePreview() {
    FinanceTrackerTheme {
        PermissionMessage(
            title = stringResource(R.string.sms_permission_title),
            body = stringResource(R.string.sms_permission_rationale),
            buttonLabel = stringResource(R.string.sms_permission_allow),
            onClick = {},
        )
    }
}
