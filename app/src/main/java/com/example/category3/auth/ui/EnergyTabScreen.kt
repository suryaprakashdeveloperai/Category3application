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
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.graphics.PathEffect
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
//  RESTRICTED THEME PALETTE
// ═══════════════════════════════════════════════════════

object FrostColors {
    val deepNavy       = Color(0xFF0A0D2F)
    val darkBlueGray   = Color(0xFF223B57)
    val steelGray      = Color(0xFF8C929C)
    val lightGray      = Color(0xFFBCBCBF)
    val offWhite       = Color(0xFFF6F6F7)

    val cyanBlue       = Color(0xFF47B3E2)
    val mutedBlue      = Color(0xFF496D89)
    val teal           = Color(0xFF11CFC9)
    val orange         = Color(0xFFF68420)
    val softOrange     = Color(0xFFD68A55)

    val pageBg            = offWhite
    val textPrimary       = deepNavy
    val textSecondary     = darkBlueGray
    val textTertiary      = steelGray
    val textDisabled      = lightGray

    val accentBlue        = cyanBlue
    val accentBlueSoft    = cyanBlue.copy(alpha = 0.15f)

    val accentGreen       = teal // Normal
    val accentGreenSoft   = teal.copy(alpha = 0.15f)
    val accentOrange      = softOrange // Warning
    val accentOrangeSoft  = softOrange.copy(alpha = 0.15f)
    val accentRed         = orange // Critical
    val accentRedSoft     = orange.copy(alpha = 0.15f)

    val chartLine         = mutedBlue
    val chartGradTop      = mutedBlue.copy(alpha = 0.4f)
    val chartGradBottom   = Color.Transparent

    val divider           = lightGray.copy(alpha = 0.4f)
    val chipBg            = lightGray.copy(alpha = 0.2f)
    val chipSelectedBg    = cyanBlue.copy(alpha = 0.15f)
}

// ═══════════════════════════════════════════════════════
//  GLASS PANEL (USING RESTRICTED COLORS)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = FrostColors.deepNavy.copy(alpha = 0.15f),
                spotColor = FrostColors.deepNavy.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        FrostColors.offWhite.copy(alpha = 0.95f),
                        FrostColors.lightGray.copy(alpha = 0.3f),
                        FrostColors.lightGray.copy(alpha = 0.1f),
                        FrostColors.offWhite.copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        FrostColors.offWhite.copy(alpha = 0.9f),
                        FrostColors.offWhite.copy(alpha = 0.3f),
                        FrostColors.deepNavy.copy(alpha = 0.05f),
                        FrostColors.darkBlueGray.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(FrostColors.offWhite.copy(alpha = 0.6f), Color.Transparent),
                        radius = 800f
                    )
                )
        )
        content()
    }
}

// ═══════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════

fun resolveKpiIcon(iconName: String): ImageVector = when (iconName) {
    "speed"         -> Icons.Rounded.Speed
    "air"           -> Icons.Rounded.Air
    "thermostat"    -> Icons.Rounded.Thermostat
    "water_drop"    -> Icons.Rounded.WaterDrop
    "factory"       -> Icons.Rounded.Factory
    "electric_bolt" -> Icons.Rounded.ElectricBolt
    "autorenew"     -> Icons.Rounded.Autorenew
    "monitor_heart" -> Icons.Rounded.MonitorHeart
    else            -> Icons.Rounded.ElectricBolt
}

fun frostStatusColor(s: KpiStatus) = when (s) {
    KpiStatus.NORMAL   -> FrostColors.accentGreen
    KpiStatus.WARNING  -> FrostColors.accentOrange
    KpiStatus.CRITICAL -> FrostColors.accentRed
}

fun frostStatusBg(s: KpiStatus) = when (s) {
    KpiStatus.NORMAL   -> FrostColors.accentGreenSoft
    KpiStatus.WARNING  -> FrostColors.accentOrangeSoft
    KpiStatus.CRITICAL -> FrostColors.accentRedSoft
}

