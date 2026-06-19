package com.example.category3.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

// ============================================================================
// 🎨 ADAPTIVE THEME CONFIG (Light & Purple Liquid Glassmorphism)
// ============================================================================
data class DashboardTheme(
    val isDark: Boolean,
    val bgBase: Color,
    val textMain: Color,
    val textMuted: Color,
    val textLightMuted: Color,
    val panelBorder: Color,
    val panelBgStart: Color,
    val panelBgEnd: Color,
    val trackBg: Color
)

fun getAdaptiveTheme(isDark: Boolean): DashboardTheme {
    return if (isDark) {
        DashboardTheme(
            isDark = true,
            bgBase = Color(0xFF1E0A3C), // Deep Purple
            textMain = Color(0xFFFFFFFF),
            textMuted = Color(0xFFAFA5CA),
            textLightMuted = Color(0xFF8679A8),
            panelBorder = Color(0x4DFFFFFF), // Translucent White
            panelBgStart = Color(0x33FFFFFF), // Frosted Liquid Glass
            panelBgEnd = Color(0x0AFFFFFF),
            trackBg = Color(0x33000000)
        )
    } else {
        DashboardTheme(
            isDark = false,
            bgBase = Color(0xFFF6F8FA),
            textMain = Color(0xFF1E293B),
            textMuted = Color(0xFF64748B),
            textLightMuted = Color(0xFF94A3B8),
            panelBorder = Color(0xFFE2E8F0),
            panelBgStart = Color(0xFFFFFFFF), // Solid White
            panelBgEnd = Color(0xFFFFFFFF),
            trackBg = Color(0xFFE2E8F0)
        )
    }
}

val AccentBlue = Color(0xFF0EA5E9)
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF97316)
val AccentPurple = Color(0xFF8B5CF6)
val AccentRed = Color(0xFFEF4444)
val AccentLightBlue = Color(0xFF38BDF8)

// ============================================================================
// 📡 DATA SPEC & STATE
// ============================================================================
data class RecommendationData(val id: String, val text: String, val priority: String, val impact: String)

data class LiveStageData(
    val id: String, val name: String, val status: String,
    val juicePercentage: Double, val actualFlow: Float, val targetFlow: Float,
    val energyKw: Float, val throughputKg: Int, val downtimeMins: Int,
    val stageRecommendations: List<RecommendationData>, val tintColor: Color,
    val aiProjection: Float
)

data class AlertData(val id: String, val message: String, val priority: String, val timestamp: String = "")

