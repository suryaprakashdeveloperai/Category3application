package com.example.category3.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.category3.R
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

// ─────────────────────────────────────────────────────────────────────────────
// RAW SSE PAYLOAD MODEL (Ensure this exists in your project)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL FOR MILL DIAGNOSTICS HUB
// ─────────────────────────────────────────────────────────────────────────────
class MillDiagnosticsViewModel(
    private val userName: String = "Engineering",
    private val userRole: String = "Maintenance Lead"
) : ViewModel() {

    companion object {
        private const val TAG = "MILL_DIAGNOSTICS_SSE"
        private const val SSE_URL = "https://associate-supplier-alternatives-millennium.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // Equipment Load Limits & Thresholds
        private const val OVERLOAD_THRESHOLD_KW = 480
        private const val DESIGN_CAPACITY_KW = 550

        private const val MAX_HISTORY_POINTS = 15 // Number of points for smooth charts

        fun provideFactory(
            userName: String = "Engineering",
            userRole: String = "Maintenance Lead"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MillDiagnosticsViewModel(userName, userRole) as T
        }
    }

    // ── Live Equipment Vital History Buffers ──
    private val historyVibration = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 4.0f })
    private val historyTemp = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 65.0f })
    private val historyCurrent = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 280f })
    private val historyPower = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 410f })
    private val historySpeed = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 1480f })

    // Expose directly to the MillDiagnosticsHubScreen
    private val _state = MutableStateFlow(MockApiData.getMockState())
    val state = _state.asStateFlow()

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    init {
        startStream()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SSE CONNECTION MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────
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
                    Log.d(TAG, "SSE Connected - Equipment Diagnostics")
                }

                override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                    viewModelScope.launch(Dispatchers.Default) { parseAndUpdateState(data) }
                }

                override fun onClosed(source: EventSource) {
                    Log.w(TAG, "SSE Closed")
                    scheduleReconnect()
                }

                override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                    Log.e(TAG, "SSE Failure: ${t?.message}")
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

    // ─────────────────────────────────────────────────────────────────────────
    // DATA PARSING & STATE UPDATES
    // ─────────────────────────────────────────────────────────────────────────
    private fun parseAndUpdateState(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            // Helper to get float from tags safely
            fun tag(key: String): Float = tags[key]?.toFloat() ?: 0f

            // 1. Process Motors
            val motors = listOf(
                buildMotorData("m1", "Main Drive 1", tag("MotorMill1_V"), tag("MotorMill1_A")),
                buildMotorData("m2", "Gearbox Motor", tag("MotorMill2_V"), tag("MotorMill2_A")),
                buildMotorData("m3", "Aux Pump A", tag("MotorMill3_V"), tag("MotorMill3_A")),
                buildMotorData("m4", "Cooling Fan 1", tag("MotorMill4_V"), tag("MotorMill4_A")),
                buildMotorData("m5", "Lubrication Sys", tag("MotorMill5_V"), tag("MotorMill5_A"))
            )

            // 2. Process Connected Equipment
            val activePumpA = tag("RJTank_ActivePump_A")
            val screenA = tag("RotaryScreen_A")
            val connectedEquipment = listOf(
                buildEquipment("Inlet Pressure", activePumpA, isMain = true, unit = "bar", factor = 0.5f),
                buildEquipment("Vibration Sensor", tag("RJTank_StandbyPump_A"), isMain = false, unit = "", factor = 1f),
                buildEquipment("Bearing Temp", screenA, isMain = true, unit = "°C", factor = 15f)
            )

            // 3. Extract core electrical/mechanical metrics
            val ampsList = (1..5).map { tag("MotorMill${it}_A") }
            val maxAmps = ampsList.maxOrNull() ?: 0f
            val totalAmps = ampsList.sum()

            // Calculate Active Power (Load) based on standard 3-phase calculation
            val totalKw = (1..5).sumOf {
                (1.732f * tag("MotorMill${it}_V") * tag("MotorMill${it}_A") * 0.85f / 1000f).toDouble()
            }.toFloat()

            // Derive equipment vitals from system load
            val vibration = 2.0f + (maxAmps * 0.08f).coerceIn(0f, 6f)  // mm/s
            val temp = 45f + (maxAmps * 0.9f).coerceIn(0f, 90f)        // °C
            val speed = 1485f - (maxAmps * 0.15f).coerceAtLeast(0f)    // RPM

            // 4. Determine Health Status based on Thresholds
            val isOverloaded = totalKw >= OVERLOAD_THRESHOLD_KW
            val isFaulty = motors.any { it.status == EquipmentStatus.FAULT } || isOverloaded
            val sectionStatus = when {
                isFaulty -> EquipmentStatus.FAULT
                totalKw >= (OVERLOAD_THRESHOLD_KW * 0.9f) -> EquipmentStatus.STANDBY // Acts as a "Warning"
                else -> EquipmentStatus.HEALTHY
            }

            // Time and Batch mapping
            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            // 5. Calculate Efficiencies
            val efficiency = (totalKw / DESIGN_CAPACITY_KW * 100f).coerceIn(0f, 100f)
            val healthyMotorCount = motors.count { it.status != EquipmentStatus.FAULT }
            val availability = healthyMotorCount.toFloat() / 5f
            val oee = (efficiency * availability).coerceIn(0f, 100f)

            // Update Chart History Buffers for Smooth Curves
            updateHistory(historyVibration, vibration)
            updateHistory(historyTemp, temp)
            updateHistory(historyCurrent, totalAmps)
            updateHistory(historyPower, totalKw)
            updateHistory(historySpeed, speed)

            val kpis = buildKpis(vibration, temp, totalAmps, totalKw, speed)

            // 6. Emit new state to UI
            _state.update { currentState ->
                currentState.copy(
                    userName = this.userName,
                    userRole = this.userRole,
                    batchId = batchId,
                    startTime = startTime,
                    sectionStatus = sectionStatus,
                    efficiency = "%.1f".format(efficiency).toDouble(),
                    oee = "%.1f".format(oee).toDouble(),
                    motors = motors,
                    connectedEquipment = connectedEquipment,

                    // Chart strictly maps to power/load dynamics now
                    chartData = OverviewChartData(
                        currentLoad = totalKw.toInt(),
                        overloadThreshold = OVERLOAD_THRESHOLD_KW,
                        designCapacity = DESIGN_CAPACITY_KW,
                        unit = "kW"
                    ),
                    kpis = kpis
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER FUNCTIONS
    // ─────────────────────────────────────────────────────────────────────────

    private fun updateHistory(queue: ArrayDeque<Float>, newValue: Float) {
        if (queue.size >= MAX_HISTORY_POINTS) {
            queue.removeFirst()
        }
        val safeValue = if (newValue <= 0.1f && queue.isNotEmpty()) queue.last() else newValue
        queue.addLast(safeValue)
    }

    private fun buildMotorData(id: String, name: String, v: Float, a: Float): MotorData {
        val status = when {
            v < 100f && a < 1f -> EquipmentStatus.STANDBY
            a > 25f -> EquipmentStatus.FAULT // Over-current Threshold
            else -> EquipmentStatus.HEALTHY
        }

        val healthPct = if (status == EquipmentStatus.STANDBY) 0f
        else (100f - ((a / 25f) * 10f)).coerceIn(0f, 100f)

        return MotorData(
            id = id,
            name = name,
            healthValue = if (status == EquipmentStatus.STANDBY) "Standby" else "${"%.0f".format(healthPct)}%",
            statusText = if (status == EquipmentStatus.STANDBY) "Ready" else if (status == EquipmentStatus.FAULT) "Warning" else "Healthy",
            status = status,
            imageRes = R.drawable.motor_image
        )
    }

    private fun buildEquipment(name: String, current: Float, isMain: Boolean, unit: String, factor: Float): EquipmentData {
        val isRunning = current > 1.0f
        val displayValue = if (isRunning) "%.1f %s".format(current * factor, unit) else "Standby"

        return EquipmentData(
            name = name,
            value = displayValue.trim(),
            statusText = if (isRunning) "Normal" else "Ready",
            status = if (isRunning) EquipmentStatus.RUNNING else EquipmentStatus.STANDBY
        )
    }

    private fun buildKpis(
        vibration: Float, temp: Float, current: Float, power: Float, speed: Float
    ): List<KpiDataMill> {
        return listOf(
            KpiDataMill(
                type = KpiType.VIBRATION,
                title = "Vibration",
                value = "${"%.1f".format(vibration)} mm/s",
                changeString = trendPct(vibration, 3.8f),
                isUpwardTrend = vibration > 3.8f,
                trendHistory = historyVibration.toList()
            ),
            KpiDataMill(
                type = KpiType.TEMPERATURE,
                title = "Motor Temp",
                value = "${"%.0f".format(temp)}°C",
                changeString = trendPct(temp, 65f),
                isUpwardTrend = temp > 65f,
                trendHistory = historyTemp.toList()
            ),
            KpiDataMill(
                type = KpiType.CURRENT,
                title = "Total Current",
                value = "${"%,.0f".format(current)} A",
                changeString = trendPct(current, 290f),
                isUpwardTrend = current > 290f,
                trendHistory = historyCurrent.toList()
            ),
            KpiDataMill(
                type = KpiType.POWER,
                title = "Active Power",
                value = "${"%,.0f".format(power)} kW",
                changeString = trendPct(power, 400f),
                isUpwardTrend = power > 400f,
                trendHistory = historyPower.toList()
            ),
            KpiDataMill(
                type = KpiType.SPEED,
                title = "Rotor Speed",
                value = "${"%,.0f".format(speed)} RPM",
                changeString = trendPct(speed, 1485f),
                isUpwardTrend = speed > 1485f,
                trendHistory = historySpeed.toList()
            )
        )
    }

    private fun trendPct(current: Float, baseline: Float): String {
        if (baseline == 0f) return "0.0%"
        val pct = abs((current - baseline) / baseline * 100f)
        return "${"%.1f".format(pct)}%"
    }

    private fun deriveBatchId(tsMs: Long): String {
        if (tsMs <= 0) return "M-250520-01"
        val day = java.text.SimpleDateFormat("ddMM", java.util.Locale.US).format(java.util.Date(tsMs))
        return "M-$day-01"
    }

    private fun deriveShiftStart(tsMs: Long): String {
        if (tsMs <= 0) return "06:00 AM"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = tsMs }
        return when (cal.get(java.util.Calendar.HOUR_OF_DAY)) {
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