fun sectionIcon(name: String): ImageVector = when (name) {
    "Boiler"        -> Icons.Outlined.Sensors
    "Milling"       -> Icons.Rounded.Factory
    "Evaporation"   -> Icons.Rounded.Thermostat
    "Concentration" -> Icons.Rounded.Speed
    "Pumps"         -> Icons.Rounded.WaterDrop
    "Power Quality" -> Icons.Outlined.ElectricBolt
    "Production"    -> Icons.Rounded.Factory
    else            -> Icons.Outlined.Sensors
}

private fun gaugeSegmentColor(fraction: Float): Color {
    val amount = fraction.coerceIn(0f, 1f)
    return Color(
        red = FrostColors.accentGreen.red + (FrostColors.accentRed.red - FrostColors.accentGreen.red) * amount,
        green = FrostColors.accentGreen.green + (FrostColors.accentRed.green - FrostColors.accentGreen.green) * amount,
        blue = FrostColors.accentGreen.blue + (FrostColors.accentRed.blue - FrostColors.accentGreen.blue) * amount,
        alpha = 1f
    )
}

// ═══════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnergyTabScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit,
    initialSection: String? = null,
    initialAlertId: String? = null,
    energyViewModel: EnergyViewModel = viewModel(factory = EnergyViewModel.provideFactory())
) {
    val kpis              by energyViewModel.kpis.collectAsStateWithLifecycle()
    val totalPower        by energyViewModel.totalPowerKw.collectAsStateWithLifecycle()
    val peakDemand        by energyViewModel.peakDemandKw.collectAsStateWithLifecycle()
    val minLoad           by energyViewModel.minLoadKw.collectAsStateWithLifecycle()
    val trend             by energyViewModel.efficiencyTrend.collectAsStateWithLifecycle()
    val connectionStatus  by energyViewModel.connectionStatus.collectAsStateWithLifecycle()
    val energyAlerts      by energyViewModel.activeEnergyAlerts.collectAsStateWithLifecycle()

    var selectedSection      by remember { mutableStateOf(initialSection) }
    var selectedAlertDetail  by remember { mutableStateOf<EnergyAlertEvent?>(null) }
    var hasHandledInitial    by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(energyAlerts, initialAlertId) {
        if (initialAlertId != null && !hasHandledInitial && energyAlerts.isNotEmpty()) {
            energyAlerts.find { it.id == initialAlertId }?.let {
                selectedSection = it.stage
                selectedAlertDetail = it
                hasHandledInitial = true
            }
        }
    }

    val sections = remember(kpis) { kpis.groupBy { it.section } }
    val sectionOrder = listOf(
        "Power Quality", "Boiler", "Milling", "Evaporation",
        "Concentration", "Pumps", "Production"
    )
    val displaySections = remember(selectedSection, sections, sectionOrder) {
        if (selectedSection != null)
            mapOf(selectedSection!! to (sections[selectedSection] ?: emptyList()))
        else
            sectionOrder.filter { sections.containsKey(it) }.associateWith { sections[it]!! }
    }
    val hasAlerts = energyAlerts.isNotEmpty()

    Box(Modifier.fillMaxSize().background(FrostColors.pageBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FrostHeader(connectionStatus)
            FrostSummaryRow(totalPower, peakDemand, minLoad, trend, kpis)
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
                        alerts = energyAlerts,
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
                    energyViewModel.acknowledgeAlert(alert.id)
                    selectedAlertDetail = null
                }
            )
        }

        RadialAppBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
                .zIndex(30f),
            activeSection = "energy_tab",
            onActionSelected = onNavigateToScreen
        )
    }
}

