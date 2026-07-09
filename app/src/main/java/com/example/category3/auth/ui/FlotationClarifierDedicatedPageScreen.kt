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

// ─── Theme Colors ───────────────────────────────────────────────────────────
private val LocalAmber = Color(0xFFF59E0B)
private val DeepBlue = Color(0xFF1E3A8A)
private val LightBg = Color(0xFFF8FAFC)
private val CardBg = Color(0xFFFFFFFF)

// Core Typography Status Layout Elements

@Composable
fun FlotationClarifierDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: FlotationClarifierDedicatedViewModel = viewModel(
        factory = FlotationClarifierDedicatedViewModel.provideFactory(userName, userRole)
    )
    val live by vm.state.collectAsStateWithLifecycle()

    FlotationClarifierDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen
    )
}

@Composable
fun FlotationClarifierDedicatedPageContent(
    live: FlotationClarifierLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val state = live.dashboard
    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(scroll)
        ) {
            // Workspace Top Bar Header - Synchronized to read sectionStatus safely
            FlotationWorkspaceHeader(
                batchId = state.batchId,
                statusText = state.sectionStatus.name,
                statusColor = when (state.sectionStatus) {
                    EquipmentStatus.FAULT -> StatusRed
                    EquipmentStatus.RUNNING, EquipmentStatus.HEALTHY -> StatusGreen
                    else -> TextGray
                },
                onBack = onBack // Attached navigation callback
            )

            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                BannerLine("Stream ${live.connectionStatus} — attempting to reconnect…", LocalAmber)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left & Middle Column (Process Stack Units & Telemetry Blocks)
                Column(
                    modifier = Modifier.weight(2.2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ClarifierProcessFlowSection(state.units)
                    ProcessOutputsHorizontalGrid(live)
                }

                // Right Side Rail (Analytics & Context Cards)
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IdentityContextCard(state)
                    PerformanceKpiPanel(state.kpis)
                    FcActiveAlertsPanel(live.alerts)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Headings & Structural Layout Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FlotationWorkspaceHeader(
    batchId: String,
    statusText: String,
    statusColor: Color,
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

            Text("Flotation Clarifier Section", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(statusColor.copy(0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("System Grid", "Flotation Separation Loop")
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("Shift Tracking", "Live Performance Stack")
        }
    }
}

@Composable
fun ClarifierProcessFlowSection(units: List<ClarifierUnitLive>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("🔧 Clarifier Operational Units", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            units.forEachIndexed { index: Int, unit: ClarifierUnitLive ->
                val isCritical = unit.status == EquipmentStatus.FAULT
                val cardColor = if (isCritical) StatusRed else if (unit.status == EquipmentStatus.RUNNING) StatusGreen else TextGray

                val modifierBase = Modifier.weight(1f).background(CardBg, RoundedCornerShape(16.dp))
                val cardModifier = if (isCritical) {
                    modifierBase.border(1.5.dp, StatusRed, RoundedCornerShape(16.dp))
                } else {
                    modifierBase
                }

                Box(modifier = cardModifier.padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(24.dp).background(LightBg, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(unit.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)

                        if (unit.statusText.isNotBlank()) {
                            Text(unit.statusText, fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(top = 2.dp), maxLines = 1)
                        }

                        Spacer(Modifier.height(28.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(cardColor, CircleShape))
                            Text(unit.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cardColor)
                        }
                        Spacer(Modifier.height(10.dp))
                        Divider(color = BorderGray)
                        Spacer(Modifier.height(10.dp))

                        KVfcLocal("Drive Motor VFD", "${"%.1f".format(unit.vfdSpeedPct)} %")
                        KVfcLocal("Inlet Valve Stat", if (unit.inletOpen) "OPEN" else "CLOSED")
                        KVfcLocal("Outlet Valve Stat", if (unit.outletOpen) "OPEN" else "CLOSED")
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessOutputsHorizontalGrid(live: FlotationClarifierLiveState) {
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw
    val vacPumpColor = if (live.vacuumPumpStatus == EquipmentStatus.RUNNING) StatusGreen else TextGray

    val outputItems = listOf(
        MetricDataFc("Clear Juice Flow", "%,.0f L/hr".format(cjFlowLhr), "Discharge Delivery", DeepBlue),
        MetricDataFc("CJ Tank Level", "${"%.1f".format(live.clearJuiceTankLevel)}%", "Storage Buffering", AccentPurple),
        MetricDataFc("Clear Juice Density", "%.2f kg/m³".format(live.clearJuiceDensity), "Juice Quality Index", TextDark),
        MetricDataFc("FC MOND Flow", "%,.1f L/h".format(live.fcMondFlow), "Process Auxiliary Flow", DeepBlue),
        MetricDataFc("Filter Vacuum Pump", live.vacuumPumpStatus.name, "Active System Block", vacPumpColor)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        outputItems.forEach { metric ->
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
                    Text(metric.description, fontSize = 10.sp, color = TextGray, maxLines = 1)
                }
            }
        }
    }
}

data class MetricDataFc(val label: String, val value: String, val description: String, val color: Color)

// ─────────────────────────────────────────────────────────────────────────────
// Right Rail Panels & Side Deck
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IdentityContextCard(state: FlotationClarifierDashboardState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🏭 Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            SummaryFieldRowFc("Section Namespace", "Flotation Clarifier Loop")
            SummaryFieldRowFc("Active Batch Reference", state.batchId)
            SummaryFieldRowFc("Shift Authorization Start", state.startTime)
            SummaryFieldRowFc("Allocated Specialist", state.userName)
            SummaryFieldRowFc("Role Configuration", state.userRole)
        }
    }
}

@Composable
fun PerformanceKpiPanel(kpis: List<Pair<String, String>>) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📊 Performance KPIs (Live)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)

            kpis.forEach { (metricName, valueString) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(metricName, fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    Text(valueString, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
        }
    }
}

@Composable
fun FcActiveAlertsPanel(alerts: List<String>) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ Critical Operational Alerts (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)
            if (alerts.isEmpty()) {
                Text("Process thresholds nominal. No limit breaches.", fontSize = 12.sp, color = TextGray)
            } else {
                alerts.forEach { error ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(error, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryFieldRowFc(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun KVfcLocal(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextGray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun BannerLine(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}