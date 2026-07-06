package com.example.category3.auth.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.components.RadialAppBar

// ----------------- helpers -----------------

fun resolveKpiIcon(iconName: String): ImageVector = when (iconName) {
    "speed" -> Icons.Rounded.Speed
    "air" -> Icons.Rounded.Air
    "thermostat" -> Icons.Rounded.Thermostat
    "water_drop" -> Icons.Rounded.WaterDrop
    "factory" -> Icons.Rounded.Factory
    "electric_bolt" -> Icons.Rounded.ElectricBolt
    "autorenew" -> Icons.Rounded.Autorenew
    "monitor_heart" -> Icons.Rounded.MonitorHeart
    else -> Icons.Rounded.ElectricBolt
}

fun statusColor(status: KpiStatus): Color = when (status) {
    KpiStatus.NORMAL -> BrandTeal
    KpiStatus.WARNING -> BrandSoftOrange
    KpiStatus.CRITICAL -> BrandOrange
}

// ----------------- screen -----------------

@Composable
fun EnergyTabScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit,
    initialSection: String? = null,
    initialAlertId: String? = null,
    energyViewModel: EnergyViewModel = viewModel(factory = EnergyViewModel.provideFactory())
) {
    val theme = getAdaptiveTheme(isDark = false)

    val kpis by energyViewModel.kpis.collectAsStateWithLifecycle()
    val totalPower by energyViewModel.totalPowerKw.collectAsStateWithLifecycle()
    val peakDemand by energyViewModel.peakDemandKw.collectAsStateWithLifecycle()
    val minLoad by energyViewModel.minLoadKw.collectAsStateWithLifecycle()
    val trend by energyViewModel.efficiencyTrend.collectAsStateWithLifecycle()
    val connectionStatus by energyViewModel.connectionStatus.collectAsStateWithLifecycle()
    val energyAlerts by energyViewModel.activeEnergyAlerts.collectAsStateWithLifecycle()

    var selectedSection: String? by remember { mutableStateOf(initialSection) }
    var selectedAlertDetail by remember { mutableStateOf<EnergyAlertEvent?>(null) }
    var hasHandledInitialAlert by rememberSaveable { mutableStateOf(false) }

    // Deep-link: open the correct section + alert dialog (only once)
    LaunchedEffect(energyAlerts, initialAlertId) {
        if (initialAlertId != null && !hasHandledInitialAlert && energyAlerts.isNotEmpty()) {
            val targetAlert = energyAlerts.find { it.id == initialAlertId }
            if (targetAlert != null) {
                selectedSection = targetAlert.stage
                selectedAlertDetail = targetAlert
                hasHandledInitialAlert = true
            }
        }
    }

    val sections = remember(kpis) { kpis.groupBy { it.section } }
    val sectionOrder = listOf(
        "Power Quality", "Boiler", "Milling", "Evaporation",
        "Concentration", "Pumps", "Production"
    )
    val bgBrush = remember { Brush.linearGradient(listOf(BrandOffWhite, Color.White)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 56.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EnergyWise",
                    color = BrandDeepNavy,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val connColor = when (connectionStatus) {
                        "CONNECTED" -> BrandTeal
                        "RECONNECTING" -> BrandSoftOrange
                        else -> BrandSteelGray
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(connColor, CircleShape)
                    )
                    Text(
                        connectionStatus,
                        color = connColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            EnergySummaryPanel(theme, totalPower, peakDemand, minLoad, trend, kpis)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionChip("All", selectedSection == null, theme) { selectedSection = null }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sectionOrder.filter { sections.containsKey(it) }.forEach { sec ->
                    val alertCount = kpis.count { it.section == sec && it.status != KpiStatus.NORMAL }
                    SectionChip(sec, selectedSection == sec, theme, alertCount) {
                        selectedSection = if (selectedSection == sec) null else sec
                    }
                }
            }

            if (energyAlerts.isNotEmpty()) {
                EnergyAlertsTable(
                    energyAlerts = energyAlerts,
                    theme = theme,
                    onAcknowledge = { energyViewModel.acknowledgeAlert(it.id) },
                    onAlertClick = { alert ->
                        selectedSection = alert.stage
                        selectedAlertDetail = alert
                    }
                )
            }

            val displaySections = remember(selectedSection, sections, sectionOrder) {
                if (selectedSection != null) {
                    mapOf(selectedSection!! to (sections[selectedSection] ?: emptyList()))
                } else {
                    sectionOrder
                        .filter { sections.containsKey(it) }
                        .associateWith { sections[it] ?: emptyList() }
                }
            }

            EnergySections(displaySections, theme)
            Spacer(modifier = Modifier.height(40.dp))
        }

        selectedAlertDetail?.let { alert ->
            EnergyAlertDialog(
                alert = alert,
                theme = theme,
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
            onActionSelected = { onNavigateToScreen(it) }
        )
    }
}

// ----------------- Summary Panel -----------------

@Composable
private fun EnergySummaryPanel(
    theme: DashboardTheme,
    totalPower: Float,
    peakDemand: Float,
    minLoad: Float,
    trend: Float,
    kpis: List<EnergyKpi>
) {
    val animTotalPower by animateFloatAsState(totalPower, tween(800), label = "power")
    val animPeak by animateFloatAsState(peakDemand, tween(800), label = "peak")
    val animMin by animateFloatAsState(
        if (minLoad == Float.MAX_VALUE) 0f else minLoad, tween(800), label = "min"
    )

    val trendData = remember(kpis) {
        val powerKpis = kpis.filter { it.unit == "kW" }.take(10)
        val values = powerKpis.map { it.value }
        val maxV = values.maxOrNull() ?: 1f
        val minV = values.minOrNull() ?: 0f
        val range = (maxV - minV).coerceAtLeast(1f)
        Triple(values, minV, range)
    }

    CleanPanel(theme = theme, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Total Plant Load",
                        color = theme.textMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format("%,.0f", animTotalPower),
                            color = theme.textMain,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            " kW",
                            color = theme.textMuted,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    HeroPill("PEAK", String.format("%,.0f", animPeak), AccentCritical, theme)
                    HeroPill("MIN", String.format("%,.0f", animMin), AccentSuccess, theme)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Efficiency Trend", color = theme.textLightMuted, fontSize = 11.sp)
                    val trendColor = if (trend >= 0f) AccentSuccess else AccentCritical
                    val trendPrefix = if (trend >= 0f) "↑" else "↓"
                    Text(
                        "$trendPrefix ${String.format("%.1f", trend)}%",
                        color = trendColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp)
                ) {
                    val (values, minV, range) = trendData
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (values.size < 2) return@Canvas
                        val path = Path()
                        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
                        values.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = size.height - ((v - minV) / range * size.height)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = AccentPrimary,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}

// ----------------- Alerts Table -----------------

@Composable
private fun EnergyAlertsTable(
    energyAlerts: List<EnergyAlertEvent>,
    theme: DashboardTheme,
    onAcknowledge: (EnergyAlertEvent) -> Unit,
    onAlertClick: (EnergyAlertEvent) -> Unit
) {
    CleanPanel(theme = theme, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Energy Alerts Log", color = AccentCritical, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${energyAlerts.count { !it.acknowledged }} Active",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("STAGE", color = theme.textLightMuted, fontSize = 9.sp, modifier = Modifier.weight(1f))
                Text("ISSUE", color = theme.textLightMuted, fontSize = 9.sp, modifier = Modifier.weight(2f))
                Text("THRESHOLD", color = theme.textLightMuted, fontSize = 9.sp, modifier = Modifier.weight(1.5f))
                Text("STATUS", color = theme.textLightMuted, fontSize = 9.sp, modifier = Modifier.width(90.dp))
            }

            val sortedAlerts = energyAlerts.sortedBy { it.acknowledged }
            sortedAlerts.forEach { alert ->
                val isAck = alert.acknowledged
                val rowColor = if (isAck) BrandSteelGray else when (alert.priority) {
                    "CRITICAL" -> AccentCritical
                    "WARNING" -> AccentWarning
                    else -> AccentPrimary
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (isAck) Color.Transparent else rowColor.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isAck) Color.Transparent else rowColor.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isAck) { onAlertClick(alert) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alert.stage,
                        color = if (isAck) theme.textLightMuted else theme.textMain,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        alert.message,
                        color = if (isAck) theme.textLightMuted else rowColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        String.format("%.0f / %.0f", alert.currentValue, alert.thresholdValue),
                        color = if (isAck) theme.textLightMuted else theme.textMain,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1.5f)
                    )
                    if (!isAck) {
                        Text(
                            "ACKNOWLEDGE",
                            color = BrandOffWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(rowColor, RoundedCornerShape(6.dp))
                                .clickable { onAcknowledge(alert) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .width(80.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(90.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = BrandSteelGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("ACK'D", color = BrandSteelGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Sections -----------------

@Composable
private fun EnergySections(
    displaySections: Map<String, List<EnergyKpi>>,
    theme: DashboardTheme
) {
    displaySections.forEach { (sectionName, sectionKpis) ->
        if (sectionKpis.isEmpty()) return@forEach

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sectionIcon = when (sectionName) {
                    "Boiler" -> Icons.Outlined.Sensors
                    "Milling" -> Icons.Rounded.Factory
                    "Evaporation" -> Icons.Rounded.Thermostat
                    "Concentration" -> Icons.Rounded.Speed
                    "Pumps" -> Icons.Rounded.WaterDrop
                    "Power Quality" -> Icons.Outlined.ElectricBolt
                    "Production" -> Icons.Rounded.Factory
                    else -> Icons.Outlined.Sensors
                }
                Icon(sectionIcon, null, tint = theme.textMuted, modifier = Modifier.size(18.dp))
                Text(
                    sectionName.uppercase(),
                    color = theme.textMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            val critCount = sectionKpis.count { it.status == KpiStatus.CRITICAL }
            val warnCount = sectionKpis.count { it.status == KpiStatus.WARNING }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (critCount > 0) StatusBadge("$critCount CRIT", AccentCritical, theme)
                if (warnCount > 0) StatusBadge("$warnCount WARN", AccentWarning, theme)
                if (critCount == 0 && warnCount == 0) StatusBadge("OK", AccentSuccess, theme)
            }
        }

        val chunked = sectionKpis.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { kpi ->
                    LiveKpiCard(
                        kpi = kpi,
                        theme = theme,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ----------------- Alert Dialog -----------------

@Composable
private fun EnergyAlertDialog(
    alert: EnergyAlertEvent,
    theme: DashboardTheme,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit
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
                    val alertColor = when (alert.priority) {
                        "CRITICAL" -> AccentCritical
                        "WARNING" -> AccentWarning
                        else -> AccentPrimary
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.WarningAmber, null, tint = alertColor, modifier = Modifier.size(28.dp))
                            Column {
                                Text("ENERGY ALERT", color = alertColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(alert.stage, color = theme.textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, null, tint = theme.textMuted)
                        }
                    }
                    Text(alert.message, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(alertColor.copy(0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Threshold Details", color = alertColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Threshold: ${String.format("%.2f", alert.thresholdValue)}", color = theme.textMain, fontSize = 13.sp)
                        Text("Current: ${String.format("%.2f", alert.currentValue)}", color = theme.textMain, fontSize = 13.sp)
                    }
                    Text(alert.description, color = theme.textMuted, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Acknowledge",
                            color = BrandOffWhite,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(alertColor, RoundedCornerShape(8.dp))
                                .clickable { onAcknowledge() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------- Small components -----------------

@Composable
fun HeroPill(label: String, value: String, color: Color, theme: DashboardTheme) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.trackBg.copy(alpha = 0.3f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionChip(
    label: String,
    isSelected: Boolean,
    theme: DashboardTheme,
    alertCount: Int = 0,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AccentPrimary.copy(alpha = 0.15f) else theme.trackBg.copy(alpha = 0.2f)
    val borderColor = if (isSelected) AccentPrimary else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            color = if (isSelected) AccentPrimary else theme.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (alertCount > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(AccentCritical, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$alertCount", color = BrandOffWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color, theme: DashboardTheme) {
    Text(
        text,
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun LiveKpiCard(
    kpi: EnergyKpi,
    theme: DashboardTheme,
    modifier: Modifier = Modifier
) {
    val statusCol by animateColorAsState(
        targetValue = statusColor(kpi.status),
        animationSpec = tween(500),
        label = "statusColor"
    )

    CleanPanel(theme = theme, modifier = modifier, cornerRadius = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        resolveKpiIcon(kpi.iconName),
                        contentDescription = null,
                        tint = statusCol,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        kpi.title,
                        color = theme.textMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (kpi.status != KpiStatus.NORMAL) {
                    Icon(Icons.Rounded.WarningAmber, null, tint = statusCol, modifier = Modifier.size(14.dp))
                } else {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        null,
                        tint = AccentSuccess.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    kpi.displayValue,
                    color = if (kpi.status != KpiStatus.NORMAL) statusCol else theme.textMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(kpi.unit, color = theme.textLightMuted, fontSize = 10.sp)
            }

            if (kpi.threshold != null && kpi.status != KpiStatus.NORMAL) {
                val thresholdLabel = when (kpi.thresholdType) {
                    ThresholdType.ABOVE -> "Max ${kpi.threshold.toInt()} ${kpi.unit}"
                    ThresholdType.BELOW -> "Min ${kpi.threshold.toInt()} ${kpi.unit}"
                }
                Text(
                    thresholdLabel,
                    color = statusCol.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}