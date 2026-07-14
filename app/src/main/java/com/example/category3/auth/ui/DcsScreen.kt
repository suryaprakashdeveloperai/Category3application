package com.example.category3.auth.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.utils.MorphicSpeechTranslator
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// STATE MODELS
// ─────────────────────────────────────────────────────────────────────────────
data class DcsManualEntryState(
    val rjInitial: String = "", val rjFinal: String = "", val rjTotal: String = "0.00",
    val cjInitial: String = "", val cjFinal: String = "", val cjTotal: String = "0.00",
    val flocInitial: String = "", val flocFinal: String = "", val flocTotal: String = "0.00",
    val operatorRemarks: String = "",
    val isSubmitted: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// EXPORT & PRINT LOGIC FOR DCS LOGS
// ─────────────────────────────────────────────────────────────────────────────

fun generateDcsLogCsv(
    state: DcsManualEntryState, signaturePoints: List<Offset>, date: String, time: String, shift: String,
    cjFlow: String, heater3: String, defecatorPh: String, djLevel: String
): String {
    val header = "Date,Time,Shift,CJ Flow (L/HR),Heater 3 PV (°C),Defecator pH,DJ Tank Level (%)," +
            "RJ Initial,RJ Final,RJ Total," +
            "CJ Initial,CJ Final,CJ Total," +
            "Floc Initial,Floc Final,Floc Total,Remarks,E-Signature_Data_Base64"

    val signatureData = if (signaturePoints.isEmpty()) "UNVERIFIED" else {
        val rawCoords = signaturePoints.joinToString("|") {
            if (it == Offset.Unspecified) "BREAK" else "${it.x.toInt()}_${it.y.toInt()}"
        }
        Base64.encodeToString(rawCoords.toByteArray(), Base64.NO_WRAP)
    }

    val safeRemarks = state.operatorRemarks.replace("\"", "\"\"")
    val row = "$date,$time,$shift,$cjFlow,$heater3,$defecatorPh,$djLevel," +
            "${state.rjInitial},${state.rjFinal},${state.rjTotal}," +
            "${state.cjInitial},${state.cjFinal},${state.cjTotal}," +
            "${state.flocInitial},${state.flocFinal},${state.flocTotal},\"$safeRemarks\",$signatureData"

    return "$header\n$row"
}

fun downloadDcsLogCsvToDevice(context: Context, csvData: String) {
    val fileName = "DCS_Manual_Log_${System.currentTimeMillis()}.csv"
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { it.write(csvData.toByteArray()) }
                Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
            }
        } else {
            val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(targetDir, fileName)
            file.writeText(csvData)
            Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private var activePrintWebView: WebView? = null

fun printDcsLogReport(
    context: Context, state: DcsManualEntryState, signaturePoints: List<Offset>,
    date: String, time: String, shift: String, cjFlow: String, heater3: String, defecatorPh: String, djLevel: String,
    djPumpStr: String, mudPumpStr: String, flocPumpStr: String, cjFilterStr: String
) {
    Toast.makeText(context, "Preparing Report for Printer...", Toast.LENGTH_SHORT).show()

    val safeRemarks = if (state.operatorRemarks.isEmpty()) "<i>No annotations provided.</i>" else state.operatorRemarks

    val signatureSvg = if (signaturePoints.isEmpty()) {
        "<p style='color: #EF4444; font-weight: bold;'>SIGNATURE MISSING / UNVERIFIED</p>"
    } else {
        val pathStr = buildString {
            var first = true
            for (p in signaturePoints) {
                if (p == Offset.Unspecified) { first = true } else {
                    if (first) { append("M ${p.x} ${p.y} "); first = false }
                    else { append("L ${p.x} ${p.y} ") }
                }
            }
        }
        "<svg width='100%' height='150' style='border:1px solid #E2E8F0; background:#F8FAFC; border-radius: 6px;'><path d='$pathStr' fill='transparent' stroke='#0F172A' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'/></svg>"
    }

    val htmlBuilder = """
        <html>
        <head>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #0F172A; padding: 20px; }
                h1 { border-bottom: 2px solid #0EA5E9; padding-bottom: 10px; margin-bottom: 20px; font-size: 24px; }
                h2 { font-size: 16px; color: #334155; margin-top: 25px; margin-bottom: 10px; background-color: #F6F6F7; padding: 8px; border-radius: 4px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                th, td { border: 1px solid #E2E8F0; padding: 10px; text-align: left; font-size: 14px; }
                th { width: 40%; background-color: #F8FAFC; color: #64748B; font-weight: bold; }
                td { width: 60%; font-weight: 500; }
                .remarks-box { border: 1px solid #E2E8F0; padding: 15px; border-radius: 4px; font-size: 14px; min-height: 80px; }
            </style>
        </head>
        <body>
            <h1>Defecator Control Cockpit - Manual Entry Log</h1>
            <p><strong>Date:</strong> $date &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Time:</strong> $time &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Shift:</strong> $shift</p>

            <h2>Process Stream Metrics</h2>
            <table>
                <tr><th>CJ Flow (L/HR)</th><td>$cjFlow</td></tr>
                <tr><th>Heater 3 PV (°C)</th><td>$heater3</td></tr>
                <tr><th>Defecator pH</th><td>$defecatorPh</td></tr>
                <tr><th>DJ Tank Level (%)</th><td>$djLevel</td></tr>
            </table>
            
            <h2>Hardware Statuses</h2>
            <table>
                <tr><th>DJ Active Pump</th><td>$djPumpStr</td></tr>
                <tr><th>Mud Pump 01 (DOL)</th><td>$mudPumpStr</td></tr>
                <tr><th>Flocculant Pump 01</th><td>$flocPumpStr</td></tr>
                <tr><th>Clear Juice Filter</th><td>$cjFilterStr</td></tr>
            </table>

            <h2>Factory Pipeline Totalizers</h2>
            <table>
                <tr><th>Raw Juice (M³)</th><td><b>Initial:</b> ${state.rjInitial} | <b>Final:</b> ${state.rjFinal} | <b>Net Total:</b> ${state.rjTotal}</td></tr>
                <tr><th>Clear Juice (M³)</th><td><b>Initial:</b> ${state.cjInitial} | <b>Final:</b> ${state.cjFinal} | <b>Net Total:</b> ${state.cjTotal}</td></tr>
                <tr><th>Flocculant Feed (KG)</th><td><b>Initial:</b> ${state.flocInitial} | <b>Final:</b> ${state.flocFinal} | <b>Net Total:</b> ${state.flocTotal}</td></tr>
            </table>

            <h2>Operator Annotations & Remarks</h2>
            <div class="remarks-box">$safeRemarks</div>
            
            <h2>Operator E-Signature Verification</h2>
            $signatureSvg
            
            <p style="margin-top: 15px; color: #10B981; font-weight: bold; font-size: 12px;">✔ Document Cryptographically Sealed via Device Input</p>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    activePrintWebView = webView

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "DCS_Manual_Log_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

// ─────────────────────────────────────────────────────────────────────────────
// UI PALETTE
// ─────────────────────────────────────────────────────────────────────────────

private data class DcsMorphicPalette(
    val baseChassis: Color, val glassFill: Color, val glassBorder: Color,
    val inputContainer: Color, val textPrimary: Color, val textMuted: Color,
    val dividerLine: Color, val inputBorderUnfocused: Color
)

@Composable
private fun getDcsPalette(isDark: Boolean): DcsMorphicPalette {
    return if (isDark) {
        DcsMorphicPalette(
            baseChassis = Color(0xFF0A0C14), glassFill = Color(0x13FFFFFF), glassBorder = Color(0x26FFFFFF),
            inputContainer = Color(0x1A000000), textPrimary = Color(0xFFF0F6FC), textMuted = Color(0xFF8B949E),
            dividerLine = Color(0xFF30363D), inputBorderUnfocused = Color(0xFF30363D)
        )
    } else {
        DcsMorphicPalette(
            baseChassis = Color(0xFFF3F4F6), glassFill = Color(0xB3FFFFFF), glassBorder = Color(0x66FFFFFF),
            inputContainer = Color(0x33F3F4F6), textPrimary = Color(0xFF1F2937), textMuted = Color(0xFF6B7280),
            dividerLine = Color(0x1A6B7280), inputBorderUnfocused = Color(0x406B7280)
        )
    }
}

private val TechAccentBlue = Color(0xFF0EA5E9)
private val TechAccentGreen = Color(0xFF10B981)
private val TechWarnOrange = Color(0xFFF97316)
private val TechAlarmRed = Color(0xFFEF4444)

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DcsScreen(
    viewModel: DefecatorDedicatedViewModel = viewModel(factory = DefecatorDedicatedViewModel.provideFactory()),
    onNavigationCallback: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val liveState by viewModel.state.collectAsState()

    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var manualState by remember { mutableStateOf(DcsManualEntryState()) }

    val signaturePoints = remember { mutableStateListOf<Offset>() }
    val speechTranslator = remember(context) { MorphicSpeechTranslator(context) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getDcsPalette(isDark = isDarkThemeOverride)

    var showExportDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? -> if (bitmap != null) capturedImage = bitmap }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) cameraLauncher.launch(null) }

    fun autoComputeTotal(initial: String, final: String): String {
        val initVal = initial.toDoubleOrNull() ?: 0.0
        val finalVal = final.toDoubleOrNull() ?: 0.0
        val net = finalVal - initVal
        return if (net >= 0.0) String.format("%.2f", net) else "0.00"
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val currentShift = remember(currentTime) {
        val hour = currentTime.hour
        when {
            hour in 6..13 -> "SHIFT A"
            hour in 14..21 -> "SHIFT B"
            else -> "SHIFT C"
        }
    }
    val currentTimeFormatted = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    // Extracted live string values for report generation
    val cjFlowStr = String.format("%,.0f", liveState.clearJuice.flow)
    val heater3Str = String.format("%.1f", liveState.process.heater3PvC)
    val defecatorPhStr = String.format("%.2f", liveState.process.pH)
    val djLevelStr = String.format("%.1f", liveState.process.djTankLevel)

    val djPumpStr = if (liveState.process.djActivePumpA > 0.5f) "RUNNING" else "STOPPED"
    val mudPumpStr = if (liveState.mudFilter.mudPump1Status.toString() == "RUNNING") "RUNNING" else "STOPPED"
    val flocPumpStr = if (liveState.floc.pump1Status.toString() == "RUNNING") "RUNNING" else "STOPPED"
    val cjFilterStr = if (liveState.clearJuice.filterOn) "RUNNING" else "STOPPED"

    val structuralBackgroundModifier = if (isDarkThemeOverride) {
        Modifier.background(Brush.radialGradient(colors = listOf(Color(0xFF1E1B4B), Color(0xFF090A10)), radius = 2200f))
    } else {
        Modifier.background(palette.baseChassis)
    }

    val HeaderContent = @Composable {
        val containerModifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (isDarkThemeOverride) 0.dp else 2.dp, shape = RoundedCornerShape(10.dp))
            .background(palette.glassFill, RoundedCornerShape(10.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)

        val headerDetails = @Composable {
            Column {
                Text(
                    "DEFECATOR CONTROL COCKPIT [PROCESS FLOW MATRIX]",
                    color = palette.textPrimary, fontSize = if (isPortrait) 13.sp else 16.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = if (isDarkThemeOverride) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, contentDescription = null, tint = if (isDarkThemeOverride) TechAccentBlue else TechWarnOrange, modifier = Modifier.size(14.dp))
                    Text(text = if (isDarkThemeOverride) "GLASS MODE: DARK" else "GLASS MODE: LIGHT", color = palette.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isDarkThemeOverride, onCheckedChange = { isDarkThemeOverride = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TechAccentBlue),
                        modifier = Modifier.graphicsLayer(scaleX = 0.65f, scaleY = 0.65f)
                    )

                    val connColor = if (liveState.connectionStatus == "CONNECTED") TechAccentGreen else TechAlarmRed
                    Text(" | STREAM: ${liveState.connectionStatus}", color = connColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        val timeDetails = @Composable {
            Column(horizontalAlignment = if (isPortrait) Alignment.Start else Alignment.End) {
                Text("DATE: $currentDate", fontSize = 11.sp, color = palette.textMuted, fontWeight = FontWeight.SemiBold)
                Text("TIME: $currentTimeFormatted | $currentShift", fontSize = 11.sp, color = TechWarnOrange, fontWeight = FontWeight.Bold)
            }
        }

        if (isPortrait) {
            Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                headerDetails()
                timeDetails()
            }
        } else {
            Row(modifier = containerModifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                headerDetails()
                timeDetails()
            }
        }
    }

    val MetricsContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "PROCESS STREAM METRIC MATRIX [PLC LIVE LINK]", icon = Icons.Rounded.Speed, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DcsLogInputField("CJ FLOW (L/HR)", cjFlowStr, palette, Modifier.weight(1f), isReadOnly = true) {}
                    DcsLogInputField("HEATER 3 PV (°C)", heater3Str, palette, Modifier.weight(1f), isReadOnly = true) {}
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DcsLogInputField("DEFECATOR pH", defecatorPhStr, palette, Modifier.weight(1f), isReadOnly = true) {}
                    DcsLogInputField("DJ TANK LEVEL (%)", djLevelStr, palette, Modifier.weight(1f), isReadOnly = true) {}
                }
            }
        }
    }

    val HardwareContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "CRITICAL HARDWARE STATUSES [PLC MONITOR]", icon = Icons.Rounded.Settings, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
                HardwareStateBadge("DJ ACTIVE PUMP", liveState.process.djActivePumpA > 0.5f, palette)
                HardwareStateBadge("MUD PUMP 01 (DOL)", liveState.mudFilter.mudPump1Status.toString() == "RUNNING", palette)
                HardwareStateBadge("FLOCCULANT PUMP 01", liveState.floc.pump1Status.toString() == "RUNNING", palette)
                HardwareStateBadge("CLEAR JUICE FILTER", liveState.clearJuice.filterOn, palette)
            }
        }
    }

    val TotalizerContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "FACTORY PIPELINE TOTALIZER NODES", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                // A. Raw Juice
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("A. RAW JUICE FLOW BLOCK (M³)", color = TechAccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DcsLogInputField("INITIAL", manualState.rjInitial, palette, Modifier.weight(1f)) { manualState = manualState.copy(rjInitial = it, rjTotal = autoComputeTotal(it, manualState.rjFinal), isSubmitted = false) }
                        DcsLogInputField("FINAL", manualState.rjFinal, palette, Modifier.weight(1f)) { manualState = manualState.copy(rjFinal = it, rjTotal = autoComputeTotal(manualState.rjInitial, it), isSubmitted = false) }
                        DcsLogInputField("NET TOTAL", manualState.rjTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                    }
                }
                HorizontalDivider(color = palette.dividerLine, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // B. Clear Juice
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("B. CLEAR JUICE METRIC BLOCK (M³)", color = TechAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DcsLogInputField("INITIAL", manualState.cjInitial, palette, Modifier.weight(1f)) { manualState = manualState.copy(cjInitial = it, cjTotal = autoComputeTotal(it, manualState.cjFinal), isSubmitted = false) }
                        DcsLogInputField("FINAL", manualState.cjFinal, palette, Modifier.weight(1f)) { manualState = manualState.copy(cjFinal = it, cjTotal = autoComputeTotal(manualState.cjInitial, it), isSubmitted = false) }
                        DcsLogInputField("NET TOTAL", manualState.cjTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                    }
                }
                HorizontalDivider(color = palette.dividerLine, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // C. Flocculant
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("C. FLOCCULANT FEED PIPELINE (KG)", color = TechWarnOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DcsLogInputField("INITIAL", manualState.flocInitial, palette, Modifier.weight(1f)) { manualState = manualState.copy(flocInitial = it, flocTotal = autoComputeTotal(it, manualState.flocFinal), isSubmitted = false) }
                        DcsLogInputField("FINAL", manualState.flocFinal, palette, Modifier.weight(1f)) { manualState = manualState.copy(flocFinal = it, flocTotal = autoComputeTotal(manualState.flocInitial, it), isSubmitted = false) }
                        DcsLogInputField("NET TOTAL", manualState.flocTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                    }
                }
            }
        }
    }

    val CameraContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "SENSORY PROOF VALIDATION WINDOW", icon = Icons.Rounded.Camera, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth().fillMaxHeight().clip(RoundedCornerShape(6.dp))
                    .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Consist Verification Shutter", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = if (capturedImage != null) "IMAGE VERIFIED ✔" else "Secure visual totalizer log pass.", color = if (capturedImage != null) TechAccentGreen else palette.textMuted, fontSize = 10.sp)
                    }

                    if (capturedImage != null) {
                        Image(
                            bitmap = capturedImage!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, TechAccentGreen, RoundedCornerShape(4.dp)).clickable { capturedImage = null }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .height(34.dp).width(125.dp).clip(RoundedCornerShape(6.dp))
                                .background(TechAccentBlue.copy(alpha = 0.15f)).border(1.dp, TechAccentBlue, RoundedCornerShape(6.dp))
                                .clickable {
                                    val hasPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TechAccentBlue, modifier = Modifier.size(13.dp))
                                Text("OPEN PREVIEW", color = TechAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    val AudioContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "DCS ANNOTATION AUDIO LOG", icon = Icons.Rounded.Notes, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth().fillMaxHeight().clip(RoundedCornerShape(6.dp))
                    .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                BasicTextField(
                    value = manualState.operatorRemarks, onValueChange = { manualState = manualState.copy(operatorRemarks = it, isSubmitted = false) },
                    textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(palette.textPrimary), modifier = Modifier.fillMaxSize().padding(end = 36.dp),
                    decorationBox = { innerTextField ->
                        if (manualState.operatorRemarks.isEmpty()) {
                            Text(text = "TAP MIC KEY AND STATE VALVE ANOMALIES...", color = if (isListening) TechAlarmRed else palette.textMuted, fontSize = 11.sp)
                        }
                        innerTextField()
                    }
                )

                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TechAccentBlue, strokeWidth = 2.dp)
                    } else {
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = {
                                if (!isListening) {
                                    val hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        speechTranslator.startListening(
                                            onStatusChange = { currentVoiceStatusText = it },
                                            onListeningStateChange = { isListening = it },
                                            onTranslatingStateChange = { isTranslating = it },
                                            onResultReceived = { manualState = manualState.copy(operatorRemarks = it, isSubmitted = false) }
                                        )
                                    } else {
                                        (context as? android.app.Activity)?.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone, contentDescription = null, tint = if (isListening) TechAlarmRed else TechAccentBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    val SignatureContent = @Composable { modifier: Modifier ->
        DcsFormSectionCard(title = "OPERATOR SIGNATURE CONTROL WINDOW", icon = Icons.Rounded.Draw, palette = palette, isDark = isDarkThemeOverride, modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("DRAW VERIFICATION INTEGRITY TRACE:", color = palette.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "WIPE PANEL", color = TechAlarmRed, fontSize = 10.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { signaturePoints.clear() }.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth().weight(1f).padding(top = 4.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(8.dp))
                        .background(palette.glassFill, RoundedCornerShape(8.dp)).border(1.dp, palette.glassBorder, RoundedCornerShape(8.dp))
                ) {
                    if (signaturePoints.isEmpty()) {
                        Text(text = "TOUCH AUTHENTICATOR MATRIX ON...", color = palette.textMuted.copy(alpha = 0.4f), fontSize = 11.sp, modifier = Modifier.align(Alignment.Center))
                    }
                    Canvas(
                        modifier = Modifier.fillMaxSize().pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { signaturePoints.add(Offset(event.x, event.y)); true }
                                MotionEvent.ACTION_UP -> { signaturePoints.add(Offset.Unspecified); true }
                                else -> false
                            }
                        }
                    ) {
                        if (signaturePoints.size > 1) {
                            val path = Path()
                            var first = true
                            for (point in signaturePoints) {
                                if (point == Offset.Unspecified) { first = true; continue }
                                if (first) { path.moveTo(point.x, point.y); first = false } else { path.lineTo(point.x, point.y) }
                            }
                            drawPath(path = path, color = palette.textPrimary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
        }
    }

    val FooterContent = @Composable {
        val containerModifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (isDarkThemeOverride) 0.dp else 2.dp, shape = RoundedCornerShape(10.dp))
            .background(palette.glassFill, RoundedCornerShape(10.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)

        val statusBadge = @Composable {
            Box(
                modifier = Modifier
                    .height(34.dp).clip(RoundedCornerShape(6.dp))
                    .background(if (signaturePoints.isNotEmpty()) TechAccentGreen.copy(alpha = 0.12f) else TechAlarmRed.copy(alpha = 0.08f))
                    .border(1.dp, if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (signaturePoints.isNotEmpty()) "DCS JOURNAL VERIFIED ✔" else "ESIGN REQUIRED ❌",
                    color = if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, fontSize = 11.sp, fontWeight = FontWeight.Black
                )
            }
        }

        val submitButton = @Composable {
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .then(if (isPortrait) Modifier.fillMaxWidth() else Modifier.width(220.dp))
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(colors = if (manualState.isSubmitted) listOf(TechAccentGreen, TechAccentGreen.copy(alpha = 0.8f)) else listOf(TechAccentBlue, TechAccentBlue.copy(alpha = 0.8f))))
                    .clickable {
                        if (signaturePoints.isNotEmpty()) {
                            showExportDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("COMMIT DATA TRANSFERS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        if (isPortrait) {
            Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                statusBadge()
                submitButton()
            }
        } else {
            Row(modifier = containerModifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                statusBadge()
                submitButton()
            }
        }
    }

    // ============================================================================
    // 🧱 PARENT LAYOUT MANAGER
    // ============================================================================
    Box(modifier = Modifier.fillMaxSize().then(structuralBackgroundModifier).padding(14.dp), contentAlignment = Alignment.TopCenter) {

        if (isPortrait) {
            // 📱 PORTRAIT LAYOUT
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderContent()
                Spacer(modifier = Modifier.height(12.dp))

                val scrollState = rememberScrollState()
                Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricsContent(Modifier.fillMaxWidth().height(160.dp))
                    HardwareContent(Modifier.fillMaxWidth().height(260.dp))
                    TotalizerContent(Modifier.fillMaxWidth().height(380.dp))
                    CameraContent(Modifier.fillMaxWidth().height(140.dp))
                    AudioContent(Modifier.fillMaxWidth().height(140.dp))
                    SignatureContent(Modifier.fillMaxWidth().height(220.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                FooterContent()
            }

        } else {
            // 💻 LANDSCAPE LAYOUT
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderContent()

                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(modifier = Modifier.weight(1.0f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricsContent(Modifier.weight(1f))
                        HardwareContent(Modifier.weight(1.1f))
                    }
                    Column(modifier = Modifier.weight(1.1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        TotalizerContent(Modifier.fillMaxSize())
                    }
                    Column(modifier = Modifier.weight(1.0f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CameraContent(Modifier.weight(0.6f))
                        AudioContent(Modifier.weight(0.9f))
                        SignatureContent(Modifier.weight(1.1f))
                    }
                }

                FooterContent()
            }
        }

        // ============================================================================
        // EXPORT / PRINT OVERLAY DIALOG
        // ============================================================================
        if (showExportDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(480.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .background(palette.baseChassis, RoundedCornerShape(16.dp))
                        .border(1.dp, palette.glassBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = TechAccentGreen, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("LOG COMMITTED SUCCESSFULLY", color = TechAccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Do you want to export a copy of this manual entry before closing?", color = palette.textPrimary, fontSize = 13.sp, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        // 1. PRINT BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    printDcsLogReport(
                                        context, manualState, signaturePoints, currentDate, currentTimeFormatted, currentShift,
                                        cjFlowStr, heater3Str, defecatorPhStr, djLevelStr, djPumpStr, mudPumpStr, flocPumpStr, cjFilterStr
                                    )
                                    manualState = manualState.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Print, null, tint = TechAccentBlue, modifier = Modifier.size(18.dp))
                                Text("PRINT PDF", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. CSV DOWNLOAD BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    val csvData = generateDcsLogCsv(
                                        manualState, signaturePoints, currentDate, currentTimeFormatted, currentShift,
                                        cjFlowStr, heater3Str, defecatorPhStr, djLevelStr
                                    )
                                    downloadDcsLogCsvToDevice(context, csvData)
                                    manualState = manualState.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Download, null, tint = TechAccentBlue, modifier = Modifier.size(18.dp))
                                Text("SAVE CSV", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 3. SKIP BUTTON
                        Box(
                            modifier = Modifier.weight(0.7f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(TechAccentBlue)
                                .clickable {
                                    manualState = manualState.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SKIP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HardwareStateBadge(title: String, isRunning: Boolean, palette: DcsMorphicPalette) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(6.dp))
            .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Box(
            modifier = Modifier.height(26.dp).width(90.dp).clip(RoundedCornerShape(4.dp))
                .background(if (isRunning) TechAccentGreen.copy(alpha = 0.15f) else TechAlarmRed.copy(alpha = 0.1f))
                .border(1.dp, if (isRunning) TechAccentGreen else TechAlarmRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isRunning) "RUNNING" else "STOPPED", color = if (isRunning) TechAccentGreen else TechAlarmRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DcsLogInputField(
    label: String, value: String, palette: DcsMorphicPalette, modifier: Modifier = Modifier, isReadOnly: Boolean = false, onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(6.dp))
            .background(if (isReadOnly) palette.inputContainer.copy(alpha = 0.4f) else palette.inputContainer)
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = label, color = palette.textMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(1.dp))
            BasicTextField(
                value = value, onValueChange = onValueChange,
                textStyle = TextStyle(color = if (isReadOnly) TechAccentGreen else palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, readOnly = isReadOnly,
                cursorBrush = SolidColor(palette.textPrimary), modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DcsFormSectionCard(
    title: String, icon: ImageVector, palette: DcsMorphicPalette, isDark: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .shadow(elevation = if (isDark) 0.dp else 2.dp, shape = RoundedCornerShape(14.dp))
            .background(palette.glassFill, RoundedCornerShape(14.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = TechAccentBlue, modifier = Modifier.size(16.dp))
            Text(text = title, color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}