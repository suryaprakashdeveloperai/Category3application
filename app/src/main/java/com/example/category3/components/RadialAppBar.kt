package com.example.category3.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.example.category3.R

// Strict Brand Palette
private val BrandDeepNavy = Color(0xFF0A0D2F)
private val BrandLightGray = Color(0xFFBCBCBF)
private val BrandOffWhite = Color(0xFFF6F6F7)
private val BrandCyanBlue = Color(0xFF47B3E2)
private val BrandMutedBlue = Color(0xFF496D89)
private val BrandTeal = Color(0xFF11CFC9)
private val BrandOrange = Color(0xFFF68420)
private val BrandSoftOrange = Color(0xFFD68A51)

// GreyFrost Glassmorphism Palette
private val FrostWhite = Color(0xFFFFFFFF)
private val FrostGray1 = Color(0xFFE8E8EA)
private val FrostGray2 = Color(0xFFD1D1D5)
private val FrostGray3 = Color(0xFFB0B0B5)
private val FrostTranslucent = Color(0x66EAEAEE)
private val FrostOverlay = Color(0x33FFFFFF)
private val FrostBorderTop = Color(0xBBFFFFFF)
private val FrostBorderBottom = Color(0x44BCBCBF)
private val FrostInnerGlow = Color(0x22FFFFFF)
private val FrostShadowAmbient = Color(0x1A0A0D2F)
private val FrostShadowSpot = Color(0x2D0A0D2F)

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
    activeSection: String = "workflow_dashboard",
    onActionSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val menuItems = remember {
        val staticDefs = listOf(
            Triple(Icons.Rounded.Dashboard, "workflow_dashboard", BrandTeal),
            Triple(Icons.Rounded.ElectricBolt, "energy_tab", BrandSoftOrange),
            Triple(Icons.Rounded.Factory, "production_tab", BrandCyanBlue),
            Triple(Icons.Rounded.Build, "maintenance_tab", BrandMutedBlue),
            Triple(Icons.Rounded.EditNote, "login_screen", BrandOrange)
        )

        staticDefs.mapIndexed { index, def ->
            val calculatedAngle = 270f + (180f * index / (staticDefs.size - 1))
            RadialMenuItem(
                icon = def.first,
                title = def.second,
                angleDegrees = calculatedAngle,
                actionId = def.second,
                iconTint = def.third
            )
        }
    }

    val trackSweep by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "track_sweep"
    )

    val maxRadius = 105.dp
    val buttonCenter = 40.dp
    val containerWidth = if (isExpanded) 160.dp else 90.dp

    Box(
        modifier = modifier
            .width(containerWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        // ── Frosted arc track with glassmorphism ──
        if (trackSweep > 0f) {
            // Blurred frost backdrop layer for the arc region
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .alpha(0.4f)
            ) {
                val strokeWidth = 68.dp.toPx()
                val currentRadius = maxRadius.toPx()
                val centerPoint = Offset(x = buttonCenter.toPx(), y = size.height / 2)
                val topLeft = Offset(centerPoint.x - currentRadius, centerPoint.y - currentRadius)

                drawArc(
                    brush = Brush.verticalGradient(
                        listOf(
                            FrostTranslucent,
                            FrostGray2.copy(alpha = 0.3f),
                            FrostTranslucent
                        ),
                        startY = centerPoint.y - currentRadius,
                        endY = centerPoint.y + currentRadius
                    ),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Main frosted glass arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 64.dp.toPx()
                val currentRadius = maxRadius.toPx()
                val centerPoint = Offset(x = buttonCenter.toPx(), y = size.height / 2)
                val topLeft =
                    Offset(centerPoint.x - currentRadius, centerPoint.y - currentRadius)

                // Outer shadow layer
                drawArc(
                    color = FrostShadowAmbient,
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Frosted glass fill – translucent grey gradient
                drawArc(
                    brush = Brush.verticalGradient(
                        listOf(
                            FrostWhite.copy(alpha = 0.65f),
                            FrostGray1.copy(alpha = 0.50f),
                            FrostGray2.copy(alpha = 0.40f),
                            FrostGray3.copy(alpha = 0.35f)
                        ),
                        startY = centerPoint.y - currentRadius,
                        endY = centerPoint.y + currentRadius
                    ),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Inner specular highlight – top edge frost shine
                drawArc(
                    brush = Brush.verticalGradient(
                        listOf(
                            FrostOverlay,
                            Color.Transparent,
                            Color.Transparent
                        ),
                        startY = centerPoint.y - currentRadius,
                        endY = centerPoint.y + currentRadius
                    ),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth - 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Outer border – subtle frost edge
                drawArc(
                    brush = Brush.verticalGradient(
                        listOf(
                            FrostBorderTop,
                            FrostBorderBottom
                        ),
                        startY = centerPoint.y - currentRadius,
                        endY = centerPoint.y + currentRadius
                    ),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // ── Radial menu items with frost glass style ──
        menuItems.forEachIndexed { index, item ->
            RadialMenuItemComposable(
                item = item,
                index = index,
                isExpanded = isExpanded,
                isActive = activeSection == item.actionId,
                maxRadius = maxRadius,
                buttonCenter = buttonCenter,
                onClick = { isExpanded = false; onActionSelected(item.actionId) }
            )
        }

        // ── Central toggle button with greyfrost glassmorphism ──
        val buttonShape = CircleShape
        Box(
            modifier = Modifier
                .offset(x = 16.dp)
                .size(48.dp)
        ) {
            // Frost blur backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .clip(buttonShape)
                    .background(FrostTranslucent)
            )

            // Main glass button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (isExpanded) 3.dp else 10.dp,
                        shape = buttonShape,
                        ambientColor = FrostShadowAmbient,
                        spotColor = FrostShadowSpot
                    )
                    .clip(buttonShape)
                    .background(
                        Brush.verticalGradient(
                            if (isExpanded) listOf(
                                FrostGray1.copy(alpha = 0.70f),
                                FrostGray2.copy(alpha = 0.55f),
                                FrostGray3.copy(alpha = 0.45f)
                            ) else listOf(
                                FrostWhite.copy(alpha = 0.85f),
                                FrostGray1.copy(alpha = 0.60f),
                                FrostGray2.copy(alpha = 0.50f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                FrostBorderTop,
                                FrostGray2.copy(alpha = 0.3f),
                                FrostBorderBottom
                            )
                        ),
                        buttonShape
                    )
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                // Inner specular highlight overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(buttonShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    FrostOverlay,
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 60f
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.auraliss_only_logo),
                    contentDescription = "Menu Toggle",
                    modifier = Modifier
                        .size(36.dp)
                        .alpha(if (isExpanded) 0.5f else 1f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun BoxScope.RadialMenuItemComposable(
    item: RadialMenuItem,
    index: Int,
    isExpanded: Boolean,
    isActive: Boolean = false,
    maxRadius: Dp,
    buttonCenter: Dp,
    onClick: () -> Unit
) {
    val itemReveal by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(450, if (isExpanded) index * 50 else 0, FastOutSlowInEasing),
        label = ""
    )
    val angleRadian = Math.toRadians(item.angleDegrees.toDouble())
    val currentRadius = maxRadius.value * itemReveal
    val offsetX = (kotlin.math.cos(angleRadian) * currentRadius).dp + buttonCenter
    val offsetY = (kotlin.math.sin(angleRadian) * currentRadius).dp

    val itemSize = 38.dp

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .size(itemSize)
            .offset(x = offsetX - (itemSize / 2), y = offsetY)
            .alpha(itemReveal)
            .scale(0.6f + (0.4f * itemReveal))
    ) {
        // Frost blur backdrop for each menu item
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .clip(CircleShape)
                .background(FrostTranslucent.copy(alpha = 0.5f))
        )

        // Glass menu item
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isActive) 8.dp else 5.dp,
                    shape = CircleShape,
                    ambientColor = if (isActive) item.iconTint.copy(alpha = 0.15f) else FrostShadowAmbient,
                    spotColor = if (isActive) item.iconTint.copy(alpha = 0.25f) else FrostShadowSpot
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        if (isActive) listOf(
                            FrostWhite.copy(alpha = 0.90f),
                            FrostGray1.copy(alpha = 0.75f),
                            item.iconTint.copy(alpha = 0.08f)
                        ) else listOf(
                            FrostWhite.copy(alpha = 0.75f),
                            FrostGray1.copy(alpha = 0.55f),
                            FrostGray2.copy(alpha = 0.45f)
                        )
                    )
                )
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    brush = Brush.verticalGradient(
                        if (isActive) listOf(
                            FrostBorderTop,
                            item.iconTint.copy(alpha = 0.3f),
                            FrostBorderBottom
                        ) else listOf(
                            FrostBorderTop,
                            FrostGray2.copy(alpha = 0.25f),
                            FrostBorderBottom
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(enabled = isExpanded) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Inner specular frost highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                FrostOverlay,
                                FrostInnerGlow,
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 50f
                        )
                    )
            )

            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isActive) item.iconTint else item.iconTint.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}