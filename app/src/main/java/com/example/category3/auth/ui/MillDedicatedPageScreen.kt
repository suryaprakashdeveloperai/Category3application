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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlin.math.cos
import kotlin.math.sin

// ─── Standardized Brand Colors ────────────────────────────────────────────
private val BrandBg = Color(0xFFF6F6F7)

private val BrandCyan = Color(0xFF47B3E2)


// ─── Grey Frost Glassmorphism Modifier ────────────────────────────────────
@Composable
fun Modifier.glassCard(): Modifier = this
    .clip(RoundedCornerShape(12.dp))
    .background(
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                BrandLightGray.copy(alpha = 0.25f) // The Grey Frost tint
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f), // Specular light reflection
                BrandLightGray.copy(alpha = 0.3f)
            )
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .padding(8.dp)

@Composable
fun MillDedicatedPageScreen(
    userName: String = "Saravanan",
    userRole: String = "Operator",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: MillDedicatedViewModel = viewModel(factory = MillDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()
    MillDedicatedPageContent(live = live, onBack = onBack)
}

@Composable
fun MillDedicatedPageContent(
    live: MillLiveState,
    onBack: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize().background(BrandBg)) {
        // Soft backdrop elements to give the glass something to distort/overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.12f), Color.Transparent)),
                radius = size.width / 2.5f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(BrandSoftOrange.copy(alpha = 0.1f), Color.Transparent)),
                radius = size.width / 2f,
                center = Offset(size.width, size.height)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            DashboardHeader(
                batchId = live.dashboard.batchId,
                operatorName = live.dashboard.userName,
                status = live.dashboard.sectionStatus,
                shiftStart = live.dashboard.startTime,
                onBack = onBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Main UI Frame
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(2.6f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProcessFlowSection(live = live, modifier = Modifier.weight(1.1f))
                    VisualMetricsGridSection(live = live, modifier = Modifier.weight(1f))
                    BottomSummaryRow(live = live, modifier = Modifier.wrapContentHeight())
                }

                // Right Column
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroJuiceTankLevelVisual(live = live, modifier = Modifier.weight(1.2f))
                    SectionSummaryCard(live = live, modifier = Modifier.wrapContentHeight())
                    PowerDistributionChart(live = live, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header & Process Flow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardHeader(batchId: String, operatorName: String, status: EquipmentStatus, shiftStart: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = BrandDeepNavy)
            }
            Text("Mill Section", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            Spacer(modifier = Modifier.width(8.dp))

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val statusColor = if (isHealthy) StatusGreen else StatusRed
            val statusLabel = if (isHealthy) "Running Smoothly" else "Intervention Req"

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
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
        Text(label, fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

@Composable
fun ProcessFlowSection(live: MillLiveState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProcessStepCard("1", "Cane Carrier", R.drawable.cane_carrier, "${live.caneStock.levelPct.toInt()}%", "Load", "0 A", Modifier.weight(1f))
        ProcessStepCard("2", "Cane Cutter", R.drawable.cane_cutter, "95%", "Draw", "0 A", Modifier.weight(1f))
        ProcessStepCard("3", "Fiberizer", R.drawable.mill_fiberizer, "91%", "Draw", "0 A", Modifier.weight(1f))
        ProcessStepCard("4", "Mill Tandem", R.drawable.mill_tandem, "${("%.1f".format(live.dashboard.efficiency.toString().toFloatOrNull() ?: 0f))}%", "Avg", "0 A", Modifier.weight(1.1f), progressLabel = "Efficiency", extraMetricLabel = "OEE", extraMetricValue = "${("%.0f".format(live.dashboard.oee.toString().toFloatOrNull() ?: 0f))}%")
        ProcessStepCard("5", "Juice Tank", R.drawable.rawjuice_tank, "${live.rawJuice.tankLevelPct.toInt()}%", "Flow", "${live.rawJuice.flowLhr.toInt()} L/h", Modifier.weight(1f), progressLabel = "Level")
    }
}

@Composable
fun ProcessStepCard(
    step: String, title: String, imageRes: Int, progress: String, metricLabel: String, metricValue: String,
    modifier: Modifier = Modifier, progressLabel: String = "Progress", isBottleneck: Boolean = false,
    extraMetricLabel: String? = null, extraMetricValue: String? = null
) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.padding(2.dp).size(22.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, BrandLightGray.copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                }
                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("ANOMALY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier.wrapContentHeight().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) StatusRed else StatusGreen, CircleShape))
                    Text(if (step == "5") "Receiving" else "Running", fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(progressLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) StatusRed else BrandDarkBlueGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(metricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(metricValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }

                if (extraMetricLabel != null && extraMetricValue != null) {
                    Divider(color = BrandLightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(extraMetricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(extraMetricValue, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
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
fun VisualMetricsGridSection(live: MillLiveState, modifier: Modifier = Modifier) {
    val eff = (live.dashboard.efficiency.toString().toFloatOrNull() ?: 0f).coerceIn(0f, 100f)
    val throughputHistory = listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * live.throughputKgHr }
    val flowHistory = listOf(0.98f, 0.99f, 1.01f, 1.0f, 0.97f, 1.0f, 1.0f).map { it * live.rawJuice.flowLhr }
    val bagasseLoss = (100f - eff).coerceAtLeast(0f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualCard(Modifier.weight(1f), "Throughput Trend", "${live.throughputKgHr.toInt()} kg/hr", Icons.Outlined.ShowChart, BrandMutedBlue) {
                ChartSparklineTrend(data = throughputHistory, color = BrandMutedBlue)
            }
            VisualCard(Modifier.weight(1f), "Extraction Goal", "${"%.1f".format(eff)}%", Icons.Outlined.DataExploration, BrandTeal) {
                ChartTargetGauge(value = eff / 100f, target = 0.95f, color = BrandTeal)
            }
            VisualCard(Modifier.weight(1f), "Steady Flow", "${live.rawJuice.flowLhr.toInt()} L/h", Icons.Outlined.WaterDrop, BrandCyan) {
                ChartBarTrend(data = flowHistory, color = BrandCyan)
            }
            VisualCard(Modifier.weight(1f), "Bagasse Loss", "${"%.1f".format(bagasseLoss)}%", Icons.Outlined.ViewInAr, BrandOrange) {
                ChartThresholdBar(value = bagasseLoss / 30f, color = BrandOrange)
            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val millAvg = (live.power.millMotorsTotalKw / 4).coerceAtLeast(10f)
            val millLoads = listOf(millAvg*0.9f, millAvg*1.1f, millAvg*0.95f, millAvg*1.05f)
            VisualCard(Modifier.weight(1f), "Mill Motors Load", "${live.power.millMotorsTotalKw.toInt()} kW", Icons.Outlined.Power, BrandDeepNavy) {
                ChartEquipmentBars(data = millLoads, color = BrandDeepNavy)
            }

            val prepAvg = (live.power.prepEquipmentTotalKw / 3).coerceAtLeast(10f)
            val prepLoads = listOf(prepAvg*1.2f, prepAvg*0.8f, prepAvg*1.0f)
            VisualCard(Modifier.weight(1f), "Prep Units Load", "${live.power.prepEquipmentTotalKw.toInt()} kW", Icons.Outlined.Power, BrandDarkBlueGray) {
                ChartEquipmentBars(data = prepLoads, color = BrandDarkBlueGray)
            }

            VisualCard(Modifier.weight(1f), "Total Power", "${live.power.totalKw.toInt()} kW", Icons.Outlined.Bolt, BrandSoftOrange) {
                ChartZoneDial(value = live.power.totalKw, max = 1500f)
            }

            val isConn = live.connectionStatus == "CONNECTED"
            VisualCard(Modifier.weight(1f), "Telemetry Status", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) BrandTeal else StatusRed) {
                ChartPulseIndicator(isConnected = isConn)
            }
        }
    }
}

@Composable
fun VisualCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, chartContent: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(title, fontSize = 12.sp, color = BrandSteelGray, maxLines = 1, fontWeight = FontWeight.Bold)
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                chartContent()
            }
        }
    }
}

// ─── Intuitive Custom Charts ───

@Composable
fun ChartSparklineTrend(data: List<Float>, color: Color) {
    if (data.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
        val maxVal = data.maxOrNull() ?: 1f
        val minVal = (data.minOrNull() ?: 0f) * 0.9f
        val range = maxVal - minVal
        val stepX = size.width / (data.size - 1)

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - ((value - minVal) / range * size.height)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastY = size.height - ((data.last() - minVal) / range * size.height)
        drawCircle(color, radius = 4.dp.toPx(), center = Offset(size.width, lastY))
    }
}

@Composable
fun ChartTargetGauge(value: Float, target: Float, color: Color) {
    val clamped = value.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 2.dp)) {
        val strokeW = 8.dp.toPx()
        val radius = minOf(size.width, size.height) / 2f - strokeW
        val center = Offset(size.width / 2f, size.height / 2f + 4.dp.toPx())

        drawArc(BrandLightGray.copy(alpha = 0.25f), 135f, 270f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        drawArc(color, 135f, 270f * clamped, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        val targetAngle = Math.toRadians((135f + 270f * target).toDouble())
        val tx = center.x + (cos(targetAngle) * radius).toFloat()
        val ty = center.y + (sin(targetAngle) * radius).toFloat()
        drawCircle(BrandDeepNavy, strokeW/2f, Offset(tx, ty))
    }
}

@Composable
fun ChartBarTrend(data: List<Float>, color: Color) {
    if (data.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxVal = (data.maxOrNull() ?: 1f) * 1.1f
        val barW = (size.width / data.size) * 0.6f
        val gap = (size.width / data.size) * 0.4f

        data.forEachIndexed { i, value ->
            val h = (value / maxVal) * size.height
            val x = i * (barW + gap) + gap/2
            val y = size.height - h
            val barColor = if (i == data.lastIndex) color else color.copy(alpha = 0.4f)
            drawRoundRect(barColor, Offset(x, y), Size(barW, h), CornerRadius(4.dp.toPx()))
        }
    }
}

@Composable
fun ChartThresholdBar(value: Float, color: Color) {
    val clamped = value.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
        val h = 14.dp.toPx()
        val y = size.height / 2f - h / 2f

        drawRoundRect(BrandLightGray.copy(alpha = 0.25f), Offset(0f, y), Size(size.width, h), CornerRadius(h/2))
        drawRoundRect(StatusRed.copy(alpha = 0.2f), Offset(size.width * 0.7f, y), Size(size.width * 0.3f, h), CornerRadius(h/2))
        drawLine(StatusRed, Offset(size.width * 0.7f, y - 4.dp.toPx()), Offset(size.width * 0.7f, y + h + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawRoundRect(color, Offset(0f, y), Size(size.width * clamped, h), CornerRadius(h/2))
    }
}

@Composable
fun ChartEquipmentBars(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 6.dp)) {
        val maxVal = (data.maxOrNull() ?: 1f) * 1.2f
        val count = data.size
        val barW = size.width / count * 0.5f
        val gap = size.width / count * 0.5f

        data.forEachIndexed { i, value ->
            val h = (value / maxVal) * size.height
            val x = i * (barW + gap) + gap/2
            val y = size.height - h

            drawRoundRect(BrandLightGray.copy(alpha = 0.25f), Offset(x, 0f), Size(barW, size.height), CornerRadius(4.dp.toPx()))
            drawRoundRect(color, Offset(x, y), Size(barW, h), CornerRadius(4.dp.toPx()))
            drawCircle(color, 3.dp.toPx(), Offset(x + barW/2, size.height + 6.dp.toPx()))
        }
    }
}

@Composable
fun ChartZoneDial(value: Float, max: Float) {
    val clamped = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)) {
        val sw = 10.dp.toPx()
        val radius = minOf(size.width/2f, size.height) - sw
        val center = Offset(size.width/2f, size.height)

        drawArc(BrandTeal.copy(alpha = 0.3f), 180f, 90f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )
        drawArc(BrandOrange.copy(alpha = 0.3f), 270f, 54f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )
        drawArc(StatusRed.copy(alpha = 0.3f), 324f, 36f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )

        val angleRad = Math.toRadians((180 + 180 * clamped).toDouble())
        val nx = center.x + (cos(angleRad) * (radius - 4.dp.toPx())).toFloat()
        val ny = center.y + (sin(angleRad) * (radius - 4.dp.toPx())).toFloat()

        drawLine(BrandDeepNavy, center, Offset(nx, ny), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(BrandDeepNavy, 4.dp.toPx(), center)
    }
}

@Composable
fun ChartPulseIndicator(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val color = if (isConnected) BrandTeal else StatusRed
            drawCircle(color, 8.dp.toPx())
            if (isConnected) {
                drawCircle(
                    color = color.copy(alpha = 1f - pulse),
                    radius = (8.dp.toPx()) + (20.dp.toPx() * pulse),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroJuiceTankLevelVisual(live: MillLiveState, modifier: Modifier = Modifier) {
    val levelPct = live.rawJuice.tankLevelPct.coerceIn(0.0F, 100.0F).toFloat()

    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WaterDrop, null, tint = BrandOrange, modifier = Modifier.size(24.dp))
                Text("Raw Juice Tank Status", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).fillMaxHeight()
                        .background(BrandLightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val color = if (levelPct > 85f) StatusRed else BrandOrange
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(levelPct / 100f)
                            .background(color, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    )
                    Text(
                        "${levelPct.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (levelPct > 40f) Color.White else BrandDeepNavy,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
                    Column {
                        Text("Current Volume", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.1f".format(live.rawJuice.volumeFlowM3hr.toString().toFloatOrNull() ?: 0f)} m³", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Temp", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.1f".format(live.rawJuice.temperatureC.toString().toFloatOrNull() ?: 0f)} °C", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Flow", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${live.rawJuice.flowLhr.toInt()} L/h", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionSummaryCard(live: MillLiveState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.ListAlt, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("Stream Performance Specs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            SummaryRow("Actual Throughput", "${live.throughputKgHr.toInt()} kg/hr")
            SummaryRow("Baseline Target", "15,000 kg/hr")
            SummaryRow("Juice Density", "${"%.1f".format(live.rawJuice.densityKgM3.toString().toFloatOrNull() ?: 0f)} kg/m³")
            SummaryRow("H3 Outlet Target", "${"%.1f".format(live.rawJuice.heater3OutletC.toString().toFloatOrNull() ?: 0f)} °C")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

@Composable
fun PowerDistributionChart(live: MillLiveState, modifier: Modifier = Modifier) {
    val millKw = live.power.millMotorsTotalKw.toString().toDoubleOrNull() ?: 0.0
    val prepKw = live.power.prepEquipmentTotalKw.toString().toDoubleOrNull() ?: 0.0
    val totalKw = (live.power.totalKw.toString().toDoubleOrNull() ?: 0.0).coerceAtLeast(1.0)
    val otherKw = (totalKw - millKw - prepKw).coerceAtLeast(0.0)

    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Bolt, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("Power Distribution", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Row(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp))) {
                if (millKw > 0) Box(Modifier.weight((millKw / totalKw).toFloat()).fillMaxHeight().background(BrandDeepNavy))
                if (prepKw > 0) Box(Modifier.weight((prepKw / totalKw).toFloat()).fillMaxHeight().background(BrandCyan))
                if (otherKw > 0) Box(Modifier.weight((otherKw / totalKw).toFloat()).fillMaxHeight().background(BrandLightGray))
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DistributionLegendItem(color = BrandDeepNavy, label = "Mill Motors", value = "${millKw.toInt()} kW")
                DistributionLegendItem(color = BrandCyan, label = "Prep Equipment", value = "${prepKw.toInt()} kW")
                DistributionLegendItem(color = BrandLightGray, label = "Auxiliary", value = "${otherKw.toInt()} kW")
            }
        }
    }
}

@Composable
fun DistributionLegendItem(color: Color, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Text(label, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomSummaryRow(live: MillLiveState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryBlock(Modifier.weight(1f), "Section OEE", "${"%.1f".format(live.dashboard.oee.toString().toFloatOrNull() ?: 0f)}%", Icons.Outlined.Speed, BrandTeal)
        SummaryBlock(Modifier.weight(1f), "Total Power", "${live.power.totalKw.toInt()} kW", Icons.Outlined.Bolt, BrandDeepNavy)
        SummaryBlock(Modifier.weight(1f), "Juice Flow", "${"%.1f".format(live.rawJuice.volumeFlowM3hr.toString().toFloatOrNull() ?: 0f)} m³/h", Icons.Outlined.WaterDrop, BrandCyan)
        SummaryBlock(Modifier.weight(1f), "Feed Metrics", "${"%.1f".format(live.throughputKgS.toString().toFloatOrNull() ?: 0f)} kg/s", Icons.Outlined.ShowChart, BrandMutedBlue)
    }
}

@Composable
fun SummaryBlock(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(modifier = modifier.glassCard()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            }
        }
    }
}