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
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// 📊 OPEN PAN DATA MODELS
// ============================================================================
data class OpenPanDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val efficiency: Double,
    val oee: Double,
    val equipment: List<OpenPanEquipment>,
    val connectedEquipment: List<EquipmentData>,
    val chartData: OpenPanChartData,
    val kpis: List<KpiDataOpenPan>
)

data class OpenPanEquipment(
    val id: String,
    val name: String,
    val healthValue: String,
    val statusText: String,
    val status: EquipmentStatus,
    val imageRes: Int,
    val temperature: String = ""
)

data class OpenPanChartData(
    val actualBrix: Int,
    val targetBrix: Int,
    val designBrix: Int
)

data class KpiDataOpenPan(
    val type: OpenPanKpiType,
    val title: String,
    val value: String,
    val changeString: String,
    val isUpwardTrend: Boolean,
    val trendHistory: List<Float>
)

enum class OpenPanKpiType { BRIX, TEMPERATURE, VACUUM, EVAPORATION_RATE, STEAM_CONSUMPTION }

// ============================================================================
// 🎨 COLORS
// ============================================================================
val OpenPanAccent = Color(0xFFF59E0B)
val OpenPanAccentLight = Color(0xFFFFF0D6)

fun OpenPanKpiType.toIcon(): ImageVector = when (this) {
    OpenPanKpiType.BRIX -> Icons.Outlined.Scale
    OpenPanKpiType.TEMPERATURE -> Icons.Rounded.Thermostat
    OpenPanKpiType.VACUUM -> Icons.Outlined.Speed
    OpenPanKpiType.EVAPORATION_RATE -> Icons.Outlined.WaterDrop
    OpenPanKpiType.STEAM_CONSUMPTION -> Icons.Outlined.Bolt
}

fun OpenPanKpiType.toColor(): Color = when (this) {
    OpenPanKpiType.BRIX -> OpenPanAccent
    OpenPanKpiType.TEMPERATURE -> Color(0xFFF43F5E)
    OpenPanKpiType.VACUUM -> Color(0xFF14B8A6)
    OpenPanKpiType.EVAPORATION_RATE -> Color(0xFF8B5CF6)
    OpenPanKpiType.STEAM_CONSUMPTION -> Color(0xFF22D3EE)
}

// ============================================================================
// 🧊 GLASS CARD
// ============================================================================
@Composable
fun OpenPanGreyFrostGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, shape, spotColor = Color(0xFF8A9AAB).copy(0.5f), ambientColor = Color(0xFF8A9AAB).copy(0.2f))
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
// 📱 MAIN SCREEN
// ============================================================================
@Composable
fun OpenPanDiagnosticsHubScreen(
    state: OpenPanDashboardState = MockOpenPanData.getMockState(),
    onNavigateToScreen: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val mainScrollState = rememberScrollState()

    // Global state for charts & KPIs
    var selectedEquipmentIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        // Main Content pushed completely to the left, minimal padding so app bar hovers on top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(mainScrollState)
        ) {
            OpenPanTopNavigationBar(userName = state.userName, userRole = state.userRole, onAction = {})

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OpenPanEquipmentSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        onItemSelected = { selectedEquipmentIndex = it },
                        modifier = Modifier.weight(1.8f).fillMaxHeight()
                    )
                    OpenPanOverviewSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        modifier = Modifier.weight(1.1f).fillMaxHeight()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OpenPanEquipmentSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        onItemSelected = { selectedEquipmentIndex = it },
                        modifier = Modifier.fillMaxWidth().height(550.dp)
                    )
                    OpenPanOverviewSection(
                        state = state,
                        selectedIndex = selectedEquipmentIndex,
                        modifier = Modifier.fillMaxWidth().height(550.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            OpenPanKpiRow(kpis = state.kpis, selectedIndex = selectedEquipmentIndex, isLandscape = isLandscape)
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
                activeSection = "openpan_dashboard",
                onActionSelected = onNavigateToScreen
            )
        }
    }
}

