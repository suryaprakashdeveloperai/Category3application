package com.example.category3.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.R

// ─── Standardized Brand Colors ────────────────────────────────────────────
private val BrandBg = Color(0xFFF6F6F7)
private val BrandCyan = Color(0xFF47B3E2)

// ─── Popup Data Model ─────────────────────────────────────────────────────
data class PopupDetails(
    val title: String,
    val description: String,
    val dataPoints: Map<String, String> = emptyMap()
)

// ─── Main Screen ──────────────────────────────────────────────────────────

@Composable
fun VacuumPanDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: VacuumPanDedicatedViewModel = viewModel(factory = VacuumPanDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()
    VacuumPanDedicatedPageContent(live = live, onBack = onBack)
}

@Composable
fun VacuumPanDedicatedPageContent(
    live: VacuumPanLiveState,
    onBack: () -> Unit = {}
) {
    val state = live.dashboard
    val p = live.process

    // State for interactive popups
    var selectedPopup by remember { mutableStateOf<PopupDetails?>(null) }

    // Critical process deviation thresholds
    val vacuumFault = p.fceVacuum != 0f && (p.fceVacuum < 620f || p.fceVacuum > 700f)
    val pressureFault = p.fcePressure != 0f && (p.fcePressure < 0.05f || p.fcePressure > 0.30f)
    val brixFault = p.fceBrix != 0f && (p.fceBrix < 82f || p.fceBrix > 92f)
    val vacPumpFault = p.vacuumPumpA in 0f..0.5f

    // Dialog Renderer
    selectedPopup?.let { popup ->
        AlertDialog(
            onDismissRequest = { selectedPopup = null },
            title = { Text(popup.title, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(popup.description, color = BrandSteelGray, fontSize = 14.sp)
                    Divider(modifier = Modifier.padding(vertical = 4.dp), color = BrandLightGray)
                    popup.dataPoints.forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = 13.sp, color = BrandDarkBlueGray)
                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPopup = null }) {
                    Text("Acknowledge", color = BrandCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandBg)) {
        // Soft backdrop elements to give the glass something to distort
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.12f), Color.Transparent)), radius = size.width / 2.5f, center = Offset(0f, 0f))
            drawCircle(Brush.radialGradient(listOf(BrandSoftOrange.copy(alpha = 0.1f), Color.Transparent)), radius = size.width / 2f, center = Offset(size.width, size.height))
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            VacuumPanWorkspaceHeader(
                batchId = state.batchId,
                stability = "%.1f".format(state.processStability),
                status = state.sectionStatus,
                onBack = onBack,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                VpConnectionBanner(status = live.connectionStatus)
                Spacer(Modifier.height(8.dp))
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // ─── Left Column (Adjusted Weights to prevent bottom squashing) ───
                Column(modifier = Modifier.weight(2.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VacuumCoreProcessFlowSection(
                        live, vacuumFault, pressureFault, brixFault,
                        modifier = Modifier.weight(1.3f),
                        onShowDetails = { selectedPopup = it }
                    )
                    VisualMetricsGridSection(
                        live,
                        modifier = Modifier.weight(1f),
                        onShowDetails = { selectedPopup = it }
                    )
                    // Assigned specific weight instead of wrapContentHeight to stop squeezing
                    VpPeripheralEquipmentSection(
                        state,
                        modifier = Modifier.weight(0.7f),
                        onShowDetails = { selectedPopup = it }
                    )
                }

                // ─── Right Rail Column (Fixed weights & spacers to prevent gaps) ───
                Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VpIdentityContextCard(
                        state,
                        modifier = Modifier.wrapContentHeight(),
                        onShowDetails = { selectedPopup = it }
                    )
                    VpBrixTargetAnalyticsCard(
                        state,
                        modifier = Modifier.weight(1.2f),
                        onShowDetails = { selectedPopup = it }
                    )
                    VpActiveAlertsPanel(
                        live.alerts, vacuumFault, pressureFault, brixFault, vacPumpFault,
                        modifier = Modifier.weight(1f),
                        onShowDetails = { selectedPopup = it }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header & Process Flow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VacuumPanWorkspaceHeader(batchId: String, stability: String, status: EquipmentStatus, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = BrandDeepNavy)
            }
            Text("Vacuum Pan Section", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            Spacer(modifier = Modifier.width(8.dp))

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val statusColor = if (isHealthy) StatusGreen else StatusRed

            Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50)).border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(if (isHealthy) "Running Smoothly" else "Intervention Req", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    live: VacuumPanLiveState, pressureFault: Boolean, vacuumFault: Boolean, brixFault: Boolean,
    modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit
) {
    val p = live.process
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        ProcessStepCard(
            step = "1", title = "FCE Pressure", imageRes = R.drawable.vacuumpan,
            progress = if (p.fcePressure == 0f) "—" else "%.3f bar".format(p.fcePressure),
            metricLabel = "Inlet Temp", metricValue = if (p.fceInletTemp == 0f) "—" else "%.1f °C".format(p.fceInletTemp),
            isBottleneck = pressureFault, modifier = Modifier.weight(1f),
            onClick = {
                onShowDetails(PopupDetails("FCE Pressure Detail", "Primary chamber pressure readings.", mapOf("Current Pressure" to "${p.fcePressure} bar", "Inlet Temp" to "${p.fceInletTemp} °C")))
            }
        )
        ProcessStepCard(
            step = "2", title = "FCE Vacuum Loop", imageRes = R.drawable.vacuumpan,
            progress = if (p.fceVacuum == 0f) "—" else "%.1f mmHg".format(p.fceVacuum),
            metricLabel = "Vac Pump", metricValue = if (p.vacuumPumpV == 0f) "—" else "%.1f V".format(p.vacuumPumpV),
            isBottleneck = vacuumFault, modifier = Modifier.weight(1f),
            onClick = {
                onShowDetails(PopupDetails("FCE Vacuum Loop", "Status of the main vacuum generation system.", mapOf("Current Vacuum" to "${p.fceVacuum} mmHg", "Pump Voltage" to "${p.vacuumPumpV} V")))
            }
        )
        ProcessStepCard(
            step = "3", title = "Brix Density Stack", imageRes = R.drawable.vacuumpan,
            progress = if (p.fceBrix == 0f) "—" else "%.2f °Bx".format(p.fceBrix),
            metricLabel = "Outlet Temp", metricValue = if (p.fceOutletTemp == 0f) "—" else "%.1f °C".format(p.fceOutletTemp),
            isBottleneck = brixFault, modifier = Modifier.weight(1f),
            onClick = {
                onShowDetails(PopupDetails("Brix Density", "Final concentration output analysis.", mapOf("Discharge Brix" to "${p.fceBrix} °Bx", "Outlet Temp" to "${p.fceOutletTemp} °C")))
            }
        )
    }
}

