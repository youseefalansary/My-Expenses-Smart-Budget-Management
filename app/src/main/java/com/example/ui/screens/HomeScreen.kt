package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.BackupRestoreModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.CycleSettingsModal
import com.example.ui.components.EditExpenseDialog
import com.example.ui.components.RollbackExpenseDialog
import com.example.ui.components.WhatsNewDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: MasrofiUiState,
    onRollbackExpense: (Int, String) -> Unit,
    onUpdateExpense: (Int, String, Double, String) -> Unit,
    onEndDay: () -> Unit,
    onToggleInstituteDay: (Boolean) -> Unit,
    onResetCycle: (Double, Int, String, Double) -> Unit,
    onOpenAppearanceModal: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEndDayConfirm by remember { mutableStateOf(false) }
    var showCycleModal by remember { mutableStateOf(false) }
    var showWhatsNewModal by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    val appState = uiState.appState
    val burnRate = uiState.burnRatePercentage
    val allowance = uiState.dailyAllowance
    val spentToday = appState.todaySpent

    val cornerRadius = LocalAppCornerRadius.current
    val currentTheme = LocalThemeMode.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    val pocketCardBrush = when (currentTheme) {
        ThemeMode.PURE_LIGHT -> Brush.linearGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF0F9FF), Color(0xFFE0F2FE))
        )
        ThemeMode.EMERALD_GOLD -> Brush.linearGradient(
            listOf(EmeraldSurface, EmeraldSurfaceVariant, Color(0xFF0F3B2E))
        )
        ThemeMode.MIDNIGHT_OLED -> Brush.linearGradient(
            listOf(OledBlack, Color(0xFF140F20), Color(0xFF1E1035))
        )
        ThemeMode.DARK_SLATE -> Brush.linearGradient(
            listOf(Slate900, Slate850, Color(0xFF0F2547))
        )
    }

    val burnRateColor by animateColorAsState(
        targetValue = when {
            burnRate <= 0.60f -> if (currentTheme == ThemeMode.PURE_LIGHT) LightSecondary else NeonEmerald
            burnRate <= 0.90f -> if (currentTheme == ThemeMode.PURE_LIGHT) LightTertiary else NeonAmber
            else -> if (currentTheme == ThemeMode.PURE_LIGHT) LightError else NeonCoral
        },
        label = "burn_color"
    )

    val progressValue by animateFloatAsState(
        targetValue = (burnRate.coerceIn(0f, 1f)),
        label = "burn_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header with Version 1.1 Badge & Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                ),
                                shape = smallShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "شعار التطبيق",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مصروفي",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Version 1.1 Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { showWhatsNewModal = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "جديد · الإصدار 1.1",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "نظام إدارة الميزانية والعمل الحر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons: Backup, Appearance, Cycle
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { showBackupModal = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, smallShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, smallShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "النسخ الاحتياطي",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenAppearanceModal,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, smallShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, smallShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "مظهر التطبيق",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showCycleModal = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, smallShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, smallShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "إعداد الدورة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // What's New Mini Banner
        item {
            Surface(
                shape = cardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWhatsNewModal = true }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ما الجديد في التحديث 1.1؟",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استرداد متعدد المصادر، حجز المواصلات الفعلي، وحماية إغلاق اليوم",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "عرض التفاصيل ←",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 1. Primary Card: Pocket Cash
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), cardShape)
                    .testTag("pocket_cash_card")
            ) {
                Box(
                    modifier = Modifier
                        .background(pocketCardBrush)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "كاش الجيب المتاح فعلياً للصرف",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = "متبقي ${appState.daysLeft} يوم",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", appState.pocketCash),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Daily Allowance and Burn-Rate Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f), smallShape)
                                .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, smallShape)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "المسموح اليومي الصافي",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", allowance)} ج.م",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "معدل الحرق اليومي (Burn-Rate)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(burnRate * 100).toInt()}% (${String.format(Locale.US, "%.1f", spentToday)} ج.م)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = burnRateColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(smallShape),
                                color = burnRateColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (spentToday > allowance)
                                    "🚨 تجاوزت المسموح اليومي بمقدار ${String.format(Locale.US, "%.1f", spentToday - allowance)} ج.م (تفعيل شلال الخصم)!"
                                else
                                    "متبقي لك اليوم: ${String.format(Locale.US, "%.1f", uiState.remainingTodayAllowance)} ج.م",
                                fontSize = 11.sp,
                                color = if (spentToday > allowance) MaterialTheme.colorScheme.error else burnRateColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Dual Cards: Splurge Pool & Mandatory Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Splurge Pool Card
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f).testTag("splurge_pool_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "حصالة الوجبة الغالية",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", appState.splurgePool)} ج.م",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "من فوائض نهاية اليوم للأكلات الفاخرة",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Vault Card (Mandatory Savings)
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f).testTag("vault_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "التوفير الإجباري",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", appState.vault)} ج.م",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (appState.savingsType == "fixed") "مبلغ ثابت: ${appState.savingsValue.toInt()} ج.م" else "نسبة مجمدة: ${appState.savingsValue}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Institute Day Card (Real 40 EGP Transport Reservation)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (appState.isInstituteDay == 1) InstituteBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth().testTag("institute_day_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                .background(
                                    if (appState.isInstituteDay == 1) InstituteBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "يوم المعهد",
                                tint = if (appState.isInstituteDay == 1) InstituteBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "يوم المعهد (حجز مواصلات فعلي)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (appState.isInstituteDay == 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = InstituteBlue.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "متبقي: ${appState.transportReserve.toInt()} ج.م",
                                            color = InstituteBlue,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (appState.isInstituteDay == 1)
                                    "يُخصم تلقائياً من الـ 40 ج.م عند تسجيل أي مواصلات دون مساس بالأكل"
                                else
                                    "تفعيل حجز 40 ج.م للمواصلات خارج ميزانية الطعام والشراب",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = appState.isInstituteDay == 1,
                        onCheckedChange = { onToggleInstituteDay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = InstituteBlue,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("institute_day_switch")
                    )
                }
            }
        }

        // 4. End Day Button (Protected against multiple closings per day)
        item {
            Button(
                onClick = {
                    if (uiState.canEndDayToday) {
                        showEndDayConfirm = true
                    }
                },
                enabled = uiState.canEndDayToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.canEndDayToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (uiState.canEndDayToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                shape = cardShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("end_day_button")
            ) {
                Icon(
                    imageVector = Icons.Default.NightlightRound,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.canEndDayToday)
                        "إنهاء اليوم الآن (ترحيل الفائض للحصالة)"
                    else
                        "اليوم أُغلق بالفعل مسبقاً (تم ترحيل فائضه)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // 5. Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر المصروفات والعمليات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${uiState.recentTransactions.size} عملية",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty state or transaction items
        if (uiState.recentTransactions.isEmpty()) {
            item {
                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد مصاريف مسجلة حتى الآن",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "انتقل إلى تبويب 'خصم صرف' لتسجيل أول مصروف!",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.recentTransactions,
                key = { it.id }
            ) { tx ->
                TransactionCard(
                    transaction = tx,
                    onEditClick = { transactionToEdit = tx },
                    onDeleteClick = { transactionToDelete = tx }
                )
            }
        }
    }

    // Confirmation dialog before ending the day
    if (showEndDayConfirm) {
        val allowanceVal = uiState.dailyAllowance
        val surplusVal = kotlin.math.max(0.0, allowanceVal - appState.todaySpent)
        ConfirmationDialog(
            title = "تأكيد إنهاء وإغلاق اليوم",
            message = "هل أنت متأكد من إنهاء اليوم الآن؟\nسيتم إنقاص أيام الدورة بمقدار 1 وترحيل الفائض (${String.format(Locale.US, "%.1f", surplusVal)} ج.م) لحصالة الوجبة الغالية.\n\nتنبيه: لا يمكن إغلاق اليوم أكثر من مرة في نفس التاريخ.",
            confirmText = "إنهاء وترحيل",
            cancelText = "تراجع",
            onConfirm = { onEndDay() },
            onDismiss = { showEndDayConfirm = false }
        )
    }

    // Rollback / Deletion Dialog with breakdown
    transactionToDelete?.let { tx ->
        RollbackExpenseDialog(
            transaction = tx,
            onConfirm = { legacyDest ->
                onRollbackExpense(tx.id, legacyDest)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }

    // Edit Expense Dialog
    transactionToEdit?.let { tx ->
        EditExpenseDialog(
            transaction = tx,
            categories = uiState.categories,
            onSave = { newTitle, newAmount, newCategory ->
                onUpdateExpense(tx.id, newTitle, newAmount, newCategory)
                transactionToEdit = null
            },
            onDismiss = { transactionToEdit = null }
        )
    }

    // Cycle Settings Modal
    if (showCycleModal) {
        CycleSettingsModal(
            onDismiss = { showCycleModal = false },
            onSave = { total, days, savingsType, savingsValue ->
                onResetCycle(total, days, savingsType, savingsValue)
            }
        )
    }

    // What's New Dialog
    if (showWhatsNewModal) {
        WhatsNewDialog(onDismiss = { showWhatsNewModal = false })
    }

    // Backup & Restore Modal
    if (showBackupModal) {
        BackupRestoreModal(
            onExport = onExportBackup,
            onImport = onImportBackup,
            onDismiss = { showBackupModal = false }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}")
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
                        .size(42.dp)
                        .background(
                            when (transaction.category) {
                                "طعام ومشروبات" -> NeonAmber.copy(alpha = 0.15f)
                                "مواصلات" -> InstituteBlue.copy(alpha = 0.15f)
                                "علاج" -> NeonCoral.copy(alpha = 0.15f)
                                else -> NeonPurple.copy(alpha = 0.15f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (transaction.category) {
                            "طعام ومشروبات" -> "🍔"
                            "مواصلات" -> "🚌"
                            "علاج" -> "💊"
                            else -> "🛍️"
                        },
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (transaction.dateTag == "اليوم") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = transaction.dateTag,
                                fontSize = 10.sp,
                                color = if (transaction.dateTag == "اليوم") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• ${transaction.createdAt}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${transaction.amount.toInt()} ج.م",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp).testTag("edit_tx_btn_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل المصروف",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp).testTag("delete_tx_btn_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "حذف واسترداد",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
