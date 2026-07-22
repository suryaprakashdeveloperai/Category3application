package com.example.category3.auth.ui

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// 🎨 UI EXTENSIONS
// ============================================================================
object MillDiagColors {
    val AppBg = Color(0xFFE4E9F0)
    val AccentOrange = Color(0xFFF97316)
    val AccentOrangeLight = Color(0xFFFFEBE6)
    val TextDark = Color(0xFF2C3A4B)
    val TextGray = Color(0xFF67778A)
    val StatusGreen = Color(0xFF26C281)
    val StatusBlue = Color(0xFF47A1F2)
    val StatusRed = Color(0xFFFF4D4D)
    val BorderGray = Color(0xFFC5D1DF)
}

fun MillDiagEquipStatus.toColor(): Color = when (this) {
    MillDiagEquipStatus.RUNNING, MillDiagEquipStatus.HEALTHY -> MillDiagColors.StatusGreen
    MillDiagEquipStatus.STANDBY -> MillDiagColors.StatusBlue
    MillDiagEquipStatus.FAULT -> MillDiagColors.StatusRed
}

fun MillDiagKpiType.toIcon(): ImageVector = when (this) {
    MillDiagKpiType.VIBRATION -> Icons.Outlined.Waves
    MillDiagKpiType.TEMPERATURE -> Icons.Outlined.Thermostat
    MillDiagKpiType.CURRENT -> Icons.Outlined.Bolt
    MillDiagKpiType.POWER -> Icons.Outlined.Power
    MillDiagKpiType.SPEED -> Icons.Outlined.Speed
}

fun MillDiagKpiType.toColor(): Color = when (this) {
    MillDiagKpiType.VIBRATION -> Color(0xFFA120FF)
    MillDiagKpiType.TEMPERATURE -> MillDiagColors.StatusRed
    MillDiagKpiType.CURRENT -> MillDiagColors.StatusBlue
    MillDiagKpiType.POWER -> MillDiagColors.StatusGreen
    MillDiagKpiType.SPEED -> MillDiagColors.AccentOrange
}

// ============================================================================
// 🚀 STATEFUL ROUTE / UI
// ============================================================================

@Composable
fun MillDiagnosticsScreenContainer(
    onNavigateToScreen: (String) -> Unit = {},
    viewModel: MillDiagnosticsViewModel = viewModel(factory = MillDiagnosticsViewModel.provideFactory())
) {
    val liveState by viewModel.state.collectAsState()
    MillDiagnosticsHubScreen(state = liveState, onNavigateToScreen = onNavigateToScreen)
}

@Composable
fun MillGreyFrostGlassCard(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(24.dp), content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .shadow(12.dp, shape, spotColor = Color(0xFF8A9AAB).copy(0.5f), ambientColor = Color(0xFF8A9AAB).copy(0.2f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color.White.copy(0.65f), Color(0xFFC9D4E2).copy(0.4f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.9f), Color(0xFFA5B4C7).copy(0.3f)), Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)), shape),
        content = content
    )
}

@Composable
fun MillDiagnosticsHubScreen(state: MillDiagDashboardState, onNavigateToScreen: (String) -> Unit = {}) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val mainScrollState = rememberScrollState()

    // Global state for charts & KPIs
    var selectedMotorIndex by remember { mutableIntStateOf(0) }

    // Root Box to allow perfect overlay of the Left Navigation Bar
    Box(modifier = Modifier.fillMaxSize().background(MillDiagColors.AppBg)) {

        // Main Content pushed completely to the left, minimal padding so app bar hovers on top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(mainScrollState)
        ) {
            // Passed down onNavigateToScreen via onAction
            TopNavigationBar(userName = state.userName, userRole = state.userRole, onAction = onNavigateToScreen)

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    MillMotorSection(
                        state = state,
                        selectedMotorIndex = selectedMotorIndex,
                        onMotorSelected = { selectedMotorIndex = it },
                        modifier = Modifier.weight(1.8f).fillMaxHeight()
                    )
                    OverviewSection(
                        state = state,
                        selectedMotorIndex = selectedMotorIndex,
                        modifier = Modifier.weight(1.1f).fillMaxHeight()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    MillMotorSection(
                        state = state,
                        selectedMotorIndex = selectedMotorIndex,
                        onMotorSelected = { selectedMotorIndex = it },
                        modifier = Modifier.fillMaxWidth().height(550.dp)
                    )
                    OverviewSection(
                        state = state,
                        selectedMotorIndex = selectedMotorIndex,
                        modifier = Modifier.fillMaxWidth().height(550.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            BottomKpiRow(kpis = state.kpis, selectedMotorIndex = selectedMotorIndex, isLandscape = isLandscape)
        }

        // Floating App Bar Overlay - Offset slightly to sit perfectly on the left edge
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .zIndex(50f)
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp)
        ) {
            RadialAppBar(
                modifier = Modifier.align(Alignment.CenterStart),
                activeSection = "mill_dashboard",
                onActionSelected = onNavigateToScreen
            )
        }
    }
}

