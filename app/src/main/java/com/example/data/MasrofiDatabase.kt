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

private fun hasColumn(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
    db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) return true
        }
    }
    return false
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val hasExpectedColumn = hasColumn(db, "app_state", "day_start_pocket")
        val hasV12Column = hasColumn(db, "app_state", "day_start_pocket_cash")

        if (hasExpectedColumn && !hasV12Column) return

        if (!hasExpectedColumn && !hasV12Column) {
            db.execSQL("ALTER TABLE `app_state` ADD COLUMN `day_start_pocket` REAL NOT NULL DEFAULT 0.0")
            db.execSQL("UPDATE `app_state` SET `day_start_pocket` = `pocket_cash` + `today_spent`")
            return
        }

        val dayStartValue = when {
            hasExpectedColumn && hasV12Column ->
                "CASE WHEN `day_start_pocket` != 0.0 THEN `day_start_pocket` ELSE `day_start_pocket_cash` END"
            hasV12Column -> "`day_start_pocket_cash`"
            else -> "`day_start_pocket`"
        }

        // Rebuild app_state to remove the obsolete v1.2 column while preserving every value.
        db.execSQL("DROP TABLE IF EXISTS `app_state_v5_new`")
        db.execSQL(
            """
            CREATE TABLE `app_state_v5_new` (
                `id` INTEGER NOT NULL,
                `pocket_cash` REAL NOT NULL,
                `vault` REAL NOT NULL,
                `splurge_pool` REAL NOT NULL,
                `days_left` INTEGER NOT NULL,
                `today_spent` REAL NOT NULL,
                `is_institute_day` INTEGER NOT NULL,
                `last_processed_date` TEXT NOT NULL,
                `work_total` REAL NOT NULL,
                `last_closed_date` TEXT NOT NULL DEFAULT '',
                `transport_reserve` REAL NOT NULL DEFAULT 0.0,
                `savings_type` TEXT NOT NULL DEFAULT 'percentage',
                `savings_value` REAL NOT NULL DEFAULT 20.0,
                `total_received` REAL NOT NULL DEFAULT 2000.0,
                `daily_reminder_enabled` INTEGER NOT NULL DEFAULT 0,
                `threshold_alert_enabled` INTEGER NOT NULL DEFAULT 0,
                `category_alert_enabled` INTEGER NOT NULL DEFAULT 0,
                `day_start_pocket` REAL NOT NULL DEFAULT 0.0,
                `currency_code` TEXT NOT NULL DEFAULT 'EGP',
                `language_code` TEXT NOT NULL DEFAULT 'ar',
                `cycle_end_date` TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `app_state_v5_new` (
                `id`, `pocket_cash`, `vault`, `splurge_pool`, `days_left`, `today_spent`,
                `is_institute_day`, `last_processed_date`, `work_total`, `last_closed_date`,
                `transport_reserve`, `savings_type`, `savings_value`, `total_received`,
                `daily_reminder_enabled`, `threshold_alert_enabled`, `category_alert_enabled`,
                `day_start_pocket`, `currency_code`, `language_code`, `cycle_end_date`
            )
            SELECT
                `id`, `pocket_cash`, `vault`, `splurge_pool`, `days_left`, `today_spent`,
                `is_institute_day`, `last_processed_date`, `work_total`, `last_closed_date`,
                `transport_reserve`, `savings_type`, `savings_value`, `total_received`,
                `daily_reminder_enabled`, `threshold_alert_enabled`, `category_alert_enabled`,
                $dayStartValue, `currency_code`, `language_code`, `cycle_end_date`
            FROM `app_state`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `app_state`")
        db.execSQL("ALTER TABLE `app_state_v5_new` RENAME TO `app_state`")
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
    version = 5,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
