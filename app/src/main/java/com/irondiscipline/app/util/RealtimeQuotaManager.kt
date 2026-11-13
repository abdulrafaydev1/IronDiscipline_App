package com.irondiscipline.app.util

import android.content.Context
import android.content.SharedPreferences
import com.irondiscipline.app.data.local.IronDisciplineDatabase
import com.irondiscipline.app.data.local.entities.DailyUsageLog
import com.irondiscipline.app.service.PenaltyLockScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

enum class CoreTargetApp(
    val appKey: String,
    val displayName: String,
    val packageNames: List<String>
) {
    YOUTUBE(
        appKey = "youtube",
        displayName = "YouTube",
        packageNames = listOf("com.google.android.youtube")
    ),
    FACEBOOK(
        appKey = "facebook",
        displayName = "Facebook",
        packageNames = listOf("com.facebook.katana")
    ),
    INSTAGRAM(
        appKey = "instagram",
        displayName = "Instagram",
        packageNames = listOf("com.instagram.android")
    ),
    TIKTOK(
        appKey = "tiktok",
        displayName = "TikTok",
        packageNames = listOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically.go"
        )
    );

    val canonicalKey: String get() = appKey

    companion object {
        val ALL_TARGET_PACKAGES: Set<String> = entries
            .flatMap { it.packageNames }
            .map { it.lowercase() }
            .toSet()

        fun fromPackageName(packageName: String): CoreTargetApp? {
            val lower = packageName.lowercase().trim()
            return entries.firstOrNull { target ->
                target.packageNames.any { it.equals(lower, ignoreCase = true) }
            }
        }

        fun fromAppKey(appKey: String): CoreTargetApp? {
            return entries.firstOrNull { it.appKey.equals(appKey, ignoreCase = true) }
        }

        fun isCoreTargetPackage(packageName: String): Boolean {
            return fromPackageName(packageName) != null
        }

        fun canonicalKeyForPackage(packageName: String): String {
            return fromPackageName(packageName)?.appKey ?: packageName
        }
    }
}

data class CoreAppQuotaStatus(
    val target: CoreTargetApp,
    val usedSeconds: Int,
    val limitSeconds: Int,
    val remainingSeconds: Int,
    val isLocked: Boolean,
    val lockedUntilEpochMs: Long,
    val lockRemainingMillis: Long,
    val remainingLockSeconds: Int = 0
) {
    val formattedRemainingMmSs: String
        get() = RealtimeQuotaManager.formatSecondsToMmSs(remainingSeconds)

    val formattedUsedMmSs: String
        get() = RealtimeQuotaManager.formatSecondsToMmSs(usedSeconds)

    val formattedLimitMinutes: String
        get() = "${limitSeconds / 60}m"

    val formattedLockRemainingHhMmSs: String
        get() = RealtimeQuotaManager.formatMillisToHhMmSs(lockRemainingMillis)

    val progressFraction: Float
        get() = if (isLocked) {
            1f
        } else if (limitSeconds > 0) {
            (usedSeconds.toFloat() / limitSeconds.toFloat()).coerceIn(0f, 1f)
        } else 1f
}

data class RealtimeQuotaSnapshot(
    val dateString: String = LocalDate.now().toString(),
    val isTestModeActive: Boolean = false,
    val coreStatuses: List<CoreAppQuotaStatus> = emptyList()
) {
    val anyAppLocked: Boolean
        get() = coreStatuses.any { it.isLocked || it.lockRemainingMillis > 0L }

    fun statusFor(appKey: String): CoreAppQuotaStatus? {
        return coreStatuses.firstOrNull { it.target.appKey == appKey }
    }
}

/**
 * Authoritative reader & wall-clock reconciler for "iron_discipline_prefs".
 * 5-hour penalty countdowns are always derived from `lockUntilEpochMs - System.currentTimeMillis()`,
 * never manually decremented, so lock timers keep running cleanly through screen-off sleep and app closes.
 */
class RealtimeQuotaManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _quotaSnapshotFlow = MutableStateFlow(RealtimeQuotaSnapshot())
    val quotaSnapshotFlow: StateFlow<RealtimeQuotaSnapshot> = _quotaSnapshotFlow.asStateFlow()

    init {
        reconcilePenaltyLocksAndResetExpired()
        publishSnapshot()
    }

    fun isTestModeActive(): Boolean {
        return prefs.getBoolean(KEY_IS_TEST_MODE, false) || prefs.getBoolean("is_test_mode", false)
    }

    fun getAppLimitSeconds(appKey: String): Int {
        return if (isTestModeActive()) TEST_MODE_LIMIT_SECONDS else NORMAL_MODE_LIMIT_SECONDS
    }

    fun getUsedSeconds(appKey: String): Int {
        return prefs.getInt(PREFIX_USED_SEC + appKey, 0).coerceAtLeast(0)
    }

    fun getUsedSecondsToday(appKeyOrPackage: String): Int {
        val appKey = CoreTargetApp.fromPackageName(appKeyOrPackage)?.appKey ?: appKeyOrPackage
        return getUsedSeconds(appKey)
    }

    fun getLockedUntilEpochMs(appKey: String): Long {
        return resolveLockUntilEpochMs(prefs, appKey)
    }

    fun getRemainingLockSeconds(appKey: String): Int {
        val remMs = getRemainingPenaltyLockMillisAt(prefs, appKey, System.currentTimeMillis())
        return if (remMs > 0L) ((remMs + 999L) / 1000L).toInt() else 0
    }

    fun isAppLocked(appKey: String, nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        reconcilePenaltyLocksAndResetExpired(nowEpochMs)
        val remMs = getRemainingPenaltyLockMillisAt(prefs, appKey, nowEpochMs)
        if (remMs > 0L) return true
        val used = getUsedSeconds(appKey)
        val limit = getAppLimitSeconds(appKey)
        return used >= limit
    }

    fun isAnyAppLocked(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        reconcilePenaltyLocksAndResetExpired(nowEpochMs)
        return CoreTargetApp.entries.any { isAppLocked(it.appKey, nowEpochMs) }
    }

    fun incrementUsedSecond(appKey: String, nowEpochMs: Long = System.currentTimeMillis()): Int {
        return getUsedSecondsToday(appKey)
    }

    /**
     * Persists a 5-hour wall-clock lock timestamp immediately to SharedPreferences (.commit) and Room DB,
     * and schedules an exact AlarmManager unlock wakeup at [lockUntilEpochMs].
     */
    @Synchronized
    fun triggerFiveHourPenaltyLock(
        appKeyOrPackage: String,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Long {
        val target = CoreTargetApp.fromPackageName(appKeyOrPackage)
            ?: CoreTargetApp.fromAppKey(appKeyOrPackage)
        val canonicalKey = target?.appKey ?: appKeyOrPackage.trim().lowercase()

        val existingUntil = resolveLockUntilEpochMs(prefs, canonicalKey)
        if (existingUntil > nowEpochMs) {
            return existingUntil
        }

        val lockUntilEpochMs = nowEpochMs + FIVE_HOURS_MS
        val editor = prefs.edit()
            .putLong(PREFIX_LOCKED_UNTIL + canonicalKey, lockUntilEpochMs)
            .putLong(PREFIX_PENALTY_LOCK_UNTIL + canonicalKey, lockUntilEpochMs)
            .putInt(PREFIX_REMAINING_LOCK_SEC + canonicalKey, 0)

        target?.packageNames?.forEach { pkg ->
            editor.putLong(PREFIX_PENALTY_LOCK_UNTIL + pkg, lockUntilEpochMs)
        }
        editor.commit()

        PenaltyLockScheduler.schedulePenaltyUnlockAlarm(appContext, canonicalKey, lockUntilEpochMs)
        persistPenaltyLockToRoomAsync(canonicalKey, getUsedSeconds(canonicalKey).toLong(), lockUntilEpochMs, nowEpochMs)
        publishSnapshot(nowEpochMs)
        return lockUntilEpochMs
    }

    /**
     * Reconciles all core target apps against hardware wall-clock time (`System.currentTimeMillis()`).
     * Automatically clears expired locks, resets accumulated daily usage to 0 for a fresh session,
     * and updates Room DB + AlarmManager.
     *
     * @return List of canonical appKeys unlocked during this pass.
     */
    @Synchronized
    fun reconcilePenaltyLocksAndResetExpired(nowEpochMs: Long = System.currentTimeMillis()): List<String> {
        val unlockedKeys = mutableListOf<String>()

        for (target in CoreTargetApp.entries) {
            val key = target.appKey
            var lockUntil = resolveLockUntilEpochMs(prefs, key)

            // Backwards-compatibility migration: if upgrading mid-lock from legacy remaining_lock_seconds
            val legacyRemSec = prefs.getInt(PREFIX_REMAINING_LOCK_SEC + key, 0)
            if (lockUntil <= 0L && legacyRemSec > 0) {
                lockUntil = nowEpochMs + (legacyRemSec.toLong() * 1000L)
                val migrateEditor = prefs.edit()
                    .putLong(PREFIX_LOCKED_UNTIL + key, lockUntil)
                    .putLong(PREFIX_PENALTY_LOCK_UNTIL + key, lockUntil)
                    .putInt(PREFIX_REMAINING_LOCK_SEC + key, 0)
                target.packageNames.forEach { pkg ->
                    migrateEditor.putLong(PREFIX_PENALTY_LOCK_UNTIL + pkg, lockUntil)
                }
                migrateEditor.commit()
                PenaltyLockScheduler.schedulePenaltyUnlockAlarm(appContext, key, lockUntil)
            }

            if (lockUntil > 0L && nowEpochMs >= lockUntil) {
                val editor = prefs.edit()
                clearPenaltyStateInEditor(editor, key, target.packageNames, resetUsage = true)
                editor.commit()

                PenaltyLockScheduler.cancelPenaltyUnlockAlarm(appContext, key)
                clearExpiredLockInRoomAsync(key, target.packageNames)
                unlockedKeys.add(key)
            }
        }

        return unlockedKeys
    }

    private fun clearPenaltyStateInEditor(
        editor: SharedPreferences.Editor,
        canonicalKey: String,
        packageNames: List<String>,
        resetUsage: Boolean
    ) {
        editor.putLong(PREFIX_LOCKED_UNTIL + canonicalKey, 0L)
            .putLong(PREFIX_PENALTY_LOCK_UNTIL + canonicalKey, 0L)
            .putInt(PREFIX_REMAINING_LOCK_SEC + canonicalKey, 0)
        packageNames.forEach { pkg ->
            editor.putLong(PREFIX_PENALTY_LOCK_UNTIL + pkg, 0L)
        }
        if (resetUsage) {
            editor.putInt(PREFIX_USED_SEC + canonicalKey, 0)
                .putLong(PREFIX_USED_MS + canonicalKey, 0L)
        }
    }

    private fun persistPenaltyLockToRoomAsync(
        canonicalKey: String,
        usedSeconds: Long,
        lockedUntilEpochMs: Long,
        nowEpochMs: Long
    ) {
        ioScope.launch {
            try {
                val dao = IronDisciplineDatabase.getInstance(appContext).disciplineDao()
                val todayStr = LocalDate.now().toString()
                val existing = dao.getUsageLogForPackage(todayStr, canonicalKey)
                dao.upsertUsageLog(
                    DailyUsageLog(
                        dateString = todayStr,
                        packageName = canonicalKey,
                        usedSeconds = maxOf(usedSeconds, existing?.usedSeconds ?: 0L),
                        blockedAttemptsCount = existing?.blockedAttemptsCount ?: 0,
                        lockedUntilEpochMs = lockedUntilEpochMs,
                        lastUpdatedEpochMs = nowEpochMs
                    )
                )
            } catch (_: Exception) {}
        }
    }

    private fun clearExpiredLockInRoomAsync(canonicalKey: String, packageNames: List<String>) {
        ioScope.launch {
            try {
                val dao = IronDisciplineDatabase.getInstance(appContext).disciplineDao()
                dao.clearLockAndResetUsageAllDatesForPackage(canonicalKey)
                for (pkg in packageNames) {
                    dao.clearLockAndResetUsageAllDatesForPackage(pkg)
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Publishes latest SharedPreferences state to the UI StateFlow after reconciling
     * any expired wall-clock penalty locks. Never mutates active usage counts.
     */
    @Synchronized
    fun publishSnapshot(nowEpochMs: Long = System.currentTimeMillis()) {
        reconcilePenaltyLocksAndResetExpired(nowEpochMs)
        val isTestMode = isTestModeActive()
        val limit = if (isTestMode) TEST_MODE_LIMIT_SECONDS else NORMAL_MODE_LIMIT_SECONDS

        val statuses = CoreTargetApp.entries.map { target ->
            val key = target.appKey
            val used = prefs.getInt(PREFIX_USED_SEC + key, 0).coerceAtLeast(0)
            val lockedUntil = resolveLockUntilEpochMs(prefs, key)
            val lockRemainingMs = if (lockedUntil > nowEpochMs) lockedUntil - nowEpochMs else 0L
            val remainingLockSec = if (lockRemainingMs > 0L) {
                ((lockRemainingMs + 999L) / 1000L).toInt()
            } else {
                0
            }
            val isLocked = lockRemainingMs > 0L || used >= limit
            val remainingSec = if (isLocked) 0 else (limit - used).coerceAtLeast(0)

            CoreAppQuotaStatus(
                target = target,
                usedSeconds = used,
                limitSeconds = limit,
                remainingSeconds = remainingSec,
                isLocked = isLocked,
                lockedUntilEpochMs = lockedUntil,
                lockRemainingMillis = lockRemainingMs,
                remainingLockSeconds = remainingLockSec
            )
        }

        _quotaSnapshotFlow.value = RealtimeQuotaSnapshot(
            dateString = LocalDate.now().toString(),
            isTestModeActive = isTestMode,
            coreStatuses = statuses
        )
    }

    fun refreshSnapshotNow(nowEpochMs: Long = System.currentTimeMillis()) {
        publishSnapshot(nowEpochMs)
    }

    fun checkMidnightResetIfNeeded(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        publishSnapshot(nowEpochMs)
        return false
    }

    /**
     * Activated when the user toggles 1-Minute Test Mode on the Dashboard.
     * Blocked if ANY app is locked for 5 hours.
     */
    @Synchronized
    fun activateGlobalOneMinuteTestMode() {
        if (isAnyAppLocked()) return
        applyGlobalLimitMode(isTestMode = true, limitSeconds = TEST_MODE_LIMIT_SECONDS)
    }

    /**
     * Activated when the user switches to 30-Minute Normal Mode on the Dashboard.
     * Blocked if ANY app is locked for 5 hours.
     */
    @Synchronized
    fun switchBackToNormalThirtyMinModeAndReset() {
        if (isAnyAppLocked()) return
        applyGlobalLimitMode(isTestMode = false, limitSeconds = NORMAL_MODE_LIMIT_SECONDS)
    }

    private fun applyGlobalLimitMode(isTestMode: Boolean, limitSeconds: Int) {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_TEST_MODE, isTestMode)
            .putBoolean("is_test_mode", isTestMode)

        for (target in CoreTargetApp.entries) {
            val key = target.appKey
            editor.putInt(PREFIX_LIMIT_SEC + key, limitSeconds)
            clearPenaltyStateInEditor(editor, key, target.packageNames, resetUsage = true)
        }
        editor.commit()
        publishSnapshot()
    }

    /**
     * Reset All Timers & Locks (Blocked while any 5-hour lock is active).
     */
    @Synchronized
    fun resetAllTimersAndLocks() {
        if (isAnyAppLocked()) return

        val editor = prefs.edit()
        for (target in CoreTargetApp.entries) {
            clearPenaltyStateInEditor(editor, target.appKey, target.packageNames, resetUsage = true)
            PenaltyLockScheduler.cancelPenaltyUnlockAlarm(appContext, target.appKey)
        }
        editor.commit()
        publishSnapshot()
    }

    @Synchronized
    fun resetAppTimerAndLock(appKey: String) {
        if (getRemainingPenaltyLockMillis(prefs, appKey) > 0L) return

        val isTest = isTestModeActive()
        val limit = if (isTest) TEST_MODE_LIMIT_SECONDS else NORMAL_MODE_LIMIT_SECONDS
        val target = CoreTargetApp.fromAppKey(appKey) ?: CoreTargetApp.fromPackageName(appKey)
        val canonicalKey = target?.appKey ?: appKey
        val editor = prefs.edit()
            .putInt(PREFIX_LIMIT_SEC + canonicalKey, limit)
        clearPenaltyStateInEditor(
            editor = editor,
            canonicalKey = canonicalKey,
            packageNames = target?.packageNames ?: emptyList(),
            resetUsage = true
        )
        editor.commit()
        PenaltyLockScheduler.cancelPenaltyUnlockAlarm(appContext, canonicalKey)
        publishSnapshot()
    }

    /**
     * Disarms active penalty locks during PIN-authorized uninstallation.
     */
    @Synchronized
    fun disarmAllPenaltyLocksForUninstall() {
        val editor = prefs.edit()
        for (target in CoreTargetApp.entries) {
            clearPenaltyStateInEditor(editor, target.appKey, target.packageNames, resetUsage = false)
            PenaltyLockScheduler.cancelPenaltyUnlockAlarm(appContext, target.appKey)
        }
        editor.commit()
        publishSnapshot()
    }

    @Synchronized
    fun setAppLimitMinutes(appKey: String, limitMinutes: Int) {
        if (getRemainingPenaltyLockMillis(prefs, appKey) > 0L) return
        val limitSec = (limitMinutes.coerceAtLeast(1)) * 60
        prefs.edit().putInt(PREFIX_LIMIT_SEC + appKey, limitSec).commit()
        publishSnapshot()
    }

    companion object {
        const val PREFS_NAME = "iron_discipline_prefs"
        const val KEY_TRACKED_DATE = "tracked_date_yyyy_mm_dd"
        const val KEY_IS_TEST_MODE = "is_one_minute_test_mode"

        const val PREFIX_USED_SEC = "used_seconds_"
        const val PREFIX_USED_MS = "used_millis_"
        const val PREFIX_LIMIT_SEC = "limit_seconds_"
        const val PREFIX_LOCKED_UNTIL = "locked_until_epoch_ms_"
        const val PREFIX_PENALTY_LOCK_UNTIL = "penalty_lock_until_"
        const val PREFIX_REMAINING_LOCK_SEC = "remaining_lock_seconds_"
        const val PREFIX_LOCK_STARTED_ELAPSED_MS = "lock_started_elapsed_ms_"

        const val FIVE_HOURS_SECONDS = 5 * 60 * 60 // 18,000 seconds
        const val FIVE_HOURS_MS = 18_000_000L      // 18,000,000 ms

        const val NORMAL_MODE_LIMIT_SECONDS = 1800 // 30 minutes
        const val TEST_MODE_LIMIT_SECONDS = 60     // 1 minute
        const val TEST_MODE_LOCK_DURATION_MS = FIVE_HOURS_MS

        @Volatile
        private var INSTANCE: RealtimeQuotaManager? = null

        fun getInstance(context: Context): RealtimeQuotaManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealtimeQuotaManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun resolveLockUntilEpochMs(prefs: SharedPreferences, packageNameOrKey: String): Long {
            val trimmed = packageNameOrKey.trim()
            if (trimmed.isEmpty()) return 0L

            val target = CoreTargetApp.fromPackageName(trimmed) ?: CoreTargetApp.fromAppKey(trimmed)
            var maxUntil = maxOf(
                prefs.getLong(PREFIX_PENALTY_LOCK_UNTIL + trimmed, 0L),
                prefs.getLong(PREFIX_LOCKED_UNTIL + trimmed, 0L)
            )
            if (target != null) {
                maxUntil = maxOf(
                    maxUntil,
                    prefs.getLong(PREFIX_PENALTY_LOCK_UNTIL + target.appKey, 0L),
                    prefs.getLong(PREFIX_LOCKED_UNTIL + target.appKey, 0L)
                )
                for (pkg in target.packageNames) {
                    maxUntil = maxOf(maxUntil, prefs.getLong(PREFIX_PENALTY_LOCK_UNTIL + pkg, 0L))
                }
            }
            return maxUntil
        }

        fun getRemainingPenaltyLockMillis(prefs: SharedPreferences, packageName: String): Long {
            return getRemainingPenaltyLockMillisAt(prefs, packageName, System.currentTimeMillis())
        }

        fun getRemainingPenaltyLockMillisAt(
            prefs: SharedPreferences,
            packageName: String,
            nowEpochMs: Long
        ): Long {
            val lockUntil = resolveLockUntilEpochMs(prefs, packageName)
            if (lockUntil <= 0L) return 0L
            val remaining = lockUntil - nowEpochMs
            return if (remaining > 0L) remaining else 0L
        }

        fun formatSecondsToMmSs(totalSeconds: Int): String {
            val clamped = totalSeconds.coerceAtLeast(0)
            val mins = clamped / 60
            val secs = clamped % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }

        fun formatMillisToHhMmSs(remainingMillis: Long): String {
            val totalSeconds = (remainingMillis.coerceAtLeast(0L) + 999L) / 1000L
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            val seconds = totalSeconds % 60L
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun formatSecondsToHhMmSs(totalSeconds: Int): String {
            val clamped = totalSeconds.coerceAtLeast(0)
            val hours = clamped / 3600
            val minutes = (clamped % 3600) / 60
            val seconds = clamped % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
