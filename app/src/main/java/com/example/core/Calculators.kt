package com.example.core

import java.util.Calendar
import java.util.concurrent.TimeUnit

object Calculators {

    /**
     * Estimates One-Repetition Maximum (1RM) using Epley formula:
     * 1RM = Weight * (1 + Reps / 30)
     * For 1 rep, 1RM = Weight.
     */
    fun estimate1RMEpley(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return weightKg
        return weightKg * (1.0 + reps / 30.0)
    }

    /**
     * Estimates One-Repetition Maximum (1RM) using Brzycki formula:
     * 1RM = Weight * (36 / (37 - Reps))
     */
    fun estimate1RMBrzycki(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        if (reps >= 37) return weightKg
        if (reps == 1) return weightKg
        return weightKg * (36.0 / (37.0 - reps))
    }

    /**
     * Calculates total volume (kg) from completed sets.
     * Volume = sum of (weightKg * reps).
     */
    fun calculateVolume(sets: List<Pair<Double?, Int?>>): Double {
        return sets.sumOf { (weight, reps) ->
            if (weight != null && weight > 0.0 && reps != null && reps > 0) {
                weight * reps
            } else {
                0.0
            }
        }
    }

    /**
     * Calculates streak in consecutive active days.
     * A day is counted if there is at least one workout recorded on that local calendar day.
     * If today has no workout yet, checks if yesterday had a workout to maintain the streak.
     */
    fun calculateDayStreak(workoutTimestamps: List<Long>, nowMillis: Long = System.currentTimeMillis()): Int {
        if (workoutTimestamps.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val daysWithWorkouts = workoutTimestamps.map { ts ->
            calendar.timeInMillis = ts
            calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
        }.toSet()

        calendar.timeInMillis = nowMillis
        val todayKey = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)

        var currentCheckKey = if (daysWithWorkouts.contains(todayKey)) todayKey else if (daysWithWorkouts.contains(yesterdayKey)) yesterdayKey else return 0

        var streak = 0
        calendar.timeInMillis = nowMillis
        if (!daysWithWorkouts.contains(todayKey)) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
            if (daysWithWorkouts.contains(key)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Count workouts in the current week (from Monday 00:00:00 to now).
     */
    fun countWorkoutsThisWeek(workoutTimestamps: List<Long>, nowMillis: Long = System.currentTimeMillis()): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = calendar.timeInMillis
        return workoutTimestamps.count { it >= startOfWeek }
    }
}
