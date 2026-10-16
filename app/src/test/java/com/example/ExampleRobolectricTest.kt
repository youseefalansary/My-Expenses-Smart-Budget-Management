package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppStateEntity
import com.example.data.MasrofiDatabase
import com.example.data.MasrofiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: MasrofiDatabase
    private lateinit var repository: MasrofiRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MasrofiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MasrofiRepository(
            appStateDao = db.appStateDao(),
            transactionDao = db.transactionDao(),
            workHistoryDao = db.workHistoryDao(),
            debtDao = db.debtDao(),
            categoryDao = db.categoryDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read app name from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("مصروفي", appName)
    }

    @Test
    fun `test rollback expense from single source`() = runBlocking {
        // Initial setup: pocket = 1600.0, vault = 400.0, splurge = 0.0, days = 30 -> allowance = 53.33
        repository.resetCycle(totalReceived = 2000.0, days = 30, savingsType = "fixed", savingsValue = 400.0)
        val initial = repository.getOrCreateAppState()
        assertEquals(1600.0, initial.pocketCash, 0.001)
        assertEquals(400.0, initial.vault, 0.001)
        assertEquals(0.0, initial.todaySpent, 0.001)

        // Expense of 30.0 within daily allowance -> single source (pocket only)
        val res = repository.recordExpense(
            title = "غداء",
            amount = 30.0,
            category = "طعام ومشروبات",
            isYesterday = false
        )
        assertTrue(res.success)
        assertFalse(res.vaultWarned)

        val txList = repository.allTransactions.first()
        assertEquals(1, txList.size)
        val tx = txList[0]
        assertEquals(30.0, tx.deductedFromPocket, 0.001)
        assertEquals(0.0, tx.deductedFromSplurge, 0.001)
        assertEquals(0.0, tx.deductedFromVault, 0.001)

        val afterRecord = repository.getOrCreateAppState()
        assertEquals(1570.0, afterRecord.pocketCash, 0.001)
        assertEquals(30.0, afterRecord.todaySpent, 0.001)

        // Rollback the expense
        repository.rollbackExpense(tx.id)

        // Verify that pocketCash and todaySpent are accurately restored
        val afterRollback = repository.getOrCreateAppState()
        assertEquals(1600.0, afterRollback.pocketCash, 0.001)
        assertEquals(400.0, afterRollback.vault, 0.001)
        assertEquals(0.0, afterRollback.splurgePool, 0.001)
        assertEquals(0.0, afterRollback.todaySpent, 0.001)
        assertTrue(repository.allTransactions.first().isEmpty())
    }

    @Test
    fun `test rollback expense from multiple sources`() = runBlocking {
        // Setup state where expense exceeds allowance and cascades:
        // Pocket = 100.0, Days = 10 (allowance = 10.0), Splurge = 50.0, Vault = 200.0
        val dbState = AppStateEntity(
            id = 1,
            pocketCash = 100.0,
            vault = 200.0,
            splurgePool = 50.0,
            daysLeft = 10,
            todaySpent = 0.0,
            isInstituteDay = 0,
            lastProcessedDate = repository.getTodayDateStr()
        )
        db.appStateDao().insertOrUpdate(dbState)

        // Record expense of 180.0
        // Daily allowance portion = 10.0 (from pocket)
        // Deficit = 170.0:
        // - from splurge: 50.0 (splurge exhausted)
        // - remaining deficit = 120.0
        // - from remaining pocket: 90.0 (pocket exhausted)
        // - remaining deficit = 30.0
        // - from vault: 30.0
        // Total deducted: pocket = 100.0, splurge = 50.0, vault = 30.0 (3 sources)
        val res = repository.recordExpense(
            title = "شراء استثنائي",
            amount = 180.0,
            category = "شخصي",
            isYesterday = false
        )
        assertTrue(res.success)
        assertTrue(res.vaultWarned)

        val tx = repository.allTransactions.first()[0]
        assertEquals(100.0, tx.deductedFromPocket, 0.001)
        assertEquals(50.0, tx.deductedFromSplurge, 0.001)
        assertEquals(30.0, tx.deductedFromVault, 0.001)

        val stateAfter = repository.getOrCreateAppState()
        assertEquals(0.0, stateAfter.pocketCash, 0.001)
        assertEquals(0.0, stateAfter.splurgePool, 0.001)
        assertEquals(170.0, stateAfter.vault, 0.001)
        assertEquals(180.0, stateAfter.todaySpent, 0.001)

        // Rollback expense: each source must receive its exact share!
        repository.rollbackExpense(tx.id)

        val stateRestored = repository.getOrCreateAppState()
        assertEquals(100.0, stateRestored.pocketCash, 0.001)
        assertEquals(50.0, stateRestored.splurgePool, 0.001)
        assertEquals(200.0, stateRestored.vault, 0.001)
        assertEquals(0.0, stateRestored.todaySpent, 0.001)
    }

    @Test
    fun `test rollback expense recorded with previous date`() = runBlocking {
        // Expense recorded for yesterday (isYesterday = true)
        val dbState = AppStateEntity(
            id = 1,
            pocketCash = 500.0,
            vault = 300.0,
            splurgePool = 80.0,
            daysLeft = 15,
            todaySpent = 45.0, // Spent earlier today
            isInstituteDay = 0,
            lastProcessedDate = repository.getTodayDateStr()
        )
        db.appStateDao().insertOrUpdate(dbState)

        // Record yesterday expense of 100.0:
        // Waterfall for yesterday: Splurge (80.0) -> Pocket (20.0)
        val res = repository.recordExpense(
            title = "عشاء أمس منسي",
            amount = 100.0,
            category = "طعام ومشروبات",
            isYesterday = true
        )
        assertTrue(res.success)

        val tx = repository.allTransactions.first()[0]
        assertEquals("أمس", tx.dateTag)
        assertEquals(20.0, tx.deductedFromPocket, 0.001)
        assertEquals(80.0, tx.deductedFromSplurge, 0.001)

        val stateAfterRecord = repository.getOrCreateAppState()
        assertEquals(480.0, stateAfterRecord.pocketCash, 0.001)
        assertEquals(0.0, stateAfterRecord.splurgePool, 0.001)
        // CRITICAL: todaySpent must NOT have changed when adding yesterday's expense
        assertEquals(45.0, stateAfterRecord.todaySpent, 0.001)

        // Rollback yesterday's expense
        repository.rollbackExpense(tx.id)

        val stateRestored = repository.getOrCreateAppState()
        assertEquals(500.0, stateRestored.pocketCash, 0.001)
        assertEquals(80.0, stateRestored.splurgePool, 0.001)
        assertEquals(300.0, stateRestored.vault, 0.001)
        // CRITICAL: todaySpent must REMAIN 45.0 (not decremented because it was yesterday)
        assertEquals(45.0, stateRestored.todaySpent, 0.001)
    }

    @Test
    fun `test prevent ending day twice on the same date`() = runBlocking {
        repository.resetCycle(totalReceived = 1500.0, days = 10, savingsType = "percentage", savingsValue = 20.0)
        val initial = repository.getOrCreateAppState()
        val initialDays = initial.daysLeft

        // First manual close
        val firstClose = repository.manualEndDay()
        assertTrue(firstClose.success)

        val stateAfterFirst = repository.getOrCreateAppState()
        assertEquals(initialDays - 1, stateAfterFirst.daysLeft)
        assertEquals(repository.getTodayDateStr(), stateAfterFirst.lastClosedDate)

        // Second manual close on the same date -> MUST BE PREVENTED
        val secondClose = repository.manualEndDay()
        assertFalse(secondClose.success)
        assertTrue(secondClose.message.contains("بالفعل"))

        // Days left must NOT have been decremented again
        val stateAfterSecond = repository.getOrCreateAppState()
        assertEquals(initialDays - 1, stateAfterSecond.daysLeft)
    }
}
