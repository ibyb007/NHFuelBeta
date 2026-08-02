package com.nh.fuel.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.flow.Flow

@IgnoreExtraProperties
@Entity(tableName = "expense_records")
data class ExpenseItem(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "", // Format: YYYY-MM-DD
    val timestamp: String = "" // Entry timestamp
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_records ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_records WHERE date = :date ORDER BY id DESC")
    fun getExpensesByDate(date: String): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(expense: ExpenseItem)

    @Delete
    suspend fun deleteExpense(expense: ExpenseItem)
}
