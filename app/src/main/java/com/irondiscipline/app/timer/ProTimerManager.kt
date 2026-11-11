package com.irondiscipline.app.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class ProTimerState {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED_RINGING
}

data class TimerSoundInfo(
    val id: Int,
    val name: String,
    val topic: String,
    val description: String
)

/**
 * Studio-Grade High-Precision Countdown Timer Engine & Topic-Based Sound Synthesizer.
 * Operates monotonically using SystemClock.elapsedRealtime() so screen-off, app switching,
 * and dialog closures never disrupt countdown precision or overtime tracking.
 */
object ProTimerManager {

    private const val TAG = "ProTimerManager"
    const val SAMPLE_RATE = 44100

    val TOPICS = listOf(
        "🔔 Classic Chimes",
        "⚡ High-Energy Alert",
        "🧘 Zen & Focus",
        "🚨 Tactical Siren",
        "🌊 Cyber Pulse"
    )

    val SOUNDS = listOf(
        // Topic 1: 🔔 Classic Chimes
        TimerSoundInfo(0, "Crystal Zen Bell", "🔔 Classic Chimes", "Harmonic 528Hz + 1056Hz decaying chime"),
        TimerSoundInfo(1, "Grand Westminster Gong", "🔔 Classic Chimes", "Warm resonant dual-chord bell"),

        // Topic 2: ⚡ High-Energy Alert
        TimerSoundInfo(2, "Hyper Pulse Beep", "⚡ High-Energy Alert", "Rapid 1000Hz staccato digital double-beep"),
        TimerSoundInfo(3, "Arcade Victory Fanfare", "⚡ High-Energy Alert", "Ascending arpeggio C5-E5-G5-C6"),

        // Topic 3: 🧘 Zen & Focus
        TimerSoundInfo(4, "Tibetan Singing Bowl", "🧘 Zen & Focus", "Smooth 432Hz sine wave with gentle vibrato"),
        TimerSoundInfo(5, "Soft Bamboo Marimba", "🧘 Zen & Focus", "Woody percussive melodic pattern"),

        // Topic 4: 🚨 Tactical Siren
        TimerSoundInfo(6, "Nuclear Bunker Klaxon", "🚨 Tactical Siren", "Sweeping 600Hz-1400Hz urgent siren"),
        TimerSoundInfo(7, "Red Alert Strobe Horn", "🚨 Tactical Siren", "Industrial low-high brass horn"),

        // Topic 5: 🌊 Cyber Pulse
        TimerSoundInfo(8, "Neon Synth Matrix", "🌊 Cyber Pulse", "Futuristic FM synth pulse"),
        TimerSoundInfo(9, "Quantum Radar Ping", "🌊 Cyber Pulse", "Sonar sweep with echo decay")
    )

    private val scope = CoroutineScope(Dispatchers.Default)

