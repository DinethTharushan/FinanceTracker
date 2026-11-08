package com.app.financetracker.ui.inbox

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

enum class SmsPermissionStatus {
    /** Not granted, and no denial seen in this session. Show the explanation first. */
    NotRequested,

    /** Denied, but the system will still show the dialog again. */
    Denied,

    /** Denied with "Don't ask again" (or denied twice). Only Settings can grant it now. */
    PermanentlyDenied,

    Granted,
}

internal const val SMS_PERMISSION = Manifest.permission.READ_SMS

internal fun Context.hasSmsPermission(): Boolean =
    checkSelfPermission(SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED

/**
 * Status right after the system dialog returns. When the user picks "Don't ask again",
 * or denies a second time on Android 11+, the system stops offering a rationale.
 */
internal fun Activity.statusAfterRequest(granted: Boolean): SmsPermissionStatus = when {
    granted -> SmsPermissionStatus.Granted
    shouldShowRequestPermissionRationale(SMS_PERMISSION) -> SmsPermissionStatus.Denied
    else -> SmsPermissionStatus.PermanentlyDenied
}

internal fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
