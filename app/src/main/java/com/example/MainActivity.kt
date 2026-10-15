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
import com.example.ui.components.AppearanceModal
import com.example.ui.components.StrictAlertWarningDialog
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.FreelanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsDebtsScreen
import com.example.ui.theme.CornerStyle
import com.example.ui.theme.LocalAppCornerRadius
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences

sealed class ScreenTab(val index: Int, val title: String, val icon: ImageVector, val tag: String) {
    object Home : ScreenTab(0, "الرئيسية", Icons.Default.Home, "tab_home")
    object AddExpense : ScreenTab(1, "خصم صرف", Icons.Default.RemoveCircle, "tab_add_expense")
    object Freelance : ScreenTab(2, "شغلي", Icons.Default.Work, "tab_freelance")
    object ReportsDebts : ScreenTab(3, "التقارير والديون", Icons.Default.Assessment, "tab_reports_debts")
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
            var showAppearanceModal by remember { mutableStateOf(false) }

            MyApplicationTheme(themeMode = themeMode, cornerStyle = cornerStyle) {
                // Enforce Arabic RTL layout for the entire application
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MasrofiApp(
                        viewModel = viewModel,
                        currentTheme = themeMode,
                        currentCorner = cornerStyle,
                        onOpenAppearanceModal = { showAppearanceModal = true }
                    )

                    if (showAppearanceModal) {
                        AppearanceModal(
                            currentTheme = themeMode,
                            currentCorner = cornerStyle,
                            onSelectTheme = { selectedMode ->
                                themeMode = selectedMode
                                themePrefs.setThemeMode(selectedMode)
                            },
                            onSelectCorner = { selectedCorner ->
                                cornerStyle = selectedCorner
                                themePrefs.setCornerStyle(selectedCorner)
                            },
                            onDismiss = { showAppearanceModal = false }
                        )
                    }
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
    onOpenAppearanceModal: () -> Unit
) {
    val context = LocalContext.current
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

    // Collect toast & notification messages
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
                        ScreenTab.ReportsDebts
                    )

                    tabs.forEach { tab ->
                        val isSelected = selectedTabIndex == tab.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = tab.index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
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
                    onRollbackExpense = { viewModel.rollbackExpense(it) },
                    onEndDay = { viewModel.endDayNow() },
                    onToggleInstituteDay = { viewModel.toggleInstituteDay(it) },
                    onResetCycle = { total, days, vaultPct ->
                        viewModel.resetCycle(total, days, vaultPct)
                    },
                    onOpenAppearanceModal = onOpenAppearanceModal
                )

                1 -> AddExpenseScreen(
                    uiState = uiState,
                    onRecordExpense = { title, amount, category, isYesterday ->
                        viewModel.recordExpense(title, amount, category, isYesterday)
                        selectedTabIndex = 0
                    }
                )

                2 -> FreelanceScreen(
                    uiState = uiState,
                    onDepositFreelance = { source, amount ->
                        viewModel.depositFreelance(source, amount)
                    }
                )

                3 -> ReportsDebtsScreen(
                    uiState = uiState,
                    onAddDebt = { person, amount, type ->
                        viewModel.addDebt(person, amount, type)
                    },
                    onSettleDebt = { debtId ->
                        viewModel.settleDebt(debtId)
                    }
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
