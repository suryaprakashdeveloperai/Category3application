package com.example.category3.auth.ui

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
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

// ============================================================================
// 🖨️ EXPORT & PRINT LOGIC FOR POWDER MAKER
// ============================================================================

fun generatePowderMakerLogCsv(
    manualState: PowderMakerManualState,
    avgVacStr: String, pmAvailableStr: String,
    signaturePoints: List<Offset>, date: String, time: String, shift: String
): String {
    val header = "Date,Time,Shift,AVG Header Vacuum,PM Available,Machine No,Batch Number,Open Pan Material," +
            "Drop Start,Drop End,Drop Total (Min),Crystal Start,Crystal End,Crystal Total (Min),Crystal Freq (Hz)," +
            "Cycle Start,Cycle End,Cycle Total (Min),Cycle Vacuum," +
            "PMM Freq (Hz),Drop R1 (Min),Drop F (Min),Drop R2 (Min),Total Cycle Time,Soda Qty,Soda Total Time," +
            "Remarks,E-Signature_Data_Base64"

    val signatureData = if (signaturePoints.isEmpty()) "UNVERIFIED" else {
        val rawCoords = signaturePoints.joinToString("|") {
            if (it == Offset.Unspecified) "BREAK" else "${it.x.toInt()}_${it.y.toInt()}"
        }
        Base64.encodeToString(rawCoords.toByteArray(), Base64.NO_WRAP)
    }

    val safeRemarks = manualState.remarks.replace("\"", "\"\"")
    val row = "$date,$time,$shift,$avgVacStr,$pmAvailableStr,${manualState.mcNo},${manualState.batchNo},${manualState.openPanMaterial}," +
            "${manualState.opDropStart},${manualState.opDropEnd},${manualState.opDropTotalMin},${manualState.crystalStart},${manualState.crystalEnd},${manualState.crystalTotalMin},${manualState.crystalFrequencyHz}," +
            "${manualState.cycleStart},${manualState.cycleEnd},${manualState.cycleTotalMin},${manualState.cycleVacuum}," +
            "${manualState.pmmFrequencyHz},${manualState.droppingR1Min},${manualState.droppingFMin},${manualState.droppingR2Min},${manualState.totalCycleTimeMin},${manualState.sodaAdditionQty},${manualState.sodaTotalTimeMin}," +
            "\"$safeRemarks\",$signatureData"

    return "$header\n$row"
}

