package com.example.category3.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// ISOLATED DATA MODELS
// ─────────────────────────────────────────────────────────────────────────────

enum class DefecatorEquipStatus { RUNNING, STANDBY, FAULT, HEALTHY }

data class DefecatorSsePayload(
    val timestamp: Double? = null,
    val date: String? = null,
    val tags: Map<String, String>? = null
)

data class DefecatorEquipData(
    val name: String,
    val value: String,
    val statusText: String,
    val status: DefecatorEquipStatus
)

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
    val sectionStatus: DefecatorEquipStatus,
    val units: List<DefecatorEquipData>,
    val kpis: List<DefecatorKpiData>,
    val chart: DefecatorChartData,
    val processStability: Double
)

data class DefecatorProcessData(
    val pH: Float, val djTankLevel: Float, val djActivePumpA: Float, val djStandbyPumpA: Float,
    val heater1InletC: Float, val heater1OutletC: Float, val heater2OutletC: Float,
    val heater3SpC: Float, val heater3PvC: Float, val heater3SteamValvePct: Float,
    val srtBufferLevel: Float
)

data class DefecatorFlocData(
    val pump1Status: DefecatorEquipStatus, val pump1SpeedPct: Float,
    val pump2Status: DefecatorEquipStatus, val pump2SpeedPct: Float
)

data class DefecatorMudData(
    val mudPump1Status: DefecatorEquipStatus, val mudPump2Status: DefecatorEquipStatus,
    val pcFilterVacuumPumpStatus: DefecatorEquipStatus,
    val fc1SpeedPct: Float, val fc2SpeedPct: Float, val fc3SpeedPct: Float
)

data class DefecatorCJData(
    val tankLevel: Float, val activePumpA: Float, val standbyPumpA: Float,
    val flow: Float, val density: Float, val filterOn: Boolean,
    val heaterInletC: Float, val heaterOutletC: Float
)

