package com.example.category3.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.components.RadialAppBar

// --- Light Theme Colors ---
private val BgLight = Color(0xFFF3F4F6)
private val CardWhite = Color(0xFFFFFFFF)
//private val TextDark = Color(0xFF1F2937)
//private val TextGray = Color(0xFF6B7280)
private val PurpleMain = Color(0xFF8B5CF6)
private val PurpleLight = Color(0xFFC4B5FD)
private val BlueAccent = Color(0xFF3B82F6)
private val GreenSuccess = Color(0xFF10B981)
private val RedDanger = Color(0xFFEF4444)
private val AmberWarning = Color(0xFFF59E0B)

@Composable
fun ProductionTabScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit,
    initialAlertId: String? = null,
    viewModel: ProductionViewModel = viewModel(factory = ProductionViewModel.provideFactory())
) {
    val kpis by viewModel.kpis.collectAsStateWithLifecycle()
    val activeProdAlerts by viewModel.activeProductionAlerts.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedAlertDetail by remember { mutableStateOf<ProductionAlertEvent?>(null) }
    var hasHandledInitialAlert by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(activeProdAlerts, initialAlertId) {
        if (initialAlertId != null && !hasHandledInitialAlert && activeProdAlerts.isNotEmpty()) {
            val targetAlert = activeProdAlerts.find { it.id == initialAlertId }
            if (targetAlert != null) {
                selectedAlertDetail = targetAlert
                hasHandledInitialAlert = true
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

    fun kpi(id: String) = kpis.firstOrNull { it.id == id }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgLight)
                .padding(start = 56.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLandscape) {
                Row(Modifier.fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MainChartCard(modifier = Modifier.weight(2f), kpi = kpi("global_throughput"))
                    GaugeCard(modifier = Modifier.weight(1f), kpi = kpi("target_achieved"))
                }
                Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AreaChartCard(modifier = Modifier.weight(1f), kpi = kpi("overall_yield"))
                    BarChartCard(modifier = Modifier.weight(1f), kpi = kpi("milling_throughput"))
                    DonutChartCard(modifier = Modifier.weight(1f), kpi = kpi("cane_stock"))
                }
            } else {
                MainChartCard(modifier = Modifier.fillMaxWidth().height(200.dp), kpi = kpi("global_throughput"))
                Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GaugeCard(modifier = Modifier.weight(1f), kpi = kpi("target_achieved"))
                    AreaChartCard(modifier = Modifier.weight(1f), kpi = kpi("overall_yield"))
                }
                Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BarChartCard(modifier = Modifier.weight(1f), kpi = kpi("milling_throughput"))
                    DonutChartCard(modifier = Modifier.weight(1f), kpi = kpi("cane_stock"))
                }
            }

            if (activeProdAlerts.isNotEmpty()) {
                ProductionAlertsTable(
                    alerts = activeProdAlerts,
                    onAcknowledge = { viewModel.acknowledgeAlert(it.id) },
                    onAlertClick = { selectedAlertDetail = it }
                )
            }

            OverviewTable(modifier = Modifier.fillMaxWidth(), kpis = kpis)
        }

        selectedAlertDetail?.let { alert ->
            ProductionAlertDialog(
                alert = alert,
                onDismiss = { selectedAlertDetail = null },
                onAcknowledge = {
                    viewModel.acknowledgeAlert(alert.id)
                    selectedAlertDetail = null
                }
            )
        }

        // Overlay Navigation (Left side, matching Energy tab behavior)
        RadialAppBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp)
                .zIndex(30f),
            activeSection = "production_tab",
            onActionSelected = { onNavigateToScreen(it) }
        )
    }
}

