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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewInAr
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
import kotlin.math.abs

// ─── Standardized Brand Colors for Defecator ──────────────────────────────
object DefecatorColors {
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

// ─── Print Report Generator ────────────────────────────────────────────────
private var activePrintWebView: WebView? = null

fun printDefecatorReport(context: Context, live: DefecatorLiveState) {
    Toast.makeText(context, "Preparing Document for Printer...", Toast.LENGTH_SHORT).show()

    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val p = live.process
    val c = live.clearJuice

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
            <h1>Defecator Section - Live Operations Report</h1>
            
            <h2>General Information</h2>
            <table>
                <tr><th>Timestamp</th><td>$timestamp</td></tr>
                <tr><th>Batch ID</th><td>${live.dashboard.batchId}</td></tr>
                <tr><th>Section Status</th><td>${live.dashboard.sectionStatus.name}</td></tr>
                <tr><th>Process Stability</th><td>${"%.1f".format(live.dashboard.processStability)} / 100</td></tr>
                <tr><th>Telemetry Link</th><td>${live.connectionStatus}</td></tr>
            </table>

            <h2>Process & Buffers</h2>
            <table>
                <tr><th>DJ Tank Level</th><td>${"%.1f".format(p.djTankLevel)} %</td></tr>
                <tr><th>DJ Active Pump Load</th><td>${"%.2f".format(p.djActivePumpA)} A</td></tr>
                <tr><th>pH Level</th><td>${"%.2f".format(p.pH)} pH</td></tr>
                <tr><th>SRT Buffer Tank Level</th><td>${"%.1f".format(p.srtBufferLevel)} mm</td></tr>
            </table>

            <h2>Thermal Stage</h2>
            <table>
                <tr><th>Heater 1 Outlet Temp</th><td>${"%.1f".format(p.heater1OutletC)} °C</td></tr>
                <tr><th>Heater 2 Outlet Temp</th><td>${"%.1f".format(p.heater2OutletC)} °C</td></tr>
                <tr><th>Heater 3 PV (Current)</th><td>${"%.1f".format(p.heater3PvC)} °C</td></tr>
                <tr><th>Heater 3 SP (Target)</th><td>${"%.1f".format(p.heater3SpC)} °C</td></tr>
                <tr><th>Heater 3 Steam Valve</th><td>${"%.1f".format(p.heater3SteamValvePct)} %</td></tr>
            </table>

            <h2>Clear Juice Discharge</h2>
            <table>
                <tr><th>Clear Juice Flow Rate</th><td>${"%,.0f".format(c.flow)} L/h</td></tr>
                <tr><th>Tank Level</th><td>${"%.1f".format(c.tankLevel)} %</td></tr>
                <tr><th>Density</th><td>${"%.2f".format(c.density)} kg/m³</td></tr>
                <tr><th>Filter Status</th><td>${if (c.filterOn) "ENGAGED" else "BYPASS"}</td></tr>
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
            val jobName = "Defecator_Data_Report_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

// ─── Modifier Extension ───────────────────────────────────────────────────
fun Modifier.defecatorGlassCard() = composed {
    this.shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF8A9AAB).copy(0.5f), ambientColor = Color(0xFF8A9AAB).copy(0.2f))
        .clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(Color.White.copy(0.65f), Color(0xFFC9D4E2).copy(0.4f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
        .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.9f), Color(0xFFA5B4C7).copy(0.3f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)), RoundedCornerShape(24.dp))
        .padding(16.dp)
}

// ─── UI Composables ───────────────────────────────────────────────────────

@Composable
fun DefecatorDedicatedPageScreen(
    userName: String = "Operator",
    userRole: String = "Shift Engineer",
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {}
) {
    val vm: DefecatorDedicatedViewModel = viewModel(factory = DefecatorDedicatedViewModel.provideFactory(userName, userRole))
    val live by vm.state.collectAsStateWithLifecycle()

    DefecatorDedicatedPageContent(
        live = live,
        onBack = onBack,
        onNavigateToScreen = onNavigateToScreen,
        getCsvData = { generateDefecatorCsvReport(live) }
    )
}

