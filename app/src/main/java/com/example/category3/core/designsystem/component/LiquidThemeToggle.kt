package com.example.category3.core.designsystem.component

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun LiquidThemeToggle(
    isDarkMode: Boolean,
    onThemeChange: () -> Unit
) {
    val transition = updateTransition(targetState = isDarkMode, label = "ThemeToggle")

    // The slider head offset coordinates
    val indicatorOffset by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "offset"
    ) { state ->
        if (state) 36.dp else 4.dp
    }

    // Give it a subtle structural stretching deformation while in mid-transit
    val indicatorWidth by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy) },
        label = "width"
    ) { 28.dp }

    val toggleBg by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB),
        animationSpec = tween(500), label = "toggleBg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF4B5563),
        animationSpec = tween(500), label = "iconColor"
    )

    Box(
        modifier = Modifier
            .width(68.dp)
            .height(36.dp)
            .background(toggleBg, RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onThemeChange() },
        contentAlignment = Alignment.CenterStart
    ) {
        // Liquid dynamic element sliding inside
        Box(
            modifier = Modifier
                .padding(start = indicatorOffset)
                .width(indicatorWidth)
                .height(28.dp)
                .background(if (isDarkMode) Color(0xFF111115) else Color.White, RoundedCornerShape(50))
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("☀️", fontSize = 12.sp, color = iconColor)
            Text("🌙", fontSize = 12.sp, color = iconColor)
        }
    }
}