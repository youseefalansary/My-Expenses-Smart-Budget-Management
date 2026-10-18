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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyConfig
import com.example.data.DateTimeUtils
import com.example.data.WorkHistoryEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.*

@Composable
fun FreelanceScreen(
    uiState: MasrofiUiState,
    currentCurrency: String,
    currentLanguage: String,
    onDepositFreelance: (source: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = currentLanguage != "en"
    var source by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val cornerRadius = LocalAppCornerRadius.current
    val currentTheme = LocalThemeMode.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val currentAmount = amountText.toDoubleOrNull() ?: 0.0

    val freelanceCardBrush = when (currentTheme) {
        ThemeMode.PURE_LIGHT -> Brush.linearGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFECFDF5), Color(0xFFD1FAE5))
        )
        ThemeMode.EMERALD_GOLD -> Brush.linearGradient(
            listOf(EmeraldSurface, EmeraldSurfaceVariant, Color(0xFF0F3B2E))
        )
        ThemeMode.MIDNIGHT_OLED -> Brush.linearGradient(
            listOf(OledBlack, Color(0xFF062016), Color(0xFF0A3324))
        )
        ThemeMode.DARK_SLATE -> Brush.linearGradient(
            listOf(Slate900, Slate850, Color(0xFF0F3B2E))
        )
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
                text = if (isAr) "محفظة دخل العمل الحر (شغلي)" else "Freelance Income Hub",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAr) "أرباحك الحرة تذهب بنسبة 100% لكاش الجيب فوراً مع توثيق التاريخ والوقت" else "Freelance earnings go 100% to Pocket cash with exact timestamp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Freelance Total Earnings Card
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("freelance_summary_card")
            ) {
                Box(
                    modifier = Modifier
                        .background(freelanceCardBrush)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Work,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isAr) "إجمالي أرباح العمل الحر المتراكمة" else "Accumulated Freelance Earnings",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isAr) "100% كاش نقي" else "100% Cash",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = CurrencyConfig.format(uiState.appState.workTotal, currentCurrency, isAr, showDecimals = true),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isAr) "كل جنيه عمل حر يدخل كاش جيبك مباشرة لرفع المسموح اليومي فوراً دون اقتطاع" else "Every income deposit raises your daily allowance directly without deductions",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Deposit Freelance Form
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "إيداع دفعة عمل حر جديدة" else "Deposit New Freelance Income",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date & Timestamp indicator
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${if (isAr) "تاريخ وتوقيت الإيداع:" else "Deposit Time:"} ${DateTimeUtils.formatFullDateTime(System.currentTimeMillis(), DateTimeUtils.getTodayIso(), isAr)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text(if (isAr) "مصدر الفلوس / المشروع أو العميل" else "Client / Project / Source") },
                        placeholder = { Text(if (isAr) "مثلاً: تصميم تطبيق، مونتاج، ترجمة..." else "e.g., App UI Design, Translation...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("freelance_source_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val symbol = CurrencyConfig.getSymbol(currentCurrency, isAr)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("${if (isAr) "المبلغ المستلم" else "Amount Received"} ($symbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("freelance_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick presets (+100, +250, +500, +1000)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100, 250, 500, 1000).forEach { preset ->
                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { amountText = preset.toString() }
                            ) {
                                Text(
                                    text = "+$preset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (currentAmount > 0) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = currentAmount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = smallShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_freelance_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (isAr) "إيداع فوري في كاش الجيب" else "Deposit to Pocket Cash"} (+${if (currentAmount > 0) CurrencyConfig.format(currentAmount, currentCurrency, isAr) else ""})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Freelance History Header
        item {
            Text(
                text = if (isAr) "سجل دفعات العمل الحر (مؤرخة باليوم والوقت)" else "Freelance Income History (Dated & Timestamped)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.workHistory.isEmpty()) {
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
                        Text("💼", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAr) "لا توجد دفعات عمل حر مسجلة حتى الآن" else "No freelance payments recorded yet",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.workHistory, key = { it.id }) { work ->
                WorkHistoryCard(
                    work = work,
                    currentCurrency = currentCurrency,
                    isArabic = isAr
                )
            }
        }
    }

    if (showConfirmDialog) {
        val finalSource = source.ifBlank { if (isAr) "مشروع حر" else "Freelance Project" }
        ConfirmationDialog(
            title = if (isAr) "تأكيد إيداع أرباح العمل الحر" else "Confirm Freelance Deposit",
            message = if (isAr)
                "هل تريد إيداع مبلغ ${CurrencyConfig.format(currentAmount, currentCurrency, isAr)} من '$finalSource'؟\nسيذهب المبلغ بنسبة 100% إلى كاش الجيب لزيادة ميزانيتك اليومية فوراً."
            else
                "Deposit ${CurrencyConfig.format(currentAmount, currentCurrency, isAr)} from '$finalSource' into Pocket Cash?",
            confirmText = if (isAr) "تأكيد الإيداع" else "Confirm",
            cancelText = if (isAr) "تراجع" else "Cancel",
            onConfirm = {
                onDepositFreelance(finalSource, currentAmount)
                source = ""
                amountText = ""
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun WorkHistoryCard(
    work: WorkHistoryEntity,
    currentCurrency: String,
    isArabic: Boolean
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
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
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = work.source,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = work.dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "+${CurrencyConfig.format(work.amount, currentCurrency, isArabic)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