@Composable
fun DefecatorDedicatedPageContent(
    live: DefecatorLiveState,
    onBack: () -> Unit = {},
    onNavigateToScreen: (String) -> Unit = {},
    getCsvData: () -> String = { "" }
) {
    val state = live.dashboard
    val context = LocalContext.current

    // Responsive Dimensions
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val isPortrait = config.orientation == Configuration.ORIENTATION_PORTRAIT
    val isTablet = screenWidth >= 600
    val isExpanded = isTablet && !isPortrait

    val phFault = live.process.pH != 0f && (live.process.pH < 6.8f || live.process.pH > 7.8f)
    val djPumpFault = live.process.djActivePumpA in 0.01f..0.5f
    val heater3Dev = abs(live.process.heater3PvC - live.process.heater3SpC)
    val heater3Fault = live.process.heater3SpC > 0f && heater3Dev > 5f

    // Document Launcher for CSV
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
        val fileName = "Defecator_Section_Data_${System.currentTimeMillis()}.csv"
        createDocumentLauncher.launch(fileName)
    }

    val onPrintClick = {
        printDefecatorReport(context, live)
    }

    Box(modifier = Modifier.fillMaxSize().background(DefecatorColors.Bg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(DefecatorColors.Cyan.copy(alpha = 0.12f), Color.Transparent)), radius = size.width / 2.5f, center = Offset(0f, 0f))
            drawCircle(Brush.radialGradient(listOf(DefecatorColors.SoftOrange.copy(alpha = 0.1f), Color.Transparent)), radius = size.width / 2f, center = Offset(size.width, size.height))
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            DefecatorWorkspaceHeader(
                batchId = state.batchId,
                stability = "%.1f".format(state.processStability),
                status = state.sectionStatus,
                onBack = onBack,
                onExportClick = onExportCsvClick,
                onPrintClick = onPrintClick,
                onLogEntryClick = { onNavigateToScreen("defecator_manual") },
                isCompact = !isTablet,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (isExpanded) {
                // Main 2-Column Layout for Tablet Landscape
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(2.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DefecationProcessFlowSection(live, phFault, heater3Fault, isCompact = false, modifier = Modifier.weight(1.2f))
                        DefecatorVisualMetricsGridSection(live, isExpanded = true, modifier = Modifier.weight(1f))
                        DefecatorBottomSummaryRow(live, isExpanded = true, modifier = Modifier.wrapContentHeight())
                    }
                    Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeroCJTankLevelVisual(live, modifier = Modifier.weight(1f))
                        FlowAnalysisCard(state, modifier = Modifier.wrapContentHeight())
                        AlarmsCriticalPanel(live.alerts, phFault, djPumpFault, heater3Fault, modifier = Modifier.wrapContentHeight())
                    }
                }
            } else {
                // Adaptive Scrolling Layout for Phone / Tablet Portrait
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DefecationProcessFlowSection(live, phFault, heater3Fault, isCompact = true, modifier = Modifier.wrapContentHeight())
                    HeroCJTankLevelVisual(live, modifier = Modifier.height(if (isTablet) 300.dp else 260.dp))
                    DefecatorVisualMetricsGridSection(live, isExpanded = false, modifier = Modifier.wrapContentHeight())

                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FlowAnalysisCard(state, modifier = Modifier.weight(1f))
                            AlarmsCriticalPanel(live.alerts, phFault, djPumpFault, heater3Fault, modifier = Modifier.weight(1f))
                        }
                    } else {
                        FlowAnalysisCard(state, modifier = Modifier.fillMaxWidth())
                        AlarmsCriticalPanel(live.alerts, phFault, djPumpFault, heater3Fault, modifier = Modifier.fillMaxWidth())
                    }

                    DefecatorBottomSummaryRow(live, isExpanded = false, modifier = Modifier.wrapContentHeight())
                    Spacer(modifier = Modifier.height(24.dp)) // Scroll Padding
                }
            }
        }
    }
}

