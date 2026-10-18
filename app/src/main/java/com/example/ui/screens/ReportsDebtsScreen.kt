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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.CurrencyConfig
import com.example.data.DateTimeUtils
import com.example.data.DebtEntity
import com.example.data.TransactionEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*

@Composable
fun ReportsDebtsScreen(
    uiState: MasrofiUiState,
    currentCurrency: String,
    currentLanguage: String,
    onAddDebt: (person: String, amount: Double, type: String) -> Unit,
    onSettleDebt: (Int) -> Unit,
    onSetPeriod: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = currentLanguage != "en"

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
        sb.append(if (isAr) "تقرير مصروفي المالي\n" else "Masrofi Financial Report\n")
        sb.append("${if (isAr) "الفترة" else "Period"}: ${when (uiState.reportPeriod) { "week" -> if (isAr) "آخر أسبوع" else "Last 7 Days"; "month" -> if (isAr) "آخر 30 يوم" else "Last 30 Days"; else -> if (isAr) "كل السجلات" else "All Time" }}\n")
        sb.append("${if (isAr) "إجمالي المصروفات" else "Total Expenses"}: ${CurrencyConfig.format(uiState.totalSpentInPeriod, currentCurrency, isAr)}\n\n")
        sb.append(if (isAr) "المعرف,التاريخ,اليوم,الوقت,اسم المصروف,التصنيف,المبلغ,العملة\n" else "ID,Date,Day,Time,Title,Category,Amount,Currency\n")

        for (tx in uiState.periodTransactions) {
            val dName = if (tx.dayName.isNotBlank()) tx.dayName else tx.dateTag
            val tTime = if (tx.exactTime.isNotBlank()) tx.exactTime else tx.createdAt
            sb.append("${tx.id},${tx.dateIso},$dName,$tTime,\"${tx.title}\",\"${tx.category}\",${tx.amount.toInt()},${CurrencyConfig.getSymbol(currentCurrency, isAr)}\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, if (isAr) "مشاركة تقرير المصروفات CSV" else "Share Expenses CSV Report")
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
                text = if (isAr) "التقارير ودفتر الديون" else "Reports & Debt Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAr) "تصفية الفترات الزمنية، حدود التصنيفات، وتصدير التقارير CSV" else "Filter periods, track category limits, and export CSV",
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
                modifier = Modifier.fillMaxWidth().testTag("period_filter_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "تحديد فترة التقرير:" else "Report Period:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("week", if (isAr) "أسبوع" else "Week", "7d"),
                            Triple("month", if (isAr) "شهر (30 يوم)" else "Month (30d)", "30d"),
                            Triple("all", if (isAr) "كل السجلات" else "All Time", "all")
                        ).forEach { (key, label, tag) ->
                            val isSelected = uiState.reportPeriod == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetPeriod(key) },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = smallShape,
                                modifier = Modifier.weight(1f).testTag("period_chip_$tag")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Spend Total Card
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAr) "إجمالي المصروف في الفترة:" else "Total Spent in Period:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyConfig.format(uiState.totalSpentInPeriod, currentCurrency, isAr),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Category Breakdown & Limits
                    if (uiState.categoryTotalsInPeriod.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isAr) "توزيع المصروفات حسب التصنيف:" else "Expenses by Category:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val total = uiState.totalSpentInPeriod.coerceAtLeast(1.0)
                        uiState.categoryTotalsInPeriod.forEach { (categoryName, amount) ->
                            val ratio = (amount / total).toFloat().coerceIn(0f, 1f)
                            val cat = uiState.categories.find { it.name == categoryName }
                            val isApproaching = cat != null && cat.budgetLimit > 0.0 && amount >= cat.budgetLimit * 0.8 && amount < cat.budgetLimit
                            val isExceeded = cat != null && cat.budgetLimit > 0.0 && amount >= cat.budgetLimit

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cat?.emoji ?: "📁"} $categoryName",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${CurrencyConfig.format(amount, currentCurrency, isAr)} (${(ratio * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(smallShape),
                                    color = if (isExceeded) MaterialTheme.colorScheme.error else if (isApproaching) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                if (cat != null && cat.budgetLimit > 0.0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isExceeded) (if (isAr) "⚠️ تم تجاوز الحد المالي المخصص!" else "⚠️ Exceeded budget limit!") else if (isApproaching) (if (isAr) "⚡ اقتربت من الحد الأقصى (80%)" else "⚡ Approaching limit (80%)") else (if (isAr) "ضمن الحد الأقصى للتصنيف" else "Within budget limit"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExceeded) MaterialTheme.colorScheme.error else if (isApproaching) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text("${amount.toInt()}/${cat.budgetLimit.toInt()} ${CurrencyConfig.getSymbol(currentCurrency, isAr)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Share CSV report button
                    Button(
                        onClick = { shareCsvReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("share_csv_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "تصدير ومشاركة تقرير الفترة (CSV / نص)" else "Export & Share Report (CSV / Text)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text(if (isAr) "ليّا بره (سلّفت)" else "Money Lent (Receivable)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyConfig.format(totalGivenActive, currentCurrency, isAr),
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
                        Text(if (isAr) "عليّا للناس (استلفت)" else "Money Borrowed (Payable)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyConfig.format(totalTakenActive, currentCurrency, isAr),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Section 3: Add New Debt Form with Timestamp
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "تسجيل سلفة أو دين جديد" else "Record New Debt / Loan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date & Time indicator for Debt
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isAr) "التاريخ:" else "Date:"} ${DateTimeUtils.formatFullDateTime(System.currentTimeMillis(), DateTimeUtils.getTodayIso(), isAr)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text(if (isAr) "اسم الشخص" else "Person's Name") },
                        placeholder = { Text(if (isAr) "مثلاً: أحمد، مصطفى، صاحب السوبرماركت..." else "e.g., Ahmed, Store Owner...") },
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("debt_person_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val symbol = CurrencyConfig.getSymbol(currentCurrency, isAr)
                    OutlinedTextField(
                        value = debtAmountText,
                        onValueChange = { debtAmountText = it },
                        label = { Text("${if (isAr) "المبلغ" else "Amount"} ($symbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("debt_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Debt Type Selector: Give (Lent) vs Take (Borrowed)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = smallShape,
                            color = if (debtType == "give") MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (debtType == "give") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { debtType = "give" }
                                .testTag("debt_type_give")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "سلّفت (خرجت من جيبي)" else "Lent (Money out)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Surface(
                            shape = smallShape,
                            color = if (debtType == "take") MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (debtType == "take") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { debtType = "take" }
                                .testTag("debt_type_take")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "استلفت (دخلت جيبي)" else "Borrowed (Money in)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentDebtAmount > 0 && personName.isNotBlank()) {
                                showDebtConfirmDialog = true
                            }
                        },
                        enabled = currentDebtAmount > 0 && personName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_debt_btn")
                    ) {
                        Text(if (isAr) "حفظ وتحديث الرصيد" else "Save & Update Balance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 4: Debts List with Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "دفتر إدارة الديون والسلف" else "Debt Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        if (isAr) "الكل" else "All",
                        if (isAr) "النشطة" else "Active",
                        if (isAr) "المسواة" else "Settled"
                    ).forEachIndexed { idx, label ->
                        val isSelected = debtFilterTab == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { debtFilterTab = idx },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = smallShape
                        )
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
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🤝", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAr) "لا توجد سجلات ديون في هذا القسم" else "No debt records in this filter",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredDebts, key = { it.id }) { debt ->
                DebtCard(
                    debt = debt,
                    currentCurrency = currentCurrency,
                    isArabic = isAr,
                    onSettleClick = { debtToSettle = debt }
                )
            }
        }
    }

    // Confirmation dialog before adding debt
    if (showDebtConfirmDialog) {
        val actionDesc = if (debtType == "give")
            if (isAr) "تسجيل سلفة لـ '$personName' بمبلغ ${CurrencyConfig.format(currentDebtAmount, currentCurrency, isAr)} وخصمها من كاش جيبك"
            else "Lend ${CurrencyConfig.format(currentDebtAmount, currentCurrency, isAr)} to '$personName' and deduct from Pocket Cash"
        else
            if (isAr) "تسجيل استلاف من '$personName' بمبلغ ${CurrencyConfig.format(currentDebtAmount, currentCurrency, isAr)} وإضافتها لكاش جيبك"
            else "Borrow ${CurrencyConfig.format(currentDebtAmount, currentCurrency, isAr)} from '$personName' and add to Pocket Cash"

        ConfirmationDialog(
            title = if (isAr) "تأكيد تسجيل المعاملة" else "Confirm Record",
            message = "$actionDesc.",
            confirmText = if (isAr) "تأكيد" else "Confirm",
            cancelText = if (isAr) "مراجعة" else "Cancel",
            onConfirm = {
                onAddDebt(personName, currentDebtAmount, debtType)
                personName = ""
                debtAmountText = ""
            },
            onDismiss = { showDebtConfirmDialog = false }
        )
    }

    // Confirmation dialog before settling debt
    debtToSettle?.let { debt ->
        val settleAction = if (debt.type == "give")
            if (isAr) "استرداد مبلغ ${CurrencyConfig.format(debt.amount, currentCurrency, isAr)} من ${debt.person} وإضافته لكاش جيبك"
            else "Collect ${CurrencyConfig.format(debt.amount, currentCurrency, isAr)} from ${debt.person} and add to Pocket Cash"
        else
            if (isAr) "سداد دين ${debt.person} بمبلغ ${CurrencyConfig.format(debt.amount, currentCurrency, isAr)} وخصمه من كاش جيبك"
            else "Pay debt of ${CurrencyConfig.format(debt.amount, currentCurrency, isAr)} to ${debt.person} and deduct from Pocket Cash"

        ConfirmationDialog(
            title = if (isAr) "تأكيد سداد / تسوية الدين" else "Confirm Debt Settlement",
            message = "$settleAction.",
            confirmText = if (isAr) "تم السداد والتسوية" else "Confirm Settlement",
            cancelText = if (isAr) "إلغاء" else "Cancel",
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
    currentCurrency: String,
    isArabic: Boolean,
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
                    if (debt.dateStr.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = debt.dateStr,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                text = if (isGive) (if (isArabic) "سلفة منّي (لك عنده)" else "Lent") else (if (isArabic) "دين عليّ (له عندك)" else "Borrowed"),
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
                                text = if (isSettled) (if (isArabic) "تمت التسوية بنجاح ✅" else "Settled ✅") else (if (isArabic) "قيد الانتظار ⏳" else "Pending ⏳"),
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
                        text = CurrencyConfig.format(debt.amount, currentCurrency, isArabic),
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
                            Text(if (isArabic) "تسوية الآن" else "Settle Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
