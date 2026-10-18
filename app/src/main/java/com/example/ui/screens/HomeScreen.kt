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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
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
import com.example.data.CurrencyConfig
import com.example.data.TransactionEntity
import com.example.ui.MasrofiUiState
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EditExpenseDialog
import com.example.ui.components.RollbackExpenseDialog
import com.example.ui.components.WhatsNewDialog
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    uiState: MasrofiUiState,
    currentCurrency: String,
    currentLanguage: String,
    onRollbackExpense: (Int, String) -> Unit,
    onUpdateExpense: (Int, String, Double, String) -> Unit,
    onEndDay: () -> Unit,
    onToggleInstituteDay: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = Localization.getStrings(currentLanguage)
    val isAr = currentLanguage != "en"

    var showEndDayConfirm by remember { mutableStateOf(false) }
    var showWhatsNewModal by remember { mutableStateOf(false) }
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

    val totalMoneyBrush = when (currentTheme) {
        ThemeMode.PURE_LIGHT -> Brush.linearGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF5F3FF), Color(0xFFEDE9FE))
        )
        ThemeMode.EMERALD_GOLD -> Brush.linearGradient(
            listOf(Color(0xFF0A2E23), Color(0xFF134E3E), Color(0xFF1D5A47))
        )
        ThemeMode.MIDNIGHT_OLED -> Brush.linearGradient(
            listOf(Color(0xFF0F0B18), Color(0xFF1A122A), Color(0xFF25173E))
        )
        ThemeMode.DARK_SLATE -> Brush.linearGradient(
            listOf(Color(0xFF131D31), Color(0xFF182A45), Color(0xFF1E355A))
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
        // App Header with Version 2.0 Badge & Quick Settings Access
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
                            contentDescription = strings.appName,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Version 2.0 Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { showWhatsNewModal = true }.testTag("v2_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.versionBadge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = strings.appSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Settings Navigation Button (Exclusively leads to Settings)
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, smallShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, smallShape)
                        .size(40.dp)
                        .testTag("home_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.settingsTitle,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // USER REQUIREMENT: Keep Total box containing Pocket cash, Mandatory savings, and Surplus piggybank,
        // and delete the separate detached boxes from it.
        // Hero Unified Wealth Card
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_money_with_me_card")
            ) {
                Box(
                    modifier = Modifier
                        .background(totalMoneyBrush)
                        .padding(18.dp)
                ) {
                    Column {
                        // Header: Title & Currency Symbol
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.totalMoneyWithMe,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = strings.totalMoneySubtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = CurrencyConfig.getSymbol(currentCurrency, isAr),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Total Amount
                        Text(
                            text = CurrencyConfig.format(uiState.totalMoneyWithMe, currentCurrency, isAr, showDecimals = true),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("total_money_amount_text")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // The 3 Core Balances inside the Total Box: Pocket cash, Mandatory savings, Surplus piggybank
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. كاش الجيب (Pocket Cash)
                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pocket_cash_card")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "💵 ${strings.pocketCash}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyConfig.format(appState.pocketCash, currentCurrency, isAr),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${appState.daysLeft} ${strings.dayUnit} · " + (if (isAr) "متاح للصرف" else "Spendable"),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 2. التوفير الإجباري (Mandatory Savings Vault)
                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vault_card")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🔒 ${strings.savingsVault}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyConfig.format(appState.vault, currentCurrency, isAr),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (appState.savingsType == "fixed") (if (isAr) "مبلغ ثابت" else "Fixed") else "${appState.savingsValue}%",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 3. حصالة الفائض (Surplus Piggybank)
                            Surface(
                                shape = smallShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("splurge_pool_card")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🎁 ${strings.splurgePool}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyConfig.format(appState.splurgePool, currentCurrency, isAr),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isAr) "فوائض الإغلاق" else "Rolled surplus",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Transit reserve if Institute day active
                        if (appState.isInstituteDay == 1 && appState.transportReserve > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = smallShape,
                                color = InstituteBlue.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, InstituteBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = null,
                                            tint = InstituteBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isAr) "محجوز مواصلات المعهد (محمي من الصرف العادي):" else "Institute Transit Reserve:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = CurrencyConfig.format(appState.transportReserve, currentCurrency, isAr),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InstituteBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Allowance and Burn-Rate Section inside the Total Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), smallShape)
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
                                        text = strings.dailyAllowance,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyConfig.format(allowance, currentCurrency, isAr, showDecimals = true),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isAr) "معدل الحرق اليومي" else "Burn Rate",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(burnRate * 100).toInt()}% (${CurrencyConfig.format(spentToday, currentCurrency, isAr)})",
                                        fontSize = 14.sp,
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
                                    if (isAr) "🚨 تجاوزت المسموح بمقدار ${CurrencyConfig.format(spentToday - allowance, currentCurrency, isAr)}" else "🚨 Exceeded daily allowance by ${CurrencyConfig.format(spentToday - allowance, currentCurrency, isAr)}"
                                else
                                    "${strings.remainingToday}: ${CurrencyConfig.format(uiState.remainingTodayAllowance, currentCurrency, isAr, showDecimals = true)}",
                                fontSize = 11.sp,
                                color = if (spentToday > allowance) MaterialTheme.colorScheme.error else burnRateColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 3. Institute Day Card (Real Transport Reservation)
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
                                contentDescription = strings.instituteDay,
                                tint = if (appState.isInstituteDay == 1) InstituteBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.instituteDay,
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
                                            text = "${if (isAr) "متبقي" else "Left"}: ${CurrencyConfig.format(appState.transportReserve, currentCurrency, isAr)}",
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
                                    strings.instituteDayActive
                                else
                                    if (isAr) "حجز مسبق للمواصلات من كاش الجيب لحماية مصاريفك" else "Pre-reserve transit funds from cash",
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

        // 4. End Day Action Button
        item {
            val canEnd = uiState.canEndDayToday
            Button(
                onClick = {
                    if (canEnd) {
                        showEndDayConfirm = true
                    }
                },
                enabled = canEnd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canEnd) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.outline
                ),
                shape = smallShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("end_day_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = strings.endDayNow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canEnd) strings.endDayNow else strings.dayAlreadyEnded,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5. Recent Expenses List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentExpenses,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${uiState.recentTransactions.size} ${if (isAr) "معاملات" else "entries"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6. Transaction Items with Full Date, Day of Week, and Time
        if (uiState.recentTransactions.isEmpty()) {
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("☕", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.noExpensesToday,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { tx ->
                TransactionItem(
                    transaction = tx,
                    currentCurrency = currentCurrency,
                    isArabic = isAr,
                    onEditClick = { transactionToEdit = tx },
                    onDeleteClick = { transactionToDelete = tx }
                )
            }
        }
    }

    // Confirmation for Manual End Day
    if (showEndDayConfirm) {
        val allowanceVal = uiState.dailyAllowance
        val surplus = kotlin.math.max(0.0, allowanceVal - appState.todaySpent)
        ConfirmationDialog(
            title = strings.endDayNow,
            message = if (isAr)
                "هل أنت متأكد من إنهاء اليوم يدوياً؟\nسيتم ترحيل الفائض المتبقي (${CurrencyConfig.format(surplus, currentCurrency, isAr)}) إلى حصالة الوجبة الغالية وخصم يوم من الدورة."
            else
                "Close day manually?\nRemaining surplus (${CurrencyConfig.format(surplus, currentCurrency, isAr)}) will roll to the Surplus Pool and 1 day will be deducted.",
            confirmText = if (isAr) "تأكيد الإغلاق" else "Confirm Close",
            cancelText = if (isAr) "إلغاء" else "Cancel",
            onConfirm = { onEndDay() },
            onDismiss = { showEndDayConfirm = false }
        )
    }

    // Rollback / Delete Expense Dialog
    transactionToDelete?.let { tx ->
        RollbackExpenseDialog(
            transaction = tx,
            onConfirm = { dest ->
                onRollbackExpense(tx.id, dest)
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
            onSave = { title, amount, category ->
                onUpdateExpense(tx.id, title, amount, category)
                transactionToEdit = null
            },
            onDismiss = { transactionToEdit = null }
        )
    }

    // What's New Dialog
    if (showWhatsNewModal) {
        WhatsNewDialog(onDismiss = { showWhatsNewModal = false })
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    currentCurrency: String,
    isArabic: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)

    val dayDisplay = if (transaction.dayName.isNotBlank()) transaction.dayName else ""
    val dateDisplay = if (transaction.dateIso.isNotBlank()) transaction.dateIso else ""
    val timeDisplay = if (transaction.exactTime.isNotBlank()) transaction.exactTime else transaction.createdAt

    val fullDateStr = if (dayDisplay.isNotBlank()) {
        "$dayDisplay $dateDisplay • $timeDisplay"
    } else {
        "${transaction.dateTag} • ${transaction.createdAt}"
    }

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
                            color = if (transaction.dateTag.startsWith("اليوم")) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = transaction.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "• $fullDateStr",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${CurrencyConfig.format(transaction.amount, currentCurrency, isArabic)}",
                    fontSize = 14.sp,
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
                        contentDescription = "تعديل",
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
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