@Composable
fun ProcessStepCard(
    step: String, title: String, imageRes: Int, progress: String, metricLabel: String, metricValue: String,
    modifier: Modifier = Modifier, isBottleneck: Boolean = false, onClick: () -> Unit
) {
    // Added clip and clickable before glassCard bounds to retain ripple effect cleanly
    Box(modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.glassCard()) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) { // Padding moved here
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(painter = painterResource(id = imageRes), contentDescription = title, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())

                Box(modifier = Modifier.padding(2.dp).size(22.dp).background(Color.White.copy(alpha = 0.85f), CircleShape).border(1.dp, BrandLightGray.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                }

                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("FAULT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.wrapContentHeight().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) BrandOrange else StatusGreen, CircleShape))
                    Text(if (isBottleneck) "Limit Breached" else "Stable Loop", fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Reading", fontSize = 10.sp, color = BrandSteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) BrandOrange else BrandDarkBlueGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(metricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(metricValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTUITIVE VISUAL DATA CHARTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualMetricsGridSection(live: VacuumPanLiveState, modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit) {
    val p = live.process
    // Deterministic visual histories
    val pressureHistory = listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * p.fcePressure }
    val vacuumHistory = listOf(0.98f, 0.99f, 1.01f, 1.0f, 0.97f, 1.0f, 1.0f).map { it * p.fceVacuum }

    // Gauges/Bars mapping
    val brixRatio = ((p.fceBrix - 60f) / 40f).coerceIn(0f, 1f)
    val tempInletRatio = (p.fceInletTemp / 150f).coerceIn(0f, 1f)
    val pumpLoads = listOf(p.vacuumPumpA * 0.9f, p.vacuumPumpA, p.vacuumPumpA * 1.05f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("Chamber Pressure", "Historical trend analysis.", mapOf("Current" to "${p.fcePressure} bar"))) }) {
                VisualCard(Modifier.fillMaxSize(), "Chamber Pressure", if (p.fcePressure == 0f) "—" else "%.3f bar".format(p.fcePressure), Icons.Outlined.ShowChart, BrandMutedBlue) {
                    ChartSparklineTrend(data = pressureHistory, color = BrandMutedBlue)
                }
            }
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("FCE Vacuum", "Vacuum loop performance trend.", mapOf("Current" to "${p.fceVacuum} mmHg"))) }) {
                VisualCard(Modifier.fillMaxSize(), "FCE Vacuum", if (p.fceVacuum == 0f) "—" else "%.1f mmHg".format(p.fceVacuum), Icons.Outlined.ViewInAr, BrandCyan) {
                    ChartBarTrend(data = vacuumHistory, color = BrandCyan)
                }
            }
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("Discharge Brix", "Real-time gauge against targets.", mapOf("Current" to "${p.fceBrix} °Bx"))) }) {
                VisualCard(Modifier.fillMaxSize(), "Discharge Brix", if (p.fceBrix == 0f) "—" else "%.1f °Bx".format(p.fceBrix), Icons.Outlined.DataExploration, BrandTeal) {
                    ChartTargetGauge(value = brixRatio, target = 0.7f, color = BrandTeal)
                }
            }
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("Boiler Feed Temp", "Inlet temperature threshold monitoring.", mapOf("Current" to "${p.fceInletTemp} °C"))) }) {
                VisualCard(Modifier.fillMaxSize(), "Boiler Feed Temp", if (p.fceInletTemp == 0f) "—" else "%.1f °C".format(p.fceInletTemp), Icons.Outlined.Bolt, BrandOrange) {
                    ChartThresholdBar(value = tempInletRatio, color = BrandOrange)
                }
            }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("Vacuum Pump Load", "Motor current distribution.", mapOf("Current Draw" to "${p.vacuumPumpA} A"))) }) {
                VisualCard(Modifier.fillMaxSize(), "Vacuum Pump Load", if (p.vacuumPumpA == 0f) "—" else "%.2f A".format(p.vacuumPumpA), Icons.Outlined.Power, BrandDeepNavy) {
                    ChartEquipmentBars(data = pumpLoads, color = BrandDeepNavy)
                }
            }

            val isConn = live.connectionStatus == "CONNECTED"
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onShowDetails(PopupDetails("Telemetry Stream", "Live equipment data flow status.", mapOf("Status" to live.connectionStatus))) }) {
                VisualCard(Modifier.fillMaxSize(), "Telemetry Stream", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) BrandTeal else StatusRed) {
                    ChartPulseIndicator(isConnected = isConn)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PERIPHERAL EQUIPMENT GRID
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VpPeripheralEquipmentSection(state: VacuumPanDashboardState, modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit) {
    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
        onShowDetails(PopupDetails("Equipment Grid", "Status of all peripheral equipment modules.", mapOf("Total Modules" to "${state.units.size}", "Active Faults" to "${state.units.count { it.status == EquipmentStatus.FAULT }}")))
    }.glassCard()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Memory, contentDescription = null, tint = BrandDeepNavy, modifier = Modifier.size(16.dp))
                Text("Component Equipment Grid (${state.units.size} Modules)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Spacer(Modifier.height(8.dp))

            // Wrapped in vertical scroll to prevent bottom squashing if modules increase
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                state.units.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { unit ->
                            val statusColor = if (unit.status == EquipmentStatus.FAULT) StatusRed else if (unit.status == EquipmentStatus.RUNNING) StatusGreen else BrandSteelGray
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(unit.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                                    Text("${unit.value}  •  ${unit.statusText}", fontSize = 10.sp, color = BrandSteelGray)
                                }
                                Text(unit.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL - Fixed WrapLayouts and Spacers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VpIdentityContextCard(state: VacuumPanDashboardState, modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit) {
    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
        onShowDetails(PopupDetails("Plant Context", "Current operational metadata.", mapOf("Batch" to state.batchId, "User" to state.userName)))
    }.glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Domain, null, tint = BrandDeepNavy, modifier = Modifier.size(18.dp))
                Text("Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))
            VpFieldRow("Sub-Section Loop", "Vacuum Pan Evaporator")
            VpFieldRow("Active Batch Code", state.batchId)
            VpFieldRow("Shift Start Timestamp", state.startTime)
            VpFieldRow("Current User", state.userName)
        }
    }
}

