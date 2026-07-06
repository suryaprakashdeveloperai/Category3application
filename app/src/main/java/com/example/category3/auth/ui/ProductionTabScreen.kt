package com.example.category3.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.components.RadialAppBar

@Composable
fun ProductionTabScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit,
    viewModel: ProductionViewModel = viewModel(factory = ProductionViewModel.provideFactory())
) {
    val theme = getAdaptiveTheme(false)
    val bgBrush = if (theme.isDark) Brush.linearGradient(listOf(BrandDeepNavy, BrandDarkBlueGray))
    else Brush.linearGradient(listOf(BrandOffWhite, Color.White))

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val kpis by viewModel.kpis.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val activeProdAlerts by viewModel.activeProductionAlerts.collectAsStateWithLifecycle()

    // Bridge -> Dashboard
    LaunchedEffect(activeProdAlerts) {
        dashboardViewModel.syncProductionAlerts(activeProdAlerts.map { it.id }.toSet())
    }
    LaunchedEffect(Unit) {
        viewModel.productionAlerts.collect { p ->
            dashboardViewModel.injectProductionAlert(
                AlertData(
                    id = p.id, stage = p.stage, message = p.message,
                    priority = p.priority, type = p.type, description = p.description,
                    sourceRoute = "production_tab", timestamp = p.timestamp,
                    targetSection = p.stage, targetAlertId = p.id, acknowledged = p.acknowledged
                )
            )
        }
    }

    fun kpi(id: String) = kpis.firstOrNull { it.id == id }
    fun statusColor(k: ProductionKpi?, normal: Color = AccentPrimary): Color = when (k?.status) {
        KpiStatus.WARNING -> AccentWarning
        KpiStatus.CRITICAL -> AccentCritical
        KpiStatus.NORMAL -> normal
        null -> theme.textMuted
    }

    @Composable fun GlobalThroughputPanel(m: Modifier) {
        val gt = kpi("global_throughput")
        CleanPanel(theme, m) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(AccentPrimary.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Factory, null, tint = AccentPrimary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("GLOBAL THROUGHPUT", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(connectionStatus, color = theme.textMuted, fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(gt?.displayValue ?: "--", color = theme.textMain, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text(" ${gt?.unit ?: "MT/Day"}", color = AccentPrimary, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }
        }
    }

    @Composable fun SimpleKpiPanel(m: Modifier, id: String, title: String, defUnit: String, normal: Color) {
        val k = kpi(id)
        CleanPanel(theme, m) { KpiCell(theme, title, k?.displayValue ?: "--", k?.unit ?: defUnit, statusColor(k, normal)) }
    }

    @Composable fun RiskPanel(m: Modifier) {
        val k = kpi("bottleneck_risk")
        val risk = k?.displayValue ?: "--"
        val c = when (risk.lowercase()) { "low" -> AccentSuccess; "medium" -> AccentWarning; "high" -> AccentCritical; else -> theme.textMuted }
        CleanPanel(theme, m) { KpiCell(theme, "Bottleneck Risk", risk, "", c) }
    }

    @Composable fun SectionPanel(m: Modifier, title: String, rows: List<Triple<String, String, Color>>) {
        CleanPanel(theme, m) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                rows.forEach { (label, valUnit, color) ->
                    val parts = valUnit.split("§")
                    KpiRow(theme, label, parts.getOrElse(0) { "--" }, parts.getOrElse(1) { "" }, color)
                }
            }
        }
    }

    fun row(id: String, label: String, defUnit: String, normal: Color = AccentPrimary): Triple<String, String, Color> {
        val k = kpi(id)
        return Triple(label, "${k?.displayValue ?: "--"}§${k?.unit ?: defUnit}", statusColor(k, normal))
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        val content: @Composable (Boolean) -> Unit = { portrait ->
            if (portrait) {
                Column(
                    Modifier.fillMaxSize().padding(start = 64.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlobalThroughputPanel(Modifier.fillMaxWidth().height(110.dp))
                    Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SimpleKpiPanel(Modifier.weight(1f).fillMaxHeight(), "target_achieved", "Target", "%", AccentSuccess)
                        SimpleKpiPanel(Modifier.weight(1f).fillMaxHeight(), "overall_yield", "Yield", "%", AccentPrimary)
                        RiskPanel(Modifier.weight(1f).fillMaxHeight())
                    }
                    SectionPanel(Modifier.fillMaxWidth().height(160.dp), "CANE & MILLING",
                        listOf(row("milling_throughput", "Cane Crushed", "T/hr"), row("cane_stock", "Cane Stock", "%", AccentSuccess)))
                    SectionPanel(Modifier.fillMaxWidth().height(200.dp), "CLARIFICATION",
                        listOf(row("raw_juice_flow", "Raw Juice", "T/hr"), row("raw_juice_temp", "Temp", "°C"),
                            row("defecator_ph", "pH", "pH", AccentSuccess), row("clear_juice_tank_level", "CJ Tank", "%")))
                    SectionPanel(Modifier.fillMaxWidth().height(200.dp), "EVAPORATION",
                        listOf(row("evap_body1_temp", "Body1 Temp", "°C"), row("evap_b4_vac", "Vac B4", "mmHg"),
                            row("evap_b5_vac", "Vac B5", "mmHg"), row("evap_b5_brix", "Brix", "Bx", AccentSuccess)))
                    SectionPanel(Modifier.fillMaxWidth().height(200.dp), "CONCENTRATION & PANS",
                        listOf(row("fce_brix", "FCE Brix", "Bx", AccentSuccess), row("fce_vacuum", "FCE Vac", "mmHg"),
                            row("opan_running", "Pans", "Nos"), row("opan_avg_temp", "Pan Temp", "°C")))
                    SectionPanel(Modifier.fillMaxWidth().height(200.dp), "PRODUCTION",
                        listOf(row("total_produced", "Total", "kg"), row("batches_completed", "Batches", ""),
                            row("yield_efficiency", "Yield", "%", AccentSuccess), row("shift", "Shift", "")))
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(start = 76.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth().weight(0.8f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlobalThroughputPanel(Modifier.weight(1.5f).fillMaxHeight())
                        SimpleKpiPanel(Modifier.weight(1f).fillMaxHeight(), "target_achieved", "Target", "%", AccentSuccess)
                        SimpleKpiPanel(Modifier.weight(1f).fillMaxHeight(), "overall_yield", "Yield", "%", AccentPrimary)
                        RiskPanel(Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(Modifier.fillMaxWidth().weight(1.5f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionPanel(Modifier.weight(1f).fillMaxHeight(), "CANE & MILLING",
                            listOf(row("milling_throughput", "Cane Crushed", "T/hr"), row("cane_stock", "Cane Stock", "%", AccentSuccess)))
                        SectionPanel(Modifier.weight(1f).fillMaxHeight(), "CLARIFICATION",
                            listOf(row("raw_juice_flow", "Raw Juice", "T/hr"), row("raw_juice_temp", "Temp", "°C"),
                                row("defecator_ph", "pH", "pH", AccentSuccess), row("clear_juice_tank_level", "CJ Tank", "%")))
                        SectionPanel(Modifier.weight(1f).fillMaxHeight(), "EVAPORATION",
                            listOf(row("evap_body1_temp", "Body1 Temp", "°C"), row("evap_b4_vac", "Vac B4", "mmHg"),
                                row("evap_b5_vac", "Vac B5", "mmHg"), row("evap_b5_brix", "Brix", "Bx", AccentSuccess)))
                    }
                    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionPanel(Modifier.weight(1f).fillMaxHeight(), "CONCENTRATION & PANS",
                            listOf(row("fce_brix", "FCE Brix", "Bx", AccentSuccess), row("fce_vacuum", "FCE Vac", "mmHg"),
                                row("opan_running", "Pans", "Nos"), row("opan_avg_temp", "Pan Temp", "°C")))
                        SectionPanel(Modifier.weight(1f).fillMaxHeight(), "PRODUCTION",
                            listOf(row("total_produced", "Total", "kg"), row("yield_efficiency", "Yield", "%", AccentSuccess),
                                row("shift", "Shift", ""), row("maintenance", "Maint", "")))
                    }
                }
            }
        }

        content(isPortrait)

        RadialAppBar(
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-8).dp).zIndex(30f),
            activeSection = "production_tab",
            onActionSelected = { onNavigateToScreen(it) }
        )
    }
}