// ─── CSV Report Generator ─────────────────────────────────────────────────
fun generateDefecatorCsvReport(live: DefecatorLiveState): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val p = live.process
    val c = live.clearJuice

    return buildString {
        appendLine("Defecator Section - Live Operations Report")
        appendLine("Generated,$timestamp")
        appendLine()
        appendLine("Section,Metric,Value,Unit")
        appendLine("General,Batch ID,${live.dashboard.batchId},")
        appendLine("General,Section Status,${live.dashboard.sectionStatus.name},")
        appendLine("General,Process Stability,${"%.1f".format(live.dashboard.processStability)},/100")
        appendLine("General,Telemetry Link,${live.connectionStatus},")
        appendLine("Process,DJ Tank Level,${"%.1f".format(p.djTankLevel)},%")
        appendLine("Process,DJ Active Pump Load,${"%.2f".format(p.djActivePumpA)},A")
        appendLine("Process,pH Level,${"%.2f".format(p.pH)},pH")
        appendLine("Process,SRT Buffer Tank Level,${"%.1f".format(p.srtBufferLevel)},mm")
        appendLine("Thermal,Heater 1 Outlet Temp,${"%.1f".format(p.heater1OutletC)},°C")
        appendLine("Thermal,Heater 2 Outlet Temp,${"%.1f".format(p.heater2OutletC)},°C")
        appendLine("Thermal,Heater 3 PV,${"%.1f".format(p.heater3PvC)},°C")
        appendLine("Thermal,Heater 3 SP,${"%.1f".format(p.heater3SpC)},°C")
        appendLine("Thermal,Heater 3 Steam Valve,${"%.1f".format(p.heater3SteamValvePct)},%")
        appendLine("Clear Juice,Flow Rate,${c.flow},L/h")
        appendLine("Clear Juice,Tank Level,${"%.1f".format(c.tankLevel)},%")
        appendLine("Clear Juice,Density,${"%.2f".format(c.density)},kg/m³")
        appendLine("Clear Juice,Filter Status,${if (c.filterOn) "ENGAGED" else "BYPASS"},")
    }
}

@Composable
fun DefecatorWorkspaceHeader(
    batchId: String,
    stability: String,
    status: DefecatorEquipStatus,
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    onPrintClick: () -> Unit,
    onLogEntryClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val isHealthy = status == DefecatorEquipStatus.RUNNING || status == DefecatorEquipStatus.HEALTHY
    val statusColor = if (isHealthy) DefecatorColors.StatusGreen else DefecatorColors.StatusRed
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = DefecatorColors.DeepNavy)
                }
                Text("Defecator Section", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DefecatorColors.DeepNavy)

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
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(DefecatorColors.Cyan.copy(alpha = 0.15f)).border(1.dp, DefecatorColors.Cyan.copy(alpha = 0.3f), RoundedCornerShape(50)).clickable { menuExpanded = true }.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Download, "Options", tint = DefecatorColors.DeepNavy, modifier = Modifier.size(16.dp))
                            Text("Export / Print", color = DefecatorColors.DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Download CSV File") }, onClick = { menuExpanded = false; onExportClick() }, leadingIcon = { Icon(Icons.Outlined.Description, null, tint = DefecatorColors.Cyan) })
                            DropdownMenuItem(text = { Text("Print Report") }, onClick = { menuExpanded = false; onPrintClick() }, leadingIcon = { Icon(Icons.Outlined.Print, null, tint = DefecatorColors.DeepNavy) })
                        }
                    }
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(DefecatorColors.DeepNavy).clickable { onLogEntryClick() }.padding(horizontal = 14.dp, vertical = 6.dp),
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
                    Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(DefecatorColors.Cyan.copy(alpha = 0.15f)).border(1.dp, DefecatorColors.Cyan.copy(alpha = 0.3f), RoundedCornerShape(50)).clickable { menuExpanded = true }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("Export", color = DefecatorColors.DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Download CSV") }, onClick = { menuExpanded = false; onExportClick() })
                        DropdownMenuItem(text = { Text("Print Report") }, onClick = { menuExpanded = false; onPrintClick() })
                    }
                }
                Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(DefecatorColors.DeepNavy).clickable { onLogEntryClick() }.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("Log Entry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.End) {
            if (isCompact) DefecatorHeaderMetaItem("System Loop", "Clarification")
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                DefecatorHeaderMetaItem("Process Stability", "$stability / 100")
                DefecatorHeaderMetaItem("Batch ID", batchId)
                if (!isCompact) DefecatorHeaderMetaItem("System Loop", "Clarification")
            }
        }
    }
}

