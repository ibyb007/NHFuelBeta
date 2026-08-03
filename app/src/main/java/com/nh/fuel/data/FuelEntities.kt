package com.nh.fuel.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlin.math.max

@IgnoreExtraProperties
data class RefillEvent(
    val amount: Double = 0.0,
    val timestamp: String = ""
)

@IgnoreExtraProperties
data class TestingEvent(
    val petrolTestingAmount: Double = 0.0,
    val dieselTestingAmount: Double = 0.0,
    val timestamp: String = ""
)

@IgnoreExtraProperties
data class NozzleShift(
    val open: Double = 0.0,
    val close: Double = 0.0,
    val testing: Double = 0.0,
    val isReset: Boolean = false,              // <--- Red Reset Badge Flag
    val originalOpenBeforeReset: Double = 0.0  // <--- Restores original open reading on Undo
) {
    val isValid: Boolean get() = close >= open || close == 0.0
    val grossSale: Double get() = if (close >= open && close > 0.0) close - open else 0.0
    val sale: Double get() = max(0.0, grossSale - testing)
    val isClosed: Boolean get() = close > 0.0 && close >= open
}

@IgnoreExtraProperties
data class DispenserShift(
    val petrolN2: NozzleShift = NozzleShift(),
    val petrolN3: NozzleShift = NozzleShift(),
    val dieselN1: NozzleShift = NozzleShift(),
    val dieselN4: NozzleShift = NozzleShift(),
    val cashCollected: Double = 0.0,
    val digitalCollected: Double = 0.0,
    val creditCollected: Double = 0.0,
    val lastTestingEvent: TestingEvent = TestingEvent()
) {
    val petrolSale: Double get() = petrolN2.sale + petrolN3.sale
    val dieselSale: Double get() = dieselN1.sale + dieselN4.sale
    val isShiftComplete: Boolean
        get() = petrolN2.isClosed && petrolN3.isClosed && dieselN1.isClosed && dieselN4.isClosed
    val totalCollected: Double get() = cashCollected + digitalCollected + creditCollected

    fun getRevenue(petrolPrice: Double, dieselPrice: Double): Double {
        return (petrolSale * petrolPrice) + (dieselSale * dieselPrice)
    }

    fun getMismatch(petrolPrice: Double, dieselPrice: Double): Double {
        return totalCollected - getRevenue(petrolPrice, dieselPrice)
    }
}

@IgnoreExtraProperties
data class DayShift(
    val shiftNumber: Int = 1,
    val mpd1: DispenserShift = DispenserShift(),
    val mpd2: DispenserShift = DispenserShift()
) {
    val petrolSale: Double get() = mpd1.petrolSale + mpd2.petrolSale
    val dieselSale: Double get() = mpd1.dieselSale + mpd2.dieselSale
    val totalPetrolTesting: Double get() = mpd1.petrolN2.testing + mpd1.petrolN3.testing + mpd2.petrolN2.testing + mpd2.petrolN3.testing
    val totalDieselTesting: Double get() = mpd1.dieselN1.testing + mpd1.dieselN4.testing + mpd2.dieselN1.testing + mpd2.dieselN4.testing
    val isComplete: Boolean get() = mpd1.isShiftComplete && mpd2.isShiftComplete
    val totalCashCollected: Double get() = mpd1.cashCollected + mpd2.cashCollected
    val totalDigitalCollected: Double get() = mpd1.digitalCollected + mpd2.digitalCollected
    val totalCreditCollected: Double get() = mpd1.creditCollected + mpd2.creditCollected
    val totalCollected: Double get() = totalCashCollected + totalDigitalCollected + totalCreditCollected

    fun getRevenue(petrolPrice: Double, dieselPrice: Double): Double {
        return mpd1.getRevenue(petrolPrice, dieselPrice) + mpd2.getRevenue(petrolPrice, dieselPrice)
    }

    fun getMismatch(petrolPrice: Double, dieselPrice: Double): Double {
        return totalCollected - getRevenue(petrolPrice, dieselPrice)
    }
}
