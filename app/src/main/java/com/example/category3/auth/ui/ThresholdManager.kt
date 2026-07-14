package com.example.category3.auth.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ThresholdManager {
    private val defaultThresholds = mapOf(
        // ENERGY / UTILITIES
        "STEAM_PRESSURE_MIN" to 10.0f,
        "STEAM_PRESSURE_MAX" to 14.0f,
        "STEAM_FLOW_MAX" to 6000.0f,
        "CONDENSER_TEMP_MAX" to 45.0f,
        "CANE_CUTTER_AMPS_MAX" to 180.0f,
        "FIBERIZOR_AMPS_MAX" to 220.0f,
        "MILL_MOTOR_AMPS_MAX" to 20.0f,
        "LV_VOLTAGE_MIN" to 400.0f,
        "LV_VOLTAGE_MAX" to 430.0f,
        "HV_VOLTAGE_MIN" to 6400.0f,
        "HV_VOLTAGE_MAX" to 6800.0f,
        "EVAP_BODY1_TEMP_MAX" to 115.0f,
        "EVAP_VACUUM_MIN" to 700.0f,
        "FCE_VACUUM_MIN" to 600.0f,
        "OPAN_AMPS_MAX" to 10.0f,
        "FEED_WATER_PUMP_MAX" to 50.0f,
        "CONDENSER_PUMP_MAX" to 65.0f,
        "VACUUM_PUMP_MAX" to 35.0f,
        "POWER_FACTOR_MIN" to 0.85f,
        "TOTAL_ENERGY_MAX" to 500.0f,

        // --- NEW ENERGY / UTILITIES ---
        "CANE_CARRIER_AMPS_MAX" to 25.0f,
        "COOLING_PUMP_AMPS_MAX" to 45.0f,
        "POWDER_MAKER_AMPS_MAX" to 20.0f,
        "CONDENSATE_TEMP_MAX" to 40.0f,

        // PROCESS / PRODUCTION
        "DEFECATOR_PH_MIN" to 6.5f,
        "DEFECATOR_PH_MAX" to 8.0f,
        "YIELD_EFFICIENCY_MIN" to 85.0f,
        "MILLING_TPH_MIN" to 8.0f,
        "MIN_PANS_RUNNING" to 2.0f,
        "OPAN_TEMP_MAX" to 110.0f,
        "RAW_JUICE_TEMP_MAX" to 40.0f,
        "FCE_BRIX_MIN" to 65.0f,
        "FCE_BRIX_MAX" to 75.0f,
        "EVAP_BODY5_BRIX_MIN" to 18.0f,

        // --- NEW PROCESS / PRODUCTION ---
        "RAW_JUICE_TANK_LEVEL_MAX" to 85.0f,
        "RAW_JUICE_TANK_LEVEL_MIN" to 15.0f,
        "CLEAR_JUICE_TANK_LEVEL_MAX" to 85.0f,
        "SYRUP_TANK_LEVEL_MAX" to 85.0f,
        "CANE_STOCK_MIN" to 20.0f,
        "EVAP_LEVEL_MIN" to 35.0f,
        "EVAP_LEVEL_MAX" to 65.0f,
        "EVAP_BODY1_PRESSURE_MAX" to 2.5f,
        "JUICE_HEATER_TEMP_MIN" to 65.0f,
        "JUICE_HEATER_TEMP_MAX" to 115.0f,
        "POWDER_MAKER_VACUUM_MIN" to 550.0f
    )

    private val _thresholds = MutableStateFlow(defaultThresholds)
    val thresholds = _thresholds.asStateFlow()

    fun get(key: String): Float =
        _thresholds.value[key] ?: defaultThresholds[key] ?: 0f

    fun updateThreshold(key: String, value: Float) {
        _thresholds.update { current ->
            current.toMutableMap().apply { put(key, value) }
        }
    }
}