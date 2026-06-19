package com.example.category3.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// 📡 UNIQUE NON-CONFLICTING MODELS & ENUMS
// ============================================================================
enum class MinimalPopupType {
    STAGE_DETAIL, OEE, THROUGHPUT, ENERGY, AI_RECOMMENDATIONS, BOTTLENECK, RESOURCE_HEALTH
}

data class MinimalLiveStageData(
    val name: String,
    val status: String,
    val efficiency: Double,
    val coreTemp: String,
    val pressure: String,
    val tintColor: Color,
    val currentLevelPct: Int // Added property for explicit real-time percentage indicators
)

data class MinimalDashboardUiState(
    val throughputValue: Int = 112,
    val totalUsageKwh: String = "1,850",
    val oeePercentage: Int = 87,
    val resourceHealthPct: Int = 91,
    val assetsDueService: Int = 3,
    val bottleneckPercentage: Int = 72,
    val currentEnergyKw: Int = 32,
    val energyVariancePct: Double = 5.0,
    val stages: List<MinimalLiveStageData> = listOf(
        MinimalLiveStageData("Mill", "Completed", 92.4, "65°C", "1.2 bar", Color(0xFF38BDF8), 92),
        MinimalLiveStageData("Defecation", "Completed", 95.1, "85°C", "2.1 bar", Color(0xFFFB923C), 95),
        MinimalLiveStageData("Clarification", "Completed", 91.7, "70°C", "1.8 bar", Color(0xFFA78BFA), 91),
        MinimalLiveStageData("Evaporation", "In Progress", 88.3, "115°C", "4.5 bar", Color(0xFFFB923C), 88),
        MinimalLiveStageData("Concentration", "Upcoming", 93.6, "45°C", "0.8 bar", Color(0xFF38BDF8), 93)
    ),
    val throughputHistory: List<Float> = listOf(0.12f, 0.18f, 0.15f, 0.25f, 0.32f, 0.28f, 0.42f, 0.38f, 0.55f, 0.48f, 0.62f, 0.75f, 0.68f, 0.92f, 0.85f, 0.98f, 0.52f)
) {
    val aiSavingRupees: String = ""
}

data class MinimalMorphicThemeConfig(
    val canvasBg: Brush, val glassFill: Color, val glassBorder: Color,
    val textMain: Color, val textMuted: Color, val systemScrim: Color,
    val popupBg: Color, val barTrackBg: Color
)

private val MinimalColorCyanNeon = Color(0xFF38BDF8)
private val MinimalColorOrangeNeon = Color(0xFFFB923C)
private val MinimalColorPurpleNeon = Color(0xFFA78BFA)
private val MinimalColorGreenNeon = Color(0xFF34D399)
private val MinimalColorRedNeon = Color(0xFFF87171)
private val MinimalColorShadow = Color(0x0C000000)
private val MinimalFontTelemetryMono = FontFamily.Monospace

private fun Path.drawMinimalSmoothCurve(points: List<Offset>) {
    if (points.isEmpty()) return
    moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        val midX = p1.x + (p2.x - p1.x) / 2f
        cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
    }
}

@Composable
private fun getMinimalMorphicTheme(isDarkPurple: Boolean): MinimalMorphicThemeConfig {
    return if (isDarkPurple) {
        MinimalMorphicThemeConfig(
            canvasBg = Brush.radialGradient(colors = listOf(Color(0xFF2E1A47), Color(0xFF0F081D)), radius = 2200f),
            glassFill = Color(0x14FFFFFF),
            glassBorder = Color(0x2BFFFFFF),
            textMain = Color(0xFFF8FAFC),
            textMuted = Color(0xFF94A3B8),
            systemScrim = Color(0x66000000),
            popupBg = Color(0xFF170E24),
            barTrackBg = Color(0x1AFFFFFF)
        )
    } else {
        MinimalMorphicThemeConfig(
            canvasBg = Brush.linearGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))),
            glassFill = Color(0xD9FFFFFF),
            glassBorder = Color(0x1F000000),
            textMain = Color(0xFF1E293B),
            textMuted = Color(0xFF64748B),
            systemScrim = Color(0x33000000),
            popupBg = Color(0xFFFFFFFF),
            barTrackBg = Color(0x0D000000)
        )
    }
}

