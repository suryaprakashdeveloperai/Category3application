package com.example.category3.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

// Theme Colors
private val NothingBlack = Color(0xFF0F0F0F)
private val NothingWhite = Color(0xFFF9F9F9)
private val NothingRed = Color(0xFFE50914)
private val FrostGray = Color(0xFFE8E8EC).copy(alpha = 0.6f)
private val TextMuted = Color(0xFF88888D)

data class ThresholdParam(
    val key: String,
    val label: String,
    val value: Float,
    val unit: String,
    val step: Float,
    val min: Float,
    val max: Float
)

class AdminViewModel : ViewModel() {
    // Automatically map the current dynamic thresholds into the UI structure
                val thresholdsMap = ThresholdManager.thresholds
                    .map { liveVals ->
                mapOf(
                    "BOILER" to listOf(
                        ThresholdParam("STEAM_PRESSURE_MIN", "Steam Pressure Min", liveVals["STEAM_PRESSURE_MIN"] ?: 10f, "Bar", 0.5f, 0f, 20f),
                        ThresholdParam("STEAM_PRESSURE_MAX", "Steam Pressure Max", liveVals["STEAM_PRESSURE_MAX"] ?: 14f, "Bar", 0.5f, 0f, 20f),
                        ThresholdParam("STEAM_FLOW_MAX", "Steam Flow Max", liveVals["STEAM_FLOW_MAX"] ?: 6000f, "kg/hr", 100f, 1000f, 10000f),
                        ThresholdParam("CONDENSER_TEMP_MAX", "Condenser Temp Max", liveVals["CONDENSER_TEMP_MAX"] ?: 45f, "°C", 1.0f, 20f, 100f)
                    ),

                    "MILLING" to listOf(
                        ThresholdParam("CANE_CUTTER_AMPS_MAX", "Cane Cutter Amps", liveVals["CANE_CUTTER_AMPS_MAX"] ?: 180f, "A", 5.0f, 50f, 300f),
                        ThresholdParam("FIBERIZOR_AMPS_MAX", "Fiberizor Amps", liveVals["FIBERIZOR_AMPS_MAX"] ?: 220f, "A", 5.0f, 50f, 350f),
                        ThresholdParam("MILL_MOTOR_AMPS_MAX", "Mill Motor Amps", liveVals["MILL_MOTOR_AMPS_MAX"] ?: 20f, "A", 1.0f, 5f, 50f),

                        // NEW (Production)
                        ThresholdParam("MILLING_TPH_MIN", "Milling Throughput Min", liveVals["MILLING_TPH_MIN"] ?: 8f, "T/hr", 0.5f, 0f, 50f)
                    ),

                    "EVAPORATION" to listOf(
                        ThresholdParam("EVAP_BODY1_TEMP_MAX", "Evap Body 1 Temp", liveVals["EVAP_BODY1_TEMP_MAX"] ?: 115f, "°C", 1.0f, 80f, 150f),
                        ThresholdParam("EVAP_VACUUM_MIN", "Evap Vacuum Min", liveVals["EVAP_VACUUM_MIN"] ?: 700f, "mmHg", 10.0f, 400f, 800f),

                        // NEW (Production)
                        ThresholdParam("EVAP_BODY5_BRIX_MIN", "Evap Body 5 Brix Min", liveVals["EVAP_BODY5_BRIX_MIN"] ?: 18f, "°Bx", 0.5f, 0f, 40f)
                    ),

                    "CONCENTRATION" to listOf(
                        ThresholdParam("FCE_VACUUM_MIN", "FCE Vacuum Min", liveVals["FCE_VACUUM_MIN"] ?: 600f, "mmHg", 10.0f, 300f, 750f),
                        ThresholdParam("OPAN_AMPS_MAX", "Open Pan Amps", liveVals["OPAN_AMPS_MAX"] ?: 10f, "A", 0.5f, 1f, 30f),

                        // NEW (Production)
                        ThresholdParam("FCE_BRIX_MIN", "FCE Brix Min", liveVals["FCE_BRIX_MIN"] ?: 65f, "°Bx", 0.5f, 40f, 90f),
                        ThresholdParam("FCE_BRIX_MAX", "FCE Brix Max", liveVals["FCE_BRIX_MAX"] ?: 75f, "°Bx", 0.5f, 40f, 95f),
                        ThresholdParam("MIN_PANS_RUNNING", "Min Open Pans Running", liveVals["MIN_PANS_RUNNING"] ?: 2f, "Nos", 1f, 0f, 4f),
                        ThresholdParam("OPAN_TEMP_MAX", "Open Pan Temp Max", liveVals["OPAN_TEMP_MAX"] ?: 110f, "°C", 1f, 60f, 140f)
                    ),

                    "PUMPS" to listOf(
                        ThresholdParam("FEED_WATER_PUMP_MAX", "Feed Water Pump", liveVals["FEED_WATER_PUMP_MAX"] ?: 50f, "A", 1.0f, 10f, 100f),
                        ThresholdParam("CONDENSER_PUMP_MAX", "Condenser Pump", liveVals["CONDENSER_PUMP_MAX"] ?: 65f, "A", 1.0f, 10f, 100f),
                        ThresholdParam("VACUUM_PUMP_MAX", "Vacuum Pump", liveVals["VACUUM_PUMP_MAX"] ?: 35f, "A", 1.0f, 10f, 80f)
                    ),

                    "POWER" to listOf(
                        ThresholdParam("LV_VOLTAGE_MIN", "LV Voltage Min", liveVals["LV_VOLTAGE_MIN"] ?: 400f, "V", 5.0f, 300f, 500f),
                        ThresholdParam("LV_VOLTAGE_MAX", "LV Voltage Max", liveVals["LV_VOLTAGE_MAX"] ?: 430f, "V", 5.0f, 300f, 500f),
                        ThresholdParam("HV_VOLTAGE_MIN", "HV Voltage Min", liveVals["HV_VOLTAGE_MIN"] ?: 6400f, "V", 100.0f, 5000f, 8000f),
                        ThresholdParam("POWER_FACTOR_MIN", "Power Factor Min", liveVals["POWER_FACTOR_MIN"] ?: 0.85f, "pf", 0.01f, 0.5f, 1.0f),
                        ThresholdParam("TOTAL_ENERGY_MAX", "Total Energy Max", liveVals["TOTAL_ENERGY_MAX"] ?: 500f, "kW", 10f, 100f, 1000f)
                    ),

                    "PROCESS" to listOf(
                        ThresholdParam("DEFECATOR_PH_MIN", "Defecator pH Min", liveVals["DEFECATOR_PH_MIN"] ?: 6.5f, "pH", 0.1f, 4f, 7f),
                        ThresholdParam("DEFECATOR_PH_MAX", "Defecator pH Max", liveVals["DEFECATOR_PH_MAX"] ?: 8.0f, "pH", 0.1f, 7f, 10f),
                        ThresholdParam("YIELD_EFFICIENCY_MIN", "Yield Efficiency Min", liveVals["YIELD_EFFICIENCY_MIN"] ?: 85f, "%", 0.5f, 50f, 100f),

                        // NEW (Production)
                        ThresholdParam("RAW_JUICE_TEMP_MAX", "Raw Juice Temp Max", liveVals["RAW_JUICE_TEMP_MAX"] ?: 40f, "°C", 1f, 20f, 80f)
                    )
                )
            }
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