    // State Flows
    private val _timerState = MutableStateFlow(ProTimerState.IDLE)
    val timerState: StateFlow<ProTimerState> = _timerState.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(5 * 60 * 1000L) // Default 5 mins
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _remainingMs = MutableStateFlow(5 * 60 * 1000L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private val _overtimeMs = MutableStateFlow(0L)
    val overtimeMs: StateFlow<Long> = _overtimeMs.asStateFlow()

    private val _selectedTopic = MutableStateFlow(TOPICS[0])
    val selectedTopic: StateFlow<String> = _selectedTopic.asStateFlow()

    private val _selectedSoundId = MutableStateFlow(0)
    val selectedSoundId: StateFlow<Int> = _selectedSoundId.asStateFlow()

    private val _timerLabel = MutableStateFlow("Deep Focus Session")
    val timerLabel: StateFlow<String> = _timerLabel.asStateFlow()

    private val _isLoopEnabled = MutableStateFlow(false)
    val isLoopEnabled: StateFlow<Boolean> = _isLoopEnabled.asStateFlow()

    private val _completedCycles = MutableStateFlow(0)
    val completedCycles: StateFlow<Int> = _completedCycles.asStateFlow()

    private val _isFinalTenSecBeepEnabled = MutableStateFlow(true)
    val isFinalTenSecBeepEnabled: StateFlow<Boolean> = _isFinalTenSecBeepEnabled.asStateFlow()

    private val _isKeepScreenOnEnabled = MutableStateFlow(false)
    val isKeepScreenOnEnabled: StateFlow<Boolean> = _isKeepScreenOnEnabled.asStateFlow()

    private val _laps = MutableStateFlow<List<String>>(emptyList())
    val laps: StateFlow<List<String>> = _laps.asStateFlow()

    private val _customPresets = MutableStateFlow<List<Pair<String, Long>>>(
        listOf(
            "🍅 Pomodoro 25m" to (25 * 60 * 1000L),
            "💻 Coding 45m" to (45 * 60 * 1000L),
            "☕ Break 5m" to (5 * 60 * 1000L),
            "⚡ Nap 20m" to (20 * 60 * 1000L),
            "🏋️ HIIT 10m" to (10 * 60 * 1000L)
        )
    )
    val customPresets: StateFlow<List<Pair<String, Long>>> = _customPresets.asStateFlow()

    // Internal timing references
    private var targetEndRealtimeMs = 0L
    private var finishedRealtimeMs = 0L
    private var tickerJob: Job? = null
    private var lastBeepSecond = -1L

    // Audio & Vibration Engine
    private val audioLock = Any()
    @Volatile private var isAudioPlaying = false
    @Volatile private var currentAudioTrack: AudioTrack? = null
    @Volatile private var audioPlaybackThread: Thread? = null
    private var previewJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Pre-synthesized 40ms 880Hz tick beep buffer
    private val tickBeepBuffer: ShortArray by lazy { generateTickBeepPattern() }

    // =========================================================================
    // TIMER CONTROLS
    // =========================================================================

    fun setTopic(topic: String) {
        _selectedTopic.value = topic
        val firstInTopic = SOUNDS.firstOrNull { it.topic == topic }
        if (firstInTopic != null && SOUNDS.none { it.id == _selectedSoundId.value && it.topic == topic }) {
            _selectedSoundId.value = firstInTopic.id
        }
    }

    fun setSelectedSoundId(soundId: Int) {
        _selectedSoundId.value = soundId
        val sound = SOUNDS.firstOrNull { it.id == soundId }
        if (sound != null) {
            _selectedTopic.value = sound.topic
        }
    }

    fun setTimerLabel(label: String) {
        _timerLabel.value = label
    }

    fun toggleLoop() {
        _isLoopEnabled.value = !_isLoopEnabled.value
    }

    fun toggleFinalTenSecBeep() {
        _isFinalTenSecBeepEnabled.value = !_isFinalTenSecBeepEnabled.value
    }

    fun toggleKeepScreenOn() {
        _isKeepScreenOnEnabled.value = !_isKeepScreenOnEnabled.value
    }

    fun setDuration(hours: Int, minutes: Int, seconds: Int) {
        if (_timerState.value != ProTimerState.IDLE) return
        val total = (hours * 3600L + minutes * 60L + seconds) * 1000L
        val safeTotal = total.coerceAtLeast(1000L)
        _totalDurationMs.value = safeTotal
        _remainingMs.value = safeTotal
    }

    fun setTotalDurationMs(ms: Long) {
        if (_timerState.value != ProTimerState.IDLE) return
        val safeTotal = ms.coerceAtLeast(1000L)
        _totalDurationMs.value = safeTotal
        _remainingMs.value = safeTotal
    }

    /**
     * Adds seconds on the fly — works both when IDLE and while RUNNING/PAUSED.
     */
    fun addSeconds(seconds: Long) {
        val addMs = seconds * 1000L
        when (_timerState.value) {
            ProTimerState.RUNNING -> {
                targetEndRealtimeMs += addMs
                _totalDurationMs.value += addMs
                _remainingMs.value = (targetEndRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            }
            ProTimerState.PAUSED -> {
                _remainingMs.value += addMs
                _totalDurationMs.value += addMs
            }
            ProTimerState.IDLE -> {
                val newTotal = (_totalDurationMs.value + addMs).coerceAtLeast(1000L)
                _totalDurationMs.value = newTotal
                _remainingMs.value = newTotal
            }
            ProTimerState.FINISHED_RINGING -> {
                // If ringing, stop ringing and set to added time
                stopCompletionRinging()
                _totalDurationMs.value = addMs.coerceAtLeast(1000L)
                _remainingMs.value = _totalDurationMs.value
                _overtimeMs.value = 0L
                _timerState.value = ProTimerState.IDLE
            }
        }
    }

    fun start(context: Context) {
        val appContext = context.applicationContext
        when (_timerState.value) {
            ProTimerState.IDLE -> {
                targetEndRealtimeMs = SystemClock.elapsedRealtime() + _totalDurationMs.value
                _remainingMs.value = _totalDurationMs.value
                _overtimeMs.value = 0L
                _timerState.value = ProTimerState.RUNNING
                lastBeepSecond = -1L
                startTicker(appContext)
            }
            ProTimerState.PAUSED -> {
                targetEndRealtimeMs = SystemClock.elapsedRealtime() + _remainingMs.value
                _timerState.value = ProTimerState.RUNNING
                lastBeepSecond = -1L
                startTicker(appContext)
            }
            ProTimerState.FINISHED_RINGING -> {
                stopCompletionRinging()
                restartSameTimer(appContext)
            }
            ProTimerState.RUNNING -> {
                // Already running
            }
        }
    }

    fun pause() {
        if (_timerState.value == ProTimerState.RUNNING) {
            val now = SystemClock.elapsedRealtime()
            _remainingMs.value = (targetEndRealtimeMs - now).coerceAtLeast(0L)
            _timerState.value = ProTimerState.PAUSED
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    fun resume(context: Context) {
        start(context)
    }

    fun reset() {
        stopCompletionRinging()
        tickerJob?.cancel()
        tickerJob = null
        _timerState.value = ProTimerState.IDLE
        _remainingMs.value = _totalDurationMs.value
        _overtimeMs.value = 0L
        _completedCycles.value = 0
    }

    fun stopAndReset() {
        reset()
    }

    fun restartSameTimer(context: Context) {
        stopCompletionRinging()
        tickerJob?.cancel()
        tickerJob = null
        _remainingMs.value = _totalDurationMs.value
        _overtimeMs.value = 0L
        _timerState.value = ProTimerState.IDLE
        start(context)
    }

    fun addLap() {
        val currentLap = when (_timerState.value) {
            ProTimerState.RUNNING, ProTimerState.PAUSED -> {
                val remFormatted = formatTimeWithDeciseconds(_remainingMs.value)
                val totalFormatted = formatTime(_totalDurationMs.value)
                "Split #${_laps.value.size + 1}: $remFormatted (of $totalFormatted)"
            }
            ProTimerState.FINISHED_RINGING -> {
                "Overtime Lap #${_laps.value.size + 1}: +${formatOvertime(_overtimeMs.value)}"
            }
            ProTimerState.IDLE -> {
                "Lap #${_laps.value.size + 1}: ${formatTime(_totalDurationMs.value)}"
            }
        }
        _laps.value = listOf(currentLap) + _laps.value
    }

    fun clearLaps() {
        _laps.value = emptyList()
    }

    fun saveCurrentAsPreset(customName: String) {
        val name = customName.trim().ifBlank {
            "⏱️ Preset ${formatTime(_totalDurationMs.value)}"
        }
        val current = _customPresets.value.toMutableList()
        current.add(name to _totalDurationMs.value)
        _customPresets.value = current
    }

    fun applyPreset(presetDurationMs: Long) {
        if (_timerState.value == ProTimerState.IDLE) {
            setTotalDurationMs(presetDurationMs)
        }
    }

    // =========================================================================
    // HIGH-PRECISION COUNTDOWN TICKER (100ms)
    // =========================================================================

    private fun startTicker(appContext: Context) {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(100L)
                val now = SystemClock.elapsedRealtime()

                when (_timerState.value) {
                    ProTimerState.RUNNING -> {
                        val rem = (targetEndRealtimeMs - now).coerceAtLeast(0L)
                        _remainingMs.value = rem

                        // 🔟 Last 10s Countdown Tick Beep
                        if (_isFinalTenSecBeepEnabled.value && rem in 1000L..10_000L) {
                            val currentSec = rem / 1000L
                            if (currentSec != lastBeepSecond) {
                                lastBeepSecond = currentSec
                                playTickBeep()
                            }
                        }

                        if (rem <= 0L) {
                            if (_isLoopEnabled.value) {
                                _completedCycles.value += 1
                                previewSound(_selectedSoundId.value)
                                targetEndRealtimeMs = SystemClock.elapsedRealtime() + _totalDurationMs.value
                                _remainingMs.value = _totalDurationMs.value
                                lastBeepSecond = -1L
                            } else {
                                _timerState.value = ProTimerState.FINISHED_RINGING
                                finishedRealtimeMs = SystemClock.elapsedRealtime()
                                _overtimeMs.value = 0L
                                startCompletionRinging(appContext)
                            }
                        }
                    }
                    ProTimerState.FINISHED_RINGING -> {
                        _overtimeMs.value = (now - finishedRealtimeMs).coerceAtLeast(0L)
                    }
                    else -> {
                        break
                    }
                }
            }
        }
    }

    // =========================================================================
    // AUDIO SYNTHESIS & SOUND PLAYBACK
    // =========================================================================

    fun previewSound(soundId: Int) {
        stopCompletionRinging()
        previewJob?.cancel()

        playSynthesizedSoundLoop(soundId, isLoop = false)

        previewJob = scope.launch {
            delay(2200L)
            stopSoundAudio()
        }
    }

    fun startCompletionRinging(context: Context) {
        val appContext = context.applicationContext
        acquireWakeLock(appContext)
        vibrateCompletion(appContext)
        playSynthesizedSoundLoop(_selectedSoundId.value, isLoop = true)
    }

    fun stopCompletionRinging() {
        stopSoundAudio()
        releaseWakeLock()
        previewJob?.cancel()
        previewJob = null
    }

    private fun playSynthesizedSoundLoop(soundId: Int, isLoop: Boolean) {
        stopSoundAudio()

        synchronized(audioLock) {
            isAudioPlaying = true

            audioPlaybackThread = Thread {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
                var track: AudioTrack? = null
                try {
                    val pcmBuffer = generateSoundPatternBuffer(soundId)
                    val minBuf = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(pcmBuffer.size * 2)

                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                    val audioFormat = AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()

                    track = AudioTrack.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setAudioFormat(audioFormat)
                        .setBufferSizeInBytes(minBuf * 2)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    currentAudioTrack = track

                    if (track.state != AudioTrack.STATE_INITIALIZED) {
                        return@Thread
                    }

                    track.play()

                    do {
                        val written = track.write(pcmBuffer, 0, pcmBuffer.size, AudioTrack.WRITE_BLOCKING)
                        if (written < 0) break
                        if (isLoop && isAudioPlaying) {
                            try { Thread.sleep(250L) } catch (_: InterruptedException) { break }
                        }
                    } while (isLoop && isAudioPlaying && !Thread.currentThread().isInterrupted)

                } catch (_: Exception) {
                } finally {
                    try {
                        if (track?.state == AudioTrack.STATE_INITIALIZED) {
                            try { track?.stop() } catch (_: Exception) {}
                            try { track?.flush() } catch (_: Exception) {}
                        }
                        try { track?.release() } catch (_: Exception) {}
                    } catch (_: Exception) {}
                    synchronized(audioLock) {
                        if (currentAudioTrack === track) {
                            currentAudioTrack = null
                        }
                    }
                }
            }.apply {
                name = "ProTimerAudioThread-${System.currentTimeMillis()}"
                isDaemon = true
                start()
            }
        }
    }

    private fun stopSoundAudio() {
        synchronized(audioLock) {
            isAudioPlaying = false
            audioPlaybackThread?.interrupt()
            audioPlaybackThread = null

            try {
                currentAudioTrack?.let { track ->
                    if (track.state == AudioTrack.STATE_INITIALIZED) {
                        try { track.pause() } catch (_: Exception) {}
                        try { track.flush() } catch (_: Exception) {}
                    }
                    try { track.release() } catch (_: Exception) {}
                }
            } catch (_: Exception) {
            } finally {
                currentAudioTrack = null
            }
        }
    }

    private fun playTickBeep() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(tickBeepBuffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(tickBeepBuffer, 0, tickBeepBuffer.size)
            track.setNotificationMarkerPosition(tickBeepBuffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try { t?.release() } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
            track.play()
        } catch (_: Exception) {}
    }

    private fun acquireWakeLock(context: Context) {
        try {
            if (wakeLock?.isHeld != true) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IronDiscipline:ProTimerWakeLock")
                wakeLock?.acquire(10 * 60 * 1000L) // 10 min safeguard
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun vibrateCompletion(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 350, 150, 350, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 350, 150, 350, 500), 0)
            }
        } catch (_: Exception) {}
    }

