package com.example.category3.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.math.abs

// ─── Standardized Brand Colors ────────────────────────────────────────────

private val BrandBg = Color(0xFFF6F6F7)

private val BrandCyan = Color(0xFF47B3E2)



// ─── Grey Frost Glassmorphism Modifier ────────────────────────────────────

@Composable
fun DefecatorDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: DefecatorDedicatedViewModel = viewModel(factory = DefecatorDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()
    DefecatorDedicatedPageContent(live = live, onBack = onBack)
}

@Composable
fun DefecatorDedicatedPageContent(
    live: DefecatorLiveState,
    onBack: () -> Unit = {}
) {
    val state = live.dashboard
    val phFault = live.process.pH != 0f && (live.process.pH < 6.8f || live.process.pH > 7.8f)
    val djPumpFault = live.process.djActivePumpA in 0f..0.5f
    val heater3Dev = abs(live.process.heater3PvC - live.process.heater3SpC)
    val heater3Fault = live.process.heater3SpC > 0f && heater3Dev > 5f

    Box(modifier = Modifier.fillMaxSize().background(BrandBg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.12f), Color.Transparent)), radius = size.width / 2.5f, center = Offset(0f, 0f))
            drawCircle(Brush.radialGradient(listOf(BrandSoftOrange.copy(alpha = 0.1f), Color.Transparent)), radius = size.width / 2f, center = Offset(size.width, size.height))
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            DefecatorWorkspaceHeader(
                batchId = state.batchId,
                stability = "%.1f".format(state.processStability),
                status = state.sectionStatus,
                onBack = onBack,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Left Column
                Column(modifier = Modifier.weight(2.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DefecationProcessFlowSection(live, phFault, heater3Fault, modifier = Modifier.weight(1.2f))
                    VisualMetricsGridSection(live, modifier = Modifier.weight(1f))
                    BottomSummaryRow(live, modifier = Modifier.wrapContentHeight())
                }

                // Right Column - FIXED STRETCHING
                Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroCJTankLevelVisual(live, modifier = Modifier.weight(1f)) // Expand nicely
                    FlowAnalysisCard(state, modifier = Modifier.wrapContentHeight()) // Tight list
                    AlarmsCriticalPanel(live.alerts, phFault, djPumpFault, heater3Fault, modifier = Modifier.wrapContentHeight()) // Tight list
                }
            }
        }
    }
}

@Composable
fun DefecatorWorkspaceHeader(batchId: String, stability: String, status: EquipmentStatus, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = BrandDeepNavy)
            }
            Text("Defecator Section", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
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
            HeaderMetaItem("System Loop", "Clarification")
        }
    }
}



@Composable
fun DefecationProcessFlowSection(live: DefecatorLiveState, phFault: Boolean, heater3Fault: Boolean, modifier: Modifier = Modifier) {
    val p = live.process
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        ProcessStepCard("1", "DJ Intake Tank", R.drawable.defecator_tank, "${"%.1f".format(p.djTankLevel)}%", "Pump Load", "${"%.2f".format(p.djActivePumpA)} A", Modifier.weight(1f))
        ProcessStepCard("2", "Neutralizer", R.drawable.defecator_neutralizer, if (p.pH == 0f) "—" else "%.2f pH".format(p.pH), "Buffer Lvl", "%.1f mm".format(p.srtBufferLevel), Modifier.weight(1f), isBottleneck = phFault)
        ProcessStepCard("3", "Thermal Stage", R.drawable.defecator_thermal, "%.1f °C".format(p.heater3PvC), "Valve", "%.1f%%".format(p.heater3SteamValvePct), Modifier.weight(1f), isBottleneck = heater3Fault, extraMetricLabel = "H2 Out", extraMetricValue = "%.1f°C".format(p.heater2OutletC))
    }
}

