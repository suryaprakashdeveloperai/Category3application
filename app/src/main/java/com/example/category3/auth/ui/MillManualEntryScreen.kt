package com.example.category3.auth.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.SettingsInputComponent
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.data.MillTelemetryState
import com.example.category3.utils.MorphicSpeechTranslator
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// EXPORT & PRINT LOGIC FOR MANUAL LOGS
// ─────────────────────────────────────────────────────────────────────────────

fun generateManualLogCsv(state: MillTelemetryState, signaturePoints: List<Offset>, date: String, time: String, shift: String): String {
    val header = "Date,Time,Shift,Juice Flow (M3/HR),Cane Carrier (RPM),Rake 01 (RPM),Rake 02 (RPM)," +
            "Mill 01 RPM,Mill 01 Amps,Mill 01 Press," +
            "Mill 02 RPM,Mill 02 Amps,Mill 02 Press," +
            "Mill 03 RPM,Mill 03 Amps,Mill 03 Press,Remarks,E-Signature_Data_Base64"

    // Convert Signature Canvas Points into a Base64 string to fit cleanly into a CSV cell
    val signatureData = if (signaturePoints.isEmpty()) "UNVERIFIED" else {
        val rawCoords = signaturePoints.joinToString("|") {
            if (it == Offset.Unspecified) "BREAK" else "${it.x.toInt()}_${it.y.toInt()}"
        }
        Base64.encodeToString(rawCoords.toByteArray(), Base64.NO_WRAP)
    }

    val safeRemarks = state.remarks.replace("\"", "\"\"") // Escape quotes
    val row = "$date,$time,$shift,${state.juiceFlow},${state.caneCarrierRpm},${state.rake1Rpm},${state.rake2Rpm}," +
            "${state.m1Rpm},${state.m1Amps},${state.m1Pressure}," +
            "${state.m2Rpm},${state.m2Amps},${state.m2Pressure}," +
            "${state.m3Rpm},${state.m3Amps},${state.m3Pressure},\"$safeRemarks\",$signatureData"

    return "$header\n$row"
}

