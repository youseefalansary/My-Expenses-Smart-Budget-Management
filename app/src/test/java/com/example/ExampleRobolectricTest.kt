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

    @Test
    fun `test 950 total 150 fixed savings 6 days with 100 expense remaining is approximately 33 point 33`() = runBlocking {
        // Requirement test: 950 EGP total, 150 EGP fixed savings, 6 days
        // Pocket cash = 950 - 150 = 800 EGP
        // Daily allowance = 800 / 6 = 133.333... EGP
        repository.resetCycle(totalReceived = 950.0, days = 6, savingsType = "fixed", savingsValue = 150.0)

        val initial = repository.getOrCreateAppState()
        assertEquals(800.0, initial.pocketCash, 0.001)
        assertEquals(150.0, initial.vault, 0.001)
        assertEquals(6, initial.daysLeft)
        assertEquals(800.0, initial.dayStartPocket, 0.001)

        val startPocketInitial = if (initial.dayStartPocket > 0.0) initial.dayStartPocket else (initial.pocketCash + initial.todaySpent)
        val initialAllowance = startPocketInitial / initial.daysLeft
        assertEquals(133.33, initialAllowance, 0.01)

        // Record 100 EGP expense
        val result = repository.recordExpense(
            title = "غداء اليوم",
            amount = 100.0,
            category = "طعام ومشروبات",
            isYesterday = false
        )
        assertTrue(result.success)

        val stateAfterExpense = repository.getOrCreateAppState()
        assertEquals(700.0, stateAfterExpense.pocketCash, 0.001)
        assertEquals(100.0, stateAfterExpense.todaySpent, 0.001)
        // CRITICAL: dayStartPocket must remain 800.0 so daily allowance does NOT drop to 116.67
        assertEquals(800.0, stateAfterExpense.dayStartPocket, 0.001)

        val startPocketAfter = if (stateAfterExpense.dayStartPocket > 0.0) stateAfterExpense.dayStartPocket else (stateAfterExpense.pocketCash + stateAfterExpense.todaySpent)
        val allowanceAfter = startPocketAfter / stateAfterExpense.daysLeft
        assertEquals(133.33, allowanceAfter, 0.01)

        // Remaining today allowance must be approximately 33.33 EGP (133.33 - 100.0), NOT 16 EGP!
        val remainingToday = kotlin.math.max(0.0, allowanceAfter - stateAfterExpense.todaySpent)
        assertEquals(33.33, remainingToday, 0.01)
    }

    @Test
    fun `test surplus transfer at end of day actually moves money from pocket cash to splurge pool`() = runBlocking {
        // Setup: Cycle with 950 total, 150 savings, 6 days -> 800 pocket (133.33/day)
        repository.resetCycle(totalReceived = 950.0, days = 6, savingsType = "fixed", savingsValue = 150.0)
        // Record 100 EGP expense -> todaySpent = 100.0, pocketCash = 700.0
        repository.recordExpense("مصروف", 100.0, "طعام ومشروبات", false)

        val stateBeforeEnd = repository.getOrCreateAppState()
        assertEquals(700.0, stateBeforeEnd.pocketCash, 0.001)
        assertEquals(0.0, stateBeforeEnd.splurgePool, 0.001)
        assertEquals(6, stateBeforeEnd.daysLeft)

        // End day: surplus = 133.33 - 100 = 33.33
        val endResult = repository.manualEndDay()
        assertTrue(endResult.success)
        assertEquals(33.33, endResult.surplus, 0.01)

        val stateAfterEnd = repository.getOrCreateAppState()
        // 1. Surplus is transferred into splurgePool
        assertEquals(33.33, stateAfterEnd.splurgePool, 0.01)
        // 2. Surplus is DEDUCTED from pocketCash (700 - 33.33 = 666.67), NOT counted in both!
        assertEquals(666.67, stateAfterEnd.pocketCash, 0.01)
        // 3. Days left decremented to 5
        assertEquals(5, stateAfterEnd.daysLeft)
        // 4. Starting pocket for day 2 is 666.67, so day 2 allowance is 666.67 / 5 = 133.33 EGP
        assertEquals(666.67, stateAfterEnd.dayStartPocket, 0.01)
        val day2Allowance = stateAfterEnd.dayStartPocket / stateAfterEnd.daysLeft
        assertEquals(133.33, day2Allowance, 0.01)

        // 5. Total system balance conservation: pocket(666.67) + splurge(33.33) + spent(100) + vault(150) = 950.0
        val totalSystemMoney = stateAfterEnd.pocketCash + stateAfterEnd.splurgePool + stateBeforeEnd.todaySpent + stateAfterEnd.vault
        assertEquals(950.0, totalSystemMoney, 0.01)
    }

    @Test
    fun `test expense and freelance record exact date, day of week, and exact time`() = runBlocking {
        repository.resetCycle(totalReceived = 1000.0, days = 10, savingsType = "fixed", savingsValue = 200.0)

        // Record expense
        repository.recordExpense("قهوة وساندوتش", 50.0, "طعام ومشروبات", false)
        val txList = repository.allTransactions.first()
        assertEquals(1, txList.size)
        val tx = txList[0]
        assertTrue("day_name should not be empty", tx.dayName.isNotBlank())
        assertTrue("exact_time should not be empty", tx.exactTime.isNotBlank())
        assertTrue("date_iso should be valid", tx.dateIso.isNotBlank())
        assertTrue("date_millis should be positive", tx.dateMillis > 0L)

        // Record freelance income
        repository.depositFreelanceIncome("تصميم شعار", 300.0)
        val workList = repository.workHistory.first()
        assertEquals(1, workList.size)
        val work = workList[0]
        assertTrue("work day_name should not be empty", work.dayName.isNotBlank())
        assertTrue("work exact_time should not be empty", work.exactTime.isNotBlank())
        assertTrue("work dateStr should contain day and time", work.dateStr.contains("•"))
    }

    @Test
    fun `test total money with me calculation and cycle duration adjustment`() = runBlocking {
        // Setup cycle: 1200 total, 200 vault -> 1000 pocket
        repository.resetCycle(totalReceived = 1200.0, days = 20, savingsType = "fixed", savingsValue = 200.0)
        val state1 = repository.getOrCreateAppState()

        // Total money with me = pocketCash(1000) + vault(200) + splurge(0) + transportReserve(0) = 1200
        val total1 = state1.pocketCash + state1.vault + state1.splurgePool + state1.transportReserve
        assertEquals(1200.0, total1, 0.001)

        // Adjust cycle duration to 15 days with specific end date
        repository.updateCycleDuration(days = 15, endDate = "2026-11-05")
        val state2 = repository.getOrCreateAppState()
        assertEquals(15, state2.daysLeft)
        assertEquals("2026-11-05", state2.cycleEndDate)
        // Balances should be completely preserved!
        assertEquals(1000.0, state2.pocketCash, 0.001)
        assertEquals(200.0, state2.vault, 0.001)

        val total2 = state2.pocketCash + state2.vault + state2.splurgePool + state2.transportReserve
        assertEquals(1200.0, total2, 0.001)
    }
}