@Composable
fun DefecatorHeaderMetaItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 11.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
    }
}

@Composable
fun DefecationProcessFlowSection(live: DefecatorLiveState, phFault: Boolean, heater3Fault: Boolean, isCompact: Boolean, modifier: Modifier = Modifier) {
    val p = live.process
    if (isCompact) {
        Row(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DefecatorProcessStepCard("1", "DJ Intake Tank", R.drawable.motor_image, "${"%.1f".format(p.djTankLevel)}%", "Pump Load", "${"%.2f".format(p.djActivePumpA)} A", Modifier.width(260.dp).height(180.dp))
            DefecatorProcessStepCard("2", "Neutralizer", R.drawable.motor_image, if (p.pH == 0f) "—" else "%.2f pH".format(p.pH), "Buffer Lvl", "%.1f mm".format(p.srtBufferLevel), Modifier.width(260.dp).height(180.dp), isBottleneck = phFault)
            DefecatorProcessStepCard("3", "Thermal Stage", R.drawable.motor_image, "%.1f °C".format(p.heater3PvC), "Valve", "%.1f%%".format(p.heater3SteamValvePct), Modifier.width(260.dp).height(180.dp), isBottleneck = heater3Fault, extraMetricLabel = "H2 Out", extraMetricValue = "%.1f°C".format(p.heater2OutletC))
        }
    } else {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DefecatorProcessStepCard("1", "DJ Intake Tank", R.drawable.motor_image, "${"%.1f".format(p.djTankLevel)}%", "Pump Load", "${"%.2f".format(p.djActivePumpA)} A", Modifier.weight(1f))
            DefecatorProcessStepCard("2", "Neutralizer", R.drawable.motor_image, if (p.pH == 0f) "—" else "%.2f pH".format(p.pH), "Buffer Lvl", "%.1f mm".format(p.srtBufferLevel), Modifier.weight(1f), isBottleneck = phFault)
            DefecatorProcessStepCard("3", "Thermal Stage", R.drawable.motor_image, "%.1f °C".format(p.heater3PvC), "Valve", "%.1f%%".format(p.heater3SteamValvePct), Modifier.weight(1f), isBottleneck = heater3Fault, extraMetricLabel = "H2 Out", extraMetricValue = "%.1f°C".format(p.heater2OutletC))
        }
    }
}

