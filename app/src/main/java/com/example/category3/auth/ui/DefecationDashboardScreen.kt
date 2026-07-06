package com.example.category3.process.ui

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.category3.components.RadialAppBar

// ============================================================================
// 📊 DOMAIN DATA MODEL (UPDATED WITH CHART LISTS)
// ============================================================================
data class DcsProcessState(
    val rawJuiceFlow: String = "119.0",
    val clearJuiceYield: String = "84.8",
    val defecatedJuicePh: String = "7.2",
    val dchTemperature: String = "105.3",

    val flowHistory: List<Float> = emptyList(),
    val yieldHistory: List<Float> = emptyList(),
    val phHistory: List<Float> = emptyList(),
    val tempHistory: List<Float> = emptyList(),
    val timeLabels: List<String> = emptyList(),

    val volumeLabels: List<String> = emptyList(),
    val volumeValues: List<Float> = emptyList(),

    val pump1Status: Boolean = true,
    val pump2Status: Boolean = false,
    val vibroscreenStatus: Boolean = true,
    val rotaryRunning: Boolean = false,

    val operatorRemarks: String = ""
)

// ============================================================================
// 🎨 COLOR PALETTE (UPDATED WITH StatusPurple)
// ============================================================================
val AppBg = Color(0xFFE4E9F0)
val AccentOrange = Color(0xFFFF6B4A)
val TextDark = Color(0xFF2C3A4B)
val TextGray = Color(0xFF67778A)
val StatusGreen = Color(0xFF26C281)
val StatusBlue = Color(0xFF47A1F2)
val StatusRed = Color(0xFFFF4D4D)
val StatusPurple = Color(0xFFA120FF) // <-- This was missing
val BorderGray = Color(0xFFC5D1DF)

// ============================================================================
// 🧊 GLASS CARD
// ============================================================================
@Composable
fun GreyFrostGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, shape, spotColor = Color(0xFF8A9AAB).copy(0.5f), ambientColor = Color(0xFF8A9AAB).copy(0.2f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color.White.copy(0.65f), Color(0xFFC9D4E2).copy(0.4f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.9f), Color(0xFFA5B4C7).copy(0.3f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)), shape),
        content = content
    )
}

// ============================================================================
// 📱 MAIN SCREEN: DEFECATION VISUALIZATION DASHBOARD
// ============================================================================
@Composable
fun DefecationDashboardScreen(
    state: DcsProcessState = MockDefecationData.getMockState(),
    onNavigateToScreen: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val mainScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 90.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(mainScrollState)
        ) {
            DefecationTopNav(onAction = {})

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth().height(800.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Left Column (Metrics & Line Chart)
                    Column(modifier = Modifier.weight(1.3f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        MetricMatrixSensors(state = state, modifier = Modifier.weight(0.9f))
                        ProcessTrendChartCard(state = state, modifier = Modifier.weight(1f))
                    }
                    // Right Column (Bar Chart & Interlocks)
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        VolumeBarChartCard(state = state, modifier = Modifier.weight(1.1f))
                        HardwareInterlockSection(state = state, modifier = Modifier.weight(0.9f))
                        OperatorRemarksCard(state = state, modifier = Modifier.weight(0.4f))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    MetricMatrixSensors(state = state, modifier = Modifier.fillMaxWidth().height(350.dp))
                    ProcessTrendChartCard(state = state, modifier = Modifier.fillMaxWidth().height(350.dp))
                    VolumeBarChartCard(state = state, modifier = Modifier.fillMaxWidth().height(300.dp))
                    HardwareInterlockSection(state = state, modifier = Modifier.fillMaxWidth())
                    OperatorRemarksCard(state = state, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        RadialAppBar(
            modifier = Modifier.align(Alignment.CenterStart).zIndex(50f),
            activeSection = "defecation_dashboard",
            onActionSelected = onNavigateToScreen
        )
    }
}

// ============================================================================
// 🌐 TOP NAV BAR
// ============================================================================
@Composable
fun DefecationTopNav(onAction: (String) -> Unit) {
    var profileMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GreyFrostGlassCard(shape = RoundedCornerShape(24.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(StatusGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Defecation Visualization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { profileMenuExpanded = true }
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Shift A", fontSize = 12.sp, color = TextGray)
                        Text("DCS Operator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark)
                }
                DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }, modifier = Modifier.background(Color.White)) {
                    DropdownMenuItem(text = { Text("Log Out", color = StatusRed) }, onClick = { profileMenuExpanded = false; onAction("Log Out") })
                }
            }
        }
    }
}

