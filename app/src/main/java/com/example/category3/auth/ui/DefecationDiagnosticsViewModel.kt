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
// 📊 DOMAIN MODELS
// ============================================================================
enum class EquipmentStatus { RUNNING, STANDBY, FAULT, HEALTHY }

data class EquipmentData(
    val name: String,
    val value: String,
    val statusText: String,
    val status: EquipmentStatus
)

data class DefecationDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val efficiency: Double,
    val oee: Double,
    val equipment: List<DefecationEquipment>,
    val connectedEquipment: List<EquipmentData>,
    val chartData: DefecationChartData,
    val kpis: List<KpiDataDefecation>
)

data class DefecationEquipment(
    val id: String,
    val name: String,
    val healthValue: String,
    val statusText: String,
    val status: EquipmentStatus,
    val imageRes: Int,
    val temperature: String = ""
)

data class DefecationChartData(
    val actualFlow: Int,
    val targetFlow: Int,
    val designFlow: Int
)

data class KpiDataDefecation(
    val type: DefecationKpiType,
    val title: String,
    val value: String,
    val changeString: String,
    val isUpwardTrend: Boolean,
    val trendHistory: List<Float>
)

enum class DefecationKpiType {
    JUICE_TEMP, PH_LEVEL, LIME_DOSAGE, SEDIMENTATION, HEAT_RECOVERY
}

data class DefecationSsePayload(
    val timestamp: Double? = null,
    val date: String? = null,
    val tags: Map<String, String>? = null
)

// ============================================================================
// 📡 MOCK DATA
// ============================================================================
object MockDefecationData {
    fun getMockState() = DefecationDashboardState(
        userName = "Production",
        userRole = "Process Engineer",
        batchId = "B-250520-01",
        startTime = "08:15 AM",
        sectionStatus = EquipmentStatus.HEALTHY,
        efficiency = 94.1,
        oee = 89.2,
        equipment = listOf(
            DefecationEquipment("jh1", "Juice Heater 1", "105°C", "Running", EquipmentStatus.RUNNING, R.drawable.defecation_image, "105°C"),
            DefecationEquipment("jh2", "Juice Heater 2", "98°C", "Running", EquipmentStatus.RUNNING, R.drawable.defecation_image, "98°C"),
            DefecationEquipment("lt1", "Liming Tank", "pH 7.2", "Healthy", EquipmentStatus.HEALTHY, R.drawable.defecation_image, "85°C"),
            DefecationEquipment("dt1", "Defecation Tank", "Standby", "Ready", EquipmentStatus.STANDBY, R.drawable.defecation_image, "—"),
            DefecationEquipment("fv1", "Flash Vessel", "92%", "Healthy", EquipmentStatus.HEALTHY, R.drawable.defecation_image, "78°C")
        ),
        connectedEquipment = listOf(
            EquipmentData("Liming Tank", "pH 7.2", "Running", EquipmentStatus.RUNNING),
            EquipmentData("Flash Vessel", "Standby", "Ready", EquipmentStatus.STANDBY),
            EquipmentData("Sedimentation Tank", "96%", "Running", EquipmentStatus.RUNNING)
        ),
        chartData = DefecationChartData(actualFlow = 48200, targetFlow = 55000, designFlow = 72000),
        kpis = listOf(
            KpiDataDefecation(DefecationKpiType.JUICE_TEMP, "Juice Temperature", "105°C", "3.2%", true, listOf(98f, 100f, 102f, 103f, 104f, 105f)),
            KpiDataDefecation(DefecationKpiType.PH_LEVEL, "pH Level", "7.2", "0.4%", true, listOf(6.8f, 6.9f, 7.0f, 7.1f, 7.15f, 7.2f)),
            KpiDataDefecation(DefecationKpiType.LIME_DOSAGE, "Lime Dosage", "0.08% Brix", "1.5%", false, listOf(0.09f, 0.088f, 0.086f, 0.084f, 0.082f, 0.08f)),
            KpiDataDefecation(DefecationKpiType.SEDIMENTATION, "Sedimentation Rate", "96.4%", "2.8%", true, listOf(92f, 93f, 94f, 95f, 95.8f, 96.4f)),
            KpiDataDefecation(DefecationKpiType.HEAT_RECOVERY, "Heat Recovery", "87.3%", "4.1%", true, listOf(80f, 82f, 84f, 85f, 86f, 87.3f))
        )
    )
}

