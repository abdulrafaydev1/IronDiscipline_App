package com.irondiscipline.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.irondiscipline.app.util.RealtimeQuotaManager

/**
 * Handles exact RTC_WAKEUP broadcasts when a 5-hour penalty lock expires,
 * clearing expired locks and resetting the daily quota even while the screen was off or app closed.
 */
class PenaltyLockExpiryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == PenaltyLockScheduler.ACTION_PENALTY_LOCK_EXPIRED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == "android.intent.action.TIME_SET" ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val quotaManager = RealtimeQuotaManager.getInstance(context.applicationContext)
            val unlockedKeys = quotaManager.reconcilePenaltyLocksAndResetExpired()
            PenaltyLockScheduler.rescheduleActivePenaltyAlarms(context.applicationContext)
            AppBlockerAccessibilityService.onPenaltyLocksReconciled(unlockedKeys)
        }
    }
}
