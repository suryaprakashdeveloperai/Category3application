@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.category3.auth.ui

import android.content.res.Configuration
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val BrandDeepNavy     = Color(0xFF0A0D2F)
val BrandDarkBlueGray = Color(0xFF223B57)
val BrandSteelGray    = Color(0xFF8C929C)
val BrandLightGray    = Color(0xFFBCBCBF)
val BrandOffWhite     = Color(0xFFF6F6F7)
val BrandCyanBlue     = Color(0xFF47B3E2)
val BrandMutedBlue    = Color(0xFF496D89)
val BrandTeal         = Color(0xFF11CFC9)
val BrandOrange       = Color(0xFFF68420)
val BrandSoftOrange   = Color(0xFFD68A51)

val AccentPrimary  = BrandCyanBlue
val AccentSuccess  = BrandTeal
val AccentWarning  = BrandSoftOrange
val AccentCritical = BrandOrange
val AccentAI       = BrandMutedBlue

data class DashboardTheme(
    val isDark: Boolean,
    val textMain: Color,
    val textMuted: Color,
    val textLightMuted: Color,
    val trackBg: Color
)

fun getAdaptiveTheme(isDark: Boolean): DashboardTheme =
    if (isDark) {
        DashboardTheme(
            true,
            BrandOffWhite,
            BrandLightGray,
            BrandSteelGray,
            BrandDeepNavy.copy(alpha = 0.5f)
        )
    } else {
        DashboardTheme(
            false,
            BrandDeepNavy,
            BrandMutedBlue,
            BrandSteelGray,
            BrandLightGray.copy(alpha = 0.3f)
        )
    }

data class StageAlertSummary(
    val critical: Int,
    val warning: Int
)

data class StageCardDisplay(
    val primaryLabel: String,
    val primaryValue: String,
    val primaryUnit: String,
    val stat1Label: String,
    val stat1Value: String,
    val stat2Label: String,
    val stat2Value: String,
    val stat3Label: String,
    val stat3Value: String
)

fun stageCardDisplay(stage: LiveStageData): StageCardDisplay = when (stage.id) {
    "01" -> StageCardDisplay(
        "THROUGHPUT",
        String.format("%,.0f", stage.actualFlow),
        "kg/hr",
        "MOTOR", "${stage.energyKw.toInt()}A",
        "RPM", "${stage.vibrationHz.toInt()}",
        "TEMP", "${stage.tempC.toInt()}°C"
    )
    "02" -> StageCardDisplay(
        "pH LEVEL",
        String.format("%.2f", stage.pressureBar),
        "pH",
        "DJ TEMP", "${stage.tempC.toInt()}°C",
        "TANK", "${stage.tankFillPercent}%",
        "PUMP", "${stage.vibrationHz.toInt()}A"
    )
    "03" -> StageCardDisplay(
        "EVAP FLOW",
        String.format("%.1f", stage.actualFlow),
        "m³/hr",
        "TEMP B1", "${stage.tempC.toInt()}°C",
        "PRESS", String.format("%.2f", stage.pressureBar),
        "BRIX", "${stage.efficiency}°Bx"
    )
    "04" -> StageCardDisplay(
        "CJ FLOW",
        String.format("%.2f", stage.actualFlow),
        "L/hr",
        "TEMP", "${stage.tempC.toInt()}°C",
        "TANK", "${stage.tankFillPercent}%",
        "FC VFD", "${stage.vibrationHz.toInt()}%"
    )
    "05" -> StageCardDisplay(
        "SYRUP FLOW",
        String.format("%,.0f", stage.actualFlow),
        "L/hr",
        "PAN TEMP", "${stage.tempC.toInt()}°C",
        "VACUUM", "${stage.pressureBar.toInt()}mb",
        "RPM", "${stage.vibrationHz.toInt()}"
    )
    else -> StageCardDisplay(
        "FLOW",
        String.format("%,.0f", stage.actualFlow),
        "u/hr",
        "TEMP", "${stage.tempC.toInt()}°C",
        "VIB", "${stage.vibrationHz.toInt()}Hz",
        "PWR", "${stage.energyKw.toInt()}kW"
    )
}

enum class AyamMood {
    NORMAL,
    WARNING,
    CRITICAL
}

@RawRes
fun ayamVideoForMood(mood: AyamMood): Int = when (mood) {
    AyamMood.NORMAL -> R.raw.ayam_happy
    AyamMood.WARNING -> R.raw.ayam_sad
    AyamMood.CRITICAL -> R.raw.ayam_angry
}

