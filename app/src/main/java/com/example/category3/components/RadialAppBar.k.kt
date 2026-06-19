package com.example.category3.components

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.category3.auth.ui.PowderMakerState
import java.io.File
import java.io.FileWriter

private val SkPlateWhite = Color(0xFFF5F7FA)
private val SkPureBlack = Color(0xFF000000)
private val SkDeepNavy = Color(0xFF1A2233)
private val SkHardwareBorder = Color(0xFFD1D6E0)

data class RadialMenuItem(
    val icon: ImageVector,
    val title: String,
    val angleDegrees: Float,
    val actionId: String,
    val iconTint: Color
)

@Composable
fun RadialAppBar(
    modifier: Modifier = Modifier,
    activeSection: String = "home",                // ✅ Tracks the currently active structural frame context
    currentPmmState: PowderMakerState = PowderMakerState(),
    onActionSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val menuItems = remember {
        val staticDefs = listOf(
            Triple(Icons.Rounded.Dashboard, "HOME", Color(0xFF10B981)),
            Triple(Icons.Rounded.Settings, "MILL", Color(0xFF3B82F6)),
            Triple(Icons.Rounded.AccountTree, "DCS", Color(0xFF06B6D4)),
            Triple(Icons.Rounded.Whatshot, "PAN", Color(0xFFF59E0B)),
            Triple(Icons.Rounded.Build, "REPAIRS", Color(0xFFEF4444)),
            Triple(Icons.Rounded.Download, "DOWNLOAD", Color(0xFF0EA5E9)),
            Triple(Icons.Rounded.Logout, "LOGOUT", Color(0xFF7A828E))
        )

        staticDefs.mapIndexed { index, def ->
            val calculatedAngle = 270f + (180f * index / (staticDefs.size - 1))
            RadialMenuItem(
                icon = def.first,
                title = def.second,
                angleDegrees = calculatedAngle,
                actionId = def.second.lowercase(),
                iconTint = def.third
            )
        }
    }

    val trackSweep by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "track_sweep"
    )

    val buttonRotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "button_rotation"
    )

    val maxRadius = 135.dp
    val buttonCenter = 36.dp
    val containerWidth = if (isExpanded) 205.dp else 90.dp

    Box(
        modifier = modifier
            .width(containerWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        if (trackSweep > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 64.dp.toPx()
                val currentRadius = maxRadius.toPx()
                val centerPoint = Offset(x = buttonCenter.toPx(), y = size.height / 2)
                val topLeft = Offset(x = centerPoint.x - currentRadius, y = centerPoint.y - currentRadius)

                drawArc(
                    color = SkDeepNavy.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFDBDFE6), Color(0xFFEAECEF), Color(0xFFF1F3F6)),
                        startY = centerPoint.y - currentRadius,
                        endY = centerPoint.y + currentRadius
                    ),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth - 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        menuItems.forEachIndexed { index, item ->
            RadialMenuItemComposable(
                item = item,
                index = index,
                isExpanded = isExpanded,
                maxRadius = maxRadius,
                buttonCenter = buttonCenter,
                onClick = {
                    isExpanded = false
                    if (item.actionId == "download") {
                        // ✅ Route parsing branch dynamically handles different exports based on section context
                        executeDynamicSectionExport(context, activeSection, currentPmmState)
                    } else {
                        onActionSelected(item.actionId)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp)
                .size(58.dp)
                .shadow(elevation = if (isExpanded) 2.dp else 8.dp, shape = CircleShape, ambientColor = SkDeepNavy.copy(alpha = 0.2f), spotColor = SkDeepNavy.copy(alpha = 0.3f))
                .background(Brush.verticalGradient(colors = if (isExpanded) listOf(Color(0xFFD6D9E0), Color(0xFFEDEFF3)) else listOf(Color.White, SkPlateWhite, Color(0xFFE2E6EE))))
                .border(width = 1.5.dp, brush = Brush.verticalGradient(colors = listOf(Color.White, SkHardwareBorder, SkHardwareBorder.copy(alpha = 0.4f))), shape = CircleShape)
                .clickable { isExpanded = !isExpanded }
                .rotate(buttonRotation),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = if (isExpanded) SkPureBlack.copy(alpha = 0.6f) else SkPureBlack,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BoxScope.RadialMenuItemComposable(
    item: RadialMenuItem, index: Int, isExpanded: Boolean, maxRadius: Dp, buttonCenter: Dp, onClick: () -> Unit
) {
    val itemReveal by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = if (isExpanded) index * 50 else 0, easing = FastOutSlowInEasing),
        label = "item_reveal"
    )

    val angleRadian = Math.toRadians(item.angleDegrees.toDouble())
    val currentRadius = maxRadius.value * itemReveal
    val offsetX = (kotlin.math.cos(angleRadian) * currentRadius).dp + buttonCenter
    val offsetY = (kotlin.math.sin(angleRadian) * currentRadius).dp

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .size(46.dp)
            .offset(x = offsetX - 23.dp, y = offsetY)
            .alpha(itemReveal)
            .scale(0.6f + (0.4f * itemReveal))
            .shadow(elevation = 5.dp, shape = CircleShape, ambientColor = SkDeepNavy.copy(alpha = 0.15f), spotColor = SkDeepNavy.copy(alpha = 0.25f))
            .background(Brush.verticalGradient(colors = listOf(Color.White, SkPlateWhite, Color(0xFFE2E6EE))))
            .border(width = 1.dp, brush = Brush.verticalGradient(colors = listOf(Color.White, SkHardwareBorder)), shape = CircleShape)
            .clickable(enabled = isExpanded) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(22.dp))
    }
}