@Composable
fun DefecatorProcessStepCard(step: String, title: String, imageRes: Int, progress: String, metricLabel: String, metricValue: String, modifier: Modifier = Modifier, isBottleneck: Boolean = false, extraMetricLabel: String? = null, extraMetricValue: String? = null) {
    Box(modifier = modifier.defecatorGlassCard()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(painter = painterResource(id = imageRes), contentDescription = title, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.padding(2.dp).size(22.dp).background(Color.White.copy(alpha = 0.85f), CircleShape).border(1.dp, DefecatorColors.LightGray.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(step, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DefecatorColors.DeepNavy)
                }
                if (isBottleneck) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(DefecatorColors.StatusRed, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("ANOMALY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.wrapContentHeight().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DefecatorColors.DeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (isBottleneck) DefecatorColors.Orange else DefecatorColors.StatusGreen, CircleShape))
                    Text(if (isBottleneck) "Deviating" else "Running", fontSize = 11.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Progress", fontSize = 10.sp, color = DefecatorColors.SteelGray)
                        Text(progress, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isBottleneck) DefecatorColors.Orange else DefecatorColors.DarkBlueGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(metricLabel, fontSize = 10.sp, color = DefecatorColors.SteelGray)
                        Text(metricValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
                    }
                }

                Divider(color = DefecatorColors.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(extraMetricLabel ?: "-", fontSize = 10.sp, color = if (extraMetricLabel != null) DefecatorColors.SteelGray else Color.Transparent)
                    Text(extraMetricValue ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (extraMetricValue != null) DefecatorColors.StatusRed else Color.Transparent)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHARTS AND VISUALS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DefecatorVisualMetricsGridSection(live: DefecatorLiveState, isExpanded: Boolean, modifier: Modifier = Modifier) {
    val flowHistory = listOf(0.9f, 0.95f, 0.85f, 0.92f, 0.98f, 0.95f, 1.0f).map { it * live.clearJuice.flow }
    val currentPhRatio = ((live.process.pH - 6.0f) / 2.0f).coerceIn(0f, 1f)
    val heater1Loads = listOf(live.process.heater1OutletC * 0.9f, live.process.heater1OutletC, live.process.heater1OutletC * 1.05f)
    val heater2Loads = listOf(live.process.heater2OutletC * 0.85f, live.process.heater2OutletC, live.process.heater2OutletC * 1.02f)
    val heater3Loads = listOf(live.process.heater3PvC * 0.95f, live.process.heater3PvC, live.process.heater3PvC * 1.01f)
    val isConn = live.connectionStatus == "CONNECTED"

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isExpanded) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f), "Clear Juice Flow", "${live.clearJuice.flow.toInt()} L/h", Icons.Outlined.ShowChart, DefecatorColors.MutedBlue) { DefecatorChartSparklineTrend(data = flowHistory, color = DefecatorColors.MutedBlue) }
                DefecatorVisualCard(Modifier.weight(1f), "pH Level", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, DefecatorColors.Teal) { DefecatorChartTargetGauge(value = currentPhRatio, target = 0.5f, color = DefecatorColors.Teal) }
                DefecatorVisualCard(Modifier.weight(1f), "DJ Tank Level", "${live.process.djTankLevel.toInt()}%", Icons.Outlined.WaterDrop, DefecatorColors.Cyan) { DefecatorChartThresholdBar(value = live.process.djTankLevel / 100f, color = DefecatorColors.Cyan) }
                DefecatorVisualCard(Modifier.weight(1f), "Buffer Tank", "${live.process.srtBufferLevel.toInt()} mm", Icons.Outlined.ViewInAr, DefecatorColors.Orange) { DefecatorChartThresholdBar(value = (live.process.srtBufferLevel / 100f).coerceIn(0f, 1f), color = DefecatorColors.Orange) }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f), "Heater 1 Out", "${"%.1f".format(live.process.heater1OutletC)} °C", Icons.Outlined.Power, DefecatorColors.DeepNavy) { DefecatorChartEquipmentBars(data = heater1Loads, color = DefecatorColors.DeepNavy) }
                DefecatorVisualCard(Modifier.weight(1f), "Heater 2 Out", "${"%.1f".format(live.process.heater2OutletC)} °C", Icons.Outlined.Power, DefecatorColors.DarkBlueGray) { DefecatorChartEquipmentBars(data = heater2Loads, color = DefecatorColors.DarkBlueGray) }
                DefecatorVisualCard(Modifier.weight(1f), "Heater 3 Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, DefecatorColors.SoftOrange) { DefecatorChartEquipmentBars(data = heater3Loads, color = DefecatorColors.SoftOrange) }
                DefecatorVisualCard(Modifier.weight(1f), "Telemetry", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) DefecatorColors.Teal else DefecatorColors.StatusRed) { DefecatorChartPulseIndicator(isConnected = isConn) }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Clear Juice Flow", "${live.clearJuice.flow.toInt()} L/h", Icons.Outlined.ShowChart, DefecatorColors.MutedBlue) { DefecatorChartSparklineTrend(data = flowHistory, color = DefecatorColors.MutedBlue) }
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "pH Level", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, DefecatorColors.Teal) { DefecatorChartTargetGauge(value = currentPhRatio, target = 0.5f, color = DefecatorColors.Teal) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "DJ Tank Level", "${live.process.djTankLevel.toInt()}%", Icons.Outlined.WaterDrop, DefecatorColors.Cyan) { DefecatorChartThresholdBar(value = live.process.djTankLevel / 100f, color = DefecatorColors.Cyan) }
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Buffer Tank", "${live.process.srtBufferLevel.toInt()} mm", Icons.Outlined.ViewInAr, DefecatorColors.Orange) { DefecatorChartThresholdBar(value = (live.process.srtBufferLevel / 100f).coerceIn(0f, 1f), color = DefecatorColors.Orange) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Heater 1 Out", "${"%.1f".format(live.process.heater1OutletC)} °C", Icons.Outlined.Power, DefecatorColors.DeepNavy) { DefecatorChartEquipmentBars(data = heater1Loads, color = DefecatorColors.DeepNavy) }
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Heater 2 Out", "${"%.1f".format(live.process.heater2OutletC)} °C", Icons.Outlined.Power, DefecatorColors.DarkBlueGray) { DefecatorChartEquipmentBars(data = heater2Loads, color = DefecatorColors.DarkBlueGray) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Heater 3 Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, DefecatorColors.SoftOrange) { DefecatorChartEquipmentBars(data = heater3Loads, color = DefecatorColors.SoftOrange) }
                DefecatorVisualCard(Modifier.weight(1f).height(130.dp), "Telemetry", if (isConn) "Online" else "Offline", Icons.Outlined.NetworkCheck, if (isConn) DefecatorColors.Teal else DefecatorColors.StatusRed) { DefecatorChartPulseIndicator(isConnected = isConn) }
            }
        }
    }
}

