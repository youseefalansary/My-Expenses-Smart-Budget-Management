package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MasrofiViewModel
import com.example.ui.components.StrictAlertWarningDialog
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.FreelanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsDebtsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CornerStyle
import com.example.ui.theme.LocalAppCornerRadius
import com.example.ui.theme.LocalAppCurrency
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.Localization
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences

sealed class ScreenTab(
    val index: Int,
    val titleAr: String,
    val titleEn: String,
    val icon: ImageVector,
    val tag: String
) {
    object Home : ScreenTab(0, "الرئيسية", "Home", Icons.Default.Home, "tab_home")
    object AddExpense : ScreenTab(1, "خصم صرف", "Expense", Icons.Default.RemoveCircle, "tab_add_expense")
    object Freelance : ScreenTab(2, "شغلي", "Income", Icons.Default.Work, "tab_freelance")
    object ReportsDebts : ScreenTab(3, "التقارير", "Reports", Icons.Default.Assessment, "tab_reports_debts")
    object Settings : ScreenTab(4, "الإعدادات", "Settings", Icons.Default.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MasrofiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePrefs = ThemePreferences(this)

        setContent {
            var themeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
            var cornerStyle by remember { mutableStateOf(themePrefs.getCornerStyle()) }
            var currencyCode by remember { mutableStateOf(themePrefs.getCurrency()) }
            var languageCode by remember { mutableStateOf(themePrefs.getLanguage()) }

            val isRtl = languageCode != "en"

            MyApplicationTheme(themeMode = themeMode, cornerStyle = cornerStyle) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides (if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr),
                    LocalAppCurrency provides currencyCode,
                    LocalAppLanguage provides languageCode
                ) {
                    MasrofiApp(
                        viewModel = viewModel,
                        currentTheme = themeMode,
                        currentCorner = cornerStyle,
                        currentCurrency = currencyCode,
                        currentLanguage = languageCode,
                        onThemeChange = { mode ->
                            themeMode = mode
                            themePrefs.setThemeMode(mode)
                        },
                        onCornerChange = { style ->
                            cornerStyle = style
                            themePrefs.setCornerStyle(style)
                        },
                        onCurrencyChange = { curr ->
                            currencyCode = curr
                            themePrefs.setCurrency(curr)
                            viewModel.updatePreferences(curr, languageCode)
                        },
                        onLanguageChange = { lang ->
                            languageCode = lang
                            themePrefs.setLanguage(lang)
                            viewModel.updatePreferences(currencyCode, lang)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MasrofiApp(
    viewModel: MasrofiViewModel,
    currentTheme: ThemeMode,
    currentCorner: CornerStyle,
    currentCurrency: String,
    currentLanguage: String,
    onThemeChange: (ThemeMode) -> Unit,
    onCornerChange: (CornerStyle) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val isAr = currentLanguage != "en"
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultWarning by viewModel.vaultWarning.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val cornerRadius = LocalAppCornerRadius.current
    val navShape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)

    // Intercept back button when not on Home screen
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    // Collect toast messages
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = navShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    val tabs = listOf(
                        ScreenTab.Home,
                        ScreenTab.AddExpense,
                        ScreenTab.Freelance,
                        ScreenTab.ReportsDebts,
                        ScreenTab.Settings
                    )

                    tabs.forEach { tab ->
                        val isSelected = selectedTabIndex == tab.index
                        val label = if (isAr) tab.titleAr else tab.titleEn
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = tab.index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    uiState = uiState,
                    currentCurrency = currentCurrency,
                    currentLanguage = currentLanguage,
                    onRollbackExpense = { id, dest -> viewModel.rollbackExpense(id, dest) },
                    onUpdateExpense = { id, title, amount, category -> viewModel.updateExpense(id, title, amount, category) },
                    onEndDay = { viewModel.endDayNow() },
                    onToggleInstituteDay = { viewModel.toggleInstituteDay(it) },
                    onNavigateToSettings = { selectedTabIndex = 4 }
                )

                1 -> AddExpenseScreen(
                    uiState = uiState,
                    currentCurrency = currentCurrency,
                    currentLanguage = currentLanguage,
                    onRecordExpense = { title, amount, category, isYesterday ->
                        viewModel.recordExpense(title, amount, category, isYesterday)
                        selectedTabIndex = 0
                    },
                    onAddCategory = { name, emoji, limit ->
                        viewModel.addCategory(name, emoji, limit)
                    },
                    onDeleteCategory = { id ->
                        viewModel.deleteCategory(id)
                    }
                )

                2 -> FreelanceScreen(
                    uiState = uiState,
                    currentCurrency = currentCurrency,
                    currentLanguage = currentLanguage,
                    onDepositFreelance = { source, amount ->
                        viewModel.depositFreelance(source, amount)
                    }
                )

                3 -> ReportsDebtsScreen(
                    uiState = uiState,
                    currentCurrency = currentCurrency,
                    currentLanguage = currentLanguage,
                    onAddDebt = { person, amount, type ->
                        viewModel.addDebt(person, amount, type)
                    },
                    onSettleDebt = { debtId ->
                        viewModel.settleDebt(debtId)
                    },
                    onSetPeriod = { period ->
                        viewModel.setReportPeriod(period)
                    }
                )

                4 -> SettingsScreen(
                    uiState = uiState,
                    currentTheme = currentTheme,
                    currentCorner = currentCorner,
                    currentCurrency = currentCurrency,
                    currentLanguage = currentLanguage,
                    onSelectTheme = onThemeChange,
                    onSelectCorner = onCornerChange,
                    onSelectCurrency = onCurrencyChange,
                    onSelectLanguage = onLanguageChange,
                    onUpdateCycleDuration = { days, endDate ->
                        viewModel.updateCycleDuration(days, endDate)
                    },
                    onResetCycle = { total, days, type, value, endDate ->
                        viewModel.resetCycle(total, days, type, value, endDate)
                    },
                    onUpdateReminders = { daily, threshold, category ->
                        viewModel.updateReminderSettings(daily, threshold, category)
                    },
                    onExportBackup = { callback -> viewModel.exportBackup(callback) },
                    onImportBackup = { json, callback -> viewModel.importBackup(json, callback) }
                )
            }
        }
    }

    // Strict Alert Warning Dialog
    vaultWarning?.let { message ->
        StrictAlertWarningDialog(
            warningMessage = message,
            onDismiss = { viewModel.dismissVaultWarning() }
        )
    }
}
