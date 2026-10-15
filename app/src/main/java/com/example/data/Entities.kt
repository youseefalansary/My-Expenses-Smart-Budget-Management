package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "pocket_cash")
    val pocketCash: Double = 1500.0,

    @ColumnInfo(name = "vault")
    val vault: Double = 500.0,

    @ColumnInfo(name = "splurge_pool")
    val splurgePool: Double = 0.0,

    @ColumnInfo(name = "days_left")
    val daysLeft: Int = 30,

    @ColumnInfo(name = "today_spent")
    val todaySpent: Double = 0.0,

    @ColumnInfo(name = "is_institute_day")
    val isInstituteDay: Int = 0,

    @ColumnInfo(name = "last_processed_date")
    val lastProcessedDate: String = "",

    @ColumnInfo(name = "work_total")
    val workTotal: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val amount: Double,

    val category: String, // طعام ومشروبات، مواصلات، علاج، شخصي

    @ColumnInfo(name = "date_tag")
    val dateTag: String, // اليوم أو أمس

    @ColumnInfo(name = "created_at")
    val createdAt: String
)

@Entity(tableName = "work_history")
data class WorkHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val source: String,

    val amount: Double,

    @ColumnInfo(name = "date_str")
    val dateStr: String
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val person: String,

    val amount: Double,

    val type: String, // 'give' = سلّفت وخرجت من جيبي / 'take' = استلفت ودخلت جيبي

    @ColumnInfo(name = "is_settled")
    val isSettled: Int = 0 // 0 أو 1
)
