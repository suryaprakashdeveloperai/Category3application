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
import kotlin.math.abs

data class VacuumPanKpiData(
    val title: String,
    val value: String,
    val changeString: String,
    val isGood: Boolean,
    val trendHistory: List<Float>
)

data class VacuumPanChartData(
    val actual: Float,
    val target: Float,
    val design: Float
)

data class VacuumPanDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val units: List<EquipmentData>,
    val kpis: List<VacuumPanKpiData>,
    val chart: VacuumPanChartData,
    val processStability: Double
)

data class VacuumPanProcessData(
    val fcePressure: Float,     // FCE_Pressure
    val fceVacuum: Float,       // FCE_Vaccum
    val fceBrix: Float,         // FCE_Brix
    val fceInletTemp: Float,    // FCE_Inlet_Temp
    val fceOutletTemp: Float,   // FCE_Outlet_Temp
    val vacuumPumpV: Float,     // Motor_VacuumPump_V
    val vacuumPumpA: Float      // Motor_VacuumPump_A
)

data class VacuumPanLiveState(
    val dashboard: VacuumPanDashboardState,
    val process: VacuumPanProcessData,
    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)

class VacuumPanDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "VACUUMPAN_SSE"
        private const val SSE_URL =
            "https://associate-supplier-alternatives-millennium.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // Targets / design (tune to your plant)
        private const val BRIX_TARGET = 86.0f
        private const val BRIX_DESIGN = 88.0f

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VacuumPanDedicatedViewModel(userName, userRole) as T
        }
    }

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    private val seedDashboard = VacuumPanDashboardState(
        userName = userName,
        userRole = userRole,
        batchId = "BATCH-000",
        startTime = "—",
        sectionStatus = EquipmentStatus.STANDBY,
        units = emptyList(),
        kpis = emptyList(),
        chart = VacuumPanChartData(0f, BRIX_TARGET, BRIX_DESIGN),
        processStability = 0.0
    )

    private val _state = MutableStateFlow(
        VacuumPanLiveState(
            dashboard = seedDashboard,
            process = VacuumPanProcessData(
                fcePressure = 0f,
                fceVacuum = 0f,
                fceBrix = 0f,
                fceInletTemp = 0f,
                fceOutletTemp = 0f,
                vacuumPumpV = 0f,
                vacuumPumpA = 0f
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

            // Same model you already use
            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            fun tagAny(vararg keys: String): Float {
                for (k in keys) {
                    val v = tags[k]
                    if (v != null) return v.toFloat()
                }
                return 0f
            }

            // -------------------- VACUUM PAN / FCE TAGS --------------------
            val fcePressure = tagAny("FCE_Pressure")
            val fceVacuum = tagAny("FCE_Vaccum") // payload spells "Vaccum"
            val fceBrix = tagAny("FCE_Brix")
            val fceInletTemp = tagAny("FCE_Inlet_Temp")
            val fceOutletTemp = tagAny("FCE_Outlet_Temp")

            val vacPumpV = tagAny("Motor_VacuumPump_V")
            val vacPumpA = tagAny("Motor_VacuumPump_A")

            val process = VacuumPanProcessData(
                fcePressure = fcePressure,
                fceVacuum = fceVacuum,
                fceBrix = fceBrix,
                fceInletTemp = fceInletTemp,
                fceOutletTemp = fceOutletTemp,
                vacuumPumpV = vacPumpV,
                vacuumPumpA = vacPumpA
            )

            // -------------------- FAULTS / ALERTS --------------------
            val vacuumFault = fceVacuum != 0f && (fceVacuum < 620f || fceVacuum > 700f)
            val pressureFault = fcePressure != 0f && (fcePressure < 0.05f || fcePressure > 0.30f)
            val brixFault = fceBrix != 0f && (fceBrix < 82f || fceBrix > 92f)
            val vacPumpFault = vacPumpA in 0f..0.5f

            val alerts = buildList {
                if (vacuumFault) add("⚠ FCE Vacuum out of range: %.1f".format(fceVacuum))
                if (pressureFault) add("⚠ FCE Pressure out of range: %.3f bar".format(fcePressure))
                if (brixFault) add("⚠ FCE Brix out of range: %.2f °Bx".format(fceBrix))
                if (vacPumpFault) add("🔴 Vacuum Pump near-zero: %.3f A".format(vacPumpA))
            }

            // -------------------- EQUIPMENT LIST --------------------
            fun statusFromRange(ok: Boolean): EquipmentStatus =
                if (ok) EquipmentStatus.HEALTHY else EquipmentStatus.FAULT

            fun pumpStatusFromAmps(a: Float): EquipmentStatus = when {
                a == 0f -> EquipmentStatus.STANDBY
                a in 0f..0.5f -> EquipmentStatus.FAULT
                else -> EquipmentStatus.RUNNING
            }

            val units = listOf(
                EquipmentData(
                    name = "FCE Vacuum",
                    value = if (fceVacuum == 0f) "—" else "%.1f".format(fceVacuum),
                    statusText = if (vacuumFault) "Out of range" else "OK",
                    status = statusFromRange(!vacuumFault && fceVacuum != 0f)
                ),
                EquipmentData(
                    name = "FCE Pressure",
                    value = if (fcePressure == 0f) "—" else "%.3f bar".format(fcePressure),
                    statusText = if (pressureFault) "Out of range" else "OK",
                    status = statusFromRange(!pressureFault && fcePressure != 0f)
                ),
                EquipmentData(
                    name = "Vacuum Pump",
                    value = "%.1f V / %.3f A".format(vacPumpV, vacPumpA),
                    statusText = if (vacPumpFault) "Near-zero current" else "Running",
                    status = pumpStatusFromAmps(vacPumpA)
                )
            )

            // -------------------- PROCESS STABILITY (simple score) --------------------
            val brixDev = abs(fceBrix - BRIX_TARGET)
            val brixScore = if (fceBrix == 0f) 0.0 else (100.0 - brixDev * 20.0).coerceIn(0.0, 100.0)

            val vacMid = 660f
            val vacDev = abs(fceVacuum - vacMid)
            val vacScore = if (fceVacuum == 0f) 0.0 else (100.0 - vacDev * 0.7).coerceIn(0.0, 100.0)

            val stability = (0.55 * brixScore + 0.45 * vacScore).coerceIn(0.0, 100.0)

            // -------------------- KPI LIST --------------------
            fun trendHistory(cur: Float): List<Float> =
                listOf(cur * 0.97f, cur * 0.985f, cur * 0.995f, cur, cur * 1.01f, cur)

            val kpis = listOf(
                VacuumPanKpiData(
                    title = "FCE Brix",
                    value = if (fceBrix == 0f) "—" else "%.2f °Bx".format(fceBrix),
                    changeString = if (fceBrix == 0f) "" else "Δ%.2f".format(fceBrix - BRIX_TARGET),
                    isGood = !brixFault && fceBrix != 0f,
                    trendHistory = trendHistory(if (fceBrix == 0f) BRIX_TARGET else fceBrix)
                ),
                VacuumPanKpiData(
                    title = "FCE Vacuum",
                    value = if (fceVacuum == 0f) "—" else "%.1f".format(fceVacuum),
                    changeString = "",
                    isGood = !vacuumFault && fceVacuum != 0f,
                    trendHistory = trendHistory(if (fceVacuum == 0f) 660f else fceVacuum)
                ),
                VacuumPanKpiData(
                    title = "FCE Pressure",
                    value = if (fcePressure == 0f) "—" else "%.3f bar".format(fcePressure),
                    changeString = "",
                    isGood = !pressureFault && fcePressure != 0f,
                    trendHistory = trendHistory(if (fcePressure == 0f) 0.15f else fcePressure)
                ),
                VacuumPanKpiData(
                    title = "Vacuum Pump A",
                    value = if (vacPumpA == 0f) "—" else "%.3f A".format(vacPumpA),
                    changeString = "",
                    isGood = !vacPumpFault && vacPumpA != 0f,
                    trendHistory = trendHistory(vacPumpA)
                )
            )

            // -------------------- CHART --------------------
            val chart = VacuumPanChartData(
                actual = fceBrix,
                target = BRIX_TARGET,
                design = BRIX_DESIGN
            )

            // -------------------- SECTION STATUS --------------------
            val sectionStatus = when {
                vacuumFault || pressureFault || brixFault || vacPumpFault -> EquipmentStatus.FAULT
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