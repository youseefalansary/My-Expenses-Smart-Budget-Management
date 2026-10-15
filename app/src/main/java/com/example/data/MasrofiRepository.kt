package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class ExpenseResult(
    val success: Boolean,
    val vaultWarned: Boolean,
    val message: String
)

class MasrofiRepository(
    private val appStateDao: AppStateDao,
    private val transactionDao: TransactionDao,
    private val workHistoryDao: WorkHistoryDao,
    private val debtDao: DebtDao
) {
    val appState: Flow<AppStateEntity?> = appStateDao.getAppState()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(20)
    val workHistory: Flow<List<WorkHistoryEntity>> = workHistoryDao.getAll()
    val debts: Flow<List<DebtEntity>> = debtDao.getAll()

    private fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun getCurrentTimeStr(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        return sdf.format(Date())
    }

    suspend fun getOrCreateAppState(): AppStateEntity {
        var state = appStateDao.getAppStateSync()
        if (state == null) {
            state = AppStateEntity(
                id = 1,
                pocketCash = 1800.0,
                vault = 600.0,
                splurgePool = 120.0,
                daysLeft = 30,
                todaySpent = 0.0,
                isInstituteDay = 0,
                lastProcessedDate = getTodayDateStr(),
                workTotal = 0.0
            )
            appStateDao.insertOrUpdate(state)
        }
        return state
    }

    suspend fun checkAndPerformMidnightRollover(): Boolean {
        val state = getOrCreateAppState()
        val today = getTodayDateStr()
        if (state.lastProcessedDate.isNotEmpty() && state.lastProcessedDate != today) {
            performDayRolloverInternal(state, today)
            return true
        }
        return false
    }

    suspend fun manualEndDay(): Double {
        val state = getOrCreateAppState()
        val today = getTodayDateStr()
        return performDayRolloverInternal(state, today)
    }

    private suspend fun performDayRolloverInternal(state: AppStateEntity, todayDate: String): Double {
        val allowance = if (state.daysLeft > 0) state.pocketCash / state.daysLeft else 0.0
        val surplus = max(0.0, allowance - state.todaySpent)
        val newSplurge = state.splurgePool + surplus
        val newDaysLeft = max(0, state.daysLeft - 1)

        val updatedState = state.copy(
            splurgePool = newSplurge,
            daysLeft = newDaysLeft,
            todaySpent = 0.0,
            isInstituteDay = 0,
            lastProcessedDate = todayDate
        )
        appStateDao.insertOrUpdate(updatedState)
        return surplus
    }

    suspend fun toggleInstituteDay(enabled: Boolean) {
        val state = getOrCreateAppState()
        appStateDao.insertOrUpdate(state.copy(isInstituteDay = if (enabled) 1 else 0))
    }

    suspend fun recordExpense(
        title: String,
        amount: Double,
        category: String,
        isYesterday: Boolean
    ): ExpenseResult {
        if (amount <= 0) {
            return ExpenseResult(false, false, "المبلغ يجب أن يكون أكبر من صفر")
        }

        val state = getOrCreateAppState()
        var vaultWarned = false
        var newPocketCash = state.pocketCash
        var newVault = state.vault
        var newSplurge = state.splurgePool
        var newTodaySpent = state.todaySpent

        if (isYesterday) {
            // Forgotten expense from yesterday: DOES NOT touch today_spent!
            // Waterfall: Splurge Pool -> Pocket Cash -> Vault
            val fromSplurge = min(newSplurge, amount)
            newSplurge -= fromSplurge
            var remaining = amount - fromSplurge

            if (remaining > 0) {
                val fromPocket = min(max(0.0, newPocketCash), remaining)
                newPocketCash -= fromPocket
                remaining -= fromPocket
            }

            if (remaining > 0) {
                val fromVault = min(max(0.0, newVault), remaining)
                newVault -= fromVault
                remaining -= fromVault
                vaultWarned = true
            }
        } else {
            // Regular today's expense: Waterfall deduction engine
            val allowance = if (state.daysLeft > 0) state.pocketCash / state.daysLeft else 0.0
            val remainingAllowanceToday = max(0.0, allowance - state.todaySpent)

            if (amount <= remainingAllowanceToday) {
                // Entire amount is within today's daily allowance
                newPocketCash = max(0.0, newPocketCash - amount)
            } else {
                // Deficit occurred!
                val portionWithin = remainingAllowanceToday
                val deficit = amount - portionWithin

                // Deduct portion within allowance from pocket cash
                newPocketCash = max(0.0, newPocketCash - portionWithin)

                // 1) Deficit deducted first from splurge pool
                val fromSplurge = min(newSplurge, deficit)
                newSplurge -= fromSplurge
                var remDeficit = deficit - fromSplurge

                // 2) If still remaining, deduct from pocket cash directly (lowers future days allowance)
                if (remDeficit > 0) {
                    val fromPocket = min(max(0.0, newPocketCash), remDeficit)
                    newPocketCash -= fromPocket
                    remDeficit -= fromPocket
                }

                // 3) If pocket cash hits 0, deduct remaining deficit from vault!
                if (remDeficit > 0) {
                    val fromVault = min(max(0.0, newVault), remDeficit)
                    newVault -= fromVault
                    remDeficit -= fromVault
                    vaultWarned = true
                }
            }
            newTodaySpent += amount
        }

        // Persist updated app state
        appStateDao.insertOrUpdate(
            state.copy(
                pocketCash = newPocketCash,
                vault = newVault,
                splurgePool = newSplurge,
                todaySpent = newTodaySpent
            )
        )

        // Insert transaction record
        val dateTag = if (isYesterday) "أمس" else "اليوم"
        val timeStr = getCurrentTimeStr()
        transactionDao.insert(
            TransactionEntity(
                title = title.ifBlank { "مصروف $category" },
                amount = amount,
                category = category,
                dateTag = dateTag,
                createdAt = timeStr
            )
        )

        return ExpenseResult(
            success = true,
            vaultWarned = vaultWarned,
            message = if (vaultWarned) "تم الخصم مع بدء استنزاف التوفير الإجباري!" else "تم تسجيل المصروف بنجاح"
        )
    }

    suspend fun rollbackExpense(transactionId: Int) {
        val tx = transactionDao.getById(transactionId) ?: return
        val state = getOrCreateAppState()

        val newPocketCash = state.pocketCash + tx.amount
        val newTodaySpent = if (tx.dateTag == "اليوم") {
            max(0.0, state.todaySpent - tx.amount)
        } else {
            state.todaySpent
        }

        appStateDao.insertOrUpdate(
            state.copy(
                pocketCash = newPocketCash,
                todaySpent = newTodaySpent
            )
        )

        transactionDao.deleteById(transactionId)
    }

    suspend fun depositFreelanceIncome(source: String, amount: Double) {
        if (amount <= 0) return
        val state = getOrCreateAppState()
        // 100% goes to pocketCash, 0% cut for vault
        val updatedState = state.copy(
            pocketCash = state.pocketCash + amount,
            workTotal = state.workTotal + amount
        )
        appStateDao.insertOrUpdate(updatedState)

        val dateStr = getTodayDateStr() + " " + getCurrentTimeStr()
        workHistoryDao.insert(
            WorkHistoryEntity(
                source = source.ifBlank { "مشروع حر" },
                amount = amount,
                dateStr = dateStr
            )
        )
    }

    suspend fun addDebt(person: String, amount: Double, type: String) {
        if (amount <= 0) return
        val state = getOrCreateAppState()

        // 'give' = سلّفت وخرجت من جيبي -> تخرج من pocket_cash
        // 'take' = استلفت ودخلت جيبي -> تدخل pocket_cash
        val newPocket = if (type == "give") {
            max(0.0, state.pocketCash - amount)
        } else {
            state.pocketCash + amount
        }

        appStateDao.insertOrUpdate(state.copy(pocketCash = newPocket))

        debtDao.insert(
            DebtEntity(
                person = person.ifBlank { "شخص" },
                amount = amount,
                type = type,
                isSettled = 0
            )
        )
    }

    suspend fun settleDebt(debtId: Int) {
        val debt = debtDao.getById(debtId) ?: return
        if (debt.isSettled == 1) return

        val state = getOrCreateAppState()
        // When settled:
        // If it was 'give' (سلّفت): رجعت الفلوس لجيبك -> تزيد pocket_cash
        // If it was 'take' (استلفت): سددت الدين من جيبك -> تخصم من pocket_cash
        val newPocket = if (debt.type == "give") {
            state.pocketCash + debt.amount
        } else {
            max(0.0, state.pocketCash - debt.amount)
        }

        appStateDao.insertOrUpdate(state.copy(pocketCash = newPocket))
        debtDao.updateSettled(debtId, 1)
    }

    suspend fun resetCycle(totalReceived: Double, days: Int, vaultPercentage: Double) {
        val vaultAmount = totalReceived * (vaultPercentage / 100.0)
        val pocketAmount = max(0.0, totalReceived - vaultAmount)
        val today = getTodayDateStr()

        val state = getOrCreateAppState()
        val newState = state.copy(
            pocketCash = pocketAmount,
            vault = vaultAmount,
            splurgePool = 0.0,
            daysLeft = max(1, days),
            todaySpent = 0.0,
            isInstituteDay = 0,
            lastProcessedDate = today
        )
        appStateDao.insertOrUpdate(newState)
    }
}