// ============================================================================
// 🌐 TOP NAV BAR
// ============================================================================
@Composable
fun OpenPanTopNavigationBar(userName: String, userRole: String, onAction: (String) -> Unit) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Log Entry", "Alerts", "Reports", "AI Insights")
    var profileMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Empty Spacer on the left so TopNav Text doesn't sit under the Radial Nav
        Spacer(Modifier.width(60.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OpenPanGreyFrostGlassCard(shape = CircleShape) {
                Box(modifier = Modifier.size(44.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = TextGray)
                }
            }
            OpenPanGreyFrostGlassCard(shape = RoundedCornerShape(24.dp)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Open Pan Section", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        OpenPanGreyFrostGlassCard(shape = RoundedCornerShape(32.dp)) {
            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                tabs.forEachIndexed { index, tab ->
                    OpenPanTopNavLink(text = tab, isActive = activeTab == index, onClick = { activeTab = index })
                }
            }
        }

        Spacer(Modifier.width(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { profileMenuExpanded = true }) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Hello, $userName", fontSize = 12.sp, color = TextGray)
                        Text(userRole, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark)
                }
                DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
                    DropdownMenuItem(text = { Text("Log Out", color = StatusRed, fontWeight = FontWeight.Bold) }, onClick = { profileMenuExpanded = false; onAction("Log Out") })
                }
            }
            OpenPanGreyFrostGlassCard(shape = CircleShape) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, null, tint = TextDark)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset((-10).dp, 10.dp)
                            .size(8.dp)
                            .background(OpenPanAccent, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun OpenPanTopNavLink(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(if (isActive) Color.White.copy(0.6f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) TextDark else TextGray
        )
    }
}

// ============================================================================
// ⚙️ OPEN PAN EQUIPMENT SECTION
// ============================================================================
@Composable
fun OpenPanEquipmentSection(state: OpenPanDashboardState, selectedIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val activeEquipment = state.equipment.getOrNull(selectedIndex)

    OpenPanGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── HEADER BLOCK ─────────────────────────────────────────────────
            // Indent the title header to avoid the floating App Bar on the left
            Column(modifier = Modifier.padding(start = 65.dp, end = 24.dp, top = 20.dp, bottom = 0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Open Pan Section", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Spacer(Modifier.width(12.dp))
                    OpenPanGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

            // ── MAIN IMAGE AREA ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = activeEquipment?.imageRes ?: R.drawable.openpan_image),
                    contentDescription = activeEquipment?.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Corner Status Cards - Moved Start/Left cards inwards
                activeEquipment?.let {
                    OpenPanFloatingStatusCard(
                        title = "${it.name} Status",
                        value = it.healthValue,
                        statusText = it.statusText,
                        statusColor = it.status.toColor(),
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 65.dp, top = 16.dp)
                    )
                }

                state.connectedEquipment.getOrNull(1)?.let { eq ->
                    OpenPanFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        isBlueIcon = eq.status == EquipmentStatus.STANDBY,
                        modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 16.dp)
                    )
                }

                state.connectedEquipment.getOrNull(0)?.let { eq ->
                    OpenPanFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 65.dp, bottom = 16.dp)
                    )
                }

                state.connectedEquipment.getOrNull(2)?.let { eq ->
                    OpenPanFloatingStatusCard(
                        title = eq.name,
                        value = eq.value,
                        statusText = eq.statusText,
                        statusColor = eq.status.toColor(),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
                    )
                }
            }

            // ── ARC CAROUSEL ─────────────────────────────────────────────────
            if (state.equipment.isNotEmpty()) {
                OpenPanBottomImageArcCarousel(
                    equipment = state.equipment,
                    selectedIndex = selectedIndex,
                    onItemSelected = onItemSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
        }
    }
}

