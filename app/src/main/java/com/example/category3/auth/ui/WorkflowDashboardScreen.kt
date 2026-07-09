package com.example.category3.auth.ui
import android.content.res.Configuration
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.category3.R
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val BrandDeepNavy     = Color(0xFF0A0D2F)
val BrandDarkBlueGray = Color(0xFF223B57)
val BrandSteelGray    = Color(0xFF8C929C)
val BrandLightGray    = Color(0xFFBCBCBF)
val BrandOffWhite     = Color(0xFFF6F6F7)
val BrandCyanBlue     = Color(0xFF47B3E2)
val BrandMutedBlue    = Color(0xFF496D89)
val BrandTeal         = Color(0xFF11CFC9)
val BrandOrange       = Color(0xFFF68420)
val BrandSoftOrange   = Color(0xFFD68A51)

// New App Background
val WarmWhite         = Color(0xFFFDFBF7)

val AccentPrimary  = BrandCyanBlue
val AccentSuccess  = BrandTeal
val AccentWarning  = BrandSoftOrange
val AccentCritical = BrandOrange
val AccentAI       = BrandMutedBlue

data class DashboardTheme(
    val isDark: Boolean,
    val textMain: Color,
    val textMuted: Color,
    val textLightMuted: Color,
    val trackBg: Color
)

fun getAdaptiveTheme(isDark: Boolean): DashboardTheme =
    if (isDark) {
        DashboardTheme(true, BrandOffWhite, BrandLightGray, BrandSteelGray, BrandDeepNavy.copy(alpha = 0.5f))
    } else {
        DashboardTheme(false, BrandDeepNavy, BrandMutedBlue, BrandSteelGray, BrandLightGray.copy(alpha = 0.3f))
    }

fun getDedicatedRouteForStage(stageId: String): String = when (stageId) {
    "01" -> AppDestinations.MILL_DEDICATED
    "02" -> AppDestinations.DEFECATOR_DEDICATED
    "03" -> AppDestinations.CONCENTRATION_DEDICATED
    "04" -> AppDestinations.FLOTATION_CLARIFIER_DEDICATED
    "05" -> AppDestinations.VACUUM_PAN_DEDICATED
    else -> AppDestinations.MILL_DEDICATED
}

data class StageAlertSummary(val critical: Int, val warning: Int)

data class StageCardDisplay(
    val primaryLabel: String, val primaryValue: String, val primaryUnit: String,
    val stat1Label: String, val stat1Value: String, val stat2Label: String,
    val stat2Value: String, val stat3Label: String, val stat3Value: String,
    val efficiency: Int, val uptime: Float, val loadPercent: Int
)

fun stageCardDisplay(stage: LiveStageData): StageCardDisplay = when (stage.id) {
    "01" -> StageCardDisplay(
        "THROUGHPUT", String.format("%,.0f", stage.actualFlow), "kg/hr",
        "MOTOR", "${stage.energyKw.toInt()}A", "RPM", "${stage.vibrationHz.toInt()}", "TEMP", "${stage.tempC.toInt()}°C",
        efficiency = stage.efficiency, uptime = 97.2f, loadPercent = 78
    )
    "02" -> StageCardDisplay(
        "pH LEVEL", String.format("%.2f", stage.pressureBar), "pH",
        "DJ TEMP", "${stage.tempC.toInt()}°C", "TANK", "${stage.tankFillPercent}%", "PUMP", "${stage.vibrationHz.toInt()}A",
        efficiency = stage.efficiency, uptime = 94.8f, loadPercent = 65
    )
    "03" -> StageCardDisplay(
        "EVAP FLOW", String.format("%.1f", stage.actualFlow), "m³/hr",
        "TEMP B1", "${stage.tempC.toInt()}°C", "PRESS", String.format("%.2f", stage.pressureBar), "BRIX", "${stage.efficiency}°Bx",
        efficiency = stage.efficiency, uptime = 91.5f, loadPercent = 88
    )
    "04" -> StageCardDisplay(
        "CJ FLOW", String.format("%.2f", stage.actualFlow), "L/hr",
        "TEMP", "${stage.tempC.toInt()}°C", "TANK", "${stage.tankFillPercent}%", "FC VFD", "${stage.vibrationHz.toInt()}%",
        efficiency = stage.efficiency, uptime = 96.1f, loadPercent = 72
    )
    "05" -> StageCardDisplay(
        "SYRUP FLOW", String.format("%,.0f", stage.actualFlow), "L/hr",
        "PAN TEMP", "${stage.tempC.toInt()}°C", "VACUUM", "${stage.pressureBar.toInt()} mmHg", "RPM", "${stage.vibrationHz.toInt()}",
        efficiency = stage.efficiency, uptime = 89.3f, loadPercent = 92
    )
    else -> StageCardDisplay(
        "FLOW", String.format("%,.0f", stage.actualFlow), "u/hr",
        "TEMP", "${stage.tempC.toInt()}°C", "VIB", "${stage.vibrationHz.toInt()}Hz", "PWR", "${stage.energyKw.toInt()}kW",
        efficiency = stage.efficiency, uptime = 95.0f, loadPercent = 70
    )
}

enum class AyamMood { NORMAL, WARNING, CRITICAL }

@RawRes
fun ayamVideoForMood(mood: AyamMood): Int = when (mood) {
    AyamMood.NORMAL -> R.raw.ayam_happy
    AyamMood.WARNING -> R.raw.ayam_sad
    AyamMood.CRITICAL -> R.raw.ayam_angry
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun AyamVideoAvatar(
    modifier: Modifier = Modifier,
    @RawRes videoResId: Int
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = 0f
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    LaunchedEffect(videoResId) {
        val uri = Uri.parse("android.resource://${context.packageName}/$videoResId")
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.play()
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.avatar_player_view, null, false)
            val playerView = view as PlayerView
            playerView.apply {
                useController = false
                player = exoPlayer
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                (videoSurfaceView as? android.view.TextureView)?.isOpaque = false
                setKeepContentOnPlayerReset(true)
            }
            playerView
        },
        update = { view -> view.player = exoPlayer }
    )
}

// ─── SYSTEM INSIGHTS DATA ───────────────────────────────────────────────────

data class SystemInsight(
    val id: String,
    val stage: String,
    val category: InsightCategory,
    val title: String,
    val problem: String,
    val rootCause: String,
    val impact: String,
    val severity: InsightSeverity,
    val metric: String,
    val metricValue: String,
    val trend: InsightTrend
)

enum class InsightCategory { EFFICIENCY, ENERGY, QUALITY, MAINTENANCE, PROCESS }
enum class InsightSeverity { INFO, WARNING, CRITICAL }
enum class InsightTrend { UP, DOWN, STABLE }

