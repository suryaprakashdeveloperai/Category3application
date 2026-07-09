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

// ─── Theme Colors matching the Concentration Grid ───────────────────────────
private val LocalAmber = Color(0xFFF59E0B)
private val DeepBlue = Color(0xFF1E3A8A)
private val LightBg = Color(0xFFF8FAFC)
private val CardBg = Color(0xFFFFFFFF)

// Additional layout theme fallbacks

@Composable
fun ConcentrationDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    // 1. Hooking into the dedicated factory instance matching live telemetry specs
    val vm: ConcentrationDedicatedViewModel = viewModel(
        factory = ConcentrationDedicatedViewModel.provideFactory(userName, userRole)
    )
    // 2. Continuous observation of the state flow context
    val live by vm.state.collectAsStateWithLifecycle()

    ConcentrationDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen
    )
}

@Composable
fun ConcentrationDedicatedPageContent(
    live: ConcentrationDedicatedLiveState,
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
            // Top Workspace Header rendering stream attributes
            ConcentrationHeader(
                batchId = state.batchId,
                statusText = state.sectionStatus.name,
                statusColor = if (state.sectionStatus == EquipmentStatus.FAULT) StatusRed else StatusGreen,
                shiftStart = state.startTime,
                onBack = onBack // Passed onBack to header
            )

            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                BannerLocal("Stream status is ${live.connectionStatus} — establishing cloud sync telemetry node…", LocalAmber)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left & Middle Column (Dynamic System Units Data & Quick Metrics)
                Column(
                    modifier = Modifier.weight(2.2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    UnitProcessFlowSection(state.openPans, state.powderMakers)
                    QuickMetricsGridSection(live = live)
                }

                // Right Side Rail (Analytics & Summary Panels)
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IdentitySummaryCard(state = state)
                    SteamTelemetryCard(live = live)
                    ActiveAlertsPanel(alerts = live.alerts)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Headings & Layout Structural Blocks (Bound to Live State)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConcentrationHeader(
    batchId: String,
    statusText: String,
    statusColor: Color,
    shiftStart: String,
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

            Text("Concentration Section", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Spacer(modifier = Modifier.size(8.dp))

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
            HeaderMetaItem("System Mode", "Open Pan + Powder Maker")
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("Shift Log Target", shiftStart)
        }
    }
}

@Composable
fun UnitProcessFlowSection(
    openPans: List<ConcentrationOpenPanUnit>,
    powderMakers: List<ConcentrationPowderMakerUnit>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("🍯 Active Processing Units", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

        // Renders all 4 elements provided dynamically from incoming telemetry stream updates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            openPans.forEachIndexed { index, op ->
                val isCritical = op.status == EquipmentStatus.FAULT
                val cardColor = if (isCritical) StatusRed else if (op.status == EquipmentStatus.RUNNING) StatusGreen else TextGray

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CardBg, RoundedCornerShape(16.dp))
                        .run { if (isCritical) border(1.5.dp, StatusRed, RoundedCornerShape(16.dp)) else this }
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(24.dp).background(LightBg, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(op.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(cardColor, CircleShape))
                            Text(op.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cardColor)
                        }
                        Spacer(Modifier.height(10.dp))
                        Divider(color = BorderGray)
                        Spacer(Modifier.height(10.dp))

                        KV("Temperature", "${"%.1f".format(op.tempC)} °C")
                        KV("Amperage", "${"%.2f".format(op.amps)} A")
                        KV("Agitator", "${"%.1f".format(op.rpm)} RPM")
                        KV("Inlet Valve", if (op.inletValveOn) "OPEN" else "CLOSED")
                    }
                }
            }
        }

        // Renders powder processing metrics dynamically across elements
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            powderMakers.chunked(2).forEach { subList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    subList.forEach { pm ->
                        val isPmFault = pm.status == EquipmentStatus.FAULT
                        val statusLabelColor = if (isPmFault) StatusRed else if (pm.status == EquipmentStatus.RUNNING) StatusGreen else TextGray

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CardBg, RoundedCornerShape(16.dp))
                                .run { if (isPmFault) border(1.5.dp, StatusRed, RoundedCornerShape(16.dp)) else this }
                                .padding(16.dp)
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(pm.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(pm.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusLabelColor)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Diagnose: ${pm.statusText}", fontSize = 11.sp, color = TextGray)
                                Spacer(Modifier.height(12.dp))
                                Divider(color = BorderGray)
                                Spacer(Modifier.height(12.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        KV("VFD Speed", "${"%.1f".format(pm.vfdSpeed)} %")
                                        KV("Load Current", "${"%.1f".format(pm.motorAmps)} A")
                                        KV("Remaining Time", "${"%.0f".format(pm.remainingCycleTimeS)}s")
                                    }
                                    Column(Modifier.weight(1f)) {
                                        KV("Vacuum Hdr", "${"%.1f".format(pm.vacuumHeader)} Bar")
                                        KV("Vacuum Local", "${"%.1f".format(pm.vacuumAtPm)} Bar")
                                        KV("Feedback Run", if (pm.runFb) "ACTIVE" else "OFF")
                                    }
                                }
                            }
                        }
                    }
                    if (subList.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMetricsGridSection(live: ConcentrationDedicatedLiveState) {
    val telemetryItems = listOf(
        MetricItemData("Steam Pressure", "%.2f Bar".format(live.steamPressure), "Baseline Nominal", StatusGreen),
        MetricItemData("Steam Mass Flow", "%,.0f kg/hr".format(live.steamFlow), "Live Distribution", DeepBlue),
        MetricItemData("Total Output", "%,.0f kg".format(live.totalJaggeryKg), "Accumulated Weight", AccentPurple),
        MetricItemData("Pans Available", "${live.openPanAvailableCount} Units", "In System Stack", StatusGreen),
        MetricItemData("Powder Buffers", "${live.powderMakerAvailableCount} Units", "Available Buffer", LocalAmber)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        telemetryItems.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(item.label, fontSize = 11.sp, color = TextGray, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Text(item.value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = item.accentColor, maxLines = 1)
                    Spacer(Modifier.height(14.dp))
                    Text(item.metaDesc, fontSize = 10.sp, color = TextGray, maxLines = 1)
                }
            }
        }
    }
}

