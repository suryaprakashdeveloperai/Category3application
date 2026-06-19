package com.example.category3.auth.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.category3.data.MaintenanceJobCard
import kotlin.math.cos
import kotlin.math.sin

// ============================================================================
// 🎨 LOCAL NEON THEME COLORS (Required for the Dark Dashboard look)
// ============================================================================
private val AccentCyan = Color(0xFF00E5FF)

@Composable
fun MillDiagnosticsHubScreen(
    uiState: DashboardUiState,
    jobs: List<MaintenanceJobCard>,
    currentTheme: MorphicThemeConfig, // Inherits global Purple Glass theme states
    onUpdateJobStatus: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedJobId by remember { mutableStateOf<String?>(jobs.firstOrNull()?.id) }

    val infiniteTransition = rememberInfiniteTransition(label = "millRotation")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "gearRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bgBase) // Deep purple background
            .padding(12.dp), // Dense full-screen mode padding
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ============================================================================
        // 📉 LEFT COLUMN BAY: Real-Time Instrumentation & Live Feeds (~35% Width)
        // ============================================================================
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BACK NAVIGATION GATEWAY BUTTON
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(12.dp))
                    .clickable { onNavigateBack() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                Text("Return to Main Console View", color = currentTheme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // LIVE MECHANICAL DIAGNOSTIC INSTRUMENTATION
            GlassPanelWrapper(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Crusher Unit Telemetry", color = currentTheme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Black)

                    // CUSTOM KINETIC ENGAGEMENT COMPONENT (Rotating Mechanical Gear Canvas)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 3f

                            // Emissive Center Glow
                            drawCircle(
                                brush = Brush.radialGradient(listOf(AccentCyan.copy(alpha = 0.3f), Color.Transparent), center = center, radius = radius * 1.5f),
                                radius = radius * 1.5f
                            )

                            // Core Ring Rim
                            drawCircle(color = currentTheme.textMain.copy(alpha = 0.2f), radius = radius, style = Stroke(width = 3.dp.toPx()))
                            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = radius - 4.dp.toPx())

                            // Dynamic teeth mapping synchronized to rotation timeline parameter
                            val teethCount = 12
                            for (i in 0 until teethCount) {
                                val angleDeg = (i * (360f / teethCount)) + gearRotation
                                val angleRad = Math.toRadians(angleDeg.toDouble())
                                val startX = center.x + (radius - 2.dp.toPx()) * cos(angleRad).toFloat()
                                val startY = center.y + (radius - 2.dp.toPx()) * sin(angleRad).toFloat()
                                val endX = center.x + (radius + 8.dp.toPx()) * cos(angleRad).toFloat()
                                val endY = center.y + (radius + 8.dp.toPx()) * sin(angleRad).toFloat()

                                drawLine(color = AccentCyan, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                            }
                            drawCircle(color = AccentCyan, radius = 6.dp.toPx(), center = center)
                        }
                    }
                }
            }
        }

        // ============================================================================
        // 📋 RIGHT COLUMN BAY: Interactive Maintenance Ticket File Desk (~65% Width)
        // ============================================================================
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // HEADER BAR TITLE
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).background(Color(0x3300E5FF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Assignment, null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Mill Interactive Maintenance Terminal", color = currentTheme.textMain, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            // PRIMARY SPLIT SYSTEM INTERFACE LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TICKET SELECTION LIST ARRAY STACK
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    jobs.forEach { job ->
                        val isSelected = selectedJobId == job.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Color(0x66FFFFFF) else Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedJobId = job.id }
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = job.id, color = AccentCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (job.initialStatus) {
                                                    "PENDING" -> AccentOrange.copy(alpha = 0.15f)
                                                    "IN_PROGRESS" -> AccentPurple.copy(alpha = 0.15f)
                                                    else -> AccentGreen.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = job.initialStatus.replace("_", " "),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (job.initialStatus) {
                                                "PENDING" -> AccentOrange
                                                "IN_PROGRESS" -> AccentPurple
                                                else -> AccentGreen
                                            }
                                        )
                                    }
                                }
                                Text(text = job.title, color = currentTheme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Layers, null, tint = currentTheme.textMuted, modifier = Modifier.size(12.dp))
                                    Text(text = "Asset Target Location: ${job.assetTag}", color = currentTheme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // CHOSEN TASK CARD DIRECTIVE DISPLAY INSPECTOR DESK Viewport
                Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                    val activeJobSelection = jobs.find { it.id == selectedJobId }
                    if (activeJobSelection != null) {
                        GlassPanelWrapper(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Task Sheet: ${activeJobSelection.id}", color = currentTheme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Rounded.ChevronRight, null, tint = currentTheme.textMuted, modifier = Modifier.size(20.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("TASK DIRECTIVE BLUEPRINT DETAILS", color = currentTheme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = activeJobSelection.instructions, color = currentTheme.textMain, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("CRITICAL SCALE", color = currentTheme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = activeJobSelection.criticalLevel,
                                            color = if (activeJobSelection.criticalLevel == "CRITICAL") AccentRed else AccentOrange,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TAG SERIAL ID", color = currentTheme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = activeJobSelection.assetTag, color = currentTheme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                when (activeJobSelection.initialStatus) {
                                    "PENDING" -> {
                                        Button(
                                            onClick = { onUpdateJobStatus(activeJobSelection.id, "IN_PROGRESS") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("Acknowledge & Clock In", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    "IN_PROGRESS" -> {
                                        Button(
                                            onClick = { onUpdateJobStatus(activeJobSelection.id, "COMPLETED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("Clear Blueprint Fault Logs", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    else -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(16.dp)
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Cleared by Operator Badge ID", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Select an operational card token to parse job instructions.", color = currentTheme.textMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassPanelWrapper(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x66000000), ambientColor = Color.Black)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF)), start = Offset.Zero, end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
            .border(width = 1.dp, color = Color(0x4DFFFFFF), shape = RoundedCornerShape(16.dp)),
        content = content
    )
}