@Composable
fun TopNavigationBar(userName: String, userRole: String, onAction: (String) -> Unit) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Log Entry")
    var profileMenuExpanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        // Empty Spacer on the left so TopNav Text doesn't sit under the Radial Nav
        Spacer(Modifier.width(60.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MillGreyFrostGlassCard(shape = CircleShape) { Box(modifier = Modifier.size(44.dp).padding(8.dp), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = MillDiagColors.TextGray) } }
            MillGreyFrostGlassCard(shape = RoundedCornerShape(24.dp)) { Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text("Equipment Vitals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextDark); Icon(Icons.Filled.ArrowDropDown, null, tint = MillDiagColors.TextDark) } }
        }

        Spacer(Modifier.weight(1f))

        MillGreyFrostGlassCard(shape = RoundedCornerShape(32.dp)) {
            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                tabs.forEachIndexed { index, tab ->
                    TopNavLink(
                        text = tab,
                        isActive = activeTab == index,
                        onClick = {
                            if (tab == "Log Entry") {
                                onAction("mill_manual") // Route triggered by the Log Entry Button
                            } else {
                                activeTab = index // Keep the Overview tab state selected
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { profileMenuExpanded = true }) {
                    Column(horizontalAlignment = Alignment.End) { Text("Hello, $userName", fontSize = 12.sp, color = MillDiagColors.TextGray); Text(userRole, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextDark) }
                    Icon(Icons.Filled.ArrowDropDown, null, tint = MillDiagColors.TextDark)
                }
                DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
                    DropdownMenuItem(text = { Text("Log Out", color = MillDiagColors.StatusRed, fontWeight = FontWeight.Bold) }, onClick = { profileMenuExpanded = false; onAction("Log Out") })
                }
            }
            MillGreyFrostGlassCard(shape = CircleShape) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, null, tint = MillDiagColors.TextDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-10).dp, 10.dp).size(8.dp).background(MillDiagColors.AccentOrange, CircleShape).border(1.5.dp, Color.White, CircleShape))
                }
            }
        }
    }
}

@Composable
fun TopNavLink(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(24.dp)).clickable { onClick() }.background(if (isActive) Color.White.copy(0.6f) else Color.Transparent).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text, fontSize = 13.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) MillDiagColors.TextDark else MillDiagColors.TextGray)
    }
}