val systemInsightsData: List<SystemInsight> = listOf(
    SystemInsight("INS-01-001","MILLING", InsightCategory.EFFICIENCY,"Mill Throughput Below Target",
        "Current mill throughput has dropped 12% below the daily target of 6,500 kg/hr over the past 3 hours.",
        "Cane feed rate inconsistency detected at Carrier #2. Hydraulic pressure fluctuation (±18 bar) is causing intermittent feed stoppages, reducing crushing efficiency.",
        "Estimated 780 kg/hr production loss. If sustained, daily TCD target will be missed by ~8%.",
        InsightSeverity.WARNING,"Throughput","5,712 kg/hr", InsightTrend.DOWN),
    SystemInsight("INS-01-002","MILLING", InsightCategory.ENERGY,"Motor Draw Exceeding Nominal",
        "Mill Motor #3 drawing 94A against nominal 82A — 14.6% overcurrent sustained for 47 minutes.",
        "Trash plate gap has narrowed to 1.1mm vs recommended 1.8mm, causing fibrous buildup between rollers. This creates mechanical resistance that the motor compensates for with higher current draw.",
        "Accelerated motor insulation degradation. MTBF reduced by est. 340 hours. Energy cost overhead ~₹2,100/hr.",
        InsightSeverity.CRITICAL,"Motor Current","94A / 82A", InsightTrend.UP),
    SystemInsight("INS-01-003","MILLING", InsightCategory.MAINTENANCE,"Roller Bearing Vibration Elevated",
        "Vibration sensor on Top Roller Bearing reads 8.7 mm/s RMS — threshold is 6.5 mm/s RMS.",
        "Roller bearing lubrication interval was last completed 312 hours ago, exceeding the 280-hour service cycle. Oil viscosity degradation confirmed by inline sensor at 42 cSt (optimal: 68–74 cSt).",
        "Risk of bearing seizure within estimated 18–24 hours of continued operation at current load. Unplanned downtime cost estimated at ₹4.8L per hour.",
        InsightSeverity.CRITICAL,"Vibration","8.7 mm/s", InsightTrend.UP),
    SystemInsight("INS-01-004","MILLING", InsightCategory.PROCESS,"Imbibition Water Ratio Off-Spec",
        "Imbibition water ratio measured at 18.2% of cane — target range is 20–25%.",
        "Flow control valve FCV-104 is operating at only 67% of commanded position due to actuator hysteresis. The actuator has not been recalibrated since the last annual shutdown 11 months ago.",
        "Reduced juice extraction efficiency. Estimated pol loss to bagasse increased by 0.4%, equivalent to ~120 kg sucrose per hour unrecovered.",
        InsightSeverity.WARNING,"Imbibition Ratio","18.2%", InsightTrend.DOWN),
    SystemInsight("INS-01-005","MILLING", InsightCategory.QUALITY,"Bagasse Moisture Above Limit",
        "Bagasse moisture at Mill #4 exit is 53.1% — boiler fuel specification requires ≤52%.",
        "Combination of low fiber content in this season's cane variety (11.8% vs 13.2% historical average) and the imbibition over-application window between 06:00–08:00 this morning increased moisture retention.",
        "Boiler combustion efficiency reduced. Supplementary fuel consumption (HFO) increased by 18% to maintain steam pressure. Additional fuel cost ~₹3,400/hr.",
        InsightSeverity.WARNING,"Bagasse Moisture","53.1%", InsightTrend.UP),
    SystemInsight("INS-01-006","MILLING", InsightCategory.EFFICIENCY,"Pol % in Juice Declining",
        "Expressed juice Pol has decreased from 18.4% to 16.9% over the last 6-hour window.",
        "Incoming cane brix has dropped — likely due to mixing of older stock (>24hr post-harvest) from Yard Zone C with fresh cane. Sucrose inversion rate in standing cane increases ~0.05% per hour after cutting.",
        "Recovery efficiency drop of ~1.5 Pol units directly reduces sugar yield. Projected daily sugar output reduced by approximately 2.1 MT.",
        InsightSeverity.WARNING,"Juice Pol%","16.9%", InsightTrend.DOWN),

    SystemInsight("INS-02-001","JUICE TREATMENT", InsightCategory.PROCESS,"pH Overcorrection Detected",
        "Juice pH oscillating between 7.4 and 8.6 over 20-minute cycles — target is a stable 7.0–7.2 for optimal clarification.",
        "Milk of lime dosing PID controller (PIC-201) has integral windup due to the lime slurry density varying between 14–19 Bé. The density sensor DS-201 has a 4-minute response lag which destabilizes the closed-loop control.",
        "pH excursions above 8.2 cause phosphate precipitation inefficiency and increase color formation (color units +240 IU measured). Excess lime also increases mud volume by ~12%.",
        InsightSeverity.WARNING,"pH Swing","7.4 – 8.6", InsightTrend.STABLE),
    SystemInsight("INS-02-002","JUICE TREATMENT", InsightCategory.QUALITY,"Clarified Juice Turbidity Elevated",
        "Clarified juice turbidity reading 420 NTU — specification requires <180 NTU for downstream evaporation.",
        "Clarifier rake mechanism is running at 1.8 RPM vs recommended 2.4 RPM. Torque limiter trip at 06:42 caused a 14-minute rake stall, allowing mud blanket to re-suspend into the clear juice zone.",
        "Suspended solids carry-over to evaporators will accelerate scale formation on heating surfaces. Estimated cleaning cycle frequency increases from 7-day to 4-day intervals, consuming 18 hours of downtime per event.",
        InsightSeverity.CRITICAL,"Turbidity","420 NTU", InsightTrend.UP),
    SystemInsight("INS-02-003","JUICE TREATMENT", InsightCategory.ENERGY,"Flash Tank Thermal Loss",
        "Flash tank #2 vent temperature is 108°C — indicating live steam bypassing the condensate recovery path.",
        "Float valve FV-208 in the flash tank condensate trap has failed open. This allows flash steam (representing ~8% of total steam input) to vent to atmosphere instead of being recovered for juice heating preheating duty.",
        "Direct steam loss equivalent to ~1.4 t/hr. Annual energy value of this loss: approximately ₹68L at current steam cost. Also reduces pre-heater inlet temperature by 6°C, increasing main heater steam consumption.",
        InsightSeverity.WARNING,"Vent Temp","108°C", InsightTrend.UP),
    SystemInsight("INS-02-004","JUICE TREATMENT", InsightCategory.PROCESS,"Phosphoric Acid Dosing Interrupted",
        "P₂O₅ concentration in treated juice dropped to 180 ppm from target 250–300 ppm for the past 90 minutes.",
        "Phosphoric acid day tank level dropped below pump suction minimum at 09:15. Tank refill valve UV-214 failed to open on low-level signal — field investigation confirms solenoid coil failure.",
        "Insufficient phosphate reduces flocculation quality. Estimated clarification efficiency reduced by 22%. Mud recirculation to clarifier increased from 8% to 14% of juice volume.",
        InsightSeverity.CRITICAL,"P₂O₅ Level","180 ppm", InsightTrend.DOWN),
    SystemInsight("INS-02-005","JUICE TREATMENT", InsightCategory.MAINTENANCE,"Juice Heater #1 Fouling Detected",
        "LMTD efficiency of Juice Heater JH-101 has degraded from 94% (clean baseline) to 71% over 6 operating days.",
        "Calcium phosphate and organic deposits building on tube surfaces due to the pH excursion events. Deposit layer estimated at 0.4mm based on heat transfer coefficient calculation (U dropped from 1,840 to 1,305 W/m²K).",
        "Steam consumption for juice heating increased by 19%. Juice outlet temperature 4°C below target, reducing downstream clarification kinetics and extending clarifier residence time.",
        InsightSeverity.WARNING,"Heater Efficiency","71%", InsightTrend.DOWN),
    SystemInsight("INS-02-006","JUICE TREATMENT", InsightCategory.QUALITY,"Mud Filter Cake Moisture High",
        "Rotary vacuum filter cake moisture averaging 68.4% — target is <62% for efficient pol recovery.",
        "Vacuum pump VP-301 running at 380 mbar absolute vs design 280 mbar. Root cause traced to air ingress at the rotary drum end seals, reducing effective vacuum. Seal wear confirmed by maintenance in last inspection.",
        "High moisture cake means increased pol loss to press water and mud. Estimated sucrose in mud: 2.8% vs target 1.5%. Approximately 41 kg/hr of recoverable sucrose being discarded.",
        InsightSeverity.WARNING,"Cake Moisture","68.4%", InsightTrend.UP),

    SystemInsight("INS-03-001","EVAPORATION", InsightCategory.ENERGY,"Steam Economy Below Design",
        "Quintuple effect steam economy is 4.1 kg water evaporated per kg steam — design target is 4.8.",
        "Effect III and Effect IV have significant scale buildup (confirmed by temperature profile analysis: ΔT deviation of +8°C and +11°C respectively). Scale thermal resistance reduces effective heat transfer area by ~18%.",
        "Excess live steam consumption: ~2.8 t/hr above target. This directly reduces power generation capacity at the cogeneration turbine by approximately 1.2 MW. Energy cost impact: ₹8,400/hr.",
        InsightSeverity.CRITICAL,"Steam Economy","4.1 kg/kg", InsightTrend.DOWN),
    SystemInsight("INS-03-002","EVAPORATION", InsightCategory.PROCESS,"Syrup Brix Out of Range",
        "Final syrup brix exiting Effect V is 58.2°Bx — target for crystallization is 60–65°Bx.",
        "Effect V vapor line temperature controller TIC-512 is in manual mode since the auto-control failed during a steam hammer event at 04:30. Operator has set fixed steam pressure resulting in under-concentration.",
        "Low brix syrup increases pan boiling time per batch by approximately 22 minutes. Crystallizer loading increases, and massecuite viscosity at crystallization temperature is sub-optimal, reducing crystal yield.",
        InsightSeverity.WARNING,"Syrup Brix","58.2°Bx", InsightTrend.DOWN),
    SystemInsight("INS-03-003","EVAPORATION", InsightCategory.MAINTENANCE,"Effect II Non-Condensable Buildup",
        "Effect II vapor space temperature is 2.8°C below expected saturation temperature for operating pressure, indicating non-condensable gas accumulation.",
        "Vent condenser exhaust valve UV-342 auto-purge cycle frequency reduced from every 4 hours to every 12 hours after a controller parameter change during last shift. Non-condensables (CO₂, air ingress) blanketing tube surfaces.",
        "Effective heat transfer coefficient in Effect II reduced by ~14%. This cascades through the multiple-effect system, reducing total evaporation capacity by ~6%. Estimated production rate impact: -180 m³/hr juice throughput.",
        InsightSeverity.WARNING,"Temp Deviation","-2.8°C", InsightTrend.STABLE),
    SystemInsight("INS-03-004","EVAPORATION", InsightCategory.QUALITY,"Color Increase in Syrup",
        "Syrup color measured at 3,840 IU (ICUMSA) — target for white sugar production is <2,800 IU.",
        "Extended residence time in Effect III due to reduced throughput. High temperature + extended time promotes Maillard reactions and caramelization. Effect III operating at 118°C vs design 112°C.",
        "Elevated color in syrup propagates to crystallization. Additional decolorization chemical dosing required (+35% activated carbon consumption). Final sugar color risk of exceeding 150 IU ICUMSA spec.",
        InsightSeverity.WARNING,"Syrup Color","3,840 IU", InsightTrend.UP),
    SystemInsight("INS-03-005","EVAPORATION", InsightCategory.PROCESS,"Condensate Contamination Alert",
        "Vapor condensate conductivity from Effect I is 680 µS/cm — process water limit is 50 µS/cm.",
        "Tube bundle leak detected in Effect I shell. Sugar juice is entering the condensate side through a pinhole failure in tube #47. Juice brix in condensate estimated at 0.08°Bx.",
        "Contaminated condensate cannot be returned to the boiler feed water system. Forced to dump ~18 m³/hr of condensate to drain — loss of treated water and heat energy. Boiler make-up water consumption increased by 22 m³/hr.",
        InsightSeverity.CRITICAL,"Condensate Cond.","680 µS/cm", InsightTrend.UP),
    SystemInsight("INS-03-006","EVAPORATION", InsightCategory.EFFICIENCY,"Juice Feed Flow Fluctuation",
        "Juice feed to Effect I varying ±340 L/hr around setpoint over 8-minute cycles.",
        "Interaction between the level controller LIC-501 (Effect I) and the clarified juice buffer tank level controller LIC-221 creating a process-to-process oscillation loop. Both controllers have similar integral time constants (Ti = 8 min) causing resonance.",
        "Unsteady feed causes vapor generation fluctuations, which propagate through all five effects. Downstream syrup brix variation ±2.1°Bx complicates pan boiling control.",
        InsightSeverity.INFO,"Feed Flow Variation","±340 L/hr", InsightTrend.STABLE),

    SystemInsight("INS-04-001","CLARIFICATION", InsightCategory.PROCESS,"CJ Flow Rate Below Setpoint",
        "Clarified juice flow to evaporators running at 82% of setpoint (4,100 vs 5,000 L/hr) for the past 2 hours.",
        "Clarifier underflow pump CP-401 showing reduced performance — discharge pressure 2.1 bar vs design 3.4 bar. Impeller wear confirmed by performance curve analysis. Pump last serviced 4,200 hours ago (interval: 3,500 hours).",
        "Reduced clarified juice supply creates starvation conditions at evaporator feed. Evaporator utilization running at 78% capacity. Downstream production chain throughput constrained.",
        InsightSeverity.WARNING,"CJ Flow","4,100 L/hr", InsightTrend.DOWN),
    SystemInsight("INS-04-002","CLARIFICATION", InsightCategory.QUALITY,"Flocculant Dosing Inefficiency",
        "Flocculant consumption has increased 34% over the past 5 days with no improvement in settling rate.",
        "Flocculant mixing tank agitator MX-411 is running at 23 RPM — specification requires 35–40 RPM for adequate polymer hydration. Under-mixing causes polymer chain entanglement, reducing effective molecular weight and flocculation bridging efficiency.",
        "Excess flocculant cost: ~₹1,800/hr. Poor floc formation leads to higher turbidity in clarified juice. Mud volume increasing, overloading filter station.",
        InsightSeverity.WARNING,"Flocculant Usage","+34% excess", InsightTrend.UP),
    SystemInsight("INS-04-003","CLARIFICATION", InsightCategory.ENERGY,"VFD Running at Fixed Speed",
        "Feed pump VFD-FC-401 locked at 48 Hz instead of variable control — operating as a fixed-speed drive.",
        "VFD keypad fault code F-021 (encoder feedback loss) caused the drive to fall back to fixed frequency preset on Thursday at 22:14. The process team has been manually adjusting a bypass valve instead of addressing the VFD fault.",
        "Energy overconsumption of ~14 kW continuously (VFD optimization savings lost). Throttling valve causing pressure drop and cavitation risk in pump. Energy waste: ~₹420/hr.",
        InsightSeverity.INFO,"VFD Frequency","48 Hz fixed", InsightTrend.STABLE),
    SystemInsight("INS-04-004","CLARIFICATION", InsightCategory.MAINTENANCE,"Clarifier Torque Trending Up",
        "Clarifier drive torque has increased from 41% to 68% of full load torque over 9 days.",
        "Progressive mud blanket thickening due to reduced rake speed. Dense mud layer increasing rake resistance. Rake flights may also have sediment buildup from the calcium phosphate precipitation events.",
        "At current rate, torque will reach trip threshold (80% FLT) within estimated 18 hours. A clarifier trip results in ~4 hours of recovery time and throughput loss of ~20,000 L clarified juice.",
        InsightSeverity.CRITICAL,"Drive Torque","68% FLT", InsightTrend.UP),
    SystemInsight("INS-04-005","CLARIFICATION", InsightCategory.PROCESS,"Retention Time Below Minimum",
        "Calculated retention time in clarifier is 68 minutes — minimum for effective separation is 90 minutes.",
        "Feed flow rate was increased to 110% of design capacity at shift change to recover throughput lost during the morning mill issues. Higher flow rate reduces residence time, preventing adequate sedimentation.",
        "Insufficient settling causes carry-over of fine particles. Juice purity reduced by 0.8 units. Downstream scaling and color formation accelerate. Crystal quality in pans affected.",
        InsightSeverity.WARNING,"Retention Time","68 min", InsightTrend.DOWN),
    SystemInsight("INS-04-006","CLARIFICATION", InsightCategory.EFFICIENCY,"Tank Level Imbalance Detected",
        "Clarified juice buffer tank TK-401 at 91% fill while TK-402 is at 34% — total ullage available is asymmetric.",
        "Automatic transfer valve TV-415 between TK-401 and TK-402 has been in manual-closed position since last week's maintenance window. Operator log shows it was closed for valve seat inspection but not returned to auto.",
        "TK-401 approaching high-high level alarm (95%). If reached, feed to clarifier will be throttled, causing upstream juice treatment backup. TK-402 spare capacity unutilized.",
        InsightSeverity.WARNING,"Tank Balance","91% / 34%", InsightTrend.UP),

    SystemInsight("INS-05-001","CRYSTALLIZATION", InsightCategory.PROCESS,"Syrup Feed Brix Variance",
        "Pan feed syrup brix ranging 56–64°Bx between batches — target is 62–64°Bx for consistent crystal formation.",
        "Brix variance originates from evaporator syrup brix instability. Without a syrup blending buffer tank at sufficient residence time, high-variance syrup directly enters pans, causing inconsistent super-saturation coefficients at nucleation.",
        "Crystal size distribution CV increased from 28% to 41%. Off-spec crystal batches requiring re-melt: approximately 2 per shift. Each re-melt cycle consumes 1.8 t steam and 45 minutes of pan capacity.",
        InsightSeverity.WARNING,"Feed Brix Range","56 – 64°Bx", InsightTrend.STABLE),
    SystemInsight("INS-05-002","CRYSTALLIZATION", InsightCategory.ENERGY,"Pan Vacuum Below Target",
        "A-massecuite pan vacuum reading 680 mbar absolute — target is 620 mbar for optimal boiling temperature.",
        "Surface condenser CW inlet temperature has risen from 28°C to 36°C due to cooling tower fan #3 being off for repairs. Warmer cooling water reduces condenser efficiency, causing vacuum degradation.",
        "Higher boiling point at lower vacuum increases steam requirement by ~11%. Pan cycle time extended by 8 minutes per batch. Daily production capacity reduced by approximately 3.2 MT sugar.",
        InsightSeverity.WARNING,"Pan Vacuum","680 mbar abs", InsightTrend.DOWN),
    SystemInsight("INS-05-003","CRYSTALLIZATION", InsightCategory.QUALITY,"Crystal MA Size Below Specification",
        "Mean aperture (MA) of A-sugar crystals measuring 0.78mm — commercial specification requires 0.85–1.05mm.",
        "Seed magma preparation in the seed crystallizer has produced seed crystals at 0.12mm (target: 0.18mm) due to a slurry concentration error. Smaller seeds create more nucleation sites, producing a finer final crystal size distribution.",
        "Fine crystal sugar has higher centrifuge losses (molasses carry-over 0.4% higher). Drying and cooling section capacity limited by increased surface area. Risk of customer rejection for grain size spec.",
        InsightSeverity.CRITICAL,"Crystal MA","0.78 mm", InsightTrend.DOWN),
    SystemInsight("INS-05-004","CRYSTALLIZATION", InsightCategory.EFFICIENCY,"Massecuite Purity Drop in B-Pan",
        "B-massecuite purity has dropped from 72% to 66% over 3 cycles — target is ≥70% for effective A-sugar recovery.",
        "Increased non-sucrose content (particularly reducing sugars and colorants) from the juice treatment pH excursions is accumulating in the B-product molasses recycle loop. Impurity buildup effect compounds over multiple crystallization cycles.",
        "Lower B-massecuite purity reduces sugar recovery from B-product. Estimated additional sucrose to final molasses: ~0.8 kg per tonne cane. At current crush rate: ~4.8 MT/day sugar lost to molasses.",
        InsightSeverity.CRITICAL,"B-Pan Purity","66%", InsightTrend.DOWN),
    SystemInsight("INS-05-005","CRYSTALLIZATION", InsightCategory.MAINTENANCE,"Pan Agitator Load Increasing",
        "Pan P-503 stirrer motor current trending up: 28A → 41A over 4 hours (FLA: 55A).",
        "Massecuite viscosity increasing beyond expected range for the current brix/purity combination. Crystal habit modification from elevated color and reducing sugars coating crystal surfaces, increasing massecuite apparent viscosity by ~35% versus model prediction.",
        "Continued viscosity increase risks stirrer overload trip. Pan P-503 forced strike will require 90-minute recovery including pan wash-out. Production loss: approximately 8 MT massecuite per trip event.",
        InsightSeverity.WARNING,"Stirrer Current","41A / 55A FLA", InsightTrend.UP),
    SystemInsight("INS-05-006","CRYSTALLIZATION", InsightCategory.PROCESS,"Centrifuge Cycle Time Extended",
        "A-centrifuge average cycle time is 22 minutes vs design 18 minutes — reducing station throughput by 18%.",
        "Fine crystal distribution requires longer washing time to achieve target color (<100 IU ICUMSA). Additionally, centrifuge screen #2 is partially blinded (estimated 12% area blocked) based on differential pressure measurement across screen.",
        "Centrifuge bottleneck constrains overall station throughput. Pans must hold massecuite beyond optimal discharge point, increasing color and reducing exhaustion. Downstream bagging line running at 82% capacity.",
        InsightSeverity.WARNING,"Cycle Time","22 min / 18 min", InsightTrend.UP)
)