// ============================================================================
// 🔌 API STRIP & REAL-TIME STREAMING INTERFACE
// ============================================================================
data class ProcessTelemetryPacket(
    val throughput: Int,
    val activeStageEfficiency: Double,
    val dynamicTemperature: String,
    val dynamicPressure: String
)

interface TelemetryLiveApiService {
    fun streamLiveEcosystemTelemetry(): Flow<ProcessTelemetryPacket>
}

class TelemetryLiveRepository(private val apiService: TelemetryLiveApiService) {
    fun fetchLiveDataStream(): Flow<ProcessTelemetryPacket> = apiService.streamLiveEcosystemTelemetry()
}

class SimulationTelemetryApiImplementation : TelemetryLiveApiService {
    override fun streamLiveEcosystemTelemetry(): Flow<ProcessTelemetryPacket> = flow {
        while (true) {
            emit(
                ProcessTelemetryPacket(
                    throughput = Random.nextInt(110, 116),
                    activeStageEfficiency = 85.0 + Random.nextDouble() * 5.0,
                    dynamicTemperature = "${Random.nextInt(112, 119)}°C",
                    dynamicPressure = "${String.format("%.1f", Random.nextFloat() * 0.5f + 4.1f)} bar"
                )
            )
            delay(1000)
        }
    }
}

// ============================================================================
// 🖥️ MINIMAL CONTENT VIEWPORT SCREEN
// ============================================================================
@Composable
fun WorkflowMinimalDashboardScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "waveOffset"
    )

    var uiState by remember { mutableStateOf(MinimalDashboardUiState()) }
    var isDarkPurpleTheme by remember { mutableStateOf(false) }
    val currentTheme = getMinimalMorphicTheme(isDarkPurple = isDarkPurpleTheme)

    var activePopupType by remember { mutableStateOf<MinimalPopupType?>(null) }
    var selectedStageIndex by remember { mutableStateOf(0) }

    val telemetryRepository = remember { TelemetryLiveRepository(SimulationTelemetryApiImplementation()) }

    LaunchedEffect(Unit) {
        telemetryRepository.fetchLiveDataStream().collect { livePacket ->
            val dynamicHistory = uiState.throughputHistory.toMutableList().apply {
                if (isNotEmpty()) removeAt(0)
                add((livePacket.throughput - 100) / 25f)
            }
            uiState = uiState.copy(
                throughputValue = livePacket.throughput,
                throughputHistory = dynamicHistory,
                stages = uiState.stages.mapIndexed { idx, stage ->
                    if (idx == 3) stage.copy(
                        efficiency = livePacket.activeStageEfficiency,
                        coreTemp = livePacket.dynamicTemperature,
                        pressure = livePacket.dynamicPressure,
                        currentLevelPct = livePacket.activeStageEfficiency.toInt()
                    )
                    else stage
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(currentTheme.canvasBg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cyanGlow = Brush.radialGradient(listOf(MinimalColorCyanNeon.copy(alpha = 0.06f), Color.Transparent), center = Offset(size.width * 0.2f, size.height * 0.4f), radius = size.width * 0.5f)
            drawCircle(brush = cyanGlow, radius = size.width * 0.5f, center = Offset(size.width * 0.2f, size.height * 0.4f))
            val orangeGlow = Brush.radialGradient(listOf(MinimalColorOrangeNeon.copy(alpha = 0.05f), Color.Transparent), center = Offset(size.width * 0.8f, size.height * 0.7f), radius = size.width * 0.4f)
            drawCircle(brush = orangeGlow, radius = size.width * 0.4f, center = Offset(size.width * 0.8f, size.height * 0.7f))
        }

        // --- MASTER APPLICATION CONTAINER ---
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // HEADER ACTION BAR CONTROL STRIP
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AURALISS", color = currentTheme.textMain, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkPurpleTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = null, tint = if (isDarkPurpleTheme) MinimalColorPurpleNeon else MinimalColorOrangeNeon,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isDarkPurpleTheme) "PURPLE DARK" else "MATRIX LIGHT",
                            color = currentTheme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = MinimalFontTelemetryMono
                        )
                        Switch(
                            checked = isDarkPurpleTheme, onCheckedChange = { isDarkPurpleTheme = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = MinimalColorPurpleNeon.copy(alpha = 0.7f),
                                uncheckedThumbColor = currentTheme.textMuted, uncheckedTrackColor = currentTheme.barTrackBg
                            ),
                            modifier = Modifier.graphicsLayer(scaleX = 0.6f, scaleY = 0.6f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MinimalHeaderPill(Icons.Rounded.CalendarToday, "May 20, 2026", currentTheme)
                    MinimalHeaderPill(Icons.Rounded.Schedule, "10:58 AM", currentTheme)
                }
            }

            // ============================================================================
            // 📦 SKELETON TWO-CHANNEL LAYOUT GRAPH CHASSIS
            // ============================================================================
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 🔲 LEFT COLUMN CONTAINER (Takes up exactly 64% Screen Width)
                Column(
                    modifier = Modifier.weight(6.4f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Top Massive Process Engine Panel Card
                    MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ShowChart, null, tint = MinimalColorCyanNeon, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Overall Process Lifecycle Engine", color = currentTheme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Real-time telemetry curve analytics and relational juice tracking level outputs", color = currentTheme.textMuted, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Pipeline Graph Line Nodes Row
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.Center)) {
                                    drawLine(
                                        color = currentTheme.glassBorder.copy(alpha = 0.25f),
                                        start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2),
                                        strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    uiState.stages.forEachIndexed { idx, stage ->
                                        MinimalWorkflowNode(idx + 1, stage.name, stage.status, stage.tintColor, stage.currentLevelPct, currentTheme) {
                                            selectedStageIndex = idx
                                            activePopupType = MinimalPopupType.STAGE_DETAIL
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(0.2f))

                            // Waveform Telemetry Chart Core
                            Box(modifier = Modifier.fillMaxWidth().weight(1f).height(140.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val midY = h * 0.55f
                                    val paddingOffset = 16.dp.toPx() + 24.dp.toPx()
                                    val activeWidth = w - (paddingOffset * 2f)
                                    val stepX = activeWidth / 4f
                                    val nodesX = List(5) { i -> paddingOffset + (i * stepX) }

                                    nodesX.forEach { xPos ->
                                        drawLine(
                                            color = currentTheme.glassBorder.copy(alpha=0.12f),
                                            start = Offset(xPos, 0f), end = Offset(xPos, h), strokeWidth = 1.dp.toPx()
                                        )
                                    }

                                    val targetPath = Path().apply { moveTo(0f, midY + 10f); cubicTo(w * 0.3f, midY - 15f, w * 0.7f, midY + 20f, w, midY - 5f) }
                                    drawPath(targetPath, color = currentTheme.textMuted.copy(alpha=0.3f), style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))

                                    val livePoints = listOf(
                                        Offset(0f, midY + 15f), Offset(nodesX[0], midY + 35f + sin(waveOffset) * 4f), Offset(nodesX[1], midY - 50f + sin(waveOffset + 1f) * 5f),
                                        Offset(nodesX[2], midY + 25f + sin(waveOffset + 2f) * 5f), Offset(nodesX[3], midY - 70f + sin(waveOffset + 3f) * 5f),
                                        Offset(nodesX[4], midY + 60f + sin(waveOffset + 4f) * 5f), Offset(w, midY + 10f)
                                    )
                                    val livePath = Path().apply { drawMinimalSmoothCurve(livePoints) }
                                    drawPath(livePath, MinimalColorOrangeNeon, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

                                    for (i in 0..4) {
                                        val ptY = livePoints[i + 1].y
                                        drawCircle(uiState.stages[i].tintColor.copy(alpha=0.25f), 16f, Offset(nodesX[i], ptY))
                                        drawCircle(if (isDarkPurpleTheme) Color.White else currentTheme.textMain, 5f, Offset(nodesX[i], ptY))
                                    }
                                }

                                Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 28.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    uiState.stages.forEachIndexed { idx, stage -> MinimalEffText("${stage.efficiency.toInt()}%", isHighlight = (idx == 3), currentTheme) }
                                }
                                val legendMuted = currentTheme.textMuted
                                Column(modifier = Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    MinimalLegendItem("AI Prediction", MinimalColorCyanNeon, true, currentTheme)
                                    MinimalLegendItem("Desired Target", legendMuted, true, currentTheme)
                                    MinimalLegendItem("Live Process", MinimalColorOrangeNeon, false, currentTheme)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Integrated Mini Status Blocks
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalInnerStatCard(Icons.Rounded.Layers, "Total Components", "${uiState.stages.size} Active Blocks", "System Healthy", currentTheme = currentTheme)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalInnerStatCard(Icons.Rounded.Autorenew, "Performance Average", "${String.format("%.1f", uiState.stages.map { it.efficiency }.average())}% Efficiency", "Aggregated Realtime", currentTheme = currentTheme)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalInnerStatCard(Icons.Rounded.CheckCircle, "Ecosystem Diagnostic", "Excellent Flow Status", "PLC Nominal", MinimalColorGreenNeon, currentTheme)
                                }
                            }
                        }
                    }

                    // 🎛 court THREE SQUARE METRIC CARDS COMPLIANT WITH THE BOTTOM EDGE PROFILE SKELETON
                    Row(
                        modifier = Modifier.height(125.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: OEE Diagnostic
                        MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.weight(1.0f).fillMaxHeight().clickable { activePopupType = MinimalPopupType.OEE }) {
                            Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.PieChart, null, tint = MinimalColorCyanNeon, modifier = Modifier.size(13.dp))
                                        Text("OEE", color = currentTheme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(currentTheme.barTrackBg)
                                            drawArc(color = MinimalColorCyanNeon, startAngle = -90f, sweepAngle = (uiState.oeePercentage * 3.6f), useCenter = false, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
                                        }
                                        Text("${uiState.oeePercentage}%", color = currentTheme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Column(modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    MinimalOeeMetricRow("Availability", "91%", MinimalColorGreenNeon, currentTheme)
                                    Box(modifier = Modifier.width(85.dp).height(1.dp).background(currentTheme.barTrackBg))
                                    MinimalOeeMetricRow("Performance", "95%", MinimalColorGreenNeon, currentTheme)
                                    Box(modifier = Modifier.width(85.dp).height(1.dp).background(currentTheme.barTrackBg))
                                    MinimalOeeMetricRow("Quality", "99%", MinimalColorGreenNeon, currentTheme)
                                }
                            }
                        }

                        // Card 2: Throughput Output Metric
                        MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.weight(1.1f).fillMaxHeight().clickable { activePopupType = MinimalPopupType.THROUGHPUT }) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Speed, null, tint = MinimalColorCyanNeon, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Throughput Output", color = currentTheme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("${uiState.throughputValue}", color = currentTheme.textMain, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = MinimalFontTelemetryMono)
                                            Text(" kg/hr", color = currentTheme.textMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 2.dp))
                                        }
                                        Text("PLC Live Stream", color = currentTheme.textMuted, fontSize = 9.sp)
                                    }
                                    Canvas(modifier = Modifier.width(90.dp).height(44.dp)) {
                                        uiState.throughputHistory.forEachIndexed { i, h ->
                                            val bw = 4.dp.toPx()
                                            val sp = 3.dp.toPx()
                                            drawRoundRect(MinimalColorCyanNeon, Offset(i * (bw + sp), size.height - (h * size.height)), Size(bw, h * size.height), cornerRadius = CornerRadius(2.dp.toPx()))
                                        }
                                    }
                                }
                            }
                        }

                        // Card 3: Energy Power Draw Gauge
                        MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.weight(1.1f).fillMaxHeight().clickable { activePopupType = MinimalPopupType.ENERGY }) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.FlashOn, null, tint = MinimalColorOrangeNeon, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Energy Consumption", color = currentTheme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Box(modifier = Modifier.size(80.dp, 44.dp).padding(end = 4.dp), contentAlignment = Alignment.BottomCenter) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(currentTheme.barTrackBg, 180f, 180f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                                            drawArc(Brush.horizontalGradient(listOf(MinimalColorGreenNeon, MinimalColorOrangeNeon)), 180f, 180f * 0.55f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                                        }
                                        Text("${uiState.currentEnergyKw} kW", color = currentTheme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                                        Text("Current Draw", color = currentTheme.textMuted, fontSize = 9.sp)
                                        Text("${uiState.currentEnergyKw} kW", color = currentTheme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("↑ 8% vs Target", color = MinimalColorOrangeNeon, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 🚨 RIGHT SCROLLABLE ALERTS STACK COLUMN (36% Width) ---
                Column(
                    modifier = Modifier.weight(3.6f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Alert 1: Critical System Disruption Block
                    MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.fillMaxWidth().clickable { activePopupType = MinimalPopupType.BOTTLENECK }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.WarningAmber, null, tint = MinimalColorOrangeNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Critical Sub-System Alert", color = currentTheme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.background(MinimalColorRedNeon.copy(alpha=0.15f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)) {
                                    Text("CRITICAL", color = MinimalColorRedNeon, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(currentTheme.barTrackBg)
                                        drawArc(MinimalColorOrangeNeon, -90f, 360f * 0.72f, false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                                    }
                                    Text("${uiState.bottleneckPercentage}%", color = currentTheme.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Evaporation Tower Stall", color = MinimalColorOrangeNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Rate Loss Impact: 28 MT/day Deficit", color = currentTheme.textMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Alert 2: Zyren AI Action Recommendation Directives
                    MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.fillMaxWidth().clickable { activePopupType = MinimalPopupType.AI_RECOMMENDATIONS }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Lightbulb, null, tint = MinimalColorCyanNeon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Zyren AI Action Directives", color = currentTheme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = currentTheme.textMuted, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MinimalAiRecommendationItem("1", "Accelerate Mill Extraction Motor Speed", "High", "+32 MT/day", "94%", currentTheme)
                                MinimalAiRecommendationItem("2", "Restrict Primary Steam Valve Opening", "High", "<28 MT/day", "91%", currentTheme)
                            }
                        }
                    }

                    // Alert 3: Asset Infrastructure Ecosystem Health
                    MinimalGlassPanelWrapper(currentTheme, modifier = Modifier.fillMaxWidth().clickable { activePopupType = MinimalPopupType.RESOURCE_HEALTH }) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.HealthAndSafety, null, tint = MinimalColorCyanNeon, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ecosystem Asset Health", color = currentTheme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("91%", color = currentTheme.textMain, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Systems Nominal", color = MinimalColorGreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("3 Pending Units", color = MinimalColorOrangeNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Window: 2 Days Left", color = currentTheme.textMuted, fontSize = 9.sp)
                            }
                        }
                    }

                }
            }
        }

        // --- 🔘 UNIVERSAL CENTRALIZED DIALOG OVERLAY MANAGER ---
        MinimalDashboardDialogOverlay(
            uiState = uiState, popupType = activePopupType, stageIndex = selectedStageIndex,
            currentTheme = currentTheme, onClose = { activePopupType = null }
        )
    }
}

// ============================================================================
// 🧩 LOCALIZED COMPOSABLE UTILITIES DESIGN ATOMIC LAYOUTS
// ============================================================================
@Composable
fun MinimalGlassPanelWrapper(currentTheme: MinimalMorphicThemeConfig, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = MinimalColorShadow, ambientColor = MinimalColorShadow)
            .background(currentTheme.glassFill, RoundedCornerShape(16.dp))
            .border(1.dp, currentTheme.glassBorder, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun MinimalHeaderPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, currentTheme: MinimalMorphicThemeConfig) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(currentTheme.glassFill, RoundedCornerShape(20.dp))
            .border(1.dp, currentTheme.glassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, tint = currentTheme.textMuted, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = currentTheme.textMain, fontSize = 12.sp)
    }
}

@Composable
fun MinimalWorkflowNode(
    step: Int,
    name: String,
    status: String,
    color: Color,
    percentage: Int, // Swapped icon out for dynamic percentage value parameter tracking
    currentTheme: MinimalMorphicThemeConfig,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(20.dp).background(color.copy(alpha=0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Text("$step", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Dynamic Liquid Gauge styling replacing traditional hardware logos
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(currentTheme.glassFill, RoundedCornerShape(23.dp))
                .border(1.5.dp, color.copy(alpha=0.6f), RoundedCornerShape(23.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$percentage%",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = MinimalFontTelemetryMono
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, color = currentTheme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(status, color = if (status == "In Progress") MinimalColorOrangeNeon else currentTheme.textMuted, fontSize = 10.sp)
    }
}

@Composable
fun MinimalEffText(eff: String, isHighlight: Boolean, currentTheme: MinimalMorphicThemeConfig) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Eff.", color = currentTheme.textMuted, fontSize = 10.sp)
        Text(eff, color = if(isHighlight) MinimalColorOrangeNeon else currentTheme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = MinimalFontTelemetryMono)
    }
}

@Composable
fun MinimalLegendItem(label: String, color: Color, isDashed: Boolean, currentTheme: MinimalMorphicThemeConfig) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.width(14.dp).height(2.dp)) {
            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.dp.toPx(), pathEffect = if(isDashed) PathEffect.dashPathEffect(floatArrayOf(5f,5f)) else null)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = currentTheme.textMuted, fontSize = 10.sp)
    }
}

