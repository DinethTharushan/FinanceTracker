package com.app.financetracker.ui.permission

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.app.financetracker.R
import com.app.financetracker.ui.theme.FinanceTrackerTheme

/**
 * Shows [content] once READ_SMS is granted; until then, explains why it is needed and
 * asks for it. [onGranted] runs when the user grants it and on every resume while it
 * is granted (the user may have changed it in Settings).
 */
@Composable
fun SmsPermissionGate(onGranted: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnGranted by rememberUpdatedState(onGranted)
    var status by rememberSaveable {
        mutableStateOf(
            if (context.hasSmsPermission()) SmsPermissionStatus.Granted else SmsPermissionStatus.NotRequested,
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = activity?.statusAfterRequest(granted)
            ?: if (granted) SmsPermissionStatus.Granted else SmsPermissionStatus.Denied
        if (granted) currentOnGranted()
    }

    LifecycleResumeEffect(Unit) {
        if (context.hasSmsPermission()) {
            status = SmsPermissionStatus.Granted
            currentOnGranted()
        } else if (status == SmsPermissionStatus.Granted) {
            status = SmsPermissionStatus.NotRequested
        }
        onPauseOrDispose { }
    }

    if (status == SmsPermissionStatus.Granted) {
        content()
        return
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (status) {
                SmsPermissionStatus.Granted -> Unit
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
