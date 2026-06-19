package com.example.category3.auth.ui // Adjust this to match your exact package

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.innerShadow(
    color: Color = Color.Black,
    cornersRadius: Dp = 0.dp,
    spread: Dp = 0.dp,
    blur: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) = drawWithContent {
    // Draw the actual content of the composable first
    drawContent()

    val rect = Rect(Offset.Zero, size)

    // Using addRoundRect perfectly handles the corners without needing an extra PathEffect
    val shadowPath = Path().apply {
        addRoundRect(RoundRect(rect, CornerRadius(cornersRadius.toPx())))
    }

    val shadowPaint = Paint().apply {
        this.color = color // Assign the parameter color to the Paint object
        this.style = PaintingStyle.Stroke
        this.strokeWidth = spread.toPx()

        // Use asFrameworkPaint() to access native Android Paint properties for blurring
        if (blur.toPx() > 0) {
            this.asFrameworkPaint().maskFilter = BlurMaskFilter(
                blur.toPx(),
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    // clipRect requires a block { } to restrict the drawing boundaries
    clipRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom) {
        drawContext.canvas.save()
        drawContext.canvas.translate(offsetX.toPx(), offsetY.toPx())
        drawContext.canvas.drawPath(shadowPath, shadowPaint)
        drawContext.canvas.restore()
    }
}