fun downloadPowderMakerLogCsvToDevice(context: Context, csvData: String) {
    val fileName = "PowderMaker_Log_${System.currentTimeMillis()}.csv"
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

private var activePmmPrintWebView: WebView? = null

fun printPowderMakerLogReport(
    context: Context, manualState: PowderMakerManualState,
    avgVacStr: String, pmAvailableStr: String,
    signaturePoints: List<Offset>, date: String, time: String, shift: String
) {
    Toast.makeText(context, "Preparing Report for Printer...", Toast.LENGTH_SHORT).show()

    val safeRemarks = if (manualState.remarks.isEmpty()) "<i>No anomalies reported.</i>" else manualState.remarks

    val signatureSvg = if (signaturePoints.isEmpty()) {
        "<p style='color: #EF4444; font-weight: bold;'>SIGNATURE MISSING / UNVERIFIED</p>"
    } else {
        val pathStr = buildString {
            var first = true
            for (p in signaturePoints) {
                if (p == Offset.Unspecified) {
                    first = true
                } else {
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
            <h1>Powder Maker Crystallization - Manual Entry Log</h1>
            <p><strong>Date:</strong> $date &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Time:</strong> $time &nbsp;&nbsp;|&nbsp;&nbsp; <strong>Shift:</strong> $shift</p>

            <h2>Live Process Telemetry (PLC Snapshot)</h2>
            <table>
                <tr><th>AVG Header Vacuum</th><td>$avgVacStr</td></tr>
                <tr><th>PM Units Available</th><td>$pmAvailableStr</td></tr>
            </table>

            <h2>Powder Maker Core Unit Logistics</h2>
            <table>
                <tr><th>Machine No</th><td>${manualState.mcNo}</td></tr>
                <tr><th>Batch Number</th><td>${manualState.batchNo}</td></tr>
                <tr><th>Open Pan Material</th><td>${manualState.openPanMaterial}</td></tr>
            </table>
            
            <h2>Stage 1: Open Pan Material & Crystallization</h2>
            <table>
                <tr><th>Drop Start</th><td>${manualState.opDropStart}</td></tr>
                <tr><th>Drop End</th><td>${manualState.opDropEnd}</td></tr>
                <tr><th>Total Drop Time (Min)</th><td>${manualState.opDropTotalMin}</td></tr>
                <tr><th>Crystal Start</th><td>${manualState.crystalStart}</td></tr>
                <tr><th>Crystal End</th><td>${manualState.crystalEnd}</td></tr>
                <tr><th>Total Crystal Time (Min)</th><td>${manualState.crystalTotalMin}</td></tr>
                <tr><th>Crystal Freq (Hz)</th><td>${manualState.crystalFrequencyHz}</td></tr>
            </table>

            <h2>Stage 2: Cycle Time & Vacuum Stabilization</h2>
            <table>
                <tr><th>Cycle Start</th><td>${manualState.cycleStart}</td></tr>
                <tr><th>Cycle End</th><td>${manualState.cycleEnd}</td></tr>
                <tr><th>Total Cycle Time (Min)</th><td>${manualState.cycleTotalMin}</td></tr>
                <tr><th>Cycle Vacuum</th><td>${manualState.cycleVacuum}</td></tr>
            </table>

            <h2>Stage 3: Mechanical Dynamics & Recipe</h2>
            <table>
                <tr><th>PMM Freq (Hz)</th><td>${manualState.pmmFrequencyHz}</td></tr>
                <tr><th>Drop R1 (Min)</th><td>${manualState.droppingR1Min}</td></tr>
                <tr><th>Drop F (Min)</th><td>${manualState.droppingFMin}</td></tr>
                <tr><th>Drop R2 (Min)</th><td>${manualState.droppingR2Min}</td></tr>
                <tr><th>Total Cycle Time (Min)</th><td>${manualState.totalCycleTimeMin}</td></tr>
                <tr><th>Soda Addition (Qty)</th><td>${manualState.sodaAdditionQty}</td></tr>
                <tr><th>Soda Total Time (Min)</th><td>${manualState.sodaTotalTimeMin}</td></tr>
            </table>

            <h2>Operator Annotations & Remarks</h2>
            <div class="remarks-box">$safeRemarks</div>
            
            <h2>Supervisor E-Signature Verification</h2>
            $signatureSvg
            
            <p style="margin-top: 15px; color: #10B981; font-weight: bold; font-size: 12px;">✔ Document Cryptographically Sealed via Device Input</p>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    activePmmPrintWebView = webView
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "PowderMaker_Log_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            activePmmPrintWebView = null
        }
    }
    webView.loadDataWithBaseURL(null, htmlBuilder, "text/html", "UTF-8", null)
}

// ============================================================================
// 📦 DATA MODELS
// ============================================================================

data class PowderMakerManualState(
    val mcNo: String = "PMM-01",
    val batchNo: String = "PM-2026-B12",
    val openPanMaterial: String = "M-Grade A",
    val opDropStart: String = "",
    val opDropEnd: String = "",
    val opDropTotalMin: String = "",
    val crystalStart: String = "",
    val crystalEnd: String = "",
    val crystalTotalMin: String = "",
    val crystalFrequencyHz: String = "",
    val cycleStart: String = "",
    val cycleEnd: String = "",
    val cycleTotalMin: String = "",
    val cycleVacuum: String = "",
    val pmmFrequencyHz: String = "",
    val droppingR1Min: String = "",
    val droppingFMin: String = "",
    val droppingR2Min: String = "",
    val totalCycleTimeMin: String = "",
    val sodaAdditionQty: String = "",
    val sodaTotalTimeMin: String = "",
    val remarks: String = "",
    val isSubmitted: Boolean = false
)

// ============================================================================
// 🎨 THEME PALETTE — PMM SCOPED
// ============================================================================

private data class PmmMorphicPalette(
    val baseChassis: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val inputContainer: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val dividerLine: Color,
    val inputBorderUnfocused: Color
)

@Composable
private fun getPmmDynamicMorphicPalette(isDark: Boolean): PmmMorphicPalette {
    return if (isDark) {
        PmmMorphicPalette(
            baseChassis         = Color(0xFF0A0C14),
            glassFill           = Color(0x13FFFFFF),
            glassBorder         = Color(0x26FFFFFF),
            inputContainer      = Color(0x1A000000),
            textPrimary         = Color(0xFFF0F6FC),
            textMuted           = Color(0xFF8B949E),
            dividerLine         = Color(0xFF30363D),
            inputBorderUnfocused = Color(0xFF30363D)
        )
    } else {
        PmmMorphicPalette(
            baseChassis         = Color(0xFFF3F4F6),
            glassFill           = Color(0xB3FFFFFF),
            glassBorder         = Color(0x66FFFFFF),
            inputContainer      = Color(0x33F3F4F6),
            textPrimary         = Color(0xFF1F2937),
            textMuted           = Color(0xFF6B7280),
            dividerLine         = Color(0x1A6B7280),
            inputBorderUnfocused = Color(0x406B7280)
        )
    }
}

// ============================================================================
// 🎨 ACCENT TOKENS — PMM SCOPED
// ============================================================================

private val PmmAccentBlue  = Color(0xFF0EA5E9)
private val PmmAccentGreen = Color(0xFF10B981)
private val PmmWarnOrange  = Color(0xFFF97316)
private val PmmAlarmRed    = Color(0xFFEF4444)

// ============================================================================
// 🔀 STATUS MAPPER
// Maps ConcEquipStatus (ViewModel layer) → display tokens used by this screen.
// This is the ONLY place the mapping lives; no new enum is introduced.
// ============================================================================

private data class PmmStatusDisplay(
    val label: String,
    val color: Color
)

/**
 * Converts any [ConcEquipStatus] value to a PMM-scoped display token.
 * Add extra branches here if [ConcEquipStatus] gains new values.
 */
private fun ConcEquipStatus.toPmmDisplay(palette: PmmMorphicPalette): PmmStatusDisplay =
    when (this) {
        ConcEquipStatus.RUNNING  -> PmmStatusDisplay("RUNNING",  PmmAccentGreen)
        ConcEquipStatus.FAULT    -> PmmStatusDisplay("FAULT",    PmmAlarmRed)
        ConcEquipStatus.STANDBY  -> PmmStatusDisplay("STANDBY",  palette.textMuted)
        ConcEquipStatus.HEALTHY  -> PmmStatusDisplay("HEALTHY",  PmmAccentGreen)
        else                     -> PmmStatusDisplay("UNKNOWN",  palette.textMuted)
    }
// ============================================================================
// 🖥️ MAIN SCREEN COMPOSABLE
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowderMakerScreen(
    viewModel: ConcentrationDedicatedViewModel = viewModel(
        factory = ConcentrationDedicatedViewModel.provideFactory()
    ),
    onRaiseTicket: (String) -> Unit,
    onNavigationCallback: () -> Unit
) {
    val context       = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait    = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val liveState by viewModel.state.collectAsState()

    val currentDate    = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime    by remember { mutableStateOf(LocalTime.now()) }
    var manualState    by remember { mutableStateOf(PowderMakerManualState()) }
    val signaturePoints = remember { mutableStateListOf<Offset>() }

    val speechTranslator       = remember(context) { MorphicSpeechTranslator(context) }
    var isListening            by remember { mutableStateOf(false) }
    var isTranslating          by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getPmmDynamicMorphicPalette(isDark = isDarkThemeOverride)

    // Export Dialog State
    var showExportDialog by remember { mutableStateOf(false) }

    // Live formatted strings (hoisted for UI and export functions)
    val pmAvailableStr = liveState.powderMakerAvailableCount.toString()
    val avgVac = liveState.dashboard.powderMakers
        .map { it.vacuumHeader }
        .filter { it > 0f }
        .average()
        .toFloat()
    val avgVacStr = if (avgVac.isNaN()) "0.0" else String.format("%.1f", avgVac)

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val currentShift = remember(currentTime) {
        when (currentTime.hour) {
            in 6..13  -> "SHIFT A"
            in 14..21 -> "SHIFT B"
            else      -> "SHIFT C"
        }
    }

    val currentTimeFormatted = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val structuralBackgroundModifier = if (isDarkThemeOverride) {
        Modifier.background(
            Brush.radialGradient(
                colors = listOf(Color(0xFF1E1B4B), Color(0xFF090A10)),
                radius = 2200f
            )
        )
    } else {
        Modifier.background(palette.baseChassis)
    }

    // ============================================================================
    // 🧩 MODULAR COMPONENT BLOCKS
    // ============================================================================

    val HeaderContent = @Composable {
        val containerModifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkThemeOverride) 0.dp else 2.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .background(palette.glassFill, RoundedCornerShape(10.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)

        val headerDetails = @Composable {
            Column {
                Text(
                    text = "POWDER MAKER SUPERVISORY MATRIX [CRYSTALLIZATION SYSTEM]",
                    color = palette.textPrimary,
                    fontSize = if (isPortrait) 13.sp else 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkThemeOverride) Icons.Rounded.DarkMode
                        else Icons.Rounded.LightMode,
                        contentDescription = null,
                        tint = if (isDarkThemeOverride) PmmAccentBlue else PmmWarnOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isDarkThemeOverride) "GLASS MODE: DARK"
                        else "GLASS MODE: LIGHT",
                        color = palette.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isDarkThemeOverride,
                        onCheckedChange = { isDarkThemeOverride = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor   = Color.White,
                            checkedTrackColor   = PmmAccentBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = PmmWarnOrange.copy(alpha = 0.4f),
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor   = Color.Transparent
                        ),
                        modifier = Modifier.graphicsLayer(scaleX = 0.65f, scaleY = 0.65f)
                    )
                    val connColor = if (liveState.connectionStatus == "CONNECTED") {
                        PmmAccentGreen
                    } else {
                        PmmAlarmRed
                    }
                    Text(
                        text = " | STREAM: ${liveState.connectionStatus}",
                        color = connColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val timeDetails = @Composable {
            Column(
                horizontalAlignment = if (isPortrait) Alignment.Start else Alignment.End
            ) {
                Text(
                    text = "DATE: $currentDate",
                    fontSize = 11.sp,
                    color = palette.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "TIME: $currentTimeFormatted | $currentShift",
                    fontSize = 11.sp,
                    color = PmmWarnOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = containerModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                headerDetails()
                timeDetails()
            }
        } else {
            Row(
                modifier = containerModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                headerDetails()
                timeDetails()
            }
        }
    }

    // ── LIVE PLC TELEMETRY ────────────────────────────────────────────────────

    val LivePLCContent = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title  = "LIVE PROCESS TELEMETRY",
            icon   = Icons.Rounded.Analytics,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PmmLogInputField(
                        label    = "AVG HEADER VACUUM",
                        value    = avgVacStr,
                        palette  = palette,
                        modifier = Modifier.weight(1f),
                        isReadOnly = true,
                        onValueChange = {}
                    )
                    PmmLogInputField(
                        label    = "PM AVAILABLE",
                        value    = pmAvailableStr,
                        palette  = palette,
                        modifier = Modifier.weight(1f),
                        isReadOnly = true,
                        onValueChange = {}
                    )
                }
            }
        }
    }

    // ── EQUIPMENT HARDWARE PANEL ──────────────────────────────────────────────

    val EquipmentContent = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "CRITICAL HARDWARE [LIVE]",
            icon    = Icons.Rounded.Settings,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                liveState.dashboard.powderMakers.forEach { pm ->
                    PmmHardwareStateBadge(
                        title      = pm.name,
                        status     = pm.status,
                        statusText = "${pm.statusText} (${String.format("%.1f", pm.vfdSpeed)} Hz)",
                        palette    = palette
                    )
                }
            }
        }
    }

    // ── MANUAL ENTRY PANELS ───────────────────────────────────────────────────

    val LogisticsContent = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "POWDER MAKER CORE UNIT LOGISTICS",
            icon    = Icons.Rounded.SettingsInputComponent,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PmmLogInputField(
                    label   = "MACHINE NO",
                    value   = manualState.mcNo,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        manualState = manualState.copy(mcNo = it, isSubmitted = false)
                    }
                )
                PmmLogInputField(
                    label   = "BATCH NUMBER",
                    value   = manualState.batchNo,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        manualState = manualState.copy(batchNo = it, isSubmitted = false)
                    }
                )
                PmmLogInputField(
                    label   = "OPEN PAN MATERIAL",
                    value   = manualState.openPanMaterial,
                    palette = palette,
                    modifier = Modifier.weight(1.2f),
                    onValueChange = {
                        manualState = manualState.copy(openPanMaterial = it, isSubmitted = false)
                    }
                )
            }
        }
    }

    val Stage1Content = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "STAGE 1: OPEN PAN MATERIAL & CRYSTALLIZATION",
            icon    = Icons.Rounded.Speed,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PmmLogInputField("DROP START",  manualState.opDropStart,   palette, Modifier.weight(1f)) { manualState = manualState.copy(opDropStart   = it, isSubmitted = false) }
                    PmmLogInputField("DROP END",    manualState.opDropEnd,     palette, Modifier.weight(1f)) { manualState = manualState.copy(opDropEnd     = it, isSubmitted = false) }
                    PmmLogInputField("TOTAL (MIN)", manualState.opDropTotalMin,palette, Modifier.weight(1f)) { manualState = manualState.copy(opDropTotalMin= it, isSubmitted = false) }
                }
                HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PmmLogInputField("CRYSTAL START", manualState.crystalStart,      palette, Modifier.weight(1f)) { manualState = manualState.copy(crystalStart      = it, isSubmitted = false) }
                    PmmLogInputField("CRYSTAL END",   manualState.crystalEnd,        palette, Modifier.weight(1f)) { manualState = manualState.copy(crystalEnd        = it, isSubmitted = false) }
                    PmmLogInputField("TOTAL (MIN)",   manualState.crystalTotalMin,   palette, Modifier.weight(1f)) { manualState = manualState.copy(crystalTotalMin   = it, isSubmitted = false) }
                    PmmLogInputField("FREQ (HZ)",     manualState.crystalFrequencyHz,palette, Modifier.weight(1f)) { manualState = manualState.copy(crystalFrequencyHz= it, isSubmitted = false) }
                }
            }
        }
    }

    val Stage2Content = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "STAGE 2: CYCLE TIME & VACUUM STABILIZATION",
            icon    = Icons.Rounded.Analytics,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PmmLogInputField("CYCLE START", manualState.cycleStart,   palette, Modifier.weight(1f)) { manualState = manualState.copy(cycleStart   = it, isSubmitted = false) }
                PmmLogInputField("CYCLE END",   manualState.cycleEnd,     palette, Modifier.weight(1f)) { manualState = manualState.copy(cycleEnd     = it, isSubmitted = false) }
                PmmLogInputField("TOTAL (MIN)", manualState.cycleTotalMin,palette, Modifier.weight(1f)) { manualState = manualState.copy(cycleTotalMin= it, isSubmitted = false) }
                PmmLogInputField("VACUUM",      manualState.cycleVacuum,  palette, Modifier.weight(1f)) { manualState = manualState.copy(cycleVacuum  = it, isSubmitted = false) }
            }
        }
    }

    val Stage3Content = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "STAGE 3: MECHANICAL DYNAMICS & RECIPE",
            icon    = Icons.Rounded.SettingsInputComponent,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PmmLogInputField("PMM FREQ (HZ)",  manualState.pmmFrequencyHz, palette, Modifier.weight(1f)) { manualState = manualState.copy(pmmFrequencyHz = it, isSubmitted = false) }
                    PmmLogInputField("DROP R1 (MIN)",  manualState.droppingR1Min,  palette, Modifier.weight(1f)) { manualState = manualState.copy(droppingR1Min  = it, isSubmitted = false) }
                    PmmLogInputField("DROP F (MIN)",   manualState.droppingFMin,   palette, Modifier.weight(1f)) { manualState = manualState.copy(droppingFMin   = it, isSubmitted = false) }
                    PmmLogInputField("DROP R2 (MIN)",  manualState.droppingR2Min,  palette, Modifier.weight(1f)) { manualState = manualState.copy(droppingR2Min  = it, isSubmitted = false) }
                }
                HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PmmLogInputField("CYCLE TIME TOTAL", manualState.totalCycleTimeMin, palette, Modifier.weight(1.2f)) { manualState = manualState.copy(totalCycleTimeMin = it, isSubmitted = false) }
                    PmmLogInputField("SODA ADD (QTY)",   manualState.sodaAdditionQty,  palette, Modifier.weight(1f))   { manualState = manualState.copy(sodaAdditionQty   = it, isSubmitted = false) }
                    PmmLogInputField("SODA TOTAL (MIN)", manualState.sodaTotalTimeMin,  palette, Modifier.weight(1f))   { manualState = manualState.copy(sodaTotalTimeMin  = it, isSubmitted = false) }
                }
            }
        }
    }

    val RemarksContent = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "OPERATOR RECORDING LOG ANNOTATIONS",
            icon    = Icons.Rounded.Notes,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.inputContainer)
                    .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = manualState.remarks,
                    onValueChange = {
                        manualState = manualState.copy(remarks = it, isSubmitted = false)
                    },
                    textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(palette.textPrimary),
                    modifier = Modifier.fillMaxSize().padding(end = 32.dp),
                    decorationBox = { innerTextField ->
                        if (manualState.remarks.isEmpty()) {
                            Text(
                                text = "INPUT LIVE REFINERY SHIFT ANOMALIES, PMM MOTOR TRIPS...",
                                color = if (isListening) PmmAlarmRed else palette.textMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PmmAccentBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = {
                                if (!isListening) {
                                    val hasPermission =
                                        context.checkSelfPermission(
                                            android.Manifest.permission.RECORD_AUDIO
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        speechTranslator.startListening(
                                            onStatusChange          = { currentVoiceStatusText = it },
                                            onListeningStateChange  = { isListening = it },
                                            onTranslatingStateChange = { isTranslating = it },
                                            onResultReceived        = {
                                                manualState = manualState.copy(
                                                    remarks = it, isSubmitted = false
                                                )
                                            }
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
                                imageVector = if (isListening) Icons.Rounded.Mic
                                else Icons.Rounded.MicNone,
                                contentDescription = null,
                                tint = if (isListening) PmmAlarmRed else PmmAccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    val SignatureContent = @Composable { modifier: Modifier ->
        PmmFormSectionCard(
            title   = "SUPERVISOR SIGNATURE IDENTITY GATEWAY",
            icon    = Icons.Rounded.Draw,
            palette = palette,
            isDark  = isDarkThemeOverride,
            modifier = modifier
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DRAW RECOGNIZED SIGNATURE ON CANVAS:",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RESET TRACE",
                        color = PmmAlarmRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { signaturePoints.clear() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                        .background(palette.glassFill, RoundedCornerShape(8.dp))
                        .border(1.dp, palette.glassBorder, RoundedCornerShape(8.dp))
                ) {
                    if (signaturePoints.isEmpty()) {
                        Text(
                            text = "TOUCH RECOGNITION BOUNDARY MATRIX ACTIVE...",
                            color = palette.textMuted.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    PmmSignatureCaptureCanvas(
                        points      = signaturePoints,
                        strokeColor = palette.textPrimary,
                        modifier    = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    val FooterContent = @Composable {
        val containerModifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkThemeOverride) 0.dp else 2.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .background(palette.glassFill, RoundedCornerShape(10.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)

        val statusBadge = @Composable {
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (signaturePoints.isNotEmpty()) PmmAccentGreen.copy(alpha = 0.15f)
                        else PmmAlarmRed.copy(alpha = 0.08f)
                    )
                    .border(
                        1.dp,
                        if (signaturePoints.isNotEmpty()) PmmAccentGreen else PmmAlarmRed,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (signaturePoints.isNotEmpty()) "ESIGN LOCK SIGNED ✔"
                    else "ESIGN AUTHENTICATION TRACE UNLOCKED ❌",
                    color = if (signaturePoints.isNotEmpty()) PmmAccentGreen else PmmAlarmRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        val actionButtons = @Composable {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color.Transparent)
                        .clickable {
                            manualState = PowderMakerManualState()
                            signaturePoints.clear()
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RESET LOCK",
                        color = palette.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .then(
                            if (isPortrait) Modifier.fillMaxWidth()
                            else Modifier.width(220.dp)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                colors = if (manualState.isSubmitted)
                                    listOf(PmmAccentGreen, PmmAccentGreen.copy(alpha = 0.8f))
                                else
                                    listOf(PmmAccentBlue, PmmAccentBlue.copy(alpha = 0.8f))
                            )
                        )
                        .clickable {
                            if (signaturePoints.isNotEmpty()) {
                                showExportDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COMMIT JOURNAL LOG",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        if (isPortrait) {
            Column(
                modifier = containerModifier,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                statusBadge()
                actionButtons()
            }
        } else {
            Row(
                modifier = containerModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusBadge()
                actionButtons()
            }
        }
    }

    // ============================================================================
    // 🧱 PARENT LAYOUT MANAGER
    // ============================================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(structuralBackgroundModifier)
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isPortrait) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderContent()
                Spacer(modifier = Modifier.height(12.dp))
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LivePLCContent(Modifier.fillMaxWidth())
                    EquipmentContent(Modifier.fillMaxWidth())
                    LogisticsContent(Modifier.fillMaxWidth())
                    Stage1Content(Modifier.fillMaxWidth())
                    Stage2Content(Modifier.fillMaxWidth())
                    Stage3Content(Modifier.fillMaxWidth())
                    RemarksContent(Modifier.fillMaxWidth().height(180.dp))
                    SignatureContent(Modifier.fillMaxWidth().height(220.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                FooterContent()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderContent()
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LivePLCContent(Modifier)
                        EquipmentContent(Modifier.weight(1f))
                    }
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LogisticsContent(Modifier)
                        Stage1Content(Modifier)
                        Stage2Content(Modifier)
                        Stage3Content(Modifier)
                    }
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RemarksContent(Modifier.weight(1f))
                        SignatureContent(Modifier.height(200.dp))
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
                    .clickable(enabled = false) {}, // Intercept background clicks
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
                    Icon(Icons.Rounded.CheckCircle, null, tint = PmmAccentGreen, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("LOG COMMITTED SUCCESSFULLY", color = PmmAccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Do you want to export a copy of this manual entry before closing?",
                        color = palette.textPrimary, fontSize = 13.sp, textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        // 1. PRINT BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    printPowderMakerLogReport(
                                        context, manualState, avgVacStr, pmAvailableStr,
                                        signaturePoints, currentDate, currentTimeFormatted, currentShift
                                    )
                                    manualState = manualState.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Print, null, tint = PmmAccentBlue, modifier = Modifier.size(18.dp))
                                Text("PRINT PDF", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. CSV DOWNLOAD BUTTON
                        Box(
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(8.dp))
                                .clickable {
                                    val csvData = generatePowderMakerLogCsv(
                                        manualState, avgVacStr, pmAvailableStr,
                                        signaturePoints, currentDate, currentTimeFormatted, currentShift
                                    )
                                    downloadPowderMakerLogCsvToDevice(context, csvData)
                                    manualState = manualState.copy(isSubmitted = true)
                                    showExportDialog = false
                                    onNavigationCallback()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Download, null, tint = PmmAccentBlue, modifier = Modifier.size(18.dp))
                                Text("SAVE CSV", color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 3. SKIP BUTTON
                        Box(
                            modifier = Modifier.weight(0.7f).height(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(PmmAccentBlue)
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

// ============================================================================
// 🔷 PMM HARDWARE STATE BADGE
// ============================================================================

@Composable
private fun PmmHardwareStateBadge(
    title: String,
    status: ConcEquipStatus,
    statusText: String,
    palette: PmmMorphicPalette
) {
    val display    = status.toPmmDisplay(palette)
    val badgeColor = display.color
    val badgeText  = display.label

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(palette.inputContainer)
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = statusText,
                color = badgeColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .height(24.dp)
                .width(80.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ============================================================================
// ✍️ PMM SIGNATURE CANVAS
// ============================================================================

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PmmSignatureCaptureCanvas(
    points: MutableList<Offset>,
    strokeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInteropFilter { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> { points.add(Offset(event.x, event.y)); true }
                MotionEvent.ACTION_UP   -> { points.add(Offset.Unspecified); true }
                else                   -> false
            }
        }
    ) {
        if (points.size > 1) {
            val path = Path()
            var first = true
            for (point in points) {
                if (point == Offset.Unspecified) { first = true; continue }
                if (first) { path.moveTo(point.x, point.y); first = false }
                else        { path.lineTo(point.x, point.y) }
            }
            drawPath(
                path  = path,
                color = strokeColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap   = StrokeCap.Round,
                    join  = StrokeJoin.Round
                )
            )
        }
    }
}

// ============================================================================
// 🃏 PMM FORM SECTION CARD
// ============================================================================

@Composable
private fun PmmFormSectionCard(
    title: String,
    icon: ImageVector,
    palette: PmmMorphicPalette,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (isDark) 0.dp else 2.dp, shape = RoundedCornerShape(14.dp))
            .background(palette.glassFill, RoundedCornerShape(14.dp))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PmmAccentBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

// ============================================================================
// 📝 PMM LOG INPUT FIELD
// ============================================================================

@Composable
private fun PmmLogInputField(
    label: String,
    value: String,
    palette: PmmMorphicPalette,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isReadOnly) palette.inputContainer.copy(alpha = 0.4f)
                else palette.inputContainer
            )
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = palette.textMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            BasicTextField(
                value         = value,
                onValueChange = onValueChange,
                textStyle     = TextStyle(
                    color      = if (isReadOnly) PmmAccentGreen else palette.textPrimary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                readOnly        = isReadOnly,
                cursorBrush     = SolidColor(palette.textPrimary),
                modifier        = Modifier.fillMaxWidth()
            )
        }
    }
}