@Composable
fun MillMotorSection(state: MillDiagDashboardState, selectedMotorIndex: Int, onMotorSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val activeMotor = state.motors.getOrNull(selectedMotorIndex)

    MillGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Indent the title header to avoid the floating App Bar on the left
            Column(modifier = Modifier.padding(start = 65.dp, end = 24.dp, top = 20.dp, bottom = 0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mill Drive System", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark)
                    Spacer(Modifier.width(12.dp))
                    MillGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(state.sectionStatus.toColor(), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(state.sectionStatus.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = state.sectionStatus.toColor(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Batch ID: ${state.batchId} | Shift Started: ${state.startTime}", fontSize = 12.sp, color = MillDiagColors.TextGray)
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Image(painter = painterResource(id = activeMotor?.imageRes ?: R.drawable.motor_image), contentDescription = activeMotor?.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

                // Moved Start/Left cards inwards so they aren't hidden by the App Bar
                activeMotor?.let { FloatingStatusCard(title = "${it.name} Status", value = it.healthValue, statusText = it.statusText, statusColor = it.status.toColor(), modifier = Modifier.align(Alignment.TopStart).padding(start = 65.dp, top = 16.dp)) }
                state.connectedEquipment.getOrNull(1)?.let { eq -> FloatingStatusCard(title = eq.name, value = eq.value, statusText = eq.statusText, statusColor = eq.status.toColor(), isBlueIcon = eq.status == MillDiagEquipStatus.STANDBY, modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 16.dp)) }
                state.connectedEquipment.getOrNull(0)?.let { eq -> FloatingStatusCard(title = eq.name, value = eq.value, statusText = eq.statusText, statusColor = eq.status.toColor(), modifier = Modifier.align(Alignment.BottomStart).padding(start = 65.dp, bottom = 16.dp)) }
                state.connectedEquipment.getOrNull(2)?.let { eq -> FloatingStatusCard(title = eq.name, value = eq.value, statusText = eq.statusText, statusColor = eq.status.toColor(), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)) }
            }
            if (state.motors.isNotEmpty()) {
                BottomImageArcCarousel(motors = state.motors, selectedIndex = selectedMotorIndex, onItemSelected = onMotorSelected, modifier = Modifier.fillMaxWidth().height(130.dp))
            }
        }
    }
}

@Composable
fun BottomImageArcCarousel(motors: List<MillDiagMotorData>, selectedIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val swipeThreshold = LocalDensity.current.run { 40.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(selectedIndex) {
            detectHorizontalDragGestures(
                onDragEnd = { dragOffset = 0f }, onDragCancel = { dragOffset = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume(); dragOffset += dragAmount
                    if (dragOffset > swipeThreshold) { if (selectedIndex > 0) onItemSelected(selectedIndex - 1); dragOffset = 0f }
                    else if (dragOffset < -swipeThreshold) { if (selectedIndex < motors.size - 1) onItemSelected(selectedIndex + 1); dragOffset = 0f }
                }
            )
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height; val curveTopY = 55.dp.toPx(); val controlY = -18.dp.toPx()
            val bandPath = Path().apply { moveTo(0f, curveTopY); quadraticBezierTo(w / 2f, controlY, w, curveTopY); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(path = bandPath, brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.92f), Color(0xFFDDE5EF).copy(0.5f)), startY = 0f, endY = h))
            drawPath(path = Path().apply { moveTo(0f, curveTopY); quadraticBezierTo(w / 2f, controlY, w, curveTopY) }, color = MillDiagColors.BorderGray.copy(alpha = 0.7f), style = Stroke(width = 1.5.dp.toPx()))
        }
        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = 10.dp)) {
            Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
                drawPath(Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close() }, color = MillDiagColors.AccentOrange)
            }
        }

        val arcRadiusDp = 420f; val springSpec = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
        motors.forEachIndexed { index, motor ->
            val diff = index - selectedIndex; val angleRad = Math.toRadians((diff * 16f).toDouble())
            val animX by animateFloatAsState((sin(angleRad) * arcRadiusDp).toFloat(), springSpec, label = "")
            val animY by animateFloatAsState((arcRadiusDp * (1.0 - cos(angleRad))).toFloat(), springSpec, label = "")
            val animScale by animateFloatAsState(if (diff == 0) 1f else 0.72f, springSpec, label = "")
            val animAlpha by animateFloatAsState(when { diff == 0 -> 1f; kotlin.math.abs(diff) == 1 -> 0.75f; kotlin.math.abs(diff) == 2 -> 0.45f; else -> 0f }, springSpec, label = "")

            Box(
                modifier = Modifier.align(Alignment.TopCenter).offset(x = animX.dp, y = (animY - arcRadiusDp * 0.003f).dp + 4.dp).zIndex(if (diff == 0) 10f else (8f - kotlin.math.abs(diff).toFloat())).scale(animScale).alpha(animAlpha).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                val ringColor = if (diff == 0) MillDiagColors.AccentOrange else MillDiagColors.BorderGray
                val itemSize = if (diff == 0) 68.dp else 44.dp
                Box(modifier = Modifier.size(itemSize).shadow(if (diff == 0) 16.dp else 4.dp, CircleShape).background(Color.White, CircleShape).border(if (diff == 0) 3.dp else 1.dp, ringColor, CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = motor.imageRes), contentDescription = motor.name, modifier = Modifier.fillMaxSize().padding(if (diff == 0) 8.dp else 6.dp), contentScale = ContentScale.Inside)
                }
                if (diff == 0) {
                    Box(modifier = Modifier.offset(y = (68.dp / 2 + 6.dp)).background(Color.White.copy(0.95f), RoundedCornerShape(10.dp)).border(1.dp, MillDiagColors.AccentOrange.copy(0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text(motor.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingStatusCard(title: String, value: String, statusText: String, statusColor: Color, modifier: Modifier = Modifier, isBlueIcon: Boolean = false) {
    MillGreyFrostGlassCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = MillDiagColors.TextGray, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(120.dp)) {
                Column { Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark); Text(statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold) }
                Box(modifier = Modifier.size(26.dp).background(statusColor, CircleShape), contentAlignment = Alignment.Center) { Icon(if (isBlueIcon) Icons.Rounded.Pause else Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
            }
        }
    }
}

@Composable
fun OverviewSection(state: MillDiagDashboardState, selectedMotorIndex: Int, modifier: Modifier = Modifier) {

    // Dynamic values based on selected motor index
    val capacityFloat = state.chartData.designCapacity.toString().toFloatOrNull() ?: 1200f
    val thresholdFloat = state.chartData.overloadThreshold.toString().toFloatOrNull() ?: 1000f
    val baseLoad = state.chartData.currentLoad.toString().toFloatOrNull() ?: 850f

    val dynamicLoad = (baseLoad + (selectedMotorIndex * 115.5f)).coerceIn(0f, capacityFloat)
    val formattedLoad = ((dynamicLoad * 10).roundToInt() / 10f).toString()

    val baseEff = state.efficiency.toString().toFloatOrNull() ?: 92f
    val dynamicEff = (baseEff - (selectedMotorIndex * 1.6f)).coerceIn(0f, 100f)
    val formattedEff = ((dynamicEff * 10).roundToInt() / 10f).toString()

    val baseOee = state.oee.toString().toFloatOrNull() ?: 88f
    val dynamicOee = (baseOee - (selectedMotorIndex * 2.2f)).coerceIn(0f, 100f)
    val formattedOee = ((dynamicOee * 10).roundToInt() / 10f).toString()

    // Animate the actual Arc Gauge Sweep
    val loadSweepTarget = (dynamicLoad / capacityFloat) * 180f
    val animatedLoadSweep by animateFloatAsState(
        targetValue = loadSweepTarget,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "LoadSweep"
    )

    // Generate historical points perfectly syncing with dynamicEff & dynamicOee
    val effPoints = remember(dynamicEff, selectedMotorIndex) {
        List(12) { i -> if (i == 11) dynamicEff else (dynamicEff + sin(i.toFloat() + selectedMotorIndex) * 5f - 2f).coerceIn(60f, 100f) }
    }
    val oeePoints = remember(dynamicOee, selectedMotorIndex) {
        List(12) { i -> if (i == 11) dynamicOee else (dynamicOee + cos(i.toFloat() + selectedMotorIndex) * 6f - 3f).coerceIn(60f, 100f) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // --- 1. GAUGE CHART CARD ---
        MillGreyFrostGlassCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Equipment Load Analytics", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark)
                    MillGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) { Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text("Real-Time", fontSize = 12.sp, color = MillDiagColors.TextDark, fontWeight = FontWeight.Bold); Icon(Icons.Filled.ArrowDropDown, null, tint = MillDiagColors.TextDark, modifier = Modifier.size(16.dp)) } }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
                        val textMeasurer = rememberTextMeasurer()
                        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp, end = 12.dp)) {
                            val w = size.width; val h = size.height; val cx = w / 2f; val cy = h - 10.dp.toPx()
                            val rOuter = minOf(w / 2f, h) * 0.95f; val rMid = rOuter * 0.72f; val rInner = rOuter * 0.45f
                            val thresholdSweep = (thresholdFloat / capacityFloat) * 180f
                            val loadColor = if (dynamicLoad >= thresholdFloat) MillDiagColors.StatusRed else MillDiagColors.AccentOrange

                            drawArc(MillDiagColors.AccentOrangeLight, 180f, 180f, true, Offset(cx - rOuter, cy - rOuter), Size(rOuter * 2, rOuter * 2))
                            drawArc(MillDiagColors.StatusRed.copy(0.15f), 180f, thresholdSweep, true, Offset(cx - rMid, cy - rMid), Size(rMid * 2, rMid * 2))
                            drawArc(loadColor, 180f, animatedLoadSweep, true, Offset(cx - rInner, cy - rInner), Size(rInner * 2, rInner * 2))

                            val dot = 4.dp.toPx()
                            drawCircle(MillDiagColors.AccentOrangeLight, dot, Offset(cx, cy - rOuter)); drawCircle(MillDiagColors.StatusRed, dot, Offset(cx, cy - rMid)); drawCircle(loadColor, dot, Offset(cx, cy - rInner))

                            val lbl = TextStyle(fontSize = 10.sp, color = MillDiagColors.TextDark, fontWeight = FontWeight.Bold); val unit = state.chartData.unit

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${state.chartData.designCapacity} $unit",
                                topLeft = Offset(cx - textMeasurer.measure("${state.chartData.designCapacity} $unit", lbl).size.width / 2f, cy - rOuter - 20f),
                                style = lbl
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${state.chartData.overloadThreshold} $unit",
                                topLeft = Offset(cx - textMeasurer.measure("${state.chartData.overloadThreshold} $unit", lbl).size.width / 2f, cy - rMid - 20f),
                                style = lbl.copy(color = MillDiagColors.StatusRed)
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "$formattedLoad $unit",
                                topLeft = Offset(cx - textMeasurer.measure("$formattedLoad $unit", lbl).size.width / 2f, cy - rInner - 20f),
                                style = lbl.copy(color = loadColor)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        val activeColor = if (dynamicLoad >= thresholdFloat) MillDiagColors.StatusRed else MillDiagColors.AccentOrange
                        LegendItem("Actual Load", "$formattedLoad ${state.chartData.unit}", activeColor)
                        Spacer(Modifier.height(14.dp))
                        LegendItem("Threshold Limit", "${state.chartData.overloadThreshold} ${state.chartData.unit}", MillDiagColors.StatusRed)
                        Spacer(Modifier.height(14.dp))
                        LegendItem("Design Capacity", "${state.chartData.designCapacity} ${state.chartData.unit}", MillDiagColors.AccentOrangeLight)
                    }
                }
            }
        }

        // --- 2. PERFORMANCE HISTORY CHART CARD ---
        MillGreyFrostGlassCard(modifier = Modifier.fillMaxWidth().weight(1.3f)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Header Texts
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Performance History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextDark)
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(state.sectionStatus.name, fontSize = 13.sp, color = state.sectionStatus.toColor(), fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp)); Box(Modifier.size(9.dp).background(state.sectionStatus.toColor(), CircleShape)) }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(MillDiagColors.StatusGreen, CircleShape)); Spacer(Modifier.width(6.dp)); Text("Drive Efficiency", fontSize = 12.sp, color = MillDiagColors.TextGray) }
                        Text("$formattedEff%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark, modifier = Modifier.padding(start = 14.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(MillDiagColors.StatusBlue, CircleShape)); Spacer(Modifier.width(6.dp)); Text("OEE", fontSize = 12.sp, color = MillDiagColors.TextGray) }
                        Text("$formattedOee%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark, modifier = Modifier.padding(start = 14.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Real Line Chart matches Eff and OEE points
                val textMeasurer = rememberTextMeasurer()
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val chartMin = 60f
                    val chartMax = 100f
                    val chartRange = chartMax - chartMin

                    // Draw horizontal grid lines & labels
                    val yLabels = listOf(100, 80, 60)
                    yLabels.forEach { value ->
                        val normalizedY = 1f - ((value - chartMin) / chartRange)
                        val yPos = normalizedY * size.height
                        drawLine(MillDiagColors.BorderGray.copy(alpha = 0.4f), Offset(25.dp.toPx(), yPos), Offset(size.width, yPos), 1.dp.toPx())
                        drawText(
                            textMeasurer = textMeasurer,
                            text = value.toString(),
                            topLeft = Offset(0f, yPos - 6.dp.toPx()),
                            style = TextStyle(fontSize = 10.sp, color = MillDiagColors.TextGray)
                        )
                    }

                    // Helper to draw the smooth lines
                    fun drawMetricLine(points: List<Float>, color: Color, isFill: Boolean = false) {
                        val path = Path()
                        val stepX = (size.width - 30.dp.toPx()) / (points.size - 1)
                        var prevX = 30.dp.toPx()
                        var prevY = size.height - (((points[0] - chartMin) / chartRange) * size.height)

                        path.moveTo(prevX, prevY)
                        for (i in 1 until points.size) {
                            val x = 30.dp.toPx() + (i * stepX)
                            val y = size.height - (((points[i] - chartMin) / chartRange) * size.height)
                            val cpX = (prevX + x) / 2f
                            path.cubicTo(cpX, prevY, cpX, y, x, y)
                            prevX = x; prevY = y
                        }

                        if (isFill) {
                            val fillPath = Path().apply { addPath(path); lineTo(prevX, size.height); lineTo(30.dp.toPx(), size.height); close() }
                            drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)))
                        } else {
                            drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                            // Draw end dot perfectly matching the large text
                            drawCircle(Color.White, 5.dp.toPx(), Offset(prevX, prevY))
                            drawCircle(color, 3.dp.toPx(), Offset(prevX, prevY))
                        }
                    }

                    // Draw OEE Line (Blue)
                    drawMetricLine(oeePoints, MillDiagColors.StatusBlue, isFill = true)
                    drawMetricLine(oeePoints, MillDiagColors.StatusBlue, isFill = false)

                    // Draw Efficiency Line (Green)
                    drawMetricLine(effPoints, MillDiagColors.StatusGreen, isFill = true)
                    drawMetricLine(effPoints, MillDiagColors.StatusGreen, isFill = false)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(9.dp).background(color, CircleShape)); Spacer(Modifier.width(6.dp)); Text(label, fontSize = 11.sp, color = MillDiagColors.TextGray) }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextDark, modifier = Modifier.padding(start = 15.dp))
    }
}

