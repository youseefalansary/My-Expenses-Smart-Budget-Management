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
    val workTotal: Double = 0.0,

    @ColumnInfo(name = "last_closed_date", defaultValue = "''")
    val lastClosedDate: String = "",

    @ColumnInfo(name = "transport_reserve", defaultValue = "0.0")
    val transportReserve: Double = 0.0,

    @ColumnInfo(name = "savings_type", defaultValue = "'percentage'")
    val savingsType: String = "percentage", // "percentage" or "fixed"

    @ColumnInfo(name = "savings_value", defaultValue = "20.0")
    val savingsValue: Double = 20.0,

    @ColumnInfo(name = "total_received", defaultValue = "2000.0")
    val totalReceived: Double = 2000.0,

    @ColumnInfo(name = "daily_reminder_enabled", defaultValue = "0")
    val dailyReminderEnabled: Int = 0,

    @ColumnInfo(name = "threshold_alert_enabled", defaultValue = "0")
    val thresholdAlertEnabled: Int = 0,

    @ColumnInfo(name = "category_alert_enabled", defaultValue = "0")
    val categoryAlertEnabled: Int = 0,

    @ColumnInfo(name = "day_start_pocket_cash", defaultValue = "0.0")
    val dayStartPocketCash: Double = pocketCash
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val amount: Double,

    val category: String, // طعام ومشروبات، مواصلات، علاج، شخصي، أو مخصص

    @ColumnInfo(name = "date_tag")
    val dateTag: String, // اليوم أو أمس

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "deducted_from_pocket", defaultValue = "0.0")
    val deductedFromPocket: Double = 0.0,

    @ColumnInfo(name = "deducted_from_splurge", defaultValue = "0.0")
    val deductedFromSplurge: Double = 0.0,

    @ColumnInfo(name = "deducted_from_vault", defaultValue = "0.0")
    val deductedFromVault: Double = 0.0,

    @ColumnInfo(name = "deducted_from_transport", defaultValue = "0.0")
    val deductedFromTransport: Double = 0.0,

    @ColumnInfo(name = "date_millis", defaultValue = "0")
    val dateMillis: Long = 0L,

    @ColumnInfo(name = "date_iso", defaultValue = "''")
    val dateIso: String = ""
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

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val emoji: String,

    @ColumnInfo(name = "budget_limit", defaultValue = "0.0")
    val budgetLimit: Double = 0.0, // 0.0 = بدون حد أقصى

    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Int = 0 // 1 = تصنيف أساسي، 0 = تصنيف مخصص من المستخدم
)