@Composable
private fun ProductionAlertsTable(
    alerts: List<ProductionAlertEvent>,
    onAcknowledge: (ProductionAlertEvent) -> Unit,
    onAlertClick: (ProductionAlertEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Alerts Log", color = RedDanger, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${alerts.count { !it.acknowledged }} Active",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("STAGE", color = TextGray, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text("ISSUE", color = TextGray, fontSize = 10.sp, modifier = Modifier.weight(2f))
                Text("VALUE/TH", color = TextGray, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text("ACTION", color = TextGray, fontSize = 10.sp, modifier = Modifier.width(90.dp))
            }

            val sortedAlerts = alerts.sortedBy { it.acknowledged }
            sortedAlerts.forEach { alert ->
                val isAck = alert.acknowledged
                val rowColor = if (isAck) TextGray else when (alert.priority) {
                    "CRITICAL" -> RedDanger
                    "WARNING" -> AmberWarning
                    else -> BlueAccent
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
                        color = if (isAck) TextGray else TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        alert.message,
                        color = if (isAck) TextGray else rowColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        String.format("%.0f / %.0f", alert.currentValue, alert.thresholdValue),
                        color = if (isAck) TextGray else TextDark,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isAck) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(90.dp)
                                .background(rowColor, RoundedCornerShape(6.dp))
                                .clickable { onAcknowledge(alert) }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                "ACKNOWLEDGE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(90.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = TextGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "ACK'D",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductionAlertDialog(
    alert: ProductionAlertEvent,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.width(400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val alertColor = when (alert.priority) {
                        "CRITICAL" -> RedDanger
                        "WARNING" -> AmberWarning
                        else -> BlueAccent
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
                            Icon(
                                Icons.Rounded.WarningAmber,
                                null,
                                tint = alertColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    "PRODUCTION ALERT",
                                    color = alertColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    alert.stage,
                                    color = TextDark,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, null, tint = TextGray)
                        }
                    }
                    Text(
                        alert.message,
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(alertColor.copy(0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "Threshold Details",
                            color = alertColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Threshold Limit: ${String.format("%.2f", alert.thresholdValue)}",
                            color = TextDark,
                            fontSize = 14.sp
                        )
                        Text(
                            "Current Reading: ${String.format("%.2f", alert.currentValue)}",
                            color = TextDark,
                            fontSize = 14.sp
                        )
                    }

                    Text(alert.description, color = TextGray, fontSize = 14.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Acknowledge Issue",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(alertColor, RoundedCornerShape(8.dp))
                                .clickable { onAcknowledge() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(modifier: Modifier = Modifier, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.05f)
        ),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
            Text(title, color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun MainChartCard(modifier: Modifier, kpi: ProductionKpi?) {
    DashboardCard(modifier, "Global Throughput Trend") {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                kpi?.displayValue ?: "--",
                color = TextDark,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                " ${kpi?.unit ?: "MT/Day"}",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            val points = listOf(0.4f, 0.3f, 0.6f, 0.5f, 0.8f, 0.6f, 0.9f, 0.7f, 0.85f, 1.0f)
            val stepX = size.width / (points.size - 1)

            points.forEachIndexed { index, y ->
                val px = index * stepX
                val py = size.height - (y * size.height)
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, color = PurpleMain, style = Stroke(width = 4.dp.toPx()))
        }
    }
}

@Composable
fun GaugeCard(modifier: Modifier, kpi: ProductionKpi?) {
    DashboardCard(modifier, "Target Achieved") {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val stroke = 12.dp.toPx()
                drawArc(
                    color = BgLight,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = BlueAccent,
                    startAngle = 180f,
                    sweepAngle = 180f * 0.85f,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 16.dp)
            ) {
                Text(
                    kpi?.displayValue ?: "85",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text("%", fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

@Composable
fun AreaChartCard(modifier: Modifier, kpi: ProductionKpi?) {
    DashboardCard(modifier, "Overall Yield Trend") {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "${kpi?.displayValue ?: "--"} ${kpi?.unit ?: "%"}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text("+2.4%", color = GreenSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Canvas(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            val path = Path()
            path.moveTo(0f, size.height)
            path.lineTo(0f, size.height * 0.5f)
            path.lineTo(size.width * 0.5f, size.height * 0.2f)
            path.lineTo(size.width, size.height * 0.6f)
            path.lineTo(size.width, size.height)
            path.close()
            drawPath(
                path,
                brush = Brush.verticalGradient(
                    listOf(
                        PurpleLight.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )

            val linePath = Path()
            linePath.moveTo(0f, size.height * 0.5f)
            linePath.lineTo(size.width * 0.5f, size.height * 0.2f)
            linePath.lineTo(size.width, size.height * 0.6f)
            drawPath(linePath, color = PurpleMain, style = Stroke(width = 3.dp.toPx()))
        }
    }
}

@Composable
fun BarChartCard(modifier: Modifier, kpi: ProductionKpi?) {
    DashboardCard(modifier, "Milling Throughput") {
        Text(
            "${kpi?.displayValue ?: "--"} ${kpi?.unit ?: "T/hr"}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(h)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(PurpleLight)
                )
            }
        }
    }
}

@Composable
fun DonutChartCard(modifier: Modifier, kpi: ProductionKpi?) {
    DashboardCard(modifier, "Cane Stock") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    val stroke = 12.dp.toPx()
                    drawArc(PurpleMain, 0f, 120f, false, style = Stroke(stroke))
                    drawArc(BlueAccent, 120f, 160f, false, style = Stroke(stroke))
                    drawArc(BgLight, 280f, 80f, false, style = Stroke(stroke))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LegendItem(PurpleMain, "High Yld")
                LegendItem(BlueAccent, "Standard")
                LegendItem(BgLight, "Low Yld")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextGray, fontSize = 10.sp)
    }
}

@Composable
fun OverviewTable(modifier: Modifier, kpis: List<ProductionKpi>) {
    val tableRows = listOf(
        TableRowData("Raw Juice", "Clarification", "raw_juice_flow", "T/hr"),
        TableRowData("pH Level", "Clarification", "defecator_ph", "pH"),
        TableRowData("Evap Body 1", "Evaporation", "evap_body1_temp", "°C"),
        TableRowData("FCE Brix", "Concentration", "fce_brix", "Bx"),
        TableRowData("Total Produced", "Production", "total_produced", "kg")
    )

    Card(
        modifier = modifier.shadow(
            8.dp,
            RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.05f)
        ),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Factory Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text("See All", color = BlueAccent, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    "Metric",
                    modifier = Modifier.weight(2f),
                    color = TextGray,
                    fontSize = 12.sp
                )
                Text(
                    "Stage",
                    modifier = Modifier.weight(2f),
                    color = TextGray,
                    fontSize = 12.sp
                )
                Text(
                    "Value",
                    modifier = Modifier.weight(1.5f),
                    color = TextGray,
                    fontSize = 12.sp
                )
                Text(
                    "Status",
                    modifier = Modifier.weight(1f),
                    color = TextGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = BgLight)

            tableRows.forEach { rowData ->
                val kpi = kpis.firstOrNull { it.id == rowData.kpiId }
                val value = kpi?.displayValue ?: "--"
                val statusColor = when (kpi?.status?.name) {
                    "CRITICAL" -> RedDanger
                    "WARNING" -> AmberWarning
                    else -> GreenSuccess
                }
                val statusText = kpi?.status?.name ?: "NORMAL"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        rowData.name,
                        modifier = Modifier.weight(2f),
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        rowData.stage,
                        modifier = Modifier.weight(2f),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Text(
                        "$value ${rowData.unit}",
                        modifier = Modifier.weight(1.5f),
                        color = TextDark,
                        fontSize = 14.sp
                    )
                    Text(
                        statusText,
                        modifier = Modifier.weight(1f),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
                HorizontalDivider(color = BgLight)
            }
        }
    }
}

data class TableRowData(val name: String, val stage: String, val kpiId: String, val unit: String)