// ============================================================================
// 📱 MAIN SCREEN COMPOSABLE
// ============================================================================
@Composable
fun WorkflowDashboardScreen(onNavigateToScreen: (String) -> Unit = {}) {
    val context = LocalContext.current
    var isLightMode by remember { mutableStateOf(true) }
    val theme = getAdaptiveTheme(!isLightMode) // Inverse because toggle says "Matrix Light"

    // --- MOCK MQTT REAL-TIME STATE ---
    var globalOee by remember { mutableStateOf(87) }
    var globalEnergy by remember { mutableStateOf(184f) }
    var globalThroughput by remember { mutableStateOf(495) }
    var lastShiftEnergy by remember { mutableStateOf(170f) }
    var yesterdayThroughput by remember { mutableStateOf(460) }

    var stages by remember {
        mutableStateOf(
            listOf(
                LiveStageData("01", "MILL", "RUN", 90.0, 12.7f, 15.0f, 42f, 110, 15, listOf(RecommendationData("1", "Increase Mill Core Speed", "High", "+2% Yield")), AccentBlue, 14.2f),
                LiveStageData("02", "DEFECATION", "RUN", 91.0, 12.8f, 14.0f, 28f, 95, 5, listOf(RecommendationData("2", "Add Lime Stabilizer", "Medium", "Balance Ph")), AccentGreen, 13.8f),
                LiveStageData("03", "EVAPORATION", "BLOCK", 70.0, 11.1f, 12.5f, 65f, 80, 45, listOf(RecommendationData("3", "Clear Tube Blockage", "Critical", "Restore Flow")), AccentOrange, 12.0f),
                LiveStageData("04", "CLARIFICATION", "RUN", 89.0, 12.2f, 11.0f, 32f, 105, 12, listOf(RecommendationData("4", "Adjust Flocculant Rate", "Low", "Optimize")), AccentPurple, 13.5f),
                LiveStageData("05", "CONCENTRATION", "RUN", 89.0, 13.2f, 10.5f, 45f, 105, 8, listOf(RecommendationData("5", "Calibrate Vacuum Pump", "Medium", "Prevent Drop")), AccentLightBlue, 14.0f)
            )
        )
    }

    val globalRecommendations = remember {
        listOf(
            RecommendationData("G1", "System-wide Power Grid Balancing", "Medium", "Save 12%"),
            RecommendationData("G2", "Inspect Boiler Pressure Valve", "High", "Prevent Outage")
        )
    }

    var liveDateString by remember { mutableStateOf("...") }
    var liveTimeString by remember { mutableStateOf("...") }
    var selectedStageId by remember { mutableStateOf<String?>(null) }

    val activeAlerts = remember { mutableStateListOf<AlertData>() }
    var toastAlert by remember { mutableStateOf<AlertData?>(null) }

    // INITIAL ALERTS
    LaunchedEffect(Unit) {
        activeAlerts.add(AlertData("pre-01", "Temp Spike Detected in Boiler", "CRITICAL", "10:23 AM"))
        activeAlerts.add(AlertData("pre-02", "Pressure Drop in Mill Stage", "CRITICAL", "10:45 AM"))
        activeAlerts.add(AlertData("pre-03", "Flow Variance at Evaporator", "WARNING", "11:02 AM"))
    }

    // CLOCK & MQTT DATA SIMULATION LOOP (1 Second ticks)
    LaunchedEffect(Unit) {
        val dateStyle = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeStyle = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

        while (true) {
            liveDateString = dateStyle.format(Date())
            liveTimeString = timeStyle.format(Date()).lowercase()

            stages = stages.map {
                it.copy(
                    actualFlow = (it.actualFlow + Random.nextFloat() * 0.6f - 0.3f).coerceIn(8f, 18f),
                    juicePercentage = (it.juicePercentage + Random.nextDouble(-1.5, 1.5)).coerceIn(60.0, 99.0),
                    energyKw = (it.energyKw + Random.nextFloat() * 2f - 1f).coerceIn(20f, 80f),
                    throughputKg = (it.throughputKg + Random.nextInt(-3, 4)).coerceIn(70, 150),
                    aiProjection = (it.aiProjection + Random.nextFloat() * 0.5f - 0.25f).coerceIn(10f, 16f)
                )
            }

            globalOee = (stages.map { it.juicePercentage }.average()).toInt()
            globalEnergy = stages.map { it.energyKw }.sum()
            globalThroughput = stages.map { it.throughputKg }.sum()

            delay(1000)
        }
    }

    // Random Alert Generator
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(30000, 60000))
            val alertMessages = listOf("Vacuum Pressure Fluctuation", "Temperature Anomaly Detected", "Flow Rate Below Threshold", "Motor Vibration Warning", "pH Level Imbalance")
            val newAlert = AlertData(UUID.randomUUID().toString(), alertMessages.random(), listOf("CRITICAL", "WARNING", "INFO").random(), SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
            activeAlerts.add(0, newAlert)
            toastAlert = newAlert
            delay(5000)
            toastAlert = null
        }
    }

    // Determine what data to show (Global vs Selected Stage)
    val activeStage = stages.find { it.id == selectedStageId }
    val displayOee = activeStage?.juicePercentage?.toInt() ?: globalOee
    val displayEnergy = activeStage?.energyKw ?: globalEnergy
    val displayThroughput = activeStage?.throughputKg ?: globalThroughput
    val displayDowntime = activeStage?.downtimeMins ?: stages.sumOf { it.downtimeMins }
    val displayRecs = activeStage?.stageRecommendations ?: globalRecommendations
    val displayProjection = activeStage?.aiProjection ?: stages.map { it.aiProjection }.average().toFloat()

    // Smooth Animations
    val animOee by animateFloatAsState(targetValue = displayOee.toFloat(), animationSpec = tween(800), label = "")
    val animEnergy by animateFloatAsState(targetValue = displayEnergy, animationSpec = tween(800), label = "")
    val animThroughput by animateFloatAsState(targetValue = displayThroughput.toFloat(), animationSpec = tween(800), label = "")
    val animProjection by animateFloatAsState(targetValue = displayProjection, animationSpec = tween(800), label = "")

    Box(modifier = Modifier.fillMaxSize().background(theme.bgBase)) {

        // 1️⃣ MAIN DASHBOARD CONTENT
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 90.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AURALISS DASHBOARD", color = theme.textMain, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text(if(isLightMode) "☀️ MATRIX LIGHT" else "🌙 MATRIX DARK", color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isLightMode,
                            onCheckedChange = { isLightMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = if(isLightMode) theme.panelBorder else AccentPurple,
                                uncheckedThumbColor = Color.White, uncheckedTrackColor = AccentPurple
                            ),
                            modifier = Modifier.scale(0.7f).height(20.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderPill(icon = { Icon(Icons.Outlined.CalendarToday, null, tint = theme.textMuted, modifier = Modifier.size(16.dp)) }, text = liveDateString, theme)
                    HeaderPill(icon = { Icon(Icons.Outlined.Schedule, null, tint = theme.textMuted, modifier = Modifier.size(18.dp)) }, text = liveTimeString, theme)
                }
            }

            // --- MAIN CONTENT GRID ---
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // ⬅️ LEFT COLUMN (Wider)
                Column(modifier = Modifier.weight(2.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // 1. TOP LEFT: Production Flow Trend (Graph Top, Cards Bottom)
                    CleanPanel(theme, modifier = Modifier.weight(2f).fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("PRODUCTION FLOW TREND (T/hr)", color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(if(activeStage != null) "Filtering metrics for: ${activeStage.name}. Click again to reset." else "Click any equipment stage to filter dashboard metrics", color = theme.textMuted, fontSize = 11.sp)
                            }

                            // 📈 MOUNTAIN GRAPH WITH AI PREDICTION (TOP)
                            Box(modifier = Modifier.fillMaxWidth().weight(1.5f).padding(bottom = 16.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width; val h = size.height
                                    val paddingLeft = 30.dp.toPx(); val graphW = w - paddingLeft; val graphH = h - 10.dp.toPx()
                                    fun calcY(f: Float) = graphH - ((f / 20f) * graphH)

                                    // Y Axis Guidelines
                                    listOf(0f, 5f, 10f, 15f, 20f).forEach { tick ->
                                        val gy = calcY(tick)
                                        drawLine(theme.panelBorder, Offset(paddingLeft, gy), Offset(w, gy), 1.dp.toPx())
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "${tick.toInt()}", 0f, gy + 4.dp.toPx(),
                                            android.graphics.Paint().apply { color = theme.textLightMuted.toArgb(); textSize = 11.sp.toPx() }
                                        )
                                    }

                                    val stepX = graphW / (stages.size - 1)
                                    val actPath = Path()
                                    val aiPath = Path()

                                    // Highlight vertical line for selected stage
                                    val selectedIndex = stages.indexOfFirst { it.id == selectedStageId }
                                    if (selectedIndex != -1) {
                                        val sx = paddingLeft + (selectedIndex * stepX)
                                        drawLine(stages[selectedIndex].tintColor.copy(alpha = if(theme.isDark) 0.3f else 0.15f), Offset(sx, 0f), Offset(sx, graphH), strokeWidth = 30.dp.toPx(), cap = StrokeCap.Round)
                                    }

                                    // Create Smooth Paths
                                    stages.forEachIndexed { i, stage ->
                                        val nx = paddingLeft + (i * stepX)
                                        val ay = calcY(stage.actualFlow)
                                        val py = calcY(stage.aiProjection)

                                        if (i == 0) {
                                            actPath.moveTo(nx, ay)
                                            aiPath.moveTo(nx, py)
                                        } else {
                                            val prevX = paddingLeft + ((i - 1) * stepX)
                                            val prevAy = calcY(stages[i - 1].actualFlow)
                                            val prevPy = calcY(stages[i - 1].aiProjection)
                                            val controlX1 = prevX + (nx - prevX) / 2f
                                            actPath.cubicTo(controlX1, prevAy, controlX1, ay, nx, ay)
                                            aiPath.cubicTo(controlX1, prevPy, controlX1, py, nx, py)
                                        }
                                    }

                                    // 🌄 MOUNTAIN FILL (Gradient Area)
                                    val fillPath = Path().apply { addPath(actPath); lineTo(paddingLeft + graphW, graphH); lineTo(paddingLeft, graphH); close() }
                                    val mountainGradient = Brush.horizontalGradient(stages.map { it.tintColor.copy(alpha = if(theme.isDark) 0.5f else 0.3f) }, startX = paddingLeft, endX = paddingLeft + graphW)
                                    drawPath(fillPath, brush = mountainGradient)

                                    // AI Prediction Line (Purple Dotted)
                                    drawPath(aiPath, AccentPurple, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)), cap = StrokeCap.Round))

                                    // Top Line
                                    drawPath(actPath, AccentBlue, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                                    // Data Points
                                    stages.forEachIndexed { i, stage ->
                                        val nx = paddingLeft + (i * stepX)
                                        val ay = calcY(stage.actualFlow)
                                        val py = calcY(stage.aiProjection)
                                        drawCircle(theme.panelBgStart, 5.dp.toPx(), Offset(nx, ay))
                                        drawCircle(stage.tintColor, 3.dp.toPx(), Offset(nx, ay))
                                        drawCircle(AccentPurple, 3.dp.toPx(), Offset(nx, py))
                                    }
                                }
                            }

                            // 📇 INTERACTIVE STAGE CARDS (BOTTOM)
                            Row(modifier = Modifier.fillMaxWidth().weight(0.9f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                stages.forEach { stage ->
                                    val isSelected = selectedStageId == stage.id
                                    val animCardOee by animateFloatAsState(targetValue = stage.juicePercentage.toFloat(), animationSpec = tween(500), label = "")
                                    val animCardFlow by animateFloatAsState(targetValue = stage.actualFlow, animationSpec = tween(500), label = "")

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) stage.tintColor.copy(alpha = 0.1f) else Color.Transparent)
                                            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) stage.tintColor else theme.panelBorder, shape = RoundedCornerShape(12.dp))
                                            .clickable { selectedStageId = if (selectedStageId == stage.id) null else stage.id }
                                            .padding(10.dp)
                                    ) {
                                        Text(stage.id, color = stage.tintColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart))

                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            CustomProcessVectorIcon(stage.name, stage.tintColor)
                                            Text(stage.name, color = theme.textMain, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    drawCircle(theme.trackBg, style = Stroke(3.dp.toPx()))
                                                    drawArc(stage.tintColor, -90f, (animCardOee / 100f) * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                                                }
                                                Text("${animCardOee.toInt()}%", color = theme.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(String.format("%.1f T/hr", animCardFlow), color = theme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. BOTTOM LEFT: 3 EXACT KPI PANELS IN ONE ROW
                    Row(modifier = Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        // CARD 1: AI PROJECTIONS (Mandatory)
                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(16.dp)) {
                                Text(if(activeStage != null) "${activeStage.name} PROJECTION" else "AI PROJECTIONS", color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${String.format("%.1f", animProjection)}", color = AccentPurple, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    Text("T/hr", color = theme.textMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ArrowUpward, null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                                    Text(" 8.2% vs Current", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Text("Next 4 Hours Forecast", color = theme.textLightMuted, fontSize = 10.sp)
                            }
                        }

                        // CARD 2: OEE DYNAMIC
                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(if(activeStage != null) "${activeStage.name} OEE" else "GLOBAL OEE", color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(theme.trackBg, style = Stroke(5.dp.toPx()))
                                            drawArc(activeStage?.tintColor ?: AccentBlue, -90f, (animOee / 100f) * 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                                        }
                                        Text("${animOee.toInt()}%", color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Avail ", color = theme.textMuted, fontSize = 11.sp)
                                        Text(if(activeStage != null) "98%" else "91%", color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Perf ", color = theme.textMuted, fontSize = 11.sp)
                                        Text(if(activeStage != null) "94%" else "89%", color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // CARD 3: DOWNTIME DYNAMIC
                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                val downColor = if(activeStage != null) AccentRed else AccentPurple
                                Box(Modifier.size(48.dp).background(downColor.copy(alpha=0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Schedule, null, tint = downColor, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(if(activeStage != null) "Unit Downtime" else "Total Downtime", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$displayDowntime", color = theme.textMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Text("Minutes / Shift", color = theme.textLightMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // ➡️ RIGHT COLUMN (Narrower)
                Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // 1. TOP RIGHT: Energy EXACTLY LIKE IMAGE & Throughput
                    Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        // ENERGY GAUGE EXACT MATCH TO IMAGE
                        CleanPanel(theme, modifier = Modifier.weight(1.1f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                // TOP ROW: Icon + Title (Left), Current Usage (Right)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(Modifier.size(24.dp).background(AccentOrange.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Outlined.ElectricBolt, null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                                        }
                                        Text("Energy\nConsumption", color = theme.textMuted, fontSize = 10.sp, lineHeight = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Current Usage", color = theme.textLightMuted, fontSize = 9.sp)
                                        Text("${animEnergy.toInt()} kW", color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // BOTTOM ROW: Gauge (Left), Percentage (Right)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    // 180 DEGREE GAUGE
                                    Box(Modifier.width(60.dp).aspectRatio(2f)) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            val strokeW = 8.dp.toPx()
                                            // Background track
                                            drawArc(theme.trackBg, 180f, 180f, false, style = Stroke(strokeW, cap = StrokeCap.Round))

                                            // Filled Gradient Arc
                                            val fillSweep = (animEnergy / 250f).coerceIn(0f, 1f) * 180f
                                            val gradient = Brush.horizontalGradient(listOf(AccentGreen, AccentOrange, AccentRed))
                                            drawArc(gradient, 180f, fillSweep, false, style = Stroke(strokeW, cap = StrokeCap.Round))

                                            // Needle Dot
                                            val angleRad = Math.toRadians((180f + fillSweep).toDouble())
                                            val r = size.width / 2f
                                            val needleX = r + r * kotlin.math.cos(angleRad).toFloat()
                                            val needleY = size.height + r * kotlin.math.sin(angleRad).toFloat()
                                            drawCircle(theme.textMain, 4.dp.toPx(), Offset(needleX, needleY))
                                        }
                                        // Number inside gauge
                                        Text("${animEnergy.toInt()}", color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp))
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.ArrowUpward, null, tint = AccentRed, modifier = Modifier.size(10.dp))
                                            Text(" 8.0%", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("vs Last Shift", color = theme.textLightMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        // THROUGHPUT
                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val tColor = activeStage?.tintColor ?: AccentBlue
                                    Box(Modifier.size(24.dp).background(tColor.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Schedule, null, tint = tColor, modifier = Modifier.size(14.dp))
                                    }
                                    Text(if(activeStage != null) "Unit Flow" else "Throughput", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${animThroughput.toInt()}", color = theme.textMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text("kg/h", color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 3.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ArrowUpward, null, tint = AccentGreen, modifier = Modifier.size(12.dp))
                                    Text(" ${String.format("%.1f", ((animThroughput - yesterdayThroughput) / yesterdayThroughput * 100))}% vs Yesterday", color = AccentGreen, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // 2. MIDDLE RIGHT: AI Recommendations
                    CleanPanel(theme, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Zyren AI Recommendations", color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(if(activeStage != null) activeStage.name else "GLOBAL", color = activeStage?.tintColor ?: theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(displayRecs) { rec ->
                                    Row(Modifier.fillMaxWidth().background(theme.panelBgStart, RoundedCornerShape(8.dp)).border(1.dp, theme.panelBorder, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val numColor = activeStage?.tintColor ?: AccentBlue
                                        Box(Modifier.size(24.dp).background(numColor.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                            Text(rec.id.takeLast(1), color = numColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(rec.text, color = theme.textMain, fontSize = 12.sp, modifier = Modifier.weight(1f))

                                        val pColor = if(rec.priority == "Critical" || rec.priority == "High") AccentRed else AccentOrange
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(rec.priority, color = pColor, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.background(pColor.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp))
                                            Spacer(Modifier.height(2.dp))
                                            Text(rec.impact, color = AccentGreen, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. BOTTOM RIGHT: Active Alerts
                    CleanPanel(theme, modifier = Modifier.fillMaxWidth().weight(1.3f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Active Alerts & Bottlenecks", color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("System log fetched via MQTT. Click to Acknowledge.", color = theme.textLightMuted, fontSize = 10.sp)
                            Spacer(Modifier.height(12.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(activeAlerts, key = { it.id }) { alert ->
                                    val isCrit = alert.priority == "CRITICAL"
                                    val color = if(isCrit) AccentRed else if(alert.priority == "WARNING") AccentOrange else AccentBlue

                                    Row(Modifier.fillMaxWidth().background(theme.panelBgStart, RoundedCornerShape(8.dp)).border(1.dp, color.copy(alpha=0.3f), RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if(alert.priority == "INFO") Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = color, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alert.message, color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Priority: ${alert.priority}", color = color, fontSize = 10.sp)
                                                if(alert.timestamp.isNotEmpty()) {
                                                    Text("• ${alert.timestamp}", color = theme.textLightMuted, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                        Box(Modifier.background(theme.trackBg, RoundedCornerShape(16.dp)).clickable { activeAlerts.remove(alert) }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                            Text("ACK", color = theme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2️⃣ RADIAL APP BAR (OVERLAY)
        RadialAppBar(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp),
            activeSection = activeStage?.name ?: "DASHBOARD",
            onActionSelected = { action ->
                when (action) {
                    "home" -> { selectedStageId = null }
                    "mill" -> onNavigateToScreen("mill_dashboard")
                    "dcs" -> onNavigateToScreen("dcs_screen")
                    "pan" -> onNavigateToScreen("vaccum_pan")
                    "repairs" -> {
                        val newAlert = AlertData(UUID.randomUUID().toString(), "Manual Repair Log Initiated", "INFO", SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
                        activeAlerts.add(0, newAlert)
                        toastAlert = newAlert
                    }
                    "download" -> toastAlert = AlertData("export", "Report Exported to Documents", "INFO", "")
                    "logout" -> onNavigateToScreen("login_screen")
                }
            }
        )

        // TOAST NOTIFICATION
        AnimatedVisibility(
            visible = toastAlert != null,
            enter = slideInVertically { -300 } + fadeIn(),
            exit = slideOutVertically { -300 } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp)
        ) {
            toastAlert?.let { alert ->
                Box(
                    modifier = Modifier
                        .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0x1A000000))
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(theme.panelBgStart, theme.panelBgEnd),
                                start = Offset.Zero,
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .border(1.dp, if(alert.priority == "CRITICAL") AccentRed else if(alert.priority == "WARNING") AccentOrange else AccentBlue, RoundedCornerShape(32.dp))
                        .padding(horizontal = 32.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(if(alert.priority == "INFO") Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = if(alert.priority == "CRITICAL") AccentRed else if(alert.priority == "WARNING") AccentOrange else AccentBlue, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Alert", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(alert.message, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
//  REUSABLE UI COMPONENTS (THEME AWARE)
// ============================================================================

@Composable
fun CleanPanel(theme: DashboardTheme, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val bgModifier = if (theme.isDark) {
        Modifier
            .shadow(16.dp, RoundedCornerShape(12.dp), spotColor = Color(0x66000000))
            .background(Brush.linearGradient(listOf(theme.panelBgStart, theme.panelBgEnd)))
            .border(1.dp, theme.panelBorder, RoundedCornerShape(12.dp))
    } else {
        Modifier
            .background(theme.panelBgStart)
            .border(1.dp, theme.panelBorder, RoundedCornerShape(12.dp))
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).then(bgModifier),
        content = content
    )
}

@Composable
fun HeaderPill(icon: @Composable () -> Unit, text: String, theme: DashboardTheme) {
    Row(
        modifier = Modifier
            .background(theme.panelBgStart, RoundedCornerShape(8.dp))
            .border(1.dp, theme.panelBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(text, color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomProcessVectorIcon(stageName: String, tintColor: Color) {
    Canvas(Modifier.size(24.dp)) {
        val cx = size.width / 2f; val cy = size.height / 2f
        val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (stageName.uppercase()) {
            "MILL" -> { drawCircle(tintColor, 5.dp.toPx(), Offset(cx - 3.dp.toPx(), cy + 3.dp.toPx()), style = stroke); drawCircle(tintColor, 4.dp.toPx(), Offset(cx + 3.dp.toPx(), cy - 3.dp.toPx()), style = stroke) }
            "DEFECATION" -> { drawRoundRect(tintColor, Offset(cx - 6.dp.toPx(), cy - 7.dp.toPx()), Size(12.dp.toPx(), 14.dp.toPx()), CornerRadius(3.dp.toPx()), style = stroke); drawLine(tintColor, Offset(cx - 3.dp.toPx(), cy + 7.dp.toPx()), Offset(cx - 3.dp.toPx(), cy + 10.dp.toPx()), strokeWidth = 2.dp.toPx()); drawLine(tintColor, Offset(cx + 3.dp.toPx(), cy + 7.dp.toPx()), Offset(cx + 3.dp.toPx(), cy + 10.dp.toPx()), strokeWidth = 2.dp.toPx()) }
            "EVAPORATION" -> { drawRoundRect(tintColor, Offset(cx - 5.dp.toPx(), cy - 8.dp.toPx()), Size(3.dp.toPx(), 16.dp.toPx()), CornerRadius(1.5.dp.toPx()), style = stroke); drawRoundRect(tintColor, Offset(cx + 2.dp.toPx(), cy - 8.dp.toPx()), Size(3.dp.toPx(), 16.dp.toPx()), CornerRadius(1.5.dp.toPx()), style = stroke) }
            "CLARIFICATION" -> { val fp = Path().apply { moveTo(cx - 8.dp.toPx(), cy - 5.dp.toPx()); lineTo(cx + 8.dp.toPx(), cy - 5.dp.toPx()); lineTo(cx + 4.dp.toPx(), cy + 6.dp.toPx()); lineTo(cx - 4.dp.toPx(), cy + 6.dp.toPx()); close() }; drawPath(fp, tintColor, style = stroke) }
            "CONCENTRATION" -> { drawCircle(tintColor, 6.dp.toPx(), Offset(cx, cy + 2.dp.toPx()), style = stroke); drawLine(tintColor, Offset(cx, cy - 4.dp.toPx()), Offset(cx, cy - 10.dp.toPx()), strokeWidth = 2.dp.toPx()); drawLine(tintColor, Offset(cx-3.dp.toPx(), cy - 8.dp.toPx()), Offset(cx+3.dp.toPx(), cy - 8.dp.toPx()), strokeWidth = 2.dp.toPx()) }
        }
    }
}