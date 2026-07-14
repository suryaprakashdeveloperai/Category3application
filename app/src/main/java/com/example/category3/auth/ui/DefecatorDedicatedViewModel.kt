package com.example.category3.auth.ui

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.abs
// KPI model for Defecator page (separate from KpiDataMill to avoid clashes)
data class DefecatorKpiData(
    val title: String,
    val value: String,
    val changeString: String,
    val isGood: Boolean,
    val trendHistory: List<Float>
)

data class DefecatorChartData(
    val actual: Float,
    val target: Float,
    val design: Float
)

data class DefecatorDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val units: List<EquipmentData>,      // pumps/filters/floc/mud, etc
    val kpis: List<DefecatorKpiData>,
    val chart: DefecatorChartData,
    val processStability: Double         // 0..100 (simple score)
)

data class DefecatorProcessData(
    val pH: Float,
    val djTankLevel: Float,
    val djActivePumpA: Float,
    val djStandbyPumpA: Float,
    val heater1InletC: Float,
    val heater1OutletC: Float,
    val heater2OutletC: Float,
    val heater3SpC: Float,
    val heater3PvC: Float,
    val heater3SteamValvePct: Float,
    val srtBufferLevel: Float
)

data class FlocculantData(
    val pump1Status: EquipmentStatus,
    val pump1SpeedPct: Float,
    val pump2Status: EquipmentStatus,
    val pump2SpeedPct: Float
)

data class MudFilterData(
    val mudPump1Status: EquipmentStatus,
    val mudPump2Status: EquipmentStatus,
    val pcFilterVacuumPumpStatus: EquipmentStatus,
    val fc1SpeedPct: Float,
    val fc2SpeedPct: Float,
    val fc3SpeedPct: Float
)

data class ClearJuiceData(
    val tankLevel: Float,
    val activePumpA: Float,
    val standbyPumpA: Float,
    val flow: Float,
    val density: Float,
    val filterOn: Boolean,
    val heaterInletC: Float,
    val heaterOutletC: Float
)

data class DefecatorLiveState(
    val dashboard: DefecatorDashboardState,
    val process: DefecatorProcessData,
    val floc: FlocculantData,
    val mudFilter: MudFilterData,
    val clearJuice: ClearJuiceData,
    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)

// Optional helper if you want a simple “good/bad” color
fun boolToColor(good: Boolean, goodColor: Color, badColor: Color): Color =
    if (good) goodColor else badColor
class DefecatorDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "DEFECATOR_SSE"
        private const val SSE_URL =
            "https://associate-supplier-alternatives-millennium.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // Thresholds (tune)
        private const val PH_TARGET = 7.2f
        private const val PH_MIN_OK = 6.8f
        private const val PH_MAX_OK = 7.8f

        private const val PUMP_NEAR_ZERO_A = 0.5f
        private const val HEATER3_DEV_WARN_C = 5.0f

        private const val CJ_FLOW_TARGET_LHR = 10_000f
        private const val CJ_FLOW_DESIGN_LHR = 12_000f

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DefecatorDedicatedViewModel(userName, userRole) as T
        }
    }

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    // Seed so UI is never empty
    private val seedDashboard = DefecatorDashboardState(
        userName = userName,
        userRole = userRole,
        batchId = "BATCH-000",
        startTime = "—",
        sectionStatus = EquipmentStatus.STANDBY,
        units = emptyList(),
        kpis = emptyList(),
        chart = DefecatorChartData(0f, CJ_FLOW_TARGET_LHR, CJ_FLOW_DESIGN_LHR),
        processStability = 0.0
    )

    private val _state = MutableStateFlow(
        DefecatorLiveState(
            dashboard = seedDashboard,
            process = DefecatorProcessData(
                pH = 0f,
                djTankLevel = 0f,
                djActivePumpA = 0f,
                djStandbyPumpA = 0f,
                heater1InletC = 0f,
                heater1OutletC = 0f,
                heater2OutletC = 0f,
                heater3SpC = 0f,
                heater3PvC = 0f,
                heater3SteamValvePct = 0f,
                srtBufferLevel = 0f
            ),
            floc = FlocculantData(
                pump1Status = EquipmentStatus.STANDBY,
                pump1SpeedPct = 0f,
                pump2Status = EquipmentStatus.STANDBY,
                pump2SpeedPct = 0f
            ),
            mudFilter = MudFilterData(
                mudPump1Status = EquipmentStatus.STANDBY,
                mudPump2Status = EquipmentStatus.STANDBY,
                pcFilterVacuumPumpStatus = EquipmentStatus.STANDBY,
                fc1SpeedPct = 0f, fc2SpeedPct = 0f, fc3SpeedPct = 0f
            ),
            clearJuice = ClearJuiceData(
                tankLevel = 0f,
                activePumpA = 0f,
                standbyPumpA = 0f,
                flow = 0f,
                density = 0f,
                filterOn = false,
                heaterInletC = 0f,
                heaterOutletC = 0f
            ),
            alerts = emptyList(),
            connectionStatus = "CONNECTING",
            lastUpdated = 0L
        )
    )
    val state = _state.asStateFlow()

    init { startStream() }

    private fun startStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val request = Request.Builder()
            .url(SSE_URL)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        eventSource = EventSources.createFactory(sseClient)
            .newEventSource(request, object : EventSourceListener() {

                override fun onOpen(source: EventSource, response: Response) {
                    _state.update { it.copy(connectionStatus = "CONNECTED") }
                    Log.d(TAG, "SSE connected")
                }

                override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                    viewModelScope.launch(Dispatchers.Default) { parseAndUpdate(data) }
                }

                override fun onClosed(source: EventSource) {
                    _state.update { it.copy(connectionStatus = "DISCONNECTED") }
                    scheduleReconnect()
                }

                override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                    Log.e(TAG, "SSE failure: ${t?.message}")
                    _state.update { it.copy(connectionStatus = "RECONNECTING") }
                    scheduleReconnect()
                }
            })
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            delay(RECONNECT_DELAY_MS)
            startStream()
        }
    }

    private fun parseAndUpdate(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            // Uses the same IndustrialTelemetryRaw you already use in Mill VM
            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            fun tagAny(vararg keys: String): Float {
                for (k in keys) {
                    val v = tags[k]
                    if (v != null) return v.toFloat()
                }
                return 0f
            }

            // -------------------- DEFECATOR PROCESS --------------------
            val pH = tagAny("Deficator_pH", "Defecator_pH")

            val djTankLevel = tagAny("DJTank_Level", "DJTank_Level_Pct")
            val djActiveA = tagAny("DJTank_Pump(Active)_amps", "DJTank_ActivePump_A")
            val djStandbyA = tagAny("DJTank_Pump(Standby)_amps", "DJTank_StandbyPump_A")

            val h1In = tagAny("DJHeater1_InletTemp")
            val h1Out = tagAny("DJHeater1_OutletTemp")
            val h2Out = tagAny("DJHeater2_OutletTemp")
            val h3Sp = tagAny("DJHeater3_Temp_SP")
            val h3Pv = tagAny("DJHeater3_Temp_PV")
            val steamValve = tagAny("DJFinalHeater3_SteamCtrlValve")

            val srtBuffer = tagAny("SRT_BufferTank_Level_LT")

            val process = DefecatorProcessData(
                pH = pH,
                djTankLevel = djTankLevel,
                djActivePumpA = djActiveA,
                djStandbyPumpA = djStandbyA,
                heater1InletC = h1In,
                heater1OutletC = h1Out,
                heater2OutletC = h2Out,
                heater3SpC = h3Sp,
                heater3PvC = h3Pv,
                heater3SteamValvePct = steamValve,
                srtBufferLevel = srtBuffer
            )

            val phFault = (pH != 0f) && (pH < PH_MIN_OK || pH > PH_MAX_OK)
            val djPumpFault = djActiveA in 0f..PUMP_NEAR_ZERO_A
            val heater3Dev = abs(h3Pv - h3Sp)
            val heater3Fault = (h3Sp > 0f) && (heater3Dev > HEATER3_DEV_WARN_C)

            // -------------------- FLOCCULANT --------------------
            val flocc1StatusRaw = tagAny("Flocc_Pump1_Status")
            val flocc2StatusRaw = tagAny("Flocc_Pump2_Status")
            val flocc1Speed = tagAny("Flocc_Pump1_VFD_Speed")
            val flocc2Speed = tagAny("Flocc_Pump2_VFD_Speed")

            fun statusFrom01(x: Float): EquipmentStatus =
                if (x >= 1f) EquipmentStatus.RUNNING else EquipmentStatus.STANDBY

            val floc = FlocculantData(
                pump1Status = statusFrom01(flocc1StatusRaw),
                pump1SpeedPct = flocc1Speed,
                pump2Status = statusFrom01(flocc2StatusRaw),
                pump2SpeedPct = flocc2Speed
            )

            // -------------------- MUD / FILTER --------------------
            val mud1Raw = tagAny("MudPump1_Status (DOL Active)")
            val mud2Raw = tagAny("MudPump2_Status (DOL Standby)")
            val pcVacRaw = tagAny("PcFilter_VacuumPump_Status(DOL)")

            val fc1 = tagAny("FC1(VFD_Motor)")
            val fc2 = tagAny("FC2(VFD_Motor)")
            val fc3 = tagAny("FC3(VFD_Motor)")

            val mudFilter = MudFilterData(
                mudPump1Status = statusFrom01(mud1Raw),
                mudPump2Status = statusFrom01(mud2Raw),
                pcFilterVacuumPumpStatus = statusFrom01(pcVacRaw),
                fc1SpeedPct = fc1,
                fc2SpeedPct = fc2,
                fc3SpeedPct = fc3
            )

            // -------------------- CLEAR JUICE (still part of clarification) --------------------
            val cjTank = tagAny("Clear Juice Tank Level")
            val cjActiveA = tagAny("CJ_pump (Active)")
            val cjStandbyA = tagAny("CJ_pump (Standby)")
            val cjFlowRaw = tagAny("CJ_JuiceFlow")
            val cjDensity = tagAny("CJ_JuiceDensity")
            val cjFilter = tagAny("CJ_Filter")
            val cjHIn = tagAny("CJ_Heater_inlet_temp")
            val cjHOut = tagAny("CJ_Heater_Outlet_temp")

            // If CJ flow is small (like 9.12), it’s often m³/hr -> convert to L/hr
            val cjFlowLhr = if (cjFlowRaw in 0f..200f) cjFlowRaw * 1000f else cjFlowRaw

            val clearJuice = ClearJuiceData(
                tankLevel = cjTank,
                activePumpA = cjActiveA,
                standbyPumpA = cjStandbyA,
                flow = cjFlowLhr,
                density = cjDensity,
                filterOn = cjFilter >= 1f,
                heaterInletC = cjHIn,
                heaterOutletC = cjHOut
            )

            // -------------------- BUILD “UNITS” LIST FOR UI --------------------
            fun pumpStatusFromAmps(a: Float): EquipmentStatus = when {
                a == 0f -> EquipmentStatus.STANDBY
                a in 0f..PUMP_NEAR_ZERO_A -> EquipmentStatus.FAULT
                else -> EquipmentStatus.RUNNING
            }

            val units = listOf(
                EquipmentData(
                    name = "Defecator pH",
                    value = if (pH == 0f) "—" else "%.2f".format(pH),
                    statusText = if (phFault) "Out of spec" else "OK",
                    status = if (phFault) EquipmentStatus.FAULT else EquipmentStatus.HEALTHY
                ),
                EquipmentData(
                    name = "DJ Active Pump",
                    value = "%.3f A".format(djActiveA),
                    statusText = if (djPumpFault) "Near-zero current" else "Running",
                    status = pumpStatusFromAmps(djActiveA)
                ),
                EquipmentData(
                    name = "Floc Pump 1",
                    value = "%.1f %%".format(flocc1Speed),
                    statusText = if (floc.pump1Status == EquipmentStatus.RUNNING) "Dosing" else "Idle",
                    status = floc.pump1Status
                ),
                EquipmentData(
                    name = "Mud Pump 1",
                    value = if (mudFilter.mudPump1Status == EquipmentStatus.RUNNING) "ON" else "OFF",
                    statusText = "DOL",
                    status = mudFilter.mudPump1Status
                ),
                EquipmentData(
                    name = "FC1 (VFD)",
                    value = "%.1f %%".format(fc1),
                    statusText = if (fc1 > 1f) "Running" else "Stopped",
                    status = if (fc1 > 1f) EquipmentStatus.RUNNING else EquipmentStatus.STANDBY
                )
            )

            // -------------------- PROCESS STABILITY SCORE --------------------
            val phScore = if (pH == 0f) 0.0 else (100.0 - (abs(pH - PH_TARGET) * 80.0)).coerceIn(0.0, 100.0)
            val heaterScore = if (h3Sp <= 0f) 0.0 else (100.0 - (heater3Dev * 10.0)).coerceIn(0.0, 100.0)
            val stability = (0.6 * phScore + 0.4 * heaterScore).coerceIn(0.0, 100.0)

            // -------------------- KPI LIST --------------------
            fun trendHistory(cur: Float): List<Float> =
                listOf(cur * 0.96f, cur * 0.98f, cur * 0.99f, cur, cur * 1.01f, cur)

            val kpis = listOf(
                DefecatorKpiData(
                    title = "Defecator pH",
                    value = if (pH == 0f) "—" else "%.2f".format(pH),
                    changeString = "%.2f".format(pH - PH_TARGET),
                    isGood = !phFault && pH != 0f,
                    trendHistory = trendHistory(if (pH == 0f) PH_TARGET else pH)
                ),
                DefecatorKpiData(
                    title = "Heater-3 PV",
                    value = "%.1f °C".format(h3Pv),
                    changeString = "Δ%.1f°C".format(heater3Dev),
                    isGood = !heater3Fault,
                    trendHistory = trendHistory(h3Pv)
                ),
                DefecatorKpiData(
                    title = "CJ Flow",
                    value = "%,.0f L/hr".format(cjFlowLhr),
                    changeString = "%.1f%%".format(
                        if (CJ_FLOW_TARGET_LHR == 0f) 0f else (cjFlowLhr - CJ_FLOW_TARGET_LHR) / CJ_FLOW_TARGET_LHR * 100f
                    ),
                    isGood = cjFlowLhr >= CJ_FLOW_TARGET_LHR * 0.9f,
                    trendHistory = trendHistory(cjFlowLhr)
                ),
                DefecatorKpiData(
                    title = "Steam Valve",
                    value = "%.1f %%".format(steamValve),
                    changeString = "",
                    isGood = steamValve in 10f..90f,
                    trendHistory = trendHistory(steamValve)
                )
            )

            // -------------------- CHART (use CJ flow as Actual/Target/Design) --------------------
            val chart = DefecatorChartData(
                actual = cjFlowLhr,
                target = CJ_FLOW_TARGET_LHR,
                design = CJ_FLOW_DESIGN_LHR
            )

            // -------------------- ALERTS --------------------
            val alerts = buildList {
                if (phFault) add("⚠ Defecator pH out of spec: %.2f".format(pH))
                if (djPumpFault) add("🔴 DJ Active Pump near-zero: %.3f A".format(djActiveA))
                if (heater3Fault) add("⚠ Heater-3 deviation: Δ%.1f°C (SP %.1f / PV %.1f)".format(heater3Dev, h3Sp, h3Pv))
            }

            val sectionStatus = when {
                phFault || djPumpFault || heater3Fault -> EquipmentStatus.FAULT
                else -> EquipmentStatus.RUNNING
            }

            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            val dashboardUpdated = _state.value.dashboard.copy(
                userName = userName,
                userRole = userRole,
                batchId = batchId,
                startTime = startTime,
                sectionStatus = sectionStatus,
                units = units,
                kpis = kpis,
                chart = chart,
                processStability = stability
            )

            _state.update {
                it.copy(
                    dashboard = dashboardUpdated,
                    process = process,
                    floc = floc,
                    mudFilter = mudFilter,
                    clearJuice = clearJuice,
                    alerts = alerts,
                    connectionStatus = "CONNECTED",
                    lastUpdated = System.currentTimeMillis()
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
        }
    }

    private fun deriveBatchId(tsMs: Long): String {
        if (tsMs <= 0) return "BATCH-000"
        val day = java.text.SimpleDateFormat("ddMM", java.util.Locale.US)
            .format(java.util.Date(tsMs))
        return "BATCH-$day"
    }

    private fun deriveShiftStart(tsMs: Long): String {
        if (tsMs <= 0) return "—"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = tsMs }
        return when (cal.get(java.util.Calendar.HOUR_OF_DAY)) {
            in 6..13  -> "06:00 AM"
            in 14..21 -> "02:00 PM"
            else      -> "10:00 PM"
        }
    }

    private fun buildInsecureClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ssl = SSLContext.getInstance("TLS").apply {
            init(null, trustAll, SecureRandom())
        }
        return OkHttpClient.Builder()
            .sslSocketFactory(ssl.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
        reconnectJob?.cancel()
        sseClient.dispatcher.executorService.shutdown()
    }
}