// ============================================================================
// CONTEXT-AWARE ROUTER EXPORT MANAGEMENT ENGINE
// ============================================================================
private fun executeDynamicSectionExport(context: Context, section: String, state: PowderMakerState) {
    when (section.lowercase()) {
        "mill" -> generateMillManualEntryCsv(context, state)
        "home" -> generateCrystallizationSystemCsv(context, state)
        else -> {
            // Fallback for general logs if other sections are mapped
            Toast.makeText(context, "No custom log data for sector: ${section.uppercase()}", Toast.LENGTH_SHORT).show()
        }
    }
}

// 🏭 1. EXCLUSIVE LOGISTICS ENGINE FOR THE MILL SECTION MANUAL ENTRY CSV
private fun generateMillManualEntryCsv(context: Context, state: PowderMakerState) {
    try {
        val targetFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MILL_ManualEntry_Batch_${state.batchNo}.csv")
        val writer = FileWriter(targetFile)

        writer.append("MILL SECTION INDUSTRIAL MATRIX,MANUAL FIELD RECORD,LOG DATA SCALE\n")
        writer.append("Associated Machine ID,${state.mcNo},Factory Asset String\n")
        writer.append("Refinery Batch Number Reference,${state.batchNo},Manufacturing Run ID\n")
        writer.append("Raw Mill Input Material Feedstock,${state.openPanMaterial},Composition Class\n")
        writer.append("Powder Maker Machine (PMM) Operational Frequency,${state.pmmFrequencyHz},Hertz (Hz)\n")
        writer.append("Material Dropping Stage - R1 Phase Window,${state.droppingR1Min},Minutes\n")
        writer.append("Material Dropping Stage - F Intermediate Phase,${state.droppingFMin},Minutes\n")
        writer.append("Material Dropping Stage - R2 Settling Phase,${state.droppingR2Min},Minutes\n")
        writer.append("Calculated Cumulative Mill Run Cycle Time,${state.totalCycleTimeMin},Minutes\n")
        writer.append("Soda Addition Powder Charge Payload,${state.sodaAdditionQty},Grams\n")
        writer.append("Alkaline Solution Dispersion Dissolve Time,${state.sodaTotalTimeMin},Minutes\n")

        val cleanRemarks = state.remarks.replace(",", ";").replace("\n", " ")
        writer.append("Mill Room Anomalies & Engineering Notes,$cleanRemarks,String Logs\n")
        writer.append("Data Authenticator Token,M-MILL SECURITY LOCK CO-SIGNED,Trace Verification\n")

        writer.flush()
        writer.close()
        Toast.makeText(context, "Mill Manual CSV Generated Successfully!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Mill Export Aborted: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ❄️ 2. ORIGINAL PROFILE FOR THE CORE CRYSTALLIZATION / MAIN MONITOR FLIGHT
private fun generateCrystallizationSystemCsv(context: Context, state: PowderMakerState) {
    try {
        val targetFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PMM_CrystalProfile_Batch_${state.batchNo}.csv")
        val writer = FileWriter(targetFile)

        writer.append("CRYSTALLIZATION SECTOR LOGS,LOGGED STATE VALUE,UNIT BOUNDS\n")
        writer.append("Machine Number,${state.mcNo},ID String\n")
        writer.append("Batch Identifier,${state.batchNo},ID String\n")
        writer.append("Open Pan Drop Start,${state.opDropStart},HH:MM\n")
        writer.append("Open Pan Drop End,${state.opDropEnd},HH:MM\n")
        writer.append("Open Pan Drop Total,${state.opDropTotalMin},Minutes\n")
        writer.append("Crystallization Start,${state.crystalStart},HH:MM\n")
        writer.append("Crystallization End,${state.crystalEnd},HH:MM\n")
        writer.append("Crystallization Total Duration,${state.crystalTotalMin},Minutes\n")
        writer.append("Crystallization Frequency,${state.crystalFrequencyHz},Hz\n")
        writer.append("Process Cycle Start,${state.cycleStart},HH:MM\n")
        writer.append("Process Cycle End,${state.cycleEnd},HH:MM\n")
        writer.append("Process Cycle Total,${state.cycleTotalMin},Minutes\n")
        writer.append("Chamber Vacuum Pressure,${state.cycleVacuum},mm Hg\n")

        writer.flush()
        writer.close()
        Toast.makeText(context, "Crystallization CSV Saved to Documents!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}