fun getInsightsForStage(stageName: String): List<SystemInsight> =
    systemInsightsData.filter { it.stage.equals(stageName, ignoreCase = true) }

fun getAllInsights(): List<SystemInsight> = systemInsightsData

// ─── MAIN SCREEN ────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun WorkflowDashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScreen: (String) -> Unit
) {
    val theme = getAdaptiveTheme(false)
    val globalOee by viewModel.globalOee.collectAsStateWithLifecycle()
    val globalEnergy by viewModel.globalEnergy.collectAsStateWithLifecycle()
    val globalThroughput by viewModel.globalThroughput.collectAsStateWithLifecycle()
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()

    val activeBatchNo by viewModel.activeBatchNumber.collectAsStateWithLifecycle()
    val opanTimers by viewModel.opanTimers.collectAsStateWithLifecycle()
    val pmTimers by viewModel.pmTimers.collectAsStateWithLifecycle()
    val pmTotalCycleMinutes by viewModel.pmTotalCycleMinutes.collectAsStateWithLifecycle()

    var selectedStageId by remember { mutableStateOf<String?>(null) }
    var insightPopup by remember { mutableStateOf<SystemInsight?>(null) }
    var isAyamAssistOpen by remember { mutableStateOf(false) }

    val stageAlertSummary = remember(activeAlerts) {
        activeAlerts.groupBy { it.stage.uppercase() }.mapValues { (_, alerts) ->
            StageAlertSummary(
                alerts.count { it.priority.equals("CRITICAL", ignoreCase = true) },
                alerts.count { it.priority.equals("WARNING", ignoreCase = true) }
            )
        }
    }

    val ayamMood = remember(activeAlerts) {
        val hasCritical = activeAlerts.any { !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true) }
        val hasWarning = activeAlerts.any { !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true) }
        when {
            hasCritical -> AyamMood.CRITICAL
            hasWarning -> AyamMood.WARNING
            else -> AyamMood.NORMAL
        }
    }

    var heatmapTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); heatmapTick++ } }

    val energyHistory = remember { mutableStateListOf(160f, 165f, 172f, 168f, 175f, 180f, 178f, 182f, 184f) }
    LaunchedEffect(globalEnergy) {
        energyHistory.add(globalEnergy.toFloat())
        if (energyHistory.size > 15) energyHistory.removeAt(0)
    }

    val throughputHistory = remember { mutableStateListOf(100f, 110f, 115f, 108f, 120f, 130f, 125f, 140f, 138f) }
    LaunchedEffect(globalThroughput) {
        throughputHistory.add(globalThroughput.toFloat())
        if (throughputHistory.size > 15) throughputHistory.removeAt(0)
    }

    val displayStages = remember(stages) {
        val evapIdx = stages.indexOfFirst { it.id == "03" || it.name.contains("Evap", ignoreCase = true) }
        val clarIdx = stages.indexOfFirst { it.id == "04" || it.name.contains("Clar", ignoreCase = true) }
        if (evapIdx != -1 && clarIdx != -1 && evapIdx != clarIdx) {
            stages.toMutableList().apply { val tmp = this[evapIdx]; this[evapIdx] = this[clarIdx]; this[clarIdx] = tmp }
        } else stages
    }

    val activeStage = displayStages.find { it.id == selectedStageId }
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    // Using BoxWithConstraints to calculate bounds for draggable assistant bot
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite) // NEW: Warm White App Background
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()
        val fabSizePx = with(LocalDensity.current) { 72.dp.toPx() }

        // --- Draggable Assistant State ---
        val coroutineScope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        var isDragging by remember { mutableStateOf(false) }
        var snappedEdge by remember { mutableStateOf("right") } // "left" or "right"
        var isInitialized by remember { mutableStateOf(false) }
        val density = LocalDensity.current

        // Initialize Bot Position to HIDE & SEEK (Peeking) edge
        LaunchedEffect(maxWidthPx, maxHeightPx) {
            if (!isInitialized && maxWidthPx > 0f && maxHeightPx > 0f) {
                val initX = maxWidthPx - fabSizePx * 0.55f // Peeking right
                val initY = maxHeightPx * 0.7f
                offsetX.snapTo(initX)
                offsetY.snapTo(initY)
                isInitialized = true
            } else if (isInitialized) {
                // Ensure it stays in bounds if screen rotates
                val clampX = offsetX.value.coerceIn(-fabSizePx, maxWidthPx)
                val clampY = offsetY.value.coerceIn(0f, maxHeightPx - fabSizePx)
                offsetX.snapTo(clampX)
                offsetY.snapTo(clampY)
            }
        }

        // Hide and seek animations based on state
        val targetRotation = when {
            isDragging -> 0f
            isAyamAssistOpen -> 0f
            snappedEdge == "left" -> 25f
            else -> -25f
        }
        val targetAlpha = if (isDragging || isAyamAssistOpen) 1f else 0.65f
        val animRotation by animateFloatAsState(targetRotation, tween(400, easing = FastOutSlowInEasing), label = "rot")
        val animAlpha by animateFloatAsState(targetAlpha, tween(400), label = "alpha")

        // Bot Click Handler
        val handleBotClick = {
            if (!isAyamAssistOpen) {
                isAyamAssistOpen = true
                coroutineScope.launch {
                    // Pull fully onto the screen when tapped
                    val safeX = if (snappedEdge == "left") with(density) { 16.dp.toPx() } else maxWidthPx - fabSizePx - with(density) { 16.dp.toPx() }
                    offsetX.animateTo(safeX, tween(300, easing = FastOutSlowInEasing))
                }
            } else {
                isAyamAssistOpen = false
                coroutineScope.launch {
                    // Send back to peek hiding spot
                    val peekX = if (snappedEdge == "left") -fabSizePx * 0.45f else maxWidthPx - fabSizePx * 0.55f
                    offsetX.animateTo(peekX, tween(300, easing = FastOutSlowInEasing))
                }
            }
        }

        // --- MAIN CONTENT LAYER ---
        Box(modifier = Modifier.fillMaxSize()) {
            if (isPortrait) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GraphPanel(
                        Modifier.fillMaxWidth().height(420.dp),
                        theme, displayStages, selectedStageId,
                        onStageSelected = { id -> selectedStageId = id },
                        onNavigateToStage = { id -> onNavigateToScreen(getDedicatedRouteForStage(id)) },
                        heatmapTick, true, stageAlertSummary,
                        opanTimers = opanTimers,
                        pmTimers = pmTimers,
                        activeBatchNo = activeBatchNo,
                        pmTotalCycleMinutes = pmTotalCycleMinutes
                    )
                    Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProjectedKpiPanel(Modifier.weight(1f).fillMaxHeight(), theme, displayStages, activeStage, globalOee)
                        EnergyPanel(Modifier.weight(1.5f).fillMaxHeight(), theme, activeStage, globalEnergy.toFloat(), globalThroughput.toFloat(), energyHistory, throughputHistory)
                    }
                    SuggestionsPanel(Modifier.fillMaxWidth().height(280.dp), theme, activeStage) { insightPopup = it }
                    AlertsPanel(Modifier.fillMaxWidth().height(380.dp), theme, activeAlerts, onNavigateToScreen)
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                Row(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(modifier = Modifier.weight(2.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GraphPanel(
                            Modifier.fillMaxWidth().weight(2.5f),
                            theme, displayStages, selectedStageId,
                            onStageSelected = { id -> selectedStageId = id },
                            onNavigateToStage = { id -> onNavigateToScreen(getDedicatedRouteForStage(id)) },
                            heatmapTick, false, stageAlertSummary,
                            opanTimers = opanTimers,
                            pmTimers = pmTimers,
                            activeBatchNo = activeBatchNo,
                            pmTotalCycleMinutes = pmTotalCycleMinutes
                        )
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProjectedKpiPanel(Modifier.weight(1f).fillMaxHeight(), theme, displayStages, activeStage, globalOee)
                            SuggestionsPanel(Modifier.weight(2.2f).fillMaxHeight(), theme, activeStage) { insightPopup = it }
                        }
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnergyPanel(Modifier.fillMaxWidth().weight(0.7f), theme, activeStage, globalEnergy.toFloat(), globalThroughput.toFloat(), energyHistory, throughputHistory)
                        AlertsPanel(Modifier.fillMaxWidth().weight(2.3f), theme, activeAlerts, onNavigateToScreen)
                    }
                }
            }
        }

        // --- OVERLAYS & POPUPS ---
        insightPopup?.let { insight ->
            InsightGlassDialog(insight, theme) { insightPopup = null }
        }

        // Overlay background when Assistant menu is open
        if (isAyamAssistOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(40f)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { handleBotClick() } // Clicking outside closes it
            ) {
                // Calculate position to place popup near the bot
                val fabY_dp = with(density) { offsetY.value.toDp() }
                val maxHDp = with(density) { maxHeightPx.toDp() }
                val isLeft = snappedEdge == "left"

                // Ensure popup doesn't go off the bottom of the screen
                val safeTopPadding = fabY_dp.coerceIn(16.dp, (maxHDp - 380.dp).coerceAtLeast(16.dp))

                Box(
                    modifier = Modifier
                        .align(if (isLeft) Alignment.TopStart else Alignment.TopEnd)
                        .padding(
                            top = safeTopPadding,
                            start = if (isLeft) 85.dp else 16.dp,
                            end = if (isLeft) 16.dp else 85.dp
                        )
                ) {
                    AyamAssistantPopup(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}, // prevent click-through on the popup card
                        theme = theme,
                        activeAlerts = activeAlerts,
                        suggestions = stages.flatMap { it.recommendations },
                        ayamMood = ayamMood,
                        onNavigateToScreen = onNavigateToScreen,
                        onClose = { handleBotClick() }
                    )
                }
            }
        }
        // --- DRAGGABLE ASSISTANT BOT ---
        if (isInitialized) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                    .wrapContentSize()                // receives screen constraints
                    .zIndex(50f)
                    .graphicsLayer(alpha = animAlpha, clip = false)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                if (isAyamAssistOpen) isAyamAssistOpen = false
                            },
                            onDragEnd = {
                                isDragging = false
                                val isLeft = offsetX.value < (maxWidthPx / 2f - fabSizePx / 2f)
                                snappedEdge = if (isLeft) "left" else "right"

                                // keep your peek behavior
                                val targetX = if (isLeft) -fabSizePx * 0.45f else maxWidthPx - fabSizePx * 0.55f
                                coroutineScope.launch {
                                    offsetX.animateTo(targetX, tween(400, easing = FastOutSlowInEasing))
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    val newX = (offsetX.value + dragAmount.x)
                                        .coerceIn(-fabSizePx * 0.5f, maxWidthPx - fabSizePx * 0.5f)
                                    val newY = (offsetY.value + dragAmount.y)
                                        .coerceIn(0f, maxHeightPx - fabSizePx)
                                    offsetX.snapTo(newX)
                                    offsetY.snapTo(newY)
                                }
                            }
                        )
                    }
            ) {
                AyamBotWithBubble(
                    theme = theme,
                    mood = ayamMood,
                    snappedEdge = snappedEdge,
                    rotationZ = animRotation,
                    showBubble = !isDragging && !isAyamAssistOpen,
                    onToggle = { handleBotClick() }
                )
            }
        }

        RadialAppBar(
            modifier = Modifier.align(Alignment.CenterStart).zIndex(30f),
            activeSection = "workflow_dashboard",
            onActionSelected = { onNavigateToScreen(it) }
        )
    }
}

