package com.irondiscipline.app.alarm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

data class MathProblem(val text: String, val answer: Int)

/**
 * Singleton state holder preserving active alarm Math Hard questions and solved count
 * across screen OFF/ON cycles, device orientation, and activity recreations.
 */
object ActiveAlarmSessionState {

    const val TOTAL_MATH_QUESTIONS = 5

    @Volatile
    var activeAlarmId: String? = null
        private set

    var solvedCount by mutableIntStateOf(0)
    var currentProblem by mutableStateOf<MathProblem?>(null)
    var problemsList: List<MathProblem> = emptyList()

    @Synchronized
    fun initSession(alarmId: String?) {
        if (activeAlarmId == alarmId && problemsList.isNotEmpty() && currentProblem != null) {
            // Existing session active, preserve solved questions and current equation
            return
        }

        activeAlarmId = alarmId
        solvedCount = 0
        problemsList = List(TOTAL_MATH_QUESTIONS) {
            generateHardMathProblem()
        }
        currentProblem = problemsList.firstOrNull()
    }

    /**
     * Increments solved count and advances to the next question.
     * Returns true when all 5 questions have been completed.
     */
    @Synchronized
    fun onQuestionSolved(): Boolean {
        val nextSolved = solvedCount + 1
        solvedCount = nextSolved
        if (nextSolved >= TOTAL_MATH_QUESTIONS) {
            return true
        }
        currentProblem = problemsList.getOrNull(nextSolved) ?: generateHardMathProblem()
        return false
    }

    @Synchronized
    fun clearSession() {
        activeAlarmId = null
        solvedCount = 0
        currentProblem = null
        problemsList = emptyList()
    }

    /**
     * Generates math problems using ONLY Addition (+) and Subtraction (-) operations
     * with strictly positive integer answers (> 0).
     * Randomly alternates between single-step and two-step + / - patterns.
     */
    fun generateHardMathProblem(): MathProblem {
        val pattern = Random.nextInt(0, 4)
        return when (pattern) {
            0 -> {
                // Single-step Addition (+): a in 15..75, b in 12..65
                val a = (15..75).random()
                val b = (12..65).random()
                MathProblem("$a + $b", a + b)
            }
            1 -> {
                // Single-step Subtraction (-): a in 35..95, b in 11..(a - 5) ensuring positive answer (> 0)
                val a = (35..95).random()
                val maxB = (a - 5).coerceAtLeast(11)
                val b = (11..maxB).random()
                MathProblem("$a - $b", a - b)
            }
            2 -> {
                // Two-step Addition and Subtraction (e.g., a + b - c)
                val a = (20..65).random()
                val b = (15..55).random()
                val sum = a + b
                val maxC = (sum - 10).coerceAtLeast(10)
                val c = (10..maxC).random()
                MathProblem("$a + $b - $c", sum - c)
            }
            else -> {
                // Two-step Subtraction and Addition (e.g., a - b + c)
                val a = (40..90).random()
                val maxB = (a - 10).coerceAtLeast(10)
                val b = (10..maxB).random()
                val c = (12..50).random()
                MathProblem("$a - $b + $c", (a - b) + c)
            }
        }
    }

    /**
     * Alias helper for generating Plus / Minus math questions.
     */
    fun generatePlusMinusMathQuestion(): MathProblem = generateHardMathProblem()
}
