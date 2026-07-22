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
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// 🎨 DEFECATION COLOR MAPPERS & UTILS
// ============================================================================
val AppBg = Color(0xFFF1F5F9)
val DefecationOrange = Color(0xFFFF6B35)
val DefecationTeal = Color(0xFF00B4D8)
val DefecationPurple = Color(0xFF7B2FF7)
val DefecationAmber = Color(0xFFFFB020)
val DefecationCyan = Color(0xFF00E5FF)

val TextDark = Color(0xFF1E293B)
val TextGray = Color(0xFF64748B)
val BorderGray = Color(0xFFE2E8F0)
val StatusGreen = Color(0xFF22C55E)
val StatusRed = Color(0xFFEF4444)

fun DefecationKpiType.toIcon(): ImageVector = when (this) {
    DefecationKpiType.JUICE_TEMP -> Icons.Rounded.Thermostat
    DefecationKpiType.PH_LEVEL -> Icons.Outlined.WaterDrop
    DefecationKpiType.LIME_DOSAGE -> Icons.Outlined.ViewInAr
    DefecationKpiType.SEDIMENTATION -> Icons.Outlined.Insights
    DefecationKpiType.HEAT_RECOVERY -> Icons.Outlined.Bolt
}

fun DefecationKpiType.toColor(): Color = when (this) {
    DefecationKpiType.JUICE_TEMP -> DefecationOrange
    DefecationKpiType.PH_LEVEL -> DefecationTeal
    DefecationKpiType.LIME_DOSAGE -> DefecationPurple
    DefecationKpiType.SEDIMENTATION -> DefecationAmber
    DefecationKpiType.HEAT_RECOVERY -> DefecationCyan
}

fun EquipmentStatus.toColor(): Color = when (this) {
    EquipmentStatus.RUNNING, EquipmentStatus.HEALTHY -> StatusGreen
    EquipmentStatus.STANDBY -> DefecationTeal
    EquipmentStatus.FAULT -> StatusRed
}

// ============================================================================
// 🧊 GLASS CARD
// ============================================================================
@Composable
fun DefecationGreyFrostGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                12.dp,
                shape,
                spotColor = Color(0xFF8A9AAB).copy(0.5f),
                ambientColor = Color(0xFF8A9AAB).copy(0.2f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(0.65f), Color(0xFFC9D4E2).copy(0.4f)),
                    Offset.Zero,
                    Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(0.9f), Color(0xFFA5B4C7).copy(0.3f)),
                    Offset.Zero,
                    Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape
            ),
        content = content
    )
}

