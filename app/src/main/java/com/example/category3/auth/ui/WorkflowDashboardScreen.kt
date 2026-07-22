package com.example.category3.auth.ui

import android.content.res.Configuration
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// --- DEFINED ROUTES: Safely maps your specific onclick redirects ---
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
val WarmWhite = Color(0xFFFDFBF7)
val AccentPrimary = BrandCyanBlue
val AccentSuccess = BrandTeal
val AccentWarning = BrandSoftOrange
val AccentCritical = BrandOrange
val AccentAI = BrandMutedBlue

data class DashboardTheme(val isDark: Boolean, val textMain: Color, val textMuted: Color, val textLightMuted: Color, val trackBg: Color)

fun getAdaptiveTheme(isDark: Boolean): DashboardTheme = if (isDark) {
    DashboardTheme(true, BrandOffWhite, BrandLightGray, BrandSteelGray, BrandDeepNavy.copy(alpha = 0.5f))
} else {
    DashboardTheme(false, BrandDeepNavy, BrandMutedBlue, BrandSteelGray, BrandLightGray.copy(alpha = 0.3f))
}

fun getDedicatedRouteForStage(stageId: String): String = when (stageId) {
    "01" -> AppDestinations.MILL_DEDICATED
    "02" -> AppDestinations.DEFECATOR_DEDICATED
    "03" -> AppDestinations.VACUUM_PAN_DEDICATED
    "04" -> AppDestinations.FLOTATION_CLARIFIER_DEDICATED
    "05" -> AppDestinations.CONCENTRATION_DEDICATED
    else -> AppDestinations.MILL_DEDICATED
}

data class StageAlertSummary(val critical: Int, val warning: Int)
data class StageCardDisplay(
    val primaryLabel: String, val primaryValue: String, val primaryUnit: String,
    val stat1Label: String, val stat1Value: String, val stat2Label: String,
    val stat2Value: String, val stat3Label: String, val stat3Value: String,
    val efficiency: Int, val uptime: Float, val loadPercent: Int
)

fun stageCardDisplay(stage: LiveStageData): StageCardDisplay = when (stage.id) {
    "01" -> StageCardDisplay("THROUGHPUT", String.format("%,.0f", stage.actualFlow), "kg/hr", "MOTOR", "${stage.energyKw.toInt()}A", "RPM", "${stage.vibrationHz.toInt()}", "TEMP", "${stage.tempC.toInt()}°C", stage.efficiency, 97.2f, 78)
    "02" -> StageCardDisplay("pH LEVEL", String.format("%.2f", stage.pressureBar), "pH", "DJ TEMP", "${stage.tempC.toInt()}°C", "TANK", "${stage.tankFillPercent}%", "PUMP", "${stage.vibrationHz.toInt()}A", stage.efficiency, 94.8f, 65)
    "03" -> StageCardDisplay("EVAP FLOW", String.format("%.1f", stage.actualFlow), "m³/hr", "TEMP B1", "${stage.tempC.toInt()}°C", "PRESS", String.format("%.2f", stage.pressureBar), "BRIX", "${stage.efficiency}°Bx", stage.efficiency, 91.5f, 88)
    "04" -> StageCardDisplay("CJ FLOW", String.format("%.2f", stage.actualFlow), "L/hr", "TEMP", "${stage.tempC.toInt()}°C", "TANK", "${stage.tankFillPercent}%", "FC VFD", "${stage.vibrationHz.toInt()}%", stage.efficiency, 96.1f, 72)
    "05" -> StageCardDisplay("SYRUP FLOW", String.format("%,.0f", stage.actualFlow), "L/hr", "PAN TEMP", "${stage.tempC.toInt()}°C", "VACUUM", "${stage.pressureBar.toInt()} mmHg", "RPM", "${stage.vibrationHz.toInt()}", stage.efficiency, 89.3f, 92)
    else -> StageCardDisplay("FLOW", String.format("%,.0f", stage.actualFlow), "u/hr", "TEMP", "${stage.tempC.toInt()}°C", "VIB", "${stage.vibrationHz.toInt()}Hz", "PWR", "${stage.energyKw.toInt()}kW", stage.efficiency, 95.0f, 70)
}

enum class AyamMood { NORMAL, WARNING, CRITICAL }
@RawRes fun ayamVideoForMood(mood: AyamMood): Int = when (mood) {
    AyamMood.NORMAL -> R.raw.ayam_happy
    AyamMood.WARNING -> R.raw.ayam_sad
    AyamMood.CRITICAL -> R.raw.ayam_angry
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun AyamVideoAvatar(modifier: Modifier = Modifier, @RawRes videoResId: Int) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = 0f
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    LaunchedEffect(videoResId) {
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/$videoResId")))
        exoPlayer.prepare()
        exoPlayer.play()
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(R.layout.avatar_player_view, null, false) as PlayerView).apply {
                useController = false
                player = exoPlayer
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                (videoSurfaceView as? android.view.TextureView)?.isOpaque = false
                setKeepContentOnPlayerReset(true)
            }
        },
        update = { view -> view.player = exoPlayer }
    )
}

data class SystemInsight(val id: String, val stage: String, val category: InsightCategory, val title: String, val problem: String, val rootCause: String, val impact: String, val severity: InsightSeverity, val metric: String, val metricValue: String, val trend: InsightTrend)
enum class InsightCategory { EFFICIENCY, ENERGY, QUALITY, MAINTENANCE, PROCESS }
enum class InsightSeverity { INFO, WARNING, CRITICAL }
enum class InsightTrend { UP, DOWN, STABLE }

val systemInsightsData = listOf(
    SystemInsight("INS-01-001", "MILLING", InsightCategory.EFFICIENCY, "Mill Throughput Below Target", "Current mill throughput has dropped 12% below the daily target of 6,500 kg/hr over the past 3 hours.", "Cane feed rate inconsistency detected at Carrier #2. Hydraulic pressure fluctuation (±18 bar) is causing intermittent feed stoppages, reducing crushing efficiency.", "Estimated 780 kg/hr production loss. If sustained, daily TCD target will be missed by ~8%.", InsightSeverity.WARNING, "Throughput", "5,712 kg/hr", InsightTrend.DOWN),
    SystemInsight("INS-01-002", "MILLING", InsightCategory.ENERGY, "Motor Draw Exceeding Nominal", "Mill Motor #3 drawing 94A against nominal 82A — 14.6% overcurrent sustained for 47 minutes.", "Trash plate gap has narrowed to 1.1mm vs recommended 1.8mm, causing fibrous buildup between rollers. This creates mechanical resistance that the motor compensates for with higher current draw.", "Accelerated motor insulation degradation. MTBF reduced by est. 340 hours. Energy cost overhead ~₹2,100/hr.", InsightSeverity.CRITICAL, "Motor Current", "94A / 82A", InsightTrend.UP),
    SystemInsight("INS-01-003", "MILLING", InsightCategory.MAINTENANCE, "Roller Bearing Vibration Elevated", "Vibration sensor on Top Roller Bearing reads 8.7 mm/s RMS — threshold is 6.5 mm/s RMS.", "Roller bearing lubrication interval was last completed 312 hours ago, exceeding the 280-hour service cycle. Oil viscosity degradation confirmed by inline sensor at 42 cSt (optimal: 68–74 cSt).", "Risk of bearing seizure within estimated 18–24 hours of continued operation at current load. Unplanned downtime cost estimated at ₹4.8L per hour.", InsightSeverity.CRITICAL, "Vibration", "8.7 mm/s", InsightTrend.UP),
    SystemInsight("INS-02-001", "JUICE TREATMENT", InsightCategory.PROCESS, "pH Overcorrection Detected", "Juice pH oscillating between 7.4 and 8.6 over 20-minute cycles — target is a stable 7.0–7.2 for optimal clarification.", "Milk of lime dosing PID controller (PIC-201) has integral windup due to the lime slurry density varying between 14–19 Bé. The density sensor DS-201 has a 4-minute response lag which destabilizes the closed-loop control.", "pH excursions above 8.2 cause phosphate precipitation inefficiency and increase color formation (color units +240 IU measured). Excess lime also increases mud volume by ~12%.", InsightSeverity.WARNING, "pH Swing", "7.4 – 8.6", InsightTrend.STABLE),
    SystemInsight("INS-03-001", "EVAPORATION", InsightCategory.ENERGY, "Steam Economy Below Design", "Quintuple effect steam economy is 4.1 kg water evaporated per kg steam — design target is 4.8.", "Effect III and Effect IV have significant scale buildup. Scale thermal resistance reduces effective heat transfer area by ~18%.", "Excess live steam consumption: ~2.8 t/hr above target.", InsightSeverity.CRITICAL, "Steam Economy", "4.1 kg/kg", InsightTrend.DOWN)
)

