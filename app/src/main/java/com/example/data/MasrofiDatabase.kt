package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Upgrade app_state
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `last_closed_date` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `transport_reserve` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `savings_type` TEXT NOT NULL DEFAULT 'percentage'")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `savings_value` REAL NOT NULL DEFAULT 20.0")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `total_received` REAL NOT NULL DEFAULT 2000.0")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `daily_reminder_enabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `threshold_alert_enabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `category_alert_enabled` INTEGER NOT NULL DEFAULT 0")

        // 2. Upgrade transactions with source deduction tracking
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `deducted_from_pocket` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `deducted_from_splurge` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `deducted_from_vault` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `deducted_from_transport` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `date_millis` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `date_iso` TEXT NOT NULL DEFAULT ''")

        // 3. Create categories table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `emoji` TEXT NOT NULL,
                `budget_limit` REAL NOT NULL DEFAULT 0.0,
                `is_default` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // Populate default categories
        db.execSQL("INSERT OR IGNORE INTO `categories` (`name`, `emoji`, `budget_limit`, `is_default`) VALUES ('طعام ومشروبات', '🍔', 0.0, 1)")
        db.execSQL("INSERT OR IGNORE INTO `categories` (`name`, `emoji`, `budget_limit`, `is_default`) VALUES ('مواصلات', '🚌', 0.0, 1)")
        db.execSQL("INSERT OR IGNORE INTO `categories` (`name`, `emoji`, `budget_limit`, `is_default`) VALUES ('علاج', '💊', 0.0, 1)")
        db.execSQL("INSERT OR IGNORE INTO `categories` (`name`, `emoji`, `budget_limit`, `is_default`) VALUES ('شخصي', '🛍️', 0.0, 1)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `day_start_pocket` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("UPDATE `app_state` SET `day_start_pocket` = `pocket_cash` + `today_spent` WHERE `day_start_pocket` = 0.0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `currency_code` TEXT NOT NULL DEFAULT 'EGP'")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `language_code` TEXT NOT NULL DEFAULT 'ar'")
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `cycle_end_date` TEXT NOT NULL DEFAULT ''")

        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `day_name` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `exact_time` TEXT NOT NULL DEFAULT ''")

        db.execSQL("ALTER TABLE `work_history` ADD COLUMN `day_name` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `work_history` ADD COLUMN `exact_time` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `work_history` ADD COLUMN `date_millis` INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE `debts` ADD COLUMN `date_str` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `debts` ADD COLUMN `date_millis` INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        AppStateEntity::class,
        TransactionEntity::class,
        WorkHistoryEntity::class,
        DebtEntity::class,
        CategoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MasrofiDatabase : RoomDatabase() {
    abstract fun appStateDao(): AppStateDao
    abstract fun transactionDao(): TransactionDao
    abstract fun workHistoryDao(): WorkHistoryDao
    abstract fun debtDao(): DebtDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MasrofiDatabase? = null

        fun getInstance(context: Context): MasrofiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasrofiDatabase::class.java,
                    "masrofi_db.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
