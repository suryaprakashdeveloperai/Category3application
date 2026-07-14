package com.example.category3.auth.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// ─── Standardized Brand Colors ────────────────────────────────────────────
private val BrandBg = Color(0xFFF6F6F7)
private val BrandCyan = Color(0xFF47B3E2)

// Keep a reference so the garbage collector doesn't destroy the WebView before printing starts
private var activePrintWebView: WebView? = null

// ─── Print Report Generator ────────────────────────────────────────────────
fun printMillReport(context: Context, live: MillLiveState) {
    Toast.makeText(context, "Preparing Document for Printer...", Toast.LENGTH_SHORT).show()

    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val eff = live.dashboard.efficiency
    val bagasseLoss = (100.0 - eff).coerceAtLeast(0.0)

    val totalKw = live.power.totalKw
    val millKw = live.power.millMotorsTotalKw
    val prepKw = live.power.prepEquipmentTotalKw
    val auxKw = (totalKw - millKw - prepKw).coerceAtLeast(0f)

    val htmlBuilder = """
        <html>
        <head>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #0F172A; padding: 20px; }
                h1 { border-bottom: 2px solid #47B3E2; padding-bottom: 10px; margin-bottom: 20px; font-size: 24px; }
                h2 { font-size: 18px; color: #334155; margin-top: 25px; margin-bottom: 10px; background-color: #F6F6F7; padding: 8px; border-radius: 4px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                th, td { border: 1px solid #E2E8F0; padding: 10px; text-align: left; font-size: 14px; }
                th { width: 40%; background-color: #F8FAFC; color: #64748B; font-weight: bold; }
                td { width: 60%; font-weight: 500; }
                .footer { margin-top: 30px; font-size: 12px; color: #94A3B8; text-align: center; border-top: 1px solid #E2E8F0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <h1>Mill Section - Live Operations Report</h1>
            
            <h2>General Information</h2>
            <table>
                <tr><th>Timestamp</th><td>$timestamp</td></tr>
                <tr><th>Operator</th><td>${live.dashboard.userName}</td></tr>
                <tr><th>Batch ID</th><td>${live.dashboard.batchId}</td></tr>
                <tr><th>Shift Start</th><td>${live.dashboard.startTime}</td></tr>
                <tr><th>Section Status</th><td>${live.dashboard.sectionStatus.name}</td></tr>
                <tr><th>Telemetry Link</th><td>${live.connectionStatus}</td></tr>
            </table>

            <h2>Key Performance Indicators (KPI)</h2>
            <table>
                <tr><th>Overall Equipment Effectiveness (OEE)</th><td>${"%.2f".format(live.dashboard.oee)} %</td></tr>
                <tr><th>Milling Efficiency</th><td>${"%.2f".format(eff)} %</td></tr>
                <tr><th>Calculated Bagasse Loss</th><td>${"%.2f".format(bagasseLoss)} %</td></tr>
            </table>

            <h2>Throughput & Feed Metrics</h2>
            <table>
                <tr><th>Throughput Mass Flow</th><td>${"%.2f".format(live.throughputKgHr)} kg/hr</td></tr>
                <tr><th>Throughput Rate</th><td>${"%.2f".format(live.throughputKgS)} kg/s</td></tr>
                <tr><th>Cane Carrier Fill Level</th><td>${"%.2f".format(live.caneStock.levelPct)} %</td></tr>
            </table>

            <h2>Raw Juice Extraction</h2>
            <table>
                <tr><th>Raw Juice Tank Level</th><td>${"%.2f".format(live.rawJuice.tankLevelPct)} %</td></tr>
                <tr><th>Juice Volume Flow</th><td>${"%.2f".format(live.rawJuice.volumeFlowM3hr)} m³/h</td></tr>
                <tr><th>Juice Liquid Flow</th><td>${"%.2f".format(live.rawJuice.flowLhr)} L/h</td></tr>
                <tr><th>Juice Temperature</th><td>${"%.2f".format(live.rawJuice.temperatureC)} °C</td></tr>
                <tr><th>Juice Density</th><td>${"%.2f".format(live.rawJuice.densityKgM3)} kg/m³</td></tr>
                <tr><th>Heater 3 Outlet Target</th><td>${"%.2f".format(live.rawJuice.heater3OutletC)} °C</td></tr>
            </table>

            <h2>Power Distribution</h2>
            <table>
                <tr><th>Total Section Power</th><td>${"%.2f".format(totalKw)} kW</td></tr>
                <tr><th>Mill Motors Active Load</th><td>${"%.2f".format(millKw)} kW</td></tr>
                <tr><th>Prep Equipment Active Load</th><td>${"%.2f".format(prepKw)} kW</td></tr>
                <tr><th>Auxiliary / Support Units</th><td>${"%.2f".format(auxKw)} kW</td></tr>
            </table>

            <div class="footer">
                Document automatically generated by Control Room Panel on $timestamp
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    activePrintWebView = webView

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Mill_Data_Report_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

// ─── Grey Frost Glassmorphism Modifier ────────────────────────────────────
@Composable
fun Modifier.glassCard(): Modifier = this
    .clip(RoundedCornerShape(12.dp))
    .background(
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                BrandLightGray.copy(alpha = 0.25f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                BrandLightGray.copy(alpha = 0.3f)
            )
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .padding(8.dp)

// ─────────────────────────────────────────────────────────────────────────────
// UI COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MillDedicatedPageScreen(
    userName: String = "Saravanan",
    userRole: String = "Operator",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: MillDedicatedViewModel = viewModel(factory = MillDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()

    MillDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen,
        getCsvData = { vm.generateCsvReport() }
    )
}

@Composable
fun MillDedicatedPageContent(
    live: MillLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {},
    getCsvData: () -> String = { "" }
) {
    val context = LocalContext.current

    // System File Picker for saving properly formatted .csv
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(getCsvData().toByteArray())
                    }
                    Toast.makeText(context, "CSV File Saved Successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val onExportCsvClick = {
        val fileName = "Mill_Section_Data_${System.currentTimeMillis()}.csv"
        createDocumentLauncher.launch(fileName)
    }

    val onPrintClick = {
        printMillReport(context, live)
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandBg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.12f), Color.Transparent)),
                radius = size.width / 2.5f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(BrandSoftOrange.copy(alpha = 0.1f), Color.Transparent)),
                radius = size.width / 2f,
                center = Offset(size.width, size.height)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            DashboardHeader(
                batchId = live.dashboard.batchId,
                operatorName = live.dashboard.userName,
                status = live.dashboard.sectionStatus,
                shiftStart = live.dashboard.startTime,
                onBack = onBack,
                onExportClick = onExportCsvClick,
                onPrintClick = onPrintClick,
                onLogEntryClick = { onNavigateToScreen("mill_manual") },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(2.6f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProcessFlowSection(live = live, modifier = Modifier.weight(1.1f))
                    VisualMetricsGridSection(live = live, modifier = Modifier.weight(1f))
                    BottomSummaryRow(live = live, modifier = Modifier.wrapContentHeight())
                }

                // Right Column
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroJuiceTankLevelVisual(live = live, modifier = Modifier.weight(1.1f))
                    SectionSummaryCard(live = live, modifier = Modifier.wrapContentHeight())
                    PowerDistributionChart(live = live, modifier = Modifier.weight(0.9f))

                    // Replaced Export Panel with Mill Section Alerts
                    MillAlertsCard(live = live, modifier = Modifier.wrapContentHeight())
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header with Dropdown Menu & Log Entry Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardHeader(
    batchId: String, operatorName: String, status: EquipmentStatus,
    shiftStart: String, onBack: () -> Unit,
    onExportClick: () -> Unit, onPrintClick: () -> Unit, onLogEntryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = BrandDeepNavy)
            }
            Text("Mill Section", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            Spacer(modifier = Modifier.width(8.dp))

            val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
            val statusColor = if (isHealthy) StatusGreen else StatusRed
            val statusLabel = if (isHealthy) "Running Smoothly" else "Intervention Req"

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Dropdown Menu Button (Export/Print)
            var menuExpanded by remember { mutableStateOf(false) }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BrandCyan.copy(alpha = 0.1f))
                        .border(1.dp, BrandCyan.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Download, "Options", tint = BrandDeepNavy, modifier = Modifier.size(16.dp))
                    Text("Export / Print", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download CSV File") },
                        onClick = {
                            menuExpanded = false
                            onExportClick()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = BrandCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Print Report") },
                        onClick = {
                            menuExpanded = false
                            onPrintClick()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Print, contentDescription = null, tint = BrandDeepNavy) }
                    )
                }
            }

            // Log Entry Button Navigates to MillManualEntryScreen
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BrandDeepNavy)
                    .clickable { onLogEntryClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Edit, "Log Entry", tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Log Entry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            HeaderMetaItem("Batch ID", batchId)
            HeaderMetaItem("Current Shift", "Started: $shiftStart")
            HeaderMetaItem("Operator", operatorName)
        }
    }
}

@Composable
fun HeaderMetaItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MILL SECTION ALERTS (Replaces the bottom right Print Panel)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MillAlertsCard(live: MillLiveState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WarningAmber, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("System Alerts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Divider(color = BrandLightGray.copy(alpha = 0.4f))

            // Generate dynamic alerts based on live state
            val alerts = mutableListOf<Pair<String, String>>()

            if (live.rawJuice.tankLevelPct > 85f) alerts.add("Critical" to "Raw Juice Tank exceeding safe limit (>85%)")
            if (live.caneStock.levelPct < 30f) alerts.add("Warning" to "Cane Carrier stock is running low (<30%)")
            if (live.rawJuice.temperatureC > 80f) alerts.add("Warning" to "Juice temperature above normal bounds")
            if (live.dashboard.efficiency < 85f) alerts.add("Alert" to "Milling efficiency dropped below target threshold")
            if (live.connectionStatus == "DISCONNECTED") alerts.add("Critical" to "Telemetry connection lost. Check network.")

            if (alerts.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All systems operating within normal parameters.", fontSize = 12.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    alerts.take(3).forEach { alert ->
                        AlertRow(type = alert.first, message = alert.second)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertRow(type: String, message: String) {
    val color = if (type == "Critical") StatusRed else BrandOrange
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Column {
            Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(message, fontSize = 12.sp, color = BrandDeepNavy, lineHeight = 16.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Process Flow Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProcessFlowSection(live: MillLiveState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProcessStepCard("1", "Cane Carrier", R.drawable.cane_carrier, "${live.caneStock.levelPct.toInt()}%", "Load", "0 A", Modifier.weight(1f))
        ProcessStepCard("2", "Cane Cutter", R.drawable.cane_cutter, "95%", "Draw", "0 A", Modifier.weight(1f))
        ProcessStepCard("3", "Fiberizer", R.drawable.mill_fiberizer, "91%", "Draw", "0 A", Modifier.weight(1f))
        ProcessStepCard("4", "Mill Tandem", R.drawable.mill_tandem, "${("%.1f".format(live.dashboard.efficiency.toString().toFloatOrNull() ?: 0f))}%", "Avg", "0 A", Modifier.weight(1.1f), progressLabel = "Efficiency", extraMetricLabel = "OEE", extraMetricValue = "${("%.0f".format(live.dashboard.oee.toString().toFloatOrNull() ?: 0f))}%")
        ProcessStepCard("5", "Juice Tank", R.drawable.rawjuice_tank, "${live.rawJuice.tankLevelPct.toInt()}%", "Flow", "${live.rawJuice.flowLhr.toInt()} L/h", Modifier.weight(1f), progressLabel = "Level")
    }
}

@Composable
fun ProcessStepCard(
    step: String, title: String, imageRes: Int, progress: String, metricLabel: String, metricValue: String,
    modifier: Modifier = Modifier, progressLabel: String = "Progress", isBottleneck: Boolean = false,
    extraMetricLabel: String? = null, extraMetricValue: String? = null
) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.padding(2.dp).size(22.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, BrandLightGray.copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                }
                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("ANOMALY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier.wrapContentHeight().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) StatusRed else StatusGreen, CircleShape))
                    Text(if (step == "5") "Receiving" else "Running", fontSize = 11.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(progressLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) StatusRed else BrandDarkBlueGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(metricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(metricValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }

                if (extraMetricLabel != null && extraMetricValue != null) {
                    Divider(color = BrandLightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(extraMetricLabel, fontSize = 10.sp, color = BrandSteelGray)
                        Text(extraMetricValue, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTUITIVE VISUAL DATA CHARTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualMetricsGridSection(live: MillLiveState, modifier: Modifier = Modifier) {
    val eff = (live.dashboard.efficiency.toString().toFloatOrNull() ?: 0f).coerceIn(0f, 100f)
    val throughputHistory = listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * live.throughputKgHr }
    val flowHistory = listOf(0.98f, 0.99f, 1.01f, 1.0f, 0.97f, 1.0f, 1.0f).map { it * live.rawJuice.flowLhr }
    val bagasseLoss = (100f - eff).coerceAtLeast(0f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualCard(Modifier.weight(1f), "Throughput Trend", "${live.throughputKgHr.toInt()} kg/hr", Icons.Outlined.ShowChart, BrandMutedBlue) {
                ChartSparklineTrend(data = throughputHistory, color = BrandMutedBlue)
            }
            VisualCard(Modifier.weight(1f), "Extraction Goal", "${"%.1f".format(eff)}%", Icons.Outlined.DataExploration, BrandTeal) {
                ChartTargetGauge(value = eff / 100f, target = 0.95f, color = BrandTeal)
            }
            VisualCard(Modifier.weight(1f), "Steady Flow", "${live.rawJuice.flowLhr.toInt()} L/h", Icons.Outlined.WaterDrop, BrandCyan) {
                ChartBarTrend(data = flowHistory, color = BrandCyan)
            }
            VisualCard(Modifier.weight(1f), "Bagasse Loss", "${"%.1f".format(bagasseLoss)}%", Icons.Outlined.ViewInAr, BrandOrange) {
                ChartThresholdBar(value = bagasseLoss / 30f, color = BrandOrange)
            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val millAvg = (live.power.millMotorsTotalKw / 4).coerceAtLeast(10f)
            val millLoads = listOf(millAvg*0.9f, millAvg*1.1f, millAvg*0.95f, millAvg*1.05f)
            VisualCard(Modifier.weight(1f), "Mill Motors Load", "${live.power.millMotorsTotalKw.toInt()} kW", Icons.Outlined.Power, BrandDeepNavy) {
                ChartEquipmentBars(data = millLoads, color = BrandDeepNavy)
            }

            val prepAvg = (live.power.prepEquipmentTotalKw / 3).coerceAtLeast(10f)
            val prepLoads = listOf(prepAvg*1.2f, prepAvg*0.8f, prepAvg*1.0f)
            VisualCard(Modifier.weight(1f), "Prep Units Load", "${live.power.prepEquipmentTotalKw.toInt()} kW", Icons.Outlined.Power, BrandDarkBlueGray) {
                ChartEquipmentBars(data = prepLoads, color = BrandDarkBlueGray)
            }

            VisualCard(Modifier.weight(1f), "Total Power", "${live.power.totalKw.toInt()} kW", Icons.Outlined.Bolt, BrandSoftOrange) {
                ChartZoneDial(value = live.power.totalKw, max = 1500f)
            }

            val isConn = live.connectionStatus == "CONNECTED"
            VisualCard(Modifier.weight(1f), "Telemetry Status", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) BrandTeal else StatusRed) {
                ChartPulseIndicator(isConnected = isConn)
            }
        }
    }
}

@Composable
fun VisualCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, chartContent: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxHeight().glassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(title, fontSize = 12.sp, color = BrandSteelGray, maxLines = 1, fontWeight = FontWeight.Bold)
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                chartContent()
            }
        }
    }
}

// ─── Custom Charts ───

@Composable
fun ChartSparklineTrend(data: List<Float>, color: Color) {
    if (data.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
        val maxVal = data.maxOrNull() ?: 1f
        val minVal = (data.minOrNull() ?: 0f) * 0.9f
        val range = maxVal - minVal
        val stepX = size.width / (data.size - 1)

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - ((value - minVal) / range * size.height)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastY = size.height - ((data.last() - minVal) / range * size.height)
        drawCircle(color, radius = 4.dp.toPx(), center = Offset(size.width, lastY))
    }
}

@Composable
fun ChartTargetGauge(value: Float, target: Float, color: Color) {
    val clamped = value.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 2.dp)) {
        val strokeW = 8.dp.toPx()
        val radius = minOf(size.width, size.height) / 2f - strokeW
        val center = Offset(size.width / 2f, size.height / 2f + 4.dp.toPx())

        drawArc(BrandLightGray.copy(alpha = 0.25f), 135f, 270f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        drawArc(color, 135f, 270f * clamped, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        val targetAngle = Math.toRadians((135f + 270f * target).toDouble())
        val tx = center.x + (cos(targetAngle) * radius).toFloat()
        val ty = center.y + (sin(targetAngle) * radius).toFloat()
        drawCircle(BrandDeepNavy, strokeW/2f, Offset(tx, ty))
    }
}

@Composable
fun ChartBarTrend(data: List<Float>, color: Color) {
    if (data.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxVal = (data.maxOrNull() ?: 1f) * 1.1f
        val barW = (size.width / data.size) * 0.6f
        val gap = (size.width / data.size) * 0.4f

        data.forEachIndexed { i, value ->
            val h = (value / maxVal) * size.height
            val x = i * (barW + gap) + gap/2
            val y = size.height - h
            val barColor = if (i == data.lastIndex) color else color.copy(alpha = 0.4f)
            drawRoundRect(barColor, Offset(x, y), Size(barW, h), CornerRadius(4.dp.toPx()))
        }
    }
}

@Composable
fun ChartThresholdBar(value: Float, color: Color) {
    val clamped = value.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
        val h = 14.dp.toPx()
        val y = size.height / 2f - h / 2f

        drawRoundRect(BrandLightGray.copy(alpha = 0.25f), Offset(0f, y), Size(size.width, h), CornerRadius(h/2))
        drawRoundRect(StatusRed.copy(alpha = 0.2f), Offset(size.width * 0.7f, y), Size(size.width * 0.3f, h), CornerRadius(h/2))
        drawLine(StatusRed, Offset(size.width * 0.7f, y - 4.dp.toPx()), Offset(size.width * 0.7f, y + h + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawRoundRect(color, Offset(0f, y), Size(size.width * clamped, h), CornerRadius(h/2))
    }
}

@Composable
fun ChartEquipmentBars(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 6.dp)) {
        val maxVal = (data.maxOrNull() ?: 1f) * 1.2f
        val count = data.size
        val barW = size.width / count * 0.5f
        val gap = size.width / count * 0.5f

        data.forEachIndexed { i, value ->
            val h = (value / maxVal) * size.height
            val x = i * (barW + gap) + gap/2
            val y = size.height - h

            drawRoundRect(BrandLightGray.copy(alpha = 0.25f), Offset(x, 0f), Size(barW, size.height), CornerRadius(4.dp.toPx()))
            drawRoundRect(color, Offset(x, y), Size(barW, h), CornerRadius(4.dp.toPx()))
            drawCircle(color, 3.dp.toPx(), Offset(x + barW/2, size.height + 6.dp.toPx()))
        }
    }
}

@Composable
fun ChartZoneDial(value: Float, max: Float) {
    val clamped = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)) {
        val sw = 10.dp.toPx()
        val radius = minOf(size.width/2f, size.height) - sw
        val center = Offset(size.width/2f, size.height)

        drawArc(BrandTeal.copy(alpha = 0.3f), 180f, 90f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )
        drawArc(BrandOrange.copy(alpha = 0.3f), 270f, 54f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )
        drawArc(StatusRed.copy(alpha = 0.3f), 324f, 36f, false,
            Offset(center.x - radius, center.y - radius),
            Size(radius*2, radius*2),
            style = Stroke(sw, cap = StrokeCap.Butt)
        )

        val angleRad = Math.toRadians((180 + 180 * clamped).toDouble())
        val nx = center.x + (cos(angleRad) * (radius - 4.dp.toPx())).toFloat()
        val ny = center.y + (sin(angleRad) * (radius - 4.dp.toPx())).toFloat()

        drawLine(BrandDeepNavy, center, Offset(nx, ny), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(BrandDeepNavy, 4.dp.toPx(), center)
    }
}

@Composable
fun ChartPulseIndicator(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val color = if (isConnected) BrandTeal else StatusRed
            drawCircle(color, 8.dp.toPx())
            if (isConnected) {
                drawCircle(
                    color = color.copy(alpha = 1f - pulse),
                    radius = (8.dp.toPx()) + (20.dp.toPx() * pulse),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroJuiceTankLevelVisual(live: MillLiveState, modifier: Modifier = Modifier) {
    val levelPct = live.rawJuice.tankLevelPct.coerceIn(0.0F, 100.0F).toFloat()

    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WaterDrop, null, tint = BrandOrange, modifier = Modifier.size(24.dp))
                Text("Raw Juice Tank Status", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).fillMaxHeight()
                        .background(BrandLightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val color = if (levelPct > 85f) StatusRed else BrandOrange
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(levelPct / 100f)
                            .background(color, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    )
                    Text(
                        "${levelPct.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (levelPct > 40f) Color.White else BrandDeepNavy,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
                    Column {
                        Text("Current Volume", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.1f".format(live.rawJuice.volumeFlowM3hr.toString().toFloatOrNull() ?: 0f)} m³", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Temp", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.1f".format(live.rawJuice.temperatureC.toString().toFloatOrNull() ?: 0f)} °C", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                    Divider(color = BrandLightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Flow", fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
                        Text("${live.rawJuice.flowLhr.toInt()} L/h", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionSummaryCard(live: MillLiveState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.ListAlt, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("Stream Performance Specs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }
            SummaryRow("Actual Throughput", "${live.throughputKgHr.toInt()} kg/hr")
            SummaryRow("Baseline Target", "15,000 kg/hr")
            SummaryRow("Juice Density", "${"%.1f".format(live.rawJuice.densityKgM3.toString().toFloatOrNull() ?: 0f)} kg/m³")
            SummaryRow("H3 Outlet Target", "${"%.1f".format(live.rawJuice.heater3OutletC.toString().toFloatOrNull() ?: 0f)} °C")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

@Composable
fun PowerDistributionChart(live: MillLiveState, modifier: Modifier = Modifier) {
    val millKw = live.power.millMotorsTotalKw.toString().toDoubleOrNull() ?: 0.0
    val prepKw = live.power.prepEquipmentTotalKw.toString().toDoubleOrNull() ?: 0.0
    val totalKw = (live.power.totalKw.toString().toDoubleOrNull() ?: 0.0).coerceAtLeast(1.0)
    val otherKw = (totalKw - millKw - prepKw).coerceAtLeast(0.0)

    Box(modifier = modifier.fillMaxWidth().glassCard()) {
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Bolt, null, tint = BrandDeepNavy, modifier = Modifier.size(20.dp))
                Text("Power Distribution", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
            }

            Row(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp))) {
                if (millKw > 0) Box(Modifier.weight((millKw / totalKw).toFloat()).fillMaxHeight().background(BrandDeepNavy))
                if (prepKw > 0) Box(Modifier.weight((prepKw / totalKw).toFloat()).fillMaxHeight().background(BrandCyan))
                if (otherKw > 0) Box(Modifier.weight((otherKw / totalKw).toFloat()).fillMaxHeight().background(BrandLightGray))
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DistributionLegendItem(color = BrandDeepNavy, label = "Mill Motors", value = "${millKw.toInt()} kW")
                DistributionLegendItem(color = BrandCyan, label = "Prep Equipment", value = "${prepKw.toInt()} kW")
                DistributionLegendItem(color = BrandLightGray, label = "Auxiliary", value = "${otherKw.toInt()} kW")
            }
        }
    }
}

@Composable
fun DistributionLegendItem(color: Color, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Text(label, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Medium)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDeepNavy)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomSummaryRow(live: MillLiveState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryBlock(Modifier.weight(1f), "Section OEE", "${"%.1f".format(live.dashboard.oee.toString().toFloatOrNull() ?: 0f)}%", Icons.Outlined.Speed, BrandTeal)
        SummaryBlock(Modifier.weight(1f), "Total Power", "${live.power.totalKw.toInt()} kW", Icons.Outlined.Bolt, BrandDeepNavy)
        SummaryBlock(Modifier.weight(1f), "Juice Flow", "${"%.1f".format(live.rawJuice.volumeFlowM3hr.toString().toFloatOrNull() ?: 0f)} m³/h", Icons.Outlined.WaterDrop, BrandCyan)
        SummaryBlock(Modifier.weight(1f), "Feed Metrics", "${"%.1f".format(live.throughputKgS.toString().toFloatOrNull() ?: 0f)} kg/s", Icons.Outlined.ShowChart, BrandMutedBlue)
    }
}

@Composable
fun SummaryBlock(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(modifier = modifier.glassCard()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, fontSize = 13.sp, color = BrandSteelGray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrandDeepNavy)
            }
        }
    }
}