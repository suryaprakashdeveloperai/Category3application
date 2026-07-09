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
import kotlin.math.abs

// ─── Theme Colors matching the Desktop Workspace Layout ──────────────────────
private val LocalAmber = Color(0xFFF59E0B)
private val DeepBlue = Color(0xFF1E3A8A)
private val LightBg = Color(0xFFF8FAFC)
private val CardBg = Color(0xFFFFFFFF)

// Core Status & Text Typography Constants
@Composable
fun DefecatorDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: DefecatorDedicatedViewModel = viewModel(
        factory = DefecatorDedicatedViewModel.provideFactory(userName, userRole)
    )
    val live by vm.state.collectAsStateWithLifecycle()

    DefecatorDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen
    )
}

@Composable
fun DefecatorDedicatedPageContent(
    live: DefecatorLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val state = live.dashboard
    val scroll = rememberScrollState()

    // Pre-calculated Fault & Deviation flags mapped out of VM specifications
    val phFault = live.process.pH != 0f && (live.process.pH < 6.8f || live.process.pH > 7.8f)
    val djPumpFault = live.process.djActivePumpA in 0f..0.5f
    val heater3Dev = abs(live.process.heater3PvC - live.process.heater3SpC)
    val heater3Fault = live.process.heater3SpC > 0f && heater3Dev > 5f

    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(scroll)
        ) {
            // Top Dashboard Section Header linking section status color rules
            DefecatorWorkspaceHeader(
                batchId = state.batchId,
                stability = "%.1f".format(state.processStability),
                status = state.sectionStatus,
                onBack = onBack // Passed navigation callback
            )

            // Live Diagnostics Banners
            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                ConnectionBannerDefecator(status = live.connectionStatus)
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left & Center Column: Juicing Process Steps + Live Grid
                Column(
                    modifier = Modifier.weight(2.2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DefecationProcessFlowSection(live, phFault, heater3Fault, heater3Dev)
                    ClearJuiceTelemetryGridSection(live)
                    EquipmentStatusListSection(state)
                }

                // Right Side Rail: Safety & Operational Insights
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IdentitySummaryPanel(state)
                    FlowAnalysisCard(state)
                    AlarmsCriticalPanel(live.alerts, phFault, djPumpFault, heater3Fault)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI Components & Structural Blocks
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DefecatorWorkspaceHeader(
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

            Text("Defecation Section", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val badgeColor = if (isHealthy) StatusGreen else StatusRed

            Box(
                modifier = Modifier
                    .background(badgeColor.copy(0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(badgeColor, CircleShape))
                    Text(status.name, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("Process Stability", "$stability / 100")
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("System Grid", "Clarification Loop")
        }
    }
}

@Composable
fun DefecationProcessFlowSection(
    live: DefecatorLiveState,
    phFault: Boolean,
    heater3Fault: Boolean,
    heater3Dev: Float
) {
    val p = live.process
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stage 1: Intake & Tanks
        StageStepCard("1", "DJ Tank Intake", "${"%.1f".format(p.djTankLevel)}%", "Tank Current Load", "${"%.2f".format(p.djActivePumpA)} A", Modifier.weight(1f))
        FlowArrow()

        // Stage 2: Neutralization (pH Core Monitoring)
        StageStepCard(
            step = "2",
            title = "Defecator Neutralizer",
            progress = if (p.pH == 0f) "—" else "%.2f pH".format(p.pH),
            metricLabel = "Buffer Tank Level",
            metricValue = "%.1f mm".format(p.srtBufferLevel),
            modifier = Modifier.weight(1.1f).run {
                if (phFault) border(1.5.dp, StatusRed.copy(0.7f), RoundedCornerShape(16.dp)) else this
            },
            isCritical = phFault,
            criticalLabel = "pH OUT OF SPEC"
        )
        FlowArrow()

        // Stage 3: Heat Exchange Thermal Stack
        StageStepCard(
            step = "3",
            title = "Thermal Stage 3",
            progress = "%.1f °C".format(p.heater3PvC),
            metricLabel = "Steam Control Valve",
            metricValue = "%.1f %%".format(p.heater3SteamValvePct),
            modifier = Modifier.weight(1.2f).run {
                if (heater3Fault) border(1.5.dp, LocalAmber.copy(0.7f), RoundedCornerShape(16.dp)) else this
            },
            isCritical = heater3Fault,
            criticalLabel = "THERMAL DEV Δ${"%.1f".format(heater3Dev)}°C",
            criticalColor = LocalAmber,
            extraMetrics = listOf(
                "Heater 1 Out" to "%.1f°C".format(p.heater1OutletC),
                "Heater 2 Out" to "%.1f°C".format(p.heater2OutletC)
            )
        )
    }
}

@Composable
fun StageStepCard(
    step: String,
    title: String,
    progress: String,
    metricLabel: String,
    metricValue: String,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    criticalLabel: String = "",
    criticalColor: Color = StatusRed,
    extraMetrics: List<Pair<String, String>> = emptyList()
) {
    Box(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
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

            Spacer(Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).background(if (isCritical) criticalColor else StatusGreen, CircleShape))
                Text("Monitoring", fontSize = 11.sp, color = TextGray)
            }

            Spacer(Modifier.height(10.dp))
            Text("Measured PV", fontSize = 10.sp, color = TextGray)
            Text(progress, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isCritical) criticalColor else DeepBlue)

            Spacer(Modifier.height(10.dp))
            Divider(color = BorderGray)
            Spacer(Modifier.height(10.dp))

            if (extraMetrics.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    extraMetrics.forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 10.sp, color = TextGray)
                            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(metricLabel, fontSize = 11.sp, color = TextGray)
            Text(metricValue, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
fun FlowArrow() {
    Text("➔", color = AccentPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun ClearJuiceTelemetryGridSection(live: DefecatorLiveState) {
    val cj = live.clearJuice
    val telemetry = listOf(
        RowMetricItemDefecator("Clear Juice Flow", "%,.0f L/hr".format(cj.flow), "System Delivery Rate", DeepBlue),
        RowMetricItemDefecator("CJ Tank Level", "%.1f%%".format(cj.tankLevel), "Storage Capacity", AccentPurple),
        RowMetricItemDefecator("Juice Density", "%.2f kg/m³".format(cj.density), "Mass Concentration", TextDark),
        RowMetricItemDefecator("Exchanger Heat Out", "%.1f °C".format(cj.heaterOutletC), "Exchanger Delivery Temp", StatusGreen),
        RowMetricItemDefecator("Clarifier Filter", if (cj.filterOn) "ENGAGED" else "BYPASS", "Active System Stack", if (cj.filterOn) StatusGreen else TextGray)
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

data class RowMetricItemDefecator(val label: String, val value: String, val desc: String, val color: Color)

@Composable
fun EquipmentStatusListSection(state: DefecatorDashboardState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text("🔧 Peripheral Equipment Clusters (${state.units.size} Modules)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(12.dp))

            state.units.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { unit ->
                        val itemColor = if (unit.status == EquipmentStatus.FAULT) StatusRed else if (unit.status == EquipmentStatus.RUNNING) StatusGreen else TextGray
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(itemColor, CircleShape))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(unit.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("${unit.value}  •  ${unit.statusText}", fontSize = 11.sp, color = TextGray)
                            }
                            Text(unit.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = itemColor)
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Right Rail Panel Infrastructure Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IdentitySummaryPanel(state: DefecatorDashboardState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🏭 Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            FieldRow("Sub-Section Loop", "Defecator Clarifier")
            FieldRow("Active Batch Code", state.batchId)
            FieldRow("Shift Start Timestamp", state.startTime)
            FieldRow("Current User", state.userName)
            FieldRow("System Role Profile", state.userRole)
        }
    }
}

@Composable
fun FlowAnalysisCard(state: DefecatorDashboardState) {
    val a = state.chart.actual
    val t = state.chart.target
    val gap = a - t
    val gapPct = if (t > 0f) gap / t * 100f else 0f

    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📈 Discharge Volume Analytics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            FieldRow("Actual Clear Flow", "%,.0f L/hr".format(a))
            FieldRow("Target Profile Flow", "%,.0f L/hr".format(t))
            FieldRow("Design Specification", "%,.0f L/hr".format(state.chart.design))
            Divider(color = BorderGray)
            FieldRow(
                label = "Operational Delta",
                value = "%+.0f L/hr (%+.1f%%)".format(gap, gapPct),
                valueColor = if (gap >= 0) StatusGreen else StatusRed
            )
        }
    }
}

@Composable
fun AlarmsCriticalPanel(
    alerts: List<String>,
    phFault: Boolean,
    djPumpFault: Boolean,
    heater3Fault: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ Live Incident Stack", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)

            if (!phFault && !djPumpFault && !heater3Fault && alerts.isEmpty()) {
                Text("All systems nominal. No process limits breached.", fontSize = 12.sp, color = TextGray)
            } else {
                if (phFault) TriggeredAlertRow("Critical: pH Out of Range")
                if (djPumpFault) TriggeredAlertRow("Hardware: DJ Intake Pump Loss")
                if (heater3Fault) TriggeredAlertRow("Thermal: Heater 3 Delta Limit")

                alerts.forEach { error ->
                    TriggeredAlertRow(text = error)
                }
            }
        }
    }
}

@Composable
fun TriggeredAlertRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
fun FieldRow(label: String, value: String, valueColor: Color = TextDark) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun ConnectionBannerDefecator(status: String) {
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