@Composable
fun DefecatorVisualCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, chart: @Composable () -> Unit) {
    Box(modifier = modifier.defecatorGlassCard().padding(0.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 11.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.SemiBold)
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = DefecatorColors.DeepNavy)
            Box(modifier = Modifier.fillMaxWidth().height(24.dp)) { chart() }
        }
    }
}

@Composable
fun DefecatorChartSparklineTrend(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val step = size.width / (data.size - 1).coerceAtLeast(1)
        val max = data.maxOrNull() ?: 1f; val min = data.minOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(0.01f)
        data.forEachIndexed { i, v ->
            val x = i * step; val y = size.height - ((v - min) / range * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun DefecatorChartTargetGauge(value: Float, target: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(DefecatorColors.LightGray, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = 8f, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, size.height/2), Offset(size.width * value, size.height/2), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(DefecatorColors.DeepNavy, radius = 6f, center = Offset(size.width * target, size.height/2))
    }
}

@Composable
fun DefecatorChartThresholdBar(value: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(DefecatorColors.LightGray, size = size)
        drawRect(color, size = Size(size.width * value, size.height))
    }
}

@Composable
fun DefecatorChartEquipmentBars(data: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val max = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barWidth = size.width / (data.size * 2)
        data.forEachIndexed { i, v ->
            val pct = v / max
            drawRect(color, topLeft = Offset(i * barWidth * 2f, size.height * (1 - pct)), size = Size(barWidth, size.height * pct))
        }
    }
}

@Composable
fun DefecatorChartPulseIndicator(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "")
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(if (isConnected) DefecatorColors.StatusGreen.copy(alpha = pulse) else DefecatorColors.StatusRed, radius = 12f, center = Offset(size.width/2, size.height/2))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RIGHT RAIL COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroCJTankLevelVisual(live: DefecatorLiveState, modifier: Modifier = Modifier) {
    val levelPct = live.clearJuice.tankLevel.coerceIn(0.0F, 100.0F)

    Box(modifier = modifier.fillMaxWidth().defecatorGlassCard()) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WaterDrop, null, tint = DefecatorColors.Cyan, modifier = Modifier.size(24.dp))
                Text("Clear Juice Storage", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).height(120.dp)
                        .background(DefecatorColors.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val color = if (levelPct > 85f) DefecatorColors.Orange else DefecatorColors.Cyan
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(levelPct / 100f).background(color, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)))
                    Text("${levelPct.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (levelPct > 40f) Color.White else DefecatorColors.DeepNavy, modifier = Modifier.padding(bottom = 6.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Clear Flow Rate", fontSize = 12.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%,.0f".format(live.clearJuice.flow)} L/h", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DefecatorColors.DeepNavy)
                    }
                    Divider(color = DefecatorColors.LightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Density", fontSize = 12.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text("${"%.2f".format(live.clearJuice.density)} kg/m³", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
                    }
                    Divider(color = DefecatorColors.LightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Filter", fontSize = 12.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                        Text(if (live.clearJuice.filterOn) "ENGAGED" else "BYPASS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(live.clearJuice.filterOn) DefecatorColors.StatusGreen else DefecatorColors.SteelGray)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowAnalysisCard(state: DefecatorDashboardState, modifier: Modifier = Modifier) {
    val a = state.chart.actual; val t = state.chart.target; val gap = a - t; val gapPct = if (t > 0f) gap / t * 100f else 0f
    Box(modifier = modifier.defecatorGlassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, null, tint = DefecatorColors.DeepNavy, modifier = Modifier.size(20.dp))
                Text("Discharge Flow Analytics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
            }
            Divider(color = DefecatorColors.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Actual Clear Flow", fontSize = 13.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(a), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target Profile", fontSize = 13.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(t), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Design Spec", fontSize = 13.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Medium)
                Text("%,.0f L/h".format(state.chart.design), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.DeepNavy)
            }

            Box(modifier = Modifier.fillMaxWidth().background(DefecatorColors.LightGray.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Operational Delta", fontSize = 12.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.Bold)
                    Text("%+.0f L/h (%+.1f%%)".format(gap, gapPct), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (gap >= 0) DefecatorColors.StatusGreen else DefecatorColors.Orange)
                }
            }
        }
    }
}