// ============================================================================
// 🎠 ARC CAROUSEL (WITH DRAG SUPPORT)
// ============================================================================
@Composable
fun OpenPanBottomImageArcCarousel(
    equipment: List<OpenPanEquipment>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeThreshold = LocalDensity.current.run { 40.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(selectedIndex) {
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
            val bandPath = Path().apply { moveTo(0f, curveTopY); quadraticBezierTo(w / 2f, controlY, w, curveTopY); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(path = bandPath, brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.92f), Color(0xFFDDE5EF).copy(0.5f)), startY = 0f, endY = h))
            drawPath(path = Path().apply { moveTo(0f, curveTopY); quadraticBezierTo(w / 2f, controlY, w, curveTopY) }, color = BorderGray.copy(alpha = 0.7f), style = Stroke(width = 1.5.dp.toPx()))
        }

        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = 10.dp)) {
            Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
                val path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close() }
                drawPath(path, color = OpenPanAccent)
            }
        }

        val arcRadiusDp = 420f; val itemSizeActive = 68.dp; val itemSizeInactive = 44.dp
        val springSpec = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)

        equipment.forEachIndexed { index, item ->
            val diff = index - selectedIndex
            val angleRad = Math.toRadians((diff * 16f).toDouble())

            val rawX = (sin(angleRad) * arcRadiusDp).toFloat()
            val rawY = (arcRadiusDp * (1.0 - cos(angleRad))).toFloat()

            val animX by animateFloatAsState(rawX, springSpec, label = "arcX")
            val animY by animateFloatAsState(rawY, springSpec, label = "arcY")
            val animScale by animateFloatAsState(if (diff == 0) 1f else 0.72f, springSpec, label = "arcScale")
            val animAlpha by animateFloatAsState(targetValue = when { diff == 0 -> 1f; kotlin.math.abs(diff) == 1 -> 0.75f; kotlin.math.abs(diff) == 2 -> 0.45f; else -> 0f }, animationSpec = springSpec, label = "arcAlpha")

            val zIndex = if (diff == 0) 10f else (8f - kotlin.math.abs(diff).toFloat())

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = animX.dp, y = (animY - arcRadiusDp * 0.003f).dp + 4.dp)
                    .zIndex(zIndex).scale(animScale).alpha(animAlpha)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                val ringColor = if (diff == 0) OpenPanAccent else BorderGray
                val itemSize = if (diff == 0) itemSizeActive else itemSizeInactive

                Box(
                    modifier = Modifier.size(itemSize).shadow(if (diff == 0) 16.dp else 4.dp, CircleShape)
                        .background(Color.White, CircleShape).border(if (diff == 0) 3.dp else 1.dp, ringColor, CircleShape).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = item.imageRes), contentDescription = item.name, modifier = Modifier.fillMaxSize().padding(if (diff == 0) 8.dp else 6.dp), contentScale = ContentScale.Inside)
                }

                if (diff == 0) {
                    Box(modifier = Modifier.offset(y = (itemSizeActive / 2 + 6.dp)).background(Color.White.copy(0.95f), RoundedCornerShape(10.dp)).border(1.dp, OpenPanAccent.copy(0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text(item.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        }
    }
}

// ============================================================================
// FLOATING STATUS CARD
// ============================================================================
@Composable
fun OpenPanFloatingStatusCard(
    title: String, value: String, statusText: String, statusColor: Color,
    modifier: Modifier = Modifier, isBlueIcon: Boolean = false
) {
    OpenPanGreyFrostGlassCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(120.dp)) {
                Column {
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Text(statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(26.dp).background(statusColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(if (isBlueIcon) Icons.Rounded.Pause else Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ============================================================================
// 📊 OVERVIEW SECTION (Right Panel — Dynamic Charts)
// ============================================================================
@Composable
fun OpenPanOverviewSection(state: OpenPanDashboardState, selectedIndex: Int, modifier: Modifier = Modifier) {

    // Dynamic values based on selected equipment index
    val capacityFloat = state.chartData.designBrix.toFloat()
    val targetFloat = state.chartData.targetBrix.toFloat()
    val baseActual = state.chartData.actualBrix.toFloat()

    val dynamicActual = (baseActual + (selectedIndex * 1.5f)).coerceIn(0f, capacityFloat)
    val formattedActualStr = ((dynamicActual * 10).roundToInt() / 10f).toString()

    val baseEff = state.efficiency.toString().toFloatOrNull() ?: 92f
    val dynamicEff = (baseEff - (selectedIndex * 1.3f)).coerceIn(0f, 100f)
    val formattedEff = ((dynamicEff * 10).roundToInt() / 10f).toString()

    val baseOee = state.oee.toString().toFloatOrNull() ?: 88f
    val dynamicOee = (baseOee - (selectedIndex * 1.8f)).coerceIn(0f, 100f)
    val formattedOee = ((dynamicOee * 10).roundToInt() / 10f).toString()

    // Animate the actual Arc Gauge Sweep
    val brixSweepTarget = (dynamicActual / capacityFloat) * 180f
    val animatedBrixSweep by animateFloatAsState(
        targetValue = brixSweepTarget,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "BrixSweep"
    )

    // Generate historical points syncing with dynamicEff & dynamicOee
    val effPoints = remember(dynamicEff, selectedIndex) {
        List(12) { i -> if (i == 11) dynamicEff else (dynamicEff + sin(i.toFloat() + selectedIndex) * 5f - 2f).coerceIn(60f, 100f) }
    }
    val oeePoints = remember(dynamicOee, selectedIndex) {
        List(12) { i -> if (i == 11) dynamicOee else (dynamicOee + cos(i.toFloat() + selectedIndex) * 6f - 3f).coerceIn(60f, 100f) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // --- 1. GAUGE CHART CARD ---
        OpenPanGreyFrostGlassCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Brix Concentration Analytics", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    OpenPanGreyFrostGlassCard(shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Real-Time", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
                        val textMeasurer = rememberTextMeasurer()
                        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp, end = 12.dp)) {
                            val w = size.width; val h = size.height
                            val cx = w / 2f; val cy = h - 10.dp.toPx()
                            val rOuter = minOf(w / 2f, h) * 0.95f
                            val rMid = rOuter * 0.72f; val rInner = rOuter * 0.45f

                            val targetSweep = (targetFloat / capacityFloat) * 180f
                            val actualColor = if (dynamicActual < targetFloat * 0.9f) StatusRed else OpenPanAccent

                            drawArc(OpenPanAccentLight, 180f, 180f, true, Offset(cx - rOuter, cy - rOuter), Size(rOuter * 2, rOuter * 2))
                            drawArc(Color(0xFFFB923C).copy(0.3f), 180f, targetSweep, true, Offset(cx - rMid, cy - rMid), Size(rMid * 2, rMid * 2))
                            drawArc(actualColor, 180f, animatedBrixSweep, true, Offset(cx - rInner, cy - rInner), Size(rInner * 2, rInner * 2))

                            val dot = 4.dp.toPx()
                            drawCircle(OpenPanAccent.copy(alpha = 0.5f), dot, Offset(cx, cy - rOuter))
                            drawCircle(Color(0xFFFB923C), dot, Offset(cx, cy - rMid))
                            drawCircle(actualColor, dot, Offset(cx, cy - rInner))

                            val lbl = TextStyle(fontSize = 10.sp, color = TextDark, fontWeight = FontWeight.Bold)

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${state.chartData.designBrix}°Bx",
                                topLeft = Offset(cx - textMeasurer.measure("${state.chartData.designBrix}°Bx", lbl).size.width / 2f, cy - rOuter - 20f),
                                style = lbl
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${state.chartData.targetBrix}°Bx",
                                topLeft = Offset(cx - textMeasurer.measure("${state.chartData.targetBrix}°Bx", lbl).size.width / 2f, cy - rMid - 20f),
                                style = lbl.copy(color = Color(0xFFFB923C))
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "$formattedActualStr°Bx",
                                topLeft = Offset(cx - textMeasurer.measure("$formattedActualStr°Bx", lbl).size.width / 2f, cy - rInner - 20f),
                                style = lbl.copy(color = actualColor)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        val activeColor = if (dynamicActual < targetFloat * 0.9f) StatusRed else OpenPanAccent
                        OpenPanLegendItem("Actual Brix", "$formattedActualStr° Brix", activeColor)
                        Spacer(Modifier.height(14.dp))
                        OpenPanLegendItem("Target Brix", "${state.chartData.targetBrix}° Brix", Color(0xFFFB923C))
                        Spacer(Modifier.height(14.dp))
                        OpenPanLegendItem("Design Max", "${state.chartData.designBrix}° Brix", OpenPanAccentLight)
                    }
                }
            }
        }

        // --- 2. PERFORMANCE HISTORY CHART CARD ---
        OpenPanGreyFrostGlassCard(modifier = Modifier.fillMaxWidth().weight(1.3f)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Performance History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.sectionStatus.name, fontSize = 13.sp, color = state.sectionStatus.toColor(), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(9.dp).background(state.sectionStatus.toColor(), CircleShape))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(StatusGreen, CircleShape)); Spacer(Modifier.width(6.dp)); Text("Efficiency", fontSize = 12.sp, color = TextGray) }
                        Text("$formattedEff%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.padding(start = 14.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(Color(0xFF47A1F2), CircleShape)); Spacer(Modifier.width(6.dp)); Text("OEE", fontSize = 12.sp, color = TextGray) }
                        Text("$formattedOee%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.padding(start = 14.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Line Chart matches Eff and OEE points
                val textMeasurer = rememberTextMeasurer()
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val chartMin = 60f
                    val chartMax = 100f
                    val chartRange = chartMax - chartMin

                    // Gridlines
                    val yLabels = listOf(100, 80, 60)
                    yLabels.forEach { value ->
                        val normalizedY = 1f - ((value - chartMin) / chartRange)
                        val yPos = normalizedY * size.height
                        drawLine(BorderGray.copy(alpha = 0.4f), Offset(25.dp.toPx(), yPos), Offset(size.width, yPos), 1.dp.toPx())
                        drawText(
                            textMeasurer = textMeasurer,
                            text = value.toString(),
                            topLeft = Offset(0f, yPos - 6.dp.toPx()),
                            style = TextStyle(fontSize = 10.sp, color = TextGray)
                        )
                    }

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
                            drawCircle(Color.White, 5.dp.toPx(), Offset(prevX, prevY))
                            drawCircle(color, 3.dp.toPx(), Offset(prevX, prevY))
                        }
                    }

                    // Draw OEE Line (Blue)
                    drawMetricLine(oeePoints, Color(0xFF47A1F2), isFill = true)
                    drawMetricLine(oeePoints, Color(0xFF47A1F2), isFill = false)

                    // Draw Efficiency Line (Green)
                    drawMetricLine(effPoints, StatusGreen, isFill = true)
                    drawMetricLine(effPoints, StatusGreen, isFill = false)
                }
            }
        }
    }
}