fun downloadManualLogCsvToDevice(context: Context, csvData: String) {
    val fileName = "Manual_Mill_Log_${System.currentTimeMillis()}.csv"
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

fun printManualLogReport(context: Context, state: MillTelemetryState, signaturePoints: List<Offset>, date: String, time: String, shift: String) {
    Toast.makeText(context, "Preparing Report for Printer...", Toast.LENGTH_SHORT).show()

    val safeRemarks = if (state.remarks.isEmpty()) "<i>No annotations provided.</i>" else state.remarks

    // Generate an SVG path string to literally draw the signature in the HTML
    val signatureSvg = if (signaturePoints.isEmpty()) {
        "<p style='color: #EF4444; font-weight: bold;'>SIGNATURE MISSING / UNVERIFIED</p>"
    } else {
        var maxX = 0f
        var maxY = 0f
        val pathStr = buildString {
            var first = true
            for (p in signaturePoints) {
                if (p == Offset.Unspecified) {
                    first = true
                } else {
                    // Track bounds to dynamically scale the SVG printout ViewBox properly (Fixing the cut-off sign)
                    if (p.x > maxX) maxX = p.x
                    if (p.y > maxY) maxY = p.y

                    if (first) { append("M ${p.x} ${p.y} "); first = false }
                    else { append("L ${p.x} ${p.y} ") }
                }
            }
        }

        // Calculate scaling variables so that high density device pixels scale flawlessly to physical paper
        val vBoxW = maxOf(maxX + 20f, 100f).toInt()
        val vBoxH = maxOf(maxY + 20f, 50f).toInt()
        val strokeW = maxOf(vBoxH / 40f, 2.5f)

        "<svg width='100%' height='150' viewBox='0 0 $vBoxW $vBoxH' preserveAspectRatio='xMidYMid meet' style='border:1px solid #E2E8F0; background:#F8FAFC; border-radius: 6px;'><path d='$pathStr' fill='transparent' stroke='#0F172A' stroke-width='$strokeW' stroke-linecap='round' stroke-linejoin='round'/></svg>"
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
            <h1>Mill Section - Manual Entry Log</h1>
            <p><strong>Date:</strong> $date &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Time:</strong> $time &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Shift:</strong> $shift</p>

            <h2>Process Balances</h2>
            <table>
                <tr><th>Juice Flow (M³/HR)</th><td>${state.juiceFlow}</td></tr>
                <tr><th>Cane Carrier (RPM)</th><td>${state.caneCarrierRpm}</td></tr>
                <tr><th>Rake Array 01 (RPM)</th><td>${state.rake1Rpm}</td></tr>
                <tr><th>Rake Array 02 (RPM)</th><td>${state.rake2Rpm}</td></tr>
            </table>

            <h2>Milling Matrix Data</h2>
            <table>
                <tr><th>Mill Drive 01</th><td><b>Spd:</b> ${state.m1Rpm} RPM | <b>Load:</b> ${state.m1Amps} A | <b>Press:</b> ${state.m1Pressure} KG/CM²</td></tr>
                <tr><th>Mill Drive 02</th><td><b>Spd:</b> ${state.m2Rpm} RPM | <b>Load:</b> ${state.m2Amps} A | <b>Press:</b> ${state.m2Pressure} KG/CM²</td></tr>
                <tr><th>Mill Drive 03</th><td><b>Spd:</b> ${state.m3Rpm} RPM | <b>Load:</b> ${state.m3Amps} A | <b>Press:</b> ${state.m3Pressure} KG/CM²</td></tr>
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
            val jobName = "Manual_Mill_Log_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

// ─────────────────────────────────────────────────────────────────────────────
// UI PALETTE & STYLES
// ─────────────────────────────────────────────────────────────────────────────
private data class MorphicPalette(
    val baseChassis: Color, val glassFill: Color, val glassBorder: Color,
    val inputContainer: Color, val textPrimary: Color, val textMuted: Color,
    val dividerLine: Color, val inputBorderUnfocused: Color
)

@Composable
private fun getDynamicMorphicPalette(isDark: Boolean): MorphicPalette {
    return if (isDark) {
        MorphicPalette(
            baseChassis = Color(0xFF0A0C14), glassFill = Color(0x13FFFFFF), glassBorder = Color(0x26FFFFFF),
            inputContainer = Color(0x1A000000), textPrimary = Color(0xFFF0F6FC), textMuted = Color(0xFF8B949E),
            dividerLine = Color(0xFF30363D), inputBorderUnfocused = Color(0xFF30363D)
        )
    } else {
        MorphicPalette(
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
// MAIN UI SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MillManualEntryScreen(
    viewModel: MillManualEntryViewModel = viewModel(factory = MillManualEntryViewModel.provideFactory()),
    onRaiseTicket: (String) -> Unit,
    onNavigationCallback: () -> Unit
) {
    val context = LocalContext.current
    val liveState by viewModel.state.collectAsState()

    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var state by remember { mutableStateOf(MillTelemetryState()) }

    val signaturePoints = remember { mutableStateListOf<Offset>() }

    val speechTranslator = remember(context) { MorphicSpeechTranslator(context) }
    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getDynamicMorphicPalette(isDark = isDarkThemeOverride)

    // Controls the export popup
    var showExportDialog by remember { mutableStateOf(false) }

    // Clock Loop
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    // Sync Live PLC Data from ViewModel when not manually overridden/submitted
    LaunchedEffect(liveState) {
        if (!state.isSubmitted) {
            val m1 = liveState.dashboard.motors.getOrNull(0)
            val m2 = liveState.dashboard.motors.getOrNull(1)
            val m3 = liveState.dashboard.motors.getOrNull(2)

            fun extractAmps(m: MillManualMotorData?) = m?.healthValue?.replace(Regex("[^0-9.]"), "") ?: ""
            fun extractRpm(m: MillManualMotorData?) = Regex("(\\d+)").find(m?.statusText ?: "")?.value ?: ""

            state = state.copy(
                juiceFlow = String.format("%.1f", liveState.rawJuice.volumeFlowM3hr),
                m1Amps = extractAmps(m1).ifEmpty { state.m1Amps },
                m1Rpm = extractRpm(m1).ifEmpty { state.m1Rpm },
                m2Amps = extractAmps(m2).ifEmpty { state.m2Amps },
                m2Rpm = extractRpm(m2).ifEmpty { state.m2Rpm },
                m3Amps = extractAmps(m3).ifEmpty { state.m3Amps },
                m3Rpm = extractRpm(m3).ifEmpty { state.m3Rpm }
            )
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

    val structuralBackgroundModifier = if (isDarkThemeOverride) {
        Modifier.background(Brush.radialGradient(colors = listOf(Color(0xFF1E1B4B), Color(0xFF090A10)), radius = 2200f))
    } else {
        Modifier.background(palette.baseChassis)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(structuralBackgroundModifier)
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(1280.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ============================================================================
            // MASTER HEADER
            // ============================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (isDarkThemeOverride) 0.dp else 2.dp, shape = RoundedCornerShape(10.dp))
                    .background(palette.glassFill, RoundedCornerShape(10.dp))
                    .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MILL HARMONIZED CONTROL MATRIX [STATUS: ${liveState.connectionStatus}]",
                        color = if (liveState.connectionStatus == "CONNECTED") TechAccentGreen else palette.textPrimary,
                        fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkThemeOverride) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = null, tint = if (isDarkThemeOverride) TechAccentBlue else TechWarnOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isDarkThemeOverride) "GLASS MODE: DARK" else "GLASS MODE: LIGHT",
                            color = palette.textMuted, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isDarkThemeOverride, onCheckedChange = { isDarkThemeOverride = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = TechAccentBlue,
                                uncheckedThumbColor = Color.White, uncheckedTrackColor = TechWarnOrange.copy(alpha = 0.4f),
                                uncheckedBorderColor = Color.Transparent, checkedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.graphicsLayer(scaleX = 0.65f, scaleY = 0.65f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("DATE: $currentDate", fontFamily = FontTelemetryMono, fontSize = 11.sp, color = palette.textMuted, fontWeight = FontWeight.SemiBold)
                    Text("TIME: $currentTimeFormatted | $currentShift", fontFamily = FontTelemetryMono, fontSize = 11.sp, color = TechWarnOrange, fontWeight = FontWeight.Bold)
                }
            }

            // ============================================================================
            // SMART BALANCED GRID ARCHITECTURE
            // ============================================================================
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🛑 LEFT DASHBOARD COLUMN
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Process Balances Card
                    MillFormSectionCard(title = "FEED CONVEYANCE LOOP PROCESS BALANCES", icon = Icons.Rounded.Speed, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MillLogInputField("JUICE FLOW (M³/HR)", state.juiceFlow, palette, Modifier.weight(1f)) { state = state.copy(juiceFlow = it, isSubmitted = false) }
                                MillLogInputField("CANE CARRIER (RPM)", state.caneCarrierRpm, palette, Modifier.weight(1f)) { state = state.copy(caneCarrierRpm = it, isSubmitted = false) }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MillLogInputField("RAKE ARRAY 01 (RPM)", state.rake1Rpm, palette, Modifier.weight(1f)) { state = state.copy(rake1Rpm = it, isSubmitted = false) }
                                MillLogInputField("RAKE ARRAY 02 (RPM)", state.rake2Rpm, palette, Modifier.weight(1f)) { state = state.copy(rake2Rpm = it, isSubmitted = false) }
                            }
                        }
                    }

                    // 2. Milling Engine Matrix Card
                    MillFormSectionCard(title = "CONDUIT MILLING ENERGY MATRIX GRIDS", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            MillConduitParameterRow(
                                indexLabel = "MILL MOTOR DRIVE 01 [PLC Live]", rpm = state.m1Rpm, amps = state.m1Amps, press = state.m1Pressure, palette = palette,
                                onRpm = { state = state.copy(m1Rpm = it, isSubmitted = false) }, onAmps = { state = state.copy(m1Amps = it, isSubmitted = false) }, onPress = { state = state.copy(m1Pressure = it, isSubmitted = false) }
                            )
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                            MillConduitParameterRow(
                                indexLabel = "MILL MOTOR DRIVE 02 [PLC Live]", rpm = state.m2Rpm, amps = state.m2Amps, press = state.m2Pressure, palette = palette,
                                onRpm = { state = state.copy(m2Rpm = it, isSubmitted = false) }, onAmps = { state = state.copy(m2Amps = it, isSubmitted = false) }, onPress = { state = state.copy(m2Pressure = it, isSubmitted = false) }
                            )
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                            MillConduitParameterRow(
                                indexLabel = "MILL MOTOR DRIVE 03 [PLC Live]", rpm = state.m3Rpm, amps = state.m3Amps, press = state.m3Pressure, palette = palette,
                                onRpm = { state = state.copy(m3Rpm = it, isSubmitted = false) }, onAmps = { state = state.copy(m3Amps = it, isSubmitted = false) }, onPress = { state = state.copy(m3Pressure = it, isSubmitted = false) }
                            )
                        }
                    }

                    // 3. Juice Recovery Sub-System
                    MillFormSectionCard(title = "JUICE RECOVERY SUB-SYSTEM (AUTOMATED BY PLC)", icon = Icons.Rounded.SettingsInputComponent, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("JUICE PUMPS CONTROLS:", color = palette.textMuted, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val isRunning = liveState.rawJuice.pumpStatus.name == "RUNNING"
                                    val isFault = liveState.rjPumpFault
                                    TechDisabledChip("PUMP 01 ACTIVE", isRunning)
                                    TechDisabledChip(if (isFault) "PUMP FAULT" else "PUMP OK", !isFault)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VIBRO SCREEN ARRAY:", color = palette.textMuted, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TechDisabledChip("RUNNING", true)
                                    TechDisabledChip("SCREEN OK", true)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ROTARY SCREEN SYSTEM:", color = palette.textMuted, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TechDisabledChip("RUNNING", true)
                                    TechDisabledChip("SCREEN OK", true)
                                }
                            }
                        }
                    }
                }

                // 🛑 RIGHT DASHBOARD COLUMN
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Operator annotations card
                    MillFormSectionCard(title = "OPERATOR RECORDING LOG ANNOTATIONS", icon = Icons.Rounded.Notes, palette = palette, isDark = isDarkThemeOverride) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer)
                                .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            BasicTextField(
                                value = state.remarks,
                                onValueChange = { state = state.copy(remarks = it, isSubmitted = false) },
                                textStyle = TextStyle(fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 14.sp),
                                cursorBrush = SolidColor(palette.textPrimary),
                                modifier = Modifier.fillMaxSize().padding(end = 32.dp),
                                decorationBox = { innerTextField ->
                                    if (state.remarks.isEmpty()) {
                                        Text(
                                            text = "INPUT LIVE PLC EXCEPTIONS, MECHANICAL DEVIATIONS OR CRITICAL SHIFT NOTES EXTENSIONS...",
                                            color = if (isListening) TechAlarmRed else palette.textMuted,
                                            fontSize = 12.sp,
                                            fontFamily = FontTelemetryMono,
                                            lineHeight = 18.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                if (isTranslating) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TechAccentBlue, strokeWidth = 2.dp)
                                } else {
                                    IconButton(
                                        modifier = Modifier.size(28.dp),
                                        onClick = {
                                            if (!isListening) {
                                                val hasRecordPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                                                        android.content.pm.PackageManager.PERMISSION_GRANTED

                                                if (hasRecordPermission) {
                                                    speechTranslator.startListening(
                                                        onStatusChange = { currentVoiceStatusText = it },
                                                        onListeningStateChange = { isListening = it },
                                                        onTranslatingStateChange = { isTranslating = it },
                                                        onResultReceived = { state = state.copy(remarks = it, isSubmitted = false) }
                                                    )
                                                } else {
                                                    (context as? android.app.Activity)?.requestPermissions(
                                                        arrayOf(android.Manifest.permission.RECORD_AUDIO), 101
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                                            contentDescription = null, tint = if (isListening) TechAlarmRed else TechAccentBlue, modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Glassfrost styled interactive Touch Signature Box
                    MillFormSectionCard(title = "OPERATOR DIGITAL ESIGN VERIFICATION", icon = Icons.Rounded.Draw, palette = palette, isDark = isDarkThemeOverride) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("DRAW RECOGNIZED SIGNATURE ON CANVAS:", color = palette.textPrimary, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "RESET TRACE",
                                    color = TechAlarmRed,
                                    fontSize = 10.sp,
                                    fontFamily = FontTelemetryMono,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { signaturePoints.clear() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp) // Increased slightly for better signature tracing
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                                    .background(palette.glassFill, RoundedCornerShape(8.dp))
                                    .border(1.dp, palette.glassBorder, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp)) // Ensuring paths do not bleed out of the box bounds
                            ) {
                                if (signaturePoints.isEmpty()) {
                                    Text(
                                        text = "TOUCH RECOGNITION BOUNDARY MATRIX ACTIVE...",
                                        color = palette.textMuted.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontFamily = FontTelemetryMono,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                SignatureCaptureCanvas(
                                    points = signaturePoints,
                                    strokeColor = if (isDarkThemeOverride) TechAccentBlue else palette.textPrimary,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // ============================================================================
            // BOTTOM ACTION FOOTER BAR
            // ============================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (isDarkThemeOverride) 0.dp else 2.dp, shape = RoundedCornerShape(10.dp))
                    .background(palette.glassFill, RoundedCornerShape(10.dp))
                    .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (signaturePoints.isNotEmpty()) TechAccentGreen.copy(alpha = 0.15f) else TechAlarmRed.copy(alpha = 0.08f))
                        .border(1.dp, if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (signaturePoints.isNotEmpty()) "ESIGN LOCK DETECTED ✔" else "ESIGN SIGNATURE TRACE MISSING ❌",
                        color = if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed,
                        fontSize = 11.sp,
                        fontFamily = FontTelemetryMono,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .background(Color.Transparent)
                            .clickable {
                                state = MillTelemetryState()
                                signaturePoints.clear()
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("RESET LOCK", color = palette.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
                    }

                    // Triggers the Export Dialog overlay
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(220.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.linearGradient(colors = if (state.isSubmitted) listOf(TechAccentGreen, TechAccentGreen.copy(alpha = 0.8f)) else listOf(TechAccentBlue, TechAccentBlue.copy(alpha = 0.8f))))
                            .clickable {
                                if (signaturePoints.isNotEmpty()) {
                                    showExportDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("COMMIT JOURNAL LOG", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                    }
                }
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
                    .clickable(enabled = false) {}, // Intercept clicks
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

                    Text("LOG COMMITTED SUCCESSFULLY", color = TechAccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Do you want to export a copy of this manual entry before closing?",
                        color = palette.textPrimary, fontSize = 13.sp, fontFamily = FontTelemetryMono, textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        // 1. PRINT BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    // Notice: we are passing the signaturePoints so the PDF draws the exact path!
                                    printManualLogReport(context, state, signaturePoints, currentDate, currentTimeFormatted, currentShift)
                                    state = state.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Print, null, tint = TechAccentBlue, modifier = Modifier.size(18.dp))
                                Text("PRINT PDF", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
                            }
                        }

                        // 2. CSV DOWNLOAD BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    // Passed signature points to convert them to Base64 in the CSV column
                                    val csvData = generateManualLogCsv(state, signaturePoints, currentDate, currentTimeFormatted, currentShift)
                                    downloadManualLogCsvToDevice(context, csvData)
                                    state = state.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Download, null, tint = TechAccentBlue, modifier = Modifier.size(18.dp))
                                Text("SAVE CSV", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
                            }
                        }

                        // 3. SKIP BUTTON
                        Box(
                            modifier = Modifier.weight(0.7f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(TechAccentBlue)
                                .clickable {
                                    state = state.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SKIP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SignatureCaptureCanvas(
    points: MutableList<Offset>,
    strokeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clipToBounds() // Guarantee visually strokes halt exactly at boundaries inside UI
            .pointerInput(Unit) { // Stable Compose Pointer Input (Replaces flaky pointerInteropFilter logic)
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue

                        if (change.pressed) {
                            points.add(change.position)
                            change.consume()
                        } else {
                            if (points.isNotEmpty() && points.last() != Offset.Unspecified) {
                                points.add(Offset.Unspecified)
                            }
                        }
                    }
                }
            }
    ) {
        if (points.size > 1) {
            val path = Path()
            var first = true
            for (point in points) {
                if (point == Offset.Unspecified) {
                    first = true
                    continue
                }
                if (first) {
                    path.moveTo(point.x, point.y)
                    first = false
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun MillFormSectionCard(
    title: String, icon: ImageVector, palette: MorphicPalette, isDark: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (isDark) 0.dp else 2.dp, shape = RoundedCornerShape(14.dp), clip = false)
            .background(palette.glassFill, RoundedCornerShape(14.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = TechAccentBlue, modifier = Modifier.size(16.dp))
            Text(text = title, color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun MillConduitParameterRow(
    indexLabel: String, rpm: String, amps: String, press: String, palette: MorphicPalette,
    onRpm: (String) -> Unit, onAmps: (String) -> Unit, onPress: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = indexLabel, color = palette.textPrimary.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MillLogInputField("SPEED (RPM)", rpm, palette, Modifier.weight(1f), onRpm)
            MillLogInputField("LOAD (AMPS)", amps, palette, Modifier.weight(1f), onAmps)
            MillLogInputField("HYD PRESS (KG/CM²)", press, palette, Modifier.weight(1f), onPress)
        }
    }
}

@Composable
private fun MillLogInputField(
    label: String, value: String, palette: MorphicPalette, modifier: Modifier = Modifier, onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(palette.inputContainer)
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, color = palette.textMuted, fontSize = 9.5.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(1.dp))
            BasicTextField(
                value = value, onValueChange = onValueChange,
                textStyle = TextStyle(fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                cursorBrush = SolidColor(palette.textPrimary), modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TechDisabledChip(text: String, isLiveActive: Boolean) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(115.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isLiveActive) Color(0x1F10B981) else Color(0x0D111827))
            .border(1.dp, if (isLiveActive) TechAccentGreen.copy(alpha = 0.4f) else Color(0x1F111827), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isLiveActive) TechAccentGreen else Color.Gray)
            )
            Text(
                text = text,
                color = if (isLiveActive) TechAccentGreen else Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontTelemetryMono,
                fontWeight = FontWeight.Bold
            )
        }
    }
}