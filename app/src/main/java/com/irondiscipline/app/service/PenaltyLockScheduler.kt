package com.irondiscipline.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.irondiscipline.app.util.CoreTargetApp
import com.irondiscipline.app.util.RealtimeQuotaManager

/**
 * Schedules and cancels exact RTC_WAKEUP alarms for 5-hour penalty lock expirations
 * so locked apps unlock and reset their daily quota right on time even when the device is idle/Doze.
 */
object PenaltyLockScheduler {

    const val ACTION_PENALTY_LOCK_EXPIRED = "com.irondiscipline.app.ACTION_PENALTY_LOCK_EXPIRED"
    const val EXTRA_APP_KEY = "extra_penalty_app_key"

    private const val REQUEST_CODE_BASE = 70000

    private fun requestCodeForKey(appKey: String): Int {
        val ordinal = CoreTargetApp.fromAppKey(appKey)?.ordinal ?: (appKey.hashCode() and 0x0FFF)
        return REQUEST_CODE_BASE + ordinal
    }

    private fun buildPendingIntent(context: Context, appKey: String): PendingIntent {
        val appCtx = context.applicationContext
        val intent = Intent(appCtx, PenaltyLockExpiryReceiver::class.java).apply {
            action = ACTION_PENALTY_LOCK_EXPIRED
            data = Uri.parse("irondiscipline://penalty_unlock/$appKey")
            putExtra(EXTRA_APP_KEY, appKey)
        }
        return PendingIntent.getBroadcast(
            appCtx,
            requestCodeForKey(appKey),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedulePenaltyUnlockAlarm(context: Context, appKey: String, triggerAtEpochMs: Long) {
        if (triggerAtEpochMs <= 0L) return
        val appCtx = context.applicationContext
        val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(appCtx, appKey)

        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtEpochMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtEpochMs,
                            pendingIntent
                        )
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtEpochMs,
                        pendingIntent
                    )
                }
                else -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtEpochMs,
                        pendingIntent
                    )
                }
            }
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtEpochMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pendingIntent)
                }
            } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    fun cancelPenaltyUnlockAlarm(context: Context, appKey: String) {
        val appCtx = context.applicationContext
        val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        try {
            alarmManager.cancel(buildPendingIntent(appCtx, appKey))
        } catch (_: Exception) {}
    }

    fun rescheduleActivePenaltyAlarms(context: Context) {
        val appCtx = context.applicationContext
        val quotaManager = RealtimeQuotaManager.getInstance(appCtx)
        val nowMs = System.currentTimeMillis()
        quotaManager.reconcilePenaltyLocksAndResetExpired(nowMs)

        val prefs = appCtx.getSharedPreferences(RealtimeQuotaManager.PREFS_NAME, Context.MODE_PRIVATE)
        for (target in CoreTargetApp.entries) {
            val lockUntil = RealtimeQuotaManager.resolveLockUntilEpochMs(prefs, target.appKey)
            if (lockUntil > nowMs) {
                schedulePenaltyUnlockAlarm(appCtx, target.appKey, lockUntil)
            }
        }
    }
}
