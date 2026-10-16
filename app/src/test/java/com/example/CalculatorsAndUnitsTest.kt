package com.example

import com.example.core.Calculators
import com.example.core.TimeUtils
import com.example.core.UnitConverter
import com.example.data.local.entity.LengthUnit
import com.example.data.local.entity.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculatorsAndUnitsTest {

    @Test
    fun testKgToLbsConversion() {
        val kg = 100.0
        val lbs = UnitConverter.kgToLbs(kg)
        assertEquals(220.46, lbs, 0.01)

        val convertedBack = UnitConverter.lbsToKg(lbs)
        assertEquals(kg, convertedBack, 0.0001)
    }

    @Test
    fun testCmToInchesConversion() {
        val cm = 100.0
        val inches = UnitConverter.cmToInches(cm)
        assertEquals(39.37, inches, 0.01)

        val convertedBack = UnitConverter.inchesToCm(inches)
        assertEquals(cm, convertedBack, 0.0001)
    }

    @Test
    fun testWeightDisplayAndStorageConversion() {
        val storedKg = 80.0
        val displayedLbs = UnitConverter.displayWeight(storedKg, WeightUnit.LBS)!!
        assertEquals(176.36, displayedLbs, 0.1)

        val backToKg = UnitConverter.toStoredWeightKg(displayedLbs, WeightUnit.LBS)
        assertEquals(storedKg, backToKg, 0.01)
    }

    @Test
    fun testEpley1RMFormula() {
        // 100kg for 10 reps: 100 * (1 + 10/30) = 133.33 kg
        val oneRM = Calculators.estimate1RMEpley(100.0, 10)
        assertEquals(133.33, oneRM, 0.01)

        // 1 rep should equal exact weight lifted
        val singleRep = Calculators.estimate1RMEpley(140.0, 1)
        assertEquals(140.0, singleRep, 0.001)

        // Zero or negative returns 0
        assertEquals(0.0, Calculators.estimate1RMEpley(-10.0, 5), 0.001)
        assertEquals(0.0, Calculators.estimate1RMEpley(100.0, 0), 0.001)
    }

    @Test
    fun testBrzycki1RMFormula() {
        // 100kg for 10 reps: 100 * (36 / (37 - 10)) = 100 * (36 / 27) = 133.33 kg
        val oneRM = Calculators.estimate1RMBrzycki(100.0, 10)
        assertEquals(133.33, oneRM, 0.01)
    }

    @Test
    fun testVolumeCalculation() {
        val sets = listOf(
            Pair(100.0, 10), // 1000
            Pair(80.0, 8),   // 640
            Pair(0.0, 10),   // 0
            Pair(50.0, null) // 0
        )
        val totalVolume = Calculators.calculateVolume(sets)
        assertEquals(1640.0, totalVolume, 0.001)
    }

    @Test
    fun testDayStreakCalculation() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgo = calendar.timeInMillis

        val timestamps = listOf(today, yesterday, twoDaysAgo)
        val streak = Calculators.calculateDayStreak(timestamps, today)
        assertEquals(3, streak)

        // Empty list returns 0
        assertEquals(0, Calculators.calculateDayStreak(emptyList(), today))
    }

    @Test
    fun testDurationFormatting() {
        assertEquals("00:45", TimeUtils.formatDuration(45))
        assertEquals("05:30", TimeUtils.formatDuration(330))
        assertEquals("01:15:00", TimeUtils.formatDuration(4500))

        assertEquals("45s", TimeUtils.formatDurationReadable(45))
        assertEquals("5m", TimeUtils.formatDurationReadable(300))
        assertEquals("1h 15m", TimeUtils.formatDurationReadable(4500))
    }
}
