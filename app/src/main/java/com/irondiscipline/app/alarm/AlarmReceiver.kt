package com.irondiscipline.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver triggered by AlarmManager.setAlarmClock.
 * Instantly acquires WakeLocks to prevent CPU sleep and starts AlarmRingingService
 * in the foreground immediately without gating on lock-screen Activity launch.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "IronAlarmTrace"
        const val EXTRA_RECEIVER_TRIGGER_MS = "extra_receiver_trigger_ms"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val safeIntent = intent ?: return
        val action = safeIntent.action
        val now = System.currentTimeMillis()

        // 1. Boot recovery and system time/timezone/permission change events
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.LOCKED_BOOT_COMPLETED" ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == "android.intent.action.TIME_SET" ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        ) {
            Log.i(TAG, "[IronAlarmTrace:SYSTEM_EVENT] Received action=$action -> rescheduling all active alarms")
            try {
                AlarmRepository.getInstance(context).rescheduleAllActiveAlarms()
            } catch (e: Exception) {
                Log.e(TAG, "[IronAlarmTrace:SYSTEM_EVENT_ERROR] Error rescheduling active alarms: ${e.message}")
            }
            try {
                com.irondiscipline.app.service.PenaltyLockScheduler.rescheduleActivePenaltyAlarms(context)
            } catch (_: Exception) {}
            return
        }

        // 2. First line for alarm triggers: Acquire CPU & Screen WakeLocks immediately
        AlarmWakeLockManager.acquireRingingWakeLocks(context)

        // Extract alarm ID from extras, with fallback to data URI path segment
        val alarmId = safeIntent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID)
            ?: safeIntent.getStringExtra("EXTRA_ALARM_ID")
            ?: safeIntent.data?.lastPathSegment
            ?: run {
                Log.e(TAG, "[IronAlarmTrace:STAGE3_RECEIVER_ERROR] Trigger intent received with NULL alarmId! data=${safeIntent.data}, action=$action")
                return
            }

        val alarmLabel = safeIntent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL)
            ?: safeIntent.getStringExtra("EXTRA_ALARM_LABEL")
        val soundIndex = if (safeIntent.hasExtra(AlarmScheduler.EXTRA_SOUND_INDEX)) {
            safeIntent.getIntExtra(AlarmScheduler.EXTRA_SOUND_INDEX, 0)
        } else {
            safeIntent.getIntExtra("EXTRA_SOUND_INDEX", 0)
        }
        val deleteAfter = if (safeIntent.hasExtra(AlarmScheduler.EXTRA_DELETE_AFTER_RINGING)) {
            safeIntent.getBooleanExtra(AlarmScheduler.EXTRA_DELETE_AFTER_RINGING, false)
        } else {
            safeIntent.getBooleanExtra("EXTRA_DELETE_AFTER_RINGING", false)
        }

        Log.i(
            TAG,
            "[IronAlarmTrace:STAGE3_RECEIVER] SUCCESS -> Trigger received at $now for Alarm ID=$alarmId, label=$alarmLabel, soundIndex=$soundIndex, deleteAfter=$deleteAfter, action=$action, data=${safeIntent.data}"
        )

        when (action) {
            AlarmScheduler.ACTION_DISMISS_ALARM -> {
                Log.d(TAG, "[IronAlarmTrace:RECEIVER_DISMISS] Explicit dismiss action for alarmId=$alarmId")
                AlarmRingingService.stopAndCleanup(context, alarmId)
            }

            AlarmScheduler.ACTION_SNOOZE_ALARM -> {
                // Snooze is permanently disabled under IronDiscipline protocols
                Log.d(TAG, "[IronAlarmTrace:RECEIVER_SNOOZE] Snooze action (disabled) for alarmId=$alarmId")
                AlarmRingingService.stopAndCleanup(context, alarmId)
            }

            else -> {
                if (alarmId == "FOLLOWUP_5MIN_ALARM") {
                    AlarmScheduler.clearFollowUpAlarm(context)
                    AlarmWakeLockManager.releaseAll()
                    return
                }

                val prefs = context.getSharedPreferences("iron_discipline_prefs", Context.MODE_PRIVATE)
                val alarm = try {
                    AlarmRepository.getInstance(context).getAlarmById(alarmId)
                } catch (_: Exception) {
                    null
                }
                val intentQuestionCount = if (safeIntent.hasExtra(AlarmScheduler.EXTRA_QUESTION_COUNT)) {
                    safeIntent.getIntExtra(AlarmScheduler.EXTRA_QUESTION_COUNT, -1)
                } else {
                    -1
                }
                val questionCount = (when {
                    intentQuestionCount in 1..100 -> intentQuestionCount
                    alarm != null -> alarm.questionCount
                    else -> prefs.getInt(
                        "alarm_question_count_$alarmId",
                        prefs.getInt("active_ringing_question_count", 5)
                    )
                }).coerceIn(1, 100)

                // Record timestamp and selected math question count synchronously via .commit()
                prefs.edit()
                    .putLong("last_triggered_timestamp_$alarmId", now)
                    .putInt("alarm_question_count_$alarmId", questionCount)
                    .putInt("active_ringing_question_count", questionCount)
                    .putBoolean("followup_alarm_pending", false)
                    .putBoolean("is_current_ring_followup", false)
                    .commit()

                // Initialize persistent Math Hard questions session state for this alarm
                ActiveAlarmSessionState.initSession(alarmId, questionCount)

                // Start Foreground Ringing Service (High Priority Audio & 100% Volume Guard)
                val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra("EXTRA_ALARM_ID", alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                    putExtra(AlarmScheduler.EXTRA_SOUND_INDEX, soundIndex)
                    putExtra(AlarmScheduler.EXTRA_DELETE_AFTER_RINGING, deleteAfter)
                    putExtra(AlarmScheduler.EXTRA_QUESTION_COUNT, questionCount)
                    putExtra(EXTRA_RECEIVER_TRIGGER_MS, now)
                    putExtra("EXTRA_IS_NEW_TRIGGER", true)
                }
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                    Log.i(TAG, "[IronAlarmTrace:STAGE3_SERVICE_CALLED] ContextCompat.startForegroundService dispatched for Alarm ID=$alarmId, questionCount=$questionCount")
                } catch (e: Exception) {
                    Log.e(TAG, "[IronAlarmTrace:STAGE3_SERVICE_ERROR] Failed to start AlarmRingingService: ${e.message}", e)
                }

                // Also launch AlarmRingingActivity directly over lock screen
                val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra("EXTRA_ALARM_ID", alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                    putExtra(AlarmScheduler.EXTRA_SOUND_INDEX, soundIndex)
                    putExtra(AlarmScheduler.EXTRA_DELETE_AFTER_RINGING, deleteAfter)
                    putExtra(AlarmScheduler.EXTRA_QUESTION_COUNT, questionCount)
                    putExtra(EXTRA_RECEIVER_TRIGGER_MS, now)
                    putExtra("EXTRA_IS_NEW_TRIGGER", true)
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                }
                try {
                    context.startActivity(activityIntent)
                    Log.i(TAG, "[IronAlarmTrace:STAGE3_ACTIVITY_CALLED] context.startActivity dispatched for Alarm ID=$alarmId")
                } catch (e: Exception) {
                    Log.w(TAG, "[IronAlarmTrace:STAGE3_ACTIVITY_WARN] Background direct startActivity blocked: ${e.message} (FGS fullScreenIntent will handle lock-screen display)")
                }
            }
        }
    }
}
