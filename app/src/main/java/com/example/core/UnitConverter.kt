package com.example.core

import com.example.data.local.entity.LengthUnit
import com.example.data.local.entity.WeightUnit
import java.util.Locale

object UnitConverter {
    private const val KG_TO_LBS = 2.20462262185
    private const val CM_TO_INCH = 0.3937007874

    fun kgToLbs(kg: Double): Double = kg * KG_TO_LBS
    fun lbsToKg(lbs: Double): Double = lbs / KG_TO_LBS

    fun cmToInches(cm: Double): Double = cm * CM_TO_INCH
    fun inchesToCm(inches: Double): Double = inches / CM_TO_INCH

    fun displayWeight(weightKg: Double?, unit: WeightUnit): Double? {
        if (weightKg == null) return null
        return when (unit) {
            WeightUnit.KG -> weightKg
            WeightUnit.LBS -> kgToLbs(weightKg)
        }
    }

    fun toStoredWeightKg(displayWeight: Double, unit: WeightUnit): Double {
        return when (unit) {
            WeightUnit.KG -> displayWeight
            WeightUnit.LBS -> lbsToKg(displayWeight)
        }
    }

    fun displayLength(lengthCm: Double?, unit: LengthUnit): Double? {
        if (lengthCm == null) return null
        return when (unit) {
            LengthUnit.CM -> lengthCm
            LengthUnit.INCH -> cmToInches(lengthCm)
        }
    }

    fun toStoredLengthCm(displayLength: Double, unit: LengthUnit): Double {
        return when (unit) {
            LengthUnit.CM -> displayLength
            LengthUnit.INCH -> inchesToCm(displayLength)
        }
    }

    fun formatWeight(weightKg: Double?, unit: WeightUnit): String {
        if (weightKg == null || weightKg <= 0.0) return "-"
        val displayed = displayWeight(weightKg, unit) ?: return "-"
        return if (displayed % 1.0 == 0.0) {
            "${displayed.toLong()} ${unit.symbol}"
        } else {
            String.format(Locale.US, "%.1f %s", displayed, unit.symbol)
        }
    }

    fun formatLength(lengthCm: Double?, unit: LengthUnit): String {
        if (lengthCm == null || lengthCm <= 0.0) return "-"
        val displayed = displayLength(lengthCm, unit) ?: return "-"
        return String.format(Locale.US, "%.1f %s", displayed, unit.symbol)
    }

    fun formatDistance(meters: Double?, useImperial: Boolean): String {
        if (meters == null || meters <= 0.0) return "-"
        return if (useImperial) {
            val miles = meters / 1609.344
            String.format(Locale.US, "%.2f mi", miles)
        } else {
            val km = meters / 1000.0
            String.format(Locale.US, "%.2f km", km)
        }
    }
}
