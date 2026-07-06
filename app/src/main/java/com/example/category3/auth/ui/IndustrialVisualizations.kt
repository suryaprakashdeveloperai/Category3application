package com.example.category3.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

// 1. GAUGE CHART (Arc with a needle)
@Composable
fun GaugeChart(value: Float, max: Float, color: Color, modifier: Modifier = Modifier) {
    val progress = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = modifier.padding(16.dp)) {
        val sweepAngle = 240f
        val startAngle = 150f
        val strokeW = 12.dp.toPx()

        // Background Arc
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        // Progress Arc
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        // Needle
        val needleAngle = startAngle + (sweepAngle * progress)
        rotate(degrees = needleAngle, pivot = center) {
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(center.x + (size.width / 2.5f), center.y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawCircle(color = color, radius = 8.dp.toPx(), center = center)
    }
}

// 2. BAR FILL (Vertical Tank/Bar)
@Composable
fun BarFill(value: Float, max: Float, color: Color, modifier: Modifier = Modifier) {
    val progress = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = modifier) {
        val corner = CornerRadius(8.dp.toPx())
        // Background track
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.2f),
            size = size,
            cornerRadius = corner
        )
        // Fill
        val fillHeight = size.height * progress
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height - fillHeight),
            size = Size(size.width, fillHeight),
            cornerRadius = corner
        )
    }
}

// 3. ARC PROGRESS (Circular Donut)
@Composable
fun ArcProgress(value: Float, max: Float, color: Color, modifier: Modifier = Modifier) {
    val progress = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = modifier.padding(8.dp)) {
        val strokeW = 14.dp.toPx()
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
    }
}

// 4. THERMOMETER (Bulb and Tube)
@Composable
fun ThermometerChart(temp: Float, maxTemp: Float, color: Color, modifier: Modifier = Modifier) {
    val progress = (temp / maxTemp).coerceIn(0f, 1f)
    Canvas(modifier = modifier.padding(vertical = 12.dp)) {
        val bulbRadius = size.width / 2
        val tubeWidth = size.width / 2.5f
        val tubeX = center.x - (tubeWidth / 2)
        val tubeHeight = size.height - bulbRadius

        // Background Tube
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.2f),
            topLeft = Offset(tubeX, 0f),
            size = Size(tubeWidth, tubeHeight),
            cornerRadius = CornerRadius(tubeWidth / 2)
        )
        // Background Bulb
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = bulbRadius,
            center = Offset(center.x, size.height - bulbRadius)
        )

        // Fill Tube
        val fillHeight = tubeHeight * progress
        drawRoundRect(
            color = color,
            topLeft = Offset(tubeX, tubeHeight - fillHeight),
            size = Size(tubeWidth, fillHeight),
            cornerRadius = CornerRadius(tubeWidth / 2)
        )
        // Fill Bulb
        drawCircle(
            color = color,
            radius = bulbRadius - 2.dp.toPx(),
            center = Offset(center.x, size.height - bulbRadius)
        )
    }
}

// 5. RANGE BAR (Horizontal min/max acceptable operating range)
@Composable
fun RangeBar(current: Float, minTarget: Float, maxTarget: Float, maxScale: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val trackH = 6.dp.toPx()
        val centerY = size.height / 2

        // Full Track
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.2f),
            topLeft = Offset(0f, centerY - trackH / 2),
            size = Size(size.width, trackH),
            cornerRadius = CornerRadius(trackH)
        )

        // Acceptable Range (Green Zone)
        val startX = (minTarget / maxScale) * size.width
        val rangeW = ((maxTarget - minTarget) / maxScale) * size.width
        drawRoundRect(
            color = Color(0xFF11CFC9).copy(alpha = 0.4f),
            topLeft = Offset(startX, centerY - trackH),
            size = Size(rangeW, trackH * 2),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        // Current Value Indicator
        val currentX = (current / maxScale).coerceIn(0f, 1f) * size.width
        val dotColor = if (current in minTarget..maxTarget) Color(0xFF11CFC9) else Color(0xFFF68420)
        drawCircle(
            color = Color.White,
            radius = 10.dp.toPx(),
            center = Offset(currentX, centerY)
        )
        drawCircle(
            color = dotColor,
            radius = 6.dp.toPx(),
            center = Offset(currentX, centerY)
        )
    }
}

// 6. RADIAL FILL (Circular capacity fill from center)
@Composable
fun RadialFill(value: Float, max: Float, color: Color, modifier: Modifier = Modifier) {
    val progress = (value / max).coerceIn(0f, 1f)
    Canvas(modifier = modifier) {
        val maxRadius = size.minDimension / 2
        val currentRadius = maxRadius * progress

        // Outer bounds
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = maxRadius,
            style = Stroke(2.dp.toPx())
        )

        // Filled capacity
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = currentRadius
        )
        drawCircle(
            color = color,
            radius = currentRadius,
            style = Stroke(3.dp.toPx())
        )
    }
}