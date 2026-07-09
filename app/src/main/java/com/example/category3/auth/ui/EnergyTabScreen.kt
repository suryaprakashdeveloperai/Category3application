package com.example.category3.auth.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// ═══════════════════════════════════════════════════════
//  GREYFROST LIQUID THEME
// ═══════════════════════════════════════════════════════

object FrostColors {
    // Richer grey-blue background to make the glass pop
    val pageBg            = Color(0xFFD5DBE5)

    val textPrimary       = Color(0xFF242933)
    val textSecondary     = Color(0xFF4C566A)
    val textTertiary      = Color(0xFF7B88A1)
    val textDisabled      = Color(0xFF98A4B8)

    // Frosty cool-toned accents
    val accentBlue        = Color(0xFF5E81AC)
    val accentBlueSoft    = Color(0x335E81AC)
    val accentGreen       = Color(0xFF389684) // Muted jade green
    val accentGreenSoft   = Color(0x33389684)
    val accentOrange      = Color(0xFFD08752) // Muted clay orange
    val accentOrangeSoft  = Color(0x33D08752)
    val accentRed         = Color(0xFFBF616A) // Muted frost red
    val accentRedSoft     = Color(0x33BF616A)

    val chartLine         = Color(0xFF5E81AC)
    val chartGradTop      = Color(0x665E81AC)
    val chartGradBottom   = Color(0x005E81AC)

    val divider           = Color(0x4DA3B1C6)
    val chipBg            = Color(0x66C8D0DD)
    val chipSelectedBg    = Color(0x4D5E81AC)
}

// ═══════════════════════════════════════════════════════
//  GREYFROST LIQUID GLASS PANEL
// ═══════════════════════════════════════════════════════