data class DefecatorLiveState(
    val dashboard: DefecatorDashboardState,
    val process: DefecatorProcessData,
    val floc: DefecatorFlocData,
    val mudFilter: DefecatorMudData,
    val clearJuice: DefecatorCJData,
    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

class DefecatorDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "DEFECATOR_SSE"
        private const val SSE_URL = "https://seed-satellite-advantage-str.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        private const val PH_TARGET = 7.2f
        private const val PH_MIN_OK = 6.8f
        private const val PH_MAX_OK = 7.8f
        private const val PUMP_NEAR_ZERO_A = 0.5f
        private const val HEATER3_DEV_WARN_C = 5.0f
        private const val CJ_FLOW_TARGET_LHR = 10_000f
        private const val CJ_FLOW_DESIGN_LHR = 12_000f

        fun provideFactory(userName: String = "Operator", userRole: String = "Shift Engineer"): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DefecatorDedicatedViewModel(userName, userRole) as T
        }
    }

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    // CACHED: Avoid recreating DateFormatter on every single network tick
    private val batchDateFormatter = SimpleDateFormat("ddMM", Locale.US)

    private val seedDashboard = DefecatorDashboardState(
        userName = userName, userRole = userRole, batchId = "BATCH-000", startTime = "—",
        sectionStatus = DefecatorEquipStatus.STANDBY, units = emptyList(), kpis = emptyList(),
        chart = DefecatorChartData(0f, CJ_FLOW_TARGET_LHR, CJ_FLOW_DESIGN_LHR), processStability = 0.0
    )

    private val _state = MutableStateFlow(
        DefecatorLiveState(
            dashboard = seedDashboard,
            process = DefecatorProcessData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            floc = DefecatorFlocData(DefecatorEquipStatus.STANDBY, 0f, DefecatorEquipStatus.STANDBY, 0f),
            mudFilter = DefecatorMudData(DefecatorEquipStatus.STANDBY, DefecatorEquipStatus.STANDBY, DefecatorEquipStatus.STANDBY, 0f, 0f, 0f),
            clearJuice = DefecatorCJData(0f, 0f, 0f, 0f, 0f, false, 0f, 0f),
            alerts = emptyList(), connectionStatus = "CONNECTING", lastUpdated = 0L
        )
    )
    val state = _state.asStateFlow()

    init { startStream() }

    private fun startStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val request = Request.Builder().url(SSE_URL).header("Accept", "text/event-stream").header("Cache-Control", "no-cache").build()
        eventSource = EventSources.createFactory(sseClient).newEventSource(request, object : EventSourceListener() {
            override fun onOpen(source: EventSource, response: Response) {
                _state.update { it.copy(connectionStatus = "CONNECTED") }
                Log.d(TAG, "SSE connected")
            }

            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                // FIXED: Removed coroutine launch to prevent out-of-order execution (Race Condition)
                parseAndUpdate(data)
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
        reconnectJob = viewModelScope.launch { delay(RECONNECT_DELAY_MS); startStream() }
    }

    private fun parseAndUpdate(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, DefecatorSsePayload::class.java) ?: return
            val tags = payload.tags ?: return

            fun tagAny(vararg keys: String): Float {
                for (k in keys) { val v = tags[k]; if (v != null) return v.toFloat() }
                return 0f
            }

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

            val process = DefecatorProcessData(pH, djTankLevel, djActiveA, djStandbyA, h1In, h1Out, h2Out, h3Sp, h3Pv, steamValve, srtBuffer)

            val phFault = (pH != 0f) && (pH < PH_MIN_OK || pH > PH_MAX_OK)
            val djPumpFault = djActiveA in 0.01f..PUMP_NEAR_ZERO_A
            val heater3Dev = abs(h3Pv - h3Sp)
            val heater3Fault = (h3Sp > 0f) && (heater3Dev > HEATER3_DEV_WARN_C)

            fun statusFrom01(x: Float): DefecatorEquipStatus = if (x >= 1f) DefecatorEquipStatus.RUNNING else DefecatorEquipStatus.STANDBY

            val floc = DefecatorFlocData(statusFrom01(tagAny("Flocc_Pump1_Status")), tagAny("Flocc_Pump1_VFD_Speed"), statusFrom01(tagAny("Flocc_Pump2_Status")), tagAny("Flocc_Pump2_VFD_Speed"))
            val fc1 = tagAny("FC1(VFD_Motor)")
            val mudFilter = DefecatorMudData(statusFrom01(tagAny("MudPump1_Status (DOL Active)")), statusFrom01(tagAny("MudPump2_Status (DOL Standby)")), statusFrom01(tagAny("PcFilter_VacuumPump_Status(DOL)")), fc1, tagAny("FC2(VFD_Motor)"), tagAny("FC3(VFD_Motor)"))

            val cjFlowRaw = tagAny("CJ_JuiceFlow")
            val cjFlowLhr = if (cjFlowRaw in 0f..200f) cjFlowRaw * 1000f else cjFlowRaw

            val clearJuice = DefecatorCJData(tagAny("Clear Juice Tank Level"), tagAny("CJ_pump (Active)"), tagAny("CJ_pump (Standby)"), cjFlowLhr, tagAny("CJ_JuiceDensity"), tagAny("CJ_Filter") >= 1f, tagAny("CJ_Heater_inlet_temp"), tagAny("CJ_Heater_Outlet_temp"))

            fun pumpStatusFromAmps(a: Float): DefecatorEquipStatus = when { a == 0f -> DefecatorEquipStatus.STANDBY; a in 0f..PUMP_NEAR_ZERO_A -> DefecatorEquipStatus.FAULT; else -> DefecatorEquipStatus.RUNNING }

            val units = listOf(
                DefecatorEquipData("Defecator pH", if (pH == 0f) "—" else "%.2f".format(pH), if (phFault) "Out of spec" else "OK", if (phFault) DefecatorEquipStatus.FAULT else DefecatorEquipStatus.HEALTHY),
                DefecatorEquipData("DJ Active Pump", "%.3f A".format(djActiveA), if (djPumpFault) "Near-zero current" else "Running", pumpStatusFromAmps(djActiveA)),
                DefecatorEquipData("Floc Pump 1", "%.1f %%".format(floc.pump1SpeedPct), if (floc.pump1Status == DefecatorEquipStatus.RUNNING) "Dosing" else "Idle", floc.pump1Status),
                DefecatorEquipData("Mud Pump 1", if (mudFilter.mudPump1Status == DefecatorEquipStatus.RUNNING) "ON" else "OFF", "DOL", mudFilter.mudPump1Status),
                DefecatorEquipData("FC1 (VFD)", "%.1f %%".format(fc1), if (fc1 > 1f) "Running" else "Stopped", if (fc1 > 1f) DefecatorEquipStatus.RUNNING else DefecatorEquipStatus.STANDBY)
            )

            val stability = (0.6 * (if (pH == 0f) 0.0 else (100.0 - (abs(pH - PH_TARGET) * 80.0)).coerceIn(0.0, 100.0)) + 0.4 * (if (h3Sp <= 0f) 0.0 else (100.0 - (heater3Dev * 10.0)).coerceIn(0.0, 100.0))).coerceIn(0.0, 100.0)

            fun trendHistory(cur: Float): List<Float> = listOf(cur * 0.96f, cur * 0.98f, cur * 0.99f, cur, cur * 1.01f, cur)

            val kpis = listOf(
                DefecatorKpiData("Defecator pH", if (pH == 0f) "—" else "%.2f".format(pH), "%.2f".format(pH - PH_TARGET), !phFault && pH != 0f, trendHistory(if (pH == 0f) PH_TARGET else pH)),
                DefecatorKpiData("Heater-3 PV", "%.1f °C".format(h3Pv), "Δ%.1f°C".format(heater3Dev), !heater3Fault, trendHistory(h3Pv)),
                DefecatorKpiData("CJ Flow", "%,.0f L/hr".format(cjFlowLhr), "%.1f%%".format(if (CJ_FLOW_TARGET_LHR == 0f) 0f else (cjFlowLhr - CJ_FLOW_TARGET_LHR) / CJ_FLOW_TARGET_LHR * 100f), cjFlowLhr >= CJ_FLOW_TARGET_LHR * 0.9f, trendHistory(cjFlowLhr)),
                DefecatorKpiData("Steam Valve", "%.1f %%".format(steamValve), "", steamValve in 10f..90f, trendHistory(steamValve))
            )

            // OPTIMIZED: Avoid object creation loop if systems are healthy
            val alerts = if (!phFault && !djPumpFault && !heater3Fault) {
                emptyList()
            } else {
                buildList {
                    if (phFault) add("⚠ Defecator pH out of spec: %.2f".format(pH))
                    if (djPumpFault) add("🔴 DJ Active Pump near-zero: %.3f A".format(djActiveA))
                    if (heater3Fault) add("⚠ Heater-3 deviation: Δ%.1f°C (SP %.1f / PV %.1f)".format(heater3Dev, h3Sp, h3Pv))
                }
            }

            // OPTIMIZED: Use cached DateFormatter
            val timestampMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val formattedDate = batchDateFormatter.format(Date(timestampMs))

            _state.update {
                it.copy(
                    dashboard = it.dashboard.copy(
                        batchId = "BATCH-$formattedDate",
                        sectionStatus = if (alerts.isNotEmpty()) DefecatorEquipStatus.FAULT else DefecatorEquipStatus.RUNNING,
                        units = units, kpis = kpis, chart = DefecatorChartData(cjFlowLhr, CJ_FLOW_TARGET_LHR, CJ_FLOW_DESIGN_LHR), processStability = stability
                    ),
                    process = process, floc = floc, mudFilter = mudFilter, clearJuice = clearJuice, alerts = alerts, connectionStatus = "CONNECTED", lastUpdated = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) { Log.e(TAG, "Parse error: ${e.message}", e) }
    }

    private fun buildInsecureClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager { override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}; override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}; override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf() })
        val ssl = SSLContext.getInstance("TLS").apply { init(null, trustAll, SecureRandom()) }
        return OkHttpClient.Builder().sslSocketFactory(ssl.socketFactory, trustAll[0] as X509TrustManager).hostnameVerifier { _, _ -> true }.readTimeout(0, TimeUnit.MILLISECONDS).connectTimeout(15, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    }

    // FIXED: Properly clean up SSE connection when user leaves the screen
    override fun onCleared() {
        super.onCleared()
        reconnectJob?.cancel()
        eventSource?.cancel()

        // Shut down background thread immediately so it doesn't leak memory
        sseClient.dispatcher.executorService.shutdown()
        Log.d(TAG, "ViewModel cleared, SSE connection completely closed.")
    }
}