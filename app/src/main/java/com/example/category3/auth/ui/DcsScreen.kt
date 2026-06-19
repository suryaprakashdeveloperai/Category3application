package com.example.category3.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.MotionEvent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CameraAlt
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.category3.utils.MorphicSpeechTranslator
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class DcsProcessState(
    // 1. Metric Matrix Inputs (Automated PLC Sensors)
    val rawJuiceFlow: String = "119.0",
    val clearJuiceYield: String = "84.8",
    val defecatedJuicePh: String = "7.2",
    val dchTemperature: String = "105.3",

    // 2. Totalizer Nodes (Manual Shift Entries)
    val rjInitial: String = "", val rjFinal: String = "", val rjTotal: String = "0.00",
    val cjInitial: String = "", val cjFinal: String = "", val cjTotal: String = "0.00",
    val flocInitial: String = "", val flocFinal: String = "", val flocTotal: String = "0.00",

    // 3. Hardware Interlock Statuses (Driven via PLC)
    val pump1Status: Boolean = true,
    val pump2Status: Boolean = false,
    val vibroscreenStatus: Boolean = true,
    val rotaryRunning: Boolean = false,

    val operatorRemarks: String = "",
    val isSubmitted: Boolean = false
)

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcsScreen(
    onNavigationCallback: () -> Unit
) {
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var state by remember { mutableStateOf(DcsProcessState()) }

    val signaturePoints = remember { mutableStateListOf<Offset>() }
    val speechTranslator = remember(context) { MorphicSpeechTranslator(context) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getDcsPalette(isDark = isDarkThemeOverride)

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

    // ============================================================================
    // 📡 PLC INTERFACE LIVE DATA FEED
    // ============================================================================
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            if (!state.isSubmitted) {
                state = state.copy(
                    rawJuiceFlow = String.format("%.1f", 119.0 + Random.nextDouble(-1.0, 1.0)),
                    clearJuiceYield = String.format("%.1f", 84.8 + Random.nextDouble(-0.3, 0.3)),
                    defecatedJuicePh = String.format("%.1f", 7.2 + Random.nextDouble(-0.04, 0.04)),
                    dchTemperature = String.format("%.1f", 105.3 + Random.nextDouble(-0.3, 0.3))
                )
            }
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

    val structuralBackgroundModifier = if (isDarkThemeOverride) {
        Modifier.background(Brush.radialGradient(colors = listOf(Color(0xFF1E1B4B), Color(0xFF090A10)), radius = 2200f))
    } else {
        Modifier.background(palette.baseChassis)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(structuralBackgroundModifier)
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DISTRIBUTED CONTROL COCKPIT [PROCESS FLOW MATRIX CONSOLE]", color = palette.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
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
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TechAccentBlue),
                            modifier = Modifier.graphicsLayer(scaleX = 0.65f, scaleY = 0.65f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("DATE: $currentDate", fontFamily = FontTelemetryMono, fontSize = 11.sp, color = palette.textMuted, fontWeight = FontWeight.SemiBold)
                    Text("TIME: ${currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} | $currentShift", fontFamily = FontTelemetryMono, fontSize = 11.sp, color = TechWarnOrange, fontWeight = FontWeight.Bold)
                }
            }

            // ============================================================================
            // DATA ENTRY WORKSPACE LAYER
            // ============================================================================
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // 🛑 LEFT COLUMN: METRIC MATRIX & STATUS BADGES
                Column(
                    modifier = Modifier.weight(1.0f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Process metrics from hardware sensors
                    DcsFormSectionCard(title = "PROCESS STREAM METRIC MATRIX [PLC LIVE LINK]", icon = Icons.Rounded.Speed, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DcsLogInputField("RAW JUICE FLOW (M³/H)", state.rawJuiceFlow, palette, Modifier.weight(1f), isReadOnly = true) {}
                                DcsLogInputField("CLEAR JUICE YIELD (%)", state.clearJuiceYield, palette, Modifier.weight(1f), isReadOnly = true) {}
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DcsLogInputField("DEFECATED JUICE (pH)", state.defecatedJuicePh, palette, Modifier.weight(1f), isReadOnly = true) {}
                                DcsLogInputField("DCH TOWER TEMP (°C)", state.dchTemperature, palette, Modifier.weight(1f), isReadOnly = true) {}
                            }
                        }
                    }

                    // Critical Hardware Badges
                    DcsFormSectionCard(title = "CRITICAL HARDWARE STATUSES [PLC MONITOR]", icon = Icons.Rounded.Settings, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.weight(1.1f)) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
                            HardwareStateBadge("JUICE TRANS PUMP 01", state.pump1Status, palette)
                            HardwareStateBadge("JUICE TRANS PUMP 02", state.pump2Status, palette)
                            HardwareStateBadge("VIBROSCREEN CLUSTER DRIVES", state.vibroscreenStatus, palette)
                            HardwareStateBadge("ROTARY SCREEN TRANSMISSION", state.rotaryRunning, palette)
                        }
                    }
                }

                // 🛑 CENTER COLUMN: MANUAL TOTALIZER PIPELINE LOGS
                Column(
                    modifier = Modifier.weight(1.1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    DcsFormSectionCard(title = "FACTORY PIPELINE TOTALIZER NODES", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            // A. Raw Juice Flow Block
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("A. RAW JUICE FLOW BLOCK (M³)", color = TechAccentBlue, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DcsLogInputField("INITIAL", state.rjInitial, palette, Modifier.weight(1f)) { state = state.copy(rjInitial = it, rjTotal = autoComputeTotal(it, state.rjFinal), isSubmitted = false) }
                                    DcsLogInputField("FINAL", state.rjFinal, palette, Modifier.weight(1f)) { state = state.copy(rjFinal = it, rjTotal = autoComputeTotal(state.rjInitial, it), isSubmitted = false) }
                                    DcsLogInputField("NET TOTAL", state.rjTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                                }
                            }
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // B. Clear Juice Block
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("B. CLEAR JUICE METRIC BLOCK (M³)", color = TechAccentGreen, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DcsLogInputField("INITIAL", state.cjInitial, palette, Modifier.weight(1f)) { state = state.copy(cjInitial = it, cjTotal = autoComputeTotal(it, state.cjFinal), isSubmitted = false) }
                                    DcsLogInputField("FINAL", state.cjFinal, palette, Modifier.weight(1f)) { state = state.copy(cjFinal = it, cjTotal = autoComputeTotal(state.cjInitial, it), isSubmitted = false) }
                                    DcsLogInputField("NET TOTAL", state.cjTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                                }
                            }
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // C. Flocculant Feed Block
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("C. FLOCCULANT FEED PIPELINE (KG)", color = TechWarnOrange, fontSize = 12.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DcsLogInputField("INITIAL", state.flocInitial, palette, Modifier.weight(1f)) { state = state.copy(flocInitial = it, flocTotal = autoComputeTotal(it, state.flocFinal), isSubmitted = false) }
                                    DcsLogInputField("FINAL", state.flocFinal, palette, Modifier.weight(1f)) { state = state.copy(flocFinal = it, flocTotal = autoComputeTotal(state.flocInitial, it), isSubmitted = false) }
                                    DcsLogInputField("NET TOTAL", state.flocTotal, palette, Modifier.weight(1f), isReadOnly = true) {}
                                }
                            }
                        }
                    }
                }

                // 🛑 RIGHT COLUMN: CAMERA SENSORY, SPEECH INPUTS & SIGNATURE
                Column(
                    modifier = Modifier.weight(1.0f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Camera Window
                    DcsFormSectionCard(title = "SENSORY PROOF VALIDATION WINDOW", icon = Icons.Rounded.Camera, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.weight(0.6f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().fillMaxHeight().clip(RoundedCornerShape(6.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Consist Verification Shutter", fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = if (capturedImage != null) "IMAGE VERIFIED ✔" else "Secure visual totalizer log pass.", fontFamily = FontTelemetryMono, color = if (capturedImage != null) TechAccentGreen else palette.textMuted, fontSize = 10.sp)
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
                                            Text("OPEN PREVIEW", color = TechAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Morphic Speech Translator
                    DcsFormSectionCard(title = "DCS ANNOTATION AUDIO LOG", icon = Icons.Rounded.Notes, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.weight(0.9f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().fillMaxHeight().clip(RoundedCornerShape(6.dp))
                                .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            BasicTextField(
                                value = state.operatorRemarks, onValueChange = { state = state.copy(operatorRemarks = it, isSubmitted = false) },
                                textStyle = TextStyle(fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(palette.textPrimary), modifier = Modifier.fillMaxSize().padding(end = 36.dp),
                                decorationBox = { innerTextField ->
                                    if (state.operatorRemarks.isEmpty()) {
                                        Text(text = "TAP MIC KEY AND STATE VALVE ANOMALIES...", color = if (isListening) TechAlarmRed else palette.textMuted, fontSize = 11.sp, fontFamily = FontTelemetryMono)
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
                                                        onResultReceived = { state = state.copy(operatorRemarks = it, isSubmitted = false) }
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

                    // Signature Matrix
                    DcsFormSectionCard(title = "OPERATOR SIGNATURE CONTROL WINDOW", icon = Icons.Rounded.Draw, palette = palette, isDark = isDarkThemeOverride, modifier = Modifier.weight(1.1f)) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("DRAW VERIFICATION INTEGRITY TRACE:", color = palette.textPrimary, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "WIPE PANEL", color = TechAlarmRed, fontSize = 10.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black,
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
                                    Text(text = "TOUCH AUTHENTICATOR MATRIX ON...", color = palette.textMuted.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontTelemetryMono, modifier = Modifier.align(Alignment.Center))
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
                                        drawPath(path = path, color = if (isDarkThemeOverride) TechAccentBlue else MinimalistTextPrimary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ============================================================================
            // FOOTER ACTION COMMIT BAR
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
                        .height(34.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (signaturePoints.isNotEmpty()) TechAccentGreen.copy(alpha = 0.12f) else TechAlarmRed.copy(alpha = 0.08f))
                        .border(1.dp, if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (signaturePoints.isNotEmpty()) "DCS JOURNAL INTERLOCKS VERIFIED ✔" else "ESIGN JOURNAL DISPATCH SECURITY REQUIRED ❌",
                        color = if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .height(38.dp).width(220.dp).clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(colors = if (state.isSubmitted) listOf(TechAccentGreen, TechAccentGreen.copy(alpha = 0.8f)) else listOf(TechAccentBlue, TechAccentBlue.copy(alpha = 0.8f))))
                        .clickable {
                            if (signaturePoints.isNotEmpty()) {
                                state = state.copy(isSubmitted = true)
                                onNavigationCallback()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("COMMIT DATA TRANSFERS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                }
            }
        }
    }
}

@Composable
private fun HardwareStateBadge(
    title: String,
    isRunning: Boolean,
    palette: DcsMorphicPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(palette.inputContainer)
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = palette.textPrimary, fontFamily = FontTelemetryMono, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .height(26.dp)
                .width(90.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isRunning) TechAccentGreen.copy(alpha = 0.15f) else TechAlarmRed.copy(alpha = 0.1f))
                .border(1.dp, if (isRunning) TechAccentGreen else TechAlarmRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRunning) "RUNNING" else "STOPPED",
                color = if (isRunning) TechAccentGreen else TechAlarmRed,
                fontFamily = FontTelemetryMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun DcsLogInputField(
    label: String, value: String, palette: DcsMorphicPalette, modifier: Modifier = Modifier, isReadOnly: Boolean = false, onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp).clip(RoundedCornerShape(6.dp))
            .background(if (isReadOnly) palette.inputContainer.copy(alpha = 0.4f) else palette.inputContainer)
            .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = label, color = palette.textMuted, fontSize = 9.5.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(1.dp))
            BasicTextField(
                value = value, onValueChange = onValueChange,
                textStyle = TextStyle(fontFamily = FontTelemetryMono, color = if (isReadOnly) TechAccentGreen else palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                readOnly = isReadOnly,
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (isDark) 0.dp else 2.dp, shape = RoundedCornerShape(14.dp))
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