@Composable
fun FrostGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            // Soft floating shadow
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x26001133),
                spotColor = Color(0x33001133)
            )
            .clip(RoundedCornerShape(cornerRadius))
            // Base translucent liquid gradient (diagonal)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xE6F8F9FA), // Dense frosty white top-left
                        Color(0x99E2E7EE), // Translucent cool grey middle
                        Color(0x66D1D8E2), // More transparent deep grey
                        Color(0xCCEAEEF3)  // Liquid reflection bottom-right
                    )
                )
            )
            // Liquid edge highlight (light top-left, dark bottom-right)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.2f),
                        Color(0x1A000000),
                        Color(0x33001122)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Inner radial glow / sheen for liquid effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
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
                .padding(start = 56.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ═══ ROW 1: HEADER ═══
            FrostHeader(connectionStatus)

            // ═══ ROW 2: STAT CARDS ═══
            FrostSummaryRow(totalPower, peakDemand, minLoad, trend, kpis)

            // ═══ ROW 3: CHIPS ═══
            FrostChipRow(sections, sectionOrder, selectedSection, kpis) { selectedSection = it }

            // ═══ ROW 4: MAIN CONTENT ═══
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── LEFT PANEL: KPI SECTIONS ──
                FrostKpiPanel(
                    modifier = Modifier
                        .weight(if (hasAlerts) 0.58f else 1f)
                        .fillMaxHeight(),
                    displaySections = displaySections
                )

                // ── RIGHT PANEL: ALERTS ──
                if (hasAlerts) {
                    FrostAlertsPanel(
                        modifier = Modifier.weight(0.42f).fillMaxHeight(),
                        alerts = energyAlerts,
                        onAcknowledge = { energyViewModel.acknowledgeAlert(it.id) },
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
                .offset(x = (-8).dp)
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
        Modifier.fillMaxWidth().height(36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EnergyWise",
                color = FrostColors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Real-time plant monitoring",
                color = FrostColors.textTertiary,
                fontSize = 12.sp
            )
        }

        FrostGlassPanel(cornerRadius = 10.dp) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp).zIndex(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.size(7.dp).background(connColor, CircleShape))
                Text(
                    connectionStatus,
                    color = connColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  SUMMARY ROW
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostSummaryRow(
    totalPower: Float,
    peakDemand: Float,
    minLoad: Float,
    trend: Float,
    kpis: List<EnergyKpi>
) {
    val animTotal by animateFloatAsState(totalPower, tween(800), label = "t")
    val animPeak  by animateFloatAsState(peakDemand, tween(800), label = "p")
    val animMin   by animateFloatAsState(
        if (minLoad == Float.MAX_VALUE) 0f else minLoad, tween(800), label = "m"
    )

    val sparkData = remember(kpis) {
        val vals = kpis.filter { it.unit == "kW" }.take(10).map { it.value }
        val mx = vals.maxOrNull() ?: 1f
        val mn = vals.minOrNull() ?: 0f
        Triple(vals, mn, (mx - mn).coerceAtLeast(1f))
    }

    Row(
        Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FrostGlassPanel(Modifier.weight(1.5f).fillMaxHeight(), cornerRadius = 16.dp) {
            Row(Modifier.fillMaxSize().padding(14.dp).zIndex(2f), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total Plant Load", color = FrostColors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format("%,.0f", animTotal),
                            color = FrostColors.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("kW", color = FrostColors.textTertiary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                Box(Modifier.width(90.dp).height(44.dp)) {
                    val (vals, mn, rng) = sparkData
                    Canvas(Modifier.fillMaxSize()) {
                        if (vals.size < 2) return@Canvas
                        val fill = Path(); val line = Path()
                        val step = size.width / (vals.size - 1).coerceAtLeast(1)
                        vals.forEachIndexed { i, v ->
                            val x = i * step; val y = size.height - ((v - mn) / rng * size.height)
                            if (i == 0) { line.moveTo(x, y); fill.moveTo(x, size.height); fill.lineTo(x, y) }
                            else { line.lineTo(x, y); fill.lineTo(x, y) }
                        }
                        fill.lineTo(size.width, size.height); fill.close()
                        drawPath(fill, Brush.verticalGradient(listOf(FrostColors.chartGradTop, FrostColors.chartGradBottom)))
                        drawPath(line, FrostColors.chartLine, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }

        FrostMiniStatCard(
            title = "Peak Demand",
            value = String.format("%,.0f", animPeak),
            unit = "kW",
            accent = FrostColors.accentRed,
            accentBg = FrostColors.accentRedSoft,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )

        FrostMiniStatCard(
            title = "Min Load",
            value = String.format("%,.0f", animMin),
            unit = "kW",
            accent = FrostColors.accentGreen,
            accentBg = FrostColors.accentGreenSoft,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )

        FrostGlassPanel(Modifier.weight(1f).fillMaxHeight(), cornerRadius = 16.dp) {
            Column(Modifier.fillMaxSize().padding(14.dp).zIndex(2f), verticalArrangement = Arrangement.SpaceBetween) {
                Text("Efficiency", color = FrostColors.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                val tCol = if (trend >= 0) FrostColors.accentGreen else FrostColors.accentRed
                val tIco = if (trend >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown
                val tBg  = if (trend >= 0) FrostColors.accentGreenSoft else FrostColors.accentRedSoft
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(28.dp).background(tBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(tIco, null, tint = tCol, modifier = Modifier.size(16.dp))
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
private fun FrostMiniStatCard(
    title: String,
    value: String,
    unit: String,
    accent: Color,
    accentBg: Color,
    modifier: Modifier = Modifier
) {
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
    sections: Map<String, List<EnergyKpi>>,
    sectionOrder: List<String>,
    selectedSection: String?,
    kpis: List<EnergyKpi>,
    onSelect: (String?) -> Unit
) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FrostChip("All", selectedSection == null) { onSelect(null) }
        sectionOrder.filter { sections.containsKey(it) }.forEach { sec ->
            val alerts = kpis.count { it.section == sec && it.status != KpiStatus.NORMAL }
            FrostChip(sec, selectedSection == sec, alerts) {
                onSelect(if (selectedSection == sec) null else sec)
            }
        }
    }
}

@Composable
fun FrostChip(label: String, isSelected: Boolean, alertCount: Int = 0, onClick: () -> Unit) {
    val bg   = if (isSelected) FrostColors.chipSelectedBg else FrostColors.chipBg
    val txt  = if (isSelected) FrostColors.accentBlue else FrostColors.textSecondary
    val bdr  = if (isSelected) FrostColors.accentBlue.copy(0.4f) else Color.White.copy(0.3f)

    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, bdr, RoundedCornerShape(8.dp))
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = txt, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium)
        if (alertCount > 0) {
            Box(Modifier.size(16.dp).background(FrostColors.accentRed, CircleShape), contentAlignment = Alignment.Center) {
                Text("$alertCount", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  KPI MONITOR (LEFT PANEL)
// ═══════════════════════════════════════════════════════

@Composable
private fun FrostKpiPanel(
    modifier: Modifier,
    displaySections: Map<String, List<EnergyKpi>>
) {
    FrostGlassPanel(modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp)
                .zIndex(2f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

                kpiList.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        row.forEach { kpi -> FrostKpiCard(kpi, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
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
//  KPI CARD (Liquid inner elements)
// ═══════════════════════════════════════════════════════

@Composable
fun FrostKpiCard(kpi: EnergyKpi, modifier: Modifier = Modifier) {
    val stCol by animateColorAsState(frostStatusColor(kpi.status), tween(500), label = "c")
    val stBg  by animateColorAsState(frostStatusBg(kpi.status), tween(500), label = "b")

    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xE6FFFFFF), Color(0x99D5DBE5))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Color.White, Color(0x33000000))),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(30.dp).background(stBg, RoundedCornerShape(8.dp)), Alignment.Center) {
                        Icon(resolveKpiIcon(kpi.iconName), null, tint = stCol, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        kpi.title, color = FrostColors.textPrimary, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(Modifier.size(20.dp).background(stBg, CircleShape), Alignment.Center) {
                    Icon(
                        if (kpi.status != KpiStatus.NORMAL) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                        null, tint = stCol.copy(if (kpi.status == KpiStatus.NORMAL) 0.6f else 1f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    kpi.displayValue,
                    color = if (kpi.status != KpiStatus.NORMAL) stCol else FrostColors.textPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
                )
                Text(kpi.unit, color = FrostColors.textTertiary, fontSize = 9.sp, modifier = Modifier.padding(bottom = 2.dp))
            }

            if (kpi.threshold != null && kpi.status != KpiStatus.NORMAL) {
                val lbl = when (kpi.thresholdType) {
                    ThresholdType.ABOVE -> "Max ${kpi.threshold.toInt()} ${kpi.unit}"
                    ThresholdType.BELOW -> "Min ${kpi.threshold.toInt()} ${kpi.unit}"
                }
                Box(Modifier.background(stBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(lbl, color = stCol, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
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
    alerts: List<EnergyAlertEvent>,
    onAcknowledge: (EnergyAlertEvent) -> Unit,
    onAlertClick: (EnergyAlertEvent) -> Unit
) {
    FrostGlassPanel(modifier) {
        Column(Modifier.fillMaxSize().padding(14.dp).zIndex(2f)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(28.dp).background(FrostColors.accentRedSoft, RoundedCornerShape(8.dp)), Alignment.Center) {
                        Icon(Icons.Rounded.WarningAmber, null, tint = FrostColors.accentRed, modifier = Modifier.size(15.dp))
                    }
                    Column {
                        Text("Energy Alerts", color = FrostColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${alerts.count { !it.acknowledged }} active", color = FrostColors.textTertiary, fontSize = 10.sp)
                    }
                }
                Text("View all", color = FrostColors.accentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth().background(FrostColors.chipBg, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("STAGE", color = FrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, modifier = Modifier.weight(1f))
                Text("ISSUE", color = FrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, modifier = Modifier.weight(1.8f))
                Text("VALUES", color = FrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, modifier = Modifier.weight(1.2f))
                Text("ACTION", color = FrostColors.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
            }

            Spacer(Modifier.height(4.dp))

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                alerts.sortedBy { it.acknowledged }.forEach { alert ->
                    FrostAlertRow(alert, onAcknowledge, onAlertClick)
                }
            }
        }
    }
}

@Composable
private fun FrostAlertRow(
    alert: EnergyAlertEvent,
    onAcknowledge: (EnergyAlertEvent) -> Unit,
    onAlertClick: (EnergyAlertEvent) -> Unit
) {
    val ack = alert.acknowledged
    val color = if (ack) FrostColors.textDisabled else when (alert.priority) {
        "CRITICAL" -> FrostColors.accentRed
        "WARNING"  -> FrostColors.accentOrange
        else       -> FrostColors.accentBlue
    }

    // Liquid gradient for active alerts
    val rowModifier = if (ack) {
        Modifier.background(Color.Transparent)
    } else {
        Modifier
            .background(Brush.horizontalGradient(listOf(color.copy(0.12f), color.copy(0.04f))))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(8.dp))
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(rowModifier)
            .clickable(enabled = !ack) { onAlertClick(alert) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(5.dp).background(color, CircleShape))
            Text(alert.stage, color = if (ack) FrostColors.textDisabled else FrostColors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(alert.message, color = if (ack) FrostColors.textDisabled else color, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.8f))
        Text(String.format("%.0f/%.0f", alert.currentValue, alert.thresholdValue), color = if (ack) FrostColors.textDisabled else FrostColors.textSecondary, fontSize = 10.sp, modifier = Modifier.weight(1.2f))

        if (!ack) {
            Box(
                Modifier.width(80.dp).clip(RoundedCornerShape(6.dp)).background(color).clickable { onAcknowledge(alert) }.padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Acknowledge", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(Modifier.width(80.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Icon(Icons.Rounded.CheckCircle, null, tint = FrostColors.accentGreen.copy(0.5f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text("ACK'D", color = FrostColors.textDisabled, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
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
            Modifier.fillMaxSize().background(FrostColors.textPrimary.copy(0.4f))
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
                        ) { Text("Acknowledge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}