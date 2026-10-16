package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DebtEntity
import com.example.data.TransactionEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ReportsDebtsScreen(
    uiState: MasrofiUiState,
    onAddDebt: (person: String, amount: Double, type: String) -> Unit,
    onSettleDebt: (Int) -> Unit,
    onSetPeriod: (String) -> Unit,
    onUpdateReminders: (daily: Boolean, threshold: Boolean, category: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var personName by remember { mutableStateOf("") }
    var debtAmountText by remember { mutableStateOf("") }
    var debtType by remember { mutableStateOf("give") }
    var showDebtConfirmDialog by remember { mutableStateOf(false) }
    var debtToSettle by remember { mutableStateOf<DebtEntity?>(null) }
    var debtFilterTab by remember { mutableIntStateOf(0) }

    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val currentDebtAmount = debtAmountText.toDoubleOrNull() ?: 0.0

    // Filter Debts
    val filteredDebts = remember(uiState.debts, debtFilterTab) {
        when (debtFilterTab) {
            1 -> uiState.debts.filter { it.isSettled == 0 }
            2 -> uiState.debts.filter { it.isSettled == 1 }
            else -> uiState.debts
        }
    }

    val totalGivenActive = uiState.debts.filter { it.type == "give" && it.isSettled == 0 }.sumOf { it.amount }
    val totalTakenActive = uiState.debts.filter { it.type == "take" && it.isSettled == 0 }.sumOf { it.amount }

    fun shareCsvReport() {
        val sb = StringBuilder()
        sb.append("تقرير مصروفي المالي\n")
        sb.append("الفترة: ${when (uiState.reportPeriod) { "week" -> "آخر أسبوع"; "month" -> "آخر 30 يوم"; else -> "كل السجلات" }}\n")
        sb.append("إجمالي المصروفات: ${uiState.totalSpentInPeriod.toInt()} ج.م\n\n")
        sb.append("المعرف,التاريخ,الوقت,اسم المصروف,التصنيف,المبلغ,من الكاش,من الحصالة,من التوفير,من المواصلات\n")

        for (tx in uiState.periodTransactions) {
            sb.append("${tx.id},${tx.dateIso},${tx.createdAt},\"${tx.title}\",\"${tx.category}\",${tx.amount.toInt()},${tx.deductedFromPocket.toInt()},${tx.deductedFromSplurge.toInt()},${tx.deductedFromVault.toInt()},${tx.deductedFromTransport.toInt()}\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير المصروفات CSV")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "التقارير ودفتر الديون",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "تصفية الفترات الزمنية، حدود التصنيفات، وتصدير التقارير CSV",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Period Filter Chips (Week / Month / All)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فترة التقرير:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "week" to "أسبوع",
                            "month" to "شهر",
                            "all" to "الكل"
                        ).forEach { (pKey, label) ->
                            FilterChip(
                                selected = uiState.reportPeriod == pKey,
                                onClick = { onSetPeriod(pKey) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = smallShape
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Expense Distribution Breakdown
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("expense_breakdown_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "توزيع المصروفات في الفترة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "إجمالي: ${uiState.totalSpentInPeriod.toInt()} ج.م",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Breakdown Cards & Budgets
                    if (uiState.categoryTotalsInPeriod.isEmpty()) {
                        Text(
                            text = "لا توجد مصروفات مسجلة في هذه الفترة المختارة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.categories.forEach { cat ->
                                val amount = uiState.categoryTotalsInPeriod[cat.name] ?: 0.0
                                val pct = if (uiState.totalSpentInPeriod > 0) (amount / uiState.totalSpentInPeriod * 100) else 0.0

                                Surface(
                                    shape = smallShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(cat.emoji, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = cat.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${amount.toInt()} ج.م",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "${pct.toInt()}%",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Category budget limit indicator
                                        if (cat.budgetLimit > 0) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val limitRatio = (amount / cat.budgetLimit).toFloat().coerceIn(0f, 1f)
                                            val isExceeded = amount >= cat.budgetLimit
                                            val isApproaching = amount >= cat.budgetLimit * 0.8 && !isExceeded

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (isExceeded) "🚨 تجاوزت الحد المالي (${cat.budgetLimit.toInt()} ج.م)"
                                                    else if (isApproaching) "⚠️ اقتربت من الحد (${cat.budgetLimit.toInt()} ج.م)"
                                                    else "حد الميزانية: ${cat.budgetLimit.toInt()} ج.م",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isExceeded) MaterialTheme.colorScheme.error else if (isApproaching) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text("${amount.toInt()}/${cat.budgetLimit.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Requirement 8: Share CSV report button
                    Button(
                        onClick = { shareCsvReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير ومشاركة تقرير الفترة (CSV / نص)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section 2: Debts Summary Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ليّا بره (سلّفت)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalGivenActive.toInt()} ج.م",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("عليّا للناس (استلفت)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalTakenActive.toInt()} ج.م",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Section 3: Add New Debt Form
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تسجيل سلفة أو دين جديد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("اسم الشخص") },
                        placeholder = { Text("مثلاً: أحمد، مصطفى، صاحب السوبرماركت...") },
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("debt_person_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = debtAmountText,
                        onValueChange = { debtAmountText = it },
                        label = { Text("المبلغ (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("debt_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = debtType == "give",
                            onClick = { debtType = "give" },
                            label = { Text("أنا سلّفت (خرجت من جيبي)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = smallShape,
                            modifier = Modifier.weight(1f).testTag("debt_type_give")
                        )

                        FilterChip(
                            selected = debtType == "take",
                            onClick = { debtType = "take" },
                            label = { Text("أنا استلفت (دخلت جيبي)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = smallShape,
                            modifier = Modifier.weight(1f).testTag("debt_type_take")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentDebtAmount > 0) {
                                showDebtConfirmDialog = true
                            }
                        },
                        enabled = currentDebtAmount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (debtType == "give") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_debt_btn")
                    ) {
                        Text(
                            text = if (debtType == "give") "تسجيل سلفة وخصم ${currentDebtAmount.toInt()} ج.م من الكاش"
                            else "تسجيل دين وإضافة ${currentDebtAmount.toInt()} ج.م للكاش",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 4: Reminders & Alerts (Requirement 8)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التنبيهات والتذكيرات الاختيارية:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تذكير مسائي بتسجيل المصروفات اليومية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = uiState.appState.dailyReminderEnabled == 1,
                            onCheckedChange = { checked ->
                                onUpdateReminders(checked, uiState.appState.thresholdAlertEnabled == 1, uiState.appState.categoryAlertEnabled == 1)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تنبيه عند استهلاك 80% من المسموح اليومي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = uiState.appState.thresholdAlertEnabled == 1,
                            onCheckedChange = { checked ->
                                onUpdateReminders(uiState.appState.dailyReminderEnabled == 1, checked, uiState.appState.categoryAlertEnabled == 1)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تنبيه عند الاقتراب أو تجاوز حد أي تصنيف", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = uiState.appState.categoryAlertEnabled == 1,
                            onCheckedChange = { checked ->
                                onUpdateReminders(uiState.appState.dailyReminderEnabled == 1, uiState.appState.thresholdAlertEnabled == 1, checked)
                            }
                        )
                    }
                }
            }
        }

        // Section 5: Debts List with Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دفتر إدارة الديون والسلف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("الكل", "نشطة", "مسددة").forEachIndexed { index, title ->
                        Surface(
                            shape = smallShape,
                            color = if (debtFilterTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { debtFilterTab = index }.testTag("debt_tab_$index")
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (debtFilterTab == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (filteredDebts.isEmpty()) {
            item {
                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("لا توجد ديون في هذه الفئة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(items = filteredDebts, key = { it.id }) { debt ->
                DebtCard(debt = debt, onSettleClick = { debtToSettle = debt })
            }
        }
    }

    // Confirmation dialog before adding debt
    if (showDebtConfirmDialog) {
        val finalPerson = personName.ifBlank { "شخص" }
        val actionDesc = if (debtType == "give")
            "سلّفت $finalPerson مبلغ ${currentDebtAmount.toInt()} ج.م (سيتم خصمه من كاش الجيب)"
        else
            "استلفت من $finalPerson مبلغ ${currentDebtAmount.toInt()} ج.م (سيتم إضافته لكاش الجيب)"

        ConfirmationDialog(
            title = "تأكيد تسجيل السلفة / الدين",
            message = "هل تريد تأكيد: $actionDesc؟",
            confirmText = "تأكيد التسجيل",
            cancelText = "تراجع",
            onConfirm = {
                onAddDebt(finalPerson, currentDebtAmount, debtType)
                personName = ""
                debtAmountText = ""
            },
            onDismiss = { showDebtConfirmDialog = false }
        )
    }

    // Confirmation dialog before settling debt
    debtToSettle?.let { debt ->
        val settleAction = if (debt.type == "give")
            "استرداد مبلغ ${debt.amount.toInt()} ج.م من ${debt.person} وإضافته لكاش جيبك"
        else
            "سداد دين ${debt.person} بمبلغ ${debt.amount.toInt()} ج.م وخصمه من كاش جيبك"

        ConfirmationDialog(
            title = "تأكيد سداد / تسوية الدين",
            message = "هل تمت التسوية بالفعل؟\nسيتم: $settleAction.",
            confirmText = "تم السداد والتسوية",
            cancelText = "إلغاء",
            onConfirm = {
                onSettleDebt(debt.id)
                debtToSettle = null
            },
            onDismiss = { debtToSettle = null }
        )
    }
}

@Composable
fun DebtCard(
    debt: DebtEntity,
    onSettleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.5f).toInt().coerceAtLeast(3).dp)
    val isGive = debt.type == "give"
    val isSettled = debt.isSettled == 1

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.person,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = smallShape,
                            color = if (isGive) NeonEmerald.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                if (isGive) NeonEmerald.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (isGive) "سلفة منّي (لك عنده)" else "دين عليّ (له عندك)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGive) NeonEmerald else NeonAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = smallShape,
                            color = if (isSettled) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                if (isSettled) NeonCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = if (isSettled) "تمت التسوية بنجاح ✅" else "قيد الانتظار ⏳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSettled) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${debt.amount.toInt()} ج.م",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isGive) NeonEmerald else NeonCoral
                    )
                    if (!isSettled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onSettleClick,
                            shape = smallShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("settle_debt_button_${debt.id}")
                        ) {
                            Text("تسوية الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
