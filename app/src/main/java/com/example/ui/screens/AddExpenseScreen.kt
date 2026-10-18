package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyConfig
import com.example.data.DateTimeUtils
import com.example.ui.MasrofiUiState
import com.example.ui.components.CategoryManagementModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*

data class QuickExpenseItem(
    val title: String,
    val amount: Int,
    val category: String,
    val emoji: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    uiState: MasrofiUiState,
    currentCurrency: String,
    currentLanguage: String,
    onRecordExpense: (title: String, amount: Double, category: String, isYesterday: Boolean) -> Unit,
    onAddCategory: (String, String, Double) -> Unit,
    onDeleteCategory: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = currentLanguage != "en"
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("طعام ومشروبات") }
    var isYesterdayExpense by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCategoryModal by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val quickItems = listOf(
        QuickExpenseItem(if (isAr) "دليفري" else "Delivery", 15, "طعام ومشروبات", "🛵"),
        QuickExpenseItem(if (isAr) "كانز" else "Soda", 15, "طعام ومشروبات", "🥤"),
        QuickExpenseItem(if (isAr) "مواصلات" else "Transit", 10, "مواصلات", "🚌"),
        QuickExpenseItem(if (isAr) "قهوة" else "Coffee", 12, "طعام ومشروبات", "☕"),
        QuickExpenseItem(if (isAr) "ساندوتش" else "Sandwich", 25, "طعام ومشروبات", "🥪"),
        QuickExpenseItem(if (isAr) "مياه" else "Water", 7, "طعام ومشروبات", "💧")
    )

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    // Dynamic waterfall calculation prediction
    val allowance = uiState.dailyAllowance
    val remainingToday = uiState.remainingTodayAllowance
    val splurge = uiState.appState.splurgePool
    val pocket = uiState.appState.pocketCash
    val vault = uiState.appState.vault
    val transportReserve = uiState.appState.transportReserve
    val isInstituteDay = uiState.appState.isInstituteDay == 1

    val waterfallExplanation = remember(parsedAmount, isYesterdayExpense, allowance, remainingToday, splurge, pocket, vault, selectedCategory, transportReserve, isInstituteDay, currentCurrency) {
        val curr = currentCurrency
        if (parsedAmount <= 0) {
            if (isAr) "أدخل المبلغ لمعاينة مسار الخصم عبر الشلال المالي الصارم." else "Enter amount to preview automatic deduction waterfall."
        } else if (isYesterdayExpense) {
            if (splurge >= parsedAmount) {
                if (isAr) "سحب كامل من حصالة الوجبة الغالية (${CurrencyConfig.format(parsedAmount, curr, isAr)}) دون المساس بميزانية اليوم!"
                else "Fully deducted from Surplus Pool (${CurrencyConfig.format(parsedAmount, curr, isAr)}) without affecting today's budget!"
            } else {
                val fromSplurge = splurge
                val fromPocket = parsedAmount - fromSplurge
                if (isAr) "سحب ${CurrencyConfig.format(fromSplurge, curr, isAr)} من حصالة الفائض + ${CurrencyConfig.format(fromPocket, curr, isAr)} من كاش الجيب دون المساس بميزانية اليوم."
                else "Deducted ${CurrencyConfig.format(fromSplurge, curr, isAr)} from Surplus + ${CurrencyConfig.format(fromPocket, curr, isAr)} from Pocket cash."
            }
        } else {
            if (selectedCategory == "مواصلات" && isInstituteDay && transportReserve > 0) {
                if (transportReserve >= parsedAmount) {
                    if (isAr) "يُخصم كامل المبلغ (${CurrencyConfig.format(parsedAmount, curr, isAr)}) من محجوز مواصلات المعهد المحمي دون استهلاك ميزانية الأكل والشرب."
                    else "Fully deducted (${CurrencyConfig.format(parsedAmount, curr, isAr)}) from protected transit reserve."
                } else {
                    val fromTrans = transportReserve
                    val rem = parsedAmount - fromTrans
                    if (isAr) "يُخصم ${CurrencyConfig.format(fromTrans, curr, isAr)} من محجوز المواصلات، والمتبقي (${CurrencyConfig.format(rem, curr, isAr)}) يخضع لميزانية اليوم والشلال."
                    else "Deducted ${CurrencyConfig.format(fromTrans, curr, isAr)} from transit reserve, remaining (${CurrencyConfig.format(rem, curr, isAr)}) cascades."
                }
            } else if (parsedAmount <= remainingToday) {
                if (isAr) "ضمن المسموح اليومي: سيخصم المبلغ بالكامل (${CurrencyConfig.format(parsedAmount, curr, isAr)}) من ميزانية اليوم وكاش الجيب."
                else "Within daily allowance: Fully deducted (${CurrencyConfig.format(parsedAmount, curr, isAr)}) from today's allowance & cash."
            } else {
                val deficit = parsedAmount - remainingToday
                if (splurge >= deficit) {
                    if (isAr) "تجاوز للمسموح اليومي: سيغطى العجز (${CurrencyConfig.format(deficit, curr, isAr)}) أولاً من حصالة الوجبة الغالية."
                    else "Exceeds daily allowance: Deficit (${CurrencyConfig.format(deficit, curr, isAr)}) covered from Surplus Pool."
                } else if (splurge + pocket >= deficit) {
                    val remDef = deficit - splurge
                    if (isAr) "تجاوز للمسموح وحصالة الوجبة: سيخصم ${CurrencyConfig.format(splurge, curr, isAr)} من الحصالة و ${CurrencyConfig.format(remDef, curr, isAr)} من كاش الجيب مباشرة."
                    else "Exceeds allowance & surplus: ${CurrencyConfig.format(splurge, curr, isAr)} from Surplus + ${CurrencyConfig.format(remDef, curr, isAr)} from Pocket cash."
                } else {
                    if (isAr) "⚠️ تحذير مالي: سينفد كاش الجيب وسيبدأ الخصم من التوفير الإجباري المجمد!"
                    else "⚠️ Warning: Cash will deplete and deduction will tap into Mandatory Savings!"
                }
            }
        }
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
                text = if (isAr) "خصم صرف جديد" else "Record New Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAr) "سجل مصروفاتك مع توثيق اليوم والتاريخ والوقت تلقائياً" else "Log expense with exact day, date, and timestamp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Exact Date, Day, and Time Indicator Card
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("expense_datetime_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAr) "تاريخ وتوقيت تسجيل المصروف:" else "Expense Date & Timestamp:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val liveDateText = if (isYesterdayExpense) {
                                "${if (isAr) "أمس" else "Yesterday"} (${DateTimeUtils.getDayOfWeekFromIso(DateTimeUtils.getYesterdayIso(), isAr)} ${DateTimeUtils.getYesterdayIso()})"
                            } else {
                                DateTimeUtils.formatFullDateTime(System.currentTimeMillis(), DateTimeUtils.getTodayIso(), isAr)
                            }
                            Text(
                                text = liveDateText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isYesterdayExpense) (if (isAr) "أمس" else "Yesterday") else (if (isAr) "اليوم" else "Today"),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Chips section
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isAr) "أصناف متكررة سريعة (نقرة واحدة للتعبئة):" else "Quick presets (Tap to fill):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickItems.forEach { item ->
                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clickable {
                                        title = item.title
                                        amountText = item.amount.toString()
                                        selectedCategory = item.category
                                        amountError = null
                                    }
                                    .testTag("quick_chip_${item.title}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.title} +${item.amount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Expense Form Fields
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isAr) "اسم الصنف أو المصروف" else "Expense Description") },
                        placeholder = { Text(if (isAr) "مثلاً: قهوة، غداء، مواصلات..." else "e.g., Coffee, Lunch, Uber...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Input (Whole Number Only)
                    val symbol = CurrencyConfig.getSymbol(currentCurrency, isAr)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            amountText = input
                            amountError = null
                        },
                        label = { Text(if (isAr) "المبلغ بأعداد صحيحة ($symbol)" else "Amount in whole numbers ($symbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = amountError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                    )

                    amountError?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) "التصنيف:" else "Category:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showCategoryModal = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "إدارة التصنيفات" else "Manage Categories", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat.name,
                                onClick = { selectedCategory = cat.name },
                                label = {
                                    Text(
                                        text = "${cat.emoji} ${cat.name}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = smallShape,
                                modifier = Modifier.testTag("cat_chip_${cat.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switch for forgotten expense from yesterday
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "أمس",
                                    tint = if (isYesterdayExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isAr) "مصروف منسي من أمس (أثر رجعي)" else "Retroactive Expense from Yesterday",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isYesterdayExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isAr) "لا يمس ميزانية اليوم الحالية ويخصم من الحصالة/الكاش" else "Does not affect today's allowance, deducted from surplus/cash",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isYesterdayExpense,
                                onCheckedChange = { isYesterdayExpense = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                                    checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("yesterday_expense_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Waterfall Engine prediction box
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (parsedAmount > remainingToday && !isYesterdayExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (parsedAmount > remainingToday && !isYesterdayExpense) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (parsedAmount > remainingToday && !isYesterdayExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = waterfallExplanation,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button with Strict Whole Number Validation
                    Button(
                        onClick = {
                            val trimmed = amountText.trim()
                            if (trimmed.isEmpty()) {
                                amountError = if (isAr) "يرجى إدخال مبلغ المصروف" else "Please enter expense amount"
                                return@Button
                            }
                            if (trimmed.contains(".") || trimmed.contains(",")) {
                                val parts = trimmed.split('.', ',')
                                if (parts.size > 1 && parts[1].any { it != '0' }) {
                                    amountError = if (isAr) "مبالغ المصروفات يجب أن تكون أعداداً صحيحة دون كسور" else "Expense amounts must be whole numbers without decimals"
                                    return@Button
                                }
                            }
                            val num = trimmed.toDoubleOrNull()
                            if (num == null || num <= 0) {
                                amountError = if (isAr) "المبلغ يجب أن يكون رقماً صحيحاً أكبر من صفر" else "Amount must be a number greater than 0"
                                return@Button
                            }
                            showConfirmDialog = true
                        },
                        enabled = parsedAmount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_expense_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "تسجيل خصم المصروف (${if (parsedAmount > 0) CurrencyConfig.format(parsedAmount, currentCurrency, isAr) else ""})" else "Record Expense (${if (parsedAmount > 0) CurrencyConfig.format(parsedAmount, currentCurrency, isAr) else ""})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog before recording expense
    if (showConfirmDialog) {
        val finalTitle = title.ifBlank { if (isAr) "مصروف $selectedCategory" else "Expense: $selectedCategory" }
        ConfirmationDialog(
            title = if (isAr) "تأكيد خصم المصروف" else "Confirm Expense",
            message = if (isAr)
                "هل تريد تأكيد خصم ${CurrencyConfig.format(parsedAmount, currentCurrency, isAr)} لـ '$finalTitle'؟\nسيتم تطبيق شلال الخصم الفوري وتحديث كاش الجيب."
            else
                "Confirm deduction of ${CurrencyConfig.format(parsedAmount, currentCurrency, isAr)} for '$finalTitle'?",
            confirmText = if (isAr) "تأكيد الخصم" else "Confirm",
            cancelText = if (isAr) "مراجعة" else "Cancel",
            onConfirm = {
                onRecordExpense(finalTitle, parsedAmount, selectedCategory, isYesterdayExpense)
                title = ""
                amountText = ""
                isYesterdayExpense = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Category Management Dialog
    if (showCategoryModal) {
        CategoryManagementModal(
            categories = uiState.categories,
            onAddCategory = onAddCategory,
            onDeleteCategory = onDeleteCategory,
            onDismiss = { showCategoryModal = false }
        )
    }
}
