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

// ─── Theme Colors matching the Dashboard ────────────────────────────────────
private val LocalAmber = Color(0xFFF59E0B)
private val DeepBlue = Color(0xFF1E3A8A)
private val LightBg = Color(0xFFF8FAFC)
private val CardBg = Color(0xFFFFFFFF)

// Additional layout theme fallbacks

@Composable
fun MillDedicatedPageScreen(
    userName: String = "Saravanan",
    userRole: String = "Operator",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    // 1. Initialized viewmodel via factory provider using state-arguments
    val vm: MillDedicatedViewModel = viewModel(
        factory = MillDedicatedViewModel.provideFactory(userName, userRole)
    )
    // 2. Realtime SSE state flow collection safe for lifecycle updates
    val live by vm.state.collectAsStateWithLifecycle()

    MillDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen
    )
}

@Composable
fun MillDedicatedPageContent(
    live: MillLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(scroll)
        ) {
            // Dynamic telemetry data feeding header elements
            DashboardHeader(
                batchId = live.dashboard.batchId,
                operatorName = live.dashboard.userName,
                status = live.dashboard.sectionStatus,
                shiftStart = live.dashboard.startTime,
                onBack = onBack // Passed navigation callback
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left & Center Column: Process Flow + Grid Cards
                Column(
                    modifier = Modifier.weight(2.2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProcessFlowSection(live = live)
                    MetricsGridSection(live = live)
                    BottomSummaryRow(live = live)
                }

                // Right Side Rail: Insights & Action Center
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    SectionSummaryCard(live = live)
                    AiRecommendationCard()
                    RootCauseAnalysisCard()
                    ActiveAlertsCard(alerts = live.alerts)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI Component Blocks (Updated with Dynamic Streaming Handles)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardHeader(
    batchId: String,
    operatorName: String,
    status: EquipmentStatus,
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

            Text("Mill Section", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Spacer(modifier = Modifier.width(8.dp))

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val statusColor = if (isHealthy) StatusGreen else StatusRed
            val statusLabel = if (isHealthy) "Running" else "Fault / Intervention Required"

            Box(
                modifier = Modifier
                    .background(statusColor.copy(0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("Current Shift", "Started: $shiftStart")
            HeaderMetaItem("Operator", operatorName)
        }
    }
}

@Composable
fun HeaderMetaItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
fun ProcessFlowSection(live: MillLiveState) {
    // Convert active equipment list into structural map for rapid layout indexing
    val equipmentMap = live.dashboard.connectedEquipment.associateBy { it.name }
    val carrier = equipmentMap["Cane Carrier"]
    val cutter = equipmentMap["Cane Cutter"]
    val fiberizer = equipmentMap["Fiberizer"]

    val millMotors = live.dashboard.motors
    val isAnyTandemFault = millMotors.any { it.status == EquipmentStatus.FAULT }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProcessStepCard("1", "Cane Carrier", "${"%.0f".format(live.caneStock.levelPct)}%", "Load Current", carrier?.value ?: "0 A", Modifier.weight(1f))
        ProcessArrow()
        ProcessStepCard("2", "Cane Cutter", "95%", "Current Draw", cutter?.value ?: "0 A", Modifier.weight(1f))
        ProcessArrow()
        ProcessStepCard("3", "Fiberizer", "91%", "Current Draw", fiberizer?.value ?: "0 A", Modifier.weight(1f))
        ProcessArrow()

        // Structural Step 4 handles calculated sub-element array configurations
        ProcessStepCard(
            step = "4",
            title = "Mill Tandem",
            progress = "${"%.1f".format(live.dashboard.efficiency)}%",
            metricLabel = "Avg Mill Motor",
            metricValue = millMotors.firstOrNull()?.healthValue ?: "0 A",
            modifier = Modifier
                .weight(1.2f)
                .border(
                    width = if (isAnyTandemFault) 1.5.dp else 0.dp,
                    color = if (isAnyTandemFault) StatusRed.copy(0.7f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            isBottleneck = isAnyTandemFault,
            progressLabel = "Efficiency",
            extraMetrics = listOf("OEE" to "${"%.0f".format(live.dashboard.oee)}%")
        )
        ProcessArrow()
        ProcessStepCard(
            step = "5",
            title = "Raw Juice Tank",
            progress = "${"%.0f".format(live.rawJuice.tankLevelPct)}%",
            metricLabel = "Outlet Flow",
            metricValue = "${"%,.0f".format(live.rawJuice.flowLhr)} L/hr",
            modifier = Modifier.weight(1f),
            progressLabel = "Level"
        )
    }
}

@Composable
fun ProcessStepCard(
    step: String,
    title: String,
    progress: String,
    metricLabel: String,
    metricValue: String,
    modifier: Modifier = Modifier,
    progressLabel: String = "Progress",
    isBottleneck: Boolean = false,
    extraMetrics: List<Pair<String, String>> = emptyList()
) {
    Box(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(LightBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(step, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)

            if (isBottleneck) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(StatusRed.copy(0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ANOMALY", color = StatusRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) StatusRed else StatusGreen, CircleShape))
                Text(if(step == "5") "Receiving" else "Running", fontSize = 11.sp, color = TextGray)
            }

            Spacer(Modifier.height(12.dp))
            Text(progressLabel, fontSize = 11.sp, color = TextGray)
            Text(progress, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) StatusRed else DeepBlue)

            Spacer(Modifier.height(12.dp))
            Divider(color = BorderGray)
            Spacer(Modifier.height(12.dp))

            if (extraMetrics.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    extraMetrics.forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 11.sp, color = TextGray)
                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusRed)
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
fun ProcessArrow() {
    Text("➔", color = AccentPurple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun MetricsGridSection(live: MillLiveState) {
    val kpis = live.dashboard.kpis.associateBy { it.type }

    val throughput = kpis[KpiType.THROUGHPUT]
    val extraction = kpis[KpiType.EXTRACTION]
    val flow = kpis[KpiType.JUICE_FLOW]
    val bagasse = kpis[KpiType.BAGASSE]
    val power = kpis[KpiType.POWER]

    val metrics = listOf(
        RowMetricItem("Throughput", throughput?.value ?: "0 kg/hr", "Target: Delta %", StatusGreen),
        RowMetricItem("Extraction %", extraction?.value ?: "0%", "Efficiency target", StatusGreen),
        RowMetricItem("Raw Juice Flow", flow?.value ?: "0 L/hr", "Deviation: ${"%.0f".format(live.rawJuice.flowDeviationLhr)} L/h", if (abs(live.rawJuice.flowDeviationLhr) > 200f) StatusRed else StatusGreen),
        RowMetricItem("Bagasse Loss", bagasse?.value ?: "0%", "Calculated residual", StatusRed),
        RowMetricItem("Mill Sub-kw", "${"%.0f".format(live.power.millMotorsTotalKw)} kW", "Motor Subsystem draw", StatusGreen),
        RowMetricItem("Prep Sub-kw", "${"%.0f".format(live.power.prepEquipmentTotalKw)} kW", "Cutter/Carrier draw", StatusGreen),
        RowMetricItem("Total Power Draw", power?.value ?: "0 kW", "Combined parameters", LocalAmber),
        RowMetricItem("Telemetry Node", live.connectionStatus, "Stream link status", if (live.connectionStatus == "CONNECTED") StatusGreen else StatusRed)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        metrics.forEach { metric ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(metric.label, fontSize = 11.sp, color = TextGray, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    Text(metric.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
                    Spacer(Modifier.height(16.dp))
                    Text(metric.trend, fontSize = 10.sp, color = metric.trendColor, fontWeight = FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

data class RowMetricItem(val label: String, val value: String, val trend: String, val trendColor: Color)

@Composable
fun BottomSummaryRow(live: MillLiveState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(40.dp).background(StatusGreen.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("💚", fontSize = 18.sp)
                }
                Column {
                    Text("Overall Section OEE", fontSize = 12.sp, color = TextGray)
                    Text("${"%.1f".format(live.dashboard.oee)}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
            }
        }

        Box(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(40.dp).background(DeepBlue.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("⚡", fontSize = 18.sp)
                }
                Column {
                    Text("Total Section Power", fontSize = 12.sp, color = TextGray)
                    Text("${"%.0f".format(live.power.totalKw)} kW", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
        }

        Box(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(40.dp).background(LocalAmber.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("📦", fontSize = 18.sp)
                }
                Column {
                    Text("Juice Tank Volume", fontSize = 12.sp, color = TextGray)
                    Text("${"%.1f".format(live.rawJuice.volumeFlowM3hr)} m³/h", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LocalAmber)
                }
            }
        }

        Box(modifier = Modifier.weight(1f).background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(40.dp).background(AccentPurple.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("📈", fontSize = 18.sp)
                }
                Column {
                    Text("Mass Feed Metrics", fontSize = 12.sp, color = TextGray)
                    Text("${"%.1f".format(live.throughputKgS)} kg/s", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Right Rail Panels
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionSummaryCard(live: MillLiveState) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📋 Stream Performance Specs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            SummaryRow("Actual Throughput", "${"%,.0f".format(live.throughputKgHr)} kg/hr")
            SummaryRow("Baseline Target", "15,000 kg/hr")
            SummaryRow("Juice Density", "${"%.1f".format(live.rawJuice.densityKgM3)} kg/m³")
            SummaryRow("Juice Flow Temp", "${"%.1f".format(live.rawJuice.temperatureC)} °C")
            SummaryRow("H3 Outlet Target", "${"%.1f".format(live.rawJuice.heater3OutletC)} °C")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextGray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun AiRecommendationCard() {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✦", color = AccentPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("AI Recommendation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
            }
            Text("Stabilize Cane Carrier Load Speeds", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Expected Gain", fontSize = 11.sp, color = TextGray)
                    Text("+3.2% Throughput", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
                Box(modifier = Modifier.background(LightBg, RoundedCornerShape(4.dp)).padding(6.dp)) {
                    Text("Confidence 96%", fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
fun RootCauseAnalysisCard() {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔍 Root Cause Analysis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalAmber)
            Text(
                "Mill Tandem load deviation correlates with sudden low variance feeding gaps.",
                fontSize = 13.sp,
                color = TextDark,
                fontWeight = FontWeight.Medium
            )
            Box(modifier = Modifier.background(LightBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Confidence  94%", fontSize = 11.sp, color = LocalAmber, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveAlertsCard(alerts: List<String>) {
    Box(modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️ Active Anomalies (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)

            if (alerts.isEmpty()) {
                Text("System values clear. Operating normally.", fontSize = 13.sp, color = TextGray)
            } else {
                alerts.forEach { warning ->
                    AlertItem(title = warning)
                }
            }
        }
    }
}

@Composable
fun AlertItem(title: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
        }
    }
}