@Composable
fun BottomKpiRow(kpis: List<MillDiagKpiData>, selectedMotorIndex: Int, isLandscape: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().let { if (!isLandscape) it.horizontalScroll(rememberScrollState()) else it }.padding(start = 65.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        kpis.forEachIndexed { index, kpi ->
            KpiCard(
                modifier = if (isLandscape) Modifier.weight(1f) else Modifier.width(220.dp),
                data = kpi,
                selectedIndex = selectedMotorIndex * 3 + index
            )
        }
    }
}

@Composable
fun KpiCard(modifier: Modifier, data: MillDiagKpiData, selectedIndex: Int) {
    val accentColor = data.type.toColor()
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val pulseAlpha by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse")

    val numVal = data.value.filter { it.isDigit() || it == '.' }.toString().toFloatOrNull()
    val dynamicValueStr = if (numVal != null && selectedIndex != 0) {
        val newVal = numVal * (1f + (selectedIndex * 0.04f))
        val formatted = ((newVal * 10f).roundToInt() / 10f).toString()
        val suffix = data.value.filterNot { it.isDigit() || it == '.' }
        formatted + suffix
    } else data.value

    val dynamicTrendHistory = remember(data.trendHistory, selectedIndex) {
        if (selectedIndex == 0) data.trendHistory
        else {
            val phaseShift = selectedIndex * 1.5f
            data.trendHistory.mapIndexed { i, v -> (v + sin(i.toFloat() + phaseShift) * (v * 0.2f)).coerceAtLeast(0f) }
        }
    }

    MillGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp, start = 18.dp, end = 18.dp, bottom = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(30.dp).background(accentColor.copy(0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(data.type.toIcon(), null, tint = accentColor, modifier = Modifier.size(16.dp)) }; Spacer(Modifier.width(8.dp)); Text(data.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.TextGray) }
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).background(MillDiagColors.StatusRed.copy(pulseAlpha), CircleShape)); Spacer(Modifier.width(3.dp)); Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MillDiagColors.StatusRed) }
            }
            Spacer(Modifier.height(12.dp)); Text(dynamicValueStr, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MillDiagColors.TextDark); Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (data.isUpwardTrend) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward, null, tint = if (data.isUpwardTrend) MillDiagColors.StatusRed else MillDiagColors.StatusGreen, modifier = Modifier.size(12.dp)); Text(" ${data.changeString} vs baseline", fontSize = 10.sp, color = MillDiagColors.TextGray) }
            Spacer(Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                if (dynamicTrendHistory.isNotEmpty()) {
                    val linePath = Path(); val fillPath = Path()
                    val stepX = size.width / (dynamicTrendHistory.size - 1).coerceAtLeast(1)
                    val minV = dynamicTrendHistory.minOrNull() ?: 0f; val maxV = dynamicTrendHistory.maxOrNull() ?: 1f
                    val range = (maxV - minV).coerceAtLeast(0.001f) * 1.2f; val yOffset = (range - (maxV - minV)) / 2f
                    var prevX = 0f; var prevY = size.height - (((dynamicTrendHistory[0] - minV + yOffset) / range) * size.height)
                    linePath.moveTo(prevX, prevY); fillPath.moveTo(prevX, size.height); fillPath.lineTo(prevX, prevY)

                    for (i in 1 until dynamicTrendHistory.size) {
                        val x = i * stepX; val y = size.height - (((dynamicTrendHistory[i] - minV + yOffset) / range) * size.height)
                        val controlPointX = (prevX + x) / 2f
                        linePath.cubicTo(controlPointX, prevY, controlPointX, y, x, y); fillPath.cubicTo(controlPointX, prevY, controlPointX, y, x, y)
                        prevX = x; prevY = y
                    }
                    fillPath.lineTo(prevX, size.height); fillPath.close()
                    drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent), startY = 0f, endY = size.height))
                    drawPath(path = linePath, color = accentColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }
}