fun getInsightsForStage(stageName: String) = systemInsightsData.filter { it.stage.equals(stageName, ignoreCase = true) }
fun getAllInsights() = systemInsightsData

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun WorkflowDashboardScreen(viewModel: DashboardViewModel, onNavigateToScreen: (String) -> Unit) {
    val theme = getAdaptiveTheme(false)
    val globalOee by viewModel.globalOee.collectAsStateWithLifecycle()
    val globalEnergy by viewModel.globalEnergy.collectAsStateWithLifecycle()
    val globalThroughput by viewModel.globalThroughput.collectAsStateWithLifecycle()
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val activeBatchNo by viewModel.activeBatchNumber.collectAsStateWithLifecycle()
    val opanTimers by viewModel.opanTimers.collectAsStateWithLifecycle()
    val pmTimers by viewModel.pmTimers.collectAsStateWithLifecycle()

    var selectedStageId by remember { mutableStateOf<String?>(null) }
    var insightPopup by remember { mutableStateOf<SystemInsight?>(null) }
    var isAyamAssistOpen by remember { mutableStateOf(false) }

    val stageAlertSummary = remember(activeAlerts) {
        activeAlerts.groupBy { it.stage.uppercase() }.mapValues { (_, al) ->
            StageAlertSummary(al.count { it.priority.equals("CRITICAL", ignoreCase = true) }, al.count { it.priority.equals("WARNING", ignoreCase = true) })
        }
    }
    val ayamMood = remember(activeAlerts) {
        when {
            activeAlerts.any { !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true) } -> AyamMood.CRITICAL
            activeAlerts.any { !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true) } -> AyamMood.WARNING
            else -> AyamMood.NORMAL
        }
    }
    var heatmapTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); heatmapTick++ } }

    val energyHistory = remember { mutableStateListOf(160f, 165f, 172f, 168f, 175f, 180f, 178f, 182f, 184f) }
    LaunchedEffect(globalEnergy) { energyHistory.add(globalEnergy.toFloat()); if (energyHistory.size > 15) energyHistory.removeAt(0) }

    val throughputHistory = remember { mutableStateListOf(100f, 110f, 115f, 108f, 120f, 130f, 125f, 140f, 138f) }
    LaunchedEffect(globalThroughput) { throughputHistory.add(globalThroughput.toFloat()); if (throughputHistory.size > 15) throughputHistory.removeAt(0) }

    val displayStages = remember(stages) {
        val evapIdx = stages.indexOfFirst { it.id == "03" || it.name.contains("Evap", ignoreCase = true) }
        val clarIdx = stages.indexOfFirst { it.id == "04" || it.name.contains("Clar", ignoreCase = true) }
        if (evapIdx != -1 && clarIdx != -1 && evapIdx != clarIdx) {
            stages.toMutableList().apply { val tmp = this[evapIdx]; this[evapIdx] = this[clarIdx]; this[clarIdx] = tmp }
        } else stages
    }
    val activeStage = displayStages.find { it.id == selectedStageId }
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(WarmWhite)) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()
        val fabSizePx = with(LocalDensity.current) { 110.dp.toPx() }
        val coroutineScope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        var isDragging by remember { mutableStateOf(false) }
        var snappedEdge by remember { mutableStateOf("right") }
        var isInitialized by remember { mutableStateOf(false) }
        val density = LocalDensity.current

        LaunchedEffect(maxWidthPx, maxHeightPx) {
            if (!isInitialized && maxWidthPx > 0f && maxHeightPx > 0f) {
                offsetX.snapTo(maxWidthPx - fabSizePx * 0.55f)
                offsetY.snapTo(maxHeightPx * 0.7f)
                isInitialized = true
            } else if (isInitialized) {
                offsetX.snapTo(offsetX.value.coerceIn(-fabSizePx, maxWidthPx))
                offsetY.snapTo(offsetY.value.coerceIn(0f, maxHeightPx - fabSizePx))
            }
        }
        val targetRotation = when { isDragging || isAyamAssistOpen -> 0f; snappedEdge == "left" -> 25f; else -> -25f }
        val animRotation by animateFloatAsState(targetRotation, tween(400, easing = FastOutSlowInEasing), label = "rot")
        val animAlpha by animateFloatAsState(if (isDragging || isAyamAssistOpen) 1f else 0.65f, tween(400), label = "alpha")

        val handleBotClick = {
            if (!isAyamAssistOpen) {
                isAyamAssistOpen = true
                coroutineScope.launch {
                    val safeX = if (snappedEdge == "left") with(density) { 16.dp.toPx() } else maxWidthPx - fabSizePx - with(density) { 16.dp.toPx() }
                    offsetX.animateTo(safeX, tween(300, easing = FastOutSlowInEasing))
                }
            } else {
                isAyamAssistOpen = false
                coroutineScope.launch { offsetX.animateTo(if (snappedEdge == "left") -fabSizePx * 0.45f else maxWidthPx - fabSizePx * 0.55f, tween(300, easing = FastOutSlowInEasing)) }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isPortrait) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GraphPanel(Modifier.fillMaxWidth().height(420.dp), theme, displayStages, selectedStageId, { selectedStageId = it }, { onNavigateToScreen(getDedicatedRouteForStage(it)) }, heatmapTick, true, stageAlertSummary, opanTimers, pmTimers, activeBatchNo)
                    Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProjectedKpiPanel(Modifier.weight(1f).fillMaxHeight(), theme, displayStages, activeStage, globalOee)
                        EnergyPanel(Modifier.weight(1.5f).fillMaxHeight(), theme, activeStage, globalEnergy.toFloat(), globalThroughput.toFloat(), energyHistory, throughputHistory)
                    }
                    SuggestionsPanel(Modifier.fillMaxWidth().height(280.dp), theme, activeStage) { insightPopup = it }
                    AlertsPanel(Modifier.fillMaxWidth().height(380.dp), theme, activeAlerts, onNavigateToScreen)
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                Row(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(modifier = Modifier.weight(2.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GraphPanel(Modifier.fillMaxWidth().weight(2.5f), theme, displayStages, selectedStageId, { selectedStageId = it }, { onNavigateToScreen(getDedicatedRouteForStage(it)) }, heatmapTick, false, stageAlertSummary, opanTimers, pmTimers, activeBatchNo)
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProjectedKpiPanel(Modifier.weight(1f).fillMaxHeight(), theme, displayStages, activeStage, globalOee)
                            SuggestionsPanel(Modifier.weight(2.2f).fillMaxHeight(), theme, activeStage) { insightPopup = it }
                        }
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnergyPanel(Modifier.fillMaxWidth().weight(0.7f), theme, activeStage, globalEnergy.toFloat(), globalThroughput.toFloat(), energyHistory, throughputHistory)
                        AlertsPanel(Modifier.fillMaxWidth().weight(2.3f), theme, activeAlerts, onNavigateToScreen)
                    }
                }
            }
        }

        insightPopup?.let { InsightGlassDialog(it, theme) { insightPopup = null } }

        if (isAyamAssistOpen) {
            Box(modifier = Modifier.fillMaxSize().zIndex(40f).background(Color.Black.copy(alpha = 0.25f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { handleBotClick() }) {
                val safeTopPadding = with(density) { offsetY.value.toDp() }.coerceIn(16.dp, (with(density) { maxHeightPx.toDp() } - 380.dp).coerceAtLeast(16.dp))
                Box(modifier = Modifier.align(if (snappedEdge == "left") Alignment.TopStart else Alignment.TopEnd).padding(top = safeTopPadding, start = if (snappedEdge == "left") 115.dp else 16.dp, end = if (snappedEdge == "left") 16.dp else 115.dp)) {
                    AyamAssistantPopup(modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}, theme = theme, activeAlerts = activeAlerts, suggestions = stages.flatMap { it.recommendations }, ayamMood = ayamMood, onNavigateToScreen = onNavigateToScreen, onClose = { handleBotClick() })
                }
            }
        }

        if (isInitialized) {
            Box(modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }.wrapContentSize().zIndex(50f).graphicsLayer(alpha = animAlpha, clip = false).pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true; if (isAyamAssistOpen) isAyamAssistOpen = false },
                    onDragEnd = {
                        isDragging = false
                        snappedEdge = if (offsetX.value < (maxWidthPx / 2f - fabSizePx / 2f)) "left" else "right"
                        coroutineScope.launch { offsetX.animateTo(if (snappedEdge == "left") -fabSizePx * 0.45f else maxWidthPx - fabSizePx * 0.55f, tween(400, easing = FastOutSlowInEasing)) }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + dragAmount.x).coerceIn(-fabSizePx * 0.5f, maxWidthPx - fabSizePx * 0.5f))
                            offsetY.snapTo((offsetY.value + dragAmount.y).coerceIn(0f, maxHeightPx - fabSizePx))
                        }
                    }
                )
            }) {
                AyamBotWithBubble(theme, ayamMood, snappedEdge, animRotation, !isDragging && !isAyamAssistOpen) { handleBotClick() }
            }
        }

        RadialAppBar(modifier = Modifier.align(Alignment.CenterStart).zIndex(30f), activeSection = AppDestinations.WORKFLOW_DASHBOARD, onActionSelected = { onNavigateToScreen(it) })
    }
}