@Composable
fun VpBrixTargetAnalyticsCard(state: VacuumPanDashboardState, modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit) {
    val a = state.chart.actual
    val t = state.chart.target
    val gap = a - t
    val gapPct = if (t != 0f) gap / t * 100f else 0f

    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
        onShowDetails(PopupDetails("Brix Analytics", "Detailed breakdown of FCE density variations.", mapOf("Target" to "$t", "Actual" to "$a", "Deviation" to "$gap")))
    }.glassCard()) {
        // Changed to Top alignment and added internal spacers to fix the huge ugly gaps
        Column(modifier = Modifier.fillMaxHeight().padding(12.dp), verticalArrangement = Arrangement.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("FCE Brix Analytics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Spacer(Modifier.height(12.dp))
            Divider(color = BrandLightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                VpFieldRow("Actual Mass Brix", "%.2f °Bx".format(a))
                VpFieldRow("Target Profile Brix", "%.2f °Bx".format(t))
                VpFieldRow("Design Specification", "%.2f °Bx".format(state.chart.design))
            }

            // This spacer fills empty area naturally pushing the operational delta cleanly to the bottom
            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth().background(BrandLightGray.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Operational Delta", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Bold)
                    Text("%+.2f °Bx (%+.1f%%)".format(gap, gapPct), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (gap >= 0) StatusGreen else BrandOrange)
                }
            }
        }
    }
}

