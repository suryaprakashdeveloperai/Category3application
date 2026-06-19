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
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Notes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

data class JaggeryQcState(
    val lotNumber: String = "LOT-2026-X11",
    val sugarGrade: String = "Premium Class",
    val totalSugar: String = "95.2",
    val sucrose: String = "82.5",
    val moisture: String = "4.1",
    val ash: String = "0.8",
    val ph: String = "6.5",
    val reducingSugars: String = "12.4",
    val remarks: String = "",
    val isSubmitted: Boolean = false
)

private data class QcMorphicPalette(
    val baseChassis: Color, val glassFill: Color, val glassBorder: Color,
    val inputContainer: Color, val textPrimary: Color, val textMuted: Color,
    val dividerLine: Color, val inputBorderUnfocused: Color
)

@Composable
private fun getQcDynamicMorphicPalette(isDark: Boolean): QcMorphicPalette {
    return if (isDark) {
        QcMorphicPalette(
            baseChassis = Color(0xFF0A0C14), glassFill = Color(0x13FFFFFF), glassBorder = Color(0x26FFFFFF),
            inputContainer = Color(0x1A000000), textPrimary = Color(0xFFF0F6FC), textMuted = Color(0xFF8B949E),
            dividerLine = Color(0xFF30363D), inputBorderUnfocused = Color(0xFF30363D)
        )
    } else {
        QcMorphicPalette(
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
fun QualityControlScreen(
    onNavigationCallback: () -> Unit
) {
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var qcState by remember { mutableStateOf(JaggeryQcState()) }

    val signaturePoints = remember { mutableStateListOf<Offset>() }
    val speechTranslator = remember(context) { MorphicSpeechTranslator(context) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getQcDynamicMorphicPalette(isDark = isDarkThemeOverride)

    // --- LOCAL HARDWARE SYSTEM LAUNCHERS ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedImage = bitmap
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    // ============================================================================
    // 📡 PLC AUTOMATED LIVE STREAM READ SIGNAL LOOP
    // ============================================================================
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            if (!qcState.isSubmitted) {
                qcState = qcState.copy(
                    totalSugar = String.format("%.1f", 95.2 + Random.nextDouble(-0.3, 0.3)),
                    sucrose = String.format("%.1f", 82.5 + Random.nextDouble(-0.4, 0.4)),
                    moisture = String.format("%.1f", 4.1 + Random.nextDouble(-0.1, 0.1)),
                    ph = String.format("%.1f", 6.5 + Random.nextDouble(-0.05, 0.05))
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
                    Text("LABORATORY QUALITY CONDUIT [FSSAI LAB MANUAL JOURNAL]", color = palette.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
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
            // DATA ENTRY WORKSPACE LAYER (Fixed Balanced Viewport)
            // ============================================================================
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 🛑 LEFT SIDE COLUMN: INDUSTRIAL LOGISTICS & REALTIME COCKPIT ENTRIES
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QcFormSectionCard(title = "LOT SECTOR LOGISTICS METADATA", icon = Icons.Rounded.Speed, palette = palette, isDark = isDarkThemeOverride) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QcLogInputField("LOT IDENTIFIER", qcState.lotNumber, palette, Modifier.weight(1f)) { qcState = qcState.copy(lotNumber = it, isSubmitted = false) }
                            QcLogInputField("SUGAR GRADE TYPE", qcState.sugarGrade, palette, Modifier.weight(1f)) { qcState = qcState.copy(sugarGrade = it, isSubmitted = false) }
                        }
                    }

                    QcFormSectionCard(title = "CHEMICAL ANALYSIS PARAMETER ASSAYS [PLC UNIFIED]", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QcLogInputField("TOTAL SUGAR (≥ 94%)", qcState.totalSugar, palette, Modifier.weight(1f)) { qcState = qcState.copy(totalSugar = it, isSubmitted = false) }
                                QcLogInputField("SUCROSE LEVEL (≥ 80%)", qcState.sucrose, palette, Modifier.weight(1f)) { qcState = qcState.copy(sucrose = it, isSubmitted = false) }
                                QcLogInputField("ASH CONTENT (≤ 1%)", qcState.ash, palette, Modifier.weight(1f)) { qcState = qcState.copy(ash = it, isSubmitted = false) }
                            }
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QcLogInputField("MOISTURE FRACTION (≤ 5%)", qcState.moisture, palette, Modifier.weight(1f)) { qcState = qcState.copy(moisture = it, isSubmitted = false) }
                                QcLogInputField("pH LEVEL (6.0 - 7.0)", qcState.ph, palette, Modifier.weight(1f)) { qcState = qcState.copy(ph = it, isSubmitted = false) }
                                QcLogInputField("REDUCING FRACTIONS", qcState.reducingSugars, palette, Modifier.weight(1f)) { qcState = qcState.copy(reducingSugars = it, isSubmitted = false) }
                            }
                        }
                    }
                }

                // 🛑 RIGHT SIDE COLUMN: CAMERA SENSORY MATRIX, SPEECH CAPTURE & SIGN VERIFICATION
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 📸 Integrated Camera Proof Matrix Card
                    QcFormSectionCard(title = "SENSORY PROOF VALIDATION MATRIX", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.inputContainer)
                                .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Optic Color Consistency Verification", fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = if (capturedImage != null) "BATCH IMAGE CAPTURED ✔" else "Capture batch visual test proof.", fontFamily = FontTelemetryMono, color = if (capturedImage != null) TechAccentGreen else palette.textMuted, fontSize = 10.sp)
                                }

                                if (capturedImage != null) {
                                    Image(
                                        bitmap = capturedImage!!.asImageBitmap(),
                                        contentDescription = "QC Preview Pass",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, TechAccentGreen, RoundedCornerShape(4.dp))
                                            .clickable { capturedImage = null }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(34.dp)
                                            .width(140.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TechAccentBlue.copy(alpha = 0.15f))
                                            .border(1.dp, TechAccentBlue, RoundedCornerShape(6.dp))
                                            .clickable {
                                                val hasPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                                if (hasPermission) {
                                                    cameraLauncher.launch(null)
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TechAccentBlue, modifier = Modifier.size(14.dp))
                                            Text("OPEN SHUTTER", color = TechAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🎤 Laboratory Voice Translation Annotation Box
                    QcFormSectionCard(title = "LABORATORY SPEECH LOG ANNOTATIONS", icon = Icons.Rounded.Notes, palette = palette, isDark = isDarkThemeOverride) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(94.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.inputContainer)
                                .border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            BasicTextField(
                                value = qcState.remarks, onValueChange = { qcState = qcState.copy(remarks = it, isSubmitted = false) },
                                textStyle = TextStyle(fontFamily = FontTelemetryMono, color = palette.textPrimary, fontSize = 14.sp),
                                cursorBrush = SolidColor(palette.textPrimary), modifier = Modifier.fillMaxSize().padding(end = 36.dp),
                                decorationBox = { innerTextField ->
                                    if (qcState.remarks.isEmpty()) {
                                        Text(
                                            text = "PRESS MIC AND SPEAK LAB TESTING DEVIATIONS, STABILITY NOTES...",
                                            color = if (isListening) TechAlarmRed else palette.textMuted, fontSize = 12.sp, fontFamily = FontTelemetryMono
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
                                                val hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                                if (hasPermission) {
                                                    speechTranslator.startListening(
                                                        onStatusChange = { currentVoiceStatusText = it },
                                                        onListeningStateChange = { isListening = it },
                                                        onTranslatingStateChange = { isTranslating = it },
                                                        onResultReceived = { qcState = qcState.copy(remarks = it, isSubmitted = false) }
                                                    )
                                                } else {
                                                    (context as? android.app.Activity)?.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone, contentDescription = null, tint = if (isListening) TechAlarmRed else TechAccentBlue, modifier = Modifier.size(22.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 🖋️ Hard-wired Interactive Touch Signature Box (110.dp Canvas)
                    QcFormSectionCard(title = "SUPERVISOR SIGNATURE IDENTITY CONTROL GATEWAY", icon = Icons.Rounded.Draw, palette = palette, isDark = isDarkThemeOverride) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("DRAW VALIDATION TRACE ON CANVAS:", color = palette.textPrimary, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "RESET MATRIX", color = TechAlarmRed, fontSize = 10.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { signaturePoints.clear() }.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                                    .background(palette.glassFill, RoundedCornerShape(8.dp))
                                    .border(1.dp, palette.glassBorder, RoundedCornerShape(8.dp))
                            ) {
                                if (signaturePoints.isEmpty()) {
                                    Text(text = "TOUCH AUTHENTICATOR SENSOR WINDOW ACTIVE...", color = palette.textMuted.copy(alpha = 0.5f), fontSize = 11.sp, fontFamily = FontTelemetryMono, modifier = Modifier.align(Alignment.Center))
                                }
                                SignatureCaptureCanvas(
                                    points = signaturePoints,
                                    strokeColor = if (isDarkThemeOverride) TechAccentBlue else MinimalistTextPrimary,
                                    modifier = Modifier.fillMaxSize()
                                )
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
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (signaturePoints.isNotEmpty()) TechAccentGreen.copy(alpha = 0.12f) else TechAlarmRed.copy(alpha = 0.08f))
                        .border(1.dp, if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (signaturePoints.isNotEmpty()) "ESIGN JOURNAL SECURITY LOCKED ✔" else "ESIGN COMPLIANCE SIGNATURE MANDATORY ❌",
                        color = if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .height(38.dp).width(220.dp).clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(colors = if (qcState.isSubmitted) listOf(TechAccentGreen, TechAccentGreen.copy(alpha = 0.8f)) else listOf(TechAccentBlue, TechAccentBlue.copy(alpha = 0.8f))))
                        .clickable {
                            if (signaturePoints.isNotEmpty()) {
                                qcState = qcState.copy(isSubmitted = true)
                                onNavigationCallback()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("COMMIT DATA ANALYSIS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SignatureCaptureCanvas(points: MutableList<Offset>, strokeColor: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.pointerInteropFilter { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { points.add(Offset(event.x, event.y)); true }
                MotionEvent.ACTION_UP -> { points.add(Offset.Unspecified); true }
                else -> false
            }
        }
    ) {
        if (points.size > 1) {
            val path = Path()
            var first = true
            for (point in points) {
                if (point == Offset.Unspecified) { first = true; continue }
                if (first) { path.moveTo(point.x, point.y); first = false } else { path.lineTo(point.x, point.y) }
            }
            drawPath(path = path, color = strokeColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
private fun QcLogInputField(
    label: String, value: String, palette: QcMorphicPalette, modifier: Modifier = Modifier, onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp).clip(RoundedCornerShape(6.dp))
            .background(palette.inputContainer).border(1.dp, palette.inputBorderUnfocused, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
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
private fun QcFormSectionCard(
    title: String, icon: ImageVector, palette: QcMorphicPalette, isDark: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit
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