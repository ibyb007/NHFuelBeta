package com.nh.fuel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Daily Fuel Records
    fun observeAllFuelRecords(): Flow<List<DailyFuelRecord>> = callbackFlow {
        val listener = db.collection("daily_fuel_records")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val records = snapshot.documents.mapNotNull { it.toObject(DailyFuelRecord::class.java) }
                    trySend(records)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveFuelRecord(record: DailyFuelRecord) {
        db.collection("daily_fuel_records")
            .document(record.date)
            .set(record)
    }

    // 2. Expenses
    fun observeAllExpenses(): Flow<List<ExpenseItem>> = callbackFlow {
        val listener = db.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { it.toObject(ExpenseItem::class.java) }
                    trySend(expenses)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveExpense(expense: ExpenseItem) {
        db.collection("expenses")
            .document(expense.id.toString()) // FIXED: converted Long to String
            .set(expense)
    }

    suspend fun deleteExpense(expense: ExpenseItem) {
        db.collection("expenses")
            .document(expense.id.toString()) // FIXED: converted Long to String
            .delete()
    }

    // 3. Credits
    fun observeAllCredits(): Flow<List<CreditRecord>> = callbackFlow {
        val listener = db.collection("credits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val credits = snapshot.documents.mapNotNull { it.toObject(CreditRecord::class.java) }
                    trySend(credits)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCredit(credit: CreditRecord) {
        db.collection("credits")
            .document(credit.id.toString()) // FIXED: converted Long to String
            .set(credit)
    }

    suspend fun deleteCredit(credit: CreditRecord) {
        db.collection("credits")
            .document(credit.id.toString()) // FIXED: converted Long to String
            .delete()
    }
}
