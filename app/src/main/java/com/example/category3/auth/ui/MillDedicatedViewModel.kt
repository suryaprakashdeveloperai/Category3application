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
// Live-only auxiliary data classes (NOT declared elsewhere)
// ─────────────────────────────────────────────────────────────────────────────

data class RawJuiceData(
    val tankLevelPct: Float,
    val activePumpA: Float,
    val standbyPumpA: Float,
    val flowLhr: Float,
    val flowSp: Float,
    val flowDeviationLhr: Float,
    val volumeFlowM3hr: Float,
    val densityKgM3: Float,
    val temperatureC: Float,
    val heater1InletC: Float,
    val heater1OutletC: Float,
    val heater2OutletC: Float,
    val heater3OutletC: Float,
    val pumpStatus: EquipmentStatus
)

data class CaneStockData(val levelPct: Float)

data class MillSectionPowerData(
    val totalKw: Float,
    val millMotorsTotalKw: Float,
    val prepEquipmentTotalKw: Float
)

/** Aggregate state consumed by the Mill Dedicated screen. */
data class MillLiveState(
    val dashboard: MillDashboardState,
    val rawJuice: RawJuiceData,
    val caneStock: CaneStockData,
    val power: MillSectionPowerData,
    val throughputKgHr: Float,
    val throughputKgS: Float,
    val rjPumpFault: Boolean,
    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)

