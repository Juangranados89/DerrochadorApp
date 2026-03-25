package com.derrochador.data.dao

import androidx.room.*
import com.derrochador.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isProcessed = 0 ORDER BY timestamp ASC")
    suspend fun getUnprocessed(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND isDuplicate = 0
        GROUP BY category
    """)
    suspend fun getDailyTotal(startOfDay: Long, endOfDay: Long): List<CategoryTotal>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
