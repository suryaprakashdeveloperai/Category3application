package com.example.category3.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ============================================================================
// 📡 CENTRALIZED ROUTING KEYS ACROSS THE MES ARCHITECTURE
// ============================================================================
object AppDestinations {
    const val LOGIN = "login_screen"
    const val WORKFLOW_DASHBOARD = "workflow_dashboard"
    const val MILL_DASHBOARD = "mill_dashboard"
    const val FLOTATION_CLARIFIER = "flotation_clarifier"
    const val VACCUM_PAN = "vaccum_pan"
    const val OPEN_PAN = "open_pan"
    const val POWDER_MAKER = "powder_maker"
    const val QUALITY_CONTROL = "quality_control"
    const val DCS_SCREEN = "dcs_screen"
}

// ============================================================================
// 🎨 LEGACY DATA & THEME CONFIG (Required for older screens)
// ============================================================================
// We keep this here so your older screens that haven't been updated to the
// new Light UI yet don't break during compilation.

class DashboardUiState // Placeholder to satisfy MillDiagnosticsHubScreen signature

data class MorphicThemeConfig(
    val bgBase: Color, val textMain: Color, val textMuted: Color, val trackBg: Color,
    val glassGradientStart: Color, val glassGradientEnd: Color, val glassBorder: Color, val glassShadow: Color
)

fun getAdaptiveGlassTheme(isDarkPurple: Boolean): MorphicThemeConfig {
    return MorphicThemeConfig(
        bgBase = Color(0xFF2B0955), textMain = Color(0xFFFFFFFF), textMuted = Color(0xFFAFA5CA),
        trackBg = Color(0x33000000), glassGradientStart = Color(0x33FFFFFF), glassGradientEnd = Color(0x0AFFFFFF),
        glassBorder = Color(0x4DFFFFFF), glassShadow = Color(0x66000000)
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.WORKFLOW_DASHBOARD
    ) {
        // ============================================================================
        // 📊 MAIN HUB: WORKFLOW PRODUCTION DASHBOARD
        // ============================================================================
        composable(route = AppDestinations.WORKFLOW_DASHBOARD) {
            WorkflowDashboardScreen(
//                onNavigateToScreen = { targetRoute ->
//                    navController.navigate(targetRoute)
//                }
            )
        }

        // ============================================================================
        // 1. GATEWAY: AUTHENTICATION INTERFACE
        // ============================================================================
        composable(route = AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { targetRoute ->
                    when (targetRoute) {
                        AppDestinations.WORKFLOW_DASHBOARD, "workflow_dashboard" -> navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.MILL_DASHBOARD, "mill_dashboard" -> navController.navigate(AppDestinations.MILL_DASHBOARD) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.FLOTATION_CLARIFIER, "flotation_clarifier" -> navController.navigate(AppDestinations.FLOTATION_CLARIFIER) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.VACCUM_PAN, "vaccum_pan" -> navController.navigate(AppDestinations.VACCUM_PAN) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.OPEN_PAN, "open_pan" -> navController.navigate(AppDestinations.OPEN_PAN) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.DCS_SCREEN, "dcs_screen" -> navController.navigate(AppDestinations.DCS_SCREEN) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.POWDER_MAKER, "powder_maker" -> navController.navigate(AppDestinations.POWDER_MAKER) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        AppDestinations.QUALITY_CONTROL, "quality_control" -> navController.navigate(AppDestinations.QUALITY_CONTROL) { popUpTo(AppDestinations.LOGIN) { inclusive = true } }
                        else -> println("LOG SYSTEM DIRECTION WARNING: Route '$targetRoute' unhandled in NavHost matrix.")
                    }
                }
            )
        }

        // ============================================================================
        // 2. PRODUCTION: PLC MILL ENTRY COCKPIT / DIAGNOSTICS HUB
        // ============================================================================
        composable(route = AppDestinations.MILL_DASHBOARD) {
            MillDiagnosticsHubScreen(
                uiState = DashboardUiState(), // Satisfied by the placeholder class above
                jobs = emptyList(),
                currentTheme = getAdaptiveGlassTheme(isDarkPurple = true),
                onUpdateJobStatus = { _, _ -> },
                onNavigateBack = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) {
                        popUpTo(AppDestinations.MILL_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // ============================================================================
        // 3. PRODUCTION: FLOTATION CLARIFIER COCKPIT
        // ============================================================================
        composable(route = AppDestinations.FLOTATION_CLARIFIER) {
            FlotationClarifierScreen(
                onRaiseTicket = { exceptionMessage -> println("LOG SYSTEM TICKET [CLARIFIER]: $exceptionMessage") },
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.FLOTATION_CLARIFIER) { inclusive = true } }
                }
            )
        }

        // ============================================================================
        // 4. PRODUCTION: VACUUM PAN CRYSTALLIZATION TERMINAL
        // ============================================================================
        composable(route = AppDestinations.VACCUM_PAN) {
            VacuumPanScreen(
                onRaiseTicket = { exceptionMessage -> println("LOG SYSTEM TICKET [VACUUM PAN]: $exceptionMessage") },
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.VACCUM_PAN) { inclusive = true } }
                }
            )
        }

        // ============================================================================
        // 5. PRODUCTION: OPEN BOILING PAN PROCESS INTERFACE
        // ============================================================================
        composable(route = AppDestinations.OPEN_PAN) {
            OpenPanScreen(
                onRaiseTicket = { exceptionMessage -> println("LOG SYSTEM TICKET [OPEN PAN]: $exceptionMessage") },
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.OPEN_PAN) { inclusive = true } }
                }
            )
        }

        // ============================================================================
        // 6. PRODUCTION: POWDER MAKER MACHINE SYSTEM REFINERY
        // ============================================================================
        composable(route = AppDestinations.POWDER_MAKER) {
            PowderMakerScreen(
                onRaiseTicket = { exceptionMessage -> println("LOG SYSTEM TICKET [POWDER MAKER]: $exceptionMessage") },
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.POWDER_MAKER) { inclusive = true } }
                }
            )
        }

        // ============================================================================
        // 7. LABORATORY: FSSAI QUALITY CONTROL ENTRY TERMINAL
        // ============================================================================
        composable(route = AppDestinations.QUALITY_CONTROL) {
            QualityControlScreen(
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.QUALITY_CONTROL) { inclusive = true } }
                }
            )
        }

        // ============================================================================
        // 8. DCS CONTROL TERMINAL
        // ============================================================================
        composable(route = AppDestinations.DCS_SCREEN) {
            DcsScreen(
                onNavigationCallback = {
                    navController.navigate(AppDestinations.WORKFLOW_DASHBOARD) { popUpTo(AppDestinations.DCS_SCREEN) { inclusive = true } }
                }
            )
        }
    }
}