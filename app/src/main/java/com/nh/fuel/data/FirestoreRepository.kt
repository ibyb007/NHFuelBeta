package com.nh.fuel.data

import android.util.Log
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
                    Log.e("FirestoreRepository", "Error observing fuel records: ${error.localizedMessage}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val records = snapshot.documents.mapNotNull { doc ->
                        runCatching { doc.toObject(DailyFuelRecord::class.java) }.getOrNull()
                    }
                    trySend(records)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveFuelRecord(record: DailyFuelRecord) {
        if (record.date.isBlank()) return
        db.collection("daily_fuel_records")
            .document(record.date)
            .set(record)
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Failed to save record: ${e.localizedMessage}")
            }
    }

    // 2. Expenses
    fun observeAllExpenses(): Flow<List<ExpenseItem>> = callbackFlow {
        val listener = db.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Error observing expenses: ${error.localizedMessage}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { doc ->
                        runCatching { doc.toObject(ExpenseItem::class.java) }.getOrNull()
                    }
                    trySend(expenses)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveExpense(expense: ExpenseItem) {
        db.collection("expenses")
            .document(expense.id.toString())
            .set(expense)
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Failed to save expense: ${e.localizedMessage}")
            }
    }

    suspend fun deleteExpense(expense: ExpenseItem) {
        db.collection("expenses")
            .document(expense.id.toString())
            .delete()
    }

    // 3. Credits
    fun observeAllCredits(): Flow<List<CreditRecord>> = callbackFlow {
        val listener = db.collection("credits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Error observing credits: ${error.localizedMessage}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val credits = snapshot.documents.mapNotNull { doc ->
                        runCatching { doc.toObject(CreditRecord::class.java) }.getOrNull()
                    }
                    trySend(credits)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCredit(credit: CreditRecord) {
        db.collection("credits")
            .document(credit.id.toString())
            .set(credit)
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Failed to save credit: ${e.localizedMessage}")
            }
    }

    suspend fun deleteCredit(credit: CreditRecord) {
        db.collection("credits")
            .document(credit.id.toString())
            .delete()
    }
}
