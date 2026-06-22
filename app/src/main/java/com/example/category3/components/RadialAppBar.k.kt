package com.example.category3.components

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Strict Brand Palette
private val BrandDeepNavy = Color(0xFF0A0D2F)
private val BrandLightGray = Color(0xFFBCBCBF)
private val BrandOffWhite = Color(0xFFF6F6F7)
private val BrandCyanBlue = Color(0xFF47B3E2)
private val BrandMutedBlue = Color(0xFF496D89)
private val BrandTeal = Color(0xFF11CFC9)
private val BrandSoftOrange = Color(0xFFD68A51)

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
    activeSection: String = "home",
    onActionSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Updated menu items using STRICT PALETTE
    val menuItems = remember {
        val staticDefs = listOf(
            Triple(Icons.Rounded.Dashboard, "HOME", BrandTeal),
            Triple(Icons.Rounded.Login, "DATA ENTRY", BrandCyanBlue),
            Triple(Icons.Rounded.Settings, "SETTINGS", BrandSoftOrange),
            Triple(Icons.Rounded.Info, "ABOUT US", BrandMutedBlue)
        )

        staticDefs.mapIndexed { index, def ->
            val calculatedAngle = 270f + (180f * index / (staticDefs.size - 1))
            RadialMenuItem(
                icon = def.first,
                title = def.second,
                angleDegrees = calculatedAngle,
                actionId = def.second.lowercase().replace(" ", "_"),
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
                val topLeft = Offset(
                    x = centerPoint.x - currentRadius,
                    y = centerPoint.y - currentRadius
                )

                drawArc(
                    color = BrandDeepNavy.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = trackSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrandLightGray.copy(alpha = 0.5f),
                            BrandOffWhite,
                            Color.White
                        ),
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
                    onActionSelected(item.actionId)
                }
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 16.dp)
                .size(58.dp)
                .shadow(
                    elevation = if (isExpanded) 2.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = BrandDeepNavy.copy(alpha = 0.2f),
                    spotColor = BrandDeepNavy.copy(alpha = 0.3f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = if (isExpanded) {
                            listOf(BrandLightGray, BrandOffWhite)
                        } else {
                            listOf(Color.White, BrandOffWhite, BrandLightGray)
                        }
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            BrandLightGray,
                            BrandLightGray.copy(alpha = 0.4f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { isExpanded = !isExpanded }
                .rotate(buttonRotation),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = if (isExpanded) BrandDeepNavy.copy(alpha = 0.6f) else BrandDeepNavy,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BoxScope.RadialMenuItemComposable(
    item: RadialMenuItem,
    index: Int,
    isExpanded: Boolean,
    maxRadius: Dp,
    buttonCenter: Dp,
    onClick: () -> Unit
) {
    val itemReveal by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 450,
            delayMillis = if (isExpanded) index * 50 else 0,
            easing = FastOutSlowInEasing
        ),
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
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = BrandDeepNavy.copy(alpha = 0.15f),
                spotColor = BrandDeepNavy.copy(alpha = 0.25f)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, BrandOffWhite, BrandLightGray)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(colors = listOf(Color.White, BrandLightGray)),
                shape = CircleShape
            )
            .clickable(enabled = isExpanded) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = item.iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}