@Composable
fun ProcessStepCard(step: String, title: String, imageRes: Int, progress: String, metricLabel: String, metricValue: String, modifier: Modifier = Modifier, isBottleneck: Boolean = false, extraMetricLabel: String? = null, extraMetricValue: String? = null) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(painter = painterResource(id = imageRes), contentDescription = title, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.padding(2.dp).size(22.dp).background(Color.White.copy(alpha = 0.85f), CircleShape).border(1.dp, BrandLightGray.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                }
                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("ANOMALY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Text block with exact alignment logic
            Column(modifier = Modifier.wrapContentHeight().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) BrandOrange else StatusGreen, CircleShape))
                    Text(if (isBottleneck) "Deviating" else "Running", fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Progress", fontSize = 10.sp, color = BrandSteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) BrandOrange else BrandDarkBlueGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(metricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(metricValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }

                Divider(color = BrandLightGray.copy(alpha = 0.3f), modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))

                // Alignment fix: Draw transparent text if null to maintain exact height
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(extraMetricLabel ?: "-", fontSize = 10.sp, color = if (extraMetricLabel != null) BrandSteelGray else Color.Transparent)
                    Text(extraMetricValue ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (extraMetricValue != null) StatusRed else Color.Transparent)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTUITIVE VISUAL DATA CHARTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualMetricsGridSection(live: DefecatorLiveState, modifier: Modifier = Modifier) {
    val flowHistory = listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * live.clearJuice.flow }
    val currentPhRatio = ((live.process.pH - 6.0f) / 2.0f).coerceIn(0f, 1f)
    val heater1Loads = listOf(live.process.heater1OutletC * 0.9f, live.process.heater1OutletC, live.process.heater1OutletC * 1.05f)
    val heater2Loads = listOf(live.process.heater2OutletC * 0.85f, live.process.heater2OutletC, live.process.heater2OutletC * 1.02f)
    val heater3Loads = listOf(live.process.heater3PvC * 0.95f, live.process.heater3PvC, live.process.heater3PvC * 1.01f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualCard(Modifier.weight(1f), "Clear Juice Flow", "${live.clearJuice.flow.toInt()} L/h", Icons.Outlined.ShowChart, BrandMutedBlue) {
                ChartSparklineTrend(data = flowHistory, color = BrandMutedBlue)
            }
            VisualCard(Modifier.weight(1f), "pH Level", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, BrandTeal) {
                ChartTargetGauge(value = currentPhRatio, target = 0.5f, color = BrandTeal)
            }
            VisualCard(Modifier.weight(1f), "DJ Tank Level", "${live.process.djTankLevel.toInt()}%", Icons.Outlined.WaterDrop, BrandCyan) {
                ChartThresholdBar(value = live.process.djTankLevel / 100f, color = BrandCyan)
            }
            VisualCard(Modifier.weight(1f), "Buffer Tank", "${live.process.srtBufferLevel.toInt()} mm", Icons.Outlined.ViewInAr, BrandOrange) {
                ChartThresholdBar(value = (live.process.srtBufferLevel / 100f).coerceIn(0f, 1f), color = BrandOrange)
            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualCard(Modifier.weight(1f), "Heater 1 Out", "${"%.1f".format(live.process.heater1OutletC)} °C", Icons.Outlined.Power, BrandDeepNavy) {
                ChartEquipmentBars(data = heater1Loads, color = BrandDeepNavy)
            }
            VisualCard(Modifier.weight(1f), "Heater 2 Out", "${"%.1f".format(live.process.heater2OutletC)} °C", Icons.Outlined.Power, BrandDarkBlueGray) {
                ChartEquipmentBars(data = heater2Loads, color = BrandDarkBlueGray)
            }
            VisualCard(Modifier.weight(1f), "Heater 3 Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, BrandSoftOrange) {
                ChartEquipmentBars(data = heater3Loads, color = BrandSoftOrange)
            }
            val isConn = live.connectionStatus == "CONNECTED"
            VisualCard(Modifier.weight(1f), "Telemetry Status", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) BrandTeal else StatusRed) {
                ChartPulseIndicator(isConnected = isConn)
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL - Tightly Packed Fixed-Wrap Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroCJTankLevelVisual(live: DefecatorLiveState, modifier: Modifier = Modifier) {
    val levelPct = live.clearJuice.tankLevel.coerceIn(0.0F, 100.0F).toFloat()

    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WaterDrop, null, tint = BrandCyan, modifier = Modifier.size(24.dp))
                Text("Clear Juice Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).height(120.dp) // Fixed height to prevent stretching bugs
                        .background(BrandLightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val color = if (levelPct > 85f) BrandOrange else BrandCyan
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(levelPct / 100f).background(color, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)))
                    Text("${levelPct.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (levelPct > 40f) Color.White else BrandDeepNavy, modifier = Modifier.padding(bottom = 6.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Clear Flow Rate", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%,.0f".format(live.clearJuice.flow)} L/h", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Density", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.2f".format(live.clearJuice.density)} kg/m³", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Filter", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text(if (live.clearJuice.filterOn) "ENGAGED" else "BYPASS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(live.clearJuice.filterOn) StatusGreen else BrandSteelGray)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowAnalysisCard(state: DefecatorDashboardState, modifier: Modifier = Modifier) {
    val a = state.chart.actual
    val t = state.chart.target
    val gap = a - t
    val gapPct = if (t > 0f) gap / t * 100f else 0f

    // wrapContentHeight inside so it doesn't stretch!
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("Discharge Flow Analytics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Actual Clear Flow", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(a), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target Profile", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(t), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Design Spec", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(state.chart.design), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Box(modifier = Modifier.fillMaxWidth().background(BrandLightGray.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Operational Delta", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Bold)
                    Text("%+.0f L/h (%+.1f%%)".format(gap, gapPct), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (gap >= 0) StatusGreen else BrandOrange)
                }
            }
        }
    }
}

@Composable
fun AlarmsCriticalPanel(alerts: List<String>, phFault: Boolean, djPumpFault: Boolean, heater3Fault: Boolean, modifier: Modifier = Modifier) {
    // wrapContentHeight inside so it stays compact
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = StatusRed, modifier = Modifier.size(20.dp))
                Text("Live Incident Stack", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusRed)
            }

            if (!phFault && !djPumpFault && !heater3Fault && alerts.isEmpty()) {
                Text("All systems nominal. No process limits breached.", fontSize = 13.sp, color = BrandSteelGray)
            } else {
                if (phFault) TriggeredAlertRow("Critical: pH Out of Range")
                if (djPumpFault) TriggeredAlertRow("Hardware: DJ Intake Pump Loss")
                if (heater3Fault) TriggeredAlertRow("Thermal: Heater 3 Delta Limit")
                alerts.forEach { error -> TriggeredAlertRow(text = error) }
            }
        }
    }
}

@Composable
fun TriggeredAlertRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandDeepNavy)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomSummaryRow(live: DefecatorLiveState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryBlock(Modifier.weight(1f), "Process Stability", "${"%.1f".format(live.dashboard.processStability)}", Icons.Outlined.Speed, BrandTeal)
        SummaryBlock(Modifier.weight(1f), "Average pH", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, BrandDeepNavy)
        SummaryBlock(Modifier.weight(1f), "Clear Juice Flow", "${"%,.0f".format(live.clearJuice.flow)} L/h", Icons.Outlined.WaterDrop, BrandCyan)
        SummaryBlock(Modifier.weight(1f), "Thermal Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, BrandSoftOrange)
    }
}