    // =========================================================================
    // PCM SYNTHESIS ALGORITHMS (10 Sounds across 5 Topics)
    // =========================================================================

    private fun generateSoundPatternBuffer(soundId: Int): ShortArray {
        return when (soundId) {
            0 -> generateCrystalZenBell()
            1 -> generateGrandWestminsterGong()
            2 -> generateHyperPulseBeep()
            3 -> generateArcadeVictoryFanfare()
            4 -> generateTibetanSingingBowl()
            5 -> generateSoftBambooMarimba()
            6 -> generateNuclearBunkerKlaxon()
            7 -> generateRedAlertStrobeHorn()
            8 -> generateNeonSynthMatrix()
            9 -> generateQuantumRadarPing()
            else -> generateCrystalZenBell()
        }
    }

    // 0. Crystal Zen Bell (528Hz + 1056Hz harmonic decaying chime)
    private fun generateCrystalZenBell(): ShortArray {
        val durationSec = 1.8
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-2.2 * t)
            val wave = 0.7 * sin(2.0 * PI * 528.0 * t) + 0.3 * sin(2.0 * PI * 1056.0 * t)
            buffer[i] = (wave * envelope * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 1. Grand Westminster Gong (Resonant dual-chord bell 440Hz + 554Hz with 220Hz sub warmth)
    private fun generateGrandWestminsterGong(): ShortArray {
        val durationSec = 2.2
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-1.7 * t)
            val wave = 0.45 * sin(2.0 * PI * 220.0 * t) +
                0.35 * sin(2.0 * PI * 440.0 * t) +
                0.20 * sin(2.0 * PI * 554.37 * t)
            buffer[i] = (wave * envelope * 29000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 2. Hyper Pulse Beep (Rapid 1000Hz staccato digital double-beep)
    private fun generateHyperPulseBeep(): ShortArray {
        val durationSec = 0.8
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val isBeep = (t in 0.0..0.12) || (t in 0.20..0.32)
            if (isBeep) {
                val wave = sin(2.0 * PI * 1000.0 * t)
                buffer[i] = (wave * 28000).toInt().coerceIn(-32768, 32767).toShort()
            } else {
                buffer[i] = 0
            }
        }
        return buffer
    }

    // 3. Arcade Victory Fanfare (Ascending arpeggio C5-E5-G5-C6)
    private fun generateArcadeVictoryFanfare(): ShortArray {
        val durationSec = 1.3
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = when {
                t < 0.20 -> 523.25 // C5
                t < 0.40 -> 659.25 // E5
                t < 0.60 -> 783.99 // G5
                else -> 1046.50    // C6
            }
            val localT = if (t >= 0.60) (t - 0.60) else (t % 0.20)
            val env = if (t >= 0.60) exp(-2.0 * localT) else (1.0 - localT * 2.0).coerceAtLeast(0.3)
            val wave = sin(2.0 * PI * freq * t)
            buffer[i] = (wave * env * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 4. Tibetan Singing Bowl (Smooth 432Hz sine wave with gentle 5Hz vibrato)
    private fun generateTibetanSingingBowl(): ShortArray {
        val durationSec = 2.4
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        var phase = 0.0
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val vibrato = 4.0 * sin(2.0 * PI * 5.0 * t)
            val instFreq = 432.0 + vibrato
            phase += 2.0 * PI * instFreq / SAMPLE_RATE
            val envelope = exp(-1.1 * t)
            val wave = 0.8 * sin(phase) + 0.2 * sin(phase * 2.0)
            buffer[i] = (wave * envelope * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 5. Soft Bamboo Marimba (Woody percussive pattern: 523Hz, 659Hz, 784Hz)
    private fun generateSoftBambooMarimba(): ShortArray {
        val durationSec = 1.2
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val (noteT, freq) = when {
                t < 0.35 -> t to 523.25
                t < 0.70 -> (t - 0.35) to 659.25
                else -> (t - 0.70) to 783.99
            }
            val env = exp(-11.0 * noteT)
            val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 2.75) * t)
            buffer[i] = (wave * env * 29000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 6. Nuclear Bunker Klaxon (Sweeping 600Hz-1400Hz urgent siren)
    private fun generateNuclearBunkerKlaxon(): ShortArray {
        val durationSec = 1.2
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        var phase = 0.0
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val sweep = 0.5 * (1.0 + sin(2.0 * PI * 1.6 * t))
            val freq = 600.0 + 800.0 * sweep
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val wave = sin(phase)
            buffer[i] = (wave * 29000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 7. Red Alert Strobe Horn (Industrial low-high brass horn)
    private fun generateRedAlertStrobeHorn(): ShortArray {
        val durationSec = 1.0
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val isHigh = (t in 0.0..0.42)
            val isLow = (t in 0.50..0.92)
            if (isHigh || isLow) {
                val freq = if (isHigh) 720.0 else 460.0
                val wave = sin(2.0 * PI * freq * t) + 0.35 * sin(2.0 * PI * (freq * 2.0) * t)
                buffer[i] = (wave * 27000).toInt().coerceIn(-32768, 32767).toShort()
            } else {
                buffer[i] = 0
            }
        }
        return buffer
    }

    // 8. Neon Synth Matrix (Futuristic FM synth pulse)
    private fun generateNeonSynthMatrix(): ShortArray {
        val durationSec = 1.2
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val mod = 2.8 * exp(-2.0 * t) * sin(2.0 * PI * 110.0 * t)
            val wave = sin(2.0 * PI * 330.0 * t + mod)
            val env = exp(-1.6 * t)
            buffer[i] = (wave * env * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 9. Quantum Radar Ping (Sonar sweep 1350Hz -> 750Hz with exponential decay & echo)
    private fun generateQuantumRadarPing(): ShortArray {
        val durationSec = 1.4
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq1 = 1350.0 - 550.0 * (t / 1.4)
            phase1 += 2.0 * PI * freq1 / SAMPLE_RATE
            val ping1 = sin(phase1) * exp(-3.2 * t)

            val echoT = (t - 0.45).coerceAtLeast(0.0)
            val freq2 = 1100.0 - 450.0 * (echoT / 1.0)
            phase2 += 2.0 * PI * freq2 / SAMPLE_RATE
            val ping2 = if (t >= 0.45) sin(phase2) * exp(-4.0 * echoT) * 0.45 else 0.0

            val sample = ping1 + ping2
            buffer[i] = (sample * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // 40ms 880Hz crisp tick beep for final 10-second countdown
    private fun generateTickBeepPattern(): ShortArray {
        val durationSec = 0.04
        val total = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            // 5ms attack/decay smoothing to eliminate clicks
            val attack = (t / 0.005).coerceIn(0.0, 1.0)
            val decay = ((0.04 - t) / 0.005).coerceIn(0.0, 1.0)
            val env = attack * decay
            val wave = sin(2.0 * PI * 880.0 * t)
            buffer[i] = (wave * env * 25000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    // =========================================================================
    // TIME FORMATTERS
    // =========================================================================

    fun formatTime(totalMs: Long): String {
        val totalSeconds = (totalMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatTimeWithDeciseconds(totalMs: Long): String {
        val totalSeconds = (totalMs / 1000L).coerceAtLeast(0L)
        val deci = ((totalMs % 1000L) / 100L).coerceIn(0L, 9L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d.%d", hours, minutes, seconds, deci)
        } else {
            String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, deci)
        }
    }

    fun formatOvertime(overtimeMs: Long): String {
        val totalSeconds = (overtimeMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