@Composable
fun GraphPanel(modifier: Modifier, theme: DashboardTheme, displayStages: List<LiveStageData>, selectedStageId: String?, onStageSelected: (String?) -> Unit, onNavigateToStage: (String) -> Unit, heatmapTick: Int, isPortrait: Boolean, alertSummary: Map<String, StageAlertSummary> = emptyMap(), opanTimers: List<OpanTimerSnapshot>, pmTimers: List<PmTimerSnapshot>, activeBatchNo: Int) {
    val actualPath = remember { Path() }
    val targetPath = remember { Path() }
    val aiPath = remember { Path() }

    CleanPanel(theme, modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight
            val stagesForLine = if (displayStages.isNotEmpty()) displayStages.dropLast(1) else emptyList()
            val maxValue = stagesForLine.maxOfOrNull { it.actualFlow } ?: 100f
            val graphMaxBound = if (maxValue > 0f) maxValue * 1.25f else 100f

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (displayStages.isEmpty()) return@Canvas
                val w = size.width; val totalH = size.height; val colW = w / displayStages.size
                val graphRenderH = totalH * 0.50f
                fun calcY(v: Float) = (graphRenderH - ((v / graphMaxBound) * graphRenderH)).coerceIn(0f, graphRenderH)

                actualPath.reset()
                targetPath.reset()
                aiPath.reset()

                for (tick in 0..4) {
                    val y = calcY((graphMaxBound / 4f) * tick)
                    drawLine(theme.textMuted.copy(alpha = 0.08f), Offset(0f, y), Offset(w, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)))
                }

                displayStages.forEachIndexed { i, stage ->
                    val colLeft = i * colW
                    drawRect(stage.color.copy(alpha = if (theme.isDark) 0.10f else 0.05f), Offset(colLeft, 0f), Size(colW, totalH))
                    if (i > 0) drawLine(theme.textMuted.copy(alpha = 0.15f), Offset(colLeft, 0f), Offset(colLeft, totalH), 0.5.dp.toPx())
                }

                if (stagesForLine.size >= 2) {
                    stagesForLine.forEachIndexed { index, stage ->
                        val x = (index * colW) + (colW / 2f)
                        val yActual = calcY(stage.actualFlow)
                        val yTarget = calcY(stage.actualFlow * 1.15f)
                        val yAi = calcY(stage.actualFlow * 0.92f)

                        if (index == 0) {
                            actualPath.moveTo(x, yActual)
                            targetPath.moveTo(x, yTarget)
                            aiPath.moveTo(x, yAi)
                        } else {
                            val prevX = ((index - 1) * colW) + (colW / 2f)
                            val prevActual = calcY(stagesForLine[index - 1].actualFlow)
                            val prevTarget = calcY(stagesForLine[index - 1].actualFlow * 1.15f)
                            val prevAi = calcY(stagesForLine[index - 1].actualFlow * 0.92f)
                            val midX = (prevX + x) / 2f

                            actualPath.cubicTo(midX, prevActual, midX, yActual, x, yActual)
                            targetPath.cubicTo(midX, prevTarget, midX, yTarget, x, yTarget)
                            aiPath.cubicTo(midX, prevAi, midX, yAi, x, yAi)
                        }
                    }
                }

                val fillPath = Path().apply {
                    addPath(actualPath)
                    if (stagesForLine.isNotEmpty()) {
                        lineTo(((stagesForLine.size - 1) * colW) + (colW / 2f), totalH)
                        lineTo(colW / 2f, totalH)
                        close()
                    }
                }
                drawPath(fillPath, Brush.verticalGradient(listOf(AccentPrimary.copy(alpha = 0.18f), AccentPrimary.copy(alpha = 0.02f))))

                drawPath(targetPath, BrandSteelGray.copy(alpha = 0.6f), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))))
                drawPath(aiPath, AccentAI.copy(alpha = 0.8f), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 10f))))
                drawPath(actualPath, AccentPrimary.copy(alpha = 0.12f), style = Stroke(7.dp.toPx()))
                drawPath(actualPath, AccentPrimary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                displayStages.forEachIndexed { i, stage ->
                    val nx = (i * colW) + (colW / 2f)
                    if (i < stagesForLine.size) {
                        val flowY = calcY(stage.actualFlow)
                        drawCircle(BrandOffWhite, 6.dp.toPx(), Offset(nx, flowY))
                        drawCircle(stage.color, 4.dp.toPx(), Offset(nx, flowY))
                    } else {
                        drawBatchGrid(stage, colW, i, totalH, heatmapTick, theme, isPortrait, opanTimers, pmTimers, activeBatchNo)
                    }
                }

                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply { color = theme.textMuted.toArgb(); textSize = 22f; isFakeBoldText = true }
                    drawText("— Actual    - - Target    ··· AI Predict", 20f, 40f, paint)
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1.3f)) {
                    val graphRenderH = maxHeight
                    if (displayStages.isNotEmpty()) {
                        val colWdp = canvasWidth / displayStages.size
                        displayStages.forEachIndexed { i, stage ->
                            if (i < stagesForLine.size) {
                                val nx = (colWdp * i) + (colWdp / 2)
                                val ay = graphRenderH.value - ((stage.actualFlow / graphMaxBound) * graphRenderH.value)
                                Box(modifier = Modifier.offset(x = nx - 50.dp, y = ay.dp - 45.dp).width(100.dp).clickable { onNavigateToStage(stage.id) }) {
                                    StageFlowGlassBox(stage, stage.actualFlow, theme)
                                }
                            } else {
                                Box(modifier = Modifier.offset(x = colWdp * i, y = 0.dp).width(colWdp).fillMaxHeight().clickable { onNavigateToStage(stage.id) })
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    displayStages.forEach { stage ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(stage.name.uppercase(), color = theme.textMain, fontSize = if (isPortrait) 12.sp else 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    displayStages.forEach { stage ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            val card = remember(stage, opanTimers, pmTimers, activeBatchNo) {
                                if (stage.id == "05") {
                                    val opMax = opanTimers.maxOfOrNull { it.elapsedMinutes } ?: 0.0
                                    val pmActive = pmTimers.firstOrNull { it.active } ?: pmTimers.firstOrNull()
                                    StageCardDisplay("SYRUP FLOW", String.format("%,.0f", stage.actualFlow), "L/hr", "OPAN", if (opMax > 0) "${opMax.toInt()}m" else "--", "PM", pmActive?.let { if (it.active) "${it.phase.take(6)} ${it.elapsedMinutes.toInt()}m" else "IDLE" } ?: "IDLE", "BATCH", "$activeBatchNo", stage.efficiency, 89.3f, 92)
                                } else stageCardDisplay(stage)
                            }
                            StageKpiCard(stage, card, selectedStageId == stage.id, isPortrait, theme, alertSummary[stage.name.uppercase()]) {
                                onStageSelected(if (selectedStageId == stage.id) null else stage.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StageFlowGlassBox(stage: LiveStageData, flowValue: Float, theme: DashboardTheme) {
    val cardShape = RoundedCornerShape(10.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentSize()) {
        Box(modifier = Modifier.shadow(6.dp, cardShape).clip(cardShape).background(Color.White.copy(alpha = 0.85f)).border(1.dp, Color.White, cardShape).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Text(String.format("%,.0f", flowValue), color = BrandDeepNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Box(modifier = Modifier.offset(y = (-1).dp).size(5.dp).clip(CircleShape).background(stage.color))
    }
}

private fun DrawScope.drawBatchGrid(stage: LiveStageData, colW: Float, i: Int, graphH: Float, heatmapTick: Int, theme: DashboardTheme, isPortrait: Boolean, opanTimers: List<OpanTimerSnapshot>, pmTimers: List<PmTimerSnapshot>, activeBatchNo: Int) {
    val colLeft = i * colW; val scaleDown = if (isPortrait) 0.7f else 1f
    val cellW = (38.dp * scaleDown).toPx(); val cellH = (26.dp * scaleDown).toPx(); val spacing = (8.dp * scaleDown).toPx()
    val batchBoxW = (38.dp * scaleDown).toPx(); val batchSpc = (16.dp * scaleDown).toPx(); val rows = 4; val cols = 2
    val totalGridW = (cols * cellW) + ((cols - 1) * spacing) + batchSpc + batchBoxW
    val gridStartX = colLeft + (colW - totalGridW) / 2
    val batchStartX = gridStartX + (cols * cellW) + ((cols - 1) * spacing) + batchSpc

    val startY = (36.dp * scaleDown).toPx().coerceAtLeast(30f)

    val hdrPaint = Paint().apply { color = stage.color.toArgb(); textSize = (11.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val textPaint = Paint().apply { color = BrandOffWhite.toArgb(); textSize = (12.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val subPaint = Paint().apply { color = BrandOffWhite.copy(alpha = 0.92f).toArgb(); textSize = (10.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val batchPaint = Paint().apply { color = stage.color.toArgb(); textSize = (12.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }

    drawContext.canvas.nativeCanvas.apply {
        drawText("PAN", gridStartX + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        drawText("PWDR", gridStartX + cellW + spacing + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        drawText("BATCH", batchStartX + batchBoxW / 2, startY - 10.dp.toPx(), hdrPaint)
    }

    val emptyColor = if (theme.isDark) BrandDarkBlueGray.copy(alpha = 0.35f) else BrandLightGray.copy(alpha = 0.45f)
    val pulse = 0.75f + 0.25f * abs(sin(heatmapTick / 2f))

    var lastRowY = startY
    var completedBatchesCount = 0

    for (r in 0 until rows) {
        val cy = startY + r * (cellH + spacing)
        lastRowY = cy

        val op = opanTimers.getOrNull(r); val pm = pmTimers.getOrNull(r)
        val isOpCompleted = (op?.elapsedMinutes ?: 0.0) >= 30.0
        val isPmCompleted = (pm?.elapsedMinutes ?: 0.0) >= 30.0

        if(isOpCompleted && isPmCompleted) completedBatchesCount++
        val displayBatchNo = activeBatchNo + r

        if (op?.running == true) drawRoundRect(stage.color.copy(alpha = 0.18f * pulse), Offset(gridStartX - 2.dp.toPx(), cy - 2.dp.toPx()), Size(cellW + 4.dp.toPx(), cellH + 4.dp.toPx()), CornerRadius(6.dp.toPx()))
        drawRoundRect(if (op?.running == true || isOpCompleted) stage.color.copy(alpha = if(isOpCompleted) 0.8f else 0.55f) else emptyColor.copy(alpha = 0.20f), Offset(gridStartX, cy), Size(cellW, cellH), CornerRadius(4.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText(if (op?.running == true || isOpCompleted) "${op?.elapsedMinutes?.toInt() ?: 0}m" else "--", gridStartX + cellW / 2, cy + cellH / 2 - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)

        val pmX = gridStartX + cellW + spacing
        if (pm?.active == true) drawRoundRect(stage.color.copy(alpha = 0.18f * pulse), Offset(pmX - 2.dp.toPx(), cy - 2.dp.toPx()), Size(cellW + 4.dp.toPx(), cellH + 4.dp.toPx()), CornerRadius(6.dp.toPx()))
        drawRoundRect(if (pm?.active == true || isPmCompleted) stage.color.copy(alpha = if(isPmCompleted) 0.8f else 0.55f) else emptyColor.copy(alpha = 0.20f), Offset(pmX, cy), Size(cellW, cellH), CornerRadius(4.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText(if (pm?.active == true || isPmCompleted) pm?.phase?.take(6) ?: "DONE" else "IDLE", pmX + cellW / 2, cy + cellH * 0.42f - (subPaint.ascent() + subPaint.descent()) / 2, subPaint)
        drawContext.canvas.nativeCanvas.drawText(if (pm?.active == true || isPmCompleted) "${pm?.elapsedMinutes?.toInt() ?: 0}m" else "--", pmX + cellW / 2, cy + cellH * 0.78f - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)

        drawRoundRect(stage.color.copy(alpha = 0.12f), Offset(batchStartX, cy), Size(batchBoxW, cellH), CornerRadius(6.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText("$displayBatchNo", batchStartX + batchBoxW / 2, cy + cellH / 2 - (batchPaint.ascent() + batchPaint.descent()) / 2, batchPaint)
    }

    val totalBoxY = lastRowY + cellH + spacing
    val totalBoxH = (38.dp * scaleDown).toPx()

    drawRoundRect(
        color = stage.color.copy(alpha = 0.15f),
        topLeft = Offset(gridStartX, totalBoxY),
        size = Size(totalGridW, totalBoxH),
        cornerRadius = CornerRadius(6.dp.toPx())
    )

    val totalBoxTextPaint = Paint().apply {
        color = theme.textMain.toArgb()
        textSize = (10.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    val totalBoxValPaint = Paint().apply {
        color = stage.color.toArgb()
        textSize = (11.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    drawContext.canvas.nativeCanvas.drawText(
        "BATCHES DONE: $completedBatchesCount",
        gridStartX + totalGridW / 2,
        totalBoxY + totalBoxH * 0.35f - (totalBoxTextPaint.ascent() + totalBoxTextPaint.descent()) / 2,
        totalBoxTextPaint
    )

    drawContext.canvas.nativeCanvas.drawText(
        "TOTAL: 1,450 TCD",
        gridStartX + totalGridW / 2,
        totalBoxY + totalBoxH * 0.75f - (totalBoxValPaint.ascent() + totalBoxValPaint.descent()) / 2,
        totalBoxValPaint
    )
}

@Composable
private fun StageKpiCard(stage: LiveStageData, cardDisplay: StageCardDisplay, isSelected: Boolean, isPortrait: Boolean, theme: DashboardTheme, alertSummary: StageAlertSummary?, onClick: () -> Unit) {
    val tankPct = stage.tankFillPercent.coerceIn(0, 100)
    val bgModifier = if (isSelected) stage.color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.50f)

    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(bgModifier).border(if (isSelected) 2.dp else 0.5.dp, if (isSelected) stage.color else BrandLightGray.copy(alpha = 0.15f), RoundedCornerShape(14.dp)).clickable { onClick() }) {
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(vertical = 4.dp).width(4.dp).fillMaxHeight((tankPct / 100f).coerceIn(0f, 1f)).clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)).background((if (tankPct >= 90) AccentCritical else stage.color).copy(alpha = 0.7f)))
        Column(modifier = Modifier.fillMaxSize().padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(cardDisplay.primaryValue, color = theme.textMain, fontSize = if (isPortrait) 16.sp else 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(cardDisplay.primaryUnit, color = stage.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            EquipmentHeatmap(stage, alertSummary)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${cardDisplay.stat1Label}: ${cardDisplay.stat1Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${cardDisplay.stat2Label}: ${cardDisplay.stat2Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${cardDisplay.stat3Label}: ${cardDisplay.stat3Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun EquipmentHeatmap(stage: LiveStageData, alertSummary: StageAlertSummary?) {
    val c1 = if ((alertSummary?.critical ?: 0) > 0) AccentCritical else AccentSuccess
    val c2 = if ((alertSummary?.warning ?: 0) > 0) AccentWarning else AccentSuccess
    val c3 = if (stage.efficiency < 85) AccentWarning else AccentSuccess
    val c4 = AccentSuccess

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("EQ HEALTH", color = BrandSteelGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(c1))
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(c2))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(c3))
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(c4))
            }
        }
    }
}

@Composable
fun ForecastDetailsGlassDialog(data: List<Float>, globalOee: Int, avgEfficiency: Int, theme: DashboardTheme, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }, contentAlignment = Alignment.Center) {
            val dialogShape = RoundedCornerShape(24.dp)
            Box(modifier = Modifier.widthIn(max = 460.dp).padding(16.dp).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}) {
                Box(modifier = Modifier.matchParentSize().clip(dialogShape).background(Color.White.copy(alpha = 0.55f)).blur(80.dp))
                Box(modifier = Modifier.matchParentSize().clip(dialogShape).background(BrandOffWhite.copy(alpha = 0.45f)).blur(60.dp))
                Box(modifier = Modifier.shadow(24.dp, dialogShape, spotColor = Color.Black.copy(alpha = 0.18f)).clip(dialogShape).background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.94f), BrandOffWhite.copy(alpha = 0.88f), Color.White.copy(alpha = 0.92f)))).border(1.5.dp, Brush.linearGradient(listOf(Color.White, AccentAI.copy(alpha = 0.25f), Color.White.copy(alpha = 0.80f))), dialogShape)) {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentAI))
                                    Text("AI FORECAST ANALYSIS", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("24-Hour Cycle Breakdown", color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Close, null, tint = BrandSteelGray, modifier = Modifier.size(18.dp)) }
                        }
                        val avgVal = data.average().toFloat()
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(AccentAI.copy(alpha = 0.08f), AccentAI.copy(alpha = 0.03f)))).border(0.5.dp, AccentAI.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Projected Avg", color = BrandSteelGray, fontSize = 11.sp); Text("${avgVal.toInt()} TCD", color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Black) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Peak", color = BrandSteelGray, fontSize = 11.sp); Text("${(data.maxOrNull() ?: avgVal).toInt()}", color = AccentSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Low", color = BrandSteelGray, fontSize = 11.sp); Text("${(data.minOrNull() ?: avgVal).toInt()}", color = AccentWarning, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        }
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.6f)).border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("Performance Scores", color = BrandDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("Rolling 24hr window", color = BrandSteelGray, fontSize = 11.sp) }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { CircularScoreIndicator("OEE", globalOee, theme); CircularScoreIndicator("EFF", avgEfficiency, theme) }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp)) { ForecastAreaChart(data, AccentAI, BrandSteelGray) }
                        Text("Sub-Stage Forecasts", color = BrandDeepNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SubForecastRow("01", "Milling", "5,800 kg/hr", "OEE Target: 88%", AccentSuccess)
                            SubForecastRow("02", "Juice Treatment", "94% Eff", "pH expected stable", AccentAI)
                            SubForecastRow("03", "Evaporation", "58° Bx", "Slight variance expected", AccentWarning)
                            SubForecastRow("04", "Vacuum Pans", "90% Uptime", "Cycle time optimal", AccentSuccess)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("Close Details", color = BrandOffWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(AccentAI, AccentAI.copy(alpha = 0.8f)))).clickable { onDismiss() }.padding(horizontal = 20.dp, vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubForecastRow(id: String, stage: String, metric: String, detail: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.6f)).border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Analytics, null, tint = color, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) { Text(stage, color = BrandDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(detail, color = BrandSteelGray, fontSize = 11.sp) }
        Text(metric, color = BrandDeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun InsightGlassDialog(insight: SystemInsight, theme: DashboardTheme, onDismiss: () -> Unit) {
    val severityColor = when (insight.severity) { InsightSeverity.CRITICAL -> AccentCritical; InsightSeverity.WARNING -> AccentWarning; InsightSeverity.INFO -> AccentPrimary }
    val categoryColor = when (insight.category) { InsightCategory.EFFICIENCY -> AccentSuccess; InsightCategory.ENERGY -> AccentWarning; InsightCategory.QUALITY -> AccentPrimary; InsightCategory.MAINTENANCE -> AccentCritical; InsightCategory.PROCESS -> AccentAI }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }, contentAlignment = Alignment.Center) {
            val dialogShape = RoundedCornerShape(24.dp)
            Box(modifier = Modifier.widthIn(max = 460.dp).padding(16.dp).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}) {
                Box(modifier = Modifier.matchParentSize().clip(dialogShape).background(Color.White.copy(alpha = 0.55f)).blur(80.dp))
                Box(modifier = Modifier.matchParentSize().clip(dialogShape).background(BrandOffWhite.copy(alpha = 0.45f)).blur(60.dp))
                Box(modifier = Modifier.shadow(24.dp, dialogShape, spotColor = Color.Black.copy(alpha = 0.18f)).clip(dialogShape).background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.94f), BrandOffWhite.copy(alpha = 0.88f), Color.White.copy(alpha = 0.92f)))).border(1.5.dp, Brush.linearGradient(listOf(Color.White, severityColor.copy(alpha = 0.25f), Color.White.copy(alpha = 0.80f))), dialogShape)) {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(severityColor))
                                    Text(insight.stage, color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text(insight.category.name, color = categoryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(categoryColor.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                                    Text(insight.severity.name, color = severityColor, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(severityColor.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(insight.title, color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Close, null, tint = BrandSteelGray, modifier = Modifier.size(18.dp)) }
                        }
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(severityColor.copy(alpha = 0.08f), severityColor.copy(alpha = 0.03f)))).border(0.5.dp, severityColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text(insight.metric, color = BrandSteelGray, fontSize = 12.sp); Text(insight.metricValue, color = BrandDeepNavy, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                            val trendColor = when (insight.trend) { InsightTrend.UP -> AccentCritical; InsightTrend.DOWN -> AccentWarning; InsightTrend.STABLE -> AccentSuccess }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(when (insight.trend) { InsightTrend.UP -> "▲"; InsightTrend.DOWN -> "▼"; InsightTrend.STABLE -> "●" }, color = trendColor, fontSize = 14.sp)
                                Text(when (insight.trend) { InsightTrend.UP -> "Increasing"; InsightTrend.DOWN -> "Decreasing"; InsightTrend.STABLE -> "Stable" }, color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(insight.id, color = BrandSteelGray, fontSize = 11.sp)
                        }
                        GlassSection("⚠️", "Problem Detected", AccentCritical, insight.problem, AccentCritical.copy(alpha = 0.05f), AccentCritical.copy(alpha = 0.12f))
                        GlassSection("🔍", "Root Cause Analysis", AccentWarning, insight.rootCause, AccentWarning.copy(alpha = 0.05f), AccentWarning.copy(alpha = 0.12f))
                        GlassSection("📉", "Operational Impact", AccentAI, insight.impact, AccentAI.copy(alpha = 0.05f), AccentAI.copy(alpha = 0.12f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("Acknowledge", color = BrandOffWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(severityColor, severityColor.copy(alpha = 0.8f)))).clickable { onDismiss() }.padding(horizontal = 20.dp, vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSection(emoji: String, label: String, labelColor: Color, body: String, bgColor: Color, borderColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bgColor).border(0.5.dp, borderColor, RoundedCornerShape(14.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emoji, fontSize = 14.sp)
            Text(label, color = labelColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(body, color = BrandDeepNavy, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun ProjectedKpiPanel(modifier: Modifier, theme: DashboardTheme, displayStages: List<LiveStageData>, activeStage: LiveStageData?, globalOee: Int = 0) {
    val displayProjection = activeStage?.aiProjection ?: if (displayStages.isNotEmpty()) displayStages.map { it.aiProjection }.average().toFloat() else 85f
    val animProjection by animateFloatAsState(displayProjection, tween(800), label = "proj")
    val animOee by animateFloatAsState(globalOee.toFloat().coerceAtLeast(1f), tween(800), label = "oee")
    var showForecastDetails by remember { mutableStateOf(false) }

    val forecastCurve = remember(displayProjection) {
        val base = displayProjection.coerceAtLeast(20f)
        List(24) { i -> base + (sin((i / 24f) * Math.PI * 2).toFloat() * (base * 0.12f)) + ((-3..3).random() / 100f * base) }
    }
    if (showForecastDetails) {
        ForecastDetailsGlassDialog(forecastCurve, globalOee, activeStage?.efficiency ?: if (displayStages.isNotEmpty()) displayStages.map { it.efficiency }.average().toInt() else 75, theme) { showForecastDetails = false }
    }
    CleanPanel(theme = theme, modifier = modifier.clickable { showForecastDetails = true }) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(if (activeStage != null) "${activeStage.name.uppercase()} FORECAST" else "PLANT FORECAST", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(String.format("%,d", animProjection.toInt()), color = AccentAI, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("TCD", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 5.dp))
                    }
                }
                CircularScoreIndicator("OEE", animOee.toInt(), theme)
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) { ForecastAreaChart(forecastCurve, AccentAI, theme.textMuted) }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("AI forecast · 24hr cycle", color = theme.textLightMuted, fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Outlined.Timeline, null, tint = theme.textLightMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("(Tap for details)", color = theme.textLightMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun CircularScoreIndicator(label: String, score: Int, theme: DashboardTheme) {
    val clampedScore = score.coerceIn(0, 100)
    val animScore by animateFloatAsState(clampedScore / 100f, tween(1000), label = "score")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(BrandLightGray.copy(alpha = 0.2f), 0f, 360f, false, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
                drawArc(when { clampedScore >= 80 -> AccentSuccess; clampedScore >= 60 -> AccentWarning; else -> AccentCritical }, -90f, 360f * animScore, false, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("$clampedScore%", color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ForecastAreaChart(data: List<Float>, color: Color, textColor: Color) {
    val path = remember { Path() }; val fillPath = remember { Path() }
    var isVisible by remember { mutableStateOf(false) }; LaunchedEffect(Unit) { isVisible = true }
    val entryProgress by animateFloatAsState(if (isVisible) 1f else 0f, tween(1000, easing = FastOutSlowInEasing), label = "entry")
    val dotPulseProgress by rememberInfiniteTransition(label = "pulse").animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing)), label = "dot")

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.size < 2 || size.width <= 0f || size.height <= 0f) return@Canvas
        val padding = 4.dp.toPx(); val drawH = size.height - padding * 2 - 18.dp.toPx(); val drawW = size.width - padding * 2
        val minV = (data.minOrNull() ?: 0f) * 0.85f; val maxV = (data.maxOrNull() ?: 1f) * 1.15f; val range = (maxV - minV).coerceAtLeast(0.01f)
        val stepX = drawW / (data.size - 1)
        path.reset(); fillPath.reset()

        data.forEachIndexed { i, v ->
            val x = padding + i * stepX
            val rawY = padding + drawH - ((v - minV) / range) * drawH
            val y = padding + drawH + (rawY - (padding + drawH)) * entryProgress
            if (i == 0) path.moveTo(x, y) else { val prevX = padding + (i - 1) * stepX; val prevRawY = padding + drawH - ((data[i - 1] - minV) / range) * drawH; path.cubicTo((prevX + x) / 2f, padding + drawH + (prevRawY - (padding + drawH)) * entryProgress, (prevX + x) / 2f, y, x, y) }
        }
        fillPath.addPath(path); fillPath.lineTo(padding + drawW, padding + drawH); fillPath.lineTo(padding, padding + drawH); fillPath.close()
        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), Color.Transparent)))
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val lastX = padding + (data.size - 1) * stepX; val lastY = padding + drawH - ((data.last() - minV) / range) * drawH + (padding + drawH - (padding + drawH)) * entryProgress
        drawCircle(color.copy(alpha = 0.6f * (1f - dotPulseProgress)), radius = 4.dp.toPx() + (14.dp.toPx() * dotPulseProgress), center = Offset(lastX, lastY))
        drawCircle(Color.White, 4.dp.toPx(), Offset(lastX, lastY)); drawCircle(color, 2.5.dp.toPx(), Offset(lastX, lastY))
        drawLine(color.copy(alpha = 0.15f), Offset(padding, padding + drawH), Offset(padding + drawW, padding + drawH), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEachIndexed { idx, label ->
            drawContext.canvas.nativeCanvas.drawText(label, padding + (idx / 4f) * drawW, size.height - 2.dp.toPx(), Paint().apply { this.color = textColor.toArgb(); this.textSize = 10.dp.toPx(); this.textAlign = Paint.Align.CENTER; this.isAntiAlias = true })
        }
    }
}

@Composable
fun EnergyPanel(modifier: Modifier, theme: DashboardTheme, activeStage: LiveStageData?, globalEnergy: Float, globalThroughput: Float, energyHistory: List<Float>, throughputHistory: List<Float>) {
    val cardShape = RoundedCornerShape(18.dp)
    CleanPanel(theme, modifier = modifier) {
        Row(modifier = Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThroughputCard(Modifier.weight(1f).fillMaxHeight(), activeStage?.actualFlow ?: globalThroughput, if (activeStage != null) "kg/hr" else "TCD", throughputHistory, cardShape)
            EnergyCard(Modifier.weight(1f).fillMaxHeight(), activeStage?.energyKw ?: globalEnergy, energyHistory, cardShape)
        }
    }
}

@Composable
private fun ThroughputCard(modifier: Modifier, value: Float, unit: String, history: List<Float>, cardShape: RoundedCornerShape) {
    Column(modifier = modifier.clip(cardShape).background(Brush.linearGradient(listOf(AccentPrimary.copy(alpha = 0.14f), AccentPrimary.copy(alpha = 0.04f)))).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(18.dp).background(AccentPrimary.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Speed, null, tint = AccentPrimary, modifier = Modifier.size(11.dp)) }
            Text("Production", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(String.format("%,d", value.toInt()), color = BrandDeepNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(unit, color = BrandMutedBlue, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        ThroughputBarChart(history, AccentPrimary, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun ThroughputBarChart(history: List<Float>, barColor: Color, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(1f, tween(700), label = "bar")
    Canvas(modifier = modifier) {
        if (history.isEmpty()) return@Canvas
        val maxV = history.maxOf { it }.coerceAtLeast(1f); val barW = ((size.width - (history.size - 1) * 2.dp.toPx()) / history.size).coerceAtLeast(2.dp.toPx())
        history.forEachIndexed { i, v ->
            val barH = size.height * (v / maxV).coerceIn(0.05f, 1f) * animatedProgress
            drawRoundRect(barColor.copy(alpha = if (i == history.lastIndex) 0.9f else 0.3f + 0.4f * (i.toFloat() / history.size)), Offset(i * (barW + 2.dp.toPx()), size.height - barH), Size(barW, barH), CornerRadius(2.dp.toPx()))
        }
    }
}

@Composable
private fun EnergyCard(modifier: Modifier, value: Float, history: List<Float>, cardShape: RoundedCornerShape) {
    val avgEnergy = history.ifEmpty { listOf(value) }.average().toFloat(); val pct = if (avgEnergy != 0f) (value - avgEnergy) / avgEnergy * 100f else 0f
    Column(modifier = modifier.clip(cardShape).background(Brush.linearGradient(listOf(AccentWarning.copy(alpha = 0.12f), AccentWarning.copy(alpha = 0.03f)))).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(18.dp).background(AccentWarning.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ElectricBolt, null, tint = AccentWarning, modifier = Modifier.size(11.dp)) }
            Text("Energy", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            EnergyNeedleGauge(value, 0f, 80f, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp)) {
                Text(value.toInt().toString(), color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("kW", color = BrandSteelGray, fontSize = 11.sp)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${if (pct >= 0f) "▲" else "▼"} ${abs(pct).toInt()}%", color = if (pct >= 0f) AccentCritical else AccentSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("vs avg ${avgEnergy.toInt()}kW", color = BrandSteelGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EnergyNeedleGauge(value: Float, min: Float, max: Float, modifier: Modifier = Modifier) {
    val animatedNorm by animateFloatAsState(if (max - min == 0f) 0f else (value.coerceIn(min, max) - min) / (max - min), tween(900, easing = FastOutSlowInEasing), label = "gauge")
    Canvas(modifier = modifier) {
        val strokeW = 8.dp.toPx(); val diameter = size.width - 2 * (strokeW / 2 + 6.dp.toPx())
        val topLeft = Offset(strokeW / 2 + 6.dp.toPx(), (size.height - diameter / 2f) / 2f); val arcSize = Size(diameter, diameter)
        for (i in 0 until 20) {
            val segFrac = i.toFloat() / 20
            drawArc(when { segFrac < 0.4f -> AccentSuccess; segFrac < 0.7f -> AccentWarning; else -> AccentCritical }.copy(alpha = if (segFrac <= animatedNorm) 0.85f else 0.12f), 180f + i * 9f, 7.5f, false, topLeft, arcSize, style = Stroke(strokeW))
        }
        val rad = (180f + 180f * animatedNorm) * (PI.toFloat() / 180f)
        val cx = topLeft.x + diameter / 2f; val cy = topLeft.y + diameter / 2f; val length = diameter / 2f - strokeW
        drawLine(Color.Black.copy(alpha = 0.1f), Offset(cx + 1, cy + 1), Offset(cx + length * cos(rad), cy + length * sin(rad)), 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(BrandDeepNavy, Offset(cx, cy), Offset(cx + length * cos(rad), cy + length * sin(rad)), 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(BrandDeepNavy, 3.dp.toPx(), Offset(cx, cy)); drawCircle(AccentWarning, 2.dp.toPx(), Offset(cx, cy))
    }
}

@Composable
fun SuggestionsPanel(modifier: Modifier, theme: DashboardTheme, activeStage: LiveStageData?, onInsightClick: (SystemInsight) -> Unit) {
    val stageInsights = remember(activeStage) { if (activeStage != null) getInsightsForStage(activeStage.name) else getAllInsights().take(10) }
    CleanPanel(theme, modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (activeStage != null) "${activeStage.name} SUGGESTIONS" else "SUGGESTIONS", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("${stageInsights.size} active diagnostics · Powered by Ayam AI", color = theme.textLightMuted, fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val cr = stageInsights.count { it.severity == InsightSeverity.CRITICAL }; val wa = stageInsights.count { it.severity == InsightSeverity.WARNING }
                    if (cr > 0) Text("$cr CRIT", color = AccentCritical, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentCritical.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                    if (wa > 0) Text("$wa WARN", color = AccentWarning, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentWarning.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                    Icon(Icons.Outlined.Memory, null, tint = AccentAI, modifier = Modifier.size(18.dp))
                }
            }
            if (stageInsights.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No system insights available.", color = theme.textLightMuted, fontSize = 13.sp) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(stageInsights, key = { it.id }) { InsightListCard(it, theme) { onInsightClick(it) } }
                }
            }
        }
    }
}

@Composable
private fun InsightListCard(insight: SystemInsight, theme: DashboardTheme, onClick: () -> Unit) {
    val severityColor = when (insight.severity) { InsightSeverity.CRITICAL -> AccentCritical; InsightSeverity.WARNING -> AccentWarning; else -> AccentPrimary }
    val categoryColor = when (insight.category) { InsightCategory.EFFICIENCY -> AccentSuccess; InsightCategory.ENERGY -> AccentWarning; InsightCategory.QUALITY -> AccentPrimary; InsightCategory.MAINTENANCE -> AccentCritical; else -> AccentAI }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(theme.trackBg.copy(alpha = 0.20f)).border(0.5.dp, severityColor.copy(alpha = 0.18f), RoundedCornerShape(10.dp)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(32.dp).clip(RoundedCornerShape(2.dp)).background(severityColor))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(insight.title, color = theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(insight.category.name, color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(categoryColor.copy(alpha = 0.10f)).padding(horizontal = 3.dp, vertical = 1.dp))
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(insight.stage, color = theme.textLightMuted, fontSize = 11.sp)
                Text("·", color = theme.textLightMuted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(when (insight.trend) { InsightTrend.UP -> "▲"; InsightTrend.DOWN -> "▼"; else -> "●" }, color = when (insight.trend) { InsightTrend.UP -> AccentCritical; InsightTrend.DOWN -> AccentWarning; else -> AccentSuccess }, fontSize = 11.sp)
                    Text(insight.metricValue, color = severityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(insight.severity.name, color = severityColor, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(severityColor.copy(alpha = 0.12f)).padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

@Composable
fun AlertsPanel(modifier: Modifier, theme: DashboardTheme, activeAlerts: List<AlertData>, onNavigateToScreen: (String) -> Unit) {
    CleanPanel(theme, modifier = modifier) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Active Alerts Log", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Medium); Text("Synced via Digital Twin API", color = theme.textLightMuted, fontSize = 11.sp) }
                Text("${activeAlerts.count { !it.acknowledged }} Active", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            if (activeAlerts.isEmpty()) {
                Text("System clear. No active bottlenecks.", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(activeAlerts, key = { it.id }) { alert ->
                        val isAck = alert.acknowledged
                        val color = if (isAck) BrandSteelGray else when (alert.priority.uppercase()) { "CRITICAL" -> AccentCritical; "WARNING" -> AccentWarning; else -> AccentPrimary }
                        Row(modifier = Modifier.fillMaxWidth().background(if (isAck) Color.Transparent else theme.trackBg.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).border(0.5.dp, color.copy(alpha = if (isAck) 0.08f else 0.25f), RoundedCornerShape(10.dp)).clickable(enabled = alert.sourceRoute == "energy_tab" || alert.sourceRoute == "production_tab") {
                            val dest = when (alert.sourceRoute) {
                                "energy_tab" -> AppDestinations.ENERGY_TAB
                                "production_tab" -> AppDestinations.PRODUCTION_TAB
                                else -> null
                            }
                            dest?.let { onNavigateToScreen("$it?section=${alert.targetSection ?: alert.stage}&alertId=${alert.targetAlertId ?: alert.id}") }
                        }.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isAck) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = color, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(alert.message, color = if (isAck) theme.textMuted else theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${alert.stage} • ${alert.priority}", color = color, fontSize = 11.sp)
                            }
                            if (isAck) Text("ACK", color = BrandSteelGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyamAssistantFab(modifier: Modifier = Modifier, mood: AyamMood, onClick: (() -> Unit)? = null) {
    val pulseColor = when (mood) { AyamMood.NORMAL -> AccentSuccess; AyamMood.WARNING -> AccentWarning; AyamMood.CRITICAL -> AccentCritical }
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val pulseAlpha by infiniteTransition.animateFloat(0.4f, 0f, infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "alpha")
    val pulseScale by infiniteTransition.animateFloat(1f, 1.5f, infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "scale")

    Box(modifier = if (onClick != null) modifier.clickable { onClick() } else modifier, contentAlignment = Alignment.Center) {
        if (mood != AyamMood.NORMAL) Box(modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha).border(2.dp, pulseColor, CircleShape))
        Image(painter = painterResource(id = R.drawable.ayam_image), contentDescription = "Ayam Assistant", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}

@Composable
fun AyamHelloBubble(modifier: Modifier = Modifier, theme: DashboardTheme, mood: AyamMood, onSpeakNow: (String) -> Unit = {}) {
    val mainText = when (mood) { AyamMood.NORMAL -> "All systems nominal"; AyamMood.WARNING -> "Warnings detected"; AyamMood.CRITICAL -> "Critical attention needed" }
    val accentColor = when (mood) { AyamMood.NORMAL -> AccentSuccess; AyamMood.WARNING -> AccentWarning; AyamMood.CRITICAL -> AccentCritical }
    val scale by animateFloatAsState(if (mood == AyamMood.NORMAL) 1f else 1.04f, tween(350), label = "bubbleScale")

    Box(modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale).shadow(8.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.94f), Color.White.copy(alpha = 0.70f)))).border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.95f), accentColor.copy(alpha = 0.4f))), RoundedCornerShape(14.dp)).clickable { onSpeakNow(mainText) }.padding(horizontal = 10.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
            Text(text = mainText, color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Clip)
        }
    }
}

@Composable
private fun AyamBotWithBubble(theme: DashboardTheme, mood: AyamMood, snappedEdge: String, rotationZ: Float, showBubble: Boolean, onToggle: () -> Unit) {
    Layout(content = {
        AyamAssistantFab(modifier = Modifier.size(110.dp).graphicsLayer(rotationZ = rotationZ), mood = mood, onClick = onToggle)
        if (showBubble) AyamHelloBubble(theme = theme, mood = mood, onSpeakNow = { onToggle() })
    }) { measurables, constraints ->
        val botSizePx = 110.dp.roundToPx(); val botPlaceable = measurables[0].measure(Constraints.fixed(botSizePx, botSizePx))
        val bubblePlaceable = if (showBubble && measurables.size > 1) measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0)) else null
        layout(botPlaceable.width, botPlaceable.height) {
            botPlaceable.placeRelative(0, 0)
            bubblePlaceable?.placeRelative(if (snappedEdge == "left") botPlaceable.width + 12.dp.roundToPx() else -bubblePlaceable.width - 12.dp.roundToPx(), (botPlaceable.height - bubblePlaceable.height) / 2)
        }
    }
}

@Composable
fun AyamAssistantPopup(modifier: Modifier = Modifier, theme: DashboardTheme, activeAlerts: List<AlertData>, suggestions: List<RecommendationData>, ayamMood: AyamMood, onNavigateToScreen: (String) -> Unit, onClose: () -> Unit) {
    val frequentStages = remember(activeAlerts) { activeAlerts.groupBy { it.stage }.filter { (_, al) -> al.size >= 2 }.keys }
    val topSuggestions = remember(suggestions) { suggestions.take(5) }
    val hasCritical = activeAlerts.any { !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true) }
    val hasWarning = activeAlerts.any { !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true) }
    val expressionColor = when { hasCritical -> AccentCritical; hasWarning -> AccentWarning; else -> AccentSuccess }

    var recognizedText by remember { mutableStateOf<String?>(null) }
    var ayamReply by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        CleanPanel(theme = theme, cornerRadius = 22.dp, modifier = Modifier.padding(start = 16.dp, top = 16.dp).widthIn(max = 380.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.matchParentSize().blur(18.dp).background(Color.White.copy(alpha = 0.72f)))
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(54.dp))
                            Column {
                                Text("Ayam Assistant", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(when { hasCritical -> "Concerned"; hasWarning -> "Alert"; else -> "Calm" }, color = expressionColor, fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, null, tint = theme.textMuted) }
                    }
                    Column(modifier = Modifier.fillMaxWidth().background(expressionColor.copy(alpha = 0.05f), RoundedCornerShape(10.dp)).padding(10.dp)) {
                        Text("Status", color = expressionColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(when { hasCritical -> "Critical issues require immediate attention."; hasWarning -> "Some warnings need your review."; else -> "Plant operating within normal parameters." }, color = theme.textMain, fontSize = 12.sp)
                    }
                    if (frequentStages.isNotEmpty()) {
                        Text("Recurring alerts", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        frequentStages.forEach { st -> Text("• $st – tap to drill into Energy view", color = theme.textMain, fontSize = 11.sp, modifier = Modifier.clickable { onNavigateToScreen("${AppDestinations.ENERGY_TAB}?section=$st") }.padding(vertical = 2.dp)) }
                    }
                    if (topSuggestions.isNotEmpty()) {
                        Text("Recommendations", color = AccentAI, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            topSuggestions.forEach { rec ->
                                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(theme.trackBg.copy(alpha = 0.2f)).border(0.5.dp, theme.textMuted.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).clickable { onNavigateToScreen(AppDestinations.WORKFLOW_DASHBOARD) }.padding(10.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(rec.stage, color = theme.textMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Text(rec.priority, color = if (rec.priority.lowercase() in listOf("critical", "high")) AccentCritical else AccentWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(rec.issue, color = theme.textMain, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(rec.action, color = AccentAI, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.clickable { recognizedText = "User: Help me reduce energy."; ayamReply = "Ayam: Try reducing steam flow by 5%." }) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentPrimary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Mic, null, tint = AccentPrimary, modifier = Modifier.size(18.dp)) }
                        Column { Text("Speak to Ayam", color = theme.textMuted, fontSize = 12.sp); Text("Voice + translate enabled", color = theme.textLightMuted, fontSize = 11.sp) }
                    }
                    recognizedText?.let { Text(it, color = theme.textMain, fontSize = 12.sp) }
                    ayamReply?.let { Text(it, color = AccentAI, fontSize = 12.sp) }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopStart).size(72.dp).zIndex(10f).shadow(8.dp, CircleShape).clip(CircleShape).background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), BrandLightGray.copy(alpha = 0.5f)))).border(2.dp, Brush.linearGradient(listOf(Color.White, expressionColor.copy(alpha = 0.6f))), CircleShape)) {
            Image(painter = painterResource(id = R.drawable.ayam_image), contentDescription = null, modifier = Modifier.matchParentSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
    }
}

@Composable fun TelemetryCol(label: String, value: String, theme: DashboardTheme) { Column { Text(label, color = theme.textLightMuted, fontSize = 11.sp); Text(value, color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold) } }

@Composable
fun CleanPanel(theme: DashboardTheme, modifier: Modifier = Modifier, cornerRadius: Dp = 20.dp, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.shadow(16.dp, RoundedCornerShape(cornerRadius), spotColor = if (theme.isDark) Color.Black else BrandDeepNavy.copy(alpha = 0.06f)).clip(RoundedCornerShape(cornerRadius)).background(Brush.linearGradient(if (theme.isDark) listOf(BrandDarkBlueGray.copy(alpha = 0.45f), BrandSteelGray.copy(alpha = 0.15f)) else listOf(BrandOffWhite.copy(alpha = 0.88f), BrandLightGray.copy(alpha = 0.30f)))).border(1.dp, Brush.linearGradient(if (theme.isDark) listOf(BrandLightGray.copy(alpha = 0.3f), Color.Transparent) else listOf(Color.White.copy(alpha = 0.95f), BrandLightGray.copy(alpha = 0.35f))), RoundedCornerShape(cornerRadius)), content = content)
}

@Composable fun KpiCell(theme: DashboardTheme, title: String, value: String, unit: String, color: Color) { Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) { Text(title, color = theme.textMuted, fontSize = 12.sp); Row(verticalAlignment = Alignment.Bottom) { Text(value, color = theme.textMain, fontSize = 22.sp, fontWeight = FontWeight.Black); if (unit.isNotEmpty()) Text(" $unit", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 3.dp)) } } }
@Composable fun KpiRow(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, color = theme.textMuted, fontSize = 12.sp); Row(verticalAlignment = Alignment.Bottom) { Text(value, color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(" $unit", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 1.dp)) } } }
@Composable fun KpiCellMini(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = theme.textLightMuted, fontSize = 11.sp); Spacer(Modifier.height(3.dp)); Text(value, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(unit, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }