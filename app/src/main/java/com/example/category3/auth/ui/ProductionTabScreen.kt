package com.example.category3.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.components.RadialAppBar
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════
//  STRICT COLOR PALETTE
// ═══════════════════════════════════════════════════════

object BrandColors {
    val DeepNavy      = Color(0xFF0A0D2F)
    val DarkBlueGray  = Color(0xFF223B57)
    val SteelGray     = Color(0xFF8C929C)
    val LightGray     = Color(0xFFBCBCBF)
    val OffWhite      = Color(0xFFF6F6F7)
    val White         = Color(0xFFFFFFFF)

    val CyanBlue      = Color(0xFF47B3E2)
    val MutedBlue     = Color(0xFF496D89)
    val Teal          = Color(0xFF11CFC9)

    val Orange        = Color(0xFFF68420)
    val SoftOrange    = Color(0xFFD68A51)
}

private object ProductionFrostColors {
    val pageBg            = BrandColors.OffWhite
    val textPrimary       = BrandColors.DeepNavy
    val textSecondary     = BrandColors.DarkBlueGray
    val textTertiary      = BrandColors.SteelGray
    val textDisabled      = BrandColors.LightGray

    val accentBlue        = BrandColors.CyanBlue
    val accentBlueSoft    = BrandColors.CyanBlue.copy(alpha = 0.15f)

    val accentNormal      = BrandColors.Teal
    val accentNormalSoft  = BrandColors.Teal.copy(alpha = 0.15f)

    val accentWarning     = BrandColors.SoftOrange
    val accentWarningSoft = BrandColors.SoftOrange.copy(alpha = 0.15f)

    val accentCritical    = BrandColors.Orange
    val accentCriticalSoft= BrandColors.Orange.copy(alpha = 0.15f)

    val divider           = BrandColors.LightGray.copy(alpha = 0.4f)
    val chipBg            = BrandColors.White
    val chipSelectedBg    = BrandColors.MutedBlue.copy(alpha = 0.12f)
    val cardBorder        = BrandColors.DeepNavy.copy(alpha = 0.08f)
}

// ═══════════════════════════════════════════════════════
//  CLEAN MODERN CARD
// ═══════════════════════════════════════════════════════

@Composable
fun CleanCardPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = BrandColors.DeepNavy.copy(alpha = 0.04f),
                spotColor = BrandColors.DeepNavy.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(BrandColors.White)
            .border(
                width = 1.dp,
                color = ProductionFrostColors.cardBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════

fun resolveKpiIcon(iconName: String?): ImageVector = when (iconName) {
    "speed"         -> Icons.Rounded.Speed
    "air"           -> Icons.Rounded.Air
    "thermostat"    -> Icons.Rounded.Thermostat
    "water_drop"    -> Icons.Rounded.WaterDrop
    "factory"       -> Icons.Rounded.Factory
    "electric_bolt" -> Icons.Rounded.ElectricBolt
    "autorenew"     -> Icons.Rounded.Autorenew
    "monitor_heart" -> Icons.Rounded.MonitorHeart
    else            -> Icons.Rounded.Factory
}

fun frostStatusColor(s: String?) = when (s) {
    "CRITICAL" -> ProductionFrostColors.accentCritical
    "WARNING"  -> ProductionFrostColors.accentWarning
    else       -> ProductionFrostColors.accentNormal
}

fun frostStatusBg(s: String?) = when (s) {
    "CRITICAL" -> ProductionFrostColors.accentCriticalSoft
    "WARNING"  -> ProductionFrostColors.accentWarningSoft
    else       -> ProductionFrostColors.accentNormalSoft
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = 1f
    )
}

private fun gaugeSegmentColor(fraction: Float): Color {
    val amount = fraction.coerceIn(0f, 1f)
    return when {
        amount < 0.5f -> lerpColor(BrandColors.Teal, BrandColors.SoftOrange, amount * 2f)
        else -> lerpColor(BrandColors.SoftOrange, BrandColors.Orange, (amount - 0.5f) * 2f)
    }
}

// ═══════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductionTabScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit,
    initialAlertId: String? = null,
    viewModel: ProductionViewModel = viewModel(factory = ProductionViewModel.provideFactory())
) {
    val kpis by viewModel.kpis.collectAsStateWithLifecycle()
    val activeProdAlerts by viewModel.activeProductionAlerts.collectAsStateWithLifecycle()

    var selectedSection      by remember { mutableStateOf<String?>(null) }
    var selectedAlertDetail  by remember { mutableStateOf<ProductionAlertEvent?>(null) }
    var hasHandledInitial    by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(activeProdAlerts, initialAlertId) {
        if (initialAlertId != null && !hasHandledInitial && activeProdAlerts.isNotEmpty()) {
            activeProdAlerts.find { it.id == initialAlertId }?.let {
                selectedSection = it.stage
                selectedAlertDetail = it
                hasHandledInitial = true
            }
        }
    }

    LaunchedEffect(activeProdAlerts) {
        dashboardViewModel.syncProductionAlerts(activeProdAlerts.map { it.id }.toSet())
    }

    LaunchedEffect(Unit) {
        viewModel.productionAlerts.collect { p ->
            dashboardViewModel.injectProductionAlert(
                AlertData(
                    id = p.id, stage = p.stage, message = p.message, priority = p.priority,
                    type = p.type, description = p.description, sourceRoute = "production_tab",
                    timestamp = p.timestamp, targetSection = p.stage, targetAlertId = p.id,
                    acknowledged = p.acknowledged
                )
            )
        }
    }

    val sections = remember(kpis) { kpis.groupBy { it.section ?: "Overview" } }
    val sectionOrder = listOf("Production", "Milling", "Clarification", "Evaporation", "Concentration")
    val displaySections = remember(selectedSection, sections, sectionOrder) {
        if (selectedSection != null)
            mapOf(selectedSection!! to (sections[selectedSection] ?: emptyList()))
        else
            sectionOrder.filter { sections.containsKey(it) }.associateWith { sections[it]!! }
    }

    val hasAlerts = activeProdAlerts.isNotEmpty()

    Box(Modifier.fillMaxSize().background(ProductionFrostColors.pageBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FrostHeader("CONNECTED")

            ProductionSummaryRow(kpis)

            FrostChipRow(sections, sectionOrder, selectedSection, kpis) { selectedSection = it }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── LEFT PANEL: KPI SECTIONS ──
                FrostKpiPanel(
                    modifier = Modifier
                        .weight(if (hasAlerts) 0.74f else 1f)
                        .fillMaxHeight(),
                    displaySections = displaySections
                )

                // ── RIGHT PANEL: ALERTS ──
                if (hasAlerts) {
                    FrostAlertsPanel(
                        modifier = Modifier.weight(0.26f).fillMaxHeight(),
                        alerts = activeProdAlerts,
                        onAlertClick = { a ->
                            selectedSection = a.stage
                            selectedAlertDetail = a
                        }
                    )
                }
            }
        }

        selectedAlertDetail?.let { alert ->
            FrostAlertDialog(
                alert = alert,
                onDismiss = { selectedAlertDetail = null },
                onAcknowledge = {
                    viewModel.acknowledgeAlert(alert.id)
                    selectedAlertDetail = null
                }
            )
        }

        RadialAppBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
                .zIndex(30f),
            activeSection = "production_tab",
            onActionSelected = onNavigateToScreen
        )
    }
}

