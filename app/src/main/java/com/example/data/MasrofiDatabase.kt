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
        db.execSQL("ALTER TABLE `app_state` ADD COLUMN `day_start_pocket_cash` REAL NOT NULL DEFAULT 0.0")
        // Recover the opening-of-day budget for users upgrading from v1.1.
        // Tracked pocket deductions are added back; legacy records fall back to today_spent.
        db.execSQL(
            """
            UPDATE `app_state`
            SET `day_start_pocket_cash` = `pocket_cash` + COALESCE(
                (SELECT SUM(`deducted_from_pocket`) FROM `transactions`
                 WHERE `date_iso` = `app_state`.`last_processed_date`),
                `today_spent`
            )
            """.trimIndent()
        )
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
    version = 3,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
