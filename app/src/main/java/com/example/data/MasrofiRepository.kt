package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class ExpenseResult(
    val success: Boolean,
    val vaultWarned: Boolean,
    val message: String
)

data class EndDayResult(
    val success: Boolean,
    val surplus: Double,
    val message: String
)

class MasrofiRepository(
    private val appStateDao: AppStateDao,
    private val transactionDao: TransactionDao,
    private val workHistoryDao: WorkHistoryDao,
    private val debtDao: DebtDao,
    private val categoryDao: CategoryDao
) {
    val appState: Flow<AppStateEntity?> = appStateDao.getAppState()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(20)
    val workHistory: Flow<List<WorkHistoryEntity>> = workHistoryDao.getAll()
    val debts: Flow<List<DebtEntity>> = debtDao.getAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentTimeStr(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        return sdf.format(Date())
    }

    suspend fun getOrCreateAppState(): AppStateEntity {
        var state = appStateDao.getAppStateSync()
        val today = getTodayDateStr()
        if (state == null) {
            state = AppStateEntity(
                id = 1,
                pocketCash = 1600.0,
                vault = 400.0,
                splurgePool = 0.0,
                daysLeft = 30,
                todaySpent = 0.0,
                isInstituteDay = 0,
                lastProcessedDate = today,
                workTotal = 0.0,
                lastClosedDate = "",
                transportReserve = 0.0,
                savingsType = "percentage",
                savingsValue = 20.0,
                totalReceived = 2000.0,
                dailyReminderEnabled = 0,
                thresholdAlertEnabled = 0,
                categoryAlertEnabled = 0,
                dayStartPocket = 1600.0
            )
            appStateDao.insertOrUpdate(state)
        }

        // Initialize default categories if empty
        val existingCats = categoryDao.getAllCategoriesSync()
        if (existingCats.isEmpty()) {
            categoryDao.insertAll(
                listOf(
                    CategoryEntity(name = "طعام ومشروبات", emoji = "🍔", budgetLimit = 0.0, isDefault = 1),
                    CategoryEntity(name = "مواصلات", emoji = "🚌", budgetLimit = 0.0, isDefault = 1),
                    CategoryEntity(name = "علاج", emoji = "💊", budgetLimit = 0.0, isDefault = 1),
                    CategoryEntity(name = "شخصي", emoji = "🛍️", budgetLimit = 0.0, isDefault = 1)
                )
            )
        }

        return state
    }

    suspend fun checkAndPerformMidnightRollover(): Boolean {
        val state = getOrCreateAppState()
        val today = getTodayDateStr()

        if (state.lastProcessedDate.isNotEmpty() && state.lastProcessedDate != today) {
            // Check if the previous day was already manually closed
            val wasAlreadyClosed = (state.lastClosedDate == state.lastProcessedDate)

            if (wasAlreadyClosed) {
                // Already closed manually: simply update lastProcessedDate, reset today's spent and set dayStartPocket
                appStateDao.insertOrUpdate(
                    state.copy(
                        lastProcessedDate = today,
                        todaySpent = 0.0,
                        isInstituteDay = 0,
                        transportReserve = 0.0,
                        dayStartPocket = state.pocketCash
                    )
                )
            } else {
                // Not closed yet: close previous day once, transfer surplus and decrement daysLeft
                val startPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else (state.pocketCash + state.todaySpent)
                val allowance = if (state.daysLeft > 0) startPocket / state.daysLeft else 0.0
                val surplus = max(0.0, allowance - state.todaySpent)
                val newSplurge = state.splurgePool + surplus
                val newPocketCash = max(0.0, state.pocketCash - surplus)
                val newDaysLeft = max(0, state.daysLeft - 1)

                appStateDao.insertOrUpdate(
                    state.copy(
                        pocketCash = newPocketCash,
                        dayStartPocket = newPocketCash,
                        splurgePool = newSplurge,
                        daysLeft = newDaysLeft,
                        todaySpent = 0.0,
                        isInstituteDay = 0,
                        transportReserve = 0.0,
                        lastClosedDate = state.lastProcessedDate,
                        lastProcessedDate = today
                    )
                )
            }
            return true
        }
        return false
    }

    suspend fun manualEndDay(): EndDayResult {
        val state = getOrCreateAppState()
        val today = getTodayDateStr()

        // Requirement 2: Prevent ending day multiple times on the same date!
        if (state.lastClosedDate == today) {
            return EndDayResult(
                success = false,
                surplus = 0.0,
                message = "لقد تم إنهاء وإغلاق هذا اليوم ($today) بالفعل مسبقاً، ولا يمكن إغلاقه مرتين في نفس التاريخ."
            )
        }

        val startPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else (state.pocketCash + state.todaySpent)
        val allowance = if (state.daysLeft > 0) startPocket / state.daysLeft else 0.0
        val surplus = max(0.0, allowance - state.todaySpent)
        val newSplurge = state.splurgePool + surplus
        val newPocketCash = max(0.0, state.pocketCash - surplus)
        val newDaysLeft = max(0, state.daysLeft - 1)

        val updatedState = state.copy(
            pocketCash = newPocketCash,
            dayStartPocket = newPocketCash,
            splurgePool = newSplurge,
            daysLeft = newDaysLeft,
            todaySpent = 0.0,
            isInstituteDay = 0,
            transportReserve = 0.0,
            lastClosedDate = today,
            lastProcessedDate = today
        )
        appStateDao.insertOrUpdate(updatedState)

        val msg = if (surplus > 0) {
            "عاش! تم إنهاء اليوم بنجاح وترحيل فائض ${formatTwoDecimals(surplus)} ج.م إلى حصالة الوجبة الغالية 🎉"
        } else {
            "تم إنهاء اليوم وإغلاقه بنجاح، وتحديث الأيام المتبقية في الدورة."
        }
        return EndDayResult(true, surplus, msg)
    }

    suspend fun toggleInstituteDay(enabled: Boolean) {
        val state = getOrCreateAppState()
        if (enabled) {
            // Requirement 3: Reserve 40 EGP specifically for transportation
            appStateDao.insertOrUpdate(
                state.copy(
                    isInstituteDay = 1,
                    transportReserve = 40.0
                )
            )
        } else {
            appStateDao.insertOrUpdate(
                state.copy(
                    isInstituteDay = 0,
                    transportReserve = 0.0
                )
            )
        }
    }

    suspend fun recordExpense(
        title: String,
        amount: Double,
        category: String,
        isYesterday: Boolean
    ): ExpenseResult {
        // Requirement 5: Whole integers only for expenses!
        if (amount <= 0) {
            return ExpenseResult(false, false, "المبلغ يجب أن يكون أكبر من صفر")
        }
        if (amount != kotlin.math.floor(amount)) {
            return ExpenseResult(
                false,
                false,
                "مبالغ المصروفات يجب أن تكون أعداداً صحيحة بالجنيه دون قروش (مثال: 12 أو 15)"
            )
        }

        val state = getOrCreateAppState()
        var vaultWarned = false

        var newPocketCash = state.pocketCash
        var newVault = state.vault
        var newSplurge = state.splurgePool
        var newTransportReserve = state.transportReserve
        var newTodaySpent = state.todaySpent

        var dedPocket = 0.0
        var dedSplurge = 0.0
        var dedVault = 0.0
        var dedTransport = 0.0

        var remainingAmount = amount

        if (isYesterday) {
            // Forgotten expense from yesterday: Waterfall: Splurge -> Pocket -> Vault
            val fromSplurge = min(newSplurge, remainingAmount)
            dedSplurge = fromSplurge
            newSplurge -= fromSplurge
            remainingAmount -= fromSplurge

            if (remainingAmount > 0) {
                val fromPocket = min(max(0.0, newPocketCash), remainingAmount)
                dedPocket = fromPocket
                newPocketCash -= fromPocket
                remainingAmount -= fromPocket
            }

            if (remainingAmount > 0) {
                val fromVault = min(max(0.0, newVault), remainingAmount)
                dedVault = fromVault
                newVault -= fromVault
                remainingAmount -= fromVault
                vaultWarned = true
            }
        } else {
            // Requirement 3: Institute Day real transportation reserve deduction
            if (category == "مواصلات" && state.isInstituteDay == 1 && newTransportReserve > 0) {
                val fromTransport = min(newTransportReserve, remainingAmount)
                dedTransport = fromTransport
                newTransportReserve -= fromTransport
                remainingAmount -= fromTransport
            }

            if (remainingAmount > 0) {
                val startPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else (state.pocketCash + state.todaySpent)
                val allowance = if (state.daysLeft > 0) startPocket / state.daysLeft else 0.0
                val remainingAllowanceToday = max(0.0, allowance - state.todaySpent)

                if (remainingAmount <= remainingAllowanceToday) {
                    dedPocket = remainingAmount
                    newPocketCash = max(0.0, newPocketCash - remainingAmount)
                } else {
                    val portionWithin = remainingAllowanceToday
                    dedPocket += portionWithin
                    newPocketCash = max(0.0, newPocketCash - portionWithin)
                    val deficit = remainingAmount - portionWithin

                    // 1) From Splurge Pool
                    val fromSplurge = min(newSplurge, deficit)
                    dedSplurge = fromSplurge
                    newSplurge -= fromSplurge
                    var remDef = deficit - fromSplurge

                    // 2) From Pocket Cash
                    if (remDef > 0) {
                        val fromPocket = min(max(0.0, newPocketCash), remDef)
                        dedPocket += fromPocket
                        newPocketCash -= fromPocket
                        remDef -= fromPocket
                    }

                    // 3) From Vault
                    if (remDef > 0) {
                        val fromVault = min(max(0.0, newVault), remDef)
                        dedVault = fromVault
                        newVault -= fromVault
                        remDef -= fromVault
                        vaultWarned = true
                    }
                }
                newTodaySpent += remainingAmount
            }
        }

        // Persist AppState with updated balances
        val effectiveStartPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else (state.pocketCash + state.todaySpent)
        appStateDao.insertOrUpdate(
            state.copy(
                pocketCash = newPocketCash,
                vault = newVault,
                splurgePool = newSplurge,
                todaySpent = newTodaySpent,
                transportReserve = newTransportReserve,
                dayStartPocket = effectiveStartPocket
            )
        )

        // Requirement 1 & 6 & v2.0: Store breakdown and real timestamp with day of week & exact time
        val nowMillis = System.currentTimeMillis()
        val todayIso = getTodayDateStr()
        val effectiveIso = if (isYesterday) DateTimeUtils.getYesterdayIso() else todayIso
        val dayName = DateTimeUtils.getDayOfWeekName(nowMillis, true)
        val timeStr = DateTimeUtils.formatTime(nowMillis, true)
        val dateTag = if (isYesterday) "أمس" else "اليوم"

        transactionDao.insert(
            TransactionEntity(
                title = title.ifBlank { "مصروف $category" },
                amount = amount,
                category = category,
                dateTag = dateTag,
                createdAt = timeStr,
                deductedFromPocket = dedPocket,
                deductedFromSplurge = dedSplurge,
                deductedFromVault = dedVault,
                deductedFromTransport = dedTransport,
                dateMillis = nowMillis,
                dateIso = effectiveIso,
                dayName = dayName,
                exactTime = timeStr
            )
        )

        val message = if (vaultWarned) {
            "تم الخصم مع بدء استنزاف التوفير الإجباري!"
        } else if (dedTransport > 0) {
            "تم الخصم بنجاح (مشمولاً ${dedTransport.toInt()} من محجوز مواصلات المعهد)"
        } else {
            "تم تسجيل المصروف بنجاح وتحديث الأرصدة"
        }

        return ExpenseResult(success = true, vaultWarned = vaultWarned, message = message)
    }

    suspend fun rollbackExpense(transactionId: Int, legacyDestination: String = "pocket") {
        val tx = transactionDao.getById(transactionId) ?: return
        val state = getOrCreateAppState()

        val isMultiOrTracked = (tx.deductedFromPocket > 0 ||
                tx.deductedFromSplurge > 0 ||
                tx.deductedFromVault > 0 ||
                tx.deductedFromTransport > 0)

        var newPocketCash = state.pocketCash
        var newSplurgePool = state.splurgePool
        var newVault = state.vault
        var newTransportReserve = state.transportReserve
        var newTodaySpent = state.todaySpent

        if (isMultiOrTracked) {
            // Requirement 1: Restore each part to the exact source it was deducted from!
            newPocketCash += tx.deductedFromPocket
            newSplurgePool += tx.deductedFromSplurge
            newVault += tx.deductedFromVault
            newTransportReserve += tx.deductedFromTransport

            if (tx.dateTag == "اليوم" || tx.dateTag.startsWith("اليوم")) {
                val nonTransportPortion = tx.amount - tx.deductedFromTransport
                newTodaySpent = max(0.0, state.todaySpent - nonTransportPortion)
            }
        } else {
            // Legacy transaction fallback: return to chosen destination
            when (legacyDestination) {
                "splurge" -> newSplurgePool += tx.amount
                "vault" -> newVault += tx.amount
                else -> newPocketCash += tx.amount
            }
            if (tx.dateTag == "اليوم" || tx.dateTag.startsWith("اليوم")) {
                newTodaySpent = max(0.0, state.todaySpent - tx.amount)
            }
        }

        // Safe atomic persistence
        appStateDao.insertOrUpdate(
            state.copy(
                pocketCash = newPocketCash,
                splurgePool = newSplurgePool,
                vault = newVault,
                transportReserve = newTransportReserve,
                todaySpent = newTodaySpent
            )
        )

        transactionDao.deleteById(transactionId)
    }

    suspend fun updateExpense(
        transactionId: Int,
        newTitle: String,
        newAmount: Double,
        newCategory: String
    ): ExpenseResult {
        val tx = transactionDao.getById(transactionId) ?: return ExpenseResult(false, false, "المصروف غير موجود")
        val isYesterday = tx.dateTag == "أمس"

        // 1. Rollback previous expense safely
        rollbackExpense(transactionId)

        // 2. Re-record with new parameters
        return recordExpense(newTitle, newAmount, newCategory, isYesterday)
    }

    suspend fun depositFreelanceIncome(source: String, amount: Double) {
        if (amount <= 0) return
        val state = getOrCreateAppState()
        val curStartPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else state.pocketCash
        val updatedState = state.copy(
            pocketCash = state.pocketCash + amount,
            dayStartPocket = curStartPocket + amount,
            workTotal = state.workTotal + amount
        )
        appStateDao.insertOrUpdate(updatedState)

        val nowMillis = System.currentTimeMillis()
        val dayName = DateTimeUtils.getDayOfWeekName(nowMillis, true)
        val timeStr = DateTimeUtils.formatTime(nowMillis, true)
        val dateIso = getTodayDateStr()
        val fullDateDisplay = "$dayName $dateIso • $timeStr"
        workHistoryDao.insert(
            WorkHistoryEntity(
                source = source.ifBlank { "مشروع حر" },
                amount = amount,
                dateStr = fullDateDisplay,
                dayName = dayName,
                exactTime = timeStr,
                dateMillis = nowMillis
            )
        )
    }

    suspend fun addDebt(person: String, amount: Double, type: String) {
        if (amount <= 0) return
        val state = getOrCreateAppState()

        val newPocket = if (type == "give") {
            max(0.0, state.pocketCash - amount)
        } else {
            state.pocketCash + amount
        }
        val curStartPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else state.pocketCash
        val newDayStart = if (type == "give") max(0.0, curStartPocket - amount) else (curStartPocket + amount)

        appStateDao.insertOrUpdate(state.copy(pocketCash = newPocket, dayStartPocket = newDayStart))

        val nowMillis = System.currentTimeMillis()
        val dayName = DateTimeUtils.getDayOfWeekName(nowMillis, true)
        val timeStr = DateTimeUtils.formatTime(nowMillis, true)
        val dateIso = getTodayDateStr()
        val fullDateDisplay = "$dayName $dateIso • $timeStr"

        debtDao.insert(
            DebtEntity(
                person = person.ifBlank { "شخص" },
                amount = amount,
                type = type,
                isSettled = 0,
                dateStr = fullDateDisplay,
                dateMillis = nowMillis
            )
        )
    }

    suspend fun settleDebt(debtId: Int) {
        val debt = debtDao.getById(debtId) ?: return
        if (debt.isSettled == 1) return

        val state = getOrCreateAppState()
        val newPocket = if (debt.type == "give") {
            state.pocketCash + debt.amount
        } else {
            max(0.0, state.pocketCash - debt.amount)
        }
        val curStartPocket = if (state.dayStartPocket > 0.0) state.dayStartPocket else state.pocketCash
        val newDayStart = if (debt.type == "give") (curStartPocket + debt.amount) else max(0.0, curStartPocket - debt.amount)

        appStateDao.insertOrUpdate(state.copy(pocketCash = newPocket, dayStartPocket = newDayStart))
        debtDao.updateSettled(debtId, 1)
    }

    // Cycle duration & target end date adjustment
    suspend fun updateCycleDuration(days: Int, endDate: String) {
        val state = getOrCreateAppState()
        appStateDao.insertOrUpdate(
            state.copy(
                daysLeft = max(1, days),
                cycleEndDate = endDate
            )
        )
    }

    // App Preferences (Currency & Language)
    suspend fun updatePreferences(currencyCode: String, languageCode: String) {
        val state = getOrCreateAppState()
        appStateDao.insertOrUpdate(
            state.copy(
                currencyCode = currencyCode,
                languageCode = languageCode
            )
        )
    }

    // Requirement 4: Savings as Percentage OR Fixed Amount
    suspend fun resetCycle(
        totalReceived: Double,
        days: Int,
        savingsType: String,
        savingsValue: Double,
        cycleEndDate: String = ""
    ) {
        val vaultAmount: Double = if (savingsType == "fixed") {
            min(totalReceived, max(0.0, savingsValue))
        } else {
            val pct = min(100.0, max(0.0, savingsValue))
            round((totalReceived * (pct / 100.0)) * 100.0) / 100.0
        }

        val pocketAmount = max(0.0, totalReceived - vaultAmount)
        val today = getTodayDateStr()

        val state = getOrCreateAppState()
        val calculatedEndDate = if (cycleEndDate.isNotBlank()) {
            cycleEndDate
        } else {
            DateTimeUtils.addDaysToIso(today, days)
        }

        val newState = state.copy(
            pocketCash = pocketAmount,
            dayStartPocket = pocketAmount,
            vault = vaultAmount,
            splurgePool = 0.0,
            daysLeft = max(1, days),
            todaySpent = 0.0,
            isInstituteDay = 0,
            transportReserve = 0.0,
            lastProcessedDate = today,
            lastClosedDate = "",
            totalReceived = totalReceived,
            savingsType = savingsType,
            savingsValue = savingsValue,
            cycleEndDate = calculatedEndDate
        )
        appStateDao.insertOrUpdate(newState)
    }

    // Categories Management
    suspend fun addCategory(name: String, emoji: String, budgetLimit: Double) {
        if (name.isBlank()) return
        categoryDao.insert(
            CategoryEntity(
                name = name.trim(),
                emoji = emoji.ifBlank { "📁" },
                budgetLimit = max(0.0, budgetLimit),
                isDefault = 0
            )
        )
    }

    suspend fun updateCategory(id: Int, name: String, emoji: String, budgetLimit: Double) {
        categoryDao.update(
            CategoryEntity(
                id = id,
                name = name.trim(),
                emoji = emoji.ifBlank { "📁" },
                budgetLimit = max(0.0, budgetLimit),
                isDefault = 0
            )
        )
    }

    suspend fun deleteCategory(id: Int) {
        categoryDao.deleteById(id)
    }

    // Reminders & Alerts
    suspend fun updateReminderSettings(daily: Boolean, threshold: Boolean, category: Boolean) {
        val state = getOrCreateAppState()
        appStateDao.insertOrUpdate(
            state.copy(
                dailyReminderEnabled = if (daily) 1 else 0,
                thresholdAlertEnabled = if (threshold) 1 else 0,
                categoryAlertEnabled = if (category) 1 else 0
            )
        )
    }

    // Requirement 7: Local Backup & Restore
    suspend fun exportBackupJson(): String {
        val state = getOrCreateAppState()
        val allTx = transactionDao.getAllTransactionsSync()
        val allWork = workHistoryDao.getAllSync()
        val allDebts = debtDao.getAllSync()
        val allCats = categoryDao.getAllCategoriesSync()

        val root = JSONObject()
        root.put("version", "2.0")
        root.put("exportDate", getTodayDateStr() + " " + getCurrentTimeStr())

        val stateObj = JSONObject()
        stateObj.put("pocketCash", state.pocketCash)
        stateObj.put("vault", state.vault)
        stateObj.put("splurgePool", state.splurgePool)
        stateObj.put("daysLeft", state.daysLeft)
        stateObj.put("todaySpent", state.todaySpent)
        stateObj.put("isInstituteDay", state.isInstituteDay)
        stateObj.put("lastProcessedDate", state.lastProcessedDate)
        stateObj.put("workTotal", state.workTotal)
        stateObj.put("lastClosedDate", state.lastClosedDate)
        stateObj.put("transportReserve", state.transportReserve)
        stateObj.put("savingsType", state.savingsType)
        stateObj.put("savingsValue", state.savingsValue)
        stateObj.put("totalReceived", state.totalReceived)
        stateObj.put("dayStartPocket", state.dayStartPocket)
        stateObj.put("currencyCode", state.currencyCode)
        stateObj.put("languageCode", state.languageCode)
        stateObj.put("cycleEndDate", state.cycleEndDate)
        root.put("appState", stateObj)

        val txArray = JSONArray()
        for (tx in allTx) {
            val obj = JSONObject()
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("category", tx.category)
            obj.put("dateTag", tx.dateTag)
            obj.put("createdAt", tx.createdAt)
            obj.put("deductedFromPocket", tx.deductedFromPocket)
            obj.put("deductedFromSplurge", tx.deductedFromSplurge)
            obj.put("deductedFromVault", tx.deductedFromVault)
            obj.put("deductedFromTransport", tx.deductedFromTransport)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("dateIso", tx.dateIso)
            obj.put("dayName", tx.dayName)
            obj.put("exactTime", tx.exactTime)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val workArray = JSONArray()
        for (w in allWork) {
            val obj = JSONObject()
            obj.put("source", w.source)
            obj.put("amount", w.amount)
            obj.put("dateStr", w.dateStr)
            obj.put("dayName", w.dayName)
            obj.put("exactTime", w.exactTime)
            obj.put("dateMillis", w.dateMillis)
            workArray.put(obj)
        }
        root.put("workHistory", workArray)

        val debtArray = JSONArray()
        for (d in allDebts) {
            val obj = JSONObject()
            obj.put("person", d.person)
            obj.put("amount", d.amount)
            obj.put("type", d.type)
            obj.put("isSettled", d.isSettled)
            obj.put("dateStr", d.dateStr)
            obj.put("dateMillis", d.dateMillis)
            debtArray.put(obj)
        }
        root.put("debts", debtArray)

        val catArray = JSONArray()
        for (c in allCats) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("emoji", c.emoji)
            obj.put("budgetLimit", c.budgetLimit)
            obj.put("isDefault", c.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("appState")) return false

            val stateObj = root.getJSONObject("appState")
            val pCash = stateObj.optDouble("pocketCash", 1500.0)
            val tSpent = stateObj.optDouble("todaySpent", 0.0)
            val newState = AppStateEntity(
                id = 1,
                pocketCash = pCash,
                dayStartPocket = stateObj.optDouble("dayStartPocket", pCash + tSpent),
                vault = stateObj.optDouble("vault", 500.0),
                splurgePool = stateObj.optDouble("splurgePool", 0.0),
                daysLeft = stateObj.optInt("daysLeft", 30),
                todaySpent = tSpent,
                isInstituteDay = stateObj.optInt("isInstituteDay", 0),
                lastProcessedDate = stateObj.optString("lastProcessedDate", getTodayDateStr()),
                workTotal = stateObj.optDouble("workTotal", 0.0),
                lastClosedDate = stateObj.optString("lastClosedDate", ""),
                transportReserve = stateObj.optDouble("transportReserve", 0.0),
                savingsType = stateObj.optString("savingsType", "percentage"),
                savingsValue = stateObj.optDouble("savingsValue", 20.0),
                totalReceived = stateObj.optDouble("totalReceived", 2000.0),
                currencyCode = stateObj.optString("currencyCode", "EGP"),
                languageCode = stateObj.optString("languageCode", "ar"),
                cycleEndDate = stateObj.optString("cycleEndDate", "")
            )

            // Clear and replace
            appStateDao.insertOrUpdate(newState)
            transactionDao.deleteAll()
            workHistoryDao.deleteAll()
            debtDao.deleteAll()

            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                val list = mutableListOf<TransactionEntity>()
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    list.add(
                        TransactionEntity(
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            dateTag = obj.getString("dateTag"),
                            createdAt = obj.getString("createdAt"),
                            deductedFromPocket = obj.optDouble("deductedFromPocket", 0.0),
                            deductedFromSplurge = obj.optDouble("deductedFromSplurge", 0.0),
                            deductedFromVault = obj.optDouble("deductedFromVault", 0.0),
                            deductedFromTransport = obj.optDouble("deductedFromTransport", 0.0),
                            dateMillis = obj.optLong("dateMillis", 0L),
                            dateIso = obj.optString("dateIso", ""),
                            dayName = obj.optString("dayName", ""),
                            exactTime = obj.optString("exactTime", "")
                        )
                    )
                }
                transactionDao.insertAll(list)
            }

            if (root.has("workHistory")) {
                val workArray = root.getJSONArray("workHistory")
                val list = mutableListOf<WorkHistoryEntity>()
                for (i in 0 until workArray.length()) {
                    val obj = workArray.getJSONObject(i)
                    list.add(
                        WorkHistoryEntity(
                            source = obj.getString("source"),
                            amount = obj.getDouble("amount"),
                            dateStr = obj.getString("dateStr"),
                            dayName = obj.optString("dayName", ""),
                            exactTime = obj.optString("exactTime", ""),
                            dateMillis = obj.optLong("dateMillis", 0L)
                        )
                    )
                }
                workHistoryDao.insertAll(list)
            }

            if (root.has("debts")) {
                val debtArray = root.getJSONArray("debts")
                val list = mutableListOf<DebtEntity>()
                for (i in 0 until debtArray.length()) {
                    val obj = debtArray.getJSONObject(i)
                    list.add(
                        DebtEntity(
                            person = obj.getString("person"),
                            amount = obj.getDouble("amount"),
                            type = obj.getString("type"),
                            isSettled = obj.optInt("isSettled", 0),
                            dateStr = obj.optString("dateStr", ""),
                            dateMillis = obj.optLong("dateMillis", 0L)
                        )
                    )
                }
                debtDao.insertAll(list)
            }

            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                val list = mutableListOf<CategoryEntity>()
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    list.add(
                        CategoryEntity(
                            name = obj.getString("name"),
                            emoji = obj.getString("emoji"),
                            budgetLimit = obj.optDouble("budgetLimit", 0.0),
                            isDefault = obj.optInt("isDefault", 0)
                        )
                    )
                }
                categoryDao.deleteAll()
                categoryDao.insertAll(list)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun formatTwoDecimals(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
}