// ============================================================================
// 🌟 FLOATING STATUS CARD
// ============================================================================
@Composable
fun DefecationFloatingStatusCard(
    title: String,
    value: String,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    isBlueIcon: Boolean = false
) {
    DefecationGreyFrostGlassCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(120.dp)
            ) {
                Column {
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Text(statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(26.dp).background(statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isBlueIcon) Icons.Rounded.Pause else Icons.Rounded.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 📱 DEFECATION MAIN SCREEN
// ============================================================================
@Composable
fun DefecationDiagnosticsHubScreen(
    userName: String = "Production",
    userRole: String = "Process Engineer",
    onNavigateToScreen: (String) -> Unit = {}
) {
    val viewModel: DefecationDiagnosticsViewModel = viewModel(
        modelClass = DefecationDiagnosticsViewModel::class.java,
        factory = DefecationDiagnosticsViewModel.provideFactory(userName, userRole)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val mainScrollState = rememberScrollState()

    var selectedEquipmentIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(mainScrollState)
        ) {
            DefecationTopNavigationBar(
                userName = state.userName,
                userRole = state.userRole,
                onAction = onNavigateToScreen // <-- Passed down to TopNav
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DefecationEquipmentSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        onItemSelected = { selectedEquipmentIndex = it },
                        modifier = Modifier.weight(1.8f).fillMaxHeight()
                    )
                    DefecationOverviewSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DefecationEquipmentSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        onItemSelected = { selectedEquipmentIndex = it },
                        modifier = Modifier.fillMaxWidth().height(550.dp)
                    )
                    DefecationOverviewSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        modifier = Modifier.fillMaxWidth().height(500.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            DefecationKpiRow(
                kpis = state.kpis,
                selectedIndex = selectedEquipmentIndex,
                isLandscape = isLandscape
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .offset(x = (-8).dp)
                .zIndex(50f)
        ) {
            RadialAppBar(
                modifier = Modifier.align(Alignment.CenterStart),
                activeSection = "defecation_dashboard",
                onActionSelected = onNavigateToScreen
            )
        }
    }
}

// ============================================================================
// 🌐 TOP NAV BAR
// ============================================================================
@Composable
fun DefecationTopNavigationBar(userName: String, userRole: String, onAction: (String) -> Unit) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Log Entry")

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(60.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DefecationGreyFrostGlassCard(shape = CircleShape) {
                Box(modifier = Modifier.size(44.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = TextGray)
                }
            }
            DefecationGreyFrostGlassCard(shape = RoundedCornerShape(24.dp)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Defecation Section", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        DefecationGreyFrostGlassCard(shape = RoundedCornerShape(32.dp)) {
            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                if (tab == "Log Entry") {
                                    onAction("dcs_screen") // Route triggered by the Log Entry Button
                                } else {
                                    activeTab = index // Keep the Overview tab state selected
                                }
                            }
                            .background(if (activeTab == index) Color.White.copy(0.6f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tab,
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == index) TextDark else TextGray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hello, $userName", fontSize = 12.sp, color = TextGray)
                    Text(userRole, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark)
            }
            DefecationGreyFrostGlassCard(shape = CircleShape) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, null, tint = TextDark)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset((-10).dp, 10.dp)
                            .size(8.dp)
                            .background(DefecationOrange, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

// ============================================================================
// ⚙️ DEFECATION EQUIPMENT SECTION
// ============================================================================
@Composable
fun DefecationEquipmentSection(
    state: DefecationDashboardState,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEquipment = state.equipment.getOrNull(selectedIndex)

    DefecationGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(start = 70.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Defecation Section", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Spacer(Modifier.width(12.dp))
                    DefecationGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(state.sectionStatus.toColor(), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                state.sectionStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                color = state.sectionStatus.toColor(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Batch ID: ${state.batchId} | Started: ${state.startTime}", fontSize = 12.sp, color = TextGray)
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Image(
                    painter = painterResource(id = activeEquipment?.imageRes ?: R.drawable.defecation_image),
                    contentDescription = activeEquipment?.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                activeEquipment?.let { eq ->
                    DefecationFloatingStatusCard(
                        title = "${eq.name} Status",
                        value = eq.healthValue,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 70.dp, top = 16.dp)
                    )
                }
                state.connectedEquipment.getOrNull(1)?.let { eq ->
                    DefecationFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        isBlueIcon = eq.status == EquipmentStatus.STANDBY,
                        modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 16.dp)
                    )
                }
                state.connectedEquipment.getOrNull(0)?.let { eq ->
                    DefecationFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 70.dp, bottom = 16.dp)
                    )
                }
                state.connectedEquipment.getOrNull(2)?.let { eq ->
                    DefecationFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
                    )
                }
            }

            if (state.equipment.isNotEmpty()) {
                DefecationArcCarousel(
                    equipment = state.equipment,
                    selectedIndex = selectedIndex,
                    onItemSelected = onItemSelected,
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                )
            }
        }
    }
}

