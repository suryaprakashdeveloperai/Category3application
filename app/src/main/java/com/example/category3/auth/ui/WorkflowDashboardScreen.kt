package com.example.category3.auth.ui

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.hypot
import kotlin.random.Random

// ============================================================================
// 🎨 STRICT CUSTOM BRAND PALETTE
// ============================================================================
val BrandDeepNavy = Color(0xFF0A0D2F)
val BrandDarkBlueGray = Color(0xFF223B57)
val BrandSteelGray = Color(0xFF8C929C)
val BrandLightGray = Color(0xFFBCBCBF)
val BrandOffWhite = Color(0xFFF6F6F7)
val BrandCyanBlue = Color(0xFF47B3E2)
val BrandMutedBlue = Color(0xFF496D89)
val BrandTeal = Color(0xFF11CFC9)
val BrandOrange = Color(0xFFF68420)
val BrandSoftOrange = Color(0xFFD68A51)

data class DashboardTheme(
    val isDark: Boolean, val textMain: Color, val textMuted: Color, val textLightMuted: Color, val trackBg: Color
)

fun getAdaptiveTheme(isDark: Boolean): DashboardTheme {
    return if (isDark) {
        DashboardTheme(isDark = true, textMain = BrandOffWhite, textMuted = BrandLightGray, textLightMuted = BrandSteelGray, trackBg = BrandDeepNavy.copy(alpha = 0.5f))
    } else {
        DashboardTheme(isDark = false, textMain = BrandDeepNavy, textMuted = BrandMutedBlue, textLightMuted = BrandSteelGray, trackBg = BrandLightGray.copy(alpha = 0.3f))
    }
}

// Map logical colors strictly to the new palette
val AccentPrimary = BrandCyanBlue
val AccentSuccess = BrandTeal
val AccentWarning = BrandSoftOrange
val AccentCritical = BrandOrange
val AccentAI = BrandMutedBlue

// ============================================================================
// 📡 DATA SPEC & MODELS
// ============================================================================
data class RecommendationData(val id: String, val problem: String, val suggestion: String, val priority: String, val stageName: String)

data class LiveStageData(
    val id: String, val name: String, val status: String,
    val juicePercentage: Double, val actualFlow: Float, val targetFlow: Float,
    val energyKw: Float, val throughputKg: Int, val downtimeMins: Int,
    val stageRecommendations: List<RecommendationData>, val tintColor: Color,
    val aiProjection: Float, val tempC: Float, val vibrationHz: Float, val pressureBar: Float
)

data class AlertData(val id: String, val stage: String, val message: String, val priority: String, val metricFailed: String = "", val details: String = "")