@Composable
fun AlarmsCriticalPanel(alerts: List<String>, phFault: Boolean, djPumpFault: Boolean, heater3Fault: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.defecatorGlassCard()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = DefecatorColors.StatusRed, modifier = Modifier.size(20.dp))
                Text("Live Incident Stack", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DefecatorColors.StatusRed)
            }

            if (!phFault && !djPumpFault && !heater3Fault && alerts.isEmpty()) {
                Text("All systems nominal. No process limits breached.", fontSize = 13.sp, color = DefecatorColors.SteelGray)
            } else {
                if (phFault) TriggeredAlertRow("Critical: pH Out of Range")
                if (djPumpFault) TriggeredAlertRow("Hardware: DJ Intake Pump Loss")
                if (heater3Fault) TriggeredAlertRow("Thermal: Heater 3 Delta Limit")
                alerts.forEach { error -> TriggeredAlertRow(text = error) }
            }
        }
    }
}

@Composable
fun TriggeredAlertRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(DefecatorColors.StatusRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DefecatorColors.DeepNavy)
    }
}

@Composable
fun DefecatorBottomSummaryRow(live: DefecatorLiveState, isExpanded: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isExpanded) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorSummaryBlock(Modifier.weight(1f), "Process Stability", "${"%.1f".format(live.dashboard.processStability)}", Icons.Outlined.Speed, DefecatorColors.Teal)
                DefecatorSummaryBlock(Modifier.weight(1f), "Average pH", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, DefecatorColors.DeepNavy)
                DefecatorSummaryBlock(Modifier.weight(1f), "Clear Juice Flow", "${"%,.0f".format(live.clearJuice.flow)} L/h", Icons.Outlined.WaterDrop, DefecatorColors.Cyan)
                DefecatorSummaryBlock(Modifier.weight(1f), "Thermal Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, DefecatorColors.SoftOrange)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorSummaryBlock(Modifier.weight(1f).height(80.dp), "Process Stability", "${"%.1f".format(live.dashboard.processStability)}", Icons.Outlined.Speed, DefecatorColors.Teal)
                DefecatorSummaryBlock(Modifier.weight(1f).height(80.dp), "Average pH", "%.2f".format(live.process.pH), Icons.Outlined.DataExploration, DefecatorColors.DeepNavy)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DefecatorSummaryBlock(Modifier.weight(1f).height(80.dp), "Clear Juice Flow", "${"%,.0f".format(live.clearJuice.flow)} L/h", Icons.Outlined.WaterDrop, DefecatorColors.Cyan)
                DefecatorSummaryBlock(Modifier.weight(1f).height(80.dp), "Thermal Out", "${"%.1f".format(live.process.heater3PvC)} °C", Icons.Outlined.Bolt, DefecatorColors.SoftOrange)
            }
        }
    }
}

@Composable
fun DefecatorSummaryBlock(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Box(modifier = modifier.defecatorGlassCard().padding(0.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontSize = 11.sp, color = DefecatorColors.SteelGray, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DefecatorColors.DeepNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}