@Composable
fun MinimalInnerStatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, subtitle: String, color: Color = Color.Unspecified, currentTheme: MinimalMorphicThemeConfig) {
    val finalValueColor = if (color == Color.Unspecified) currentTheme.textMain else color
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(currentTheme.glassFill, RoundedCornerShape(12.dp)).border(1.dp, currentTheme.glassBorder, RoundedCornerShape(12.dp)).padding(8.dp)
    ) {
        Box(modifier = Modifier.size(30.dp).background(currentTheme.barTrackBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = currentTheme.textMuted, modifier = Modifier.size(15.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = currentTheme.textMuted, fontSize = 10.sp)
            Text(text = value, color = finalValueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = currentTheme.textMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun MinimalOeeMetricRow(label: String, value: String, color: Color, currentTheme: MinimalMorphicThemeConfig) {
    Row(modifier = Modifier.width(90.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = currentTheme.textMuted, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = currentTheme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Rounded.ArrowUpward, null, tint = color, modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
fun MinimalAiRecommendationItem(num: String, text: String, priority: String, impact1: String, impact2: String, currentTheme: MinimalMorphicThemeConfig) {
    Row(
        modifier = Modifier.fillMaxWidth().background(currentTheme.barTrackBg, RoundedCornerShape(12.dp)).border(1.dp, currentTheme.glassBorder, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp).background(MinimalColorCyanNeon.copy(alpha=0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(num, color = MinimalColorCyanNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = currentTheme.textMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.background(if(priority=="High") ColorRedNeon.copy(alpha=0.15f) else ColorOrangeNeon.copy(alpha=0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(priority, color = if(priority=="High") ColorRedNeon else ColorOrangeNeon, fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(impact1, color = MinimalColorGreenNeon, fontSize = 10.sp)
            Text("Impact $impact2", color = currentTheme.textMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun MinimalDashboardDialogOverlay(
    uiState: MinimalDashboardUiState, popupType: MinimalPopupType?, stageIndex: Int, currentTheme: MinimalMorphicThemeConfig, onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = popupType != null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()
    ) {
        if (popupType != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(currentTheme.systemScrim).clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = currentTheme.popupBg, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(2.dp, MinimalColorCyanNeon.copy(alpha = 0.5f)),
                    shadowElevation = 24.dp, modifier = Modifier.width(340.dp).clickable(enabled = false) {}
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val dialogTitle = when (popupType) {
                                MinimalPopupType.STAGE_DETAIL -> "${uiState.stages[stageIndex].name} Metrics"
                                MinimalPopupType.OEE -> "OEE Diagnostic Report"
                                MinimalPopupType.THROUGHPUT -> "Throughput Matrix Log"
                                MinimalPopupType.ENERGY -> "Power Consumption Log"
                                MinimalPopupType.AI_RECOMMENDATIONS -> "Zyren AI Optimization"
                                MinimalPopupType.BOTTLENECK -> "Bottleneck Breakdown"
                                MinimalPopupType.RESOURCE_HEALTH -> "Asset Ecosystem Health"
                            }
                            Text(text = dialogTitle, color = currentTheme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Box(
                                modifier = Modifier.size(28.dp).background(currentTheme.textMuted.copy(alpha = 0.1f), RoundedCornerShape(14.dp)).clickable { onClose() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Close, null, tint = currentTheme.textMain, modifier = Modifier.size(15.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        when (popupType) {
                            MinimalPopupType.STAGE_DETAIL -> {
                                val stage = uiState.stages[stageIndex]
                                MinimalPopupMetricRowItem("Current State Log", stage.status, stage.tintColor, currentTheme)
                                MinimalPopupMetricRowItem("Chamber Core Temperature", stage.coreTemp, currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Atmospheric Pressure", stage.pressure, currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Calculated Efficiency Yield", "${String.format("%.1f", stage.efficiency)}%", currentTheme.textMain, currentTheme)
                            }
                            MinimalPopupType.OEE -> {
                                MinimalPopupMetricRowItem("Aggregated OEE Status", "${uiState.oeePercentage}%", MinimalColorCyanNeon, currentTheme)
                                MinimalPopupMetricRowItem("Total Operational Availability", "91.2%", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Mechanical Performance Index", "95.4%", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Quality Output Consistency", "99.1%", currentTheme.textMain, currentTheme)
                            }
                            MinimalPopupType.THROUGHPUT -> {
                                MinimalPopupMetricRowItem("Current Flow Rate", "${uiState.throughputValue} kg/hr", MinimalColorCyanNeon, currentTheme)
                                MinimalPopupMetricRowItem("Target Shift Floor Quota", "120 kg/hr", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Peak Surge Throughput Today", "148 kg/hr", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Net Volume Produced Today", "1,248 MT", currentTheme.textMain, currentTheme)
                            }
                            MinimalPopupType.ENERGY -> {
                                MinimalPopupMetricRowItem("Instantaneous Power Draw", "${uiState.currentEnergyKw} kW", MinimalColorOrangeNeon, currentTheme)
                                MinimalPopupMetricRowItem("Cumulative Combined Consumption", "${uiState.totalUsageKwh} kWh", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Calculated Machine Grid Variance", "+${uiState.energyVariancePct}%", MinimalColorRedNeon, currentTheme)
                                MinimalPopupMetricRowItem("Financial Resource Saving (AI)", "₹${uiState.aiSavingRupees}", MinimalColorGreenNeon, currentTheme)
                            }
                            MinimalPopupType.AI_RECOMMENDATIONS -> {
                                MinimalPopupMetricRowItem("Total System Prescriptions", "3 Optimization Tips", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Primary Recommendation", "Increase Mill Core Speed", MinimalColorCyanNeon, currentTheme)
                                MinimalPopupMetricRowItem("Projected Structural Yield", "+32 MT/Shift Volume", MinimalColorGreenNeon, currentTheme)
                                MinimalPopupMetricRowItem("Prediction Probability Confidence", "94.2% Verified Score", currentTheme.textMain, currentTheme)
                            }
                            MinimalPopupType.BOTTLENECK -> {
                                MinimalPopupMetricRowItem("Primary System Blockage", "Evaporation Tower", MinimalColorOrangeNeon, currentTheme)
                                MinimalPopupMetricRowItem("Current Congestion Footprint", "${uiState.bottleneckPercentage}% Intensity", MinimalColorRedNeon, currentTheme)
                                MinimalPopupMetricRowItem("Estimated Rate Deficit Loss", "28 MT / Production Day", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Aggregated Overhead Financial Impact", "₹18,000 / Day Loss", currentTheme.textMuted, currentTheme)
                            }
                            MinimalPopupType.RESOURCE_HEALTH -> {
                                MinimalPopupMetricRowItem("Ecosystem Health Metric", "${uiState.resourceHealthPct}% Structural Integr.", MinimalColorGreenNeon, currentTheme)
                                MinimalPopupMetricRowItem("Mechanical Assets Pending Maintenance", "${uiState.assetsDueService} System Units", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Upcoming Calibration Window", "In 2 Maintenance Days", currentTheme.textMain, currentTheme)
                                MinimalPopupMetricRowItem("Hydraulic Node Grid Stability", "Nominal Balance", currentTheme.textMain, currentTheme)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalPopupMetricRowItem(label: String, value: String, valueColor: Color, currentTheme: MinimalMorphicThemeConfig) {
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(vertical = 5.dp)
            .background(currentTheme.textMuted.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = currentTheme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = MinimalFontTelemetryMono)
    }
}