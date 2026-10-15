package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    fun getAppState(): Flow<AppStateEntity?>

    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    suspend fun getAppStateSync(): AppStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: AppStateEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 15): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface WorkHistoryDao {
    @Query("SELECT * FROM work_history ORDER BY id DESC")
    fun getAll(): Flow<List<WorkHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: WorkHistoryEntity): Long

    @Query("DELETE FROM work_history WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY is_settled ASC, id DESC")
    fun getAll(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity): Long

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DebtEntity?

    @Query("UPDATE debts SET is_settled = :settled WHERE id = :id")
    suspend fun updateSettled(id: Int, settled: Int)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteById(id: Int)
}
