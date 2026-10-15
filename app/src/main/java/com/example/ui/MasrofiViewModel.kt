package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppStateEntity
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

data class MasrofiUiState(
    val appState: AppStateEntity = AppStateEntity(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val workHistory: List<WorkHistoryEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val dailyAllowance: Double = 50.0,
    val burnRatePercentage: Float = 0f, // 0.0 to 1.0 (or > 1.0)
    val remainingTodayAllowance: Double = 50.0,
    val isLoading: Boolean = false
)

class MasrofiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MasrofiRepository

    init {
        val database = MasrofiDatabase.getInstance(application)
        repository = MasrofiRepository(
            database.appStateDao(),
            database.transactionDao(),
            database.workHistoryDao(),
            database.debtDao()
        )

        // Initialize state & check rollover
        viewModelScope.launch {
            repository.getOrCreateAppState()
            repository.checkAndPerformMidnightRollover()
        }
    }

    private val _vaultWarning = MutableStateFlow<String?>(null)
    val vaultWarning: StateFlow<String?> = _vaultWarning.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val uiState: StateFlow<MasrofiUiState> = combine(
        repository.appState,
        repository.recentTransactions,
        repository.allTransactions,
        repository.workHistory,
        repository.debts
    ) { state, recentTx, allTx, work, debtsList ->
        val current = state ?: AppStateEntity()
        val allowance = if (current.daysLeft > 0) current.pocketCash / current.daysLeft else 0.0
        val burnRate = if (allowance > 0) (current.todaySpent / allowance).toFloat() else 0f
        val remToday = kotlin.math.max(0.0, allowance - current.todaySpent)

        MasrofiUiState(
            appState = current,
            recentTransactions = recentTx,
            allTransactions = allTx,
            workHistory = work,
            debts = debtsList,
            dailyAllowance = allowance,
            burnRatePercentage = burnRate,
            remainingTodayAllowance = remToday,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MasrofiUiState(isLoading = true)
    )

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
                _vaultWarning.value = "⚠️ تحذير مالي صارم: التوفير الإجباري بدأ في التآكل!\n\nلقد تجاوزت المسموح اليومي ونفدت حصالة الوجبة الغالية وكاش الجيب بالكامل. تم سحب العجز من التوفير الإجباري المجمد!"
            }
            _toastEvent.emit(result.message)
        }
    }

    fun rollbackExpense(transactionId: Int) {
        viewModelScope.launch {
            repository.rollbackExpense(transactionId)
            _toastEvent.emit("تم التراجع عن المصروف واستعادة المبلغ بنجاح")
        }
    }

    fun endDayNow() {
        viewModelScope.launch {
            val surplus = repository.manualEndDay()
            if (surplus > 0) {
                _toastEvent.emit("عاش! تم إنهاء اليوم وترحيل فائض ${String.format("%.1f", surplus)} ج.م لحصالة الوجبة الغالية 🎉")
            } else {
                _toastEvent.emit("تم إنهاء اليوم وتحديث الدورة المالية")
            }
        }
    }

    fun toggleInstituteDay(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleInstituteDay(enabled)
            if (enabled) {
                _toastEvent.emit("تم تفعيل يوم المعهد (حجز 40 ج.م للمواصلات)")
            } else {
                _toastEvent.emit("تم إلغاء يوم المعهد")
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

    fun resetCycle(totalReceived: Double, days: Int, vaultPercentage: Double) {
        viewModelScope.launch {
            repository.resetCycle(totalReceived, days, vaultPercentage)
            _toastEvent.emit("تم بدء دورة جديدة بميزانية $totalReceived ج.م لمدة $days يوم")
        }
    }
}