// ============================================================================
// 🎠 DEFECATION ARC CAROUSEL
// ============================================================================
@Composable
fun DefecationArcCarousel(
    equipment: List<DefecationEquipment>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeThreshold = LocalDensity.current.run { 40.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(selectedIndex) {
            detectHorizontalDragGestures(
                onDragEnd = { dragOffset = 0f },
                onDragCancel = { dragOffset = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                    if (dragOffset > swipeThreshold) {
                        if (selectedIndex > 0) onItemSelected(selectedIndex - 1)
                        dragOffset = 0f
                    } else if (dragOffset < -swipeThreshold) {
                        if (selectedIndex < equipment.size - 1) onItemSelected(selectedIndex + 1)
                        dragOffset = 0f
                    }
                }
            )
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val curveTopY = 55.dp.toPx()
            val controlY = -18.dp.toPx()

            val bandPath = Path().apply {
                moveTo(0f, curveTopY)
                quadraticBezierTo(w / 2f, controlY, w, curveTopY)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = bandPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.92f), Color(0xFFDDE5EF).copy(0.5f)),
                    startY = 0f,
                    endY = h
                )
            )
            drawPath(
                path = Path().apply {
                    moveTo(0f, curveTopY)
                    quadraticBezierTo(w / 2f, controlY, w, curveTopY)
                },
                color = BorderGray.copy(alpha = 0.7f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = 10.dp)) {
            Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                drawPath(path, color = DefecationOrange)
            }
        }

        val arcRadiusDp = 420f
        val springSpec = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)

        equipment.forEachIndexed { index, eq ->
            val diff = index - selectedIndex
            val angleRad = Math.toRadians((diff * 16f).toDouble())

            val animX by animateFloatAsState((sin(angleRad) * arcRadiusDp).toFloat(), springSpec, label = "arcX")
            val animY by animateFloatAsState((arcRadiusDp * (1.0 - cos(angleRad))).toFloat(), springSpec, label = "arcY")
            val animScale by animateFloatAsState(if (diff == 0) 1f else 0.72f, springSpec, label = "arcScale")
            val animAlpha by animateFloatAsState(
                when {
                    diff == 0 -> 1f
                    kotlin.math.abs(diff) == 1 -> 0.75f
                    kotlin.math.abs(diff) == 2 -> 0.45f
                    else -> 0f
                },
                springSpec,
                label = "arcAlpha"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = animX.dp, y = (animY - arcRadiusDp * 0.003f).dp + 4.dp)
                    .zIndex(if (diff == 0) 10f else (8f - kotlin.math.abs(diff).toFloat()))
                    .scale(animScale)
                    .alpha(animAlpha)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                val itemSize = if (diff == 0) 68.dp else 44.dp
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .shadow(if (diff == 0) 16.dp else 4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .border(if (diff == 0) 3.dp else 1.dp, if (diff == 0) DefecationOrange else BorderGray, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = eq.imageRes),
                        contentDescription = eq.name,
                        modifier = Modifier.fillMaxSize().padding(if (diff == 0) 8.dp else 6.dp),
                        contentScale = ContentScale.Inside
                    )
                }

                if (diff == 0) {
                    Box(
                        modifier = Modifier
                            .offset(y = (68.dp / 2 + 6.dp))
                            .background(Color.White.copy(0.95f), RoundedCornerShape(10.dp))
                            .border(1.dp, DefecationOrange.copy(0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(eq.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 📊 DEFECATION OVERVIEW SECTION
// ============================================================================
@Composable
fun DefecationOverviewSection(
    state: DefecationDashboardState,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    val capacity = state.chartData.designFlow.toFloat()
    val target = state.chartData.targetFlow.toFloat()
    val baseActual = state.chartData.actualFlow.toFloat()

    val dynamicActual = (baseActual + selectedIndex * 3500f).coerceIn(0f, capacity)
    val actualFlowText = String.format("%,d", dynamicActual.roundToInt())

    val baseEfficiency = state.efficiency.toString().toFloatOrNull() ?: 92f
    val baseOee = state.oee.toString().toFloatOrNull() ?: 88f

    val dynamicEfficiency = (baseEfficiency - selectedIndex * 1.7f).coerceIn(0f, 100f)
    val dynamicOee = (baseOee - selectedIndex * 2.1f).coerceIn(0f, 100f)

    val actualSweep by animateFloatAsState(
        targetValue = (dynamicActual / capacity) * 180f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "actualSweep"
    )

    val efficiencyHistory = remember(dynamicEfficiency, selectedIndex) {
        List(12) { index ->
            if (index == 11) dynamicEfficiency
            else (dynamicEfficiency + sin(index.toFloat() + selectedIndex) * 4.5f - 2f).coerceIn(60f, 100f)
        }
    }

    val oeeHistory = remember(dynamicOee, selectedIndex) {
        List(12) { index ->
            if (index == 11) dynamicOee
            else (dynamicOee + cos(index.toFloat() + selectedIndex) * 5f - 2.5f).coerceIn(55f, 100f)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        DefecationGreyFrostGlassCard(modifier = Modifier.weight(1.15f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    DefecationGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Today", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
                        val textMeasurer = rememberTextMeasurer()

                        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 50.dp, top = 20.dp, end = 12.dp)) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h - 8.dp.toPx()
                            val rOuter = minOf(w / 2f, h) * 0.95f
                            val rMid = rOuter * 0.7f
                            val rInner = rOuter * 0.42f

                            val targetSweep = (target / capacity) * 180f
                            val actualColor = if (dynamicActual >= target) DefecationOrange else DefecationTeal

                            drawArc(
                                color = DefecationOrange.copy(alpha = 0.15f),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(cx - rOuter, cy - rOuter),
                                size = Size(rOuter * 2, rOuter * 2)
                            )
                            drawArc(
                                color = DefecationTeal.copy(alpha = 0.35f),
                                startAngle = 180f,
                                sweepAngle = targetSweep,
                                useCenter = true,
                                topLeft = Offset(cx - rMid, cy - rMid),
                                size = Size(rMid * 2, rMid * 2)
                            )
                            drawArc(
                                color = actualColor,
                                startAngle = 180f,
                                sweepAngle = actualSweep,
                                useCenter = true,
                                topLeft = Offset(cx - rInner, cy - rInner),
                                size = Size(rInner * 2, rInner * 2)
                            )

                            val labelStyle = TextStyle(fontSize = 10.sp, color = TextDark, fontWeight = FontWeight.Bold)

                            val designLabel = "${state.chartData.designFlow / 1000}K+"
                            val targetLabel = "${state.chartData.targetFlow / 1000}K+"
                            val actualLabel = "${dynamicActual.roundToInt() / 1000}K+"

                            drawText(
                                textMeasurer = textMeasurer,
                                text = designLabel,
                                topLeft = Offset(
                                    cx - textMeasurer.measure(designLabel, labelStyle).size.width / 2f,
                                    cy - rOuter - 20f
                                ),
                                style = labelStyle
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = targetLabel,
                                topLeft = Offset(
                                    cx - textMeasurer.measure(targetLabel, labelStyle).size.width / 2f,
                                    cy - rMid - 20f
                                ),
                                style = labelStyle.copy(color = DefecationTeal)
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = actualLabel,
                                topLeft = Offset(
                                    cx - textMeasurer.measure(actualLabel, labelStyle).size.width / 2f,
                                    cy - rInner - 20f
                                ),
                                style = labelStyle.copy(color = actualColor)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        DefecationLegendItem("Actual", "$actualFlowText L/hr", if (dynamicActual >= target) DefecationOrange else DefecationTeal)
                        Spacer(Modifier.height(14.dp))
                        DefecationLegendItem("Target", "${String.format("%,d", state.chartData.targetFlow)} L/hr", DefecationTeal)
                        Spacer(Modifier.height(14.dp))
                        DefecationLegendItem("Design", "${String.format("%,d", state.chartData.designFlow)} L/hr", DefecationOrange.copy(alpha = 0.5f))
                    }
                }
            }
        }

        DefecationGreyFrostGlassCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Section Performance", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.sectionStatus.name, fontSize = 13.sp, color = state.sectionStatus.toColor(), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(9.dp).background(state.sectionStatus.toColor(), CircleShape))
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = BorderGray.copy(0.5f))
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efficiency", fontSize = 12.sp, color = TextGray)
                        Text("${((dynamicEfficiency * 10).roundToInt() / 10f)}%", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OEE", fontSize = 12.sp, color = TextGray)
                        Text("${((dynamicOee * 10).roundToInt() / 10f)}%", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    }
                }

                Spacer(Modifier.height(18.dp))

                val textMeasurer = rememberTextMeasurer()
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val chartMin = 55f
                    val chartMax = 100f
                    val chartRange = chartMax - chartMin
                    val leftPad = 28.dp.toPx()

                    listOf(100, 80, 60).forEach { label ->
                        val y = (1f - ((label - chartMin) / chartRange)) * size.height
                        drawLine(
                            color = BorderGray.copy(alpha = 0.45f),
                            start = Offset(leftPad, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = label.toString(),
                            topLeft = Offset(0f, y - 8.dp.toPx()),
                            style = TextStyle(fontSize = 10.sp, color = TextGray)
                        )
                    }

                    fun drawSeries(points: List<Float>, color: Color, fill: Boolean) {
                        if (points.isEmpty()) return

                        val stepX = (size.width - leftPad) / (points.size - 1).coerceAtLeast(1)
                        val linePath = Path()
                        var prevX = leftPad
                        var prevY = size.height - (((points[0] - chartMin) / chartRange) * size.height)

                        linePath.moveTo(prevX, prevY)

                        for (i in 1 until points.size) {
                            val x = leftPad + (i * stepX)
                            val y = size.height - (((points[i] - chartMin) / chartRange) * size.height)
                            val cpX = (prevX + x) / 2f
                            linePath.cubicTo(cpX, prevY, cpX, y, x, y)
                            prevX = x
                            prevY = y
                        }

                        if (fill) {
                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(prevX, size.height)
                                lineTo(leftPad, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(color.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )
                        } else {
                            drawPath(
                                path = linePath,
                                color = color,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawCircle(Color.White, 5.dp.toPx(), Offset(prevX, prevY))
                            drawCircle(color, 3.dp.toPx(), Offset(prevX, prevY))
                        }
                    }

                    drawSeries(oeeHistory, DefecationTeal, true)
                    drawSeries(oeeHistory, DefecationTeal, false)
                    drawSeries(efficiencyHistory, StatusGreen, true)
                    drawSeries(efficiencyHistory, StatusGreen, false)
                }
            }
        }
    }
}

@Composable
fun DefecationLegendItem(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = TextGray)
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(start = 15.dp))
    }
}

