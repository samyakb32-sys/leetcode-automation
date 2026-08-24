package com.leetcodeautomation.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val CHANNEL_ID = "streak_automation"
private const val NOTIFICATION_ID = 1001

/** Posts a summary notification after a headless streak run, since there's no UI watching. */
object StreakNotifier {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Streak automation",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Results of automatic background LeetCode streak runs"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun notifyResult(context: Context, solvedSlugs: List<String>, failedSlugs: List<String>) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val title = if (failedSlugs.isEmpty()) "Streak protected ✅" else "Streak run needs attention ⚠️"
        val body = buildString {
            if (solvedSlugs.isNotEmpty()) append("Accepted: ${solvedSlugs.joinToString(", ")}")
            if (failedSlugs.isNotEmpty()) {
                if (isNotEmpty()) append(" — ")
                append("Failed: ${failedSlugs.joinToString(", ")}")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
