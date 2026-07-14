package com.example.category3.auth.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object AppDestinations {
    const val LOGIN = "login_screen"
    const val WORKFLOW_DASHBOARD = "workflow_dashboard"
    const val DEFECATION_DASHBOARD = "defecation_dashboard"
    const val VACUUM_PAN_DEDICATED = "vacuum_pan_dedicated"

    const val FLOTATION_CLARIFIER_DEDICATED = "flotation_clarifier_live"
    const val ENERGY_TAB = "energy_tab"
    const val PRODUCTION_TAB = "production_tab"
    const val MAINTENANCE_TAB = "maintenance_tab"
    const val MILLMANUALENTRY = "mill_manual"
    const val MILL_DASHBOARD = "mill_dashboard"
    const val MILL_DEDICATED = "mill_dedicated"
    const val DEFECATOR_DEDICATED = "defecator_dedicated"
    // ← NEW: Live SSE Mill View
    const val FLOTATION_CLARIFIER = "flotation_clarifier"
    const val VACCUM_PAN = "vaccum_pan"
    const val OPEN_PAN = "open_pan"
    const val POWDER_MAKER = "powder_maker"
    const val QUALITY_CONTROL = "quality_control"
    const val DCS_SCREEN = "dcs_screen"
    const val ADMIN_PANEL = "admin_panel"
    const val OPENPAN_DASHBOARD = "openpan_dashboard"
    const val CONCENTRATION_DEDICATED = "concentration_dedicated"
    const val HMI_LOGIN = "hmi_login"
    const val HMI_WEB_VIEW = "hmi_web_view"
}

data class MorphicThemeConfig(
    val bgBase: androidx.compose.ui.graphics.Color,
    val textMain: androidx.compose.ui.graphics.Color,
    val textMuted: androidx.compose.ui.graphics.Color,
    val trackBg: androidx.compose.ui.graphics.Color,
    val glassGradientStart: androidx.compose.ui.graphics.Color,
    val glassGradientEnd: androidx.compose.ui.graphics.Color,
    val glassBorder: androidx.compose.ui.graphics.Color,
    val glassShadow: androidx.compose.ui.graphics.Color
)

fun getAdaptiveGlassTheme(isDarkPurple: Boolean): MorphicThemeConfig {
    return MorphicThemeConfig(
        bgBase = androidx.compose.ui.graphics.Color(0xFF2B0955),
        textMain = androidx.compose.ui.graphics.Color.White,
        textMuted = androidx.compose.ui.graphics.Color(0xFFAFA5CA),
        trackBg = androidx.compose.ui.graphics.Color(0x33000000),
        glassGradientStart = androidx.compose.ui.graphics.Color(0x33FFFFFF),
        glassGradientEnd = androidx.compose.ui.graphics.Color(0x0AFFFFFF),
        glassBorder = androidx.compose.ui.graphics.Color(0x4DFFFFFF),
        glassShadow = androidx.compose.ui.graphics.Color(0x66000000)
    )
}

class DashboardUiState

/**
 * Keeps DashboardViewModel's alerts in sync with EnergyViewModel.
 */
@Composable
private fun EnergyAlertsBridge(
    dashboardViewModel: DashboardViewModel,
    energyViewModel: EnergyViewModel
) {
    val energyActiveAlerts = energyViewModel.activeEnergyAlerts.collectAsStateWithLifecycle().value

    LaunchedEffect(energyActiveAlerts) {
        val ids = energyActiveAlerts.map { it.id }.toSet()
        dashboardViewModel.syncEnergyAlerts(ids)

        energyActiveAlerts.forEach { a ->
            dashboardViewModel.injectEnergyAlert(
                AlertData(
                    id = a.id,
                    stage = a.stage,
                    message = a.message,
                    priority = a.priority,
                    type = a.type,
                    description = a.description,
                    sourceRoute = "energy_tab",
                    timestamp = a.timestamp,
                    acknowledged = a.acknowledged,
                    targetSection = a.stage,
                    targetAlertId = a.id
                )
            )
        }
    }
}

