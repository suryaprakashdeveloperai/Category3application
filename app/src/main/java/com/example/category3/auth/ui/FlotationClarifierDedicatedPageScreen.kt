package com.example.category3.auth.ui

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Warning
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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

// ─── Routing Constants ───────────────────────────────────────────────────────
const val WORKFLOW_DASHBOARD = "workflow_dashboard"

// ─── Colors ──────────────────────────────────────────────────────────────────
object FlotationColors {
    val Bg = Color(0xFFF6F6F7)
    val Cyan = Color(0xFF47B3E2)
    val Teal = Color(0xFF0D9488)
    val DeepNavy = Color(0xFF1A2B4C)
    val SoftOrange = Color(0xFFFFB020)
    val Orange = Color(0xFFFF6B35)
    val MutedBlue = Color(0xFF6366F1)
    val DarkBlueGray = Color(0xFF334155)
    val SteelGray = Color(0xFF64748B)
    val LightGray = Color(0xFFE2E8F0)
    val StatusGreen = Color(0xFF26C281)
    val StatusRed = Color(0xFFFF4D4D)
}

// ─── Print & CSV Generators ────────────────────────────────────────────────
private var activePrintWebView: WebView? = null

fun printFlotationReport(context: Context, live: FlotationClarifierLiveState) {
    Toast.makeText(context, "Preparing Document for Printer...", Toast.LENGTH_SHORT).show()

    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw

    val htmlBuilder = """
        <html>
        <head>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #0F172A; padding: 20px; }
                h1 { border-bottom: 2px solid #0D9488; padding-bottom: 10px; margin-bottom: 20px; font-size: 24px; }
                h2 { font-size: 18px; color: #334155; margin-top: 25px; margin-bottom: 10px; background-color: #F6F6F7; padding: 8px; border-radius: 4px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                th, td { border: 1px solid #E2E8F0; padding: 10px; text-align: left; font-size: 14px; }
                th { width: 40%; background-color: #F8FAFC; color: #64748B; font-weight: bold; }
                td { width: 60%; font-weight: 500; }
                .footer { margin-top: 30px; font-size: 12px; color: #94A3B8; text-align: center; border-top: 1px solid #E2E8F0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <h1>Flotation Clarifier Section - Live Operations Report</h1>
            
            <h2>General Information</h2>
            <table>
                <tr><th>Timestamp</th><td>$timestamp</td></tr>
                <tr><th>Batch ID</th><td>${live.dashboard.batchId}</td></tr>
                <tr><th>Section Status</th><td>${live.dashboard.sectionStatus.name}</td></tr>
                <tr><th>Telemetry Link</th><td>${live.connectionStatus}</td></tr>
            </table>

            <h2>Clarifier Units</h2>
            <table>
                ${live.dashboard.units.joinToString("") { "<tr><th>${it.name} Status</th><td>${it.status.name} (${"%.1f".format(it.vfdSpeedPct)}%) - ${it.statusText}</td></tr>" }}
            </table>

            <h2>Process Flows</h2>
            <table>
                <tr><th>Vacuum Pump Status</th><td>${live.vacuumPumpStatus.name}</td></tr>
                <tr><th>FC MOND Flow</th><td>${"%,.0f".format(live.fcMondFlow)} L/h</td></tr>
            </table>

            <h2>Clear Juice Parameters</h2>
            <table>
                <tr><th>Clear Juice Flow Rate</th><td>${"%,.0f".format(cjFlowLhr)} L/h</td></tr>
                <tr><th>Tank Level</th><td>${"%.1f".format(live.clearJuiceTankLevel)} %</td></tr>
                <tr><th>Juice Density</th><td>${"%.2f".format(live.clearJuiceDensity)} kg/m³</td></tr>
                <tr><th>Filter Status</th><td>${if (live.cjFilterOn) "ENGAGED" else "BYPASS"}</td></tr>
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
            val jobName = "Flotation_Clarifier_Report_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

fun generateFlotationCsvReport(live: FlotationClarifierLiveState): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw

    return buildString {
        appendLine("Flotation Clarifier Section - Live Operations Report")
        appendLine("Generated,$timestamp")
        appendLine()
        appendLine("Section,Metric,Value,Unit")
        appendLine("General,Batch ID,${live.dashboard.batchId},")
        appendLine("General,Section Status,${live.dashboard.sectionStatus.name},")
        appendLine("General,Telemetry Link,${live.connectionStatus},")

        live.dashboard.units.forEach { unit ->
            appendLine("Clarifier Units,${unit.name} VFD Speed,${"%.1f".format(unit.vfdSpeedPct)},%")
            appendLine("Clarifier Units,${unit.name} Status,${unit.status.name},")
        }

        appendLine("Process,Vacuum Pump Status,${live.vacuumPumpStatus.name},")
        appendLine("Process,FC MOND Flow,${live.fcMondFlow},L/h")

        appendLine("Clear Juice,Flow Rate,$cjFlowLhr,L/h")
        appendLine("Clear Juice,Tank Level,${"%.1f".format(live.clearJuiceTankLevel)},%")
        appendLine("Clear Juice,Density,${"%.2f".format(live.clearJuiceDensity)},kg/m³")
        appendLine("Clear Juice,Filter Status,${if (live.cjFilterOn) "ENGAGED" else "BYPASS"},")
    }
}

// ─── HIGH PERFORMANCE UI MODIFIER ─────────────────────────────────────────
fun Modifier.flotationGlassCard() = composed {
    this.shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF8A9AAB).copy(0.5f), ambientColor = Color(0xFF8A9AAB).copy(0.2f))
        .clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(Color.White.copy(0.65f), Color(0xFFC9D4E2).copy(0.4f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
        .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.9f), Color(0xFFA5B4C7).copy(0.3f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)), RoundedCornerShape(24.dp))
        .padding(16.dp)
}

// ─── UI Composables ───────────────────────────────────────────────────────

@Composable
fun FlotationClarifierDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: FlotationClarifierDedicatedViewModel = viewModel(factory = FlotationClarifierDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()

    FlotationClarifierDedicatedPageContent(
        live = live,
        onNavigateToScreen = onNavigateToScreen,
        getCsvData = { generateFlotationCsvReport(live) }
    )
}

@Composable
fun FlotationClarifierDedicatedPageContent(
    live: FlotationClarifierLiveState,
    onNavigateToScreen: (String) -> Unit = {},
    getCsvData: () -> String = { "" }
) {
    val state = live.dashboard
    val context = LocalContext.current

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val isPortrait = config.orientation == Configuration.ORIENTATION_PORTRAIT
    val isTablet = screenWidth >= 600
    val isExpanded = isTablet && !isPortrait

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

    Box(modifier = Modifier.fillMaxSize().background(FlotationColors.Bg)) {
        val cyanBrush = remember { Brush.radialGradient(listOf(FlotationColors.Cyan.copy(alpha = 0.12f), Color.Transparent)) }
        val orangeBrush = remember { Brush.radialGradient(listOf(FlotationColors.SoftOrange.copy(alpha = 0.1f), Color.Transparent)) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(cyanBrush, radius = size.width / 2.5f, center = Offset(0f, 0f))
            drawCircle(orangeBrush, radius = size.width / 2f, center = Offset(size.width, size.height))
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            FlotationWorkspaceHeader(
                batchId = state.batchId,
                status = state.sectionStatus,
                onNavigateDashboard = { onNavigateToScreen(WORKFLOW_DASHBOARD) },
                onExportClick = { createDocumentLauncher.launch("Flotation_Clarifier_Data_${System.currentTimeMillis()}.csv") },
                onPrintClick = { printFlotationReport(context, live) },
                onLogEntryClick = { onNavigateToScreen("flotation_manual") }, // Placeholder for manual entry route
                isCompact = !isTablet,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (isExpanded) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(2.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ClarifierProcessFlowSection(state.units, isCompact = false, modifier = Modifier.weight(1.2f))
                        FlotationVisualMetricsGridSection(live, isExpanded = true, modifier = Modifier.weight(1f))
                        FlotationBottomSummaryRow(state, isExpanded = true, modifier = Modifier.wrapContentHeight())
                    }
                    Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeroFCVisual(live, modifier = Modifier.weight(1f))
                        IdentityContextCard(state, modifier = Modifier.wrapContentHeight())
                        FcActiveAlertsPanel(live.alerts, modifier = Modifier.wrapContentHeight())
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClarifierProcessFlowSection(state.units, isCompact = true, modifier = Modifier.wrapContentHeight())
                    HeroFCVisual(live, modifier = Modifier.height(if (isTablet) 300.dp else 260.dp))
                    FlotationVisualMetricsGridSection(live, isExpanded = false, modifier = Modifier.wrapContentHeight())

                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IdentityContextCard(state, modifier = Modifier.weight(1f))
                            FcActiveAlertsPanel(live.alerts, modifier = Modifier.weight(1f))
                        }
                    } else {
                        IdentityContextCard(state, modifier = Modifier.fillMaxWidth())
                        FcActiveAlertsPanel(live.alerts, modifier = Modifier.fillMaxWidth())
                    }

                    FlotationBottomSummaryRow(state, isExpanded = false, modifier = Modifier.wrapContentHeight())
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ─── Header & Meta ──────────────────────────────────────────────────────────

@Composable
fun FlotationWorkspaceHeader(
    batchId: String,
    status: EquipmentStatus,
    onNavigateDashboard: () -> Unit,
    onExportClick: () -> Unit,
    onPrintClick: () -> Unit,
    onLogEntryClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val isHealthy = status == EquipmentStatus.RUNNING || status == EquipmentStatus.HEALTHY
    val statusColor = if (isHealthy) FlotationColors.StatusGreen else FlotationColors.StatusRed
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNavigateDashboard, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = FlotationColors.DeepNavy)
                }
                Text("Flotation Clarifier", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy)

                if (!isCompact) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50)).border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                            Text(if (isHealthy) "Running Smoothly" else "Intervention Req", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!isCompact) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(FlotationColors.Cyan.copy(alpha = 0.15f)).border(1.dp, FlotationColors.Cyan.copy(alpha = 0.3f), RoundedCornerShape(50)).clickable { menuExpanded = true }.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Download, "Options", tint = FlotationColors.DeepNavy, modifier = Modifier.size(16.dp))
                            Text("Export / Print", color = FlotationColors.DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Download CSV File") }, onClick = { menuExpanded = false; onExportClick() }, leadingIcon = { Icon(Icons.Outlined.Description, null, tint = FlotationColors.Cyan) })
                            DropdownMenuItem(text = { Text("Print Report") }, onClick = { menuExpanded = false; onPrintClick() }, leadingIcon = { Icon(Icons.Outlined.Print, null, tint = FlotationColors.DeepNavy) })
                        }
                    }
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(FlotationColors.DeepNavy).clickable { onLogEntryClick() }.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, "Log Entry", tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Log Entry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isCompact) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50)).border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                        Text(if (isHealthy) "Running" else "Intervention Req", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box {
                    Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(FlotationColors.Cyan.copy(alpha = 0.15f)).border(1.dp, FlotationColors.Cyan.copy(alpha = 0.3f), RoundedCornerShape(50)).clickable { menuExpanded = true }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("Export", color = FlotationColors.DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Download CSV") }, onClick = { menuExpanded = false; onExportClick() })
                        DropdownMenuItem(text = { Text("Print Report") }, onClick = { menuExpanded = false; onPrintClick() })
                    }
                }
                Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(FlotationColors.DeepNavy).clickable { onLogEntryClick() }.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("Log Entry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.End) {
            if (isCompact) FlotationHeaderMetaItem("System Loop", "Clarification")
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FlotationHeaderMetaItem("Batch ID", batchId)
                if (!isCompact) FlotationHeaderMetaItem("System Loop", "Clarification")
            }
        }
    }
}

@Composable
fun FlotationHeaderMetaItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 11.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FlotationColors.DeepNavy)
    }
}

// ─── Process Flow ────────────────────────────────────────────────────────────

@Composable
fun ClarifierProcessFlowSection(units: List<ClarifierUnitLive>, isCompact: Boolean, modifier: Modifier = Modifier) {
    if (isCompact) {
        Row(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            units.forEachIndexed { index, unit ->
                FlotationProcessStepCard("${index + 1}", unit.name, R.drawable.floatation_clarifier, "${"%.1f".format(unit.vfdSpeedPct)}%", unit.status == EquipmentStatus.FAULT, unit.inletOpen, unit.outletOpen, Modifier.width(260.dp).height(180.dp))
            }
        }
    } else {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            units.forEachIndexed { index, unit ->
                FlotationProcessStepCard("${index + 1}", unit.name, R.drawable.floatation_clarifier, "${"%.1f".format(unit.vfdSpeedPct)}%", unit.status == EquipmentStatus.FAULT, unit.inletOpen, unit.outletOpen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FlotationProcessStepCard(step: String, title: String, imageRes: Int, progress: String, isBottleneck: Boolean, inletOpen: Boolean, outletOpen: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.flotationGlassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(painter = painterResource(id = imageRes), contentDescription = title, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.padding(2.dp).size(22.dp).background(Color.White.copy(alpha = 0.85f), CircleShape).border(1.dp, FlotationColors.LightGray.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy)
                }
                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(FlotationColors.StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("FAULT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.wrapContentHeight().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) FlotationColors.StatusRed else FlotationColors.StatusGreen, CircleShape))
                    Text(if (isBottleneck) "Stopped/Fault" else "Running", fontSize = 11.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Drive VFD", fontSize = 10.sp, color = FlotationColors.SteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) FlotationColors.StatusRed else FlotationColors.DarkBlueGray)
                    }
                }

                Divider(color = FlotationColors.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Inlet Valve", fontSize = 10.sp, color = FlotationColors.SteelGray)
                    Text(if(inletOpen) "OPEN" else "CLOSED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(inletOpen) FlotationColors.Teal else FlotationColors.SteelGray)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Outlet Valve", fontSize = 10.sp, color = FlotationColors.SteelGray)
                    Text(if(outletOpen) "OPEN" else "CLOSED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(outletOpen) FlotationColors.MutedBlue else FlotationColors.SteelGray)
                }
            }
        }
    }
}

// ─── Visual Grid ─────────────────────────────────────────────────────────────

private val FlowTrendPattern = floatArrayOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f)
private val FcMondTrendPattern = floatArrayOf(0.98f, 0.99f, 1.01f, 1.0f, 0.97f, 1.0f, 1.0f)

@Composable
fun FlotationVisualMetricsGridSection(live: FlotationClarifierLiveState, isExpanded: Boolean, modifier: Modifier = Modifier) {
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw
    val flowHistory = remember(cjFlowLhr) { FlowTrendPattern.map { it * cjFlowLhr } }
    val fcMondHistory = remember(live.fcMondFlow) { FcMondTrendPattern.map { it * live.fcMondFlow } }
    val densityRatio = ((live.clearJuiceDensity - 1.0f) / 0.1f).coerceIn(0f, 1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isExpanded) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlotationVisualCard(Modifier.weight(1f), "Clear Juice Flow", "${"%,.0f".format(cjFlowLhr)} L/h", Icons.Outlined.ShowChart, FlotationColors.MutedBlue) { FlotationChartSparklineTrend(data = flowHistory, color = FlotationColors.MutedBlue) }
                FlotationVisualCard(Modifier.weight(1f), "Juice Density", "%.2f".format(live.clearJuiceDensity), Icons.Outlined.DataExploration, FlotationColors.Teal) { FlotationChartTargetGauge(value = densityRatio, target = 0.5f, color = FlotationColors.Teal) }
                FlotationVisualCard(Modifier.weight(1f), "FC MOND Flow", "${"%,.0f".format(live.fcMondFlow)} L/h", Icons.Outlined.Speed, FlotationColors.Orange) { FlotationChartEquipmentBars(data = fcMondHistory, color = FlotationColors.Orange) }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isConn = live.connectionStatus == "CONNECTED"
                val pumpColor = if (live.vacuumPumpStatus == EquipmentStatus.RUNNING) FlotationColors.StatusGreen else FlotationColors.StatusRed
                FlotationMiniStatusCard(Modifier.weight(1f), "Vacuum Pump Status", live.vacuumPumpStatus.name, Icons.Outlined.Power, pumpColor)
                FlotationMiniStatusCard(Modifier.weight(1f), "Telemetry Stream", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) FlotationColors.Teal else FlotationColors.StatusRed)
                Box(Modifier.weight(1f)) // Empty block to align grid
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlotationVisualCard(Modifier.weight(1f).height(130.dp), "Clear Juice Flow", "${"%,.0f".format(cjFlowLhr)} L/h", Icons.Outlined.ShowChart, FlotationColors.MutedBlue) { FlotationChartSparklineTrend(data = flowHistory, color = FlotationColors.MutedBlue) }
                FlotationVisualCard(Modifier.weight(1f).height(130.dp), "Juice Density", "%.2f".format(live.clearJuiceDensity), Icons.Outlined.DataExploration, FlotationColors.Teal) { FlotationChartTargetGauge(value = densityRatio, target = 0.5f, color = FlotationColors.Teal) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlotationVisualCard(Modifier.weight(1f).height(130.dp), "FC MOND Flow", "${"%,.0f".format(live.fcMondFlow)} L/h", Icons.Outlined.Speed, FlotationColors.Orange) { FlotationChartEquipmentBars(data = fcMondHistory, color = FlotationColors.Orange) }
                val pumpColor = if (live.vacuumPumpStatus == EquipmentStatus.RUNNING) FlotationColors.StatusGreen else FlotationColors.StatusRed
                FlotationMiniStatusCard(Modifier.weight(1f).height(130.dp), "Vacuum Pump", live.vacuumPumpStatus.name, Icons.Outlined.Power, pumpColor)
            }
        }
    }
}

@Composable
fun FlotationVisualCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, chart: @Composable () -> Unit) {
    Box(modifier = modifier.flotationGlassCard().padding(0.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 11.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.SemiBold)
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = FlotationColors.DeepNavy)
            Box(modifier = Modifier.fillMaxWidth().height(24.dp)) { chart() }
        }
    }
}

@Composable
fun FlotationMiniStatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Box(modifier = modifier.flotationGlassCard()) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(color.copy(alpha = 0.15f), CircleShape).border(1.dp, color.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, fontSize = 12.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Bold)
                    Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy)
                }
            }
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                FlotationChartPulseIndicator(isConnected = value == "RUNNING" || value == "Online", overrideColor = color)
            }
        }
    }
}

@Composable
fun FlotationChartPulseIndicator(isConnected: Boolean, overrideColor: Color? = null) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "pulse")
    val strokeStyle = remember { Stroke(width = 4f) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val color = overrideColor ?: (if (isConnected) FlotationColors.Teal else FlotationColors.StatusRed)
            drawCircle(color, 6.dp.toPx())
            if (isConnected) drawCircle(color = color.copy(alpha = 1f - pulse), radius = (6.dp.toPx()) + (10.dp.toPx() * pulse), style = strokeStyle)
        }
    }
}

@Composable
fun FlotationChartSparklineTrend(data: List<Float>, color: Color) {
    val style = remember { Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round) }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val step = size.width / (data.size - 1).coerceAtLeast(1)
        val max = data.maxOrNull() ?: 1f; val min = data.minOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(0.01f)
        data.forEachIndexed { i, v ->
            val x = i * step; val y = size.height - ((v - min) / range * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = style)
    }
}

@Composable
fun FlotationChartTargetGauge(value: Float, target: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(FlotationColors.LightGray, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = 8f, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, size.height/2), Offset(size.width * value, size.height/2), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(FlotationColors.DeepNavy, radius = 6f, center = Offset(size.width * target, size.height/2))
    }
}

@Composable
fun FlotationChartEquipmentBars(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val max = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barWidth = size.width / (data.size * 2)
        data.forEachIndexed { i, v ->
            val pct = v / max
            drawRect(color, topLeft = Offset(i * barWidth * 2f, size.height * (1 - pct)), size = Size(barWidth, size.height * pct))
        }
    }
}

// ─── Right Rail & Summary ────────────────────────────────────────────────────

@Composable
fun HeroFCVisual(live: FlotationClarifierLiveState, modifier: Modifier = Modifier) {
    val levelPct = live.clearJuiceTankLevel.coerceIn(0.0F, 100.0F)
    val cjFlowLhr = if (live.clearJuiceFlowRaw in 0f..200f) live.clearJuiceFlowRaw * 1000f else live.clearJuiceFlowRaw

    Box(modifier = modifier.fillMaxWidth().flotationGlassCard()) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WaterDrop, null, tint = FlotationColors.Cyan, modifier = Modifier.size(24.dp))
                Text("Clear Juice Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FlotationColors.DeepNavy)
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).height(120.dp)
                        .background(FlotationColors.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val color = if (levelPct > 85f) FlotationColors.Orange else FlotationColors.Cyan
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(levelPct / 100f).background(color, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)))
                    Text("${levelPct.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (levelPct > 40f) Color.White else FlotationColors.DeepNavy, modifier = Modifier.padding(bottom = 6.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Clear Flow Rate", fontSize = 12.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%,.0f".format(cjFlowLhr)} L/h", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy)
                    }
                    Divider(color = FlotationColors.LightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Density", fontSize = 12.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.2f".format(live.clearJuiceDensity)} kg/m³", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FlotationColors.DeepNavy)
                    }
                    Divider(color = FlotationColors.LightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Filter", fontSize = 12.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text(if (live.cjFilterOn) "ENGAGED" else "BYPASS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(live.cjFilterOn) FlotationColors.StatusGreen else FlotationColors.SteelGray)
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityContextCard(state: FlotationClarifierDashboardState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().flotationGlassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Domain, null, tint = FlotationColors.DeepNavy, modifier = Modifier.size(18.dp))
                Text("Plant Context Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FlotationColors.DeepNavy)
            }
            Divider(color = FlotationColors.LightGray.copy(alpha = 0.3f))
            FlotationSummaryFieldRow("Section", "FC Loop")
            FlotationSummaryFieldRow("Batch Ref", state.batchId)
            FlotationSummaryFieldRow("Shift Start", state.startTime)
            FlotationSummaryFieldRow("Operator", state.userName)
        }
    }
}

@Composable
fun FcActiveAlertsPanel(alerts: List<String>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().flotationGlassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = FlotationColors.StatusRed, modifier = Modifier.size(18.dp))
                Text("Critical Alerts (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FlotationColors.StatusRed)
            }
            Divider(color = FlotationColors.LightGray.copy(alpha = 0.3f))

            if (alerts.isEmpty()) {
                Text("Process thresholds nominal. No limit breaches.", fontSize = 12.sp, color = FlotationColors.SteelGray)
            } else {
                alerts.forEach { error ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(FlotationColors.StatusRed, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(error, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = FlotationColors.DeepNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun FlotationBottomSummaryRow(state: FlotationClarifierDashboardState, isExpanded: Boolean, modifier: Modifier = Modifier) {
    val kpis = state.kpis
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isExpanded) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kpis.take(4).forEach { kpi ->
                    FlotationSummaryBlock(Modifier.weight(1f), kpi.first, kpi.second, Icons.Outlined.Speed, FlotationColors.Teal)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kpis.take(2).forEach { kpi -> FlotationSummaryBlock(Modifier.weight(1f).height(80.dp), kpi.first, kpi.second, Icons.Outlined.Speed, FlotationColors.Teal) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kpis.drop(2).take(2).forEach { kpi -> FlotationSummaryBlock(Modifier.weight(1f).height(80.dp), kpi.first, kpi.second, Icons.Outlined.Speed, FlotationColors.Teal) }
            }
        }
    }
}

@Composable
fun FlotationSummaryBlock(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Box(modifier = modifier.flotationGlassCard().padding(0.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontSize = 11.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FlotationColors.DeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun FlotationSummaryFieldRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = FlotationColors.SteelGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FlotationColors.DeepNavy)
    }
}