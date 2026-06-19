package com.example.category3.auth.ui

import android.view.MotionEvent
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
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Scale
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
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

private data class OpenPanState(
    val batchNo: String = "OP-2026-B9",
    val openPanNo: String = "PAN-02",
    val steamPressure: String = "2.1",
    val startTime: String = "08:30",
    val endTime: String = "09:45",
    val duration: String = "75 Min",
    val coconutOilMl: String = "150",
    val sodaAdditionGrams: String = "50",
    val jaggeryQtyKg: String = "450",
    val remarks: String = "",
    val isSubmitted: Boolean = false
)

private data class OpenPanMorphicPalette(
    val baseChassis: Color, val glassFill: Color, val glassBorder: Color,
    val inputContainer: Color, val textPrimary: Color, val textMuted: Color,
    val dividerLine: Color, val inputBorderUnfocused: Color
)

@Composable
private fun getOpenPanDynamicMorphicPalette(isDark: Boolean): OpenPanMorphicPalette {
    return if (isDark) {
        OpenPanMorphicPalette(
            baseChassis = Color(0xFF0A0C14), glassFill = Color(0x13FFFFFF), glassBorder = Color(0x26FFFFFF),
            inputContainer = Color(0x1A000000), textPrimary = Color(0xFFF0F6FC), textMuted = Color(0xFF8B949E),
            dividerLine = Color(0xFF30363D), inputBorderUnfocused = Color(0xFF30363D)
        )
    } else {
        OpenPanMorphicPalette(
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
fun OpenPanScreen(
    onRaiseTicket: (String) -> Unit,
    onNavigationCallback: () -> Unit
) {
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var state by remember { mutableStateOf(OpenPanState()) }
    val signaturePoints = remember { mutableStateListOf<Offset>() }

    val speechTranslator = remember(context) { MorphicSpeechTranslator(context) }
    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }
    var currentVoiceStatusText by remember { mutableStateOf("") }

    var isDarkThemeOverride by remember { mutableStateOf(false) }
    val palette = getOpenPanDynamicMorphicPalette(isDark = isDarkThemeOverride)

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            if (!state.isSubmitted) {
                state = state.copy(
                    steamPressure = String.format("%.2f", 2.10 + Random.nextDouble(-0.08, 0.08))
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
                    Text("OPEN PAN CONDUIT MATRIX [RECIPE EVAPORATION TERMINAL]", color = palette.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
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
            // FIXED TWO-COLUMN WORKSPACE (No Scroll Weights Enabled)
            // ============================================================================
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 🛑 LEFT SIDE COLUMN: TIMELINES, STEAM TELEMETRY & RELOCATED RAW MATERIALS
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Cooking Batch Identifiers & Timers
                    OpenPanFormSectionCard(title = "BATCH SCHEDULING TIMELINES", icon = Icons.Rounded.Speed, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OpenPanLogInputField("BATCH NUMBER", state.batchNo, palette, Modifier.weight(1f)) { state = state.copy(batchNo = it, isSubmitted = false) }
                                OpenPanLogInputField("OPEN PAN UNIT NO", state.openPanNo, palette, Modifier.weight(1f)) { state = state.copy(openPanNo = it, isSubmitted = false) }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OpenPanLogInputField("START TIME", state.startTime, palette, Modifier.weight(1f)) { state = state.copy(startTime = it, isSubmitted = false) }
                                OpenPanLogInputField("END TIME", state.endTime, palette, Modifier.weight(1f)) { state = state.copy(endTime = it, isSubmitted = false) }
                            }
                            OpenPanLogInputField("TOTAL PROCESSING DURATION", state.duration, palette, Modifier.fillMaxWidth()) { state = state.copy(duration = it, isSubmitted = false) }
                        }
                    }

                    // Card 2: Steam Monitoring Matrix
                    OpenPanFormSectionCard(title = "THERMAL EVAPORATION ENERGY LINE", icon = Icons.Rounded.Analytics, palette = palette, isDark = isDarkThemeOverride) {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            OpenPanLogInputField("STEAM PRESSURE (KG/CM²)", state.steamPressure, palette, Modifier.fillMaxWidth()) { state = state.copy(steamPressure = it, isSubmitted = false) }
                        }
                    }

                    // 🚀 RELOCATED PLACE: Card 3 now sits cleanly below Thermal Evaporation Energy Line card
                    OpenPanFormSectionCard(title = "RAW MATERIAL IMPRINT ADDITIONS", icon = Icons.Rounded.Scale, palette = palette, isDark = isDarkThemeOverride) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OpenPanLogInputField("COCONUT OIL ADDED (ML)", state.coconutOilMl, palette, Modifier.weight(1f)) { state = state.copy(coconutOilMl = it, isSubmitted = false) }
                                OpenPanLogInputField("SODA ADDITION (GRAMS)", state.sodaAdditionGrams, palette, Modifier.weight(1f)) { state = state.copy(sodaAdditionGrams = it, isSubmitted = false) }
                            }
                            HorizontalDivider(color = palette.dividerLine, thickness = 1.dp)
                            OpenPanLogInputField("TOTAL JAGGERY QUANTITY CHARGED (KG)", state.jaggeryQtyKg, palette, Modifier.fillMaxWidth()) { state = state.copy(jaggeryQtyKg = it, isSubmitted = false) }
                        }
                    }
                }

                // 🛑 RIGHT SIDE COLUMN: ANNOTATIONS & DIGITAL ESIGN
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 4: Audio Log Annotations (Height Balanced)
                    OpenPanFormSectionCard(title = "OPERATOR RECORDING LOG ANNOTATIONS", icon = Icons.Rounded.Notes, palette = palette, isDark = isDarkThemeOverride) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
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
                                            text = "INPUT LIVE CRITICAL PROCESS SHIFT ANOMALIES...",
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
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TechAccentBlue, strokeWidth = 2.dp)
                                } else {
                                    IconButton(
                                        modifier = Modifier.size(26.dp),
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
                                                    (context as? android.app.Activity)?.requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
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

                    // Card 5: Touch Signature Verification Pad (Height Balanced)
                    OpenPanFormSectionCard(title = "SUPERVISOR SIGNATURE IDENTITY GATEWAY", icon = Icons.Rounded.Draw, palette = palette, isDark = isDarkThemeOverride) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("DRAW RECOGNIZED SIGNATURE ON CANVAS:", color = palette.textPrimary, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "RESET TRACE", color = TechAlarmRed, fontSize = 10.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { signaturePoints.clear() }.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                                    .background(palette.glassFill, RoundedCornerShape(8.dp))
                                    .border(1.dp, palette.glassBorder, RoundedCornerShape(8.dp))
                            ) {
                                if (signaturePoints.isEmpty()) {
                                    Text(text = "TOUCH RECOGNITION BOUNDARY MATRIX ACTIVE...", color = palette.textMuted.copy(alpha = 0.5f), fontSize = 11.sp, fontFamily = FontTelemetryMono, modifier = Modifier.align(Alignment.Center))
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
                        text = if (signaturePoints.isNotEmpty()) "ESIGN LOCK SIGNED ✔" else "ESIGN AUTHENTICATION TRACE UNLOCKED ❌",
                        color = if (signaturePoints.isNotEmpty()) TechAccentGreen else TechAlarmRed, fontSize = 11.sp, fontFamily = FontTelemetryMono, fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.height(36.dp).background(Color.Transparent)
                            .clickable { state = OpenPanState(); signaturePoints.clear() }.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("RESET LOCK", color = palette.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontTelemetryMono)
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp).width(220.dp).clip(RoundedCornerShape(50))
                            .background(Brush.linearGradient(colors = if (state.isSubmitted) listOf(TechAccentGreen, TechAccentGreen.copy(alpha = 0.8f)) else listOf(TechAccentBlue, TechAccentBlue.copy(alpha = 0.8f))))
                            .clickable { if (signaturePoints.isNotEmpty()) { state = state.copy(isSubmitted = true); onNavigationCallback() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("COMMIT JOURNAL LOG", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontTelemetryMono)
                    }
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
private fun OpenPanFormSectionCard(
    title: String, icon: ImageVector, palette: OpenPanMorphicPalette, isDark: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit
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

@Composable
private fun OpenPanLogInputField(
    label: String, value: String, palette: OpenPanMorphicPalette, modifier: Modifier = Modifier, onValueChange: (String) -> Unit
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