@Composable
fun AyamVideoAvatar(
    modifier: Modifier = Modifier,
    @RawRes videoResId: Int
) {
    val context = LocalContext.current

    val exoPlayer = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            val uri = "android.resource://${context.packageName}/$videoResId"
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = 0f
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer

                // IMPORTANT: use TextureView, not SurfaceView
//             setSurfaceType(PlayerView.SURFACE_TYPE_TEXTURE_VIEW)

                // Fill the circle, no letterboxing
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                // No black shutter or background
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { view ->
            view.player = exoPlayer
        }
    )
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun WorkflowDashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit
) {
    val theme = getAdaptiveTheme(false)

    val globalOee by viewModel.globalOee.collectAsStateWithLifecycle()
    val globalEnergy by viewModel.globalEnergy.collectAsStateWithLifecycle()
    val globalThroughput by viewModel.globalThroughput.collectAsStateWithLifecycle()
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()

    var selectedStageId by remember { mutableStateOf<String?>(null) }
    var ayamPopupData by remember { mutableStateOf<RecommendationData?>(null) }
    var isAyamAssistOpen by remember { mutableStateOf(false) }

    val activeAiLogs = remember(stages) { stages.flatMap { it.recommendations } }

    val stageAlertSummary = remember(activeAlerts) {
        activeAlerts
            .groupBy { it.stage.uppercase() }
            .mapValues { (_, alerts) ->
                val crit = alerts.count { it.priority.equals("CRITICAL", ignoreCase = true) }
                val warn = alerts.count { it.priority.equals("WARNING", ignoreCase = true) }
                StageAlertSummary(crit, warn)
            }
    }

    val ayamMood = remember(activeAlerts) {
        val hasCritical = activeAlerts.any {
            !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true)
        }
        val hasWarning = activeAlerts.any {
            !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true)
        }
        when {
            hasCritical -> AyamMood.CRITICAL
            hasWarning -> AyamMood.WARNING
            else -> AyamMood.NORMAL
        }
    }

    var heatmapTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            heatmapTick++
        }
    }

    val energyHistory = remember {
        mutableStateListOf(160f, 165f, 172f, 168f, 175f, 180f, 178f, 182f, 184f)
    }
    LaunchedEffect(globalEnergy) {
        energyHistory.add(globalEnergy.toFloat())
        if (energyHistory.size > 15) energyHistory.removeAt(0)
    }

    val throughputHistory = remember {
        mutableStateListOf(100f, 110f, 115f, 108f, 120f, 130f, 125f, 140f, 138f)
    }
    LaunchedEffect(globalThroughput) {
        throughputHistory.add(globalThroughput.toFloat())
        if (throughputHistory.size > 15) throughputHistory.removeAt(0)
    }

    val displayStages = remember(stages) {
        val evapIdx =
            stages.indexOfFirst { it.id == "03" || it.name.contains("Evap", ignoreCase = true) }
        val clarIdx =
            stages.indexOfFirst { it.id == "04" || it.name.contains("Clar", ignoreCase = true) }
        if (evapIdx != -1 && clarIdx != -1 && evapIdx != clarIdx) {
            stages.toMutableList().apply {
                val tmp = this[evapIdx]
                this[evapIdx] = this[clarIdx]
                this[clarIdx] = tmp
            }
        } else stages
    }

    val activeStage = displayStages.find { it.id == selectedStageId }
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(BrandOffWhite, Color.White)))
    ) {
        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GraphPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    theme = theme,
                    displayStages = displayStages,
                    selectedStageId = selectedStageId,
                    onStageSelected = { selectedStageId = it },
                    heatmapTick = heatmapTick,
                    isPortrait = true,
                    alertSummary = stageAlertSummary
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectedKpiPanel(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        theme = theme,
                        displayStages = displayStages,
                        activeStage = activeStage
                    )
                    EnergyPanel(
                        modifier = Modifier.weight(1.5f).fillMaxHeight(),
                        theme = theme,
                        activeStage = activeStage,
                        globalEnergy = globalEnergy.toFloat(),
                        globalThroughput = globalThroughput.toFloat(),
                        energyHistory = energyHistory,
                        throughputHistory = throughputHistory
                    )
                }

                SuggestionsPanel(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    theme = theme,
                    activeStage = activeStage,
                    suggestions = activeAiLogs,
                    onSuggestionClick = { ayamPopupData = it }
                )

                AlertsPanel(
                    modifier = Modifier.fillMaxWidth().height(380.dp),
                    theme = theme,
                    activeAlerts = activeAlerts,
                    onNavigateToScreen = onNavigateToScreen
                )

                Spacer(Modifier.height(16.dp))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(2.1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GraphPanel(
                        modifier = Modifier.fillMaxWidth().weight(2.5f),
                        theme = theme,
                        displayStages = displayStages,
                        selectedStageId = selectedStageId,
                        onStageSelected = { selectedStageId = it },
                        heatmapTick = heatmapTick,
                        isPortrait = false,
                        alertSummary = stageAlertSummary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProjectedKpiPanel(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            theme = theme,
                            displayStages = displayStages,
                            activeStage = activeStage
                        )
                        SuggestionsPanel(
                            modifier = Modifier.weight(2.2f).fillMaxHeight(),
                            theme = theme,
                            activeStage = activeStage,
                            suggestions = activeAiLogs,
                            onSuggestionClick = { ayamPopupData = it }
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyPanel(
                        modifier = Modifier.fillMaxWidth().weight(0.7f),
                        theme = theme,
                        activeStage = activeStage,
                        globalEnergy = globalEnergy.toFloat(),
                        globalThroughput = globalThroughput.toFloat(),
                        energyHistory = energyHistory,
                        throughputHistory = throughputHistory
                    )
                    AlertsPanel(
                        modifier = Modifier.fillMaxWidth().weight(2.3f),
                        theme = theme,
                        activeAlerts = activeAlerts,
                        onNavigateToScreen = onNavigateToScreen
                    )
                }
            }
        }

        ayamPopupData?.let { data ->
            AyamAiDialog(data, theme) { ayamPopupData = null }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            AyamAssistantFab(
                modifier = Modifier.size(72.dp),
                mood = ayamMood,
                onClick = { isAyamAssistOpen = !isAyamAssistOpen }
            )

            AyamHelloBubble(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-40).dp),
                theme = theme,
                mood = ayamMood,
                onSpeakNow = { }
            )
        }

        if (isAyamAssistOpen) {
            AyamAssistantPopup(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                theme = theme,
                activeAlerts = activeAlerts,
                suggestions = activeAiLogs,
                onNavigateToScreen = onNavigateToScreen,
                onClose = { isAyamAssistOpen = false }
            )
        }

        RadialAppBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .zIndex(30f),
            activeSection = "workflow_dashboard",
            onActionSelected = { onNavigateToScreen(it) }
        )
    }
}