@Composable
fun VpActiveAlertsPanel(alerts: List<String>, vacuumFault: Boolean, pressureFault: Boolean, brixFault: Boolean, vacPumpFault: Boolean, modifier: Modifier = Modifier, onShowDetails: (PopupDetails) -> Unit) {
    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
        onShowDetails(PopupDetails("Live Incidents", "Review active system faults and limits.", mapOf("Active Alarms" to "${alerts.size}")))
    }.glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = StatusRed, modifier = Modifier.size(20.dp))
                Text("Live Incident Stack", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusRed)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))

            // Wrapped in verticalScroll to safely handle many alerts
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!vacuumFault && !pressureFault && !brixFault && !vacPumpFault && alerts.isEmpty()) {
                    Text("All systems nominal. No process limits breached.", fontSize = 12.sp, color = BrandSteelGray)
                } else {
                    if (vacuumFault) VpTriggeredAlertRow("Critical: FCE Vacuum Limit Exceeded")
                    if (pressureFault) VpTriggeredAlertRow("Critical: Chamber Pressure Boundary")
                    if (brixFault) VpTriggeredAlertRow("Warning: Crystallization Brix Deviation")
                    if (vacPumpFault) VpTriggeredAlertRow("Hardware: Vacuum Pump Current Drop")
                    alerts.forEach { error -> VpTriggeredAlertRow(text = error) }
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
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandDeepNavy)
    }
}

@Composable
fun VpFieldRow(label: String, value: String, valueColor: Color = BrandDeepNavy) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun VpConnectionBanner(status: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(BrandOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, BrandOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Sync, null, tint = BrandOrange, modifier = Modifier.size(16.dp))
            Text("Connection Stream Status: $status — Retrying stack...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
        }
    }
}