// ============================================================================
// 🎛️ 1. METRIC MATRIX WITH SPARKLINES
// ============================================================================
@Composable
fun MetricMatrixSensors(state: DcsProcessState, modifier: Modifier = Modifier) {
    GreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Live Telemetry", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(StatusRed, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SparklineMetricCard(
                        title = "Raw Juice Flow", value = state.rawJuiceFlow, unit = "m³/h",
                        icon = Icons.Outlined.WaterDrop, color = StatusBlue, data = state.flowHistory, modifier = Modifier.weight(1f)
                    )
                    SparklineMetricCard(
                        title = "Defecated Juice pH", value = state.defecatedJuicePh, unit = "pH",
                        icon = Icons.Outlined.Science, color = StatusPurple, data = state.phHistory, modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SparklineMetricCard(
                        title = "DCH Temp", value = state.dchTemperature, unit = "°C",
                        icon = Icons.Outlined.DeviceThermostat, color = AccentOrange, data = state.tempHistory, modifier = Modifier.weight(1f)
                    )
                    SparklineMetricCard(
                        title = "Clear Juice Yield", value = state.clearJuiceYield, unit = "%",
                        icon = Icons.Outlined.Analytics, color = StatusGreen, data = state.yieldHistory, modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SparklineMetricCard(title: String, value: String, unit: String, icon: ImageVector, color: Color, data: List<Float>, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.4f))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        if (data.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(top = 40.dp, bottom = 10.dp)) {
                val path = Path()
                val stepX = size.width / (data.size - 1)
                val max = data.maxOrNull() ?: 1f
                val min = data.minOrNull() ?: 0f
                val range = (max - min).coerceAtLeast(0.1f)

                data.forEachIndexed { index, dataPoint ->
                    val x = index * stepX
                    val y = size.height - ((dataPoint - min) / range * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color.copy(alpha = 0.3f), style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).background(color.copy(0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Spacer(Modifier.width(4.dp))
                Text(unit, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

// ============================================================================
// 📈 2. PROCESS TREND LINE CHART
// ============================================================================
@Composable
fun ProcessTrendChartCard(state: DcsProcessState, modifier: Modifier = Modifier) {
    GreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("pH & Temp Correlation Trend", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegend("pH Level", StatusPurple)
                    ChartLegend("Temp °C", AccentOrange)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val textMeasurer = rememberTextMeasurer()
            var animationPlayed by remember { mutableStateOf(false) }
            val progress by animateFloatAsState(
                targetValue = if (animationPlayed) 1f else 0f,
                animationSpec = tween(1500), label = "chart_progress"
            )

            LaunchedEffect(Unit) { animationPlayed = true }

            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                if (state.phHistory.isEmpty() || state.tempHistory.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height
                val stepX = w / (state.timeLabels.size - 1).coerceAtLeast(1)

                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = h - (i * (h / gridLines))
                    drawLine(color = BorderGray.copy(0.3f), start = Offset(0f, y), end = Offset(w, y), strokeWidth = 2f)
                }

                val labelStyle = TextStyle(fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                state.timeLabels.forEachIndexed { i, label ->
                    val x = i * stepX
                    drawText(textMeasurer, label, style = labelStyle, topLeft = Offset(x - 15f, h + 10f))
                }

                fun drawTrendLine(data: List<Float>, color: Color, minY: Float, maxY: Float) {
                    val path = Path()
                    val fillPath = Path()
                    val range = (maxY - minY).coerceAtLeast(0.1f)

                    val points = data.take((data.size * progress).toInt().coerceAtLeast(1))

                    points.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = h - ((value - minY) / range * h)
                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevY = h - ((data[index - 1] - minY) / range * h)
                            val cpX = (prevX + x) / 2
                            path.cubicTo(cpX, prevY, cpX, y, x, y)
                            fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                        }
                    }

                    if (points.isNotEmpty()) {
                        fillPath.lineTo((points.size - 1) * stepX, h)
                        fillPath.close()
                        drawPath(fillPath, brush = Brush.verticalGradient(listOf(color.copy(0.2f), Color.Transparent)))
                        drawPath(path, color, style = Stroke(width = 6f, cap = StrokeCap.Round))

                        val lastIndex = points.size - 1
                        val lastX = lastIndex * stepX
                        val lastY = h - ((points.last() - minY) / range * h)
                        drawCircle(Color.White, radius = 6f, center = Offset(lastX, lastY))
                        drawCircle(color, radius = 4f, center = Offset(lastX, lastY))
                    }
                }

                drawTrendLine(state.phHistory, StatusPurple, minY = 6.0f, maxY = 8.5f)
                drawTrendLine(state.tempHistory, AccentOrange, minY = 90f, maxY = 110f)
            }
        }
    }
}

@Composable
fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
    }
}

// ============================================================================
// 📊 3. VOLUME ANALYSIS BAR CHART
// ============================================================================
@Composable
fun VolumeBarChartCard(state: DcsProcessState, modifier: Modifier = Modifier) {
    GreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text("Shift Volume Analysis", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Text("Processed Totalizers (Tons)", fontSize = 12.sp, color = TextGray)
            Spacer(modifier = Modifier.height(24.dp))

            var animationPlayed by remember { mutableStateOf(false) }
            val progress by animateFloatAsState(targetValue = if (animationPlayed) 1f else 0f, animationSpec = tween(1200), label = "bar_anim")
            LaunchedEffect(Unit) { animationPlayed = true }

            val textMeasurer = rememberTextMeasurer()

            Canvas(modifier = Modifier.fillMaxSize().padding(top = 10.dp, bottom = 20.dp)) {
                if (state.volumeValues.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height
                val maxVal = state.volumeValues.maxOrNull() ?: 1f
                val barWidth = 45.dp.toPx()
                val spacing = (w - (barWidth * state.volumeValues.size)) / (state.volumeValues.size + 1)

                val colors = listOf(AccentOrange, StatusGreen, StatusBlue)
                val labelStyle = TextStyle(fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Bold)
                val valStyle = TextStyle(fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.SemiBold)

                state.volumeValues.forEachIndexed { i, value ->
                    val x = spacing + (i * (barWidth + spacing))
                    val barHeight = (value / maxVal) * h * progress
                    val y = h - barHeight

                    drawRoundRect(
                        color = colors[i % colors.size].copy(alpha = 0.8f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    val label = state.volumeLabels[i]
                    drawText(textMeasurer, label, style = labelStyle, topLeft = Offset(x + (barWidth/2) - (textMeasurer.measure(label, labelStyle).size.width/2), h + 10f))

                    if (progress > 0.8f) {
                        val vText = "${value.toInt()}"
                        drawText(textMeasurer, vText, style = valStyle, topLeft = Offset(x + (barWidth/2) - (textMeasurer.measure(vText, valStyle).size.width/2), y - 40f))
                    }
                }
            }
        }
    }
}

// ============================================================================
// ⚙️ 4. HARDWARE INTERLOCK STATUS
// ============================================================================
@Composable
fun HardwareInterlockSection(state: DcsProcessState, modifier: Modifier = Modifier) {
    GreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text("Interlocks", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InterlockRow("Pump 1 (RJ)", state.pump1Status)
                InterlockRow("Pump 2 (CJ)", state.pump2Status)
                InterlockRow("Vibroscreen", state.vibroscreenStatus)
            }
        }
    }
}

@Composable
fun InterlockRow(name: String, isRunning: Boolean) {
    val statusColor = if (isRunning) StatusGreen else StatusBlue
    val icon = if (isRunning) Icons.Rounded.Check else Icons.Rounded.Pause

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.5f)).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (isRunning) "RUN" else "STBY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(20.dp).background(statusColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
    }
}