// ═══════════════════════════════════════════════════════
//  HEADER
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostHeader(connectionStatus: String) {
    val connColor = when (connectionStatus) {
        "CONNECTED"    -> FrostColors.accentGreen
        "RECONNECTING" -> FrostColors.accentOrange
        else           -> FrostColors.textDisabled
    }

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
                text = "EnergyWise",
                color = FrostColors.textPrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Real-time plant monitoring",
                color = FrostColors.textTertiary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        FrostGlassPanel(cornerRadius = 9.dp) {
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
private fun FrostSummaryRow(
    totalPower: Float, peakDemand: Float, minLoad: Float, trend: Float, kpis: List<EnergyKpi>
) {
    val animTotal by animateFloatAsState(totalPower, tween(800), label = "t")
    val animPeak  by animateFloatAsState(peakDemand, tween(800), label = "p")
    val animMin   by animateFloatAsState(if (minLoad == Float.MAX_VALUE) 0f else minLoad, tween(800), label = "m")

    val livePowerFeeds = remember(kpis) { kpis.count { it.unit.equals("kW", ignoreCase = true) } }
    val gaugeMaximum = maxOf(animPeak * 1.12f, animTotal * 1.05f, 1f)
    val peakPercentage = if (animPeak > 0f) { (animTotal / animPeak * 100f).roundToInt() } else { 0 }

    Row(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

        // 1. Total Plant Load Card
        FrostGlassPanel(modifier = Modifier.weight(1.45f).fillMaxHeight(), cornerRadius = 16.dp) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 9.dp).zIndex(2f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Plant Load", color = FrostColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FrostBadge("LIVE", FrostColors.accentGreen, FrostColors.accentGreenSoft)
                    Column {
                        Text("$peakPercentage% of recorded peak", color = FrostColors.accentOrange, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text("$livePowerFeeds live power feeds", color = FrostColors.textTertiary, fontSize = 8.sp)
                    }
                }
                FrostLoadGauge(
                    value = animTotal,
                    maxValue = gaugeMaximum,
                    modifier = Modifier.width(145.dp).height(74.dp)
                )
            }
        }

        // 2. Peak Demand
        FrostMiniStatCard("Peak Demand", String.format("%,.0f", animPeak), "kW", FrostColors.accentRed, FrostColors.accentRedSoft, Modifier.weight(1f).fillMaxHeight())

        // 3. Min Load
        FrostMiniStatCard("Min Load", String.format("%,.0f", animMin), "kW", FrostColors.accentGreen, FrostColors.accentGreenSoft, Modifier.weight(1f).fillMaxHeight())

        // 4. Efficiency Trend
        FrostGlassPanel(Modifier.weight(1f).fillMaxHeight(), cornerRadius = 16.dp) {
            Column(Modifier.fillMaxSize().padding(14.dp).zIndex(2f), verticalArrangement = Arrangement.SpaceBetween) {
                Text("Efficiency", color = FrostColors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                val tCol = if (trend >= 0) FrostColors.accentGreen else FrostColors.accentRed
                val tBg  = if (trend >= 0) FrostColors.accentGreenSoft else FrostColors.accentRedSoft
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(28.dp).background(tBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(if (trend >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown, null, tint = tCol, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text("${String.format("%.1f", trend)}%", color = tCol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("vs last period", color = FrostColors.textTertiary, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FrostLoadGauge(
    value: Float,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (maxValue > 0f) { (value / maxValue).coerceIn(0f, 1f) } else { 0f }
    val animatedProgress by animateFloatAsState(targetValue = targetProgress, animationSpec = tween(900), label = "plantLoadGauge")

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
                val segmentColor = if (index < activeSegments) gaugeSegmentColor(fraction) else FrostColors.divider
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

            drawLine(color = FrostColors.textPrimary, start = center, end = needleEnd, strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color = FrostColors.textPrimary, radius = 3.dp.toPx(), center = center)
            drawCircle(color = FrostColors.offWhite, radius = 1.3.dp.toPx(), center = center)
        }

        Column(modifier = Modifier.padding(bottom = 1.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(String.format("%,.0f", value), color = FrostColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
            Text("kW", color = FrostColors.textTertiary, fontSize = 8.sp, lineHeight = 9.sp)
        }
    }
}

@Composable
private fun FrostMiniStatCard(title: String, value: String, unit: String, accent: Color, accentBg: Color, modifier: Modifier = Modifier) {
    FrostGlassPanel(modifier, cornerRadius = 16.dp) {
        Column(Modifier.fillMaxSize().padding(14.dp).zIndex(2f), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = FrostColors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = FrostColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Spacer(Modifier.width(3.dp))
                Text(unit, color = FrostColors.textTertiary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
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
    sections: Map<String, List<EnergyKpi>>, sectionOrder: List<String>, selectedSection: String?, kpis: List<EnergyKpi>, onSelect: (String?) -> Unit
) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FrostChip("All", selectedSection == null) { onSelect(null) }
        sectionOrder.filter { sections.containsKey(it) }.forEach { sec ->
            val alerts = kpis.count { it.section == sec && it.status != KpiStatus.NORMAL }
            FrostChip(sec, selectedSection == sec, alerts) { onSelect(if (selectedSection == sec) null else sec) }
        }
    }
}

@Composable
fun FrostChip(label: String, isSelected: Boolean, alertCount: Int = 0, onClick: () -> Unit) {
    val bg = if (isSelected) FrostColors.chipSelectedBg else FrostColors.chipBg
    val txt = if (isSelected) FrostColors.accentBlue else FrostColors.textSecondary
    val bdr = if (isSelected) FrostColors.accentBlue.copy(0.4f) else FrostColors.offWhite.copy(0.3f)
    Row(
        Modifier.clip(RoundedCornerShape(8.dp)).background(bg).border(1.dp, bdr, RoundedCornerShape(8.dp))
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }.padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = txt, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium)
        if (alertCount > 0) {
            Box(Modifier.size(16.dp).background(FrostColors.accentRed, CircleShape), contentAlignment = Alignment.Center) {
                Text("$alertCount", color = FrostColors.offWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  KPI MONITOR (LEFT PANEL)
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostKpiPanel(modifier: Modifier, displaySections: Map<String, List<EnergyKpi>>) {
    FrostGlassPanel(modifier) {
        Column(
            Modifier.fillMaxSize().padding(14.dp).zIndex(2f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("KPI Monitor", color = FrostColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            displaySections.forEach { (name, kpiList) ->
                if (kpiList.isEmpty()) return@forEach

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(26.dp).background(FrostColors.chipBg, RoundedCornerShape(7.dp)), Alignment.Center) {
                            Icon(sectionIcon(name), null, tint = FrostColors.textSecondary, modifier = Modifier.size(14.dp))
                        }
                        Text(name, color = FrostColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    val crit = kpiList.count { it.status == KpiStatus.CRITICAL }
                    val warn = kpiList.count { it.status == KpiStatus.WARNING }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (crit > 0) FrostBadge("$crit CRIT", FrostColors.accentRed, FrostColors.accentRedSoft)
                        if (warn > 0) FrostBadge("$warn WARN", FrostColors.accentOrange, FrostColors.accentOrangeSoft)
                        if (crit == 0 && warn == 0) FrostBadge("OK", FrostColors.accentGreen, FrostColors.accentGreenSoft)
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
                Box(Modifier.fillMaxWidth().height(1.dp).background(FrostColors.divider))
            }
        }
    }
}

@Composable
fun FrostBadge(text: String, textColor: Color, bgColor: Color) {
    Text(
        text, color = textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp,
        modifier = Modifier.background(bgColor, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// ═══════════════════════════════════════════════════════
//  KPI CARD (INTERACTIVE)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostKpiCard(kpi: EnergyKpi, modifier: Modifier = Modifier) {
    val stCol by animateColorAsState(frostStatusColor(kpi.status), tween(500), label = "c")
    val stBg  by animateColorAsState(frostStatusBg(kpi.status), tween(500), label = "b")

    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(colors = listOf(FrostColors.offWhite.copy(0.95f), FrostColors.lightGray.copy(0.6f))))
            .border(1.dp, Brush.linearGradient(listOf(FrostColors.offWhite, FrostColors.deepNavy.copy(0.1f))), RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(24.dp).background(stBg, RoundedCornerShape(6.dp)), Alignment.Center) {
                        Icon(resolveKpiIcon(kpi.iconName), null, tint = stCol, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        kpi.title, color = FrostColors.textPrimary, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(Modifier.size(16.dp).background(stBg, CircleShape), Alignment.Center) {
                    Icon(
                        if (kpi.status != KpiStatus.NORMAL) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                        null, tint = stCol.copy(if (kpi.status == KpiStatus.NORMAL) 0.6f else 1f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // Value & Unit Row
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    kpi.displayValue, color = if (kpi.status != KpiStatus.NORMAL) stCol else FrostColors.textPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
                )
                Text(kpi.unit, color = FrostColors.textTertiary, fontSize = 9.sp, modifier = Modifier.padding(bottom = 2.dp))
            }

            // Linear Progress Bar Visualization
            if (kpi.threshold != null) {
                val progressRatio = (kpi.value / kpi.threshold).toFloat()
                val animProgress by animateFloatAsState(
                    targetValue = progressRatio.coerceIn(0f, 1f), tween(800), label = "p"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val lbl = when (kpi.thresholdType) {
                            ThresholdType.ABOVE -> "Max ${kpi.threshold.toInt()}"
                            ThresholdType.BELOW -> "Min ${kpi.threshold.toInt()}"
                        }
                        Text(lbl, color = FrostColors.textTertiary, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                        Text("${(progressRatio * 100).toInt()}%", color = stCol, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                        drawRoundRect(color = FrostColors.divider, size = size, cornerRadius = CornerRadius(2.dp.toPx()))
                        drawRoundRect(
                            color = stCol,
                            size = size.copy(width = size.width * animProgress),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }
            }

            // Dynamic Visualizations mapped by Unit
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(FrostColors.divider))
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("7-Period Trend", color = FrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        if (kpi.threshold != null) {
                            Text("Limit: ${kpi.threshold}", color = FrostColors.accentRed.copy(0.8f), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                        }
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
fun FrostDynamicChart(kpi: EnergyKpi, color: Color) {
    val unitLower = kpi.unit.lowercase()

    when {
        // Power metrics -> Glass Bar Chart
        unitLower.contains("kw") || unitLower.contains("mw") -> {
            FrostGlassBarChart(kpi, color)
        }
        // Environment/Continuous Metrics -> Smooth Area Chart
        unitLower.contains("c") || unitLower.contains("bar") || unitLower.contains("v") || unitLower.contains("hz") -> {
            FrostGlassAreaChart(kpi, color)
        }
        // Percentages/Efficiencies -> Step Chart
        unitLower.contains("%") -> {
            FrostGlassStepChart(kpi, color)
        }
        // Flow/Speed (RPM, kg/s, t/h) -> Lollipop Chart
        else -> {
            FrostGlassLollipopChart(kpi, color)
        }
    }
}

// ═══════════════════════════════════════════════════════
//  1. GLASS BAR CHART (For Power/Volumes)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassBarChart(kpi: EnergyKpi, color: Color) {
    val dataPointsCount = 7
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value
        val variance = base * 0.15f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2)
        } + kpi.value
    }

    val thresholdVal = kpi.threshold ?: 0f
    val maxData = historicalData.maxOrNull() ?: 1f
    val maxVal = maxOf(maxData, thresholdVal.toFloat()) * 1.15f

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        if (kpi.threshold != null && maxVal > 0f) {
            val yOffset = 1f - (kpi.threshold.toFloat() / maxVal)
            Canvas(Modifier.fillMaxSize()) {
                drawLine(
                    color = FrostColors.accentRed.copy(alpha = 0.6f),
                    start = Offset(0f, size.height * yOffset),
                    end = Offset(size.width, size.height * yOffset),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            historicalData.forEachIndexed { index, value ->
                val fraction = if (maxVal > 0f) (value / maxVal).toFloat() else 0f
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
                                        FrostColors.offWhite.copy(alpha = if (isCurrent) 0.6f else 0.2f),
                                        color.copy(alpha = if (isCurrent) 0.7f else 0.2f),
                                        color.copy(alpha = if (isCurrent) 0.3f else 0.05f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        FrostColors.offWhite.copy(alpha = 0.9f),
                                        FrostColors.offWhite.copy(alpha = 0.2f),
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
//  2. SMOOTH AREA CHART (For Temps, Pressures, Voltage)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassAreaChart(kpi: EnergyKpi, color: Color) {
    val dataPointsCount = 8
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value
        val variance = base * 0.08f // Less variance for continuous variables
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2)
        } + kpi.value
    }

    val thresholdVal = kpi.threshold ?: 0f
    val maxData = historicalData.maxOrNull() ?: 1f
    val minData = historicalData.minOrNull() ?: 0f
    // Create a sensible window so the curve doesn't look completely flat
    val range = (maxOf(maxData, thresholdVal.toFloat()) - minData).coerceAtLeast(1f)
    val maxVal = maxData + range * 0.1f
    val minVal = (minData - range * 0.1f).coerceAtLeast(0f)

    val revealFraction by animateFloatAsState(targetValue = 1f, tween(1000), label = "a")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)
            val path = Path()
            val fillPath = Path()

            // Threshold line
            if (kpi.threshold != null) {
                val tY = size.height - ((kpi.threshold.toFloat() - minVal) / (maxVal - minVal) * size.height)
                if (tY in 0f..size.height) {
                    drawLine(
                        color = FrostColors.accentRed.copy(alpha = 0.6f),
                        start = Offset(0f, tY),
                        end = Offset(size.width, tY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // Build cubic bezier curve
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

            // Draw Area
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.4f), Color.Transparent)
                )
            )
            // Draw Line
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Dot at current
            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(prevX, prevY))
            drawCircle(color = FrostColors.offWhite, radius = 1.dp.toPx(), center = Offset(prevX, prevY))
        }
    }
}

// ═══════════════════════════════════════════════════════
//  3. STEP CHART (For Percentages/Efficiency)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassStepChart(kpi: EnergyKpi, color: Color) {
    val dataPointsCount = 7
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value
        val variance = base * 0.1f
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2)
        } + kpi.value
    }

    val maxVal = 100f // Always bound percentages to 100
    val revealFraction by animateFloatAsState(targetValue = 1f, tween(900), label = "s")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)
            val path = Path()
            val fillPath = Path()

            if (kpi.threshold != null) {
                val tY = size.height - ((kpi.threshold.toFloat()) / maxVal * size.height)
                drawLine(
                    color = FrostColors.accentRed.copy(alpha = 0.6f),
                    start = Offset(0f, tY),
                    end = Offset(size.width, tY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            var prevX = 0f
            var prevY = size.height - (historicalData[0] / maxVal * size.height) * revealFraction

            path.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, size.height)
            fillPath.lineTo(prevX, prevY)

            for (i in 1 until historicalData.size) {
                val curX = i * step
                val curY = size.height - (historicalData[i] / maxVal * size.height) * revealFraction

                // Horizontal line then vertical line
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

            // Highlight current step block
            drawRect(
                color = FrostColors.offWhite.copy(alpha = 0.2f),
                topLeft = Offset(prevX - step, prevY),
                size = Size(step, size.height - prevY)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
//  4. LOLLIPOP CHART (For Flow, RPM, Speeds)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassLollipopChart(kpi: EnergyKpi, color: Color) {
    val dataPointsCount = 8
    val historicalData = remember(kpi.value) {
        val random = Random(kpi.title.hashCode())
        val base = kpi.value
        val variance = base * 0.25f // High variance to make it spiky
        List(dataPointsCount - 1) {
            (base - variance) + random.nextFloat() * (variance * 2)
        } + kpi.value
    }

    val thresholdVal = kpi.threshold ?: 0f
    val maxData = historicalData.maxOrNull() ?: 1f
    val maxVal = maxOf(maxData, thresholdVal.toFloat()) * 1.1f

    val revealFraction by animateFloatAsState(targetValue = 1f, tween(1100), label = "l")

    Box(Modifier.fillMaxWidth().height(55.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / (historicalData.size - 1)

            if (kpi.threshold != null && maxVal > 0f) {
                val tY = size.height - (kpi.threshold.toFloat() / maxVal * size.height)
                drawLine(
                    color = FrostColors.accentRed.copy(alpha = 0.6f),
                    start = Offset(0f, tY),
                    end = Offset(size.width, tY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            historicalData.forEachIndexed { i, value ->
                val x = i * step
                val targetY = size.height - (value / maxVal * size.height)
                // Animate Y position falling into place
                val currentY = size.height - ((size.height - targetY) * revealFraction)
                val isCurrent = i == historicalData.size - 1

                // Stick
                drawLine(
                    color = FrostColors.lightGray.copy(alpha = if (isCurrent) 0.6f else 0.3f),
                    start = Offset(x, size.height),
                    end = Offset(x, currentY),
                    strokeWidth = if (isCurrent) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Candy (Dot)
                drawCircle(
                    color = color.copy(alpha = if (isCurrent) 1f else 0.6f),
                    radius = if (isCurrent) 4.5.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(x, currentY)
                )

                if (isCurrent) {
                    drawCircle(
                        color = FrostColors.offWhite,
                        radius = 2.dp.toPx(),
                        center = Offset(x, currentY)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  ALERTS PANEL (RIGHT PANEL) - ACTIVE ALERTS LOG
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostAlertsPanel(
    modifier: Modifier,
    alerts: List<EnergyAlertEvent>,
    onAlertClick: (EnergyAlertEvent) -> Unit
) {
    val sortedAlerts = remember(alerts) {
        alerts.sortedWith(
            compareBy<EnergyAlertEvent> { it.acknowledged }
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

    FrostGlassPanel(modifier = modifier) {
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
                            .background(
                                FrostColors.accentOrangeSoft,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = FrostColors.accentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Active Alerts Log",
                            color = FrostColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Synced via Digital Twin API",
                            color = FrostColors.textTertiary,
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = "$activeCount Active",
                    color = FrostColors.accentOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sortedAlerts.forEach { alert ->
                    FrostAlertLogRow(
                        alert = alert,
                        onClick = { onAlertClick(alert) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FrostAlertLogRow(
    alert: EnergyAlertEvent,
    onClick: () -> Unit
) {
    val acknowledged = alert.acknowledged

    val severityColor = when {
        acknowledged -> FrostColors.textDisabled
        alert.priority == "CRITICAL" -> FrostColors.accentRed
        alert.priority == "WARNING" -> FrostColors.accentOrange
        else -> FrostColors.accentBlue
    }

    val rowShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(
                brush = if (acknowledged) {
                    SolidColor(FrostColors.offWhite.copy(alpha = 0.18f))
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            severityColor.copy(alpha = 0.09f),
                            FrostColors.offWhite.copy(alpha = 0.26f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (acknowledged) {
                    FrostColors.divider
                } else {
                    severityColor.copy(alpha = 0.20f)
                },
                shape = rowShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            imageVector = if (acknowledged) {
                Icons.Rounded.CheckCircle
            } else {
                Icons.Rounded.WarningAmber
            },
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
                color = if (acknowledged) {
                    FrostColors.textDisabled
                } else {
                    FrostColors.textPrimary
                },
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
    alert: EnergyAlertEvent,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().background(FrostColors.deepNavy.copy(0.6f))
                .clickable(remember { MutableInteractionSource() }, null) { onDismiss() },
            Alignment.Center
        ) {
            val aColor = when (alert.priority) {
                "CRITICAL" -> FrostColors.accentRed; "WARNING" -> FrostColors.accentOrange; else -> FrostColors.accentBlue
            }
            val aBg = when (alert.priority) {
                "CRITICAL" -> FrostColors.accentRedSoft; "WARNING" -> FrostColors.accentOrangeSoft; else -> FrostColors.accentBlueSoft
            }

            FrostGlassPanel(
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
                                Text("ENERGY ALERT", color = aColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(alert.stage, color = FrostColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Box(Modifier.size(32.dp).background(FrostColors.chipBg, CircleShape), Alignment.Center) {
                                Icon(Icons.Rounded.Close, null, tint = FrostColors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(alert.message, color = FrostColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

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
                                Text("Threshold", color = FrostColors.textTertiary, fontSize = 10.sp)
                                Text(String.format("%.2f", alert.thresholdValue), color = FrostColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Current", color = FrostColors.textTertiary, fontSize = 10.sp)
                                Text(String.format("%.2f", alert.currentValue), color = aColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(alert.description, color = FrostColors.textSecondary, fontSize = 12.sp, lineHeight = 18.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp)).background(FrostColors.chipBg)
                                .clickable { onDismiss() }.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) { Text("Cancel", color = FrostColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(aColor, aColor.copy(0.7f))))
                                .clickable { onAcknowledge() }.padding(horizontal = 20.dp, vertical = 10.dp)
                        ) { Text("Acknowledge", color = FrostColors.offWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}