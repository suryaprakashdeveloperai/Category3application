package com.example.category3.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Theme Colors matching the Desktop Workspace Layout ──────────────────────
private val LocalAmber = Color(0xFFF59E0B)
private val DeepBlue = Color(0xFF1E3A8A)
private val LightBg = Color(0xFFF8FAFC)
private val CardBg = Color(0xFFFFFFFF)

// Core Status Colors & Text Typography Fallbacks
@Composable
fun VacuumPanDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: VacuumPanDedicatedViewModel = viewModel(
        factory = VacuumPanDedicatedViewModel.provideFactory(userName, userRole)
    )
    val live by vm.state.collectAsStateWithLifecycle()

    VacuumPanDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen
    )
}

@Composable
fun VacuumPanDedicatedPageContent(
    live: VacuumPanLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val state = live.dashboard
    val scroll = rememberScrollState()

    val p = live.process
    // Critical process deviation thresholds
    val vacuumFault = p.fceVacuum != 0f && (p.fceVacuum < 620f || p.fceVacuum > 700f)
    val pressureFault = p.fcePressure != 0f && (p.fcePressure < 0.05f || p.fcePressure > 0.30f)
    val brixFault = p.fceBrix != 0f && (p.fceBrix < 82f || p.fceBrix > 92f)
    val vacPumpFault = p.vacuumPumpA in 0f..0.5f

    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(scroll)
        ) {
            // Workspace Header Bar bound to live state attributes
            VacuumPanWorkspaceHeader(
                batchId = state.batchId,
                stability = "%.1f".format(state.processStability),
                status = state.sectionStatus,
                onBack = onBack // Passed down the navigation callback
            )

            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                VpConnectionBanner(status = live.connectionStatus)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left & Center Core Panel Content
                Column(
                    modifier = Modifier.weight(2.2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    VacuumCoreProcessFlowSection(live, vacuumFault, pressureFault, brixFault)
                    VpTelemetryHorizontalGrid(live)
                    VpPeripheralEquipmentSection(state)
                }

                // Right Rail Panel Layout Section
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    VpIdentityContextCard(state)
                    VpBrixTargetAnalyticsCard(state)
                    VpActiveAlertsPanel(live.alerts, vacuumFault, pressureFault, brixFault, vacPumpFault)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Headings & Structural Layout Blocks
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VacuumPanWorkspaceHeader(
    batchId: String,
    stability: String,
    status: EquipmentStatus,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Back Button Points to Workflow Dashboard Screen
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Workflow Dashboard",
                    tint = TextDark
                )
            }

            Text("Vacuum Pan Section", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Spacer(modifier = Modifier.width(8.dp))

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val statusColor = if (isHealthy) StatusGreen else StatusRed

            Box(
                modifier = Modifier
                    .background(statusColor.copy(0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(status.name, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("Process Stability", "$stability / 100")
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("System Mode", "FCE Evaporation")
        }
    }
}

@Composable
fun VacuumCoreProcessFlowSection(
    live: VacuumPanLiveState,
    vacuumFault: Boolean,
    pressureFault: Boolean,
    brixFault: Boolean
) {
    val p = live.process
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stage 1: Absolute Atmospheric Vacuum
        VpStageStepCard(
            step = "1",
            title = "FCE Pressure",
            progress = if (p.fcePressure == 0f) "—" else "%.3f bar".format(p.fcePressure),
            metricLabel = "Inlet Temp",
            metricValue = if (p.fceInletTemp == 0f) "—" else "%.1f °C".format(p.fceInletTemp),
            modifier = Modifier.weight(1f),
            isCritical = pressureFault,
            criticalLabel = "PRESSURE OUT"
        )
        VpFlowArrow()

        // Stage 2: Chamber Evaporation Pressure
        VpStageStepCard(
            step = "2",
            title = "FCE Vacuum Loop",
            progress = if (p.fceVacuum == 0f) "—" else "%.1f mmHg".format(p.fceVacuum),
            metricLabel = "Vacuum Pump V",
            metricValue = if (p.vacuumPumpV == 0f) "—" else "%.1f V".format(p.vacuumPumpV),
            modifier = Modifier.weight(1.1f),
            isCritical = vacuumFault,
            criticalLabel = "VACUUM LIMIT"
        )
        VpFlowArrow()

        // Stage 3: Concentrated Brix Crystallization
        VpStageStepCard(
            step = "3",
            title = "Brix Density Stack",
            progress = if (p.fceBrix == 0f) "—" else "%.2f °Bx".format(p.fceBrix),
            metricLabel = "Outlet Temp",
            metricValue = if (p.fceOutletTemp == 0f) "—" else "%.1f °C".format(p.fceOutletTemp),
            modifier = Modifier.weight(1.2f),
            isCritical = brixFault,
            criticalLabel = "BRIX DEVIATION",
            criticalColor = LocalAmber
        )
    }
}

@Composable
fun VpStageStepCard(
    step: String,
    title: String,
    progress: String,
    metricLabel: String,
    metricValue: String,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    criticalLabel: String = "",
    criticalColor: Color = StatusRed
) {
    val modifierBase = Modifier.background(CardBg, RoundedCornerShape(16.dp))
    val finalModifier = if (isCritical) modifierBase.border(1.5.dp, criticalColor, RoundedCornerShape(16.dp)) else modifierBase

    Box(modifier = modifier.then(finalModifier).padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(24.dp).background(LightBg, CircleShape), contentAlignment = Alignment.Center) {
                Text(step, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)

            if (isCritical) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(criticalColor.copy(0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(criticalLabel, color = criticalColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).background(if (isCritical) criticalColor else StatusGreen, CircleShape))
                Text("Live Loop", fontSize = 11.sp, color = TextGray)
            }

            Spacer(Modifier.height(10.dp))
            Text("Process Reading", fontSize = 10.sp, color = TextGray)
            Text(progress, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isCritical) criticalColor else DeepBlue)

            Spacer(Modifier.height(10.dp))
            Divider(color = BorderGray)
            Spacer(Modifier.height(10.dp))

            Text(metricLabel, fontSize = 11.sp, color = TextGray)
            Text(metricValue, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
fun VpFlowArrow() {
    Text("➔", color = AccentPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun VpTelemetryHorizontalGrid(live: VacuumPanLiveState) {
    val p = live.process
    val telemetry = listOf(
        RowMetricItemVp("Chamber Pressure", if (p.fcePressure == 0f) "—" else "%.3f bar".format(p.fcePressure), "Absolute Level", DeepBlue),
        RowMetricItemVp("Discharge Brix", if (p.fceBrix == 0f) "—" else "%.1f °Bx".format(p.fceBrix), "Crystallization Profile", AccentPurple),
        RowMetricItemVp("Boiler Feed Temp", if (p.fceInletTemp == 0f) "—" else "%.1f °C".format(p.fceInletTemp), "Exchanger Input", TextDark),
        RowMetricItemVp("Exhaust Run Temp", if (p.fceOutletTemp == 0f) "—" else "%.1f °C".format(p.fceOutletTemp), "Exchanger Output", StatusGreen),
        RowMetricItemVp("Vacuum Pump Drive", if (p.vacuumPumpA == 0f) "—" else "%.3f A".format(p.vacuumPumpA), "Induction Motor Load", if (p.vacuumPumpA in 0f..0.5f) StatusRed else TextDark)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        telemetry.forEach { metric ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(metric.label, fontSize = 11.sp, color = TextGray, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    Text(metric.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = metric.color, maxLines = 1)
                    Spacer(Modifier.height(14.dp))
                    Text(metric.desc, fontSize = 10.sp, color = TextGray, maxLines = 1)
                }
            }
        }
    }
}

data class RowMetricItemVp(val label: String, val value: String, val desc: String, val color: Color)

@Composable
fun VpPeripheralEquipmentSection(state: VacuumPanDashboardState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text("🔧 Component Equipment Grid (${state.units.size} Modules)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(12.dp))

            state.units.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { unit ->
                        val statusColor = if (unit.status == EquipmentStatus.FAULT) StatusRed else if (unit.status == EquipmentStatus.RUNNING) StatusGreen else TextGray
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(unit.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("${unit.value}  •  ${unit.statusText}", fontSize = 11.sp, color = TextGray)
                            }
                            Text(unit.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Right Rail Panels & Side Deck Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VpIdentityContextCard(state: VacuumPanDashboardState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🏭 Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            VpFieldRow("Sub-Section Loop", "Vacuum Pan Evaporator")
            VpFieldRow("Active Batch Code", state.batchId)
            VpFieldRow("Shift Start Timestamp", state.startTime)
            VpFieldRow("Current User", state.userName)
            VpFieldRow("System Role Profile", state.userRole)
        }
    }
}

@Composable
fun VpBrixTargetAnalyticsCard(state: VacuumPanDashboardState) {
    val a = state.chart.actual
    val t = state.chart.target
    val gap = a - t
    val gapPct = if (t != 0f) gap / t * 100f else 0f

    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📈 FCE Brix Analytics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            VpFieldRow("Actual Mass Brix", "%.2f °Bx".format(a))
            VpFieldRow("Target Profile Brix", "%.2f °Bx".format(t))
            VpFieldRow("Design Specification", "%.2f °Bx".format(state.chart.design))
            Divider(color = BorderGray)
            VpFieldRow(
                label = "Operational Delta",
                value = "%+.2f °Bx (%+.1f%%)".format(gap, gapPct),
                valueColor = if (gap >= 0) StatusGreen else StatusRed
            )
        }
    }
}

@Composable
fun VpActiveAlertsPanel(
    alerts: List<String>,
    vacuumFault: Boolean,
    pressureFault: Boolean,
    brixFault: Boolean,
    vacPumpFault: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ Live Incident Stack", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)

            if (!vacuumFault && !pressureFault && !brixFault && !vacPumpFault && alerts.isEmpty()) {
                Text("All systems nominal. No process limits breached.", fontSize = 12.sp, color = TextGray)
            } else {
                if (vacuumFault) VpTriggeredAlertRow("Critical: FCE Vacuum Limit Exceeded")
                if (pressureFault) VpTriggeredAlertRow("Critical: Chamber Pressure Boundary")
                if (brixFault) VpTriggeredAlertRow("Warning: Crystallization Brix Deviation")
                if (vacPumpFault) VpTriggeredAlertRow("Hardware: Vacuum Pump Current Drop")

                alerts.forEach { error ->
                    VpTriggeredAlertRow(text = error)
                }
            }
        }
    }
}

@Composable
fun VpTriggeredAlertRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
fun VpFieldRow(label: String, value: String, valueColor: Color = TextDark) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}



@Composable
private fun VpConnectionBanner(status: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAmber.copy(0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, LocalAmber.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("🔄 Connection Stream Status: $status — Retrying stack...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LocalAmber)
    }
}