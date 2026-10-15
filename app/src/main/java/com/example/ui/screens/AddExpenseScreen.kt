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
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*

data class QuickExpenseItem(
    val title: String,
    val amount: Double,
    val category: String,
    val emoji: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    uiState: MasrofiUiState,
    onRecordExpense: (title: String, amount: Double, category: String, isYesterday: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("طعام ومشروبات") }
    var isYesterdayExpense by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val categories = listOf("طعام ومشروبات", "مواصلات", "علاج", "شخصي")

    val quickItems = listOf(
        QuickExpenseItem("دليفري", 15.0, "طعام ومشروبات", "🛵"),
        QuickExpenseItem("كانز", 15.0, "طعام ومشروبات", "🥤"),
        QuickExpenseItem("مواصلات", 10.0, "مواصلات", "🚌"),
        QuickExpenseItem("قهوة", 12.0, "طعام ومشروبات", "☕"),
        QuickExpenseItem("ساندوتش", 25.0, "طعام ومشروبات", "🥪"),
        QuickExpenseItem("مياه", 7.0, "طعام ومشروبات", "💧")
    )

    val currentAmount = amountText.toDoubleOrNull() ?: 0.0

    // Dynamic waterfall calculation prediction
    val allowance = uiState.dailyAllowance
    val remainingToday = uiState.remainingTodayAllowance
    val splurge = uiState.appState.splurgePool
    val pocket = uiState.appState.pocketCash
    val vault = uiState.appState.vault

    val waterfallExplanation = remember(currentAmount, isYesterdayExpense, allowance, remainingToday, splurge, pocket, vault) {
        if (currentAmount <= 0) {
            "أدخل المبلغ لمعاينة مسار الخصم عبر الشلال المالي الصارم."
        } else if (isYesterdayExpense) {
            if (splurge >= currentAmount) {
                "سحب كامل من حصالة الوجبة الغالية (${currentAmount} ج.م) دون المساس بميزانية اليوم!"
            } else {
                val fromSplurge = splurge
                val fromPocket = currentAmount - fromSplurge
                "سحب ${fromSplurge} ج.م من حصالة الوجبة الغالية + ${fromPocket} ج.م من كاش الجيب دون المساس بميزانية اليوم."
            }
        } else {
            if (currentAmount <= remainingToday) {
                "ضمن المسموح اليومي: سيخصم المبلغ بالكامل (${currentAmount} ج.م) من ميزانية اليوم وكاش الجيب."
            } else {
                val deficit = currentAmount - remainingToday
                if (splurge >= deficit) {
                    "تجاوز للمسموح اليومي: سيغطى العجز (${deficit} ج.م) أولاً من حصالة الوجبة الغالية."
                } else if (splurge + pocket >= deficit) {
                    val remDef = deficit - splurge
                    "تجاوز للمسموح وحصالة الوجبة: سيخصم ${splurge} ج.م من الحصالة و ${remDef} ج.م من كاش الجيب مباشرة (يقلل ميزانية الأيام المتبقية)."
                } else {
                    "⚠️ تحذير: سينفد كاش الجيب وسيبدأ الخصم من التوفير الإجباري المجمد!"
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
                text = "خصم صرف جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "سجل مصروفاتك لتطبيق شلال الخصم الفوري التلقائي",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                        text = "أصناف متكررة سريعة (نقرة واحدة للتعبئة):",
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
                                        amountText = item.amount.toInt().toString()
                                        selectedCategory = item.category
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
                                        text = "${item.title} +${item.amount.toInt()}",
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
                        label = { Text("اسم الصنف أو المصروف") },
                        placeholder = { Text("مثلاً: قهوة، مواصلات، علاج...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("المبلغ (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Selection Chips
                    Text(
                        text = "التصنيف:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cat_chip_$cat")
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
                                        text = "مصروف منسي من أمس (أثر رجعي)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isYesterdayExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "لا يمس ميزانية اليوم الحالية نهائياً ويخصم من الحصالة/الكاش",
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
                            if (currentAmount > remainingToday && !isYesterdayExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentAmount > remainingToday && !isYesterdayExpense) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (currentAmount > remainingToday && !isYesterdayExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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

                    // Submit Button
                    Button(
                        onClick = {
                            if (currentAmount > 0) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = currentAmount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_expense_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل خصم المصروف (${if (currentAmount > 0) "$currentAmount ج.م" else ""})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Mandatory Confirmation Dialog before recording expense
    if (showConfirmDialog) {
        val finalTitle = title.ifBlank { "مصروف $selectedCategory" }
        ConfirmationDialog(
            title = "تأكيد خصم المصروف",
            message = "هل تريد تأكيد خصم مبلغ $currentAmount ج.م لـ '$finalTitle'؟\nسيتم تطبيق شلال الخصم الفوري وتحديث كاش الجيب.",
            confirmText = "تأكيد الخصم",
            cancelText = "مراجعة",
            onConfirm = {
                onRecordExpense(finalTitle, currentAmount, selectedCategory, isYesterdayExpense)
                title = ""
                amountText = ""
                isYesterdayExpense = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}
