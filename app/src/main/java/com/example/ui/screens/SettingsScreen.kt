package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyConfig
import com.example.data.CurrencyItem
import com.example.data.DateTimeUtils
import com.example.ui.MasrofiUiState
import com.example.ui.components.BackupRestoreModal
import com.example.ui.components.CycleSettingsModal
import com.example.ui.components.WhatsNewDialog
import com.example.ui.theme.CornerStyle
import com.example.ui.theme.LocalAppCornerRadius
import com.example.ui.theme.LocalAppCurrency
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.Localization
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: MasrofiUiState,
    currentTheme: ThemeMode,
    currentCorner: CornerStyle,
    currentCurrency: String,
    currentLanguage: String,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectCorner: (CornerStyle) -> Unit,
    onSelectCurrency: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onUpdateCycleDuration: (days: Int, endDate: String) -> Unit,
    onResetCycle: (total: Double, days: Int, type: String, value: Double, endDate: String) -> Unit,
    onUpdateReminders: (daily: Boolean, threshold: Boolean, category: Boolean) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = Localization.getStrings(currentLanguage)
    val isAr = currentLanguage != "en"

    val cornerRadius = LocalAppCornerRadius.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val smallShape = RoundedCornerShape((cornerRadius.value * 0.6f).toInt().coerceAtLeast(3).dp)

    var showCycleResetModal by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }
    var showWhatsNewModal by remember { mutableStateOf(false) }

    // Cycle duration & target end date inputs
    var daysLeftInput by remember(uiState.appState.daysLeft) {
        mutableStateOf(uiState.appState.daysLeft.toString())
    }
    var targetEndDateInput by remember(uiState.appState.cycleEndDate) {
        mutableStateOf(
            if (uiState.appState.cycleEndDate.isNotBlank()) {
                uiState.appState.cycleEndDate
            } else {
                DateTimeUtils.addDaysToIso(DateTimeUtils.getTodayIso(), uiState.appState.daysLeft)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Header
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
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                ),
                                shape = smallShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isAr) "تخصيص كامل للتطبيق في مكان واحد موحد" else "All app configurations centralized in one place",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showWhatsNewModal = true }
                ) {
                    Text(
                        text = "v2.0",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Section 1: Currency Selection (All Arab Countries + USD)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_currency_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.currencySection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isAr) "العملة المعتمدة لحسابات وكافة بطاقات التطبيق (22 دولة عربية + الدولار):" else "Active currency across all transactions and cards (All Arab countries + USD):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CurrencyConfig.ALL_CURRENCIES.forEach { curr ->
                            val isSelected = currentCurrency.equals(curr.code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCurrency(curr.code) },
                                label = {
                                    Text(
                                        text = "${curr.flagEmoji} ${if (isAr) curr.symbolAr else curr.symbolEn} (${curr.code})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = smallShape,
                                modifier = Modifier.testTag("currency_chip_${curr.code}")
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Language Selection (Arabic / English)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_language_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.languageSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isAr) "اختر لغة واجهة التطبيق المفضلة:" else "Choose preferred app interface language:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isArabicSelected = currentLanguage != "en"
                        Surface(
                            shape = smallShape,
                            color = if (isArabicSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isArabicSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectLanguage("ar") }
                                .testTag("lang_btn_ar")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🇪🇬 العربية (العالم العربي)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isArabicSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        val isEnglishSelected = currentLanguage == "en"
                        Surface(
                            shape = smallShape,
                            color = if (isEnglishSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isEnglishSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectLanguage("en") }
                                .testTag("lang_btn_en")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🇬🇧 English (US/UK)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isEnglishSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Cycle Duration & Target End Date Adjustment
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_cycle_duration_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.cycleSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isAr)
                            "تعديل مدة الدورة المالية الحالية أو تحديد تاريخ انتهائها بدقة دون تصفير ميزانيتك:"
                        else
                            "Adjust current cycle duration or set a specific end date without resetting your balances:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Days input
                    OutlinedTextField(
                        value = daysLeftInput,
                        onValueChange = { input ->
                            daysLeftInput = input
                            val days = input.toIntOrNull()
                            if (days != null && days > 0) {
                                targetEndDateInput = DateTimeUtils.addDaysToIso(DateTimeUtils.getTodayIso(), days)
                            }
                        },
                        label = { Text(if (isAr) "الأيام المتبقية في الدورة" else "Days Left in Cycle") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("cycle_days_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target End Date input (YYYY-MM-DD)
                    OutlinedTextField(
                        value = targetEndDateInput,
                        onValueChange = { dateInput ->
                            targetEndDateInput = dateInput
                            val calcDays = DateTimeUtils.calculateDaysBetween(DateTimeUtils.getTodayIso(), dateInput)
                            if (calcDays > 0) {
                                daysLeftInput = calcDays.toString()
                            }
                        },
                        label = { Text(if (isAr) "تاريخ انتهاء الدورة (YYYY-MM-DD)" else "Target End Date (YYYY-MM-DD)") },
                        placeholder = { Text("مثال: 2026-11-15") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().testTag("cycle_end_date_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Duration Button
                    Button(
                        onClick = {
                            val days = daysLeftInput.toIntOrNull() ?: uiState.appState.daysLeft
                            onUpdateCycleDuration(days, targetEndDateInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("save_cycle_duration_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "حفظ وتطبيق مدة وتاريخ الدورة" else "Save Cycle Duration & Target Date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Reset Button
                    OutlinedButton(
                        onClick = { showCycleResetModal = true },
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("reset_full_cycle_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "إعادة ضبط الدورة المالية بالكامل (ميزانية وتوفير جديد)" else "Full Budget & Savings Reconfiguration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 4: Appearance (Themes & Corner Styles)
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_appearance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.appearanceSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAr) "السمات اللونية المتاحة (بما فيها الفاتح الأبيض):" else "Color Themes (including Pure Light):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeMode.entries.forEach { mode ->
                        val isSelected = currentTheme == mode
                        Surface(
                            shape = smallShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectTheme(mode) }
                                .testTag("theme_opt_${mode.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mode.titleAr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(mode.subtitleAr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isAr) "أنماط حواف الواجهة (الحاد، الدائري، والمربع الناعم):" else "UI Corner Shapes (Sharp, Classic, Smooth, Hyper):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CornerStyle.entries.forEach { style ->
                        val isSelected = currentCorner == style
                        Surface(
                            shape = smallShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectCorner(style) }
                                .testTag("corner_opt_${style.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(style.titleAr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(style.subtitleAr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Reminders & Notifications
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_reminders_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.remindersSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isAr) "تذكير مسائي بتسجيل المصروفات اليومية" else "Evening reminder to log daily expenses",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                        Text(
                            if (isAr) "تنبيه عند استهلاك 80% من المسموح اليومي" else "Alert when 80% of daily allowance is spent",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                        Text(
                            if (isAr) "تنبيه عند الاقتراب أو تجاوز حد أي تصنيف" else "Alert when approaching or exceeding category budget limit",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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

        // Section 6: Backup & Restore
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("settings_backup_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.backupSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isAr) "حفظ وتصدير بياناتك المالية بالكامل محلياً أو استعادتها بأمان:" else "Export and restore all your financial records safely in JSON format:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = { showBackupModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = smallShape,
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_backup_modal_btn")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "فتح شاشة النسخ الاحتياطي والاستعادة" else "Open Backup & Restore Center", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section 7: About App & v2.0 Highlights
        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().clickable { showWhatsNewModal = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(strings.whatsNewV2Title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (isAr) "اضغط لعرض تفاصيل التحديث الجديد 2.0" else "Tap to view v2.0 release notes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text("v2.0", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }

    if (showBackupModal) {
        BackupRestoreModal(
            onExport = onExportBackup,
            onImport = onImportBackup,
            onDismiss = { showBackupModal = false }
        )
    }

    if (showCycleResetModal) {
        CycleSettingsModal(
            currentTotal = uiState.appState.totalReceived,
            currentDays = uiState.appState.daysLeft,
            currentSavingsType = uiState.appState.savingsType,
            currentSavingsValue = uiState.appState.savingsValue,
            onConfirm = { total, days, type, value ->
                val calculatedEnd = DateTimeUtils.addDaysToIso(DateTimeUtils.getTodayIso(), days)
                onResetCycle(total, days, type, value, calculatedEnd)
                showCycleResetModal = false
            },
            onDismiss = { showCycleResetModal = false }
        )
    }

    if (showWhatsNewModal) {
        WhatsNewDialog(
            onDismiss = { showWhatsNewModal = false }
        )
    }
}
