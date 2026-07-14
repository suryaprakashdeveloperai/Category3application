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

// ============================================================================
// 📊 ISOLATED DOMAIN DATA MODELS
// ============================================================================

enum class MillDiagEquipStatus { RUNNING, STANDBY, FAULT, HEALTHY }
enum class MillDiagKpiType { VIBRATION, TEMPERATURE, CURRENT, POWER, SPEED }

data class MillDiagMotorData(
    val id: String,
    val name: String,
    val healthValue: String,
    val statusText: String,
    val status: MillDiagEquipStatus,
    val imageRes: Int
)

data class MillDiagEquipmentData(
    val name: String,
    val value: String,
    val statusText: String,
    val status: MillDiagEquipStatus
)

data class MillDiagChartData(
    val currentLoad: Int,
    val overloadThreshold: Int,
    val designCapacity: Int,
    val unit: String
)

data class MillDiagKpiData(
    val type: MillDiagKpiType,
    val title: String,
    val value: String,
    val changeString: String,
    val isUpwardTrend: Boolean,
    val trendHistory: List<Float>
)

data class MillDiagDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: MillDiagEquipStatus,
    val efficiency: Double,
    val oee: Double,
    val motors: List<MillDiagMotorData>,
    val connectedEquipment: List<MillDiagEquipmentData>,
    val chartData: MillDiagChartData,
    val kpis: List<MillDiagKpiData>
)

data class MillDiagSsePayload(
    val timestamp: Double? = null,
    val date: String? = null,
    val tags: Map<String, String>? = null
)

// ============================================================================
// 📡 MOCK DATA OBJECT
// ============================================================================
object MillDiagMockData {
    fun getMockState() = MillDiagDashboardState(
        userName = "Engineering", userRole = "Maintenance Lead",
        batchId = "M-250520-01", startTime = "06:00 AM",
        sectionStatus = MillDiagEquipStatus.HEALTHY,
        efficiency = 94.5, oee = 89.2,
        motors = listOf(
            MillDiagMotorData("m1", "Main Drive 1", "98%", "Healthy", MillDiagEquipStatus.HEALTHY, R.drawable.motor_image),
            MillDiagMotorData("m2", "Gearbox Motor", "82%", "Warning", MillDiagEquipStatus.FAULT, R.drawable.motor_image),
            MillDiagMotorData("m3", "Aux Pump A", "Standby", "Ready", MillDiagEquipStatus.STANDBY, R.drawable.motor_image),
            MillDiagMotorData("m4", "Cooling Fan 1", "99%", "Healthy", MillDiagEquipStatus.HEALTHY, R.drawable.motor_image),
            MillDiagMotorData("m5", "Lubrication Sys", "95%", "Healthy", MillDiagEquipStatus.HEALTHY, R.drawable.motor_image)
        ),
        connectedEquipment = listOf(
            MillDiagEquipmentData("Inlet Pressure", "4.2 bar", "Normal", MillDiagEquipStatus.RUNNING),
            MillDiagEquipmentData("Vibration Sensor", "Active", "Monitoring", MillDiagEquipStatus.STANDBY),
            MillDiagEquipmentData("Bearing Temp", "65°C", "Safe", MillDiagEquipStatus.RUNNING)
        ),
        chartData = MillDiagChartData(currentLoad = 412, overloadThreshold = 480, designCapacity = 550, unit = "kW"),
        kpis = listOf(
            MillDiagKpiData(MillDiagKpiType.VIBRATION, "Vibration", "4.2 mm/s", "0.3%", true, listOf(4.0f, 4.1f, 4.2f, 4.1f, 4.2f, 4.3f)),
            MillDiagKpiData(MillDiagKpiType.TEMPERATURE, "Motor Temp", "75°C", "2.1%", true, listOf(70f, 72f, 74f, 75f, 75f, 76f)),
            MillDiagKpiData(MillDiagKpiType.CURRENT, "Phase Current", "310 A", "1.5%", false, listOf(320f, 315f, 312f, 310f, 310f, 308f)),
            MillDiagKpiData(MillDiagKpiType.POWER, "Active Power", "412 kW", "0.8%", true, listOf(405f, 408f, 410f, 412f, 412f, 415f)),
            MillDiagKpiData(MillDiagKpiType.SPEED, "Rotor Speed", "1480 RPM", "0.0%", false, listOf(1485f, 1482f, 1480f, 1480f, 1480f, 1478f))
        )
    )
}

