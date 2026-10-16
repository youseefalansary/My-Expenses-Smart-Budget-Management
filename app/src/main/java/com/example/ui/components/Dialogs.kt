package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "تأكيد",
    cancelText: String = "إلغاء",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isDestructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary
                ),
                shape = smallShape,
                modifier = Modifier.testTag("dialog_confirm_btn")
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = smallShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.testTag("dialog_cancel_btn")
            ) {
                Text(cancelText)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = cardShape
    )
}

@Composable
fun StrictAlertWarningDialog(
    warningMessage: String,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تحذير",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🚨 إنذار مالي صارم!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = warningMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = smallShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("warning_dismiss_btn")
                ) {
                    Text("فهمت الخطر وسألتزم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Requirement 1: Rollback / Delete dialog with breakdown or legacy destination choice
@Composable
fun RollbackExpenseDialog(
    transaction: TransactionEntity,
    onConfirm: (legacyDestination: String) -> Unit,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val isTracked = (transaction.deductedFromPocket > 0 ||
            transaction.deductedFromSplurge > 0 ||
            transaction.deductedFromVault > 0 ||
            transaction.deductedFromTransport > 0)

    var selectedLegacyDest by remember { mutableStateOf("pocket") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "تأكيد حذف المصروف والتراجع الدقيق",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "سيتم حذف '${transaction.title}' بمبلغ ${transaction.amount.toInt()} ج.م.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isTracked) {
                    // Display exact source breakdown
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "تفاصيل إعادة المبلغ لمصادره الأصلية:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (transaction.deductedFromPocket > 0) {
                                Text("• استرداد لكاش الجيب: ${transaction.deductedFromPocket.toInt()} ج.م", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            if (transaction.deductedFromSplurge > 0) {
                                Text("• استرداد لحصالة الوجبة: ${transaction.deductedFromSplurge.toInt()} ج.م", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            if (transaction.deductedFromVault > 0) {
                                Text("• استرداد للتوفير الإجباري: ${transaction.deductedFromVault.toInt()} ج.م", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                            if (transaction.deductedFromTransport > 0) {
                                Text("• استرداد لمحجوز مواصلات المعهد: ${transaction.deductedFromTransport.toInt()} ج.م", fontSize = 12.sp, color = InstituteBlue)
                            }
                        }
                    }
                } else {
                    // Legacy transaction: let user choose where to return
                    Text(
                        text = "هذا المصروف مسجل بنظام قديم دون تفصيل مصادر الخصم. اختر أين تريد إعادة المبلغ:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "pocket" to "إعادة إلى كاش الجيب",
                        "splurge" to "إعادة إلى حصالة الوجبة الغالية",
                        "vault" to "إعادة إلى التوفير الإجباري"
                    ).forEach { (destKey, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLegacyDest = destKey }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLegacyDest == destKey,
                                onClick = { selectedLegacyDest = destKey },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            onConfirm(selectedLegacyDest)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حذف واسترداد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Requirement 4 & 5: Cycle Settings Modal supporting Percentage OR Fixed Amount
@Composable
fun CycleSettingsModal(
    onDismiss: () -> Unit,
    onSave: (total: Double, days: Int, savingsType: String, savingsValue: Double) -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    var totalAmountInput by remember { mutableStateOf("2000") }
    var selectedDays by remember { mutableIntStateOf(30) }
    var savingsType by remember { mutableStateOf("percentage") } // "percentage" or "fixed"
    var savingsValueInput by remember { mutableStateOf("20") } // percentage (e.g. 12.5) or fixed (e.g. 400)
    var inputError by remember { mutableStateOf<String?>(null) }

    val total = totalAmountInput.toDoubleOrNull() ?: 0.0
    val savingsVal = savingsValueInput.toDoubleOrNull() ?: 0.0

    // Accurate calculation
    val vaultAmount: Double = if (savingsType == "fixed") {
        min(total, max(0.0, savingsVal))
    } else {
        val pct = min(100.0, max(0.0, savingsVal))
        round((total * (pct / 100.0)) * 100.0) / 100.0
    }
    val pocketAmount = max(0.0, total - vaultAmount)
    val dailyAllowance = if (selectedDays > 0) pocketAmount / selectedDays else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إعداد الدورة المالية الجديدة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = totalAmountInput,
                    onValueChange = { totalAmountInput = it; inputError = null },
                    label = { Text("المبلغ الإجمالي المستلم (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth().testTag("cycle_total_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Days selection
                Text(
                    text = "عدد الأيام في الدورة:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(6, 15, 30).forEach { days ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text("$days يوم", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = smallShape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Requirement 4: Savings Type Choice (Percentage vs Fixed Amount)
                Text(
                    text = "طريقة تحديد التوفير الإجباري:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = savingsType == "percentage",
                        onClick = {
                            savingsType = "percentage"
                            savingsValueInput = "20"
                            inputError = null
                        },
                        label = { Text("نسبة مئوية (%)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = savingsType == "fixed",
                        onClick = {
                            savingsType = "fixed"
                            savingsValueInput = "400"
                            inputError = null
                        },
                        label = { Text("مبلغ ثابت (ج.م)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = savingsValueInput,
                    onValueChange = { savingsValueInput = it; inputError = null },
                    label = {
                        Text(if (savingsType == "percentage") "النسبة المئوية (مثل: 12.5% أو 20%)" else "مبلغ التوفير الثابت (ج.م)")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth().testTag("savings_value_input")
                )

                inputError?.let { err ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant calculation preview card
                Surface(
                    shape = smallShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "المعاينة المالية الدقيقة قبل البدء:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("التوفير الإجباري المجمد:", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            Text(
                                "${String.format(Locale.US, "%.2f", vaultAmount)} ج.م",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("كاش الجيب المتاح للصرف:", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            Text(
                                "${String.format(Locale.US, "%.2f", pocketAmount)} ج.م",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المسموح اليومي المحسوب:", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            Text(
                                "${String.format(Locale.US, "%.2f", dailyAllowance)} ج.م / يوم",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (total <= 0) {
                            inputError = "المبلغ الإجمالي يجب أن يكون أكبر من صفر"
                            return@Button
                        }
                        if (savingsType == "percentage") {
                            if (savingsVal < 0 || savingsVal > 100) {
                                inputError = "النسبة يجب أن تكون بين 0% و 100%"
                                return@Button
                            }
                        } else {
                            if (savingsVal < 0 || savingsVal > total) {
                                inputError = "مبلغ التوفير الثابت يجب ألا يتجاوز إجمالي المبلغ المستلم"
                                return@Button
                            }
                        }
                        onSave(total, selectedDays, savingsType, savingsVal)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cycle_apply_btn")
                ) {
                    Text("بدء الدورة وتطبيق الخطة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// Requirement 6: Edit Expense Dialog
@Composable
fun EditExpenseDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onSave: (title: String, amount: Double, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    var editTitle by remember { mutableStateOf(transaction.title) }
    var editAmountText by remember { mutableStateOf(transaction.amount.toInt().toString()) }
    var editCategory by remember { mutableStateOf(transaction.category) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "تعديل المصروف المسجل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("اسم الصنف") },
                    singleLine = true,
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = it; errorMessage = null },
                    label = { Text("المبلغ (أعداد صحيحة بالجنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("التصنيف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = editCategory == cat.name,
                            onClick = { editCategory = cat.name },
                            label = { Text(cat.name, fontSize = 10.sp) },
                            shape = smallShape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val parsed = editAmountText.toDoubleOrNull()
                            if (parsed == null || parsed <= 0 || parsed != kotlin.math.floor(parsed)) {
                                errorMessage = "المبلغ يجب أن يكون عدداً صحيحاً أكبر من صفر (مثال: 15 أو 20)"
                                return@Button
                            }
                            onSave(editTitle.ifBlank { "مصروف $editCategory" }, parsed, editCategory)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ التعديل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Requirement 6: Categories Management Dialog
@Composable
fun CategoryManagementModal(
    categories: List<CategoryEntity>,
    onAddCategory: (name: String, emoji: String, limit: Double) -> Unit,
    onDeleteCategory: (id: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    var newName by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("☕") }
    var newLimitText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إدارة تصنيفات المصروفات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Existing categories list
                Text("التصنيفات الحالية وحدودها:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                categories.forEach { cat ->
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cat.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                if (cat.budgetLimit > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("(حد: ${cat.budgetLimit.toInt()} ج.م)", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }

                            if (cat.isDefault == 0) {
                                IconButton(
                                    onClick = { onDeleteCategory(cat.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add new category
                Text("إضافة تصنيف جديد:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newEmoji,
                        onValueChange = { newEmoji = it },
                        label = { Text("أيقونة") },
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.width(80.dp)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم التصنيف") },
                        singleLine = true,
                        shape = smallShape,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newLimitText,
                    onValueChange = { newLimitText = it },
                    label = { Text("الحد المالي الأقصى اختياري (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val limit = newLimitText.toDoubleOrNull() ?: 0.0
                            onAddCategory(newName.trim(), newEmoji.trim(), limit)
                            newName = ""
                            newLimitText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة التصنيف", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Requirement: What's New Dialog
@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ما الجديد في الإصدار 1.1؟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تحديثات دقيقة ومزايا مالية متقدمة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val features = listOf(
                    "إصلاح شلال التراجع" to "عند حذف أي مصروف، يتم استرداد كل قرش للرصيد الدقيق الذي خُصم منه (كاش الجيب، حصالة الوجبة، التوفير، أو مواصلات المعهد).",
                    "تنفيذ حجز يوم المعهد فعلياً" to "تخصيص 40 ج.م للمواصلات تغطي مشاويرك اليومية تلقائياً دون استهلاك ميزانية الطعام والشراب.",
                    "حماية إغلاق اليوم" to "منع إنهاء اليوم أكثر من مرة في نفس التاريخ، مع مزامنة ذكية بين الإغلاق اليدوي والإغلاق التلقائي لمنتصف الليل.",
                    "خيارات التوفير المتطورة" to "إمكانية تحديد التوفير الإجباري بنسبة دقيقة (مع الكسور مثل 12.5%) أو بمبلغ ثابت محدد (مثل 150 ج.م).",
                    "دقة المبالغ المالية" to "اعتماد مبالغ المصروفات كأعداد صحيحة بالجنيه لضمان واقعية الصرف وسهولة التتبع.",
                    "تقارير وفترات زمنية مخصصة" to "تصفية التقارير بأسبوع أو شهر أو جميع السجلات مع رسم بياني وإمكانية تعديل أي مصروف.",
                    "إدارة التصنيفات والحدود" to "إضافة وحذف تصنيفات خاصة ووضع حد مالي أقصى مع تنبيه عند الاقتراب أو التجاوز.",
                    "النسخ الاحتياطي والتصدير" to "تصدير تقرير المصروفات CSV، مع نسخ احتياطي محلي كامل واستعادة آمنة بضغطة زر."
                )

                features.forEach { (title, desc) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("• $title", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("رائع، فهمت التحديثات", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Requirement 7 & 8: Backup & Restore Modal
@Composable
fun BackupRestoreModal(
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    var backupJsonInput by remember { mutableStateOf("") }
    var exportedJson by remember { mutableStateOf<String?>(null) }
    var showConfirmRestore by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "النسخ الاحتياطي والاستعادة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Export
                Text("1. تصدير نسخة احتياطية:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        onExport { json ->
                            exportedJson = json
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("توليد وتصدير ملف النسخة الاحتياطية", fontWeight = FontWeight.Bold)
                }

                exportedJson?.let { json ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = smallShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("تم توليد النسخة بنجاح (${json.length} حرف):", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(json.take(150) + "...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Restore
                Text("2. استعادة نسخة سابقة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = backupJsonInput,
                    onValueChange = { backupJsonInput = it },
                    label = { Text("الصق كود النسخة الاحتياطية JSON هنا") },
                    maxLines = 4,
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (backupJsonInput.isNotBlank()) {
                            showConfirmRestore = true
                        }
                    },
                    enabled = backupJsonInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = smallShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("استعادة البيانات الآن", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmRestore) {
        ConfirmationDialog(
            title = "تحذير: استبدال البيانات الحالية",
            message = "استعادة النسخة الاحتياطية ستقوم بحذف السجلات الحالية واستبدالها بسجلات النسخة. هل أنت متأكد تماماً من المتابعة؟",
            confirmText = "استبدال واستعادة",
            cancelText = "تراجع",
            isDestructive = true,
            onConfirm = {
                onImport(backupJsonInput) { success ->
                    if (success) {
                        onDismiss()
                    }
                }
                showConfirmRestore = false
            },
            onDismiss = { showConfirmRestore = false }
        )
    }
}