data class MetricItemData(val label: String, val value: String, val metaDesc: String, val accentColor: Color)

// ─────────────────────────────────────────────────────────────────────────────
// Side Panel Right-Rail Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IdentitySummaryCard(state: ConcentrationDedicatedDashboardState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🏭 Section Identity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            SummaryFieldRow("Section Workspace", "Concentration Plan")
            SummaryFieldRow("Batch Run Identifier", state.batchId)
            SummaryFieldRow("Shift Registered Start", state.startTime)
            SummaryFieldRow("Assigned Operator", state.userName)
            SummaryFieldRow("Operational Title", state.userRole)
        }
    }
}

@Composable
fun SteamTelemetryCard(live: ConcentrationDedicatedLiveState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 14.sp)
                Text("Boiler Grid Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }
            SummaryFieldRow("Steam Line Pressure", "%.2f Bar".format(live.steamPressure))
            SummaryFieldRow("Discharge Stream Flow", "%,.1f TPH".format(live.steamFlow / 1000.0))
            SummaryFieldRow("Total Mass Output", "%,.1f MT".format(live.totalJaggeryKg / 1000.0))
        }
    }
}

@Composable
fun ActiveAlertsPanel(alerts: List<String>) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ Active Critical Alerts (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)
            if (alerts.isEmpty()) {
                Text("No equipment exceptions logged.", fontSize = 12.sp, color = TextGray)
            } else {
                alerts.forEach { error ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
                            Text(error, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryFieldRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun BannerLocal(text: String, color: Color) {
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

@Composable
private fun KV(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextGray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}