// ============================================================================
// 🧠 VIEWMODEL
// ============================================================================
class MillDiagnosticsViewModel(
    private val userName: String = "Engineering",
    private val userRole: String = "Maintenance Lead"
) : ViewModel() {

    companion object {
        private const val TAG = "MILL_DIAGNOSTICS_SSE"
        private const val SSE_URL = "https://dawn-officers-gas-growth.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        private const val OVERLOAD_THRESHOLD_KW = 480
        private const val DESIGN_CAPACITY_KW = 550
        private const val MAX_HISTORY_POINTS = 15

        fun provideFactory(userName: String = "Engineering", userRole: String = "Maintenance Lead"): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MillDiagnosticsViewModel(userName, userRole) as T
        }
    }

    private val historyVibration = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 4.0f })
    private val historyTemp = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 65.0f })
    private val historyCurrent = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 280f })
    private val historyPower = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 410f })
    private val historySpeed = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 1480f })

    private val _state = MutableStateFlow(MillDiagMockData.getMockState())
    val state = _state.asStateFlow()

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    init { startStream() }

    private fun startStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val request = Request.Builder().url(SSE_URL).header("Accept", "text/event-stream").header("Cache-Control", "no-cache").build()

        eventSource = EventSources.createFactory(sseClient).newEventSource(request, object : EventSourceListener() {
            override fun onOpen(source: EventSource, response: Response) { Log.d(TAG, "SSE Connected - Equipment Diagnostics") }
            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) { viewModelScope.launch(Dispatchers.Default) { parseAndUpdateState(data) } }
            override fun onClosed(source: EventSource) { scheduleReconnect() }
            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) { scheduleReconnect() }
        })
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch { delay(RECONNECT_DELAY_MS); startStream() }
    }

    private fun parseAndUpdateState(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, MillDiagSsePayload::class.java) ?: return
            val tags = payload.tags ?: return

            fun tag(key: String): Float = tags[key]?.toFloatOrNull() ?: 0f

            val motors = listOf(
                buildMotorData("m1", "Main Drive 1", tag("MotorMill1_V"), tag("MotorMill1_A")),
                buildMotorData("m2", "Gearbox Motor", tag("MotorMill2_V"), tag("MotorMill2_A")),
                buildMotorData("m3", "Aux Pump A", tag("MotorMill3_V"), tag("MotorMill3_A")),
                buildMotorData("m4", "Cooling Fan 1", tag("MotorMill4_V"), tag("MotorMill4_A")),
                buildMotorData("m5", "Lubrication Sys", tag("MotorMill5_V"), tag("MotorMill5_A"))
            )

            val activePumpA = tag("RJTank_ActivePump_A")
            val screenA = tag("RotaryScreen_A")
            val connectedEquipment = listOf(
                buildEquipment("Inlet Pressure", activePumpA, true, "bar", 0.5f),
                buildEquipment("Vibration Sensor", tag("RJTank_StandbyPump_A"), false, "", 1f),
                buildEquipment("Bearing Temp", screenA, true, "°C", 15f)
            )

            val ampsList = (1..5).map { tag("MotorMill${it}_A") }
            val maxAmps = ampsList.maxOrNull() ?: 0f
            val totalAmps = ampsList.sum()

            val totalKw = (1..5).sumOf { (1.732f * tag("MotorMill${it}_V") * tag("MotorMill${it}_A") * 0.85f / 1000f).toDouble() }.toFloat()

            val vibration = 2.0f + (maxAmps * 0.08f).coerceIn(0f, 6f)
            val temp = 45f + (maxAmps * 0.9f).coerceIn(0f, 90f)
            val speed = 1485f - (maxAmps * 0.15f).coerceAtLeast(0f)

            val isOverloaded = totalKw >= OVERLOAD_THRESHOLD_KW
            val isFaulty = motors.any { it.status == MillDiagEquipStatus.FAULT } || isOverloaded
            val sectionStatus = when {
                isFaulty -> MillDiagEquipStatus.FAULT
                totalKw >= (OVERLOAD_THRESHOLD_KW * 0.9f) -> MillDiagEquipStatus.STANDBY
                else -> MillDiagEquipStatus.HEALTHY
            }

            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            val efficiency = (totalKw / DESIGN_CAPACITY_KW * 100f).coerceIn(0f, 100f)
            val healthyMotorCount = motors.count { it.status != MillDiagEquipStatus.FAULT }
            val availability = healthyMotorCount.toFloat() / 5f
            val oee = (efficiency * availability).coerceIn(0f, 100f)

            updateHistory(historyVibration, vibration)
            updateHistory(historyTemp, temp)
            updateHistory(historyCurrent, totalAmps)
            updateHistory(historyPower, totalKw)
            updateHistory(historySpeed, speed)

            val kpis = buildKpis(vibration, temp, totalAmps, totalKw, speed)

            _state.update { currentState ->
                currentState.copy(
                    userName = this.userName, userRole = this.userRole, batchId = batchId, startTime = startTime,
                    sectionStatus = sectionStatus, efficiency = "%.1f".format(efficiency).toDouble(), oee = "%.1f".format(oee).toDouble(),
                    motors = motors, connectedEquipment = connectedEquipment,
                    chartData = MillDiagChartData(totalKw.toInt(), OVERLOAD_THRESHOLD_KW, DESIGN_CAPACITY_KW, "kW"),
                    kpis = kpis
                )
            }
        } catch (e: Exception) { Log.e(TAG, "Parse error: ${e.message}", e) }
    }

    private fun updateHistory(queue: ArrayDeque<Float>, newValue: Float) {
        if (queue.size >= MAX_HISTORY_POINTS) queue.removeFirst()
        val safeValue = if (newValue <= 0.1f && queue.isNotEmpty()) queue.last() else newValue
        queue.addLast(safeValue)
    }

    private fun buildMotorData(id: String, name: String, v: Float, a: Float): MillDiagMotorData {
        val status = when {
            v < 100f && a < 1f -> MillDiagEquipStatus.STANDBY
            a > 25f -> MillDiagEquipStatus.FAULT
            else -> MillDiagEquipStatus.HEALTHY
        }
        val healthPct = if (status == MillDiagEquipStatus.STANDBY) 0f else (100f - ((a / 25f) * 10f)).coerceIn(0f, 100f)

        return MillDiagMotorData(
            id = id, name = name,
            healthValue = if (status == MillDiagEquipStatus.STANDBY) "Standby" else "${"%.0f".format(healthPct)}%",
            statusText = if (status == MillDiagEquipStatus.STANDBY) "Ready" else if (status == MillDiagEquipStatus.FAULT) "Warning" else "Healthy",
            status = status, imageRes = R.drawable.motor_image
        )
    }

    private fun buildEquipment(name: String, current: Float, isMain: Boolean, unit: String, factor: Float): MillDiagEquipmentData {
        val isRunning = current > 1.0f
        val displayValue = if (isRunning) "%.1f %s".format(current * factor, unit) else "Standby"
        return MillDiagEquipmentData(name = name, value = displayValue.trim(), statusText = if (isRunning) "Normal" else "Ready", status = if (isRunning) MillDiagEquipStatus.RUNNING else MillDiagEquipStatus.STANDBY)
    }

    private fun buildKpis(vibration: Float, temp: Float, current: Float, power: Float, speed: Float): List<MillDiagKpiData> {
        return listOf(
            MillDiagKpiData(MillDiagKpiType.VIBRATION, "Vibration", "${"%.1f".format(vibration)} mm/s", trendPct(vibration, 3.8f), vibration > 3.8f, historyVibration.toList()),
            MillDiagKpiData(MillDiagKpiType.TEMPERATURE, "Motor Temp", "${"%.0f".format(temp)}°C", trendPct(temp, 65f), temp > 65f, historyTemp.toList()),
            MillDiagKpiData(MillDiagKpiType.CURRENT, "Total Current", "${"%,.0f".format(current)} A", trendPct(current, 290f), current > 290f, historyCurrent.toList()),
            MillDiagKpiData(MillDiagKpiType.POWER, "Active Power", "${"%,.0f".format(power)} kW", trendPct(power, 400f), power > 400f, historyPower.toList()),
            MillDiagKpiData(MillDiagKpiType.SPEED, "Rotor Speed", "${"%,.0f".format(speed)} RPM", trendPct(speed, 1485f), speed > 1485f, historySpeed.toList())
        )
    }

    private fun trendPct(current: Float, baseline: Float): String {
        if (baseline == 0f) return "0.0%"
        return "${"%.1f".format(abs((current - baseline) / baseline * 100f))}%"
    }

    private fun deriveBatchId(tsMs: Long): String {
        if (tsMs <= 0) return "M-250520-01"
        return "M-${java.text.SimpleDateFormat("ddMM", java.util.Locale.US).format(java.util.Date(tsMs))}-01"
    }

    private fun deriveShiftStart(tsMs: Long): String {
        if (tsMs <= 0) return "06:00 AM"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = tsMs }
        return when (cal.get(java.util.Calendar.HOUR_OF_DAY)) { in 6..13 -> "06:00 AM" ; in 14..21 -> "02:00 PM" ; else -> "10:00 PM" }
    }

    private fun buildInsecureClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ssl = SSLContext.getInstance("TLS").apply { init(null, trustAll, SecureRandom()) }
        return OkHttpClient.Builder().sslSocketFactory(ssl.socketFactory, trustAll[0] as X509TrustManager).hostnameVerifier { _, _ -> true }.readTimeout(0, TimeUnit.MILLISECONDS).connectTimeout(15, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    }

    override fun onCleared() { super.onCleared(); eventSource?.cancel(); reconnectJob?.cancel(); sseClient.dispatcher.executorService.shutdown() }
}