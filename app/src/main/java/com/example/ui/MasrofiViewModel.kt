package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppStateEntity
import com.example.data.CategoryEntity
import com.example.data.DebtEntity
import com.example.data.MasrofiDatabase
import com.example.data.MasrofiRepository
import com.example.data.TransactionEntity
import com.example.data.WorkHistoryEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MasrofiUiState(
    val appState: AppStateEntity = AppStateEntity(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val workHistory: List<WorkHistoryEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val dailyAllowance: Double = 50.0,
    val burnRatePercentage: Float = 0f,
    val remainingTodayAllowance: Double = 50.0,
    val reportPeriod: String = "month", // "week", "month", "all"
    val periodTransactions: List<TransactionEntity> = emptyList(),
    val categoryTotalsInPeriod: Map<String, Double> = emptyMap(),
    val totalSpentInPeriod: Double = 0.0,
    val canEndDayToday: Boolean = true,
    val approachingCategories: List<CategoryEntity> = emptyList(),
    val exceededCategories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

class MasrofiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MasrofiRepository
    private val selectedPeriod = MutableStateFlow("month")

    init {
        val database = MasrofiDatabase.getInstance(application)
        repository = MasrofiRepository(
            database.appStateDao(),
            database.transactionDao(),
            database.workHistoryDao(),
            database.debtDao(),
            database.categoryDao()
        )

        viewModelScope.launch {
            repository.getOrCreateAppState()
            repository.checkAndPerformMidnightRollover()
        }
    }

    private val _vaultWarning = MutableStateFlow<String?>(null)
    val vaultWarning: StateFlow<String?> = _vaultWarning.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val txFlow = combine(
        repository.appState,
        repository.recentTransactions,
        repository.allTransactions
    ) { state, recentTx, allTx ->
        Triple(state ?: AppStateEntity(), recentTx, allTx)
    }

    private val dataFlow = combine(
        repository.workHistory,
        repository.debts,
        repository.categories
    ) { work, debts, cats ->
        Triple(work, debts, cats)
    }

    val uiState: StateFlow<MasrofiUiState> = combine(
        txFlow,
        dataFlow,
        selectedPeriod
    ) { (state, recentTx, allTx), (work, debtsList, catsList), period ->
        val allowance = if (state.daysLeft > 0) state.pocketCash / state.daysLeft else 0.0
        val burnRate = if (allowance > 0) (state.todaySpent / allowance).toFloat() else 0f
        val remToday = kotlin.math.max(0.0, allowance - state.todaySpent)
        val todayStr = repository.getTodayDateStr()
        val canEndDay = (state.lastClosedDate != todayStr)

        // Filter transactions based on selected period
        val periodTx = filterTransactionsByPeriod(allTx, period)
        val catMap = mutableMapOf<String, Double>()
        for (tx in periodTx) {
            catMap[tx.category] = (catMap[tx.category] ?: 0.0) + tx.amount
        }
        val totalPeriod = catMap.values.sum()

        // Check category budget limits
        val approaching = mutableListOf<CategoryEntity>()
        val exceeded = mutableListOf<CategoryEntity>()
        for (cat in catsList) {
            if (cat.budgetLimit > 0.0) {
                val spent = catMap[cat.name] ?: 0.0
                if (spent >= cat.budgetLimit) {
                    exceeded.add(cat)
                } else if (spent >= cat.budgetLimit * 0.8) {
                    approaching.add(cat)
                }
            }
        }

        MasrofiUiState(
            appState = state,
            recentTransactions = recentTx,
            allTransactions = allTx,
            workHistory = work,
            debts = debtsList,
            categories = catsList,
            dailyAllowance = allowance,
            burnRatePercentage = burnRate,
            remainingTodayAllowance = remToday,
            reportPeriod = period,
            periodTransactions = periodTx,
            categoryTotalsInPeriod = catMap,
            totalSpentInPeriod = totalPeriod,
            canEndDayToday = canEndDay,
            approachingCategories = approaching,
            exceededCategories = exceeded,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MasrofiUiState(isLoading = true)
    )

    fun setReportPeriod(period: String) {
        selectedPeriod.value = period
    }

    private fun filterTransactionsByPeriod(list: List<TransactionEntity>, period: String): List<TransactionEntity> {
        val cal = Calendar.getInstance()

        return when (period) {
            "week" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val cutoff = cal.timeInMillis
                list.filter { tx ->
                    if (tx.dateMillis > 0) tx.dateMillis >= cutoff
                    else tx.dateTag == "اليوم" || tx.dateTag == "أمس"
                }
            }
            "month" -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val cutoff = cal.timeInMillis
                list.filter { tx ->
                    if (tx.dateMillis > 0) tx.dateMillis >= cutoff
                    else true
                }
            }
            else -> list // "all"
        }
    }

    fun dismissVaultWarning() {
        _vaultWarning.value = null
    }

    fun recordExpense(
        title: String,
        amount: Double,
        category: String,
        isYesterday: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.recordExpense(title, amount, category, isYesterday)
            if (result.vaultWarned) {
                _vaultWarning.value = "⚠️ تحذير مالي صارم: التوفير الإجباري بدأ في التآكل!\n\nلقد تجاوزت المسموح اليومي وحصالة الوجبة الغالية ونفد كاش الجيب بالكامل. تم سحب العجز من التوفير الإجباري المجمد!"
            }
            _toastEvent.emit(result.message)
        }
    }

    fun updateExpense(
        id: Int,
        title: String,
        amount: Double,
        category: String
    ) {
        viewModelScope.launch {
            val result = repository.updateExpense(id, title, amount, category)
            _toastEvent.emit(result.message)
        }
    }

    fun rollbackExpense(transactionId: Int, legacyDestination: String = "pocket") {
        viewModelScope.launch {
            repository.rollbackExpense(transactionId, legacyDestination)
            _toastEvent.emit("تم التراجع عن المصروف وإعادة المبالغ لمصادرها الأصلية بنجاح")
        }
    }

    fun endDayNow() {
        viewModelScope.launch {
            val result = repository.manualEndDay()
            _toastEvent.emit(result.message)
        }
    }

    fun toggleInstituteDay(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleInstituteDay(enabled)
            if (enabled) {
                _toastEvent.emit("تم تفعيل يوم المعهد: حجز 40 ج.م للمواصلات تغطي مشاوير اليوم تلقائياً")
            } else {
                _toastEvent.emit("تم إلغاء يوم المعهد وإعادة المبلغ غير المستهلك")
            }
        }
    }

    fun depositFreelance(source: String, amount: Double) {
        viewModelScope.launch {
            repository.depositFreelanceIncome(source, amount)
            _toastEvent.emit("تم إيداع $amount ج.م بنسبة 100% في كاش الجيب بدون أي استقطاع!")
        }
    }

    fun addDebt(person: String, amount: Double, type: String) {
        viewModelScope.launch {
            repository.addDebt(person, amount, type)
            if (type == "give") {
                _toastEvent.emit("تم تسجيل سلفة $amount ج.م لـ $person وخصمها من الكاش")
            } else {
                _toastEvent.emit("تم تسجيل دين $amount ج.م من $person وإضافتها للكاش")
            }
        }
    }

    fun settleDebt(debtId: Int) {
        viewModelScope.launch {
            repository.settleDebt(debtId)
            _toastEvent.emit("تمت تسوية الدين وتحديث كاش الجيب بنجاح")
        }
    }

    fun resetCycle(totalReceived: Double, days: Int, savingsType: String, savingsValue: Double) {
        viewModelScope.launch {
            repository.resetCycle(totalReceived, days, savingsType, savingsValue)
            _toastEvent.emit("تم بدء دورة جديدة بميزانية $totalReceived ج.م لمدة $days يوم")
        }
    }

    fun addCategory(name: String, emoji: String, budgetLimit: Double) {
        viewModelScope.launch {
            repository.addCategory(name, emoji, budgetLimit)
            _toastEvent.emit("تمت إضافة التصنيف '$name' بنجاح")
        }
    }

    fun updateCategory(id: Int, name: String, emoji: String, budgetLimit: Double) {
        viewModelScope.launch {
            repository.updateCategory(id, name, emoji, budgetLimit)
            _toastEvent.emit("تم تحديث بيانات التصنيف بنجاح")
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _toastEvent.emit("تم حذف التصنيف المخصص")
        }
    }

    fun updateReminderSettings(daily: Boolean, threshold: Boolean, category: Boolean) {
        viewModelScope.launch {
            repository.updateReminderSettings(daily, threshold, category)
            _toastEvent.emit("تم حفظ تفضيلات التنبيهات والتذكيرات")
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onResult(json)
        }
    }

    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            onComplete(success)
            if (success) {
                _toastEvent.emit("تمت استعادة النسخة الاحتياطية وتحديث السجلات بنجاح")
            } else {
                _toastEvent.emit("فشلت استعادة النسخة، تأكد من صحة ملف البيانات")
            }
        }
    }
}