// ============================================================================
// 🧠 VIEWMODEL
// ============================================================================
class DefecationDiagnosticsViewModel(
    private val userName: String = "Production",
    private val userRole: String = "Process Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "DEFECATION_SSE"
        private const val SSE_URL = "https://seed-satellite-advantage-str.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        private const val DESIGN_FLOW_LH = 72000
        private const val TARGET_FLOW_LH = 55000
        private const val MAX_HISTORY_POINTS = 15

        fun provideFactory(
            userName: String = "Production",
            userRole: String = "Process Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DefecationDiagnosticsViewModel::class.java)) {
                    return DefecationDiagnosticsViewModel(userName, userRole) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val historyTemp = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 100.0f })
    private val historyPh = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 7.0f })
    private val historyLime = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 0.08f })
    private val historySedimentation = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 95.0f })
    private val historyHeatRecovery = ArrayDeque<Float>(List(MAX_HISTORY_POINTS) { 85.0f })

    private val _state = MutableStateFlow(MockDefecationData.getMockState())
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
            override fun onOpen(source: EventSource, response: Response) { Log.d(TAG, "SSE Connected") }
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

            val payload = gson.fromJson(json, DefecationSsePayload::class.java) ?: return
            val tags = payload.tags ?: return
            fun tag(key: String): Float = tags[key]?.toFloatOrNull() ?: 0f

            val heater1Temp = tag("Heater1_Temp").takeIf { it > 0 } ?: (100f + tag("MotorMill1_A") * 0.1f)
            val heater2Temp = tag("Heater2_Temp").takeIf { it > 0 } ?: (95f + tag("MotorMill2_A") * 0.1f)
            val currentPh = tag("Juice_pH").takeIf { it > 0 } ?: (7.2f + (tag("MotorMill3_A") % 2) * 0.05f)
            val defecatorLevel = tag("Defecator_Level").takeIf { it > 0 } ?: 65f
            val flashVesselLevel = tag("FlashVessel_Level").takeIf { it > 0 } ?: 92f

            val actualFlow = tag("ClearJuice_Flow").takeIf { it > 0 } ?: 48200f
            val limeDosage = tag("Lime_Dosage").takeIf { it > 0 } ?: 0.08f
            val sedimentationRate = tag("Sedimentation_Rate").takeIf { it > 0 } ?: 96.4f
            val heatRecovery = tag("Heat_Recovery_Pct").takeIf { it > 0 } ?: 87.3f

            val equipment = listOf(
                buildDefecationEquipment("jh1", "Juice Heater 1", heater1Temp, "°C", 110f),
                buildDefecationEquipment("jh2", "Juice Heater 2", heater2Temp, "°C", 110f),
                buildDefecationEquipment("lt1", "Liming Tank", currentPh, "pH", 8.5f, isPh = true),
                buildDefecationEquipment("dt1", "Defecation Tank", defecatorLevel, "%", 95f),
                buildDefecationEquipment("fv1", "Flash Vessel", flashVesselLevel, "%", 98f)
            )

            val connectedEquipment = listOf(
                EquipmentData("Liming Tank", "pH ${"%.1f".format(currentPh)}", if (currentPh in 6.8..7.5) "Running" else "Warning", if (currentPh in 6.8..7.5) EquipmentStatus.RUNNING else EquipmentStatus.FAULT),
                EquipmentData("Flash Vessel", "${"%.0f".format(flashVesselLevel)}%", "Running", EquipmentStatus.RUNNING),
                EquipmentData("Sedimentation Tank", "${"%.1f".format(sedimentationRate)}%", "Running", EquipmentStatus.RUNNING)
            )

            val isFaulty = equipment.any { it.status == EquipmentStatus.FAULT } || currentPh < 6.5f || currentPh > 8.0f
            val sectionStatus = when {
                isFaulty -> EquipmentStatus.FAULT
                actualFlow < (TARGET_FLOW_LH * 0.5f) -> EquipmentStatus.STANDBY
                else -> EquipmentStatus.HEALTHY
            }

            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            val efficiency = (actualFlow / DESIGN_FLOW_LH * 100f).coerceIn(0f, 100f)
            val healthyEquipCount = equipment.count { it.status != EquipmentStatus.FAULT }
            val availability = healthyEquipCount.toFloat() / equipment.size.toFloat()
            val oee = (efficiency * availability).coerceIn(0f, 100f)

            updateHistory(historyTemp, heater1Temp)
            updateHistory(historyPh, currentPh)
            updateHistory(historyLime, limeDosage)
            updateHistory(historySedimentation, sedimentationRate)
            updateHistory(historyHeatRecovery, heatRecovery)

            val kpis = buildKpis(heater1Temp, currentPh, limeDosage, sedimentationRate, heatRecovery)

            _state.update { currentState ->
                currentState.copy(
                    userName = this.userName, userRole = this.userRole, batchId = batchId, startTime = startTime,
                    sectionStatus = sectionStatus, efficiency = "%.1f".format(efficiency).toDouble(), oee = "%.1f".format(oee).toDouble(),
                    equipment = equipment, connectedEquipment = connectedEquipment,
                    chartData = DefecationChartData(actualFlow.toInt(), TARGET_FLOW_LH, DESIGN_FLOW_LH), kpis = kpis
                )
            }
        } catch (e: Exception) { Log.e(TAG, "Parse error: ${e.message}", e) }
    }

    private fun updateHistory(queue: ArrayDeque<Float>, newValue: Float) {
        if (queue.size >= MAX_HISTORY_POINTS) queue.removeFirst()
        queue.addLast(if (newValue <= 0.01f && queue.isNotEmpty()) queue.last() else newValue)
    }

    private fun buildDefecationEquipment(id: String, name: String, value: Float, unit: String, threshold: Float, isPh: Boolean = false): DefecationEquipment {
        val status = when {
            value <= 0.1f -> EquipmentStatus.STANDBY
            isPh && (value < 6.5f || value > 8.0f) -> EquipmentStatus.FAULT
            !isPh && value > threshold -> EquipmentStatus.FAULT
            else -> EquipmentStatus.HEALTHY
        }
        val displayValue = if (status == EquipmentStatus.STANDBY) "Standby" else "${"%.1f".format(value)} $unit"
        val statusText = if (status == EquipmentStatus.STANDBY) "Ready" else if (status == EquipmentStatus.FAULT) "Warning" else "Running"

        return DefecationEquipment(id, name, displayValue, statusText, status, R.drawable.defecation_image, if(unit == "°C") displayValue else "—")
    }

    private fun buildKpis(temp: Float, ph: Float, lime: Float, sed: Float, heat: Float): List<KpiDataDefecation> {
        return listOf(
            KpiDataDefecation(DefecationKpiType.JUICE_TEMP, "Juice Temperature", "${"%.1f".format(temp)}°C", trendPct(temp, 100f), temp > 100f, historyTemp.toList()),
            KpiDataDefecation(DefecationKpiType.PH_LEVEL, "pH Level", "%.2f".format(ph), trendPct(ph, 7.2f), ph >= 7.2f, historyPh.toList()),
            KpiDataDefecation(DefecationKpiType.LIME_DOSAGE, "Lime Dosage", "${"%.3f".format(lime)}%", trendPct(lime, 0.08f), lime < 0.08f, historyLime.toList()),
            KpiDataDefecation(DefecationKpiType.SEDIMENTATION, "Sedimentation Rate", "${"%.1f".format(sed)}%", trendPct(sed, 95f), sed > 95f, historySedimentation.toList()),
            KpiDataDefecation(DefecationKpiType.HEAT_RECOVERY, "Heat Recovery", "${"%.1f".format(heat)}%", trendPct(heat, 85f), heat > 85f, historyHeatRecovery.toList())
        )
    }

    private fun trendPct(current: Float, baseline: Float) = if (baseline == 0f) "0.0%" else "${"%.1f".format(abs((current - baseline) / baseline * 100f))}%"
    private fun deriveBatchId(tsMs: Long) = if (tsMs <= 0) "D-250520-01" else "D-${java.text.SimpleDateFormat("ddMM", java.util.Locale.US).format(java.util.Date(tsMs))}-01"
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