// ============================================================================
// 📈 DEFECATION KPI ROW
// ============================================================================
@Composable
fun DefecationKpiRow(
    kpis: List<KpiDataDefecation>,
    selectedIndex: Int,
    isLandscape: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (!isLandscape) it.horizontalScroll(rememberScrollState()) else it }
            .padding(start = 70.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        kpis.forEachIndexed { index, kpi ->
            DefecationKpiCard(
                modifier = if (isLandscape) Modifier.weight(1f) else Modifier.width(190.dp),
                data = kpi,
                selectedIndex = selectedIndex * 3 + index
            )
        }
    }
}

@Composable
fun DefecationKpiCard(
    modifier: Modifier,
    data: KpiDataDefecation,
    selectedIndex: Int
) {
    val accentColor = data.type.toColor()
    val pulseAlpha by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )

    val numericValue = data.value.filter { it.isDigit() || it == '.' }.toFloatOrNull()
    val dynamicValue = if (numericValue != null && selectedIndex != 0) {
        val varied = numericValue * (1f + selectedIndex * 0.035f)
        val suffix = data.value.filterNot { it.isDigit() || it == '.' }
        "${((varied * 10).roundToInt() / 10f)}$suffix"
    } else {
        data.value
    }

    val dynamicTrendHistory = remember(data.trendHistory, selectedIndex) {
        if (selectedIndex == 0) {
            data.trendHistory
        } else {
            val phase = selectedIndex * 1.2f
            data.trendHistory.mapIndexed { i, v ->
                (v + sin(i.toFloat() + phase) * (v * 0.18f)).coerceAtLeast(0f)
            }
        }
    }

    DefecationGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).background(accentColor.copy(0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(data.type.toIcon(), null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(data.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(StatusRed.copy(pulseAlpha), CircleShape))
                    Spacer(Modifier.width(3.dp))
                    Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(dynamicValue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (data.isUpwardTrend) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                    null,
                    tint = if (data.isUpwardTrend) StatusGreen else DefecationOrange,
                    modifier = Modifier.size(12.dp)
                )
                Text(" ${data.changeString} vs Yesterday", fontSize = 10.sp, color = TextGray)
            }

            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                if (dynamicTrendHistory.isNotEmpty()) {
                    val linePath = Path()
                    val fillPath = Path()
                    val stepX = size.width / (dynamicTrendHistory.size - 1).coerceAtLeast(1)
                    val maxV = dynamicTrendHistory.maxOrNull() ?: 1f
                    val minV = dynamicTrendHistory.minOrNull() ?: 0f
                    val range = (maxV - minV).coerceAtLeast(0.01f) * 1.2f
                    val yOffset = (range - (maxV - minV)) / 2f

                    var prevX = 0f
                    var prevY = size.height - (((dynamicTrendHistory[0] - minV + yOffset) / range) * size.height)

                    linePath.moveTo(prevX, prevY)
                    fillPath.moveTo(prevX, size.height)
                    fillPath.lineTo(prevX, prevY)

                    for (i in 1 until dynamicTrendHistory.size) {
                        val x = i * stepX
                        val y = size.height - (((dynamicTrendHistory[i] - minV + yOffset) / range) * size.height)
                        val cpX = (prevX + x) / 2f
                        linePath.cubicTo(cpX, prevY, cpX, y, x, y)
                        fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                        prevX = x
                        prevY = y
                    }

                    fillPath.lineTo(prevX, size.height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.30f), Color.Transparent),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                    drawPath(
                        path = linePath,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}