@Composable
fun OpenPanLegendItem(label: String, value: String, color: Color) {
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
// 📈 BOTTOM KPI ROW WITH LIVE BADGE (DYNAMIC)
// ============================================================================
@Composable
fun OpenPanKpiRow(kpis: List<KpiDataOpenPan>, selectedIndex: Int, isLandscape: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (!isLandscape) it.horizontalScroll(rememberScrollState()) else it }
            .padding(start = 65.dp), // Clears the floating App Bar
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        kpis.forEachIndexed { index, kpi ->
            OpenPanKpiCard(
                modifier = if (isLandscape) Modifier.weight(1f) else Modifier.width(220.dp),
                data = kpi,
                selectedIndex = selectedIndex * 3 + index
            )
        }
    }
}

@Composable
fun OpenPanKpiCard(modifier: Modifier, data: KpiDataOpenPan, selectedIndex: Int) {
    val accentColor = data.type.toColor()
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )

    // Synthesize KPI numeric values realistically based on selected part
    val numVal = data.value.filter { it.isDigit() || it == '.' }.toString().toFloatOrNull()
    val dynamicValueStr = if (numVal != null && selectedIndex != 0) {
        val newVal = numVal * (1f + (selectedIndex * 0.04f))
        val formatted = ((newVal * 10f).roundToInt() / 10f).toString()
        val suffix = data.value.filterNot { it.isDigit() || it == '.' }
        formatted + suffix
    } else data.value

    // Smooth bezier array alterations
    val dynamicTrendHistory = remember(data.trendHistory, selectedIndex) {
        if (selectedIndex == 0) data.trendHistory
        else {
            val phaseShift = selectedIndex * 1.5f
            data.trendHistory.mapIndexed { i, v -> (v + sin(i.toFloat() + phaseShift) * (v * 0.2f)).coerceAtLeast(0f) }
        }
    }

    OpenPanGreyFrostGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(30.dp).background(accentColor.copy(0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
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
            Text(dynamicValueStr, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (data.isUpwardTrend) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                    null,
                    tint = if (data.isUpwardTrend) StatusGreen else OpenPanAccent,
                    modifier = Modifier.size(12.dp)
                )
                Text(" ${data.changeString} vs baseline", fontSize = 10.sp, color = TextGray)
            }
            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                if (dynamicTrendHistory.isNotEmpty()) {
                    val linePath = Path()
                    val fillPath = Path()
                    val stepX = size.width / (dynamicTrendHistory.size - 1).coerceAtLeast(1)
                    val minV = dynamicTrendHistory.minOrNull() ?: 0f
                    val maxV = dynamicTrendHistory.maxOrNull() ?: 1f
                    val range = (maxV - minV).coerceAtLeast(0.001f) * 1.2f
                    val yOffset = (range - (maxV - minV)) / 2f

                    var prevX = 0f
                    var prevY = size.height - (((dynamicTrendHistory[0] - minV + yOffset) / range) * size.height)
                    linePath.moveTo(prevX, prevY)
                    fillPath.moveTo(prevX, size.height)
                    fillPath.lineTo(prevX, prevY)

                    for (i in 1 until dynamicTrendHistory.size) {
                        val x = i * stepX
                        val y = size.height - (((dynamicTrendHistory[i] - minV + yOffset) / range) * size.height)
                        val controlPointX = (prevX + x) / 2f
                        linePath.cubicTo(controlPointX, prevY, controlPointX, y, x, y)
                        fillPath.cubicTo(controlPointX, prevY, controlPointX, y, x, y)
                        prevX = x; prevY = y
                    }
                    fillPath.lineTo(prevX, size.height)
                    fillPath.close()

                    drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent), startY = 0f, endY = size.height))
                    drawPath(path = linePath, color = accentColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }
}