// ─────────────────────────────────────────────────────────────────────────────
// SSE payload
// ─────────────────────────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class MillDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "MILL_SSE"
        private const val SSE_URL =
            "https://rounds-clicks-nutten-put.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // Thresholds
        private const val HV_NOMINAL       = 6_600f
        private const val HV_BAND_PCT      = 0.03f
        private const val MILL_RPM_MIN     = 720f
        private const val MILL_RPM_MAX     = 760f
        private const val MILL_A_MAX       = 25f
        private const val CUTTER_A_WARN    = 160f
        private const val FIBER_A_WARN     = 200f
        private const val CARRIER_A_WARN   = 25f
        private const val RJ_PUMP_FAULT_A  = 0.5f
        private const val RJ_FLOW_SP       = 5_000f
        private const val RJ_FLOW_TOL      = 200f
        private const val CANE_STOCK_LOW   = 20f
        private const val TARGET_THROUGHPUT = 15_000
        private const val DESIGN_THROUGHPUT = 20_000

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MillDedicatedViewModel(userName, userRole) as T
        }
    }

    // Seed with existing mock so UI is never empty
    private val seedDashboard = MockApiData.getMockState().copy(
        userName = userName, userRole = userRole
    )

    private val _state = MutableStateFlow(
        MillLiveState(
            dashboard        = seedDashboard,
            rawJuice         = emptyRawJuice(),
            caneStock        = CaneStockData(0f),
            power            = MillSectionPowerData(0f, 0f, 0f),
            throughputKgHr   = 0f,
            throughputKgS    = 0f,
            rjPumpFault      = false,
            alerts           = emptyList(),
            connectionStatus = "CONNECTING",
            lastUpdated      = 0L
        )
    )
    val state = _state.asStateFlow()

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    init { startStream() }

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
                    _state.update { it.copy(connectionStatus = "CONNECTED") }
                    Log.d(TAG, "SSE connected")
                }

                override fun onEvent(
                    source: EventSource, id: String?, type: String?, data: String
                ) {
                    viewModelScope.launch(Dispatchers.Default) { parseAndUpdate(data) }
                }

                override fun onClosed(source: EventSource) {
                    _state.update { it.copy(connectionStatus = "DISCONNECTED") }
                    scheduleReconnect()
                }

                override fun onFailure(
                    source: EventSource, t: Throwable?, response: Response?
                ) {
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

    // ─────────────────────────────────────────────────────────────────────────
    private fun parseAndUpdate(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return
            fun tag(key: String): Float = tags[key]?.toFloat() ?: 0f

            // ── Mill Motors (4 units) ──────────────────────────────────────
            val motors: List<MotorData> = (1..4).map { i ->
                val v   = tag("MotorMill${i}_V")
                val a   = tag("MotorMill${i}_A")
                val rpm = tag("MotorMill${i}_RPM")
                val status = deriveMotorStatus(v, a, rpm)
                MotorData(
                    id          = "mm$i",
                    name        = "Mill Motor $i",
                    healthValue = "${fmt2(a)} A",
                    statusText  = buildMotorStatusText(v, a, rpm, status),
                    status      = status,
                    imageRes    = R.drawable.motor_image
                )
            }

            // ── Cane Prep ──────────────────────────────────────────────────
            val carrierV = tag("Motor_CaneCarrier_V")
            val carrierA = tag("Motor_CaneCarrier_A")
            val carrierR = tag("Motor_CaneCarrier_RPM")
            val cutterV  = tag("Motor_CaneCutter_V")
            val cutterA  = tag("Motor_CaneCutter_A")
            val cutterR  = tag("Motor_CaneCutter_RPM")
            val fiberV   = tag("MotorFiberizor_V")
            val fiberA   = tag("MotorFiberizor_A")
            val fiberR   = tag("MotorFiberizor_RPM")

            val connectedEquipment: List<EquipmentData> = listOf(
                buildPrepEquipment("Cane Carrier", carrierV, carrierA, carrierR, CARRIER_A_WARN),
                buildPrepEquipment("Cane Cutter",  cutterV,  cutterA,  cutterR,  CUTTER_A_WARN),
                buildPrepEquipment("Fiberizer",    fiberV,   fiberA,   fiberR,   FIBER_A_WARN)
            )

            // ── Raw Juice ─────────────────────────────────────────────────
            val rjTankLevel    = tag("RJTank_Level_Pct")
            val rjActivePumpA  = tag("RJTank_ActivePump_A")
            val rjStandbyPumpA = tag("RJTank_StandbyPump_A")
            val rjFlowLhr      = tag("RawJuiceFlow")
            val rjVolFlowM3hr  = tag("RawJuice_VolumetricFlow")
            val rjDensity      = tag("RawJuice_Density")
            val rjTemp         = tag("RawJuice_Temperature")
            val rjH1In         = tag("RJHeater1_InletTemp")
            val rjH1Out        = tag("RJHeater1_OutletTemp")
            val rjH2Out        = tag("RJHeater2_OutletTemp")
            val rjH3Out        = tag("RJHeater3_OutletTemp")
            val rjFlowDev      = rjFlowLhr - RJ_FLOW_SP

            val rjPumpFault = rjActivePumpA in 0f..RJ_PUMP_FAULT_A
            val rjPumpStatus = when {
                rjPumpFault        -> EquipmentStatus.FAULT
                rjActivePumpA > 1f -> EquipmentStatus.RUNNING
                else               -> EquipmentStatus.STANDBY
            }

            val rawJuice = RawJuiceData(
                tankLevelPct     = rjTankLevel,
                activePumpA      = rjActivePumpA,
                standbyPumpA     = rjStandbyPumpA,
                flowLhr          = rjFlowLhr,
                flowSp           = RJ_FLOW_SP,
                flowDeviationLhr = rjFlowDev,
                volumeFlowM3hr   = rjVolFlowM3hr,
                densityKgM3      = rjDensity,
                temperatureC     = rjTemp,
                heater1InletC    = rjH1In,
                heater1OutletC   = rjH1Out,
                heater2OutletC   = rjH2Out,
                heater3OutletC   = rjH3Out,
                pumpStatus       = rjPumpStatus
            )

            // ── Cane stock ─────────────────────────────────────────────────
            val caneStock = CaneStockData(tag("CaneStock_Level_Pct"))

            // ── Throughput ─────────────────────────────────────────────────
            val throughputKgHr = tag("MillingThroughput_KG_HR")
            val throughputKgS  = throughputKgHr / 3_600f

            // ── Power ──────────────────────────────────────────────────────
            val millMotorKw = (1..4).sumOf {
                calcKw3Phase(tag("MotorMill${it}_V"), tag("MotorMill${it}_A")).toDouble()
            }.toFloat()
            val prepKw = calcKw3Phase(carrierV, carrierA) +
                    calcKw1Phase(cutterV, cutterA) +
                    calcKw1Phase(fiberV, fiberA)
            val totalKw = millMotorKw + prepKw
            val power = MillSectionPowerData(totalKw, millMotorKw, prepKw)

            // ── Efficiency & OEE ──────────────────────────────────────────
            val healthyMotors = motors.count { it.status != EquipmentStatus.FAULT }
            val availability  = if (motors.isNotEmpty())
                healthyMotors.toFloat() / motors.size.toFloat() else 0f
            val efficiency    = (throughputKgHr / TARGET_THROUGHPUT * 100f).coerceIn(0f, 100f)
            val oee           = (efficiency * availability).coerceIn(0f, 100f)

            // ── Batch / shift ─────────────────────────────────────────────
            val tsMs      = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId   = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            // ── Section status ────────────────────────────────────────────
            val sectionStatus = when {
                motors.any { it.status == EquipmentStatus.FAULT } -> EquipmentStatus.FAULT
                rjPumpFault                                        -> EquipmentStatus.FAULT
                else                                               -> EquipmentStatus.RUNNING
            }

            // ── Alerts ────────────────────────────────────────────────────
            val alerts = buildAlerts(motors, rjPumpFault, rjFlowDev,
                caneStock.levelPct, rjActivePumpA)

            // ── KPIs (reuse existing KpiDataMill / KpiType) ───────────────
            val kpis = buildKpis(throughputKgHr, rjFlowLhr, totalKw)

            // ── Chart ─────────────────────────────────────────────────────
            val chart = OverviewChartData(
                actualThroughput = throughputKgHr.toInt(),
                targetThroughput = TARGET_THROUGHPUT,
                designThroughput = DESIGN_THROUGHPUT
            )

            // ── Compose dashboard update using copy() ────────────────────
            val dashboardUpdated = _state.value.dashboard.copy(
                userName           = userName,
                userRole           = userRole,
                batchId            = batchId,
                startTime          = startTime,
                sectionStatus      = sectionStatus,
                motors             = motors,
                connectedEquipment = connectedEquipment,
                kpis               = kpis,
                chartData          = chart,
                efficiency         = efficiency.toDouble(),
                oee                = oee.toDouble()
            )

            _state.update {
                it.copy(
                    dashboard        = dashboardUpdated,
                    rawJuice         = rawJuice,
                    caneStock        = caneStock,
                    power            = power,
                    throughputKgHr   = throughputKgHr,
                    throughputKgS    = throughputKgS,
                    rjPumpFault      = rjPumpFault,
                    alerts           = alerts,
                    connectionStatus = "CONNECTED",
                    lastUpdated      = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun deriveMotorStatus(v: Float, a: Float, rpm: Float): EquipmentStatus {
        val vLow  = HV_NOMINAL * (1 - HV_BAND_PCT)
        val vHigh = HV_NOMINAL * (1 + HV_BAND_PCT)
        return when {
            v == 0f && a == 0f && rpm == 0f -> EquipmentStatus.STANDBY
            v < vLow || v > vHigh           -> EquipmentStatus.FAULT
            a > MILL_A_MAX                  -> EquipmentStatus.FAULT
            rpm !in MILL_RPM_MIN..MILL_RPM_MAX -> EquipmentStatus.FAULT
            else                            -> EquipmentStatus.HEALTHY
        }
    }

    private fun buildMotorStatusText(
        v: Float, a: Float, rpm: Float, status: EquipmentStatus
    ): String = when (status) {
        EquipmentStatus.STANDBY -> "Ready"
        EquipmentStatus.FAULT   -> "⚠ Out of range"
        else                    -> "Stable · ${fmt0(rpm)} RPM"
    }

    private fun buildPrepEquipment(
        name: String, v: Float, a: Float, rpm: Float, faultA: Float
    ): EquipmentData {
        val status = when {
            v == 0f && a == 0f -> EquipmentStatus.STANDBY
            else               -> EquipmentStatus.RUNNING
        }
        val text = if (a > faultA) "High load" else "Running"
        return EquipmentData(
            name       = name,
            value      = "${fmt1(a)} A",
            statusText = text,
            status     = status
        )
    }

    private fun buildKpis(
        throughputKgHr: Float,
        rjFlowLhr: Float,
        totalKw: Float
    ): List<KpiDataMill> = listOf(
        KpiDataMill(
            type          = KpiType.THROUGHPUT,
            title         = "Throughput",
            value         = "${"%,.0f".format(throughputKgHr)} kg/hr",
            changeString  = trendPct(throughputKgHr, TARGET_THROUGHPUT.toFloat()),
            isUpwardTrend = throughputKgHr >= TARGET_THROUGHPUT * 0.85f,
            trendHistory  = listOf(
                throughputKgHr * 0.90f, throughputKgHr * 0.94f,
                throughputKgHr * 0.97f, throughputKgHr * 0.99f,
                throughputKgHr * 1.01f, throughputKgHr
            )
        ),
        KpiDataMill(
            type          = KpiType.JUICE_FLOW,
            title         = "Raw Juice Flow",
            value         = "${"%,.0f".format(rjFlowLhr)} L/hr",
            changeString  = trendPct(rjFlowLhr, RJ_FLOW_SP),
            isUpwardTrend = abs(rjFlowLhr - RJ_FLOW_SP) <= RJ_FLOW_TOL,
            trendHistory  = listOf(
                rjFlowLhr * 0.97f, rjFlowLhr * 0.98f,
                rjFlowLhr, rjFlowLhr * 1.01f,
                rjFlowLhr * 1.00f, rjFlowLhr
            )
        ),
        KpiDataMill(
            type          = KpiType.EXTRACTION,
            title         = "Extraction %",
            value         = "${"%.1f".format(estimateExtraction(rjFlowLhr, throughputKgHr))}%",
            changeString  = "1.2%",
            isUpwardTrend = true,
            trendHistory  = listOf(90f, 91f, 92f, 92.5f, 93f, 93.6f)
        ),
        KpiDataMill(
            type          = KpiType.BAGASSE,
            title         = "Bagasse %",
            value         = "${"%.1f".format(estimateBagasse(rjFlowLhr, throughputKgHr))}%",
            changeString  = "2.0%",
            isUpwardTrend = false,
            trendHistory  = listOf(30f, 29.5f, 29f, 28.8f, 28.5f, 28.4f)
        ),
        KpiDataMill(
            type          = KpiType.POWER,
            title         = "Power Consumption",
            value         = "${"%,.0f".format(totalKw)} kW",
            changeString  = "3.6%",
            isUpwardTrend = false,
            trendHistory  = listOf(
                totalKw * 1.03f, totalKw * 1.02f,
                totalKw * 1.01f, totalKw * 1.00f,
                totalKw * 0.99f, totalKw
            )
        )
    )

    private fun estimateExtraction(rjFlowLhr: Float, throughputKgHr: Float): Float {
        if (throughputKgHr <= 0f) return 0f
        // rjFlow (L) ≈ (L→kg using density 1.06)
        val juiceKg = rjFlowLhr * 1.06f
        return (juiceKg / throughputKgHr * 100f).coerceIn(0f, 100f)
    }

    private fun estimateBagasse(rjFlowLhr: Float, throughputKgHr: Float): Float =
        (100f - estimateExtraction(rjFlowLhr, throughputKgHr)).coerceIn(0f, 100f)

    private fun trendPct(current: Float, baseline: Float): String {
        if (baseline == 0f) return "0.0%"
        val pct = (current - baseline) / baseline * 100f
        return "${"%.1f".format(abs(pct))}%"
    }

    private fun buildAlerts(
        motors: List<MotorData>,
        rjPumpFault: Boolean,
        rjFlowDev: Float,
        caneStockLevel: Float,
        rjActivePumpA: Float
    ): List<String> {
        val list = mutableListOf<String>()
        motors.filter { it.status == EquipmentStatus.FAULT }.forEach {
            list.add("⚠ ${it.name} — FAULT (${it.statusText})")
        }
        if (rjPumpFault)
            list.add("🔴 RJ Active Pump near-zero (${fmt2(rjActivePumpA)} A)")
        if (abs(rjFlowDev) > RJ_FLOW_TOL * 2)
            list.add("⚠ RJ Flow deviation ${if (rjFlowDev > 0) "+" else ""}${fmt0(rjFlowDev)} L/hr")
        if (caneStockLevel < CANE_STOCK_LOW)
            list.add("⚠ Cane stock low: ${fmt1(caneStockLevel)}%")
        return list
    }

    private fun calcKw3Phase(v: Float, a: Float, pf: Float = 0.85f): Float =
        (1.732f * v * a * pf) / 1_000f

    private fun calcKw1Phase(v: Float, a: Float, pf: Float = 0.85f): Float =
        (v * a * pf) / 1_000f

    private fun fmt0(f: Float) = "%.0f".format(f)
    private fun fmt1(f: Float) = "%.1f".format(f)
    private fun fmt2(f: Float) = "%.2f".format(f)

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

    private fun emptyRawJuice() = RawJuiceData(
        0f, 0f, 0f, 0f, RJ_FLOW_SP, 0f,
        0f, 0f, 0f, 0f, 0f, 0f, 0f,
        EquipmentStatus.STANDBY
    )

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