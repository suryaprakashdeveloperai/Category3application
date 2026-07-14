package com.example.category3.auth.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
private val FrostGray = Color(0xFFE8E8EC).copy(alpha = 0.4f)
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
    val thresholdsMap = ThresholdManager.thresholds
        .map { liveVals ->
            mapOf(
                "BOILER" to listOf(
                    ThresholdParam("STEAM_PRESSURE_MIN", "Steam Pressure Min", liveVals["STEAM_PRESSURE_MIN"] ?: 10f, "Bar", 0.5f, 0f, 20f),
                    ThresholdParam("STEAM_PRESSURE_MAX", "Steam Pressure Max", liveVals["STEAM_PRESSURE_MAX"] ?: 14f, "Bar", 0.5f, 0f, 20f),
                    ThresholdParam("STEAM_FLOW_MAX", "Steam Flow Max", liveVals["STEAM_FLOW_MAX"] ?: 6000f, "kg/hr", 100f, 1000f, 10000f),
                    ThresholdParam("CONDENSER_TEMP_MAX", "Condenser Temp Max", liveVals["CONDENSER_TEMP_MAX"] ?: 45f, "°C", 1f, 20f, 100f)
                ),
                "MILLING" to listOf(
                    ThresholdParam("CANE_CUTTER_AMPS_MAX", "Cane Cutter", liveVals["CANE_CUTTER_AMPS_MAX"] ?: 180f, "A", 5f, 50f, 300f),
                    ThresholdParam("FIBERIZOR_AMPS_MAX", "Fiberizor", liveVals["FIBERIZOR_AMPS_MAX"] ?: 220f, "A", 5f, 50f, 350f),
                    ThresholdParam("MILL_MOTOR_AMPS_MAX", "Mill Motor", liveVals["MILL_MOTOR_AMPS_MAX"] ?: 20f, "A", 1f, 5f, 50f),
                    ThresholdParam("MILLING_TPH_MIN", "Throughput Min", liveVals["MILLING_TPH_MIN"] ?: 8f, "T/hr", 0.5f, 0f, 50f)
                ),
                "EVAPORATION" to listOf(
                    ThresholdParam("EVAP_BODY1_TEMP_MAX", "Body 1 Temp", liveVals["EVAP_BODY1_TEMP_MAX"] ?: 115f, "°C", 1f, 80f, 150f),
                    ThresholdParam("EVAP_VACUUM_MIN", "Vacuum Min", liveVals["EVAP_VACUUM_MIN"] ?: 700f, "mmHg", 10f, 400f, 800f),
                    ThresholdParam("EVAP_BODY5_BRIX_MIN", "Body 5 Brix", liveVals["EVAP_BODY5_BRIX_MIN"] ?: 18f, "°Bx", 0.5f, 0f, 40f)
                ),
                "CONCENTRATION" to listOf(
                    ThresholdParam("FCE_VACUUM_MIN", "FCE Vacuum", liveVals["FCE_VACUUM_MIN"] ?: 600f, "mmHg", 10f, 300f, 750f),
                    ThresholdParam("FCE_BRIX_MIN", "FCE Brix Min", liveVals["FCE_BRIX_MIN"] ?: 65f, "°Bx", 0.5f, 40f, 90f),
                    ThresholdParam("FCE_BRIX_MAX", "FCE Brix Max", liveVals["FCE_BRIX_MAX"] ?: 75f, "°Bx", 0.5f, 40f, 95f),
                    ThresholdParam("MIN_PANS_RUNNING", "Min Pans", liveVals["MIN_PANS_RUNNING"] ?: 2f, "Nos", 1f, 0f, 4f),
                    ThresholdParam("OPAN_TEMP_MAX", "Pan Temp Max", liveVals["OPAN_TEMP_MAX"] ?: 110f, "°C", 1f, 60f, 140f)
                ),
                "PUMPS" to listOf(
                    ThresholdParam("FEED_WATER_PUMP_MAX", "Feed Water", liveVals["FEED_WATER_PUMP_MAX"] ?: 50f, "A", 1f, 10f, 100f),
                    ThresholdParam("CONDENSER_PUMP_MAX", "Condenser", liveVals["CONDENSER_PUMP_MAX"] ?: 65f, "A", 1f, 10f, 100f),
                    ThresholdParam("VACUUM_PUMP_MAX", "Vacuum", liveVals["VACUUM_PUMP_MAX"] ?: 35f, "A", 1f, 10f, 80f)
                ),
                "POWER" to listOf(
                    ThresholdParam("LV_VOLTAGE_MIN", "LV Min", liveVals["LV_VOLTAGE_MIN"] ?: 400f, "V", 5f, 300f, 500f),
                    ThresholdParam("LV_VOLTAGE_MAX", "LV Max", liveVals["LV_VOLTAGE_MAX"] ?: 430f, "V", 5f, 300f, 500f),
                    ThresholdParam("HV_VOLTAGE_MIN", "HV Min", liveVals["HV_VOLTAGE_MIN"] ?: 6400f, "V", 100f, 5000f, 8000f),
                    ThresholdParam("POWER_FACTOR_MIN", "PF Min", liveVals["POWER_FACTOR_MIN"] ?: 0.85f, "pf", 0.01f, 0.5f, 1f)
                ),
                "PROCESS" to listOf(
                    ThresholdParam("DEFECATOR_PH_MIN", "pH Min", liveVals["DEFECATOR_PH_MIN"] ?: 6.5f, "pH", 0.1f, 4f, 7f),
                    ThresholdParam("DEFECATOR_PH_MAX", "pH Max", liveVals["DEFECATOR_PH_MAX"] ?: 8f, "pH", 0.1f, 7f, 10f),
                    ThresholdParam("YIELD_EFFICIENCY_MIN", "Yield %", liveVals["YIELD_EFFICIENCY_MIN"] ?: 85f, "%", 0.5f, 50f, 100f),
                    ThresholdParam("RAW_JUICE_TEMP_MAX", "Juice Temp", liveVals["RAW_JUICE_TEMP_MAX"] ?: 40f, "°C", 1f, 20f, 80f)
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
            .background(Color(0xFFF5F5F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    "Back",
                    tint = NothingBlack,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateBack() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "SYSTEM_ADMIN",
                        color = NothingRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Thresholds",
                        color = NothingBlack,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            // Compact Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NothingBlack else Color.White)
                            .border(
                                0.5.dp,
                                if (isSelected) NothingBlack else BorderGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            category,
                            color = if (isSelected) NothingWhite else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Compact List
            val currentItems = thresholdsMap[selectedCategory] ?: emptyList()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentItems, key = { it.key }) { param ->
                    CompactThresholdCard(param = param) { newValue ->
                        viewModel.updateThreshold(param.key, newValue)
                    }
                }
            }
        }

        // Compact Bottom Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFFF5F5F7), Color(0xFFF5F5F7)),
                        endY = 80f
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NothingBlack)
                    .clickable { onNavigateBack() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(NothingRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DEPLOY",
                    color = NothingWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactThresholdCard(
    param: ThresholdParam,
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember(param.key) { mutableStateOf(param.value) }

    LaunchedEffect(param.value) {
        if (param.value != sliderValue) sliderValue = param.value
    }

    fun setValue(rawValue: Float) {
        val clamped = rawValue.coerceIn(param.min, param.max)
        val stepped = if (param.step > 0f) {
            val index = ((clamped - param.min) / param.step).roundToInt()
            (param.min + index * param.step).coerceIn(param.min, param.max)
        } else clamped

        if (stepped != sliderValue) {
            sliderValue = stepped
            onValueChange(stepped)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Label | Value | Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label (truncated if needed)
                Text(
                    param.label,
                    color = NothingBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Value Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (param.step % 1.0f == 0f)
                            String.format("%.0f", sliderValue)
                        else
                            String.format("%.1f", sliderValue),
                        color = NothingBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        param.unit,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // +/- Buttons (Compact)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (sliderValue > param.min) FrostGray else Color.Transparent)
                            .clickable(enabled = sliderValue > param.min) { setValue(sliderValue - param.step) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            null,
                            tint = if (sliderValue > param.min) NothingBlack else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (sliderValue < param.max) FrostGray else Color.Transparent)
                            .clickable(enabled = sliderValue < param.max) { setValue(sliderValue + param.step) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            null,
                            tint = if (sliderValue < param.max) NothingBlack else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Compact Slider
            Slider(
                value = sliderValue,
                onValueChange = { setValue(it) },
                valueRange = param.min..param.max,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = NothingBlack,
                    activeTrackColor = NothingBlack,
                    inactiveTrackColor = BorderGray.copy(alpha = 0.5f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(NothingBlack, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            )
        }
    }
}