// ============================================================================
// 💬 5. OPERATOR REMARKS
// ============================================================================
@Composable
fun OperatorRemarksCard(state: DcsProcessState, modifier: Modifier = Modifier) {
    GreyFrostGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Comment, null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shift Remarks", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.operatorRemarks.ifEmpty { "No remarks." }, fontSize = 12.sp, color = TextDark, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ============================================================================
// 📡 MOCK DATA
// ============================================================================
object MockDefecationData {
    fun getMockState() = DcsProcessState(
        rawJuiceFlow = "119.0", clearJuiceYield = "84.8", defecatedJuicePh = "7.2", dchTemperature = "105.3",
        flowHistory = listOf(115f, 118f, 119f, 112f, 116f, 119f),
        yieldHistory = listOf(82f, 83.5f, 84f, 84.8f, 84.5f, 84.8f),
        phHistory = listOf(6.8f, 6.9f, 7.0f, 7.1f, 7.2f, 7.2f, 7.1f),
        tempHistory = listOf(102f, 103f, 104.5f, 105f, 105.3f, 105.1f, 105.3f),
        timeLabels = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00"),
        volumeLabels = listOf("Raw Juice", "Clear Juice", "Flocculant"),
        volumeValues = listOf(750f, 650f, 120f),
        pump1Status = true, pump2Status = false, vibroscreenStatus = true, rotaryRunning = false,
        operatorRemarks = "Pump 2 put on standby for routine maintenance. Trend stabilized."
    )
}