/**
 * Keeps DashboardViewModel's alerts in sync with ProductionViewModel.
 */
@Composable
private fun ProductionAlertsBridge(
    dashboardViewModel: DashboardViewModel,
    productionViewModel: ProductionViewModel
) {
    val prodActiveAlerts = productionViewModel.activeProductionAlerts.collectAsStateWithLifecycle().value

    LaunchedEffect(prodActiveAlerts) {
        val ids = prodActiveAlerts.map { it.id }.toSet()
        dashboardViewModel.syncProductionAlerts(ids)

        prodActiveAlerts.forEach { a ->
            dashboardViewModel.injectProductionAlert(
                AlertData(
                    id = a.id,
                    stage = a.stage,
                    message = a.message,
                    priority = a.priority,
                    type = a.type,
                    description = a.description,
                    sourceRoute = "production_tab",
                    timestamp = a.timestamp,
                    acknowledged = a.acknowledged,
                    targetSection = a.stage,
                    targetAlertId = a.id
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val activityViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    // Shared ViewModels (activity scope)
    val dashboardViewModel: DashboardViewModel = viewModel(
        viewModelStoreOwner = activityViewModelStoreOwner,
        factory = DashboardViewModel.provideFactory()
    )
    val energyViewModel: EnergyViewModel = viewModel(
        viewModelStoreOwner = activityViewModelStoreOwner,
        factory = EnergyViewModel.provideFactory()
    )
    val productionViewModel: ProductionViewModel = viewModel(
        viewModelStoreOwner = activityViewModelStoreOwner,
        factory = ProductionViewModel.provideFactory()
    )

    // Always-on bridges (so alerts appear in Workflow dashboard even if tab not opened)
    EnergyAlertsBridge(dashboardViewModel, energyViewModel)
    ProductionAlertsBridge(dashboardViewModel, productionViewModel)

    NavHost(
        navController = navController,
        startDestination = AppDestinations.WORKFLOW_DASHBOARD
    ) {
        // ==================== LOGIN ====================
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ==================== WORKFLOW DASHBOARD ====================
        composable(AppDestinations.WORKFLOW_DASHBOARD) {
            WorkflowDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute)
                }
            )
        }

        // ==================== ENERGY TAB ====================
        composable(
            route = "${AppDestinations.ENERGY_TAB}?section={section}&alertId={alertId}",
            arguments = listOf(
                navArgument("section") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("alertId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val section = backStackEntry.arguments?.getString("section")
            val alertId = backStackEntry.arguments?.getString("alertId")

            EnergyTabScreen(
                dashboardViewModel = dashboardViewModel,
                energyViewModel = energyViewModel,
                initialSection = section,
                initialAlertId = alertId,
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }

        // ==================== PRODUCTION TAB ====================
        composable(
            route = "${AppDestinations.PRODUCTION_TAB}?section={section}&alertId={alertId}",
            arguments = listOf(
                navArgument("section") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("alertId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            ProductionTabScreen(
                dashboardViewModel = dashboardViewModel,
                viewModel = productionViewModel,
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }

        // ==================== ADMIN PANEL ====================
        composable(AppDestinations.ADMIN_PANEL) {
            AdminPanelScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==================== NEW DIAGNOSTIC DASHBOARDS ====================
        composable(AppDestinations.DEFECATION_DASHBOARD) {
            DefecationDiagnosticsHubScreen(
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute)
                }
            )
        }

        // ─── MILL DASHBOARD (Mock/Carousel UI) ───────────────────────────────
        composable(AppDestinations.MILL_DASHBOARD) {



            // 👇 Change this to call the Screen Container instead of the Hub Screen
            MillDiagnosticsScreenContainer(
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute)
                }
            )
        }
        // ─── MILL DEDICATED (Live SSE UI) ← NEW ──────────────────────────────
        composable(AppDestinations.MILL_DEDICATED) {
            MillDedicatedPageScreen(
                userName = "Operator",
                userRole = "Shift Engineer",
                onBack = { navController.popBackStack() },
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }

        composable(AppDestinations.OPENPAN_DASHBOARD) {
            OpenPanDiagnosticsHubScreen(
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute)
                }
            )
        }
        // ==================== HMI PORTAL ====================
        composable(AppDestinations.HMI_LOGIN) {
            IpLoginScreen(
                onNavigateToHmi = { targetUrl ->
                    // IMPORTANT: We MUST URL-encode the IP address before passing it.
                    // Otherwise, Jetpack Navigation gets confused by the "http://" slashes.
                    val encodedUrl = java.net.URLEncoder.encode(
                        targetUrl,
                        java.nio.charset.StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate("${AppDestinations.HMI_WEB_VIEW}?ip=$encodedUrl") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "${AppDestinations.HMI_WEB_VIEW}?ip={ip}",
            arguments = listOf(
                navArgument("ip") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val encodedIp = backStackEntry.arguments?.getString("ip") ?: ""
            // Decode the URL back to its original form (e.g. http://192...)
            val decodedIp = java.net.URLDecoder.decode(
                encodedIp,
                java.nio.charset.StandardCharsets.UTF_8.toString()
            )

            HmiScreen(url = decodedIp)
        }

        // ==================== LEGACY MANUAL / FORM SCREENS ====================
        composable(AppDestinations.MAINTENANCE_TAB) {
            MaintenanceTabScreen(
                onNavigateToScreen = { route ->
                    // Assuming your NavController is named 'navController'
                    navController.navigate(route) {
                        // Optional: Standard tab navigation behavior to avoid stacking backstack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(AppDestinations.MILLMANUALENTRY) {
            MillManualEntryScreen(
                onRaiseTicket = {},
                onNavigationCallback = { navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) }
            )
        }

        composable(AppDestinations.FLOTATION_CLARIFIER) {
            FlotationClarifierScreen(
                onRaiseTicket = {},
                onNavigationCallback = { navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) }
            )
        }

        composable(AppDestinations.VACCUM_PAN) {
            VacuumPanScreen(
                onRaiseTicket = {},
                onNavigationCallback = { navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) }
            )
        }

        composable(AppDestinations.OPEN_PAN) {
            OpenPanScreen(
                onRaiseTicket = {},
                onNavigationCallback = { navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) }
            )
        }
        // ─── DEFECATOR DEDICATED (Live SSE UI) ──────────────────────────────
        composable(AppDestinations.DEFECATOR_DEDICATED) {
            DefecatorDedicatedPageScreen(
                userName = "Operator",
                userRole = "Shift Engineer",
                onBack = { navController.popBackStack() },
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }
        composable(AppDestinations.VACUUM_PAN_DEDICATED) {
            VacuumPanDedicatedPageScreen(
                userName = "Operator",
                userRole = "Shift Engineer",
                onBack = { navController.popBackStack() },
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }
        composable(AppDestinations.FLOTATION_CLARIFIER_DEDICATED) {
            FlotationClarifierDedicatedPageScreen()
        }
        composable(AppDestinations.CONCENTRATION_DEDICATED) {
            ConcentrationDedicatedPageScreen(
                userName = "Operator",
                userRole = "Shift Engineer",
                onBack = { navController.popBackStack() },
                onNavigateToScreen = { targetRoute ->
                    navController.navigate(targetRoute) { launchSingleTop = true }
                }
            )
        }

        composable(AppDestinations.POWDER_MAKER) {
            PowderMakerScreen(
                onRaiseTicket = {},
                onNavigationCallback = { navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) }
            )
        }


        composable(AppDestinations.QUALITY_CONTROL) {
            QualityControlScreen(onNavigationCallback = {
                navController.navigate(AppDestinations.WORKFLOW_DASHBOARD)
            })
        }

        composable(AppDestinations.DCS_SCREEN) {
            DcsScreen(onNavigationCallback = {
                navController.navigate(AppDestinations.WORKFLOW_DASHBOARD)
            })
        }
    }
}