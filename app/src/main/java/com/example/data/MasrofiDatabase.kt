package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppStateEntity::class,
        TransactionEntity::class,
        WorkHistoryEntity::class,
        DebtEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MasrofiDatabase : RoomDatabase() {
    abstract fun appStateDao(): AppStateDao
    abstract fun transactionDao(): TransactionDao
    abstract fun workHistoryDao(): WorkHistoryDao
    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var INSTANCE: MasrofiDatabase? = null

        fun getInstance(context: Context): MasrofiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasrofiDatabase::class.java,
                    "masrofi_db.db" // Matching exact database name requested
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