            fun updateThreshold(key: String, newValue: Float) {
                ThresholdManager.updateThreshold(key, newValue)
            }
        }

@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val thresholdsMap by viewModel.thresholdsMap.collectAsState()
    if (thresholdsMap.isEmpty()) return

    val categories = thresholdsMap.keys.toList()
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color.White, Color(0xFFF3F3F5))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    "Back",
                    tint = NothingBlack,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "SYSTEM_ADMIN",
                        color = NothingRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Thresholds Configuration",
                        color = NothingBlack,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) NothingBlack else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) NothingBlack else BorderGray,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedCategory = category }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            category,
                            color = if (isSelected) NothingWhite else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            val currentItems = thresholdsMap[selectedCategory] ?: emptyList()
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentItems, key = { it.key }) { param ->
                    ThresholdControlCard(param = param) { newValue ->
                        viewModel.updateThreshold(param.key, newValue)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.White, Color.White),
                        endY = 150f
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NothingBlack)
                    .clickable { onNavigateBack() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(NothingRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "DEPLOY CONFIGURATION",
                    color = NothingWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
fun ThresholdControlCard(
    param: ThresholdParam,
    onValueChange: (Float) -> Unit
) {
    // Local state representing the current value shown/controlled by the slider
    var sliderValue by remember(param.key) {
        mutableStateOf(param.value)
    }

    // Sync with external changes (e.g. from ThresholdManager)
    LaunchedEffect(param.value) {
        if (param.value != sliderValue) {
            sliderValue = param.value
        }
    }

    // Helper to apply clamping + stepping and call onValueChange
    fun setValue(rawValue: Float) {
        val clamped = rawValue.coerceIn(param.min, param.max)
        val stepped = if (param.step > 0f) {
            val index = ((clamped - param.min) / param.step).roundToInt()
            (param.min + index * param.step).coerceIn(param.min, param.max)
        } else {
            clamped
        }

        if (stepped != sliderValue) {
            sliderValue = stepped
            onValueChange(stepped)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                16.dp,
                RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.03f),
                ambientColor = Color.Black.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(FrostGray)
            .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        param.key,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        param.label,
                        color = NothingBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Current value + unit + +/- buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Decrement button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (sliderValue > param.min) Color(0xFFF5F5F5) else Color.Transparent
                            )
                            .clickable(enabled = sliderValue > param.min) {
                                setValue(sliderValue - param.step)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            "Decrease",
                            tint = if (sliderValue > param.min) NothingBlack else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (param.step % 1.0f == 0f)
                                String.format("%.0f", sliderValue)
                            else
                                String.format("%.2f", sliderValue),
                            color = NothingBlack,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            param.unit,
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Increment button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (sliderValue < param.max) Color(0xFFF5F5F5) else Color.Transparent
                            )
                            .clickable(enabled = sliderValue < param.max) {
                                setValue(sliderValue + param.step)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            "Increase",
                            tint = if (sliderValue < param.max) NothingBlack else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Optional dashed separator
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(1.dp)
            ) {
                drawLine(
                    color = BorderGray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // Slider control
            Slider(
                value = sliderValue,
                onValueChange = { value -> setValue(value) },
                valueRange = param.min..param.max,
                // You can set steps if you want discrete ticks visible:
                // steps = max(0, ((param.max - param.min) / param.step).roundToInt() - 1),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NothingBlack,
                    activeTrackColor = NothingBlack,
                    inactiveTrackColor = BorderGray.copy(alpha = 0.6f)
                )
            )
        }
    }
}