@Composable
fun GraphPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    displayStages: List<LiveStageData>,
    selectedStageId: String?,
    onStageSelected: (String?) -> Unit,
    heatmapTick: Int,
    isPortrait: Boolean,
    alertSummary: Map<String, StageAlertSummary> = emptyMap()
) {
    val flowPath = remember { Path() }
    val tempPath = remember { Path() }

    CleanPanel(theme, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.8f)
                    .padding(bottom = 8.dp)
            ) {
                val canvasWidth = maxWidth
                val canvasHeight = maxHeight - 10.dp
                val stagesForLine =
                    if (displayStages.isNotEmpty()) displayStages.dropLast(1) else emptyList()

                val maxValue = stagesForLine.maxOfOrNull {
                    maxOf(it.actualFlow, it.tempC.toFloat())
                } ?: 100f
                val graphMaxBound = if (maxValue > 0f) maxValue * 1.25f else 100f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (displayStages.isEmpty()) return@Canvas

                    val w = size.width
                    val h = size.height
                    val graphH = h - 10.dp.toPx()
                    val colW = w / displayStages.size

                    fun calcY(v: Float) =
                        (graphH - ((v / graphMaxBound) * graphH)).coerceIn(0f, graphH)

                    flowPath.reset()
                    tempPath.reset()

                    for (tick in 0..4) {
                        val y = calcY((graphMaxBound / 4f) * tick)
                        drawLine(
                            color = theme.textMuted.copy(alpha = 0.12f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    displayStages.forEachIndexed { i, stage ->
                        val colLeft = i * colW
                        drawRect(
                            color = stage.color.copy(alpha = if (theme.isDark) 0.12f else 0.07f),
                            topLeft = Offset(colLeft, 0f),
                            size = Size(colW, graphH)
                        )
                        if (i > 0) {
                            drawLine(
                                color = theme.textMuted.copy(alpha = 0.3f),
                                start = Offset(colLeft, 0f),
                                end = Offset(colLeft, graphH),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    fun Path.buildSmooth(values: List<Float>) {
                        if (values.size < 2) return
                        values.forEachIndexed { index, value ->
                            if (index >= stagesForLine.size) return@forEachIndexed
                            val x = (index * colW) + (colW / 2f)
                            val y = calcY(value)
                            if (index == 0) moveTo(x, y)
                            else {
                                val prevX = ((index - 1) * colW) + (colW / 2f)
                                val prevY = calcY(values[index - 1])
                                val midX = (prevX + x) / 2f
                                cubicTo(midX, prevY, midX, y, x, y)
                            }
                        }
                    }

                    val flowValues = stagesForLine.map { it.actualFlow }
                    val tempValues = stagesForLine.map { it.tempC.toFloat() }

                    flowPath.buildSmooth(flowValues)
                    tempPath.buildSmooth(tempValues)

                    drawPath(
                        path = flowPath,
                        color = AccentPrimary.copy(alpha = 0.18f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    drawPath(
                        path = tempPath,
                        color = AccentWarning.copy(alpha = 0.15f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    drawPath(
                        path = tempPath,
                        color = AccentWarning,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = flowPath,
                        color = AccentPrimary,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    displayStages.forEachIndexed { i, stage ->
                        val nx = (i * colW) + (colW / 2f)
                        if (i < stagesForLine.size) {
                            val flowY = calcY(stage.actualFlow)
                            val tempY = calcY(stage.tempC.toFloat())

                            val summary = alertSummary[stage.name.uppercase()]
                            val hasAlert = (summary?.critical ?: 0) > 0 || (summary?.warning ?: 0) > 0

                            if (hasAlert) {
                                drawCircle(
                                    color = AccentCritical.copy(alpha = 0.35f),
                                    radius = 9.dp.toPx(),
                                    center = Offset(nx, flowY)
                                )
                            }

                            drawCircle(BrandOffWhite, 6.dp.toPx(), Offset(nx, tempY))
                            drawCircle(AccentWarning, 4.dp.toPx(), Offset(nx, tempY))
                            drawCircle(BrandOffWhite, 7.dp.toPx(), Offset(nx, flowY))
                            drawCircle(AccentPrimary, 5.dp.toPx(), Offset(nx, flowY))
                        } else {
                            drawBatchGrid(stage, colW, i, h - 10.dp.toPx(), heatmapTick, theme, isPortrait)
                        }
                    }
                }

                if (stagesForLine.isNotEmpty()) {
                    val colWdp = canvasWidth / displayStages.size
                    val maxValue = stagesForLine.maxOfOrNull {
                        maxOf(it.actualFlow, it.tempC.toFloat())
                    } ?: 100f
                    val graphMaxBound = if (maxValue > 0f) maxValue * 1.25f else 100f

                    stagesForLine.forEachIndexed { i, stage ->
                        val nx = (colWdp * i) + (colWdp / 2)
                        val actualV = stage.actualFlow
                        val ay = canvasHeight.value - ((actualV / graphMaxBound) * canvasHeight.value)
                        val summary = alertSummary[stage.name.uppercase()]

                        Box(
                            modifier = Modifier
                                .offset(x = nx - 70.dp, y = ay.dp - 64.dp)
                                .width(140.dp)
                                .clickable {
                                    onStageSelected(if (selectedStageId == stage.id) null else stage.id)
                                }
                        ) {
                            StageFlowGlassBox(
                                stage = stage,
                                flowValue = actualV,
                                summary = summary,
                                theme = theme
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayStages.forEach { stage ->
                    val isSelected = selectedStageId == stage.id
                    Text(
                        text = stage.name,
                        color = if (isSelected) stage.color else theme.textMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStageSelected(if (isSelected) null else stage.id) }
                            .padding(vertical = 2.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                displayStages.forEach { stage ->
                    val isSelected = selectedStageId == stage.id
                    val cardDisplay = stageCardDisplay(stage)

                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) stage.color.copy(alpha = 0.18f)
                                    else theme.trackBg.copy(alpha = 0.3f)
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) stage.color else BrandLightGray.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onStageSelected(if (selectedStageId == stage.id) null else stage.id)
                                }
                        ) {
                            val tankPct = stage.tankFillPercent.coerceIn(0, 100)
                            val fillHeight = (tankPct / 100f).coerceIn(0f, 1f)
                            val baseColor = stage.color
                            val barColor = if (tankPct >= 90) AccentCritical else baseColor

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(vertical = 4.dp)
                                    .width(5.dp)
                                    .fillMaxHeight(fillHeight)
                                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                    .background(barColor)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 6.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = cardDisplay.primaryValue,
                                        color = theme.textMain,
                                        fontSize = if (isPortrait) 14.sp else 20.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = BrandDeepNavy.copy(alpha = 0.1f),
                                                offset = Offset(0f, 2f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    Text(
                                        text = cardDisplay.primaryLabel,
                                        color = stage.color,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val statStyle = TextStyle(
                                    shadow = Shadow(
                                        color = BrandDeepNavy.copy(alpha = 0.1f),
                                        blurRadius = 2f
                                    )
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${cardDisplay.stat1Label}: ${cardDisplay.stat1Value}", color = theme.textLightMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${cardDisplay.stat2Label}: ${cardDisplay.stat2Value}", color = theme.textLightMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${cardDisplay.stat3Label}: ${cardDisplay.stat3Value}", color = theme.textLightMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBatchGrid(
    stage: LiveStageData,
    colW: Float,
    i: Int,
    graphH: Float,
    heatmapTick: Int,
    theme: DashboardTheme,
    isPortrait: Boolean
) {
    val colLeft = i * colW
    val scaleDown = if (isPortrait) 0.7f else 1f
    val cellW = (34.dp * scaleDown).toPx()
    val cellH = (22.dp * scaleDown).toPx()
    val spacing = (8.dp * scaleDown).toPx()
    val batchBoxW = (28.dp * scaleDown).toPx()
    val batchSpc = (16.dp * scaleDown).toPx()
    val rows = 4
    val cols = 2
    val totalGridW = (cols * cellW) + ((cols - 1) * spacing) + batchSpc + batchBoxW
    val totalGridH = (rows * cellH) + ((rows - 1) * spacing)
    val gridStartX = colLeft + (colW - totalGridW) / 2
    val batchStartX = gridStartX + (cols * cellW) + ((cols - 1) * spacing) + batchSpc
    val startY = (graphH - totalGridH) / 2 + 10.dp.toPx()

    val hdrPaint = Paint().apply {
        color = stage.color.toArgb()
        textSize = (9.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val tp = Paint().apply {
        color = BrandOffWhite.toArgb()
        textSize = (11.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val bpActive = Paint().apply {
        color = stage.color.toArgb()
        textSize = (13.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val bpInactive = Paint().apply {
        color = theme.textMuted.toArgb()
        textSize = (13.sp * scaleDown).toPx()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    drawContext.canvas.nativeCanvas.apply {
        drawText("PAN", gridStartX + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        drawText("PWDR", gridStartX + cellW + spacing + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        drawText("BATCH", batchStartX + batchBoxW / 2, startY - 10.dp.toPx(), hdrPaint)
    }

    val emptyColor =
        if (theme.isDark) BrandDarkBlueGray.copy(alpha = 0.4f)
        else BrandLightGray.copy(alpha = 0.5f)
    val activeColor = stage.color

    for (r in 0 until rows) {
        val cy = startY + r * (cellH + spacing)
        val currentBatchTime = (heatmapTick + r * 7) % 25
        val isActive = currentBatchTime < 21

        for (c in 0 until cols) {
            val cx = gridStartX + c * (cellW + spacing)
            val cellActive = when {
                !isActive -> false
                c == 0 -> currentBatchTime < 12
                else -> currentBatchTime >= 12
            }
            if (cellActive) {
                drawRoundRect(
                    color = activeColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx - 2.dp.toPx(), cy - 2.dp.toPx()),
                    size = Size(cellW + 4.dp.toPx(), cellH + 4.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
            drawRoundRect(
                color = if (cellActive) activeColor else emptyColor.copy(alpha = 0.2f),
                topLeft = Offset(cx, cy),
                size = Size(cellW, cellH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            if (cellActive) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${currentBatchTime + 1}m",
                    cx + cellW / 2,
                    cy + cellH / 2 - (tp.ascent() + tp.descent()) / 2,
                    tp
                )
            }
        }

        drawRoundRect(
            color = if (isActive) activeColor.copy(alpha = 0.15f) else emptyColor.copy(alpha = 0.1f),
            topLeft = Offset(batchStartX, cy),
            size = Size(batchBoxW, cellH),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        val currentBp = if (isActive) bpActive else bpInactive
        drawContext.canvas.nativeCanvas.drawText(
            "#${r + 1}",
            batchStartX + batchBoxW / 2,
            cy + cellH / 2 - (currentBp.ascent() + currentBp.descent()) / 2,
            currentBp
        )
    }
}

@Composable
fun StageFlowGlassBox(
    stage: LiveStageData,
    flowValue: Float,
    summary: StageAlertSummary?,
    theme: DashboardTheme
) {
    val critical = summary?.critical ?: 0
    val warning = summary?.warning ?: 0

    val (statusText, statusColor) = when {
        critical > 0 -> "CRITICAL" to AccentCritical
        warning > 0 -> "WARNING" to AccentWarning
        else -> "OK" to AccentSuccess
    }

    val alertsText = when {
        critical > 0 && warning > 0 -> "$critical CRIT · $warning WARN"
        critical > 0 -> "$critical CRIT"
        warning > 0 -> "$warning WARN"
        else -> "No active alerts"
    }

    val cardShape = RoundedCornerShape(14.dp)

    Box(modifier = Modifier.wrapContentSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 10.dp,
                        shape = cardShape,
                        spotColor = Color.Black.copy(alpha = 0.12f),
                        ambientColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .clip(cardShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.88f), Color.White.copy(alpha = 0.55f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.35f))
                        ),
                        cardShape
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stage.name.uppercase(),
                        color = stage.color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format("%,.0f", flowValue),
                        color = BrandDeepNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$statusText · $alertsText",
                        color = statusColor.copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset(y = (-2).dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary)
            )
        }
    }
}

@Composable
fun ProjectedKpiPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    displayStages: List<LiveStageData>,
    activeStage: LiveStageData?
) {
    val displayProjection =
        activeStage?.aiProjection
            ?: if (displayStages.isNotEmpty()) displayStages.map { it.aiProjection }.average().toFloat()
            else 0f

    val animProjection by animateFloatAsState(displayProjection, tween(800), label = "proj")

    CleanPanel(theme, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            Text(
                if (activeStage != null) "${activeStage.name} AI PROJ." else "GLOBAL AI PROJ.",
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    String.format("%,d", animProjection.toInt()),
                    color = AccentAI,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "TCD",
                    color = theme.textMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "AI forecast for next 4 hours.",
                color = theme.textLightMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun EnergyPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    activeStage: LiveStageData?,
    globalEnergy: Float,
    globalThroughput: Float,
    energyHistory: List<Float>,
    throughputHistory: List<Float>
) {
    val displayEnergy = activeStage?.energyKw ?: globalEnergy
    val displayThroughput = activeStage?.actualFlow ?: globalThroughput

    val animEnergy by animateFloatAsState(displayEnergy, tween(800), label = "energy")
    val animThroughput by animateFloatAsState(displayThroughput, tween(800), label = "throughput")
    val cardShape = RoundedCornerShape(18.dp)

    CleanPanel(theme, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThroughputCard(
                modifier = Modifier.weight(1f),
                value = animThroughput,
                unit = if (activeStage != null) "kg/hr" else "TCD",
                history = throughputHistory,
                cardShape = cardShape
            )
            EnergyCard(
                modifier = Modifier.weight(1f),
                value = animEnergy,
                history = energyHistory,
                cardShape = cardShape
            )
        }
    }
}

@Composable
private fun ThroughputCard(
    modifier: Modifier,
    value: Float,
    unit: String,
    history: List<Float>,
    cardShape: RoundedCornerShape
) {
    Column(
        modifier = modifier
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(AccentPrimary.copy(alpha = 0.18f), AccentPrimary.copy(alpha = 0.08f))
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier.size(20.dp).background(AccentPrimary.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Schedule, null, tint = AccentPrimary, modifier = Modifier.size(12.dp))
            }
            Text("Throughput", color = BrandDeepNavy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = String.format("%,d", value.toInt()),
                color = BrandDeepNavy,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                color = BrandMutedBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Text("Production rate", color = BrandSteelGray, fontSize = 10.sp)

        ThroughputDotRow(
            history = history,
            dotColor = AccentPrimary,
            modifier = Modifier.fillMaxWidth().height(22.dp)
        )
    }
}

@Composable
private fun ThroughputDotRow(
    history: List<Float>,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(1f, tween(700), label = "dotRow")

    Canvas(modifier = modifier) {
        if (history.isEmpty()) return@Canvas

        val maxV = history.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val dotCount = history.size
        val baseDiameter = 6.dp.toPx()
        val totalDotsWidth = dotCount * baseDiameter
        val spacing = (size.width - totalDotsWidth) / (dotCount - 1).coerceAtLeast(1)

        history.forEachIndexed { index, v ->
            val frac = (v / maxV).coerceIn(0.15f, 1f)
            val radius = (baseDiameter / 2f) * frac * animatedProgress
            val x = index * (baseDiameter + spacing) + baseDiameter / 2f
            val y = size.height - radius - 1.dp.toPx()

            drawCircle(dotColor.copy(alpha = 0.18f), radius * 1.6f, Offset(x, y))
            drawCircle(dotColor.copy(alpha = 0.55f + 0.45f * frac), radius, Offset(x, y))
        }
    }
}

@Composable
private fun EnergyCard(
    modifier: Modifier,
    value: Float,
    history: List<Float>,
    cardShape: RoundedCornerShape
) {
    val avgEnergy = history.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: value
    val diff = value - avgEnergy
    val pct = if (avgEnergy != 0f) diff / avgEnergy * 100f else 0f
    val isUp = pct >= 0f

    Column(
        modifier = modifier
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(AccentWarning.copy(alpha = 0.16f), AccentWarning.copy(alpha = 0.05f))
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier.size(20.dp).background(AccentWarning.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ElectricBolt, null, tint = AccentWarning, modifier = Modifier.size(12.dp))
            }
            Text("Energy Consumption", color = BrandDeepNavy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(76.dp), contentAlignment = Alignment.Center) {
                EnergyGauge(value = value, min = 0f, max = 60f, arcColor = AccentWarning)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value.toInt().toString(), color = BrandDeepNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("kW", color = BrandSteelGray, fontSize = 9.sp)
                }
            }

            Spacer(Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("Current usage", color = BrandSteelGray, fontSize = 9.sp)
                Text("${value.toInt()} kW", color = BrandDeepNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                val diffColor = if (isUp) AccentCritical else AccentSuccess
                Text(
                    text = "${if (isUp) "↑" else "↓"} ${kotlin.math.abs(pct).toInt()}% vs avg",
                    color = diffColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EnergyGauge(
    value: Float,
    min: Float,
    max: Float,
    arcColor: Color
) {
    val clamped = value.coerceIn(min, max)
    val normalized = if (max - min == 0f) 0f else (clamped - min) / (max - min)
    val animatedNorm by animateFloatAsState(normalized, tween(700), label = "energyGauge")

    Canvas(
        modifier = Modifier.fillMaxWidth().height(76.dp)
    ) {
        val strokeWidth = 9.dp.toPx()
        val padding = strokeWidth / 2 + 4.dp.toPx()

        val arcWidth = size.width - 2 * padding
        val arcHeight = size.height * 1.5f - 2 * padding

        val topLeft = Offset(padding, padding)
        val arcSize = Size(arcWidth, arcHeight)

        val startAngle = 180f
        val fullSweep = 180f
        val sweep = fullSweep * animatedNorm

        drawArc(
            color = BrandSteelGray.copy(alpha = 0.25f),
            startAngle = startAngle,
            sweepAngle = fullSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(AccentSuccess, arcColor, AccentCritical),
                start = Offset(topLeft.x, topLeft.y + arcSize.height),
                end = Offset(topLeft.x + arcSize.width, topLeft.y)
            ),
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val centerX = topLeft.x + arcSize.width / 2f
        val centerY = topLeft.y + arcSize.height / 2f
        val radius = arcSize.width / 2f
        val angleDeg = startAngle + sweep
        val angleRad = angleDeg * (PI.toFloat() / 180f)
        val pointerX = centerX + radius * cos(angleRad.toDouble()).toFloat()
        val pointerY = centerY + radius * sin(angleRad.toDouble()).toFloat()

        drawCircle(Color.White, strokeWidth * 0.75f, Offset(pointerX, pointerY))
    }
}

@Composable
fun SuggestionsPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    activeStage: LiveStageData?,
    suggestions: List<RecommendationData>,
    onSuggestionClick: (RecommendationData) -> Unit
) {
    val stageSuggestions = remember(activeStage, suggestions) {
        val base = if (activeStage != null) {
            suggestions.filter { it.stage.equals(activeStage.name, ignoreCase = true) }
        } else suggestions
        base.take(6)
    }

    CleanPanel(theme, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (activeStage != null) "${activeStage.name} Suggestions" else "System Suggestions",
                        color = theme.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Insights from Ayam AI", color = theme.textLightMuted, fontSize = 10.sp)
                }
                Icon(Icons.Outlined.Memory, null, tint = AccentAI, modifier = Modifier.size(18.dp))
            }

            if (stageSuggestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("No active recommendations at this time.", color = theme.textLightMuted, fontSize = 11.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stageSuggestions.forEach { rec ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.trackBg.copy(alpha = 0.3f))
                                .border(1.dp, theme.textMuted.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .clickable { onSuggestionClick(rec) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rec.stage,
                                    color = theme.textMain,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val pColor = when (rec.priority.lowercase()) {
                                    "critical", "high" -> AccentCritical
                                    else -> AccentWarning
                                }
                                Text(rec.priority, color = pColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(rec.issue, color = theme.textMain, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text(rec.action, color = AccentAI, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    activeAlerts: List<AlertData>,
    onNavigateToScreen: (String) -> Unit
) {
    CleanPanel(theme, modifier = modifier) {
        Column(
            Modifier.fillMaxSize().padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Active Alerts Log", color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Synced via Digital Twin API", color = theme.textLightMuted, fontSize = 10.sp)
                }
                Text("${activeAlerts.count { !it.acknowledged }} Active", color = AccentCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            if (activeAlerts.isEmpty()) {
                Text("System clear. No active bottlenecks.", color = theme.textMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 20.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeAlerts, key = { it.id }) { alert ->
                        val isAck = alert.acknowledged
                        val color = if (isAck) BrandSteelGray else when (alert.priority.uppercase()) {
                            "CRITICAL" -> AccentCritical
                            "WARNING" -> AccentWarning
                            else -> AccentPrimary
                        }
                        val canNavigate =
                            alert.sourceRoute == "energy_tab" || alert.sourceRoute == "production_tab"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isAck) Color.Transparent else theme.trackBg.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    color.copy(alpha = if (isAck) 0.1f else 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = canNavigate) {
                                    val section = alert.targetSection ?: alert.stage
                                    val alertId = alert.targetAlertId ?: alert.id
                                    val route = when (alert.sourceRoute) {
                                        "energy_tab" -> "${AppDestinations.ENERGY_TAB}?section=$section&alertId=$alertId"
                                        "production_tab" -> "${AppDestinations.PRODUCTION_TAB}?section=$section&alertId=$alertId"
                                        else -> null
                                    }
                                    route?.let(onNavigateToScreen)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isAck) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                                null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    alert.message,
                                    color = if (isAck) theme.textMuted else theme.textMain,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text("${alert.stage} • ${alert.priority}", color = color, fontSize = 10.sp)
                            }
                            if (isAck) {
                                Text("ACKNOWLEDGED", color = BrandSteelGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyamAiDialog(
    ayamPopupData: RecommendationData,
    theme: DashboardTheme,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CleanPanel(theme = theme, modifier = Modifier.width(420.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Memory, null, tint = AccentAI, modifier = Modifier.size(28.dp))
                            Column {
                                Text("Ayam AI DIAGNOSIS", color = AccentAI, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(ayamPopupData.stage, color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, null, tint = theme.textMuted)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentCritical.copy(0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Predicted Bottleneck", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(ayamPopupData.issue, color = theme.textMain, fontSize = 14.sp)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentSuccess.copy(0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("AI Suggested Solution", color = AccentSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(ayamPopupData.action, color = theme.textMain, fontSize = 14.sp)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Acknowledge",
                            color = BrandOffWhite,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(AccentAI, RoundedCornerShape(8.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AyamAssistantFab(
    modifier: Modifier = Modifier,
    mood: AyamMood,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        // Glassmorphic background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            BrandLightGray.copy(alpha = 0.20f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            BrandLightGray.copy(alpha = 0.45f)
                        )
                    ),
                    shape = CircleShape
                )
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.16f),
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                )
        )

        // Video avatar on top, inside the glass circle
        AyamVideoAvatar(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape),
            videoResId = ayamVideoForMood(mood)
        )
    }
}
@Composable
fun AyamHelloBubble(
    modifier: Modifier = Modifier,
    theme: DashboardTheme,
    mood: AyamMood,
    onSpeakNow: (String) -> Unit = {}
) {
    val shape = RoundedCornerShape(18.dp)

    val (mainText, accentColor) = when (mood) {
        AyamMood.NORMAL -> "Hello, I'm Ayam" to AccentPrimary
        AyamMood.WARNING -> "I'm concerned" to AccentWarning
        AyamMood.CRITICAL -> "I'm concerned" to AccentCritical
    }

    val subText = "Use Google Speak now"

    val scale by animateFloatAsState(
        targetValue = if (mood == AyamMood.NORMAL) 1f else 1.05f,
        animationSpec = tween(350),
        label = "ayamHelloScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = Color.Black.copy(alpha = 0.16f),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.94f), Color.White.copy(alpha = 0.70f))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.95f), accentColor.copy(alpha = 0.45f))
                ),
                shape
            )
            .clickable { onSpeakNow("$mainText. $subText") }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column {
            Text(mainText, color = BrandDeepNavy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(subText, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AyamAssistantPopup(
    modifier: Modifier = Modifier,
    theme: DashboardTheme,
    activeAlerts: List<AlertData>,
    suggestions: List<RecommendationData>,
    onNavigateToScreen: (String) -> Unit,
    onClose: () -> Unit
) {
    val frequentStages = remember(activeAlerts) {
        activeAlerts.groupBy { it.stage }.filter { (_, alerts) -> alerts.size >= 2 }.keys
    }

    val topSuggestions = remember(suggestions) { suggestions.take(5) }

    val hasCritical = activeAlerts.any {
        !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true)
    }
    val hasWarning = activeAlerts.any {
        !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true)
    }

    val expressionTitle: String
    val expressionText: String
    val expressionColor: Color
    val popupMood: AyamMood

    when {
        hasCritical -> {
            expressionTitle = "Concerned"
            expressionText = "I’m seeing critical issues that need immediate attention."
            expressionColor = AccentCritical
            popupMood = AyamMood.CRITICAL
        }
        hasWarning -> {
            expressionTitle = "Alert"
            expressionText = "There are some warnings you may want to check."
            expressionColor = AccentWarning
            popupMood = AyamMood.WARNING
        }
        else -> {
            expressionTitle = "Calm"
            expressionText = "System looks stable. No major issues right now."
            expressionColor = AccentSuccess
            popupMood = AyamMood.NORMAL
        }
    }

    var recognizedText by remember { mutableStateOf<String?>(null) }
    var ayamReply by remember { mutableStateOf<String?>(null) }

    fun fakeVoiceInteraction() {
        recognizedText = "User: Help me reduce energy in evaporation."
        ayamReply = "Ayam: Check steam pressure and vacuum in Evaporation. Try reducing steam flow by 5%."
    }

    Box(modifier = modifier) {
        CleanPanel(
            theme = theme,
            cornerRadius = 24.dp,
            modifier = Modifier.padding(start = 36.dp).widthIn(max = 420.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(18.dp)
                        .background(Color.White.copy(alpha = 0.72f))
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ayam Assistant", color = theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(expressionTitle, color = expressionColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Rounded.Close, null, tint = theme.textMuted)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(expressionColor.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Text("Expression", color = expressionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(expressionText, color = theme.textMain, fontSize = 10.sp)
                    }

                    if (frequentStages.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Repeated alerts", color = AccentCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        frequentStages.forEach { st ->
                            Text(
                                "• $st – tap to drill into Energy view",
                                color = theme.textMain,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable { onNavigateToScreen("${AppDestinations.ENERGY_TAB}?section=$st") }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }

                    if (topSuggestions.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Suggestions", color = AccentAI, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            topSuggestions.forEach { rec ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.trackBg.copy(alpha = 0.3f))
                                        .border(1.dp, theme.textMuted.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToScreen(AppDestinations.WORKFLOW_DASHBOARD) }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            rec.stage,
                                            color = theme.textMain,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val pColor = when (rec.priority.lowercase()) {
                                            "critical", "high" -> AccentCritical
                                            else -> AccentWarning
                                        }
                                        Text(rec.priority, color = pColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(rec.issue, color = theme.textMain, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(rec.action, color = AccentAI, fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentPrimary.copy(alpha = 0.15f))
                                .clickable { fakeVoiceInteraction() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Mic, "Speak to Ayam", tint = AccentPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Speak to Ayam (voice + translate)", color = theme.textMuted, fontSize = 10.sp)
                            Text("TODO: hook Google Speech‑to‑Text, TTS & translation here.", color = theme.textLightMuted, fontSize = 9.sp)
                        }
                    }

                    recognizedText?.let {
                        Text(it, color = theme.textMain, fontSize = 10.sp)
                    }
                    ayamReply?.let {
                        Text(it, color = AccentAI, fontSize = 10.sp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(72.dp)
                .offset(x = (-8).dp, y = (-24).dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.30f), BrandLightGray.copy(alpha = 0.18f))
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.90f), BrandLightGray.copy(alpha = 0.45f))
                    ),
                    CircleShape
                )
        ) {
            AyamVideoAvatar(
                modifier = Modifier.matchParentSize().clip(CircleShape),
                videoResId = ayamVideoForMood(popupMood)
            )
        }
    }
}

@Composable
fun TelemetryCol(label: String, value: String, theme: DashboardTheme) {
    Column {
        Text(label, color = theme.textLightMuted, fontSize = 9.sp)
        Text(value, color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CleanPanel(
    theme: DashboardTheme,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val bgColors =
        if (theme.isDark)
            listOf(BrandDarkBlueGray.copy(alpha = 0.45f), BrandSteelGray.copy(alpha = 0.15f))
        else
            listOf(BrandOffWhite.copy(alpha = 0.85f), BrandLightGray.copy(alpha = 0.35f))
    val borderColors =
        if (theme.isDark)
            listOf(BrandLightGray.copy(alpha = 0.3f), Color.Transparent)
        else
            listOf(Color.White, BrandLightGray.copy(alpha = 0.4f))
    val shadowColor =
        if (theme.isDark) Color.Black
        else BrandDeepNavy.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    bgColors,
                    Offset(0f, 0f),
                    Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    borderColors,
                    Offset(0f, 0f),
                    Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape
            ),
        content = content
    )
}

@Composable
fun KpiCell(theme: DashboardTheme, title: String, value: String, unit: String, color: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = theme.textMain, fontSize = 24.sp, fontWeight = FontWeight.Black)
            if (unit.isNotEmpty()) {
                Text(
                    " $unit",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
fun KpiRow(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = theme.textMuted, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                " $unit",
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
    }
}

@Composable
fun KpiCellMini(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = theme.textLightMuted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}