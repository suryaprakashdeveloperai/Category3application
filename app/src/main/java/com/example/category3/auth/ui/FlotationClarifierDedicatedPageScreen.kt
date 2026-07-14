package com.example.category3.auth.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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


@Composable
fun FlotationClarifierDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: FlotationClarifierDedicatedViewModel = viewModel(factory = FlotationClarifierDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()
    FlotationClarifierDedicatedPageContent(live = live, onBack = onBack)
}

@Composable
fun FlotationClarifierDedicatedPageContent(
    live: FlotationClarifierLiveState,
    onBack: () -> Unit = {}
) {
    val state = live.dashboard

    // ✅ OPTIMIZATION 1: Instant Layout Loading State
    // Returns a fast loader instantly during the screen slide-in transition.
    // The UI only renders the heavy data components once data arrives.
    if (state.units.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(BrandBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = BrandCyan)
                Text("Connecting to Telemetry Data...", color = BrandSteelGray, fontWeight = FontWeight.Medium)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandBg)) {

        // ✅ OPTIMIZATION 2: Cached Gradients to stop GC Churn
        val cyanBrush = remember { Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.12f), Color.Transparent)) }
        val orangeBrush = remember { Brush.radialGradient(listOf(BrandSoftOrange.copy(alpha = 0.1f), Color.Transparent)) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(cyanBrush, radius = size.width / 2.5f, center = Offset(0f, 0f))
            drawCircle(orangeBrush, radius = size.width / 2f, center = Offset(size.width, size.height))
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            FlotationWorkspaceHeader(
                batchId = state.batchId,
                statusText = state.sectionStatus.name,
                statusColor = when (state.sectionStatus) {
                    EquipmentStatus.FAULT -> StatusRed
                    EquipmentStatus.RUNNING, EquipmentStatus.HEALTHY -> StatusGreen
                    else -> BrandSteelGray
                },
                onBack = onBack,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (live.connectionStatus == "RECONNECTING" || live.connectionStatus == "DISCONNECTED") {
                BannerLine("Stream ${live.connectionStatus} — retrying...", BrandOrange)
                Spacer(Modifier.height(8.dp))
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Left & Center Column
                Column(modifier = Modifier.weight(2.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ClarifierProcessFlowSection(state.units, modifier = Modifier.weight(1.3f))
                    VisualMetricsGridSection(live, modifier = Modifier.weight(1f))
                }

                // Right Rail Column
                Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IdentityContextCard(state, modifier = Modifier.wrapContentHeight())
                    PerformanceKpiPanel(state.kpis, modifier = Modifier.weight(1f))
                    FcActiveAlertsPanel(live.alerts, modifier = Modifier.wrapContentHeight())
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header & Process Flow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FlotationWorkspaceHeader(batchId: String, statusText: String, statusColor: Color, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = BrandDeepNavy)
            }
            Text("Flotation Clarifier Section", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50)).border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("System Grid", "Flotation Loop")
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("Shift Tracking", "Live Stack")
        }
    }
}


