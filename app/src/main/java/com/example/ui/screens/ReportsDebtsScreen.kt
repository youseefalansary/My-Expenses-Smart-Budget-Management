package com.example.ui.screens

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
import androidx.compose.material.icons.filled.PieChart
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DebtEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*

@Composable
fun ReportsDebtsScreen(
    uiState: MasrofiUiState,
    onAddDebt: (person: String, amount: Double, type: String) -> Unit,
    onSettleDebt: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Calculate Category Distribution
    val categoryTotals = remember(uiState.allTransactions) {
        val map = mutableMapOf(
            "طعام ومشروبات" to 0.0,
            "مواصلات" to 0.0,
            "علاج" to 0.0,
            "شخصي" to 0.0
        )
        for (tx in uiState.allTransactions) {
            val cat = if (map.containsKey(tx.category)) tx.category else "شخصي"
            map[cat] = (map[cat] ?: 0.0) + tx.amount
        }
        map
    }

    val totalExpensesSum = categoryTotals.values.sum()

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
                text = "تحليل توزيع مصاريفك وإدارة السلفيات مع تسوية فورية بالكاش",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Expense Distribution Breakdown
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_breakdown_card")
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
                                text = "توزيع المصروفات حسب التصنيف",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "إجمالي: ${String.format("%.1f", totalExpensesSum)} ج.م",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented color bar representing category ratios
                    if (totalExpensesSum > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(smallShape)
                        ) {
                            listOf(
                                "طعام ومشروبات" to NeonAmber,
                                "مواصلات" to InstituteBlue,
                                "علاج" to NeonCoral,
                                "شخصي" to NeonPurple
                            ).forEach { (cat, color) ->
                                val catAmount = categoryTotals[cat] ?: 0.0
                                val weight = (catAmount / totalExpensesSum).toFloat()
                                if (weight > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .background(color)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Category Breakdown Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("طعام ومشروبات", NeonAmber, "🍔"),
                            Triple("مواصلات", InstituteBlue, "🚌"),
                            Triple("علاج", NeonCoral, "💊"),
                            Triple("شخصي", NeonPurple, "🛍️")
                        ).forEach { (cat, color, emoji) ->
                            val amount = categoryTotals[cat] ?: 0.0
                            val pct = if (totalExpensesSum > 0) (amount / totalExpensesSum * 100) else 0.0

                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${String.format("%.1f", amount)} ج.م",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = color.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${pct.toInt()}%",
                                                color = color,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                // Given (ليّا بره)
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
                            text = "${String.format("%.1f", totalGivenActive)} ج.م",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Taken (عليّا للناس)
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
                            text = "${String.format("%.1f", totalTakenActive)} ج.م",
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_person_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = debtAmountText,
                        onValueChange = { debtAmountText = it },
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
                            .testTag("debt_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Type Choice ('give' vs 'take')
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = debtType == "give",
                            onClick = { debtType = "give" },
                            label = {
                                Text(
                                    "أنا سلّفت (خرجت من جيبي)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = smallShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debt_type_give")
                        )

                        FilterChip(
                            selected = debtType == "take",
                            onClick = { debtType = "take" },
                            label = {
                                Text(
                                    "أنا استلفت (دخلت جيبي)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = smallShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debt_type_take")
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
                            containerColor = if (debtType == "give") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_debt_btn")
                    ) {
                        Text(
                            text = if (debtType == "give")
                                "تسجيل سلفة وخصم $currentDebtAmount ج.م من الكاش"
                            else
                                "تسجيل دين وإضافة $currentDebtAmount ج.م للكاش",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 4: Debts List with Filter
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
                            modifier = Modifier
                                .clickable { debtFilterTab = index }
                                .testTag("debt_tab_$index")
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
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد ديون في هذه الفئة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredDebts,
                key = { it.id }
            ) { debt ->
                DebtCard(
                    debt = debt,
                    onSettleClick = { debtToSettle = debt }
                )
            }
        }
    }

    // Confirmation dialog before adding debt
    if (showDebtConfirmDialog) {
        val finalPerson = personName.ifBlank { "شخص" }
        val actionDesc = if (debtType == "give")
            "سلّفت $finalPerson مبلغ $currentDebtAmount ج.م (سيتم خصمه من كاش الجيب)"
        else
            "استلفت من $finalPerson مبلغ $currentDebtAmount ج.م (سيتم إضافته لكاش الجيب)"

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
            "استرداد مبلغ ${debt.amount} ج.م من ${debt.person} وإضافته لكاش جيبك"
        else
            "سداد دين ${debt.person} بمبلغ ${debt.amount} ج.م وخصمه من كاش جيبك"

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
    onSettleClick: () -> Unit
) {
    val isGive = debt.type == "give"
    val isSettled = debt.isSettled == 1
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSettled) MaterialTheme.colorScheme.outlineVariant
            else if (isGive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(
                            if (isSettled) MaterialTheme.colorScheme.surfaceVariant
                            else if (isGive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSettled) Icons.Default.CheckCircle
                        else if (isGive) Icons.Default.ArrowUpward
                        else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isSettled) MaterialTheme.colorScheme.outline
                        else if (isGive) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = debt.person,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSettled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = smallShape,
                            color = if (isSettled) MaterialTheme.colorScheme.surfaceVariant
                            else if (isGive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isGive) "سلّفته" else "استلفت منه",
                                fontSize = 10.sp,
                                color = if (isSettled) MaterialTheme.colorScheme.outline
                                else if (isGive) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isSettled) "تمت التسوية بنجاح" else if (isGive) "في انتظار استرداد المبلغ" else "عليك سداده قريباً",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format("%.1f", debt.amount)} ج.م",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSettled) MaterialTheme.colorScheme.outline
                    else if (isGive) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.error
                )

                if (!isSettled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSettleClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = smallShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("settle_btn_${debt.id}")
                    ) {
                        Text(
                            text = if (isGive) "استرديت" else "سددت",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
