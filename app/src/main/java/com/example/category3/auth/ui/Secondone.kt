//package com.example.category3.auth.ui
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AttachMoney
//import androidx.compose.material.icons.filled.AutoAwesome
//import androidx.compose.material.icons.filled.Build
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.Factory
//import androidx.compose.material.icons.filled.Layers
//import androidx.compose.material.icons.filled.LinkOff
//import androidx.compose.material.icons.filled.MonitorHeart
//import androidx.compose.material.icons.filled.Power
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.Speed
//import androidx.compose.material.icons.filled.WaterDrop
//import androidx.compose.material.icons.outlined.Assessment
//import androidx.compose.material.icons.outlined.Inventory
//import androidx.compose.material.icons.outlined.Lightbulb
//import androidx.compose.material.icons.outlined.PrecisionManufacturing
//import androidx.compose.material.icons.outlined.Schedule
//import androidx.compose.material.icons.outlined.Settings
//import androidx.compose.material.icons.outlined.WarningAmber
//import androidx.compose.material.icons.rounded.Info
//import androidx.compose.material.icons.rounded.Warning
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//import kotlinx.coroutines.delay
//import kotlin.random.Random
//
//// ============================================================================
//// 🎨 PROFESSIONAL LIGHT SAAS THEME
//// ============================================================================
//val BgAppSolid = Color(0xFFF4F7FC)
//val CardBgSolid = Color(0xFFFFFFFF)
//val BorderLight = Color(0xFFE2E8F0)
//
//val TextDark = Color(0xFF0F172A)
//val TextMuted = Color(0xFF64748B)
//
//val BlueBrand = Color(0xFF2563EB)
//val BlueActual = Color(0xFF3B82F6)
//val PurplePredict = Color(0xFF8B5CF6)
//val GreenTarget = Color(0xFF10B981)
//val OrangeAlert = Color(0xFFF97316)
//val RedCritical = Color(0xFFEF4444)
//val CyanAccent = Color(0xFF06B6D4)
//
//// ============================================================================
//// 📡 DATA MODELS
//// ============================================================================
//data class ProcessStage(
//    val id: String, val name: String, val efficiency: Int,
//    val metricLabel: String, val metricValue: String, val metricUnit: String,
//    val status: String, val color: Color,
//    val actualFlow: Float, val predictedFlow: Float, val targetFlow: Float
//)
//
//data class AlertMessage(val title: String, val message: String, val isCritical: Boolean)
//
//// ============================================================================
//// 📱 MAIN DASHBOARD SCREEN
//// ============================================================================
//@Composable
//fun WorkflowDashboardScreen() {
//
//    var topKpis by remember { mutableStateOf(listOf("1,248", "31,250", "91.8%", "92%", "112", "2h 15m")) }
//    var currentEnergy by remember { mutableStateOf(495) }
//    var oee by remember { mutableStateOf(87) }
//
//    var stages by remember {
//        mutableStateOf(
//            listOf(
//                ProcessStage("01", "MILL", 92, "Juice Flow", "13.5", "T/hr", "Running", BlueActual, 12.8f, 14.2f, 15.0f),
//                ProcessStage("02", "DEFECATION", 95, "pH Level", "6.8", "", "Running", GreenTarget, 13.8f, 13.6f, 14.0f),
//                ProcessStage("03", "EVAPORATION", 72, "Steam Efficiency", "72", "%", "BOTTLENECK", OrangeAlert, 11.2f, 10.8f, 11.0f),
//                ProcessStage("04", "CLARIFICATION", 91, "Purity", "91", "%", "Running", PurplePredict, 11.7f, 11.7f, 12.5f),
//                ProcessStage("05", "CONCENTRATION", 93, "Brix", "72", "°Bx", "Running", BlueActual, 8.8f, 10.3f, 10.5f)
//            )
//        )
//    }
//
//    var activeAlert by remember { mutableStateOf<AlertMessage?>(null) }
//
//    // DATA SIMULATION LOOP (Live Updates)
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(5000)
//            stages = stages.map {
//                it.copy(
//                    efficiency = (it.efficiency + Random.nextInt(-4, 5)).coerceIn(60, 100),
//                    actualFlow = (it.actualFlow + Random.nextFloat() * 0.4f - 0.2f).coerceIn(8f, 18f)
//                )
//            }
//            currentEnergy = (currentEnergy + Random.nextInt(-5, 6)).coerceIn(450, 550)
//            oee = (oee + Random.nextInt(-1, 2)).coerceIn(0, 100)
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val alerts = listOf(
//            AlertMessage("Evaporation Bottleneck", "Steam pressure dropping below target threshold.", true),
//            AlertMessage("AI Optimization", "Mill speed adjusted automatically. Yield +1.2%.", false)
//        )
//        while (true) {
//            delay(8000)
//            activeAlert = alerts.random()
//            delay(4000)
//            activeAlert = null
//        }
//    }
//
//    // ---------------------------------------------------------
//    // 🧮 DYNAMIC BOTTLENECK AUTO-CENTERING
//    // ---------------------------------------------------------
//    val lowestStage = stages.minByOrNull { it.efficiency } ?: stages[2]
//
//    // Maintain sequential order for the non-bottleneck cards
//    val others = stages.filter { it.id != lowestStage.id }.sortedBy { it.id }
//
//    val stagePositions = remember(stages) {
//        mapOf(
//            others.getOrNull(0)?.id to 0,
//            others.getOrNull(1)?.id to 1,
//            lowestStage.id to 2, // Forces the lowest efficiency stage to the exact center
//            others.getOrNull(2)?.id to 3,
//            others.getOrNull(3)?.id to 4
//        )
//    }
//
//    // ---------------------------------------------------------
//    // 🎨 UI LAYOUT (70% / 30% Split)
//    // ---------------------------------------------------------
//    Box(modifier = Modifier.fillMaxSize().background(BgAppSolid)) {
//        Row(modifier = Modifier.fillMaxSize()) {
//            Sidebar()
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(24.dp),
//                verticalArrangement = Arrangement.spacedBy(20.dp)
//            ) {
//                // ROW 1: HEADER
//                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                    Text("AURALISS ", color = BlueBrand, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                    Text("DASHBOARD", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = Arrangement.spacedBy(20.dp)
//                ) {
//                    // ==========================================
//                    // LEFT COLUMN (70%)
//                    // ==========================================
//                    Column(
//                        modifier = Modifier.weight(0.7f).fillMaxHeight(),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        // 1. TOP KPIs
//                        Row(modifier = Modifier.fillMaxWidth().height(86.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Factory, BlueActual, "Production Today", topKpis[0] + " MT")
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.AttachMoney, GreenTarget, "Cost / Ton", "₹" + topKpis[1])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Layers, CyanAccent, "Yield", topKpis[2])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.MonitorHeart, PurplePredict, "Machine Health", topKpis[3])
//                            TopKpiCard(Modifier.weight(1f), Icons.Filled.Speed, BlueActual, "Throughput", topKpis[4])
//                            TopKpiCard(Modifier.weight(1f), Icons.Outlined.Schedule, OrangeAlert, "Downtime", topKpis[5])
//                        }
//
//                        // 2. MIDDLE AREA (Animated Overlapping Carousel + Line Chart)
//                        WhitePanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
//                            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
//
//                                // 🎴 5 OVERLAPPING CARDS (Replicating exact image proportions)
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .weight(0.6f)
//                                        .padding(bottom = 16.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    // Exact geometry to match the reference image's deep overlapping stack
//                                    val cardWidth = 150.dp
//                                    val cardHeight = 260.dp
//                                    val offsets = listOf((-210).dp, (-105).dp, 0.dp, 105.dp, 210.dp)
//                                    val scales = listOf(0.75f, 0.85f, 1.15f, 0.85f, 0.75f) // Center card pops forward
//                                    val zIndices = listOf(1f, 2f, 3f, 2f, 1f)
//
//                                    stages.forEach { stage ->
//                                        val targetPos = stagePositions[stage.id] ?: 0
//
//                                        // Smooth, premium gliding animations
//                                        val animOffset by animateDpAsState(targetValue = offsets[targetPos], animationSpec = tween(800, easing = FastOutSlowInEasing))
//                                        val animScale by animateFloatAsState(targetValue = scales[targetPos], animationSpec = tween(800, easing = FastOutSlowInEasing))
//                                        val animZIndex by animateFloatAsState(targetValue = zIndices[targetPos], animationSpec = tween(800))
//
//                                        val isCenter = targetPos == 2
//
//                                        ProcessCard(
//                                            stage = stage,
//                                            isCenter = isCenter,
//                                            modifier = Modifier
//                                                .offset(x = animOffset)
//                                                .zIndex(animZIndex)
//                                                .scale(animScale)
//                                                .width(cardWidth)
//                                                .height(cardHeight)
//                                        )
//                                    }
//                                }
//
//                                // 📈 LINE CHART
//                                TrendChartSection(stages, lowestStage.name, modifier = Modifier.weight(0.4f))
//                            }
//                        }
//
//                        // 3. BOTTOM ROW (3 Cards)
//                        Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                            OeeCard(Modifier.weight(1f), oee)
//                            EnergyCard(Modifier.weight(1f), currentEnergy)
//                            HealthCard(Modifier.weight(1f))
//                        }
//                    }
//
//                    // ==========================================
//                    // RIGHT COLUMN (30%) - DECISION CENTER
//                    // ==========================================
//                    Column(
//                        modifier = Modifier.weight(0.3f).fillMaxHeight(),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        DecisionCenterUnified(Modifier.weight(1f), lowestStage)
//                        AiGainCard(Modifier.height(120.dp))
//                    }
//                }
//            }
//        }
//
//        // ==========================================
//        // OVERLAY: ALERT TOAST
//        // ==========================================
//        Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp).zIndex(100f), contentAlignment = Alignment.TopCenter) {
//            AnimatedVisibility(
//                visible = activeAlert != null,
//                enter = slideInVertically(initialOffsetY = { -it - 50 }) + fadeIn(),
//                exit = slideOutVertically(targetOffsetY = { -it - 50 }) + fadeOut()
//            ) {
//                activeAlert?.let { alert ->
//                    Row(
//                        modifier = Modifier
//                            .shadow(16.dp, RoundedCornerShape(30.dp), spotColor = if (alert.isCritical) RedCritical else BlueBrand)
//                            .clip(RoundedCornerShape(30.dp))
//                            .background(CardBgSolid)
//                            .border(1.dp, (if (alert.isCritical) RedCritical else BlueBrand).copy(alpha = 0.3f), RoundedCornerShape(30.dp))
//                            .padding(horizontal = 24.dp, vertical = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Icon(
//                            if (alert.isCritical) Icons.Rounded.Warning else Icons.Rounded.Info,
//                            contentDescription = null,
//                            tint = if (alert.isCritical) RedCritical else BlueBrand,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Column {
//                            Text(alert.title, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                            Text(alert.message, color = TextMuted, fontSize = 12.sp)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ============================================================================
//// 🧩 UI COMPONENTS
//// ============================================================================
//
//@Composable
//fun WhitePanel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
//    Box(
//        modifier = modifier
//            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x1A000000), ambientColor = Color(0x0A000000))
//            .clip(RoundedCornerShape(16.dp))
//            .background(CardBgSolid)
//            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
//        content = content
//    )
//}
//
//@Composable
//fun Sidebar() {
//    Column(
//        modifier = Modifier.fillMaxHeight().width(80.dp).background(CardBgSolid).border(1.dp, BorderLight).padding(vertical = 32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(36.dp)
//    ) {
//        Icon(Icons.Filled.Dashboard, null, tint = BlueBrand, modifier = Modifier.size(28.dp))
//        Icon(Icons.Outlined.PrecisionManufacturing, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Inventory, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.WarningAmber, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Assessment, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Icon(Icons.Outlined.Lightbulb, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//        Spacer(modifier = Modifier.weight(1f))
//        Icon(Icons.Outlined.Settings, null, tint = TextMuted, modifier = Modifier.size(24.dp))
//    }
//}
//
//@Composable
//fun TopKpiCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, title: String, value: String) {
//    WhitePanel(modifier = modifier.fillMaxHeight()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(12.dp),
//            verticalArrangement = Arrangement.SpaceBetween,
//            horizontalAlignment = Alignment.Start
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                Box(modifier = Modifier.size(30.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
//                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
//                }
//                Text(value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//            }
//            Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 🏭 OVERLAPPING PROCESS CARD (Matches the Image perfectly)
//// ----------------------------------------------------------------------------
//@Composable
//fun ProcessCard(stage: ProcessStage, isCenter: Boolean, modifier: Modifier) {
//    val activeColor = if (isCenter) OrangeAlert else stage.color
//
//    Box(
//        modifier = modifier
//            .shadow(if(isCenter) 24.dp else 4.dp, RoundedCornerShape(20.dp), spotColor = if(isCenter) OrangeAlert.copy(alpha=0.4f) else Color.Black.copy(alpha=0.05f))
//            .clip(RoundedCornerShape(20.dp))
//            .background(CardBgSolid)
//            .border(if(isCenter) 2.dp else 1.dp, if(isCenter) OrangeAlert.copy(alpha=0.8f) else BorderLight, RoundedCornerShape(20.dp))
//            .padding(16.dp)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Top Badge
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
//                Box(modifier = Modifier.size(24.dp).background(activeColor, CircleShape), contentAlignment = Alignment.Center) {
//                    Text(stage.id, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//
//            Icon(Icons.Outlined.PrecisionManufacturing, null, tint = TextMuted.copy(alpha=0.7f), modifier = Modifier.size(48.dp))
//            Text(stage.name, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
//
//            // Circular Progress
//            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
//                Canvas(Modifier.fillMaxSize()) {
//                    drawArc(Color(0xFFF1F5F9), -90f, 360f, false, style = Stroke(6.dp.toPx()))
//                    drawArc(activeColor, -90f, (stage.efficiency / 100f) * 360f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
//                }
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("${stage.efficiency}%", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                    Text("Efficiency", color = TextMuted, fontSize = 9.sp)
//                }
//            }
//
//            // Metrics Row
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Filled.WaterDrop, null, tint = activeColor, modifier = Modifier.size(12.dp))
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(stage.metricLabel, color = TextMuted, fontSize = 10.sp)
//                }
//                Row(verticalAlignment = Alignment.Bottom) {
//                    Text(stage.metricValue, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                    if (stage.metricUnit.isNotEmpty()) Text(" ${stage.metricUnit}", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(bottom=2.dp))
//                }
//            }
//
//            // Status Pill (Bottom)
//            val statusColor = if(isCenter) RedCritical else GreenTarget
//            val statusText = if(isCenter) "BOTTLENECK" else "Running"
//            Row(
//                modifier = Modifier
//                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
//                    .padding(horizontal = 12.dp, vertical = 6.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 📈 MULTI-LINE TREND CHART
//// ----------------------------------------------------------------------------
//@Composable
//fun TrendChartSection(stages: List<ProcessStage>, bottleneckName: String, modifier: Modifier) {
//    Column(modifier = modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(16.dp)) {
//
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            Text("PRODUCTION FLOW TREND (T/hr)", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                LegendLine("Actual", BlueActual, false)
//                LegendLine("Predicted", PurplePredict, true)
//                LegendLine("Design Target", GreenTarget, true)
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Fixed Chronological ordering for the X-axis of the chart
//        val chronoStages = stages.sortedBy { it.id }
//
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val w = size.width
//            val h = size.height
//            val maxVal = 20f
//            val paddingX = 40.dp.toPx()
//            val stepX = (w - paddingX * 2) / (chronoStages.size - 1)
//
//            val ySteps = listOf(0, 5, 10, 15, 20)
//            ySteps.forEach { v ->
//                val y = h - (v / maxVal) * h
//                drawContext.canvas.nativeCanvas.drawText(v.toString(), 0f, y + 4.dp.toPx(), android.graphics.Paint().apply { color = TextMuted.toArgb(); textSize = 9.sp.toPx() })
//                drawLine(Color.LightGray.copy(alpha=0.3f), Offset(20.dp.toPx(), y), Offset(w, y), 1.dp.toPx())
//            }
//
//            // Highlight the bottleneck stage
//            val bottleneckIndex = chronoStages.indexOfFirst { it.name == bottleneckName }
//            if (bottleneckIndex != -1) {
//                val highlightX = paddingX + (bottleneckIndex * stepX)
//                drawRect(
//                    color = OrangeAlert.copy(alpha = 0.05f),
//                    topLeft = Offset(highlightX - (stepX/2), 0f),
//                    size = Size(stepX, h)
//                )
//            }
//
//            val pathActual = Path()
//            val pathPredict = Path()
//            val pathTarget = Path()
//
//            chronoStages.forEachIndexed { i, stage ->
//                val nx = paddingX + (i * stepX)
//                val ay = h - (stage.actualFlow / maxVal) * h
//                val py = h - (stage.predictedFlow / maxVal) * h
//                val ty = h - (stage.targetFlow / maxVal) * h
//
//                if (i == 0) {
//                    pathActual.moveTo(nx, ay); pathPredict.moveTo(nx, py); pathTarget.moveTo(nx, ty)
//                } else {
//                    val prevX = paddingX + ((i - 1) * stepX)
//                    val prevAy = h - (chronoStages[i - 1].actualFlow / maxVal) * h
//                    val prevPy = h - (chronoStages[i - 1].predictedFlow / maxVal) * h
//                    val prevTy = h - (chronoStages[i - 1].targetFlow / maxVal) * h
//
//                    pathActual.cubicTo(prevX + stepX/2, prevAy, prevX + stepX/2, ay, nx, ay)
//                    pathPredict.cubicTo(prevX + stepX/2, prevPy, prevX + stepX/2, py, nx, py)
//                    pathTarget.cubicTo(prevX + stepX/2, prevTy, prevX + stepX/2, ty, nx, ty)
//                }
//
//                drawCircle(CardBgSolid, 4.dp.toPx(), Offset(nx, ty))
//                drawCircle(GreenTarget, 3.dp.toPx(), Offset(nx, ty), style = Stroke(2.dp.toPx()))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.targetFlow), nx, ty - 8.dp.toPx(), android.graphics.Paint().apply { color = GreenTarget.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true })
//
//                drawCircle(CardBgSolid, 4.dp.toPx(), Offset(nx, py))
//                drawCircle(PurplePredict, 3.dp.toPx(), Offset(nx, py), style = Stroke(2.dp.toPx()))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.predictedFlow), nx, py + 16.dp.toPx(), android.graphics.Paint().apply { color = PurplePredict.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
//
//                drawCircle(BlueActual, 4.dp.toPx(), Offset(nx, ay))
//                drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", stage.actualFlow), nx, ay + 16.dp.toPx(), android.graphics.Paint().apply { color = BlueActual.toArgb(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true })
//
//                val label = stage.name.lowercase().replaceFirstChar { it.uppercase() }
//                drawContext.canvas.nativeCanvas.drawText(label, nx, h + 20.dp.toPx(), android.graphics.Paint().apply { color = TextDark.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
//            }
//
//            drawPath(pathTarget, GreenTarget, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
//            drawPath(pathPredict, PurplePredict, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
//            drawPath(pathActual, BlueActual, style = Stroke(2.dp.toPx()))
//        }
//    }
//}
//
//@Composable
//fun LegendLine(text: String, color: Color, isDashed: Boolean) {
//    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//        Canvas(modifier = Modifier.width(16.dp).height(2.dp)) {
//            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx(), pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null)
//        }
//        Text(text, color = TextDark, fontSize = 10.sp)
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 🤖 UNIFIED DECISION CENTER (Right Sidebar)
//// ----------------------------------------------------------------------------
//@Composable
//fun DecisionCenterUnified(modifier: Modifier, lowestStage: ProcessStage) {
//    WhitePanel(modifier = modifier.fillMaxWidth()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text("AURALISS AI DECISION CENTER", color = BlueBrand, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, RedCritical.copy(alpha=0.3f), RoundedCornerShape(12.dp))
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
//                    Icon(Icons.Filled.Search, null, tint = RedCritical, modifier = Modifier.size(18.dp))
//                    Column {
//                        Text("ROOT CAUSE ANALYSIS", color = RedCritical, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//                        Text("Why is production low?", color = TextMuted, fontSize = 9.sp)
//                    }
//                }
//                Column(modifier = Modifier.padding(start = 24.dp)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("↓", color = TextMuted, fontSize = 14.sp, modifier = Modifier.offset(x = (-12).dp))
//                        Text(" Low Steam Pressure (2.1 bar)", color = TextDark, fontSize = 10.sp)
//                    }
//                    Spacer(Modifier.height(8.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("↓", color = TextMuted, fontSize = 14.sp, modifier = Modifier.offset(x = (-12).dp))
//                        val causeText = if(lowestStage.name == "MILL") "Mill RPM Low" else "${lowestStage.name.lowercase().replaceFirstChar { it.uppercase() }} Throughput Low"
//                        Text(" $causeText", color = TextDark, fontSize = 10.sp)
//                    }
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, OrangeAlert.copy(alpha=0.3f), RoundedCornerShape(12.dp))
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
//                    Icon(Icons.Filled.LinkOff, null, tint = OrangeAlert, modifier = Modifier.size(18.dp))
//                    Column {
//                        Text("CRITICAL BOTTLENECK", color = OrangeAlert, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//                        Text("Where is the constraint?", color = TextMuted, fontSize = 9.sp)
//                    }
//                }
//                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                    Column {
//                        val sectionName = lowestStage.name.lowercase().replaceFirstChar { it.uppercase() }
//                        Text("$sectionName Section", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
//                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=4.dp)) {
//                            Text("Loss Impact", color = RedCritical, fontSize = 9.sp, modifier = Modifier.background(RedCritical.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp))
//                            Spacer(Modifier.width(8.dp))
//                            Text("28 MT/day", color = TextDark, fontSize = 10.sp)
//                        }
//                    }
//
//                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
//                        Canvas(Modifier.fillMaxSize()) {
//                            drawArc(Color(0xFFE2E8F0), -90f, 360f, false, style = Stroke(5.dp.toPx()))
//                            drawArc(OrangeAlert, -90f, (lowestStage.efficiency / 100f) * 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
//                        }
//                        Text("${lowestStage.efficiency}%", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
//                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
//                    .clip(RoundedCornerShape(12.dp))
//            ) {
//                Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(GreenTarget))
//
//                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
//                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Icon(Icons.Filled.Build, null, tint = GreenTarget, modifier = Modifier.size(18.dp))
//                        Column {
//                            Text("RECOMMENDED ACTION", color = GreenTarget, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//                            Text("What should we do?", color = TextMuted, fontSize = 9.sp)
//                        }
//                    }
//
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Filled.CheckCircle, null, tint = GreenTarget, modifier = Modifier.size(14.dp))
//                            Text(" Increase Steam Pressure by 0.7 bar", color = TextDark, fontSize = 11.sp, modifier = Modifier.padding(start=8.dp))
//                        }
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Filled.CheckCircle, null, tint = GreenTarget, modifier = Modifier.size(14.dp))
//                            Text(" Inspect Steam Control Valve V-12", color = TextDark, fontSize = 11.sp, modifier = Modifier.padding(start=8.dp))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AiGainCard(modifier: Modifier) {
//    WhitePanel(modifier = modifier.fillMaxWidth()) {
//        Box(Modifier.fillMaxSize().background(GreenTarget.copy(alpha=0.04f))) {
//            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Filled.AutoAwesome, null, tint = GreenTarget, modifier = Modifier.size(16.dp))
//                    Text(" AI GAIN (IMPACT)", color = GreenTarget, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start=8.dp))
//                }
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
//                    Column {
//                        Text("+4.8%", color = GreenTarget, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
//                        Text("vs Yesterday", color = TextMuted, fontSize = 10.sp)
//                    }
//                    Canvas(Modifier.width(50.dp).height(24.dp)) {
//                        val path = Path()
//                        path.moveTo(0f, size.height*0.8f); path.lineTo(size.width*0.3f, size.height*0.6f); path.lineTo(size.width*0.6f, size.height*0.2f); path.lineTo(size.width, 0f)
//                        drawPath(path, GreenTarget, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
//                    }
//                }
//                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                    Text("Savings", color = TextMuted, fontSize = 11.sp)
//                    Text("₹18,500/day", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}
//
//// ----------------------------------------------------------------------------
//// 📊 BOTTOM ROW KPIs (Less Congested Text & Padding)
//// ----------------------------------------------------------------------------
//@Composable
//fun OeeCard(modifier: Modifier, oee: Int) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp)) {
//            Text("OEE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(Modifier.fillMaxSize().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
//                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
//                    Canvas(Modifier.fillMaxSize()) {
//                        drawArc(Color(0xFFE2E8F0), -90f, 360f, false, style = Stroke(5.dp.toPx()))
//                        drawArc(BlueActual, -90f, (oee / 100f) * 360f, false, style = Stroke(5.dp.toPx(), cap=StrokeCap.Round))
//                    }
//                    Text("$oee%", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                }
//                Column(Modifier.padding(start=12.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
//                    OeeStat("Availability", "91%")
//                    OeeStat("Performance", "95%")
//                    OeeStat("Quality", "99%")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EnergyCard(modifier: Modifier, energy: Int) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
//            Text("Energy Efficiency", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Filled.Power, null, tint = BlueBrand, modifier = Modifier.size(28.dp))
//                Column(Modifier.padding(start = 12.dp)) {
//                    Text("$energy kW", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                    Text("Current Consumption", color = TextMuted, fontSize = 10.sp)
//                }
//            }
//            Text("Total Usage: 1,850 kWh", color = TextMuted, fontSize = 10.sp)
//        }
//    }
//}
//
//@Composable
//fun HealthCard(modifier: Modifier) {
//    WhitePanel(modifier = modifier) {
//        Column(Modifier.fillMaxSize().padding(12.dp)) {
//            Text("Resource Health", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            Row(Modifier.fillMaxWidth().padding(top=8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
//                Text("91%", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                Text("Healthy", color = GreenTarget, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            }
//            Box(Modifier.fillMaxWidth().padding(top=8.dp, bottom=8.dp).height(5.dp).background(Color(0xFFE2E8F0), CircleShape)) {
//                Box(Modifier.fillMaxWidth(0.91f).fillMaxHeight().background(GreenTarget, CircleShape))
//            }
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text("Next Service", color = TextMuted, fontSize = 10.sp)
//                Text("2 Days", color = TextDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//@Composable
//fun OeeStat(label: String, value: String) {
//    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//        Text(label, color = TextMuted, fontSize = 10.sp)
//        Text(value, color = GreenTarget, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//    }
//}