// ============================================================================
// 📱 MAIN SCREEN COMPOSABLE
// ============================================================================
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun WorkflowDashboardScreen(onNavigateToScreen: (String) -> Unit = {}) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    var isLightMode by remember { mutableStateOf(true) }
    val theme = getAdaptiveTheme(!isLightMode)

    // STATE VARIABLES
    var globalOee by remember { mutableStateOf(87) }
    var globalEnergy by remember { mutableStateOf(217f) }
    var globalThroughput by remember { mutableStateOf(495) }

    val energyHistory = remember { mutableStateListOf(160f, 165f, 172f, 168f, 175f, 180f, 178f, 182f, 184f) }
    val oeeHistory = remember { mutableStateListOf(82f, 83f, 81f, 85f, 86f, 84f, 87f, 88f, 87f) }

    var stages by remember {
        mutableStateOf(
            listOf(
                LiveStageData("01", "MILL", "RUN", 25.0, 25f, 30f, 47f, 110, 15, listOf(RecommendationData("1", "Vibration exceeding 120Hz.", "Increase Mill Core Speed.", "High", "MILL")), AccentPrimary, 25f, 64f, 124f, 2.5f),
                LiveStageData("02", "DEFECATION", "RUN", 45.0, 47f, 45f, 26f, 95, 5, listOf(RecommendationData("2", "pH dropping rapidly.", "Add Lime Stabilizer automatically.", "Medium", "DEFECATION")), AccentSuccess, 39f, 82f, 43f, 1.2f),
                LiveStageData("03", "EVAPORATION", "BLOCK", 85.0, 86f, 80f, 64f, 80, 45, listOf(RecommendationData("3", "Thermal buildup detected.", "Clear Tube Blockage immediately.", "Critical", "EVAPORATION")), AccentCritical, 70f, 100f, 51f, 3.8f),
                LiveStageData("04", "CLARIFICATION", "RUN", 50.0, 50f, 60f, 34f, 105, 12, listOf(RecommendationData("4", "Turbidity increasing.", "Adjust Flocculant Rate by 5%.", "Low", "CLARIFICATION")), AccentAI, 34f, 75f, 64f, 1.8f),
                LiveStageData("05", "CONCENTRATION", "RUN", 60.0, 61f, 75f, 45f, 105, 8, listOf(RecommendationData("5", "Vacuum pressure drop.", "Calibrate Vacuum Pump.", "Medium", "CONCENTRATION")), AccentWarning, 71f, 59f, 80f, 0.4f)
            )
        )
    }

    var selectedStageId by remember { mutableStateOf<String?>(null) }
    val activeAlerts = remember { mutableStateListOf<AlertData>() }
    val activeAiLogs = remember { mutableStateListOf<RecommendationData>() }
    var toastAlert by remember { mutableStateOf<AlertData?>(null) }
    var popupAlertToView by remember { mutableStateOf<AlertData?>(null) }
    var zyrenPopupData by remember { mutableStateOf<RecommendationData?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            stages = stages.map {
                it.copy(
                    actualFlow = (it.actualFlow + Random.nextFloat() * 4f - 2f).coerceIn(10f, 100f),
                    aiProjection = (it.aiProjection + Random.nextFloat() * 2f - 1f).coerceIn(10f, 100f),
                    tempC = (it.tempC + Random.nextFloat() * 2f - 1f).coerceIn(40f, 120f),
                    vibrationHz = (it.vibrationHz + Random.nextFloat() * 5f - 2.5f).coerceIn(30f, 150f),
                    energyKw = (it.energyKw + Random.nextFloat() * 3f - 1.5f).coerceIn(20f, 80f),
                    juicePercentage = (it.juicePercentage + Random.nextDouble(-2.0, 2.0)).coerceIn(10.0, 99.0)
                )
            }

            globalOee = stages.map { it.juicePercentage }.average().toInt()
            globalEnergy = stages.map { it.energyKw }.sum()
            globalThroughput = stages.map { it.throughputKg }.sum()

            energyHistory.add(globalEnergy)
            if (energyHistory.size > 15) energyHistory.removeAt(0)
            oeeHistory.add(globalOee.toFloat())
            if (oeeHistory.size > 15) oeeHistory.removeAt(0)
        }
    }

    LaunchedEffect(Unit) {
        val randomAlerts = listOf(
            AlertData("A1", "MILL", "Pressure Drop in Core", "WARNING", "PRES: 1.2 Bar", "Hydraulic pressure fell below operational baseline."),
            AlertData("A2", "MILL", "Pressure Drop in Core", "WARNING", "PRES: 1.2 Bar", "Hydraulic pressure fell below operational baseline.")
        )
        while (true) {
            delay(5000)
            val nextToast = randomAlerts.random().copy(id = UUID.randomUUID().toString())
            toastAlert = nextToast
            activeAlerts.add(0, nextToast)
            if(activeAlerts.size > 15) activeAlerts.removeLast()
            delay(3000)
            if (toastAlert?.id == nextToast.id) toastAlert = null
        }
    }

    val actualLinePath = remember { Path() }
    val predictLinePath = remember { Path() }
    val yAxisPaint = remember(theme) { Paint().apply { color = theme.textMuted.toArgb(); textSize = 10f * density; isFakeBoldText = true } }
    val stageNamePaint = remember(theme) { Paint().apply { color = theme.textMain.toArgb(); textSize = 11f * density; textAlign = Paint.Align.CENTER; isFakeBoldText = true } }
    val predictTextPaint = remember { Paint().apply { color = AccentSuccess.toArgb(); textSize = 12f * density; textAlign = Paint.Align.CENTER; isFakeBoldText = true } }
    val actualTextPaint = remember { Paint().apply { color = AccentPrimary.toArgb(); textSize = 12f * density; textAlign = Paint.Align.CENTER; isFakeBoldText = true } }

    val activeStage = stages.find { it.id == selectedStageId }
    val displayOee = activeStage?.juicePercentage?.toFloat() ?: globalOee.toFloat()
    val displayEnergy = activeStage?.energyKw ?: globalEnergy
    val displayThroughput = activeStage?.throughputKg?.toFloat() ?: globalThroughput.toFloat()
    val displayProjection = activeStage?.aiProjection ?: stages.map { it.aiProjection }.average().toFloat()

    val animOee by animateFloatAsState(targetValue = displayOee, animationSpec = tween(800), label = "")
    val animEnergy by animateFloatAsState(targetValue = displayEnergy, animationSpec = tween(800), label = "")
    val animThroughput by animateFloatAsState(targetValue = displayThroughput, animationSpec = tween(800), label = "")
    val animProjection by animateFloatAsState(targetValue = displayProjection, animationSpec = tween(800), label = "")

    val bgBrush = if (theme.isDark) Brush.linearGradient(listOf(BrandDeepNavy, BrandDarkBlueGray))
    else Brush.linearGradient(listOf(BrandOffWhite, Color.White))

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {

        Column(modifier = Modifier.fillMaxSize().padding(start = 32.dp, top = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // HEADER
            Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AURALISS DASHBOARD", color = theme.textMain, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val matrixColor by animateColorAsState(if(isLightMode) BrandOrange else BrandCyanBlue, label="")
                    Text(if(isLightMode) "☀️ MATRIX LIGHT" else "🌙 MATRIX DARK", color = matrixColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isLightMode, onCheckedChange = { isLightMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = BrandOffWhite, checkedTrackColor = BrandDarkBlueGray.copy(alpha=0.5f), uncheckedThumbColor = BrandOffWhite, uncheckedTrackColor = BrandMutedBlue), modifier = Modifier.scale(0.8f).height(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(BrandLightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AccountCircle, null, tint = theme.textMuted, modifier = Modifier.size(28.dp)) }
                }
            }

            Row(modifier = Modifier.fillMaxSize().padding(start = 24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                // ⬅️ LEFT COLUMN
                Column(modifier = Modifier.weight(2.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    CleanPanel(theme, modifier = Modifier.weight(2f).fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("PRODUCTION FLOW TREND (OEE %)", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Interactive Canvas:", color = theme.textMuted, fontSize = 12.sp)
                                    Text("Tap dotted nodes to trigger AI Diagnosis & log to history.", color = AccentAI, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth().weight(1.5f).padding(bottom = 16.dp, top = 24.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize().pointerInput(stages) {
                                    detectTapGestures { offset ->
                                        val w = this.size.width.toFloat(); val h = this.size.height.toFloat()
                                        val paddingLeft = 35.dp.toPx()
                                        val stepX = (w - paddingLeft - 20.dp.toPx()) / (stages.size - 1)

                                        stages.forEachIndexed { i, stage ->
                                            val nx = paddingLeft + (i * stepX)
                                            val py = h - 20.dp.toPx() - ((stage.aiProjection / 100f) * (h - 20.dp.toPx()))

                                            if (hypot(offset.x - nx, offset.y - py) < 50.dp.toPx()) {
                                                val aiRec = stage.stageRecommendations.firstOrNull()
                                                if (aiRec != null) {
                                                    zyrenPopupData = aiRec
                                                    if (!activeAiLogs.any { it.id == aiRec.id }) {
                                                        activeAiLogs.add(0, aiRec)
                                                        if(activeAiLogs.size > 10) activeAiLogs.removeLast()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }) {
                                    val w = size.width; val h = size.height;
                                    val paddingLeft = 35.dp.toPx()
                                    val graphW = w - paddingLeft - 20.dp.toPx(); val graphH = h - 20.dp.toPx()
                                    fun calcY(f: Float) = graphH - ((f / 100f) * graphH).coerceIn(0f, graphH)

                                    actualLinePath.reset(); predictLinePath.reset()
                                    val stepX = graphW / (stages.size - 1)

                                    // 1. Draw Alternating Background Columns
                                    stages.forEachIndexed { i, _ ->
                                        val nx = paddingLeft + (i * stepX)
                                        val bandLeft = if (i == 0) paddingLeft else nx - (stepX / 2)
                                        val bandRight = if (i == stages.size - 1) w else nx + (stepX / 2)
                                        val bandWidth = bandRight - bandLeft

                                        val bandColor = if (i % 2 == 0) AccentPrimary.copy(alpha = if(theme.isDark) 0.08f else 0.04f)
                                        else AccentSuccess.copy(alpha = if(theme.isDark) 0.08f else 0.04f)

                                        drawRect(
                                            color = bandColor,
                                            topLeft = Offset(bandLeft, 0f),
                                            size = Size(bandWidth, graphH)
                                        )
                                    }

                                    // 2. Draw Y-Axis & Grids
                                    drawLine(theme.textMuted.copy(alpha=0.5f), Offset(paddingLeft - 10.dp.toPx(), 0f), Offset(paddingLeft - 10.dp.toPx(), graphH), 1.5.dp.toPx())
                                    listOf(0f, 25f, 50f, 75f, 100f).forEach { tick ->
                                        val y = calcY(tick)
                                        drawContext.canvas.nativeCanvas.drawText("${tick.toInt()}%", 0f, y + 4.sp.toPx(), yAxisPaint)
                                        drawLine(theme.textMuted.copy(alpha = 0.15f), Offset(paddingLeft, y), Offset(w, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                                    }

                                    // 3. Build Line Paths
                                    stages.forEachIndexed { i, stage ->
                                        val nx = paddingLeft + (i * stepX); val ay = calcY(stage.actualFlow); val py = calcY(stage.aiProjection)
                                        if (i == 0) { actualLinePath.moveTo(nx, ay); predictLinePath.moveTo(nx, py) }
                                        else { actualLinePath.lineTo(nx, ay); predictLinePath.lineTo(nx, py) }
                                    }

                                    // 4. Draw Lines
                                    drawPath(predictLinePath, AccentSuccess.copy(alpha=0.9f), style = Stroke(2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))))
                                    drawPath(actualLinePath, AccentPrimary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                                    // 5. Draw Dots & Text
                                    stages.forEachIndexed { i, stage ->
                                        val nx = paddingLeft + (i * stepX); val ay = calcY(stage.actualFlow); val py = calcY(stage.aiProjection)

                                        // Predict Point (Green)
                                        drawCircle(BrandOffWhite, 6.dp.toPx(), Offset(nx, py)); drawCircle(AccentSuccess, 4.dp.toPx(), Offset(nx, py))
                                        drawContext.canvas.nativeCanvas.drawText("${stage.aiProjection.toInt()}%", nx, py + 22.sp.toPx(), predictTextPaint)

                                        // Actual Point (Blue)
                                        drawCircle(BrandOffWhite, 6.dp.toPx(), Offset(nx, ay)); drawCircle(AccentPrimary, 4.dp.toPx(), Offset(nx, ay))
                                        drawContext.canvas.nativeCanvas.drawText("${stage.actualFlow.toInt()}%", nx, ay - 12.sp.toPx(), actualTextPaint)

                                        // Stage Name
                                        drawContext.canvas.nativeCanvas.drawText(stage.name, nx, 10.sp.toPx(), stageNamePaint)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth().weight(0.9f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                stages.forEach { stage ->
                                    val isSelected = selectedStageId == stage.id
                                    val animCardOee by animateFloatAsState(targetValue = stage.juicePercentage.toFloat(), animationSpec = tween(500), label = "")

                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) stage.tintColor.copy(alpha = 0.2f) else theme.trackBg.copy(alpha=0.3f))
                                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) stage.tintColor else BrandLightGray.copy(0.2f), RoundedCornerShape(16.dp))
                                            .clickable { selectedStageId = if(selectedStageId == stage.id) null else stage.id }
                                    ) {
                                        val fillHeight = (animCardOee / 100f).coerceIn(0f, 1f)

                                        // Small bar joined at the edge showing the level
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(vertical = 4.dp)
                                                .width(6.dp)
                                                .fillMaxHeight(fillHeight)
                                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                                .background(stage.tintColor)
                                        )

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(start = 8.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stage.name, color = theme.textMain, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)

                                            // Stage and Flow Rate combined in the box
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "${stage.actualFlow.toInt()}",
                                                    color = if(isLightMode) theme.textLightMuted else BrandOffWhite,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Black,
                                                    style = TextStyle(shadow = Shadow(color = BrandDeepNavy.copy(alpha = 0.1f), offset = Offset(0f, 2f), blurRadius = 4f))
                                                )
                                                Text("FLOW RATE", color = stage.tintColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }

                                            val statStyle = TextStyle(shadow = Shadow(color = BrandDeepNavy.copy(alpha = 0.1f), blurRadius = 2f))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("TMP: ${stage.tempC.toInt()}°C", color = if(isLightMode) theme.textLightMuted else BrandOffWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, style = statStyle)
                                                Text("VIB: ${stage.vibrationHz.toInt()}Hz", color = if(isLightMode) theme.textLightMuted else BrandOffWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, style = statStyle)
                                                Text("PWR: ${stage.energyKw.toInt()}kW", color = if(isLightMode) theme.textLightMuted else BrandOffWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, style = statStyle)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(20.dp)) {
                                Text(if(activeStage != null) "${activeStage.name} AI PROJECTION" else "GLOBAL AI PROJECTIONS", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${String.format("%.1f", animProjection)}", color = AccentAI, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                                    Text("T/hr", color = theme.textMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val trendColor = if(animProjection > 50f) AccentSuccess else AccentCritical
                                    Icon(Icons.Rounded.ArrowUpward, null, tint = trendColor, modifier = Modifier.size(16.dp))
                                    Text(" ${if(animProjection > 50f) "8.2%" else "-14%"} vs Current", color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(if(activeStage != null) "Stage AI Forecast" else "System Next 4 Hours Forecast", color = theme.textLightMuted, fontSize = 11.sp)
                            }
                        }

                        CleanPanel(theme, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(20.dp)) {
                                Text(if(activeStage != null) "${activeStage.name} EFFICIENCY" else "GLOBAL OEE", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${animOee.toInt()}", color = activeStage?.tintColor ?: AccentPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                                    Text("%", color = theme.textMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                if(activeStage != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.WarningAmber, null, tint = AccentCritical, modifier = Modifier.size(14.dp))
                                        Text(" Downtime: ${activeStage.downtimeMins} mins", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            val minVal = 70f; val maxVal = 100f; val stepX = size.width / (oeeHistory.size - 1).coerceAtLeast(1)
                                            val chartPath = Path()
                                            oeeHistory.forEachIndexed { i, value ->
                                                val nx = i * stepX; val ny = size.height - ((value - minVal) / (maxVal - minVal) * size.height).coerceIn(0f, size.height)
                                                if (i == 0) chartPath.moveTo(nx, ny) else chartPath.lineTo(nx, ny)
                                            }
                                            drawPath(chartPath, AccentPrimary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                        }
                                    }
                                }
                            }
                        }

                        CleanPanel(theme, modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                            Column(Modifier.fillMaxSize().padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.Sensors, null, tint = theme.textMuted, modifier = Modifier.size(16.dp))
                                    Text(if(activeStage != null) "${activeStage.name} TELEMETRY" else "SYSTEM TELEMETRY", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(12.dp))
                                val t = activeStage?.tempC ?: stages.map{it.tempC}.average().toFloat()
                                val v = activeStage?.vibrationHz ?: stages.map{it.vibrationHz}.average().toFloat()
                                val p = activeStage?.pressureBar ?: stages.map{it.pressureBar}.average().toFloat()

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column { Text(if(activeStage != null) "TEMP" else "AVG TEMP", color = theme.textLightMuted, fontSize = 10.sp); Text("${t.toInt()} °C", color = if(t > 90f) AccentCritical else theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                                    Column { Text(if(activeStage != null) "VIBE" else "AVG VIBE", color = theme.textLightMuted, fontSize = 10.sp); Text("${v.toInt()} Hz", color = if(v > 150f) AccentWarning else theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                                    Column { Text(if(activeStage != null) "PRES" else "AVG PRES", color = theme.textLightMuted, fontSize = 10.sp); Text(String.format("%.1f", p), color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                                }
                                Spacer(Modifier.weight(1f))
                                val stateColor = if(activeStage?.status == "BLOCK") AccentCritical else AccentSuccess
                                Text("STATUS: ${activeStage?.status ?: "ALL NOMINAL"}", color = stateColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ➡️ RIGHT COLUMN
                Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // MERGED ENERGY AND THROUGHPUT BOX
                    CleanPanel(theme, modifier = Modifier.fillMaxWidth().height(170.dp)) {
                        Row(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                            // Left Side: Energy
                            Column(Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.size(26.dp).background(AccentWarning.copy(alpha=0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ElectricBolt, null, tint = AccentWarning, modifier = Modifier.size(16.dp)) }
                                    Text(if(activeStage != null) "${activeStage.name} Energy" else "Global Energy", color = theme.textMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${animEnergy.toInt()}", color = theme.textMain, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                                    Text("kW", color = theme.textMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                                    if(activeStage == null) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            val barWidth = size.width / (energyHistory.size * 1.5f); val minVal = 100f; val maxVal = 200f
                                            energyHistory.forEachIndexed { index, value ->
                                                val fillRatio = ((value - minVal) / (maxVal - minVal)).coerceIn(0.1f, 1f); val barHeight = fillRatio * size.height
                                                val color = if (value > 180f) AccentCritical else if (value > 165f) AccentWarning else AccentSuccess
                                                drawRoundRect(color = color, topLeft = Offset(index * (size.width / energyHistory.size.toFloat()), size.height - barHeight), size = Size(barWidth, barHeight), cornerRadius = CornerRadius(4.dp.toPx()))
                                            }
                                        }
                                    }
                                }
                            }

                            // Vertical Subtle Divider
                            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(theme.textMuted.copy(alpha = 0.2f)))

                            // Right Side: Throughput
                            Column(Modifier.weight(1f).fillMaxHeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val tColor = activeStage?.tintColor ?: AccentPrimary
                                    Box(Modifier.size(26.dp).background(tColor.copy(alpha=0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Schedule, null, tint = tColor, modifier = Modifier.size(16.dp)) }
                                    Text(if(activeStage != null) "Flow Rate" else "Throughput", color = theme.textMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${animThroughput.toInt()}", color = theme.textMain, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    Text("kg/h", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                // Empty spacer to balance layout with the energy chart space
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    }

                    CleanPanel(theme, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Zyren AI Predictions Log", color = AccentAI, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            if(activeAiLogs.isEmpty()) {
                                Text("No AI Logs stored. Tap a dashed prediction node on the graph to analyze.", color = theme.textMuted, fontSize = 12.sp, modifier = Modifier.padding(top=10.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(activeAiLogs) { rec ->
                                        Column(Modifier.fillMaxWidth().background(theme.trackBg.copy(alpha=0.2f), RoundedCornerShape(14.dp)).border(1.dp, theme.textMuted.copy(alpha=0.1f), RoundedCornerShape(14.dp)).clickable { zyrenPopupData = rec }.padding(14.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(rec.stageName, color = theme.textMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                val pColor = if(rec.priority == "Critical" || rec.priority == "High") AccentCritical else AccentWarning
                                                Text(rec.priority, color = pColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text("⚠️ ${rec.problem}", color = AccentCritical, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CleanPanel(theme, modifier = Modifier.fillMaxWidth().weight(1.3f)) {
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            Text("Active Alerts & Bottlenecks", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Auto-logging anomalies every 5s.", color = theme.textLightMuted, fontSize = 11.sp)
                            Spacer(Modifier.height(16.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(activeAlerts, key = { it.id }) { alert ->
                                    val isCrit = alert.priority == "CRITICAL"
                                    val color = if(isCrit) AccentCritical else if(alert.priority == "WARNING") AccentWarning else AccentPrimary

                                    Row(Modifier.fillMaxWidth().background(theme.trackBg.copy(alpha=0.2f), RoundedCornerShape(14.dp)).border(1.dp, color.copy(alpha=0.3f), RoundedCornerShape(14.dp)).clickable { popupAlertToView = alert }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if(alert.priority == "INFO") Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = color, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alert.message, color = theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text("${alert.stage} • ${alert.metricFailed}", color = color, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================================
        // 🚨 DIALOG POPUPS
        // ============================================================================
        if (zyrenPopupData != null) {
            Dialog(onDismissRequest = { zyrenPopupData = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CleanPanel(theme = theme, modifier = Modifier.width(420.dp)) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Outlined.Memory, null, tint = AccentAI, modifier = Modifier.size(28.dp))
                                    Column { Text("ZYREN AI DIAGNOSIS", color = AccentAI, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(zyrenPopupData!!.stageName, color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                                }
                                IconButton(onClick = { zyrenPopupData = null }) { Icon(Icons.Rounded.Close, null, tint = theme.textMuted) }
                            }

                            Column(modifier = Modifier.fillMaxWidth().background(AccentCritical.copy(0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                                Text("Predicted Bottleneck", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(zyrenPopupData!!.problem, color = theme.textMain, fontSize = 14.sp)
                            }

                            Column(modifier = Modifier.fillMaxWidth().background(AccentSuccess.copy(0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                                Text("AI Suggested Solution", color = AccentSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(zyrenPopupData!!.suggestion, color = theme.textMain, fontSize = 14.sp)
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("Acknowledge", color = BrandOffWhite, fontSize = 12.sp, modifier = Modifier.background(AccentAI, RoundedCornerShape(8.dp)).clickable { zyrenPopupData = null }.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        if (popupAlertToView != null) {
            Dialog(onDismissRequest = { popupAlertToView = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CleanPanel(theme = theme, modifier = Modifier.width(420.dp)) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.WarningAmber, null, tint = AccentCritical, modifier = Modifier.size(28.dp))
                                    Column { Text("SYSTEM ALERT", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(popupAlertToView!!.stage, color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                                }
                                IconButton(onClick = { popupAlertToView = null }) { Icon(Icons.Rounded.Close, null, tint = theme.textMuted) }
                            }
                            Text(popupAlertToView!!.message, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(popupAlertToView!!.details, color = theme.textMuted, fontSize = 14.sp)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("Acknowledge", color = BrandOffWhite, fontSize = 12.sp, modifier = Modifier.background(AccentCritical, RoundedCornerShape(8.dp)).clickable { popupAlertToView = null }.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp).zIndex(20f), contentAlignment = Alignment.TopCenter) {
            AnimatedVisibility(visible = toastAlert != null, enter = slideInVertically(initialOffsetY = { -it - 50 }) + fadeIn(tween(400)), exit = slideOutVertically(targetOffsetY = { -it - 50 }) + fadeOut(tween(400))) {
                toastAlert?.let { alert ->
                    val pColor = when (alert.priority) { "CRITICAL" -> AccentCritical; "WARNING" -> AccentWarning; else -> AccentPrimary }
                    Row(modifier = Modifier.width(320.dp).shadow(20.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(colors = listOf(BrandOffWhite, BrandLightGray))).border(1.5.dp, BrandOffWhite, RoundedCornerShape(24.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(42.dp).background(pColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = if(alert.priority == "INFO") Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, contentDescription = null, tint = pColor, modifier = Modifier.size(24.dp)) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = alert.message, color = BrandDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "${alert.stage} - ${alert.metricFailed}", color = BrandDarkBlueGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        RadialAppBar(
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-8).dp).zIndex(30f),
            activeSection = "HOME",
            onActionSelected = { action ->
                when (action) {
                    "home" -> { onNavigateToScreen("workflow_dashboard") }
                    "data_entry" -> { onNavigateToScreen("login_screen") }
                    "settings" -> { onNavigateToScreen("settings_screen") }
                    "about_us" -> { onNavigateToScreen("about_us_screen") }
                }
            }
        )
    }
}

// ============================================================================
// 💎 PREMIUM LIQUID GREY FROST GLASS-NEUMORPHISM COMPONENT
// ============================================================================
@Composable
fun CleanPanel(theme: DashboardTheme, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)

    // Liquid Grey Frost Gradient
    val bgColors = if (theme.isDark) {
        listOf(BrandDarkBlueGray.copy(alpha = 0.45f), BrandSteelGray.copy(alpha = 0.15f))
    } else {
        // Elegant light grey frosting that lets background bleed through
        listOf(BrandOffWhite.copy(alpha = 0.85f), BrandLightGray.copy(alpha = 0.35f))
    }

    // Reflective edge highlight for glass feel
    val borderColors = if (theme.isDark) {
        listOf(BrandLightGray.copy(alpha = 0.3f), Color.Transparent)
    } else {
        // Crisp white rim fading into faint grey
        listOf(Color.White, BrandLightGray.copy(alpha = 0.4f))
    }

    // Soft, diffuse drop shadow
    val shadowColor = if (theme.isDark) Color.Black else BrandDeepNavy.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 24.dp,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = bgColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = borderColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            ),
        content = content
    )
}
//package com.example.category3.auth.ui
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AttachMoney
//import androidx.compose.material.icons.filled.AutoAwesome
//import androidx.compose.material.icons.filled.Build
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.Factory
//import androidx.compose.material.icons.filled.Layers
//import androidx.compose.material.icons.filled.LinkOff
//import androidx.compose.material.icons.filled.MonitorHeart
//import androidx.compose.material.icons.filled.Power
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.Speed
//import androidx.compose.material.icons.filled.WaterDrop
//import androidx.compose.material.icons.outlined.Assessment
//import androidx.compose.material.icons.outlined.Inventory
//import androidx.compose.material.icons.outlined.Lightbulb
//import androidx.compose.material.icons.outlined.PrecisionManufacturing
//import androidx.compose.material.icons.outlined.Schedule
//import androidx.compose.material.icons.outlined.Settings
//import androidx.compose.material.icons.outlined.WarningAmber
//import androidx.compose.material.icons.rounded.Info
//import androidx.compose.material.icons.rounded.Warning
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//import kotlinx.coroutines.delay
//import kotlin.random.Random
//
//// ============================================================================
//// 🎨 PROFESSIONAL LIGHT SAAS THEME
//// ============================================================================
//val BgAppSolid = Color(0xFFF4F7FC)
//val CardBgSolid = Color(0xFFFFFFFF)
//val BorderLight = Color(0xFFE2E8F0)
//
//val TextDark = Color(0xFF0F172A)
//val TextMuted = Color(0xFF64748B)
//
//val BlueBrand = Color(0xFF2563EB)
//val BlueActual = Color(0xFF3B82F6)
//val PurplePredict = Color(0xFF8B5CF6)
//val GreenTarget = Color(0xFF10B981)
//val OrangeAlert = Color(0xFFF97316)
//val RedCritical = Color(0xFFEF4444)
//val CyanAccent = Color(0xFF06B6D4)
//
//// ============================================================================
//// 📡 DATA MODELS
//// ============================================================================
//data class ProcessStage(
//    val id: String, val name: String, val efficiency: Int,
//    val metricLabel: String, val metricValue: String, val metricUnit: String,
//    val status: String, val isBottleneck: Boolean, val color: Color,
//    val actualFlow: Float, val predictedFlow: Float, val targetFlow: Float
//)
//
//data class AlertMessage(val title: String, val message: String, val isCritical: Boolean)
//
//// ============================================================================
//// 📱 MAIN DASHBOARD SCREEN
//// ============================================================================
//@Composable
//fun WorkflowDashboardScreen() {
//
//    var topKpis by remember { mutableStateOf(listOf("1,248", "31,250", "91.8%", "92%", "112", "2h 15m")) }
//    var currentEnergy by remember { mutableStateOf(495) }
//    var oee by remember { mutableStateOf(87) }
//
//    var stages by remember {
//        mutableStateOf(
//            listOf(
//                ProcessStage("01", "MILL", 96, "Juice Flow", "13.5", "T/hr", "Running", false, BlueActual, 12.8f, 14.2f, 15.0f),
//                ProcessStage("02", "DEFECATION", 100, "pH Level", "6.8", "", "Running", false, GreenTarget, 13.8f, 13.6f, 14.0f),
//                ProcessStage("03", "EVAPORATION", 57, "Steam Efficiency", "57", "%", "BOTTLENECK", true, OrangeAlert, 11.2f, 10.8f, 11.0f),
//                ProcessStage("04", "CLARIFICATION", 97, "Purity", "91", "%", "Running", false, PurplePredict, 11.7f, 11.7f, 12.5f),
//                ProcessStage("05", "CONCENTRATION", 86, "Brix", "72", "°Bx", "Running", false, BlueActual, 8.8f, 10.3f, 10.5f)
//            )
//        )
//    }
//
//    var activeAlert by remember { mutableStateOf<AlertMessage?>(null) }
//
//    // DATA SIMULATION LOOP
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(2000)
//            stages = stages.map {
//                it.copy(
//                    efficiency = (it.efficiency + Random.nextInt(-2, 3)).coerceIn(0, 100),
//                    actualFlow = (it.actualFlow + Random.nextFloat() * 0.4f - 0.2f).coerceIn(8f, 18f)
//                )
//            }
//            currentEnergy = (currentEnergy + Random.nextInt(-5, 6)).coerceIn(450, 550)
//            oee = (oee + Random.nextInt(-1, 2)).coerceIn(0, 100)
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val alerts = listOf(
//            AlertMessage("Evaporation Bottleneck", "Steam pressure dropping below target threshold.", true),
//            AlertMessage("AI Optimization", "Mill speed adjusted automatically. Yield +1.2%.", false)
//        )
//        while (true) {
//            delay(6000)
//            activeAlert = alerts.random()
//            delay(4000)
//            activeAlert = null
//        }
//    }
//
//    // ---------------------------------------------------------
//    // 🎨 UI LAYOUT (70% / 30% Split for less congestion)
//    // ---------------------------------------------------------
//    Box(modifier = Modifier.fillMaxSize().background(BgAppSolid)) {
//        Row(modifier = Modifier.fillMaxSize()) {
//            Sidebar()
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(24.dp),
//                verticalArrangement = Arrangement.spacedBy(20.dp)
//            ) {
//                // ROW 1: HEADER
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Text("AURALISS ", color = BlueBrand, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                    Text("DASHBOARD", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = Arrangement.spacedBy(20.dp)
//                ) {
//                    // ==========================================
//                    // LEFT COLUMN (70% instead of 75%)
//                    // ==========================================
//                    Column(
//                        modifier = Modifier.weight(0.7f).fillMaxHeight(),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        // 1. TOP KPIs (Slightly taller to breathe)
//                        Row(modifier = Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Factory, BlueActual, "Production Today", topKpis[0] + " MT")
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.AttachMoney, GreenTarget, "Cost / Ton", "₹" + topKpis[1])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Layers, CyanAccent, "Yield", topKpis[2])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.MonitorHeart, PurplePredict, "Machine Health", topKpis[3])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Speed, BlueActual, "Throughput", topKpis[4])
//                            TopKpiCard(Modifier.weight(1f), Icons.Outlined.Schedule, OrangeAlert, "Downtime", topKpis[5])
//                        }
//
//                        // 2. MIDDLE AREA (Carousel + Chart)
//                        WhitePanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
//                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { // Reduced padding for inner space
//
//                                // 🎴 5 OVERLAPPING CARDS (Reduced scaling & width to fix congestion)
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .weight(0.55f)
//                                        .padding(bottom = 16.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    val cardWidth = 140.dp // Reduced from 160.dp
//                                    val offsets = listOf((-220).dp, (-110).dp, 0.dp, 110.dp, 220.dp) // Adjusted spread
//                                    val scales = listOf(0.75f, 0.85f, 1.05f, 0.85f, 0.75f) // Reduced scale so center isn't massive
//                                    val zIndices = listOf(1f, 2f, 3f, 2f, 1f)
//
//                                    stages.forEachIndexed { index, stage ->
//                                        ProcessCard(
//                                            stage = stage,
//                                            modifier = Modifier
//                                                .zIndex(zIndices[index])
//                                                .offset(x = offsets[index])
//                                                .scale(scales[index])
//                                                .width(cardWidth)
//                                                .fillMaxHeight(0.9f)
//                                        )
//                                    }
//                                }
//
//                                // 📈 LINE CHART
//                                TrendChartSection(stages, modifier = Modifier.weight(0.45f))
//                            }
//                        }
//
//                        // 3. BOTTOM ROW (3 Cards)
//                        Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                            OeeCard(Modifier.weight(1f), oee)
//                            EnergyCard(Modifier.weight(1f), currentEnergy)
//                            HealthCard(Modifier.weight(1f))
//                        }
//                    }
//
//                    // ==========================================
//                    // RIGHT COLUMN (30% instead of 25%)
//                    // ==========================================
//                    Column(
//                        modifier = Modifier.weight(0.3f).fillMaxHeight(),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        DecisionCenterUnified(Modifier.weight(1f))
//                        AiGainCard(Modifier.height(120.dp))
//                    }
//                }
//            }
//        }
//
//        // ==========================================
//        // OVERLAY: ALERT TOAST
//        // ==========================================
//        Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.TopCenter) {
//            AnimatedVisibility(
//                visible = activeAlert != null,
//                enter = slideInVertically(initialOffsetY = { -it - 50 }) + fadeIn(),
//                exit = slideOutVertically(targetOffsetY = { -it - 50 }) + fadeOut()
//            ) {
//                activeAlert?.let { alert ->
//                    Row(
//                        modifier = Modifier
//                            .shadow(16.dp, RoundedCornerShape(30.dp), spotColor = if (alert.isCritical) RedCritical else BlueBrand)
//                            .clip(RoundedCornerShape(30.dp))
//                            .background(CardBgSolid)
//                            .border(1.dp, (if (alert.isCritical) RedCritical else BlueBrand).copy(alpha = 0.3f), RoundedCornerShape(30.dp))
//                            .padding(horizontal = 24.dp, vertical = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Icon(
//                            if (alert.isCritical) Icons.Rounded.Warning else Icons.Rounded.Info,
//                            contentDescription = null,
//                            tint = if (alert.isCritical) RedCritical else BlueBrand,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Column {
//                            Text(alert.title, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                            Text(alert.message, color = TextMuted, fontSize = 12.sp)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ============================================================================
//// 🧩 UI COMPONENTS
//// ============================================================================
//
//@Composable
//fun WhitePanel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
//    Box(
//        modifier = modifier
//            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x14000000), ambientColor = Color(0x0A000000))
//            .clip(RoundedCornerShape(16.dp))
//            .background(CardBgSolid)
//            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
//        content = content
//    )
//}
//
//@Composable
//fun Sidebar() {
//    Column(
//        modifier = Modifier.fillMaxHeight().width(80.dp).background(CardBgSolid).border(1.dp, BorderLight).padding(vertical = 32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(36.dp)
//    ) {
//        Icon(Icons.Filled.Dashboard, null, tint = BlueBrand, modifier = Modifier.size(28.dp))
//        Icon(Icons.Outlined.PrecisionManufacturing, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Inventory, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.WarningAmber, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Assessment, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Lightbulb, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Spacer(modifier = Modifier.weight(1f))
//        Icon(Icons.Outlined.Settings, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//    }
//}
//
//// ----------------------------------------------------------------------------
//// Fix for Wrapping text: Stack layout for KPIs
//// ----------------------------------------------------------------------------
//@Composable
//fun TopKpiCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, title: String, value: String) {
//    WhitePanel(modifier = modifier.fillMaxHeight()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(12.dp),
//            verticalArrangement = Arrangement.SpaceBetween,
//            horizontalAlignment = Alignment.Start
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
//                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
//                }
//                Text(value, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
//            }
//            Text(title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 🏭 OVERLAPPING PROCESS CARD (Smaller Elements)
//// ----------------------------------------------------------------------------
//@Composable
//fun ProcessCard(stage: ProcessStage, modifier: Modifier) {
//    val isMain = stage.isBottleneck
//
//    Box(
//        modifier = modifier
//            .shadow(if(isMain) 16.dp else 4.dp, RoundedCornerShape(16.dp), spotColor = if(isMain) OrangeAlert.copy(alpha=0.3f) else Color.Black.copy(alpha=0.05f))
//            .clip(RoundedCornerShape(16.dp))
//            .background(CardBgSolid)
//            .border(if(isMain) 2.dp else 1.dp, if(isMain) stage.color.copy(alpha=0.5f) else BorderLight, RoundedCornerShape(16.dp))
//            .padding(12.dp) // Reduced padding for breathing room
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
//                Box(modifier = Modifier.size(20.dp).background(stage.color, CircleShape), contentAlignment = Alignment.Center) {
//                    Text(stage.id, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//
//            Icon(Icons.Outlined.PrecisionManufacturing, null, tint = TextMuted.copy(alpha=0.6f), modifier = Modifier.size(if(isMain) 48.dp else 36.dp))
//            Text(stage.name, color = TextDark, fontSize = if(isMain) 13.sp else 11.sp, fontWeight = FontWeight.ExtraBold)
//
//            Box(modifier = Modifier.size(if(isMain) 76.dp else 60.dp), contentAlignment = Alignment.Center) {
//                Canvas(Modifier.fillMaxSize()) {
//                    drawArc(Color(0xFFF1F5F9), -90f, 360f, false, style = Stroke(5.dp.toPx()))
//                    drawArc(stage.color, -90f, (stage.efficiency / 100f) * 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
//                }
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("${stage.efficiency}%", color = TextDark, fontSize = if(isMain) 20.sp else 16.sp, fontWeight = FontWeight.Bold)
//                    Text("Efficiency", color = TextMuted, fontSize = 8.sp)
//                }
//            }
//
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Filled.WaterDrop, null, tint = stage.color, modifier = Modifier.size(10.dp))
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(stage.metricLabel, color = TextMuted, fontSize = 9.sp)
//                }
//                Row(verticalAlignment = Alignment.Bottom) {
//                    Text(stage.metricValue, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                    if (stage.metricUnit.isNotEmpty()) Text(" ${stage.metricUnit}", color = TextMuted, fontSize = 9.sp, modifier = Modifier.padding(bottom=2.dp))
//                }
//            }
//
//            val statusColor = if(stage.status == "Running") GreenTarget else RedCritical
//            Row(
//                modifier = Modifier
//                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
//                    .padding(horizontal = 10.dp, vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(modifier = Modifier.size(5.dp).background(statusColor, CircleShape))
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(stage.status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 📈 MULTI-LINE TREND CHART
//// ----------------------------------------------------------------------------
//@Composable
//fun TrendChartSection(stages: List<ProcessStage>, modifier: Modifier) {
//    Column(modifier = modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(16.dp)) {
//
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            Text("PRODUCTION FLOW TREND (T/hr)", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                LegendLine("Actual", BlueActual, false)
//                LegendLine("Predicted", PurplePredict, true)
//                LegendLine("Design Target", GreenTarget, true)
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val w = size.width
//            val h = size.height
//            val maxVal = 20f
//            val paddingX = 40.dp.toPx()
//            val stepX = (w - paddingX * 2) / (stages.size - 1)
//
//            val ySteps = listOf(0, 5, 10, 15, 20)
//            ySteps.forEach { v ->
//                val y = h - (v / maxVal) * h
//                drawContext.canvas.nativeCanvas.drawText(v.toString(), 0f, y + 4.dp.toPx(), android.graphics.Paint().apply { color = TextMuted.toArgb(); textSize = 9.sp.toPx() })
//                drawLine(Color.LightGray.copy(alpha=0.3f), Offset(20.dp.toPx(), y), Offset(w, y), 1.dp.toPx())
//            }
//
//            val highlightX = paddingX + (2 * stepX)
//            drawRect(
//                color = OrangeAlert.copy(alpha = 0.05f),
//                topLeft = Offset(highlightX - (stepX/2), 0f),
//                size = Size(stepX, h)
//            )
//
//            val pathActual = Path()
//            val pathPredict = Path()
//            val pathTarget = Path()
//
//            stages.forEachIndexed { i, stage ->
//                val nx = paddingX + (i * stepX)
//                val ay = h - (stage.actualFlow / maxVal) * h
//                val py = h - (stage.predictedFlow / maxVal) * h
//                val ty = h - (stage.targetFlow / maxVal) * h
//
//                if (i == 0) {
//                    pathActual.moveTo(nx, ay); pathPredict.moveTo(nx, py); pathTarget.moveTo(nx, ty)
//                } else {
//                    val prevX = paddingX + ((i - 1) * stepX)
//                    val prevAy = h - (stages[i - 1].actualFlow / maxVal) * h
//                    val prevPy = h - (stages[i - 1].predictedFlow / maxVal) * h
//                    val prevTy = h - (stages[i - 1].targetFlow / maxVal) * h
//
//                    pathActual.cubicTo(prevX + stepX/2, prevAy, prevX + stepX/2, ay, nx, ay)
//                    pathPredict.cubicTo(prevX + stepX/2, prevPy, prevX + stepX/2, py, nx, py)
//                    pathTarget.cubicTo(prevX + stepX/2, prevTy, prevX + stepX/2, ty, nx, ty)
//                }
//
//                drawCircle(CardBgSolid, 4.dp.toPx(), Offset(nx, ty))
//                drawCircle(GreenTarget, 3.dp.toPx(), Offset(nx, ty), style = Stroke(2.dp.toPx()))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.targetFlow), nx, ty - 8.dp.toPx(), android.graphics.Paint().apply { color = GreenTarget.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true })
//
//                drawCircle(CardBgSolid, 4.dp.toPx(), Offset(nx, py))
//                drawCircle(PurplePredict, 3.dp.toPx(), Offset(nx, py), style = Stroke(2.dp.toPx()))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.predictedFlow), nx, py + 16.dp.toPx(), android.graphics.Paint().apply { color = PurplePredict.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
//
//                drawCircle(BlueActual, 4.dp.toPx(), Offset(nx, ay))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.actualFlow), nx, ay + 16.dp.toPx(), android.graphics.Paint().apply { color = BlueActual.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true })
//
//                val label = stage.name.lowercase().replaceFirstChar { it.uppercase() }
//                drawContext.canvas.nativeCanvas.drawText(label, nx, h + 20.dp.toPx(), android.graphics.Paint().apply { color = TextDark.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
//            }
//
//            drawPath(pathTarget, GreenTarget, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
//            drawPath(pathPredict, PurplePredict, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
//            drawPath(pathActual, BlueActual, style = Stroke(2.dp.toPx()))
//        }
//    }
//}
//
//@Composable
//fun LegendLine(text: String, color: Color, isDashed: Boolean) {
//    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//        Canvas(modifier = Modifier.width(16.dp).height(2.dp)) {
//            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx(), pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null)
//        }
//        Text(text, color = TextDark, fontSize = 10.sp)
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 🤖 UNIFIED DECISION CENTER (Right Sidebar)
//// ----------------------------------------------------------------------------
//@Composable
//fun DecisionCenterUnified(modifier: Modifier) {
//    WhitePanel(modifier = modifier.fillMaxWidth()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text("AURALISS AI DECISION CENTER", color = BlueBrand, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, RedCritical.copy(alpha=0.3f), RoundedCornerShape(12.dp))
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
//                    Icon(Icons.Filled.Search, null, tint = RedCritical, modifier = Modifier.size(18.dp))
//                    Column {
//                        Text("ROOT CAUSE ANALYSIS", color = RedCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                        Text("Why is production low?", color = TextMuted, fontSize = 10.sp)
//                    }
//                }
//                Column(modifier = Modifier.padding(start = 24.dp)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("↓", color = TextMuted, fontSize = 14.sp, modifier = Modifier.offset(x = (-12).dp))
//                        Text(" Low Steam Pressure (2.1 bar)", color = TextDark, fontSize = 11.sp)
//                    }
//                    Spacer(Modifier.height(8.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("↓", color = TextMuted, fontSize = 14.sp, modifier = Modifier.offset(x = (-12).dp))
//                        Text(" Evaporator Throughput Low", color = TextDark, fontSize = 11.sp)
//                    }
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, OrangeAlert.copy(alpha=0.3f), RoundedCornerShape(12.dp))
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
//                    Icon(Icons.Filled.LinkOff, null, tint = OrangeAlert, modifier = Modifier.size(18.dp))
//                    Column {
//                        Text("CRITICAL BOTTLENECK", color = OrangeAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                        Text("Where is the constraint?", color = TextMuted, fontSize = 10.sp)
//                    }
//                }
//                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                    Column {
//                        Text("Evaporation Section", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
//                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=4.dp)) {
//                            Text("Loss Impact", color = RedCritical, fontSize = 10.sp, modifier = Modifier.background(RedCritical.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp))
//                            Spacer(Modifier.width(8.dp))
//                            Text("28 MT/day", color = TextDark, fontSize = 11.sp)
//                        }
//                    }
//
//                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
//                        Canvas(Modifier.fillMaxSize()) {
//                            drawArc(Color(0xFFE2E8F0), -90f, 360f, false, style = Stroke(5.dp.toPx()))
//                            drawArc(OrangeAlert, -90f, 0.72f * 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
//                        }
//                        Text("72%", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
//                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
//                    .clip(RoundedCornerShape(12.dp))
//            ) {
//                Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(GreenTarget))
//
//                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
//                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Icon(Icons.Filled.Build, null, tint = GreenTarget, modifier = Modifier.size(18.dp))
//                        Column {
//                            Text("RECOMMENDED ACTION", color = GreenTarget, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                            Text("What should we do?", color = TextMuted, fontSize = 10.sp)
//                        }
//                    }
//
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Filled.CheckCircle, null, tint = GreenTarget, modifier = Modifier.size(14.dp))
//                            Text(" Increase Steam Pressure by 0.7 bar", color = TextDark, fontSize = 11.sp, modifier = Modifier.padding(start=8.dp))
//                        }
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Filled.CheckCircle, null, tint = GreenTarget, modifier = Modifier.size(14.dp))
//                            Text(" Inspect Steam Control Valve V-12", color = TextDark, fontSize = 11.sp, modifier = Modifier.padding(start=8.dp))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AiGainCard(modifier: Modifier) {
//    WhitePanel(modifier = modifier.fillMaxWidth()) {
//        Box(Modifier.fillMaxSize().background(GreenTarget.copy(alpha=0.04f))) {
//            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Filled.AutoAwesome, null, tint = GreenTarget, modifier = Modifier.size(16.dp))
//                    Text(" AI GAIN (IMPACT)", color = GreenTarget, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start=8.dp))
//                }
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
//                    Column {
//                        Text("+4.8%", color = GreenTarget, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                        Text("vs Yesterday", color = TextMuted, fontSize = 10.sp)
//                    }
//                    Canvas(Modifier.width(50.dp).height(24.dp)) {
//                        val path = Path()
//                        path.moveTo(0f, size.height*0.8f); path.lineTo(size.width*0.3f, size.height*0.6f); path.lineTo(size.width*0.6f, size.height*0.2f); path.lineTo(size.width, 0f)
//                        drawPath(path, GreenTarget, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
//                    }
//                }
//                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                    Text("Savings", color = TextMuted, fontSize = 11.sp)
//                    Text("₹18,500/day", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 📊 BOTTOM ROW KPIs (Less Congested Text & Padding)
//// ----------------------------------------------------------------------------
//@Composable
//fun OeeCard(modifier: Modifier, oee: Int) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp)) {
//            Text("OEE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(Modifier.fillMaxSize().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
//                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
//                    Canvas(Modifier.fillMaxSize()) {
//                        drawArc(Color(0xFFE2E8F0), -90f, 360f, false, style = Stroke(5.dp.toPx()))
//                        drawArc(BlueActual, -90f, (oee / 100f) * 360f, false, style = Stroke(5.dp.toPx(), cap=StrokeCap.Round))
//                    }
//                    Text("$oee%", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                }
//                Column(Modifier.padding(start=12.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
//                    OeeStat("Availability", "91%")
//                    OeeStat("Performance", "95%")
//                    OeeStat("Quality", "99%")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EnergyCard(modifier: Modifier, energy: Int) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
//            Text("Energy Efficiency", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Filled.Power, null, tint = BlueBrand, modifier = Modifier.size(28.dp))
//                Column(Modifier.padding(start = 12.dp)) {
//                    Text("$energy kW", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                    Text("Current Consumption", color = TextMuted, fontSize = 10.sp)
//                }
//            }
//            Text("Total Usage: 1,850 kWh", color = TextMuted, fontSize = 10.sp)
//        }
//    }
//}
//
//@Composable
//fun HealthCard(modifier: Modifier) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp)) {
//            Text("Resource Health", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(Modifier.fillMaxWidth().padding(top=8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
//                Text("91%", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                Text("Healthy", color = GreenTarget, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            }
//            Box(Modifier.fillMaxWidth().padding(top=8.dp, bottom=8.dp).height(5.dp).background(Color(0xFFE2E8F0), CircleShape)) {
//                Box(Modifier.fillMaxWidth(0.91f).fillMaxHeight().background(GreenTarget, CircleShape))
//            }
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text("Next Service", color = TextMuted, fontSize = 10.sp)
//                Text("2 Days", color = TextDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//@Composable
//fun OeeStat(label: String, value: String) {
//    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//        Text(label, color = TextMuted, fontSize = 10.sp)
//        Text(value, color = GreenTarget, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//    }
//}