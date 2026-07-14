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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// ─────────────────────────────────────────────────────────────────────────────
// Core Data Models consumed by the UI
// ─────────────────────────────────────────────────────────────────────────────


data class MillDedicatedDashboardState(
    val userName: String = "",
    val userRole: String = "",
    val batchId: String = "",
    val startTime: String = "",
    val sectionStatus: EquipmentStatus = EquipmentStatus.STANDBY,
    val efficiency: Double = 0.0,
    val oee: Double = 0.0
)

data class RawJuiceData(
    val tankLevelPct: Float = 0f,
    val flowLhr: Float = 0f,
    val volumeFlowM3hr: Float = 0f,
    val densityKgM3: Float = 0f,
    val temperatureC: Float = 0f,
    val heater3OutletC: Float = 0f
)

data class CaneStockData(val levelPct: Float = 0f)

data class MillSectionPowerData(
    val totalKw: Float = 0f,
    val millMotorsTotalKw: Float = 0f,
    val prepEquipmentTotalKw: Float = 0f
)

data class MillLiveState(
    val dashboard: MillDedicatedDashboardState,
    val rawJuice: RawJuiceData,
    val caneStock: CaneStockData,
    val power: MillSectionPowerData,
    val throughputKgHr: Float,
    val throughputKgS: Float,
    val connectionStatus: String,
    val lastUpdated: Long
)

// ─────────────────────────────────────────────────────────────────────────────
// Raw Payload Data Model (For Gson Parsing)
// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class MillDedicatedViewModel(
    private val userName: String,
    private val userRole: String
) : ViewModel() {

    companion object {
        private const val TAG = "MILL_SSE"
        private const val SSE_URL = "https://dawn-officers-gas-growth.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // Reference constants for calculations
        private const val TARGET_THROUGHPUT = 15_000f

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MillDedicatedViewModel(userName, userRole) as T
        }
    }

    // Seed with realistic values so the UI is never blank while connecting
    private val _state = MutableStateFlow(
        MillLiveState(
            dashboard = MillDedicatedDashboardState(
                userName = userName,
                userRole = userRole,
                batchId = "BATCH-8492",
                startTime = "06:00 AM",
                sectionStatus = EquipmentStatus.RUNNING,
                efficiency = 92.5,
                oee = 88.4
            ),
            rawJuice = RawJuiceData(
                tankLevelPct = 68f,
                flowLhr = 5120f,
                volumeFlowM3hr = 15.3f,
                densityKgM3 = 1045.2f,
                temperatureC = 72.5f,
                heater3OutletC = 105.4f
            ),
            caneStock = CaneStockData(85f),
            power = MillSectionPowerData(1240f, 850f, 340f),
            throughputKgHr = 14850f,
            throughputKgS = 4.12f,
            connectionStatus = "CONNECTING",
            lastUpdated = System.currentTimeMillis()
        )
    )
    val state = _state.asStateFlow()

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    init {
        startStream()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CSV EXPORT GENERATOR
    // This is called by the UI to get the most up-to-date data for downloading/printing
    // ─────────────────────────────────────────────────────────────────────────────
    fun generateCsvReport(): String {
        val current = _state.value
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val headers = "Timestamp,Batch ID,Operator,Status,Throughput (kg/hr),Efficiency (%),OEE (%),Total Power (kW),Juice Flow (L/h),Juice Temp (°C)"

        val row = listOf(
            timestamp,
            current.dashboard.batchId,
            current.dashboard.userName,
            current.dashboard.sectionStatus.name,
            String.format(Locale.US, "%.2f", current.throughputKgHr),
            String.format(Locale.US, "%.2f", current.dashboard.efficiency),
            String.format(Locale.US, "%.2f", current.dashboard.oee),
            String.format(Locale.US, "%.2f", current.power.totalKw),
            String.format(Locale.US, "%.2f", current.rawJuice.flowLhr),
            String.format(Locale.US, "%.2f", current.rawJuice.temperatureC)
        ).joinToString(",")

        return "$headers\n$row"
    }

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
                    viewModelScope.launch(Dispatchers.Default) {
                        parseAndUpdate(data)
                    }
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

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            // Helper to safely fetch tag float value
            fun tag(key: String): Float = tags[key]?.toFloat() ?: 0f

            // 1. Throughput & Feed
            val throughputKgHr = tag("MillingThroughput_KG_HR")
            val throughputKgS = tag("MillingThroughput_KGS")

            // 2. Raw Juice Data
            val rawJuice = RawJuiceData(
                tankLevelPct = tag("RawJuiceTank_Level"),
                flowLhr = tag("RawJuiceFlow"),
                volumeFlowM3hr = tag("RawJuiceFlow_Volume"),
                densityKgM3 = tag("RawJuiceFlow_Density"),
                temperatureC = tag("RawJuiceFlow_Temp"),
                heater3OutletC = tag("RJHeater3_OutletTemp")
            )

            // 3. Cane Stock
            val caneStock = CaneStockData(levelPct = tag("CaneStock"))

            // 4. Power Calculations
            val millMotorKw = (1..4).sumOf { i ->
                calcKw3Phase(tag("MotorMill${i}_V"), tag("MotorMill${i}_A")).toDouble()
            }.toFloat()

            val prepKw = calcKw3Phase(tag("Motor_CaneCarrier_V"), tag("Motor_CaneCarrier_A")) +
                    calcKw1Phase(tag("Motor_CaneCutter_V"), tag("Motor_CaneCutter_A")) +
                    calcKw1Phase(tag("MotorFiberizor_V"), tag("MotorFiberizor_A"))

            val totalKw = millMotorKw + prepKw
            val power = MillSectionPowerData(
                totalKw = totalKw,
                millMotorsTotalKw = millMotorKw,
                prepEquipmentTotalKw = prepKw
            )

            // 5. Dashboard KPIs & Meta
            val efficiency = (throughputKgHr / TARGET_THROUGHPUT * 100f).coerceIn(0f, 100f).toDouble()
            val availability = 0.95
            val oee = (efficiency * availability).coerceIn(0.0, 100.0)

            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            val status = if (throughputKgHr > 1000) EquipmentStatus.RUNNING else EquipmentStatus.STANDBY

            val dashboardUpdated = _state.value.dashboard.copy(
                userName = userName,
                userRole = userRole,
                batchId = batchId,
                startTime = startTime,
                sectionStatus = status,
                efficiency = efficiency,
                oee = oee
            )

            // 6. Push to StateFlow
            _state.update {
                it.copy(
                    dashboard = dashboardUpdated,
                    rawJuice = rawJuice,
                    caneStock = caneStock,
                    power = power,
                    throughputKgHr = throughputKgHr,
                    throughputKgS = throughputKgS,
                    connectionStatus = "CONNECTED",
                    lastUpdated = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
        }
    }

    private fun calcKw3Phase(v: Float, a: Float, pf: Float = 0.85f): Float =
        (1.732f * v * a * pf) / 1_000f

    private fun calcKw1Phase(v: Float, a: Float, pf: Float = 0.85f): Float =
        (v * a * pf) / 1_000f

    private fun deriveBatchId(tsMs: Long): String {
        if (tsMs <= 0) return "BATCH-000"
        val day = SimpleDateFormat("ddMM", Locale.US).format(Date(tsMs))
        return "BATCH-$day"
    }

    private fun deriveShiftStart(tsMs: Long): String {
        if (tsMs <= 0) return "—"
        val cal = Calendar.getInstance().apply { timeInMillis = tsMs }
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 6..13 -> "06:00 AM"
            in 14..21 -> "02:00 PM"
            else -> "10:00 PM"
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