@Composable
fun ClarifierProcessFlowSection(units: List<ClarifierUnitLive>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        units.forEachIndexed { index, unit ->
            val isCritical = unit.status == EquipmentStatus.FAULT
            ProcessStepCard(
                step = "${index + 1}",
                title = unit.name,
                imageRes = R.drawable.floatation_clarifier,
                progress = "${"%.1f".format(unit.vfdSpeedPct)}%",
                metricLabel = "Drive VFD",
                isBottleneck = isCritical,
                inletOpen = unit.inletOpen,
                outletOpen = unit.outletOpen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProcessStepCard(
    step: String, title: String, imageRes: Int, progress: String, metricLabel: String,
    isBottleneck: Boolean = false, inletOpen: Boolean, outletOpen: Boolean, modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) StatusRed else StatusGreen, CircleShape))
                    Text(if (isBottleneck) "Stopped/Fault" else "Running", fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(metricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) StatusRed else BrandDarkBlueGray)
                    }
                }

                Divider(color = BrandLightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Inlet Valve", fontSize = 10.sp, color = BrandSteelGray)
                    Text(if(inletOpen) "OPEN" else "CLOSED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(inletOpen) BrandTeal else BrandSteelGray)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Outlet Valve", fontSize = 10.sp, color = BrandSteelGray)
                    Text(if(outletOpen) "OPEN" else "CLOSED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(outletOpen) BrandMutedBlue else BrandSteelGray)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTUITIVE VISUAL DATA CHARTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualMetricsGridSection(live: FlotationClarifierLiveState, modifier: Modifier = Modifier) {
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw

    // ✅ OPTIMIZATION 3: Cached Data Mapping (Stops main thread from doing heavy loops on every UI frame)
    val flowHistory = remember(cjFlowLhr) { listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * cjFlowLhr } }
    val fcMondHistory = remember(live.fcMondFlow) { listOf(0.98f, 0.99f, 1.01f, 1.0f, 0.97f, 1.0f, 1.0f).map { it * live.fcMondFlow } }

    val densityRatio = ((live.clearJuiceDensity - 1.0f) / 0.1f).coerceIn(0f, 1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VisualCard(Modifier.weight(1f), "Clear Juice Flow", "${"%,.0f".format(cjFlowLhr)} L/h", Icons.Outlined.ShowChart, BrandMutedBlue) {
                ChartSparklineTrend(data = flowHistory, color = BrandMutedBlue)
            }
            VisualCard(Modifier.weight(1f), "CJ Tank Level", "${"%.1f".format(live.clearJuiceTankLevel)}%", Icons.Outlined.WaterDrop, BrandCyan) {
                ChartThresholdBar(value = live.clearJuiceTankLevel / 100f, color = BrandCyan)
            }
            VisualCard(Modifier.weight(1f), "Juice Density", "%.2f".format(live.clearJuiceDensity), Icons.Outlined.DataExploration, BrandTeal) {
                ChartTargetGauge(value = densityRatio, target = 0.5f, color = BrandTeal)
            }
            VisualCard(Modifier.weight(1f), "FC MOND Flow", "${"%,.0f".format(live.fcMondFlow)} L/h", Icons.Outlined.Speed, BrandOrange) {
                ChartBarTrend(data = fcMondHistory, color = BrandOrange)
            }
        }

        Row(modifier = Modifier.height(80.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val pumpColor = if (live.vacuumPumpStatus == EquipmentStatus.RUNNING) StatusGreen else StatusRed
            MiniStatusCard(Modifier.weight(1f), "Vacuum Pump Status", live.vacuumPumpStatus.name, Icons.Outlined.Power, pumpColor)

            val isConn = live.connectionStatus == "CONNECTED"
            MiniStatusCard(Modifier.weight(1f), "Telemetry Stream", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) BrandTeal else StatusRed)
        }
    }
}

@Composable
fun MiniStatusCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(color.copy(alpha = 0.15f), CircleShape).border(1.dp, color.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Bold)
                    Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                }
            }
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                ChartPulseIndicator(isConnected = value == "RUNNING" || value == "Online", overrideColor = color)
            }
        }
    }
}

@Composable
fun ChartPulseIndicator(isConnected: Boolean, overrideColor: Color? = null) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "pulse")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val color = overrideColor ?: (if (isConnected) BrandTeal else StatusRed)
            drawCircle(color, 6.dp.toPx())
            if (isConnected) drawCircle(color.copy(alpha = 1f - pulse), radius = (6.dp.toPx()) + (10.dp.toPx() * pulse), style = Stroke(width = 2.dp.toPx()))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL - Fixed WrapLayouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IdentityContextCard(state: FlotationClarifierDashboardState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Domain, null, tint = BrandDeepNavy, modifier = Modifier.size(18.dp))
                Text("Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))
            SummaryFieldRowFc("Section", "FC Loop")
            SummaryFieldRowFc("Batch Ref", state.batchId)
            SummaryFieldRowFc("Shift Start", state.startTime)
            SummaryFieldRowFc("Operator", state.userName)
        }
    }
}

@Composable
fun PerformanceKpiPanel(kpis: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, null, tint = BrandDeepNavy, modifier = Modifier.size(18.dp))
                Text("Live Performance KPIs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))

            kpis.forEach { (metricName, valueString) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandLightGray.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(metricName, fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                    Text(valueString, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                }
            }
        }
    }
}

@Composable
fun FcActiveAlertsPanel(alerts: List<String>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                Text("Critical Alerts (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRed)
            }
            Divider(color = BrandLightGray.copy(alpha = 0.3f))

            if (alerts.isEmpty()) {
                Text("Process thresholds nominal. No limit breaches.", fontSize = 12.sp, color = BrandSteelGray)
            } else {
                alerts.forEach { error ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(StatusRed, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(error, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BrandDeepNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryFieldRowFc(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

@Composable
private fun BannerLine(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Sync, null, tint = color, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOTE: Make sure your `glassCard`, `VisualCard`, `ChartSparklineTrend`,
// `HeaderMetaItem`, `ChartThresholdBar`, `ChartTargetGauge`, and `ChartBarTrend`
// functions remain imported or defined alongside this file as they were previously!
// ─────────────────────────────────────────────────────────────────────────────