// ─── INSIGHT GLASS DIALOG (ENHANCED BLUR) ───────────────────────────────────

@Composable
fun InsightGlassDialog(
    insight: SystemInsight,
    theme: DashboardTheme,
    onDismiss: () -> Unit
) {
    val severityColor = when (insight.severity) {
        InsightSeverity.CRITICAL -> AccentCritical
        InsightSeverity.WARNING -> AccentWarning
        InsightSeverity.INFO -> AccentPrimary
    }
    val categoryColor = when (insight.category) {
        InsightCategory.EFFICIENCY -> AccentSuccess
        InsightCategory.ENERGY -> AccentWarning
        InsightCategory.QUALITY -> AccentPrimary
        InsightCategory.MAINTENANCE -> AccentCritical
        InsightCategory.PROCESS -> AccentAI
    }
    val trendIcon = when (insight.trend) {
        InsightTrend.UP -> "▲"
        InsightTrend.DOWN -> "▼"
        InsightTrend.STABLE -> "●"
    }
    val trendColor = when (insight.trend) {
        InsightTrend.UP -> AccentCritical
        InsightTrend.DOWN -> AccentWarning
        InsightTrend.STABLE -> AccentSuccess
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val dialogShape = RoundedCornerShape(24.dp)

            Box(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .padding(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(dialogShape)
                        .background(Color.White.copy(alpha = 0.55f))
                        .blur(80.dp)
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(dialogShape)
                        .background(BrandOffWhite.copy(alpha = 0.45f))
                        .blur(60.dp)
                )

                Box(
                    modifier = Modifier
                        .shadow(24.dp, dialogShape, spotColor = Color.Black.copy(alpha = 0.18f), ambientColor = Color.Black.copy(alpha = 0.10f))
                        .clip(dialogShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.94f),
                                    BrandOffWhite.copy(alpha = 0.88f),
                                    Color.White.copy(alpha = 0.92f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White,
                                    severityColor.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.80f)
                                )
                            ),
                            dialogShape
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(severityColor))
                                    Text(insight.stage, color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text(
                                        insight.category.name, color = categoryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(categoryColor.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        insight.severity.name, color = severityColor, fontSize = 11.sp, fontWeight = FontWeight.Black,
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(severityColor.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(insight.title, color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, null, tint = BrandSteelGray, modifier = Modifier.size(18.dp))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(severityColor.copy(alpha = 0.08f), severityColor.copy(alpha = 0.03f))))
                                .border(0.5.dp, severityColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(insight.metric, color = BrandSteelGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(insight.metricValue, color = BrandDeepNavy, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(trendIcon, color = trendColor, fontSize = 14.sp)
                                Text(
                                    when (insight.trend) {
                                        InsightTrend.UP -> "Increasing"
                                        InsightTrend.DOWN -> "Decreasing"
                                        InsightTrend.STABLE -> "Stable"
                                    },
                                    color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Text(insight.id, color = BrandSteelGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        GlassSection("⚠️", "Problem Detected", AccentCritical, insight.problem, AccentCritical.copy(alpha = 0.05f), AccentCritical.copy(alpha = 0.12f))
                        GlassSection("🔍", "Root Cause Analysis", AccentWarning, insight.rootCause, AccentWarning.copy(alpha = 0.05f), AccentWarning.copy(alpha = 0.12f))
                        GlassSection("📉", "Operational Impact", AccentAI, insight.impact, AccentAI.copy(alpha = 0.05f), AccentAI.copy(alpha = 0.12f))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                "Acknowledge", color = BrandOffWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.linearGradient(listOf(severityColor, severityColor.copy(alpha = 0.8f))))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSection(
    emoji: String,
    label: String,
    labelColor: Color,
    body: String,
    bgColor: Color,
    borderColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emoji, fontSize = 14.sp)
            Text(label, color = labelColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(body, color = BrandDeepNavy, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

// ─── GRAPH PANEL ────────────────────────────────────────────────────────────

@Composable
fun GraphPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    displayStages: List<LiveStageData>,
    selectedStageId: String?,
    onStageSelected: (String?) -> Unit,
    onNavigateToStage: (String) -> Unit,
    heatmapTick: Int,
    isPortrait: Boolean,
    alertSummary: Map<String, StageAlertSummary> = emptyMap(),
    opanTimers: List<OpanTimerSnapshot>,
    pmTimers: List<PmTimerSnapshot>,
    activeBatchNo: Int,
    pmTotalCycleMinutes: Double
) {
    val flowPath = remember { Path() }

    CleanPanel(theme, modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1.6f)) {
                val canvasWidth = maxWidth
                val canvasHeight = maxHeight
                val stagesForLine = if (displayStages.isNotEmpty()) displayStages.dropLast(1) else emptyList()
                val maxValue = stagesForLine.maxOfOrNull { it.actualFlow } ?: 100f
                val graphMaxBound = if (maxValue > 0f) maxValue * 1.25f else 100f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (displayStages.isEmpty()) return@Canvas
                    val w = size.width; val h = size.height; val colW = w / displayStages.size

                    fun calcY(v: Float) = (h - ((v / graphMaxBound) * h)).coerceIn(0f, h)

                    flowPath.reset()

                    for (tick in 0..4) {
                        val y = calcY((graphMaxBound / 4f) * tick)
                        drawLine(theme.textMuted.copy(alpha = 0.08f), Offset(0f, y), Offset(w, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)))
                    }

                    displayStages.forEachIndexed { i, stage ->
                        val colLeft = i * colW
                        drawRect(stage.color.copy(alpha = if (theme.isDark) 0.10f else 0.05f), Offset(colLeft, 0f), Size(colW, h))
                        if (i > 0) drawLine(theme.textMuted.copy(alpha = 0.15f), Offset(colLeft, 0f), Offset(colLeft, h), 0.5.dp.toPx())
                    }

                    fun Path.buildSmooth(values: List<Float>) {
                        if (values.size < 2) return
                        values.forEachIndexed { index, value ->
                            if (index >= stagesForLine.size) return@forEachIndexed
                            val x = (index * colW) + (colW / 2f); val y = calcY(value)
                            if (index == 0) moveTo(x, y)
                            else { val prevX = ((index - 1) * colW) + (colW / 2f); val prevY = calcY(values[index - 1]); val midX = (prevX + x) / 2f; cubicTo(midX, prevY, midX, y, x, y) }
                        }
                    }

                    val flowValues = stagesForLine.map { it.actualFlow }
                    flowPath.buildSmooth(flowValues)

                    val fillPath = Path().apply {
                        addPath(flowPath)
                        if (stagesForLine.isNotEmpty()) {
                            val lastX = ((stagesForLine.size - 1) * colW) + (colW / 2f); val firstX = colW / 2f
                            lineTo(lastX, h); lineTo(firstX, h); close()
                        }
                    }
                    drawPath(fillPath, Brush.verticalGradient(listOf(AccentPrimary.copy(alpha = 0.15f), AccentPrimary.copy(alpha = 0.02f))))
                    drawPath(flowPath, AccentPrimary.copy(alpha = 0.12f), style = Stroke(7.dp.toPx()))
                    drawPath(flowPath, AccentPrimary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                    displayStages.forEachIndexed { i, stage ->
                        val nx = (i * colW) + (colW / 2f)
                        if (i < stagesForLine.size) {
                            val flowY = calcY(stage.actualFlow)
                            val summary = alertSummary[stage.name.uppercase()]
                            if ((summary?.critical ?: 0) > 0 || (summary?.warning ?: 0) > 0)
                                drawCircle(AccentCritical.copy(alpha = 0.25f), 11.dp.toPx(), Offset(nx, flowY))
                            drawCircle(BrandOffWhite, 7.dp.toPx(), Offset(nx, flowY))
                            drawCircle(stage.color, 5.dp.toPx(), Offset(nx, flowY))
                        } else {
                            drawBatchGrid(
                                stage = stage,
                                colW = colW,
                                i = i,
                                graphH = h,
                                heatmapTick = heatmapTick,
                                theme = theme,
                                isPortrait = isPortrait,
                                opanTimers = opanTimers,
                                pmTimers = pmTimers,
                                activeBatchNo = activeBatchNo,
                                pmTotalCycleMinutes = pmTotalCycleMinutes
                            )
                        }
                    }
                }

                if (displayStages.isNotEmpty()) {
                    val colWdp = canvasWidth / displayStages.size
                    displayStages.forEachIndexed { i, stage ->
                        if (i < stagesForLine.size) {
                            val nx = (colWdp * i) + (colWdp / 2)
                            val ay = canvasHeight.value - ((stage.actualFlow / graphMaxBound) * canvasHeight.value)
                            val summary = alertSummary[stage.name.uppercase()]
                            Box(
                                modifier = Modifier
                                    .offset(x = nx - 65.dp, y = ay.dp - 58.dp)
                                    .width(130.dp)
                                    .clickable { onNavigateToStage(stage.id) }
                            ) {
                                StageFlowGlassBox(stage, stage.actualFlow, summary, theme)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .offset(x = colWdp * i, y = 0.dp)
                                    .width(colWdp)
                                    .fillMaxHeight()
                                    .clickable { onNavigateToStage(stage.id) }
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                displayStages.forEach { stage ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(stage.name.uppercase(), color = theme.textMain, fontSize = if (isPortrait) 12.sp else 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = 0.5.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                displayStages.forEach { stage ->
                    val isSelected = selectedStageId == stage.id
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {

                        val card = remember(stage, opanTimers, pmTimers, activeBatchNo) {
                            if (stage.id == "05") {
                                val opMax = opanTimers.maxOfOrNull { it.elapsedMinutes } ?: 0.0
                                val pmActive = pmTimers.firstOrNull { it.active } ?: pmTimers.firstOrNull()

                                StageCardDisplay(
                                    primaryLabel = "SYRUP FLOW",
                                    primaryValue = String.format("%,.0f", stage.actualFlow),
                                    primaryUnit = "L/hr",
                                    stat1Label = "OPAN",
                                    stat1Value = if (opMax > 0) "${opMax.toInt()}m" else "--",
                                    stat2Label = "PM",
                                    stat2Value = pmActive?.let {
                                        if (it.active) "${it.phase.take(6)} ${it.elapsedMinutes.toInt()}m" else "IDLE"
                                    } ?: "IDLE",
                                    stat3Label = "BATCH",
                                    stat3Value = "#$activeBatchNo",
                                    efficiency = stage.efficiency,
                                    uptime = 89.3f,
                                    loadPercent = 92
                                )
                            } else {
                                stageCardDisplay(stage)
                            }
                        }

                        StageKpiCard(stage, card, isSelected, isPortrait, theme) {
                            onStageSelected(if (selectedStageId == stage.id) null else stage.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageKpiCard(stage: LiveStageData, cardDisplay: StageCardDisplay, isSelected: Boolean, isPortrait: Boolean, theme: DashboardTheme, onClick: () -> Unit) {
    val tankPct = stage.tankFillPercent.coerceIn(0, 100)
    val fillHeight = (tankPct / 100f).coerceIn(0f, 1f)
    val barColor = if (tankPct >= 90) AccentCritical else stage.color
    val efficiencyNorm = (cardDisplay.efficiency / 100f).coerceIn(0f, 1f)
    val animEfficiency by animateFloatAsState(efficiencyNorm, tween(800), label = "eff_${stage.id}")

    Box(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) stage.color.copy(alpha = 0.14f) else theme.trackBg.copy(alpha = 0.25f))
            .border(if (isSelected) 2.dp else 0.5.dp, if (isSelected) stage.color else BrandLightGray.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(vertical = 4.dp).width(4.dp).fillMaxHeight(fillHeight).clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)).background(barColor.copy(alpha = 0.7f)))
        Column(modifier = Modifier.fillMaxSize().padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(cardDisplay.primaryValue, color = theme.textMain, fontSize = if (isPortrait) 16.sp else 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(shadow = Shadow(BrandDeepNavy.copy(alpha = 0.08f), Offset(0f, 1f), 3f)))
                Text(cardDisplay.primaryUnit, color = stage.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val strokeW = 3.dp.toPx(); val pad = strokeW / 2 + 1.dp.toPx()
                    val arcSize = Size(size.width - 2 * pad, size.height - 2 * pad); val topLeft = Offset(pad, pad)
                    drawArc(BrandLightGray.copy(alpha = 0.2f), -90f, 360f, false, topLeft, arcSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                    val effColor = when { cardDisplay.efficiency >= 85 -> AccentSuccess; cardDisplay.efficiency >= 60 -> AccentWarning; else -> AccentCritical }
                    drawArc(effColor, -90f, 360f * animEfficiency, false, topLeft, arcSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                }
                Text("${cardDisplay.efficiency}", color = theme.textMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val statStyle = TextStyle(shadow = Shadow(BrandDeepNavy.copy(alpha = 0.06f), blurRadius = 1f))
                Text("${cardDisplay.stat1Label}: ${cardDisplay.stat1Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${cardDisplay.stat2Label}: ${cardDisplay.stat2Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${cardDisplay.stat3Label}: ${cardDisplay.stat3Value}", color = theme.textLightMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, style = statStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun DrawScope.drawBatchGrid(
    stage: LiveStageData,
    colW: Float,
    i: Int,
    graphH: Float,
    heatmapTick: Int,
    theme: DashboardTheme,
    isPortrait: Boolean,
    opanTimers: List<OpanTimerSnapshot>,
    pmTimers: List<PmTimerSnapshot>,
    activeBatchNo: Int,
    pmTotalCycleMinutes: Double
) {
    val colLeft = i * colW
    val scaleDown = if (isPortrait) 0.7f else 1f

    val cellW = (38.dp * scaleDown).toPx()
    val cellH = (26.dp * scaleDown).toPx()
    val spacing = (8.dp * scaleDown).toPx()

    val batchBoxW = (38.dp * scaleDown).toPx()
    val batchSpc = (16.dp * scaleDown).toPx()

    val rows = 4
    val cols = 2

    val totalGridW = (cols * cellW) + ((cols - 1) * spacing) + batchSpc + batchBoxW
    val totalGridH = (rows * cellH) + ((rows - 1) * spacing)

    val gridStartX = colLeft + (colW - totalGridW) / 2
    val batchStartX = gridStartX + (cols * cellW) + ((cols - 1) * spacing) + batchSpc
    val startY = (graphH - totalGridH) / 2 + 10.dp.toPx()

    val hdrPaint = Paint().apply { color = stage.color.toArgb(); textSize = (11.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val textPaint = Paint().apply { color = BrandOffWhite.toArgb(); textSize = (12.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val subPaint = Paint().apply { color = BrandOffWhite.copy(alpha = 0.92f).toArgb(); textSize = (10.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    val batchPaint = Paint().apply { color = stage.color.toArgb(); textSize = (12.sp * scaleDown).toPx(); textAlign = Paint.Align.CENTER; isFakeBoldText = true }

    drawContext.canvas.nativeCanvas.apply {
        drawText("PAN", gridStartX + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        drawText("PWDR", gridStartX + cellW + spacing + cellW / 2, startY - 10.dp.toPx(), hdrPaint)
        val pmT = if (pmTotalCycleMinutes > 0.0) "(${pmTotalCycleMinutes.toInt()}m)" else ""
        drawText("BATCH $pmT", batchStartX + batchBoxW / 2, startY - 10.dp.toPx(), hdrPaint)
    }

    val emptyColor = if (theme.isDark) BrandDarkBlueGray.copy(alpha = 0.35f) else BrandLightGray.copy(alpha = 0.45f)
    val pulse = 0.75f + 0.25f * kotlin.math.abs(kotlin.math.sin(heatmapTick / 2f))

    for (r in 0 until rows) {
        val cy = startY + r * (cellH + spacing)

        val op = opanTimers.getOrNull(r)
        val pm = pmTimers.getOrNull(r)

        val opActive = op?.running == true
        val opMin = op?.elapsedMinutes ?: 0.0
        val opText = if (opActive) "${opMin.toInt()}m" else "--"

        val opX = gridStartX
        if (opActive) {
            drawRoundRect(stage.color.copy(alpha = 0.18f * pulse), Offset(opX - 2.dp.toPx(), cy - 2.dp.toPx()), Size(cellW + 4.dp.toPx(), cellH + 4.dp.toPx()), CornerRadius(6.dp.toPx()))
        }
        drawRoundRect(if (opActive) stage.color.copy(alpha = 0.55f) else emptyColor.copy(alpha = 0.20f), Offset(opX, cy), Size(cellW, cellH), CornerRadius(4.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText(opText, opX + cellW / 2, cy + cellH / 2 - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)

        val pmX = gridStartX + cellW + spacing
        val pmActive = pm?.active == true
        val pmPhase = (pm?.phase ?: "IDLE").take(6)
        val pmElapsed = pm?.elapsedMinutes ?: 0.0

        if (pmActive) {
            drawRoundRect(stage.color.copy(alpha = 0.18f * pulse), Offset(pmX - 2.dp.toPx(), cy - 2.dp.toPx()), Size(cellW + 4.dp.toPx(), cellH + 4.dp.toPx()), CornerRadius(6.dp.toPx()))
        }
        drawRoundRect(if (pmActive) stage.color.copy(alpha = 0.55f) else emptyColor.copy(alpha = 0.20f), Offset(pmX, cy), Size(cellW, cellH), CornerRadius(4.dp.toPx()))

        val pmTop = if (pmActive) pmPhase else "IDLE"
        val pmBottom = if (pmActive) "${pmElapsed.toInt()}m" else "--"

        drawContext.canvas.nativeCanvas.drawText(pmTop, pmX + cellW / 2, cy + cellH * 0.42f - (subPaint.ascent() + subPaint.descent()) / 2, subPaint)
        drawContext.canvas.nativeCanvas.drawText(pmBottom, pmX + cellW / 2, cy + cellH * 0.78f - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)

        drawRoundRect(stage.color.copy(alpha = 0.12f), Offset(batchStartX, cy), Size(batchBoxW, cellH), CornerRadius(6.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText("#$activeBatchNo", batchStartX + batchBoxW / 2, cy + cellH / 2 - (batchPaint.ascent() + batchPaint.descent()) / 2, batchPaint)
    }
}

@Composable
fun StageFlowGlassBox(stage: LiveStageData, flowValue: Float, summary: StageAlertSummary?, theme: DashboardTheme) {
    val critical = summary?.critical ?: 0; val warning = summary?.warning ?: 0
    val (statusText, statusColor) = when { critical > 0 -> "CRITICAL" to AccentCritical; warning > 0 -> "WARNING" to AccentWarning; else -> "STABLE" to AccentSuccess }
    val alertsText = when { critical > 0 && warning > 0 -> "$critical CRIT · $warning WARN"; critical > 0 -> "$critical CRIT"; warning > 0 -> "$warning WARN"; else -> "Operating normally" }
    val cardShape = RoundedCornerShape(12.dp)
    val sparkData = remember(stage) { val base = flowValue; listOf(base * 0.92f, base * 0.97f, base * 1.02f, base * 0.95f, base * 1.05f, base * 0.98f, base * 1.03f, base) }

    Box(modifier = Modifier.wrapContentSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.shadow(8.dp, cardShape, spotColor = Color.Black.copy(alpha = 0.10f), ambientColor = Color.Black.copy(alpha = 0.05f)).clip(cardShape)
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.60f))))
                    .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.95f), statusColor.copy(alpha = 0.3f))), cardShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(String.format("%,.0f", flowValue), color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Canvas(modifier = Modifier.width(80.dp).height(16.dp)) {
                        val sparkPath = Path(); val minV = sparkData.minOrNull() ?: 0f; val maxV = sparkData.maxOrNull() ?: 1f; val range = (maxV - minV).coerceAtLeast(1f)
                        sparkData.forEachIndexed { idx, v -> val x = (idx.toFloat() / (sparkData.size - 1)) * size.width; val y = size.height - ((v - minV) / range) * size.height; if (idx == 0) sparkPath.moveTo(x, y) else sparkPath.lineTo(x, y) }
                        drawPath(sparkPath, statusColor.copy(alpha = 0.5f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    Text("$statusText · $alertsText", color = statusColor.copy(alpha = 0.80f), fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(modifier = Modifier.offset(y = (-1).dp).size(5.dp).clip(CircleShape).background(stage.color))
        }
    }
}

// ─── PROJECTED KPI PANEL ────────────────────────────────────────────────────

@Composable
fun ProjectedKpiPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    displayStages: List<LiveStageData>,
    activeStage: LiveStageData?,
    globalOee: Int = 0
) {
    val displayProjection = activeStage?.aiProjection
        ?: if (displayStages.isNotEmpty()) displayStages.map { it.aiProjection }.average().toFloat() else 85f
    val animProjection by animateFloatAsState(displayProjection, tween(800), label = "proj")
    val avgEfficiency = activeStage?.efficiency
        ?: if (displayStages.isNotEmpty()) displayStages.map { it.efficiency }.average().toInt() else 75
    val animOee by animateFloatAsState(globalOee.toFloat().coerceAtLeast(1f), tween(800), label = "oee")

    val forecastCurve = remember(displayProjection) {
        val base = displayProjection.coerceAtLeast(20f)
        listOf(base * 0.88f, base * 0.94f, base * 1.01f, base * 0.96f, base * 1.08f, base * 1.03f, base * 0.99f, base * 1.05f, base)
    }

    CleanPanel(theme, modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(if (activeStage != null) "${activeStage.name.uppercase()} FORECAST" else "PLANT FORECAST", color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(String.format("%,d", animProjection.toInt()), color = AccentAI, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("TCD", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 5.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularScoreIndicator("OEE", animOee.toInt(), theme)
                    CircularScoreIndicator("EFF", avgEfficiency, theme)
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ForecastAreaChart(forecastCurve, AccentAI)
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("AI forecast · next 4hr window", color = theme.textLightMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CircularScoreIndicator(label: String, score: Int, theme: DashboardTheme) {
    val clampedScore = score.coerceIn(0, 100)
    val animScore by animateFloatAsState(clampedScore / 100f, tween(1000), label = "score_$label")
    val color = when { clampedScore >= 80 -> AccentSuccess; clampedScore >= 60 -> AccentWarning; else -> AccentCritical }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(BrandLightGray.copy(alpha = 0.2f), 0f, 360f, false, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
                drawArc(color, -90f, 360f * animScore, false, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("$clampedScore%", color = theme.textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ForecastAreaChart(data: List<Float>, color: Color) {
    val path = remember { Path() }
    val fillPath = remember { Path() }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val entryProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "forecastEntry"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")

    val edgeSpike by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0f at 0
                0f at 1400
                -0.15f at 1500
                1.0f at 1600
                -0.25f at 1700
                0f at 1800
                0.1f at 1900
                0f at 2050
                0f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "edgeSpike"
    )

    val dotPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.size < 2 || size.width <= 0f || size.height <= 0f) return@Canvas

        val w = size.width
        val h = size.height
        val padding = 4.dp.toPx()
        val drawH = h - padding * 2
        val drawW = w - padding * 2

        val minV = (data.minOrNull() ?: 0f) * 0.85f
        val maxV = (data.maxOrNull() ?: 1f) * 1.15f
        val range = (maxV - minV).coerceAtLeast(0.01f)
        val stepX = drawW / (data.size - 1)

        path.reset()
        fillPath.reset()

        fun getY(index: Int, value: Float): Float {
            var rawY = padding + drawH - ((value - minV) / range) * drawH
            if (index == data.lastIndex) {
                val spikeAmplitude = drawH * 0.45f
                rawY -= (edgeSpike * spikeAmplitude)
            }
            val startY = padding + drawH
            return startY + (rawY - startY) * entryProgress
        }

        data.forEachIndexed { i, v ->
            val x = padding + i * stepX
            val y = getY(i, v)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = padding + (i - 1) * stepX
                val prevY = getY(i - 1, data[i - 1])
                val cpX = (prevX + x) / 2f
                path.cubicTo(cpX, prevY, cpX, y, x, y)
            }
        }

        fillPath.addPath(path)
        fillPath.lineTo(padding + drawW, padding + drawH)
        fillPath.lineTo(padding, padding + drawH)
        fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.0f))))
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastX = padding + (data.size - 1) * stepX
        val lastY = getY(data.lastIndex, data.last())

        val haloRadius = 4.dp.toPx() + (14.dp.toPx() * dotPulseProgress)
        val haloAlpha = 0.6f * (1f - dotPulseProgress)
        drawCircle(color.copy(alpha = haloAlpha), radius = haloRadius, center = Offset(lastX, lastY))

        drawCircle(Color.White, 4.dp.toPx(), Offset(lastX, lastY))
        drawCircle(color, 2.5.dp.toPx(), Offset(lastX, lastY))

        drawLine(
            color.copy(alpha = 0.15f),
            Offset(padding, padding + drawH),
            Offset(padding + drawW, padding + drawH),
            1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }
}

// ─── ENERGY PANEL ───────────────────────────────────────────────────────────

@Composable
fun EnergyPanel(modifier: Modifier, theme: DashboardTheme, activeStage: LiveStageData?, globalEnergy: Float, globalThroughput: Float, energyHistory: List<Float>, throughputHistory: List<Float>) {
    val displayEnergy = activeStage?.energyKw ?: globalEnergy
    val displayThroughput = activeStage?.actualFlow ?: globalThroughput
    val animEnergy by animateFloatAsState(displayEnergy, tween(800), label = "energy")
    val animThroughput by animateFloatAsState(displayThroughput, tween(800), label = "throughput")
    val cardShape = RoundedCornerShape(18.dp)

    CleanPanel(theme, modifier = modifier) {
        Row(modifier = Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThroughputCard(Modifier.weight(1f).fillMaxHeight(), animThroughput, if (activeStage != null) "kg/hr" else "TCD", throughputHistory, cardShape)
            EnergyCard(Modifier.weight(1f).fillMaxHeight(), animEnergy, energyHistory, cardShape)
        }
    }
}

@Composable
private fun ThroughputCard(modifier: Modifier, value: Float, unit: String, history: List<Float>, cardShape: RoundedCornerShape) {
    Column(modifier = modifier.clip(cardShape).background(Brush.linearGradient(listOf(AccentPrimary.copy(alpha = 0.14f), AccentPrimary.copy(alpha = 0.04f)))).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(18.dp).background(AccentPrimary.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Speed, null, tint = AccentPrimary, modifier = Modifier.size(11.dp))
            }
            Text("Production", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(String.format("%,d", value.toInt()), color = BrandDeepNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(unit, color = BrandMutedBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        ThroughputBarChart(history, AccentPrimary, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun ThroughputBarChart(history: List<Float>, barColor: Color, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(1f, tween(700), label = "barChart")
    Canvas(modifier = modifier) {
        if (history.isEmpty()) return@Canvas
        val maxV = history.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barCount = history.size; val totalSpacing = (barCount - 1) * 2.dp.toPx()
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
        history.forEachIndexed { index, v ->
            val frac = (v / maxV).coerceIn(0.05f, 1f); val barHeight = size.height * frac * animatedProgress
            val x = index * (barWidth + 2.dp.toPx()); val y = size.height - barHeight
            val isLast = index == history.lastIndex; val alpha = if (isLast) 0.9f else 0.3f + 0.4f * (index.toFloat() / barCount)
            drawRoundRect(barColor.copy(alpha = alpha), Offset(x, y), Size(barWidth, barHeight), CornerRadius(2.dp.toPx()))
        }
    }
}

@Composable
private fun EnergyCard(modifier: Modifier, value: Float, history: List<Float>, cardShape: RoundedCornerShape) {
    val avgEnergy = history.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: value
    val diff = value - avgEnergy; val pct = if (avgEnergy != 0f) diff / avgEnergy * 100f else 0f; val isUp = pct >= 0f

    Column(modifier = modifier.clip(cardShape).background(Brush.linearGradient(listOf(AccentWarning.copy(alpha = 0.12f), AccentWarning.copy(alpha = 0.03f)))).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(18.dp).background(AccentWarning.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ElectricBolt, null, tint = AccentWarning, modifier = Modifier.size(11.dp))
            }
            Text("Energy", color = BrandDeepNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            EnergyNeedleGauge(value, 0f, 80f, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp)) {
                Text(value.toInt().toString(), color = BrandDeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("kW", color = BrandSteelGray, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${if (isUp) "▲" else "▼"} ${abs(pct).toInt()}%", color = if (isUp) AccentCritical else AccentSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("vs avg ${avgEnergy.toInt()}kW", color = BrandSteelGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EnergyNeedleGauge(value: Float, min: Float, max: Float, modifier: Modifier = Modifier) {
    val clamped = value.coerceIn(min, max); val normalized = if (max - min == 0f) 0f else (clamped - min) / (max - min)
    val animatedNorm by animateFloatAsState(normalized, tween(900, easing = FastOutSlowInEasing), label = "needleGauge")
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx(); val padding = strokeWidth / 2 + 6.dp.toPx()
        val diameter = size.width - 2 * padding; val arcSize = Size(diameter, diameter)
        val arcHeight = diameter / 2f; val topOffset = (size.height - arcHeight) / 2f; val topLeft = Offset(padding, topOffset)
        val startAngle = 180f; val fullSweep = 180f; val segmentCount = 20; val segmentSweep = fullSweep / segmentCount; val segmentGap = 1.5f
        for (i in 0 until segmentCount) {
            val segStart = startAngle + i * segmentSweep; val segFrac = i.toFloat() / segmentCount
            val segColor = when { segFrac < 0.4f -> AccentSuccess; segFrac < 0.7f -> AccentWarning; else -> AccentCritical }
            val isActive = segFrac <= animatedNorm; val alpha = if (isActive) 0.85f else 0.12f
            drawArc(segColor.copy(alpha = alpha), segStart, segmentSweep - segmentGap, false, topLeft, arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
        }
        val needleAngle = startAngle + fullSweep * animatedNorm; val needleRad = needleAngle * (PI.toFloat() / 180f)
        val centerX = topLeft.x + arcSize.width / 2f; val centerY = topLeft.y + arcSize.height / 2f
        val needleLength = arcSize.width / 2f - strokeWidth
        val needleEndX = centerX + needleLength * cos(needleRad.toDouble()).toFloat(); val needleEndY = centerY + needleLength * sin(needleRad.toDouble()).toFloat()
        drawLine(Color.Black.copy(alpha = 0.1f), Offset(centerX + 1, centerY + 1), Offset(needleEndX + 1, needleEndY + 1), 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(BrandDeepNavy, Offset(centerX, centerY), Offset(needleEndX, needleEndY), 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(BrandDeepNavy, 3.dp.toPx(), Offset(centerX, centerY)); drawCircle(AccentWarning, 2.dp.toPx(), Offset(centerX, centerY))
    }
}

// ─── SUGGESTIONS PANEL ─────────────────────────────────────────────────────

@Composable
fun SuggestionsPanel(
    modifier: Modifier,
    theme: DashboardTheme,
    activeStage: LiveStageData?,
    onInsightClick: (SystemInsight) -> Unit
) {
    val stageInsights = remember(activeStage) {
        if (activeStage != null) getInsightsForStage(activeStage.name)
        else getAllInsights().take(10)
    }

    CleanPanel(theme, modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (activeStage != null) "${activeStage.name} SUGGESTIONS" else "SUGGESTIONS", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("${stageInsights.size} active diagnostics · Powered by Ayam AI", color = theme.textLightMuted, fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val critCount = stageInsights.count { it.severity == InsightSeverity.CRITICAL }
                    val warnCount = stageInsights.count { it.severity == InsightSeverity.WARNING }
                    if (critCount > 0) {
                        Text("$critCount CRIT", color = AccentCritical, fontSize = 11.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentCritical.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    if (warnCount > 0) {
                        Text("$warnCount WARN", color = AccentWarning, fontSize = 11.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentWarning.copy(alpha = 0.12f)).padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Icon(Icons.Outlined.Memory, null, tint = AccentAI, modifier = Modifier.size(18.dp))
                }
            }

            if (stageInsights.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No system insights available.", color = theme.textLightMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(stageInsights, key = { it.id }) { insight ->
                        InsightListCard(insight, theme) { onInsightClick(insight) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightListCard(
    insight: SystemInsight,
    theme: DashboardTheme,
    onClick: () -> Unit
) {
    val severityColor = when (insight.severity) {
        InsightSeverity.CRITICAL -> AccentCritical
        InsightSeverity.WARNING -> AccentWarning
        InsightSeverity.INFO -> AccentPrimary
    }
    val categoryColor = when (insight.category) {
        InsightCategory.EFFICIENCY -> AccentSuccess
        InsightCategory.ENERGY -> AccentWarning
        InsightCategory.QUALITY -> AccentPrimary
        InsightCategory.MAINTENANCE -> AccentCritical
        InsightCategory.PROCESS -> AccentAI
    }
    val trendIcon = when (insight.trend) { InsightTrend.UP -> "▲"; InsightTrend.DOWN -> "▼"; InsightTrend.STABLE -> "●" }
    val trendColor = when (insight.trend) { InsightTrend.UP -> AccentCritical; InsightTrend.DOWN -> AccentWarning; InsightTrend.STABLE -> AccentSuccess }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.trackBg.copy(alpha = 0.20f))
            .border(0.5.dp, severityColor.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(32.dp).clip(RoundedCornerShape(2.dp)).background(severityColor))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(insight.title, color = theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(insight.category.name, color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(categoryColor.copy(alpha = 0.10f)).padding(horizontal = 3.dp, vertical = 1.dp))
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(insight.stage, color = theme.textLightMuted, fontSize = 11.sp)
                Text("·", color = theme.textLightMuted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(trendIcon, color = trendColor, fontSize = 11.sp)
                    Text(insight.metricValue, color = severityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(insight.severity.name, color = severityColor, fontSize = 10.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(severityColor.copy(alpha = 0.12f)).padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

// ─── ALERTS PANEL ───────────────────────────────────────────────────────────

@Composable
fun AlertsPanel(modifier: Modifier, theme: DashboardTheme, activeAlerts: List<AlertData>, onNavigateToScreen: (String) -> Unit) {
    CleanPanel(theme, modifier = modifier) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Active Alerts Log", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Medium); Text("Synced via Digital Twin API", color = theme.textLightMuted, fontSize = 11.sp) }
                Text("${activeAlerts.count { !it.acknowledged }} Active", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            if (activeAlerts.isEmpty()) {
                Text("System clear. No active bottlenecks.", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(activeAlerts, key = { it.id }) { alert ->
                        val isAck = alert.acknowledged
                        val color = if (isAck) BrandSteelGray else when (alert.priority.uppercase()) { "CRITICAL" -> AccentCritical; "WARNING" -> AccentWarning; else -> AccentPrimary }
                        val canNavigate = alert.sourceRoute == "energy_tab" || alert.sourceRoute == "production_tab"
                        Row(
                            modifier = Modifier.fillMaxWidth().background(if (isAck) Color.Transparent else theme.trackBg.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(0.5.dp, color.copy(alpha = if (isAck) 0.08f else 0.25f), RoundedCornerShape(10.dp))
                                .clickable(enabled = canNavigate) {
                                    val route = when (alert.sourceRoute) {
                                        "energy_tab" -> "${AppDestinations.ENERGY_TAB}?section=${alert.targetSection ?: alert.stage}&alertId=${alert.targetAlertId ?: alert.id}"
                                        "production_tab" -> "${AppDestinations.PRODUCTION_TAB}?section=${alert.targetSection ?: alert.stage}&alertId=${alert.targetAlertId ?: alert.id}"
                                        else -> null
                                    }
                                    route?.let(onNavigateToScreen)
                                }.padding(9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (isAck) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = color, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(alert.message, color = if (isAck) theme.textMuted else theme.textMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${alert.stage} • ${alert.priority}", color = color, fontSize = 11.sp)
                            }
                            if (isAck) Text("ACK", color = BrandSteelGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ─── AYAM FAB ───────────────────────────────────────────────────────────────

@Composable
fun AyamAssistantFab(modifier: Modifier = Modifier, mood: AyamMood, onClick: (() -> Unit)? = null) {
    val pulseColor = when (mood) { AyamMood.NORMAL -> AccentSuccess; AyamMood.WARNING -> AccentWarning; AyamMood.CRITICAL -> AccentCritical }
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val pulseAlpha by infiniteTransition.animateFloat(0.4f, 0f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "pulseAlpha")
    val pulseScale by infiniteTransition.animateFloat(1f, 1.5f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "pulseScale")

    Box(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        contentAlignment = Alignment.Center
    ) {
        if (mood != AyamMood.NORMAL) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha).border(2.dp, pulseColor, CircleShape))
        }
        Image(
            painter = painterResource(id = R.drawable.ayam_image),
            contentDescription = "Ayam Assistant",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun AyamHelloBubble(
    modifier: Modifier = Modifier,
    theme: DashboardTheme,
    mood: AyamMood,
    onSpeakNow: (String) -> Unit = {}
) {
    val shape = RoundedCornerShape(14.dp)
    val (mainText, accentColor) = when (mood) {
        AyamMood.NORMAL -> "All systems nominal" to AccentSuccess
        AyamMood.WARNING -> "Warnings detected" to AccentWarning
        AyamMood.CRITICAL -> "Critical attention needed" to AccentCritical
    }
    val scale by animateFloatAsState(
        targetValue = if (mood == AyamMood.NORMAL) 1f else 1.04f,
        animationSpec = tween(350),
        label = "ayamHelloScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(8.dp, shape, spotColor = Color.Black.copy(alpha = 0.12f), ambientColor = Color.Black.copy(alpha = 0.06f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.94f),
                        Color.White.copy(alpha = 0.70f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.95f), accentColor.copy(alpha = 0.4f))),
                shape
            )
            .clickable { onSpeakNow(mainText) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
            Text(
                text = mainText,
                color = BrandDeepNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}
@Composable
private fun AyamBotWithBubble(
    theme: DashboardTheme,
    mood: AyamMood,
    snappedEdge: String, // "left" or "right"
    rotationZ: Float,
    showBubble: Boolean,
    onToggle: () -> Unit
) {
    Layout(
        content = {
            // 0: Bot
            AyamAssistantFab(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer(rotationZ = rotationZ),
                mood = mood,
                onClick = onToggle
            )

            // 1: Bubble
            if (showBubble) {
                AyamHelloBubble(
                    theme = theme,
                    mood = mood,
                    onSpeakNow = { onToggle() }
                )
            }
        }
    ) { measurables, constraints ->

        val botSizePx = 72.dp.roundToPx()

        // Measure bot at fixed size
        val botPlaceable = measurables[0].measure(Constraints.fixed(botSizePx, botSizePx))

        // Measure bubble with the *incoming* constraints (screen), not bot constraints
        val bubblePlaceable =
            if (showBubble && measurables.size > 1) {
                measurables[1].measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0
                    )
                )
            } else null

        // IMPORTANT: layout size stays bot-size (so drag/tap area stays correct)
        layout(botPlaceable.width, botPlaceable.height) {
            botPlaceable.placeRelative(0, 0)

            bubblePlaceable?.let { b ->
                val gap = 10.dp.roundToPx()
                val y = (-26).dp.roundToPx() // raise bubble a bit

                val x = if (snappedEdge == "left") {
                    // bot peeking on left -> bubble goes right (into screen)
                    botPlaceable.width + gap
                } else {
                    // bot peeking on right -> bubble goes left (into screen)
                    -b.width - gap
                }

                b.placeRelative(x, y)
            }
        }
    }
}


// ─── AYAM ASSISTANT POPUP ───────────────────────────────────────────────────

@Composable
fun AyamAssistantPopup(
    modifier: Modifier = Modifier,
    theme: DashboardTheme,
    activeAlerts: List<AlertData>,
    suggestions: List<RecommendationData>,
    ayamMood: AyamMood,
    onNavigateToScreen: (String) -> Unit,
    onClose: () -> Unit
) {
    val frequentStages = remember(activeAlerts) {
        activeAlerts.groupBy { it.stage }.filter { (_, alerts) -> alerts.size >= 2 }.keys
    }
    val topSuggestions = remember(suggestions) { suggestions.take(5) }
    val hasCritical = activeAlerts.any { !it.acknowledged && it.priority.equals("CRITICAL", ignoreCase = true) }
    val hasWarning = activeAlerts.any { !it.acknowledged && it.priority.equals("WARNING", ignoreCase = true) }

    val expressionTitle = when { hasCritical -> "Concerned"; hasWarning -> "Alert"; else -> "Calm" }
    val expressionText = when { hasCritical -> "Critical issues require immediate attention."; hasWarning -> "Some warnings need your review."; else -> "Plant operating within normal parameters." }
    val expressionColor = when { hasCritical -> AccentCritical; hasWarning -> AccentWarning; else -> AccentSuccess }

    var recognizedText by remember { mutableStateOf<String?>(null) }
    var ayamReply by remember { mutableStateOf<String?>(null) }

    fun fakeVoiceInteraction() {
        recognizedText = "User: Help me reduce energy in evaporation."
        ayamReply = "Ayam: Check steam pressure and vacuum in Evaporation. Try reducing steam flow by 5%."
    }

    Box(modifier = modifier) {
        CleanPanel(
            theme = theme,
            cornerRadius = 22.dp,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp).widthIn(max = 380.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.matchParentSize().blur(18.dp).background(Color.White.copy(alpha = 0.72f)))

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pushes header text safely past the overlapping image
                            Spacer(modifier = Modifier.width(56.dp))
                            Column {
                                Text("Ayam Assistant", color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(expressionTitle, color = expressionColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, null, tint = theme.textMuted) }
                    }

                    Column(modifier = Modifier.fillMaxWidth().background(expressionColor.copy(alpha = 0.05f), RoundedCornerShape(10.dp)).padding(10.dp)) {
                        Text("Status", color = expressionColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(expressionText, color = theme.textMain, fontSize = 12.sp)
                    }

                    if (frequentStages.isNotEmpty()) {
                        Text("Recurring alerts", color = AccentCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        frequentStages.forEach { st ->
                            Text("• $st – tap to drill into Energy view", color = theme.textMain, fontSize = 11.sp,
                                modifier = Modifier.clickable { onNavigateToScreen("${AppDestinations.ENERGY_TAB}?section=$st") }.padding(vertical = 2.dp))
                        }
                    }

                    if (topSuggestions.isNotEmpty()) {
                        Text("Recommendations", color = AccentAI, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            topSuggestions.forEach { rec ->
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(theme.trackBg.copy(alpha = 0.2f))
                                        .border(0.5.dp, theme.textMuted.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToScreen(AppDestinations.WORKFLOW_DASHBOARD) }.padding(10.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(rec.stage, color = theme.textMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(rec.priority, color = if (rec.priority.lowercase() in listOf("critical", "high")) AccentCritical else AccentWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(rec.issue, color = theme.textMain, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(rec.action, color = AccentAI, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentPrimary.copy(alpha = 0.12f)).clickable { fakeVoiceInteraction() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Mic, "Speak to Ayam", tint = AccentPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Speak to Ayam", color = theme.textMuted, fontSize = 12.sp)
                            Text("Voice + translate enabled", color = theme.textLightMuted, fontSize = 11.sp)
                        }
                    }

                    recognizedText?.let { Text(it, color = theme.textMain, fontSize = 12.sp) }
                    ayamReply?.let { Text(it, color = AccentAI, fontSize = 12.sp) }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(76.dp)
                .zIndex(10f)
                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.12f), ambientColor = Color.Black.copy(alpha = 0.06f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), BrandLightGray.copy(alpha = 0.5f))))
                .border(2.dp, Brush.linearGradient(listOf(Color.White, expressionColor.copy(alpha = 0.6f))), CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ayam_image),
                contentDescription = "Ayam Assistant",
                modifier = Modifier.matchParentSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ─── UTILITY COMPOSABLES ────────────────────────────────────────────────────

@Composable
fun TelemetryCol(label: String, value: String, theme: DashboardTheme) {
    Column { Text(label, color = theme.textLightMuted, fontSize = 11.sp); Text(value, color = theme.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun CleanPanel(theme: DashboardTheme, modifier: Modifier = Modifier, cornerRadius: Dp = 20.dp, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(cornerRadius); val shadowColor = if (theme.isDark) Color.Black else BrandDeepNavy.copy(alpha = 0.06f)
    Box(
        modifier = modifier.shadow(16.dp, shape, spotColor = shadowColor, ambientColor = shadowColor)
            .clip(shape).background(Brush.linearGradient(if (theme.isDark) listOf(BrandDarkBlueGray.copy(alpha = 0.45f), BrandSteelGray.copy(alpha = 0.15f)) else listOf(BrandOffWhite.copy(alpha = 0.88f), BrandLightGray.copy(alpha = 0.30f)), Offset(0f, 0f), Offset(0f, Float.POSITIVE_INFINITY)))
            .border(1.dp, Brush.linearGradient(if (theme.isDark) listOf(BrandLightGray.copy(alpha = 0.3f), Color.Transparent) else listOf(Color.White.copy(alpha = 0.95f), BrandLightGray.copy(alpha = 0.35f)), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)), shape),
        content = content
    )
}

@Composable
fun KpiCell(theme: DashboardTheme, title: String, value: String, unit: String, color: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = theme.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium); Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) { Text(value, color = theme.textMain, fontSize = 22.sp, fontWeight = FontWeight.Black); if (unit.isNotEmpty()) Text(" $unit", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 3.dp)) }
    }
}

@Composable
fun KpiRow(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = theme.textMuted, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) { Text(value, color = theme.textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(" $unit", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 1.dp)) }
    }
}

@Composable
fun KpiCellMini(theme: DashboardTheme, label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = theme.textLightMuted, fontSize = 11.sp); Spacer(Modifier.height(3.dp)); Text(value, color = theme.textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(unit, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

