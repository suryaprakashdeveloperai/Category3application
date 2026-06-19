package com.example.category3.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.category3.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 🎨 CORE BRAND COLOR SCHEME (Premium High-Contrast Light Theme)
val MinimalistTextPrimary = Color(0xFF111827)
val MinimalistTextMuted = Color(0xFF4B5563)
val PremiumAccentCore = Color(0xFF0EA5E9)
val PremiumSystemAlarm = Color(0xFFEF4444)

// ⚡ RE-ENGINEERED: EXACT LOGO ASSET COLOR TOKENS
val LogoTealCyan = Color(0xFF00A3A4)
val LogoCircuitCobalt = Color(0xFF2563EB)
val LogoSafetyAmber = Color(0xFFD97706)

// --- LIGHT MORPHIC GLASS DESIGN FORMULAS ---
val PremiumGlassFill = Color(0x0A111827)
val PremiumGlassBorder = Color(0x1F111827)
val PremiumBaseBackground = Color(0xFFF9FAFB)

private data class TelemetryNodeConfig(val positionOffset: Offset, val sampledLogoColor: Color)

@Composable
fun Modifier.premiumGlassmorphicPlate(shape: Shape): Modifier {
    return this
        .shadow(
            elevation = 50.dp,
            shape = shape,
            ambientColor = Color(0xFF111827).copy(alpha = 0.05f),
            spotColor = Color(0xFF111827).copy(alpha = 0.03f)
        )
        .background(PremiumGlassFill, shape = shape)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(PremiumGlassBorder, PremiumGlassBorder.copy(alpha = 0.010f)),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val globalRevealProgress = remember { Animatable(0f) }
    val containerWidthExpansion = remember { Animatable(0.01f) }
    val formUIAlpha = remember { Animatable(0f) }

    val logoVideoScale = remember { Animatable(2.5f) }
    val logoVideoAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "TelemetryPulsing")

    val telemetryPulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 26f,
        animationSpec = infiniteRepeatable(animation = tween(2800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "PulseRadius"
    )
    val telemetryPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0.0f,
        animationSpec = infiniteRepeatable(animation = tween(2800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "PulseAlpha"
    )

    val logoFloatingOffset by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(animation = tween(3200, easing = androidx.compose.animation.core.FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "LogoFloat"
    )

    val ambientGlowPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(animation = tween(3200, easing = androidx.compose.animation.core.FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "TealGlowPulse"
    )

    LaunchedEffect(Unit) {
        launch { logoVideoAlpha.animateTo(1f, animationSpec = tween(500, easing = LinearEasing)) }
        launch { logoVideoScale.animateTo(targetValue = 1f, animationSpec = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
        launch { containerWidthExpansion.animateTo(targetValue = 1f, animationSpec = tween(800, easing = androidx.compose.animation.core.CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))) }
        launch { globalRevealProgress.animateTo(1f, animationSpec = tween(600, easing = LinearEasing)) }
        launch { delay(400); formUIAlpha.animateTo(1f, animationSpec = tween(400, easing = LinearEasing)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBaseBackground),
        contentAlignment = Alignment.Center
    ) {

        // ============================================================================
        // 🌐 INDUSTRIAL BLUEPRINT MESH WITH LOGO-MATCHED NODES
        // ============================================================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacingPx = 40.dp.toPx()
            val gridColor = Color(0x06111827)

            var x = 0f
            while (x < size.width) {
                drawLine(start = Offset(x, 0f), end = Offset(x, size.height), color = gridColor, strokeWidth = 1.dp.toPx())
                x += gridSpacingPx
            }

            var y = 0f
            while (y < size.height) {
                drawLine(start = Offset(0f, y), end = Offset(size.width, y), color = gridColor, strokeWidth = 1.dp.toPx())
                y += gridSpacingPx
            }

            val multiColorTelemetryNodes = listOf(
                TelemetryNodeConfig(Offset(gridSpacingPx * 3, gridSpacingPx * 4), LogoTealCyan),
                TelemetryNodeConfig(Offset(size.width - (gridSpacingPx * 4), gridSpacingPx * 2), LogoCircuitCobalt),
                TelemetryNodeConfig(Offset(gridSpacingPx * 5, size.height - (gridSpacingPx * 3)), LogoSafetyAmber),
                TelemetryNodeConfig(Offset(size.width - (gridSpacingPx * 3), size.height - (gridSpacingPx * 4)), LogoTealCyan),
                TelemetryNodeConfig(Offset(size.width / 2f - (gridSpacingPx * 8), size.height / 2f + (gridSpacingPx * 4)), LogoCircuitCobalt),
                TelemetryNodeConfig(Offset(size.width / 2f + (gridSpacingPx * 8), size.height / 2f - (gridSpacingPx * 5)), LogoSafetyAmber)
            )

            multiColorTelemetryNodes.forEach { node ->
                drawCircle(
                    color = node.sampledLogoColor.copy(alpha = telemetryPulseAlpha),
                    radius = telemetryPulseRadius.dp.toPx(),
                    center = node.positionOffset
                )
                drawCircle(
                    color = node.sampledLogoColor.copy(alpha = 0.25f),
                    radius = 3.5.dp.toPx(),
                    center = node.positionOffset
                )
            }
        }

        // ============================================================================
        // CONTAINER SHEET HOUSING VIEWPORT FRAMES
        // ============================================================================
        Row(
            modifier = Modifier
                .width(900.dp * containerWidthExpansion.value)
                .height(540.dp)
                .alpha(globalRevealProgress.value)
                .clip(RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ============================================================================
            // CONTAINER 1: LEFT BRANDING PANE (🚀 Glow Embedded Directly Behind Logo)
            // ============================================================================
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .graphicsLayer {
                            scaleX = ambientGlowPulseScale
                            scaleY = ambientGlowPulseScale
                            translationY = logoFloatingOffset.dp.toPx()
                        }
                        .blur(150.dp)
                        .alpha(0.24f * globalRevealProgress.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(LogoTealCyan, Color.Transparent),
                                radius = 450f
                            ),
                            shape = RoundedCornerShape(100.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(44.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .graphicsLayer {
                                scaleX = logoVideoScale.value
                                scaleY = logoVideoScale.value
                                alpha = logoVideoAlpha.value
                                translationY = logoFloatingOffset.dp.toPx()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_auraliss),
                            contentDescription = "Auraliss Primary Brand Identity",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // ============================================================================
            // CONTAINER 2: RIGHT INTERACTION PANE
            // ============================================================================
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .premiumGlassmorphicPlate(shape = RoundedCornerShape(24.dp))
                    .padding(44.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Column(modifier = Modifier.fillMaxWidth().alpha(formUIAlpha.value)) {
                    Text(
                        text = "SIGN IN",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.25.sp
                    )
                    Text(
                        text = "Dynamic Auth Node Active...",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PremiumAccentCore,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; showError = false },
                        label = {
                            Text(
                                "OPERATOR ID",
                                fontFamily = FontFamily.SansSerif,
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalistTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif, color = MinimalistTextPrimary, fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PremiumAccentCore,
                            unfocusedBorderColor = PremiumGlassBorder,
                            focusedTextColor = MinimalistTextPrimary,
                            unfocusedTextColor = MinimalistTextPrimary,
                            focusedLabelColor = PremiumAccentCore,
                            unfocusedLabelColor = MinimalistTextMuted,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; showError = false },
                        label = {
                            Text(
                                "SECURITY PIN",
                                fontFamily = FontFamily.SansSerif,
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalistTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, null, tint = PremiumAccentCore)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif, color = MinimalistTextPrimary, fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PremiumAccentCore,
                            unfocusedBorderColor = PremiumGlassBorder,
                            focusedTextColor = MinimalistTextPrimary,
                            unfocusedTextColor = MinimalistTextPrimary,
                            focusedLabelColor = PremiumAccentCore,
                            unfocusedLabelColor = MinimalistTextMuted,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = showError) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .border(1.5.dp, PremiumSystemAlarm, RoundedCornerShape(50))
                                .background(PremiumSystemAlarm.copy(alpha = 0.08f))
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCESS DENIED - INVALID CREDENTIALS",
                                fontFamily = FontFamily.SansSerif,
                                style = MaterialTheme.typography.labelSmall,
                                color = PremiumSystemAlarm,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ============================================================================
                    // 🕹️ SKEUOMORPHIC HIGH-TACTILE CONTROL HARDWARE BUTTON
                    // ============================================================================
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                showError = false

                                val targetRoute = when {
                                    username == "1" && password == "1" -> AppDestinations.MILL_DASHBOARD
                                    username == "2" && password == "2" -> AppDestinations.FLOTATION_CLARIFIER
                                    username == "3" && password == "3" -> AppDestinations.FLOTATION_CLARIFIER
                                    username == "4" && password == "4" -> AppDestinations.VACCUM_PAN
                                    username == "5" && password == "5" -> AppDestinations.OPEN_PAN
                                    username == "6" && password == "6" -> AppDestinations.POWDER_MAKER
                                    username == "7" && password == "7" -> AppDestinations.QUALITY_CONTROL
                                    username == "8" && password == "8" -> AppDestinations.DCS_SCREEN
                                    else -> null
                                }

                                coroutineScope.launch {
                                    delay(1200)
                                    isLoading = false
                                    if (targetRoute != null) onLoginSuccess(targetRoute) else showError = true
                                }
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            // Double Outer Shadows (Industrial 3D Push-Button Spec)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = PremiumAccentCore.copy(alpha = 0.35f),
                                spotColor = Color(0xFF0F172A).copy(alpha = 0.4f)
                            ),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // High-contrast physical bevel gradient map
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            PremiumAccentCore.copy(alpha = 0.95f), // Shiny Top Rim Highlight
                                            PremiumAccentCore,
                                            Color(0xFF0284C7)                      // Deep Extruded Bevel Base
                                        )
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                // Concentric Skeuomorphic Bezel Ring Borders
                                .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                                .border(0.5.dp, Color(0xFF0369A1).copy(alpha = 0.6f), RoundedCornerShape(50))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    text = "AUTHORIZE INTERFACE",
                                    fontFamily = FontFamily.SansSerif,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    color = Color.White,
                                    modifier = Modifier.graphicsLayer {
                                        // Subtle internal offset mimicking 3D stamp engraving
                                        translationY = (-0.5).dp.toPx()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (containerWidthExpansion.value < 0.95f) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(540.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, PremiumAccentCore, PremiumAccentCore, Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                // Hardware transition line anchor
            }
        }
    }
}