// ═══════════════════════════════════════════════════════
//  HEADER
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostHeader(connectionStatus: String) {
    val connColor = ProductionFrostColors.accentNormal

    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ProdMaster",
                color = ProductionFrostColors.textPrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Real-time production monitoring",
                color = ProductionFrostColors.textTertiary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        CleanCardPanel(cornerRadius = 9.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp).zIndex(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(Modifier.size(6.dp).background(connColor, CircleShape))
                Text(
                    text = connectionStatus,
                    color = connColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  SUMMARY ROW & GAUGES
// ═══════════════════════════════════════════════════════

@Composable
private fun ProductionSummaryRow(kpis: List<ProductionKpi>) {
    val globalThroughput = kpis.firstOrNull { it.id == "global_throughput" }
    val targetAchieved = kpis.firstOrNull { it.id == "target_achieved" }
    val overallYield = kpis.firstOrNull { it.id == "overall_yield" }
    val millingThroughput = kpis.firstOrNull { it.id == "milling_throughput" }

    Row(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

        // 1. Global Throughput
        CleanCardPanel(modifier = Modifier.weight(1.45f).fillMaxHeight(), cornerRadius = 16.dp) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 9.dp).zIndex(2f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Global Throughput", color = ProductionFrostColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FrostBadge("LIVE", BrandColors.MutedBlue, BrandColors.MutedBlue.copy(alpha = 0.15f))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(globalThroughput?.displayValue ?: "--", color = ProductionFrostColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                        Text(globalThroughput?.unit ?: "MT/Day", color = ProductionFrostColors.textTertiary, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    }
                }

                Box(modifier = Modifier.width(120.dp).height(50.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val fillPath = Path()
                        val points = listOf(0.4f, 0.3f, 0.6f, 0.5f, 0.8f, 0.6f, 0.9f, 0.7f, 0.85f, 1.0f)
                        val stepX = size.width / (points.size - 1)
                        points.forEachIndexed { index, y ->
                            val px = index * stepX
                            val py = size.height - (y * size.height)
                            if (index == 0) {
                                path.moveTo(px, py)
                                fillPath.moveTo(px, size.height)
                                fillPath.lineTo(px, py)
                            } else {
                                path.lineTo(px, py)
                                fillPath.lineTo(px, py)
                            }
                        }
                        fillPath.lineTo(size.width, size.height)
                        fillPath.close()
                        drawPath(fillPath, Brush.verticalGradient(listOf(ProductionFrostColors.accentBlue.copy(0.15f), Color.Transparent)))
                        drawPath(path, color = ProductionFrostColors.accentBlue, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }

        // 2. Target Achieved (Gauge)
        CleanCardPanel(modifier = Modifier.weight(1f).fillMaxHeight(), cornerRadius = 16.dp) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 9.dp).zIndex(2f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Achieved", color = ProductionFrostColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FrostBadge("GOAL", ProductionFrostColors.accentNormal, ProductionFrostColors.accentNormalSoft)
                }

                val gaugeValue = targetAchieved?.value?.toFloat() ?: 85f
                FrostLoadGauge(
                    value = gaugeValue,
                    maxValue = 100f,
                    unit = "%",
                    modifier = Modifier.width(90.dp).height(54.dp)
                )
            }
        }

        // 3. Overall Yield
        FrostMiniStatCard("Overall Yield", overallYield?.displayValue ?: "--", overallYield?.unit ?: "%", ProductionFrostColors.accentNormal, ProductionFrostColors.accentNormalSoft, Modifier.weight(1f).fillMaxHeight())

        // 4. Milling Throughput
        FrostMiniStatCard("Milling T/P", millingThroughput?.displayValue ?: "--", millingThroughput?.unit ?: "T/hr", ProductionFrostColors.accentBlue, ProductionFrostColors.accentBlueSoft, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun FrostLoadGauge(
    value: Float,
    maxValue: Float,
    unit: String,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (maxValue > 0f) { (value / maxValue).coerceIn(0f, 1f) } else { 0f }
    val animatedProgress by animateFloatAsState(targetValue = targetProgress, animationSpec = tween(900), label = "gauge")

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val segmentCount = 22
            val segmentSweep = 180f / segmentCount
            val gap = 2.2f

            val center = Offset(x = size.width / 2f, y = size.height - 5.dp.toPx())
            val radius = minOf(size.width / 2f - strokeWidth, size.height - 12.dp.toPx()).coerceAtLeast(1f)
            val arcTopLeft = Offset(x = center.x - radius, y = center.y - radius)
            val arcSize = Size(width = radius * 2f, height = radius * 2f)
            val activeSegments = (animatedProgress * segmentCount).roundToInt()

            repeat(segmentCount) { index ->
                val fraction = index.toFloat() / (segmentCount - 1).coerceAtLeast(1)
                val segmentColor = if (index < activeSegments) gaugeSegmentColor(fraction) else ProductionFrostColors.divider
                drawArc(
                    color = segmentColor,
                    startAngle = 180f + index * segmentSweep,
                    sweepAngle = segmentSweep - gap,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            val needleAngle = Math.toRadians((180f + animatedProgress * 180f).toDouble())
            val needleEnd = Offset(
                x = center.x + cos(needleAngle).toFloat() * radius * 0.60f,
                y = center.y + sin(needleAngle).toFloat() * radius * 0.60f
            )

            drawLine(color = ProductionFrostColors.textPrimary, start = center, end = needleEnd, strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = ProductionFrostColors.textPrimary, radius = 3.dp.toPx(), center = center)
            drawCircle(color = BrandColors.White, radius = 1.3.dp.toPx(), center = center)
        }

        Column(modifier = Modifier.padding(bottom = 1.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(String.format("%,.0f", value), color = ProductionFrostColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
            Text(unit, color = ProductionFrostColors.textTertiary, fontSize = 8.sp, lineHeight = 9.sp)
        }
    }
}

@Composable
private fun FrostMiniStatCard(title: String, value: String, unit: String, accent: Color, accentBg: Color, modifier: Modifier = Modifier) {
    CleanCardPanel(modifier, cornerRadius = 16.dp) {
        Column(Modifier.fillMaxSize().padding(14.dp).zIndex(2f), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = ProductionFrostColors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = ProductionFrostColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Spacer(Modifier.width(3.dp))
                Text(unit, color = ProductionFrostColors.textTertiary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            Box(Modifier.background(accentBg, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(title.uppercase(), color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  CHIPS
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrostChipRow(
    sections: Map<String, List<ProductionKpi>>, sectionOrder: List<String>, selectedSection: String?, kpis: List<ProductionKpi>, onSelect: (String?) -> Unit
) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FrostChip("All", selectedSection == null) { onSelect(null) }
        sectionOrder.filter { sections.containsKey(it) }.forEach { sec ->
            val alerts = kpis.count { (it.section ?: "Overview") == sec && it.status?.name != "NORMAL" }
            FrostChip(sec, selectedSection == sec, alerts) { onSelect(if (selectedSection == sec) null else sec) }
        }
    }
}
// ═══════════════════════════════════════════════════════
//  KPI MONITOR (LEFT PANEL)
// ═══════════════════════════════════════════════════════
@Composable
private fun FrostKpiPanel(modifier: Modifier, displaySections: Map<String, List<ProductionKpi>>) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = BrandColors.DeepNavy.copy(alpha = 0.04f),
                spotColor = BrandColors.DeepNavy.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BrandColors.White)
            .border(
                width = 1.dp,
                color = ProductionFrostColors.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            Modifier.fillMaxSize().padding(14.dp).zIndex(2f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("KPI Monitor", color = ProductionFrostColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            displaySections.forEach { (name, kpiList) ->
                if (kpiList.isEmpty()) return@forEach

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(26.dp).background(BrandColors.OffWhite, RoundedCornerShape(7.dp)).border(1.dp, ProductionFrostColors.cardBorder, RoundedCornerShape(7.dp)), Alignment.Center) {
                            Icon(sectionIcon(name), null, tint = ProductionFrostColors.textSecondary, modifier = Modifier.size(14.dp))
                        }
                        Text(name, color = ProductionFrostColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    val crit = kpiList.count { it.status?.name == "CRITICAL" }
                    val warn = kpiList.count { it.status?.name == "WARNING" }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (crit > 0) FrostBadge("$crit CRIT", ProductionFrostColors.accentCritical, ProductionFrostColors.accentCriticalSoft)
                        if (warn > 0) FrostBadge("$warn WARN", ProductionFrostColors.accentWarning, ProductionFrostColors.accentWarningSoft)
                        if (crit == 0 && warn == 0) FrostBadge("OK", ProductionFrostColors.accentNormal, ProductionFrostColors.accentNormalSoft)
                    }
                }

                kpiList.chunked(3).forEach { rowKpis ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKpis.forEach { kpi ->
                            FrostKpiCard(kpi = kpi, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(ProductionFrostColors.divider))
            }
        }
    }
}
// ═══════════════════════════════════════════════════════
//  KPI CARD
// ═══════════════════════════════════════════════════════
@Composable
fun FrostKpiCard(kpi: ProductionKpi, modifier: Modifier = Modifier) {
    val statusName = kpi.status?.name ?: "NORMAL"
    val stCol by animateColorAsState(frostStatusColor(statusName), tween(500), label = "c")
    val stBg  by animateColorAsState(frostStatusBg(statusName), tween(500), label = "b")

    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.OffWhite.copy(alpha = 0.75f),
                        BrandColors.SteelGray.copy(alpha = 0.08f)
                    )
                )
            )
            .background(stBg.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.White.copy(alpha = 0.6f),
                        stCol.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(24.dp).background(BrandColors.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp)), Alignment.Center) {
                        Icon(resolveKpiIcon("factory"), null, tint = stCol, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        kpi.title, color = ProductionFrostColors.textPrimary, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(Modifier.size(16.dp).background(BrandColors.White.copy(alpha = 0.6f), CircleShape), Alignment.Center) {
                    Icon(
                        if (statusName != "NORMAL") Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                        null, tint = stCol.copy(if (statusName == "NORMAL") 0.6f else 1f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    kpi.displayValue, color = if (statusName != "NORMAL") stCol else ProductionFrostColors.textPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
                )
                Text(kpi.unit, color = ProductionFrostColors.textTertiary, fontSize = 9.sp, modifier = Modifier.padding(bottom = 2.dp))
            }

            val kpiValueFloat = kpi.value.toFloat()
            val denom = if (kpiValueFloat > 0f) kpiValueFloat * 1.5f else 1f
            val progressRatio = (kpiValueFloat / denom).coerceIn(0f, 1f)
            val animProgress by animateFloatAsState(targetValue = progressRatio, tween(800), label = "p")

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Current Load", color = ProductionFrostColors.textTertiary, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                    Text("${(progressRatio * 100).toInt()}%", color = stCol, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                    drawRoundRect(color = BrandColors.White.copy(alpha = 0.5f), size = size, cornerRadius = CornerRadius(2.dp.toPx()))
                    drawRoundRect(
                        color = stCol,
                        size = size.copy(width = size.width * animProgress),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(ProductionFrostColors.divider))
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("7-Period Trend", color = ProductionFrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(6.dp))
                    FrostDynamicChart(kpi = kpi, color = stCol)
                }
            }
        }
    }
}
// ═══════════════════════════════════════════════════════
//  DYNAMIC CHART ROUTER
// ═══════════════════════════════════════════════════════
@Composable
fun FrostDynamicChart(kpi: ProductionKpi, color: Color) {
    val unitLower = kpi.unit.lowercase()

    when {
        // Volume/Mass/Power -> Bar Chart
        unitLower.contains("kw") || unitLower.contains("mw") || unitLower.contains("mt") || unitLower.contains("t/h") || unitLower.contains("kg") -> {
            FrostGlassBarChart(kpi, color)
        }
        // Environment/Continuous Metrics -> Area Chart
        unitLower.contains("c") || unitLower.contains("bar") || unitLower.contains("v") || unitLower.contains("hz") -> {
            FrostGlassAreaChart(kpi, color)
        }
        // Percentages/Efficiencies -> Step Chart
        unitLower.contains("%") -> {
            FrostGlassStepChart(kpi, color)
        }
        // Speed/Flow Rate (RPM, m/s) -> Lollipop Chart (Fallback)
        else -> {
            FrostGlassLollipopChart(kpi, color)
        }
    }
}

// ═══════════════════════════════════════════════════════
//  1. GLASS BAR CHART
// ═══════════════════════════════════════════════════════
@Composable
fun FrostGlassBarChart(kpi: ProductionKpi, color: Color) {
    val dataPointsCount = 7
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value.toFloat()
        val variance = base * 0.15f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2f)
        } + base
    }

    val maxData = historicalData.maxOrNull() ?: 1f
    val maxVal = (maxData * 1.15f).coerceAtLeast(1f)

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            historicalData.forEachIndexed { index, value ->
                val fraction = if (maxVal > 0f) (value / maxVal) else 0f
                val isCurrent = index == historicalData.size - 1
                val animFraction by animateFloatAsState(targetValue = fraction, tween(700), label = "b")

                Box(modifier = Modifier.weight(1f).padding(horizontal = 2.5.dp).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animFraction.coerceAtLeast(0.06f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        BrandColors.White.copy(alpha = if (isCurrent) 0.6f else 0.2f),
                                        color.copy(alpha = if (isCurrent) 0.7f else 0.2f),
                                        color.copy(alpha = if (isCurrent) 0.3f else 0.05f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        BrandColors.White.copy(alpha = 0.9f),
                                        BrandColors.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
    }
}
// ═══════════════════════════════════════════════════════
//  2. SMOOTH AREA CHART
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassAreaChart(kpi: ProductionKpi, color: Color) {
    val dataPointsCount = 8
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value.toFloat()
        val variance = base * 0.08f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2f)
        } + base
    }

    val maxData = historicalData.maxOrNull() ?: 1f
    val minData = historicalData.minOrNull() ?: 0f
    val range = (maxData - minData).coerceAtLeast(1f)
    val maxVal = maxData + range * 0.1f
    val minVal = (minData - range * 0.1f).coerceAtLeast(0f)

    val revealFraction by animateFloatAsState(targetValue = 1f, tween(1000), label = "a")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)
            val path = Path()
            val fillPath = Path()

            var prevX = 0f
            var prevY = size.height - ((historicalData[0] - minVal) / (maxVal - minVal) * size.height) * revealFraction

            path.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, size.height)
            fillPath.lineTo(prevX, prevY)

            for (i in 1 until historicalData.size) {
                val curX = i * step
                val curY = size.height - ((historicalData[i] - minVal) / (maxVal - minVal) * size.height) * revealFraction

                val cpX = (prevX + curX) / 2f
                path.cubicTo(cpX, prevY, cpX, curY, curX, curY)
                fillPath.cubicTo(cpX, prevY, cpX, curY, curX, curY)

                prevX = curX
                prevY = curY
            }

            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.4f), Color.Transparent)
                )
            )
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(prevX, prevY))
            drawCircle(color = BrandColors.White, radius = 1.dp.toPx(), center = Offset(prevX, prevY))
        }
    }
}
// ═══════════════════════════════════════════════════════
//  3. STEP CHART
// ═══════════════════════════════════════════════════════
@Composable
fun FrostGlassStepChart(kpi: ProductionKpi, color: Color) {
    val dataPointsCount = 7
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value.toFloat()
        val variance = base * 0.1f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2f)
        } + base
    }

    val maxVal = 100f
    val revealFraction by animateFloatAsState(targetValue = 1f, tween(900), label = "s")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)
            val path = Path()
            val fillPath = Path()

            var prevX = 0f
            var prevY = size.height - (historicalData[0] / maxVal * size.height) * revealFraction

            path.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, size.height)
            fillPath.lineTo(prevX, prevY)

            for (i in 1 until historicalData.size) {
                val curX = i * step
                val curY = size.height - (historicalData[i] / maxVal * size.height) * revealFraction

                path.lineTo(curX, prevY)
                path.lineTo(curX, curY)

                fillPath.lineTo(curX, prevY)
                fillPath.lineTo(curX, curY)

                prevX = curX
                prevY = curY
            }

            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.05f))
                )
            )
            drawPath(
                path = path,
                color = color.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            drawRect(
                color = BrandColors.White.copy(alpha = 0.2f),
                topLeft = Offset(prevX - step, prevY),
                size = Size(step, size.height - prevY)
            )
        }
    }
}
// ═══════════════════════════════════════════════════════
//  4. LOLLIPOP CHART
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassLollipopChart(kpi: ProductionKpi, color: Color) {
    val dataPointsCount = 8
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value.toFloat()
        val variance = base * 0.25f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2f)
        } + base
    }

    val maxData = historicalData.maxOrNull() ?: 1f
    val maxVal = (maxData * 1.1f).coerceAtLeast(1f)

    val revealFraction by animateFloatAsState(targetValue = 1f, tween(1100), label = "l")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)

            historicalData.forEachIndexed { i, value ->
                val x = i * step
                val targetY = size.height - (value / maxVal * size.height)
                val currentY = size.height - ((size.height - targetY) * revealFraction)
                val isCurrent = i == historicalData.size - 1

                drawLine(
                    color = BrandColors.LightGray.copy(alpha = if (isCurrent) 0.6f else 0.3f),
                    start = Offset(x, size.height),
                    end = Offset(x, currentY),
                    strokeWidth = if (isCurrent) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = color.copy(alpha = if (isCurrent) 1f else 0.6f),
                    radius = if (isCurrent) 4.5.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(x, currentY)
                )

                if (isCurrent) {
                    drawCircle(
                        color = BrandColors.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, currentY)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  ALERTS PANEL (RIGHT PANEL)
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostAlertsPanel(
    modifier: Modifier,
    alerts: List<ProductionAlertEvent>,
    onAlertClick: (ProductionAlertEvent) -> Unit
) {
    val sortedAlerts = remember(alerts) {
        alerts.sortedWith(
            compareBy<ProductionAlertEvent> { it.acknowledged }
                .thenBy {
                    when (it.priority) {
                        "CRITICAL" -> 0
                        "WARNING" -> 1
                        else -> 2
                    }
                }
        )
    }

    val activeCount = alerts.count { !it.acknowledged }

    CleanCardPanel(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(ProductionFrostColors.accentWarningSoft, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = ProductionFrostColors.accentWarning,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text("Active Alerts Log", color = ProductionFrostColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Synced via Digital Twin API", color = ProductionFrostColors.textTertiary, fontSize = 9.sp)
                    }
                }
                Text("$activeCount Active", color = ProductionFrostColors.accentWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sortedAlerts.forEach { alert ->
                    FrostAlertLogRow(alert = alert, onClick = { onAlertClick(alert) })
                }
            }
        }
    }
}
@Composable
private fun FrostAlertLogRow(
    alert: ProductionAlertEvent,
    onClick: () -> Unit
) {
    val acknowledged = alert.acknowledged

    val severityColor = when {
        acknowledged -> ProductionFrostColors.textDisabled
        alert.priority == "CRITICAL" -> ProductionFrostColors.accentCritical
        alert.priority == "WARNING" -> ProductionFrostColors.accentWarning
        else -> ProductionFrostColors.accentBlue
    }

    val rowShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(
                brush = if (acknowledged) {
                    SolidColor(BrandColors.OffWhite)
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            severityColor.copy(alpha = 0.08f),
                            BrandColors.White
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (acknowledged) ProductionFrostColors.divider else severityColor.copy(alpha = 0.20f),
                shape = rowShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            imageVector = if (acknowledged) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = severityColor,
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = alert.message,
                color = if (acknowledged) ProductionFrostColors.textDisabled else ProductionFrostColors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${alert.stage} · ${alert.priority}",
                color = severityColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
// ═══════════════════════════════════════════════════════
//  ALERT DIALOG
// ═══════════════════════════════════════════════════════
@Composable
private fun FrostAlertDialog(
    alert: ProductionAlertEvent,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().background(ProductionFrostColors.textPrimary.copy(0.4f))
                .clickable(remember { MutableInteractionSource() }, null) { onDismiss() },
            Alignment.Center
        ) {
            val aColor = when (alert.priority) {
                "CRITICAL" -> ProductionFrostColors.accentCritical; "WARNING" -> ProductionFrostColors.accentWarning; else -> ProductionFrostColors.accentBlue
            }
            val aBg = when (alert.priority) {
                "CRITICAL" -> ProductionFrostColors.accentCriticalSoft; "WARNING" -> ProductionFrostColors.accentWarningSoft; else -> ProductionFrostColors.accentBlueSoft
            }

            CleanCardPanel(
                Modifier.width(420.dp).clickable(remember { MutableInteractionSource() }, null) {},
                cornerRadius = 22.dp
            ) {
                Column(Modifier.padding(24.dp).zIndex(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(40.dp).background(aBg, RoundedCornerShape(12.dp)), Alignment.Center) {
                                Icon(Icons.Rounded.WarningAmber, null, tint = aColor, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text("PRODUCTION ALERT", color = aColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(alert.stage, color = ProductionFrostColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Box(Modifier.size(32.dp).background(BrandColors.OffWhite, CircleShape), Alignment.Center) {
                                Icon(Icons.Rounded.Close, null, tint = ProductionFrostColors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(alert.message, color = ProductionFrostColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                    Column(
                        Modifier.fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(aBg, aBg.copy(0.3f))), RoundedCornerShape(12.dp))
                            .border(1.dp, aColor.copy(0.2f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Threshold Details", color = aColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Column {
                                Text("Threshold Limit", color = ProductionFrostColors.textTertiary, fontSize = 10.sp)
                                Text(String.format("%.2f", alert.thresholdValue), color = ProductionFrostColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Current Reading", color = ProductionFrostColors.textTertiary, fontSize = 10.sp)
                                Text(String.format("%.2f", alert.currentValue), color = aColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(alert.description, color = ProductionFrostColors.textSecondary, fontSize = 12.sp, lineHeight = 18.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp)).background(BrandColors.OffWhite)
                                .clickable { onDismiss() }.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) { Text("Cancel", color = ProductionFrostColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp))
                                .background(aColor)
                                .clickable { onAcknowledge() }.padding(horizontal = 20.dp, vertical = 10.dp)
                        ) { Text("Acknowledge", color = BrandColors.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}