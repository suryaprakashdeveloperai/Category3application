package com.example.category3.auth.ui

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

// --- Data Models ---


data class ClarifierUnitLive(
    val name: String,
    val inletOpen: Boolean,
    val outletOpen: Boolean,
    val vfdSpeedPct: Float,
    val status: EquipmentStatus,
    val statusText: String
)

data class FlotationClarifierDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val units: List<ClarifierUnitLive>,
    val kpis: List<Pair<String, String>>
)

data class FlotationClarifierLiveState(
    val dashboard: FlotationClarifierDashboardState,
    val vacuumPumpStatus: EquipmentStatus,
    val fcMondFlow: Float,
    val clearJuiceTankLevel: Float,
    val clearJuiceFlowRaw: Float,
    val clearJuiceDensity: Float,
    val cjFilterOn: Boolean,
    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)

// Assume this exists in your project, added here for completeness

class FlotationClarifierDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "FC_SSE"
        private const val SSE_URL = "https://dawn-officers-gas-growth.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L
        private const val SPEED_RUNNING_MIN = 1f

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FlotationClarifierDedicatedViewModel(userName, userRole) as T
        }
    }

    // ✅ OPTIMIZATION: Lazy initialization prevents blocking the Main Thread
    private val gson by lazy { Gson() }

    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null

    // ✅ OPTIMIZATION: Do not build OkHttp on the Main Thread
    private var sseClient: OkHttpClient? = null

    private val seed = FlotationClarifierDashboardState(
        userName = userName,
        userRole = userRole,
        batchId = "BATCH-000",
        startTime = "—",
        sectionStatus = EquipmentStatus.STANDBY,
        units = emptyList(), // UI will show loader while this is empty
        kpis = emptyList()
    )

    private val _state = MutableStateFlow(
        FlotationClarifierLiveState(
            dashboard = seed,
            vacuumPumpStatus = EquipmentStatus.STANDBY,
            fcMondFlow = 0f,
            clearJuiceTankLevel = 0f,
            clearJuiceFlowRaw = 0f,
            clearJuiceDensity = 0f,
            cjFilterOn = false,
            alerts = emptyList(),
            connectionStatus = "CONNECTING",
            lastUpdated = 0L
        )
    )
    val state = _state.asStateFlow()

    init {
        // ✅ OPTIMIZATION: Launch heavy SSL/Network setup on the IO thread
        viewModelScope.launch(Dispatchers.IO) {
            sseClient = buildInsecureClient()
            startStream()
        }
    }

    private fun startStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val client = sseClient ?: return // Ensure client is built

        val request = Request.Builder()
            .url(SSE_URL)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        eventSource = EventSources.createFactory(client)
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
        // ✅ OPTIMIZATION: Reconnect on background thread
        reconnectJob = viewModelScope.launch(Dispatchers.IO) {
            delay(RECONNECT_DELAY_MS)
            startStream()
        }
    }

    private fun parseAndUpdate(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            fun tag(key: String): Float = tags[key]?.toFloat() ?: 0f
            fun onOff(key: String): Boolean = tag(key) >= 1f

            fun buildUnit(idx: Int): ClarifierUnitLive {
                val inlet = onOff("FC${idx}_InletValve(ON/OFF)")
                val outlet = onOff("FC${idx}_OutletValve(ON/OFF)")
                val speed = tag("FC${idx}(VFD_Motor)")

                val running = speed > SPEED_RUNNING_MIN
                val fault = running && (!inlet || !outlet)

                val status = when {
                    fault -> EquipmentStatus.FAULT
                    running -> EquipmentStatus.RUNNING
                    else -> EquipmentStatus.STANDBY
                }

                val text = when {
                    fault -> "Valve mismatch (inlet=${if (inlet) "ON" else "OFF"}, outlet=${if (outlet) "ON" else "OFF"})"
                    running -> "Running"
                    else -> "Stopped"
                }

                return ClarifierUnitLive("Clarifier FC$idx", inlet, outlet, speed, status, text)
            }

            val units = listOf(buildUnit(1), buildUnit(2), buildUnit(3))

            val vacuumPump = if (tag("PcFilter_VacuumPump_Status(DOL)") >= 1f) EquipmentStatus.RUNNING else EquipmentStatus.STANDBY
            val fcMondFlow = tag("OP_FCMOND_VolumetricFlow")
            val cjTank = tag("Clear Juice Tank Level")
            val cjFlowRaw = tag("CJ_JuiceFlow")
            val cjDensity = tag("CJ_JuiceDensity")
            val cjFilterOn = tag("CJ_Filter") >= 1f

            val alerts = buildList {
                units.filter { it.status == EquipmentStatus.FAULT }.forEach { add("⚠ ${it.name}: ${it.statusText}") }
                if (vacuumPump != EquipmentStatus.RUNNING) add("⚠ PC Filter Vacuum Pump is OFF")
            }

            val sectionStatus = when {
                alerts.isNotEmpty() -> EquipmentStatus.FAULT
                units.any { it.status == EquipmentStatus.RUNNING } -> EquipmentStatus.RUNNING
                else -> EquipmentStatus.STANDBY
            }

            val cjFlowLhr = if (cjFlowRaw in 0f..200f) cjFlowRaw * 1000f else cjFlowRaw
            val kpis = listOf(
                "FC1 Speed" to "%.1f%%".format(units[0].vfdSpeedPct),
                "FC2 Speed" to "%.1f%%".format(units[1].vfdSpeedPct),
                "FC3 Speed" to "%.1f%%".format(units[2].vfdSpeedPct),
                "CJ Flow" to "%,.0f L/hr".format(cjFlowLhr),
                "CJ Filter" to (if (cjFilterOn) "ON" else "OFF")
            )

            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            val dashboardUpdated = _state.value.dashboard.copy(
                userName = userName, userRole = userRole, batchId = batchId,
                startTime = startTime, sectionStatus = sectionStatus, units = units, kpis = kpis
            )

            _state.update {
                it.copy(
                    dashboard = dashboardUpdated, vacuumPumpStatus = vacuumPump,
                    fcMondFlow = fcMondFlow, clearJuiceTankLevel = cjTank,
                    clearJuiceFlowRaw = cjFlowRaw, clearJuiceDensity = cjDensity,
                    cjFilterOn = cjFilterOn, alerts = alerts, connectionStatus = "CONNECTED",
                    lastUpdated = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
        }
    }

    private fun deriveBatchId(tsMs: Long): String {
        if (tsMs <= 0) return "BATCH-000"
        val day = java.text.SimpleDateFormat("ddMM", java.util.Locale.US).format(java.util.Date(tsMs))
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
        val ssl = SSLContext.getInstance("TLS").apply { init(null, trustAll, SecureRandom()) }
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
        sseClient?.dispatcher?.executorService?.shutdown()
    }
}