// ============================================================================
// 📡 MOCK DATA
// ============================================================================
object MockOpenPanData {
    fun getMockState() = OpenPanDashboardState(
        userName = "Production",
        userRole = "Boiling Supervisor",
        batchId = "OP-2026-B9",
        startTime = "08:30 AM",
        sectionStatus = EquipmentStatus.HEALTHY,
        efficiency = 93.8,
        oee = 88.9,
        equipment = listOf(
            OpenPanEquipment("op1", "Open Pan 1", "94.2°Bx", "Running", EquipmentStatus.RUNNING, R.drawable.openpan_image, "118°C"),
            OpenPanEquipment("op2", "Open Pan 2", "87.6°Bx", "Running", EquipmentStatus.RUNNING, R.drawable.openpan_image, "112°C"),
            OpenPanEquipment("vp1", "Vacuum Pan", "Standby", "Ready", EquipmentStatus.STANDBY, R.drawable.openpan_image),
            OpenPanEquipment("ev1", "Calandria", "91.3°Bx", "Healthy", EquipmentStatus.HEALTHY, R.drawable.openpan_image),
            OpenPanEquipment("cd1", "Condenser", "98%", "Running", EquipmentStatus.RUNNING, R.drawable.openpan_image)
        ),
        connectedEquipment = listOf(
            EquipmentData("Steam Header", "2.1 kg/cm²", "Stable", EquipmentStatus.RUNNING),
            EquipmentData("Coconut Oil", "150 ml", "Added", EquipmentStatus.HEALTHY),
            EquipmentData("Soda Ash", "50 g", "Added", EquipmentStatus.HEALTHY)
        ),
        chartData = OpenPanChartData(94, 96, 98),
        kpis = listOf(
            KpiDataOpenPan(OpenPanKpiType.BRIX, "Current Brix", "94.2°Bx", "1.8%", true, listOf(88f, 90f, 91f, 92.5f, 93.8f, 94.2f)),
            KpiDataOpenPan(OpenPanKpiType.TEMPERATURE, "Pan Temperature", "118°C", "2.4%", false, listOf(122f, 120f, 119f, 118.5f, 118f)),
            KpiDataOpenPan(OpenPanKpiType.VACUUM, "Vacuum Level", "680 mmHg", "0.8%", true, listOf(650f, 660f, 670f, 675f, 680f)),
            KpiDataOpenPan(OpenPanKpiType.EVAPORATION_RATE, "Evaporation Rate", "285 kg/hr", "4.2%", true, listOf(260f, 270f, 275f, 280f, 285f)),
            KpiDataOpenPan(OpenPanKpiType.STEAM_CONSUMPTION, "Steam Economy", "2.8 kg/kg", "3.1%", false, listOf(3.1f, 3.0f, 2.9f, 2.85f, 2.8f))
        )
    )
}