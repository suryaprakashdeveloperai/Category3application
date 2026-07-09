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
data class ConcentrationOpenPanUnit(
    val name: String,
    val status: EquipmentStatus,
    val statusText: String,
    val inletValveOn: Boolean,
    val amps: Float,
    val volts: Float,
    val rpm: Float,
    val tempC: Float
)

data class ConcentrationPowderMakerUnit(
    val name: String,
    val status: EquipmentStatus,
    val statusText: String,
    val runFb: Boolean,
    val tripFb: Boolean,
    val systemFaultFb: Boolean,
    val systemReady: Boolean,
    val vacuumHeader: Float,
    val vacuumAtPm: Float,
    val vfdSpeed: Float,
    val motorAmps: Float,
    val remainingCycleTimeS: Float
)

data class ConcentrationDedicatedKpi(
    val title: String,
    val value: String
)

data class ConcentrationDedicatedDashboardState(
    val userName: String,
    val userRole: String,
    val batchId: String,
    val startTime: String,
    val sectionStatus: EquipmentStatus,
    val openPans: List<ConcentrationOpenPanUnit>,
    val powderMakers: List<ConcentrationPowderMakerUnit>,
    val kpis: List<ConcentrationDedicatedKpi>
)

data class ConcentrationDedicatedLiveState(
    val dashboard: ConcentrationDedicatedDashboardState,

    // Boiler / OpenPan overview
    val steamPressure: Float,
    val steamFlow: Float,
    val totalJaggeryKg: Float,
    val openPanAvailableCount: Int,

    // Powder Maker overview
    val powderMakerAvailableCount: Int,

    val alerts: List<String>,
    val connectionStatus: String,
    val lastUpdated: Long
)
class ConcentrationDedicatedViewModel(
    private val userName: String = "Operator",
    private val userRole: String = "Shift Engineer"
) : ViewModel() {

    companion object {
        private const val TAG = "CONCENTRATION_SSE"
        private const val SSE_URL =
            "https://rounds-clicks-nutten-put.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        // OpenPan thresholds (tune)
        private const val OP_TEMP_MIN_OK = 95f
        private const val OP_TEMP_MAX_OK = 110f
        private const val OP_RUNNING_AMPS_MIN = 0.2f

        fun provideFactory(
            userName: String = "Operator",
            userRole: String = "Shift Engineer"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ConcentrationDedicatedViewModel(userName, userRole) as T
        }
    }

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val sseClient = buildInsecureClient()

    private val seedDash = ConcentrationDedicatedDashboardState(
        userName = userName,
        userRole = userRole,
        batchId = "BATCH-000",
        startTime = "—",
        sectionStatus = EquipmentStatus.STANDBY,
        openPans = emptyList(),
        powderMakers = emptyList(),
        kpis = emptyList()
    )

    private val _state = MutableStateFlow(
        ConcentrationDedicatedLiveState(
            dashboard = seedDash,
            steamPressure = 0f,
            steamFlow = 0f,
            totalJaggeryKg = 0f,
            openPanAvailableCount = 0,
            powderMakerAvailableCount = 0,
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

            // IMPORTANT: Uses your existing SSE payload type (same as MillDedicatedViewModel)
            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            fun tag(key: String): Float = tags[key]?.toFloat() ?: 0f
            fun onOff(key: String): Boolean = tag(key) >= 1f

            // ── Boiler / OpenPan overview ─────────────────────────────────
            val steamPressure = tag("Blr_SteamPressure")
            val steamFlow = tag("Blr_SteamFlow")
            val totalJaggeryKg = tag("Total_Jaggery_Produced_KG")
            val openPanAvailableCount = tag("OpenPan_Available_Count").toInt()

            fun buildOpenPan(i: Int): ConcentrationOpenPanUnit {
                val a = tag("OPan${i}_A")
                val v = tag("OPan${i}_V")
                val rpm = tag("OPan${i}_RPM")
                val statusRaw = tag("OPan${i}_Status")
                val temp = tag("OPan${i}_Temp")
                val inletOn = onOff("OPan${i}_InletValve(ON/OFF)")

                val runningCmd = statusRaw >= 1f
                val running = runningCmd && a >= OP_RUNNING_AMPS_MIN
                val tempBad = running && (temp < OP_TEMP_MIN_OK || temp > OP_TEMP_MAX_OK)
                val valveBad = running && !inletOn

                val st = when {
                    running && (tempBad || valveBad) -> EquipmentStatus.FAULT
                    running -> EquipmentStatus.RUNNING
                    else -> EquipmentStatus.STANDBY
                }

                val text = when {
                    !runningCmd -> "Idle"
                    valveBad -> "Inlet valve OFF"
                    tempBad -> "Temp out of band"
                    else -> "Stable"
                }

                return ConcentrationOpenPanUnit(
                    name = "Open Pan $i",
                    status = st,
                    statusText = text,
                    inletValveOn = inletOn,
                    amps = a,
                    volts = v,
                    rpm = rpm,
                    tempC = temp
                )
            }

            val openPans = (1..4).map { buildOpenPan(it) }

            // ── Powder Maker overview ─────────────────────────────────────
            val powderMakerAvailableCount = tag("PowderMaker_Available_Count").toInt()

            fun buildPowderMaker(i: Int): ConcentrationPowderMakerUnit {
                val runFb = tag("PM${i}_RUN_FB") >= 1f
                val tripFb = tag("PM${i}_TRIP_FB") >= 1f
                val sysFault = tag("PM${i}_SYSTEM_FAULT_FB") >= 1f
                val ready = tag("PM${i}_SYSTEM_READY") >= 1f

                val vacuumHeader = tag("PM${i}_VACUUM_AT_HEADER")
                val vacuumAtPm = tag("PM${i}_VACUUM_AT_PM")
                val vfdSpeed = tag("PM${i}_VFD_SPEED_FB")
                val remCycle = tag("PM${i}_REMAINING_CYCLE_TIME")
                val motorAmps = tag("MotorPM${i}_AMPS")

                val st = when {
                    tripFb || sysFault -> EquipmentStatus.FAULT
                    runFb -> EquipmentStatus.RUNNING
                    ready -> EquipmentStatus.HEALTHY
                    else -> EquipmentStatus.STANDBY
                }

                val text = when {
                    tripFb -> "TRIP"
                    sysFault -> "SYSTEM FAULT"
                    runFb -> "Running"
                    ready -> "Ready"
                    else -> "Standby"
                }

                return ConcentrationPowderMakerUnit(
                    name = "Powder Maker $i",
                    status = st,
                    statusText = text,
                    runFb = runFb,
                    tripFb = tripFb,
                    systemFaultFb = sysFault,
                    systemReady = ready,
                    vacuumHeader = vacuumHeader,
                    vacuumAtPm = vacuumAtPm,
                    vfdSpeed = vfdSpeed,
                    motorAmps = motorAmps,
                    remainingCycleTimeS = remCycle
                )
            }

            val powderMakers = (1..4).map { buildPowderMaker(it) }

            // ── Alerts ────────────────────────────────────────────────────
            val alerts = buildList {
                openPans.filter { it.status == EquipmentStatus.FAULT }.forEach { p ->
                    add("⚠ ${p.name}: ${p.statusText} (A=${"%.2f".format(p.amps)} T=${"%.1f".format(p.tempC)}°C)")
                }
                powderMakers.filter { it.status == EquipmentStatus.FAULT }.forEach { pm ->
                    add("⚠ ${pm.name}: ${pm.statusText} (VFD=${"%.1f".format(pm.vfdSpeed)}% A=${"%.1f".format(pm.motorAmps)})")
                }
                if (steamPressure <= 0f) add("⚠ Steam pressure is 0 (check transmitter/line)")
            }

            val anyFault = alerts.isNotEmpty()
            val anyRunning = openPans.any { it.status == EquipmentStatus.RUNNING } ||
                    powderMakers.any { it.status == EquipmentStatus.RUNNING }

            val sectionStatus = when {
                anyFault -> EquipmentStatus.FAULT
                anyRunning -> EquipmentStatus.RUNNING
                else -> EquipmentStatus.STANDBY
            }

            // ── Batch + shift labels ───────────────────────────────────────
            val tsMs = ((payload.timestamp ?: 0.0) * 1_000.0).toLong()
            val batchId = deriveBatchId(tsMs)
            val startTime = deriveShiftStart(tsMs)

            // ── KPIs ───────────────────────────────────────────────────────
            val opRunning = openPans.count { it.status == EquipmentStatus.RUNNING }
            val opFault = openPans.count { it.status == EquipmentStatus.FAULT }
            val pmRunning = powderMakers.count { it.status == EquipmentStatus.RUNNING }
            val pmFault = powderMakers.count { it.status == EquipmentStatus.FAULT }

            val avgOpTemp = openPans.map { it.tempC }.filter { it > 0f }.average().toFloat()
            val avgPmVac = powderMakers.map { it.vacuumHeader }.filter { it > 0f }.average().toFloat()

            val kpis = listOf(
                ConcentrationDedicatedKpi("Open Pans Running", "$opRunning / 4"),
                ConcentrationDedicatedKpi("Open Pan Faults", opFault.toString()),
                ConcentrationDedicatedKpi("Powder Makers Running", "$pmRunning / 4"),
                ConcentrationDedicatedKpi("Powder Maker Faults", pmFault.toString()),
                ConcentrationDedicatedKpi("Steam Pressure", "%.2f".format(steamPressure)),
                ConcentrationDedicatedKpi("Steam Flow", "%,.0f".format(steamFlow)),
                ConcentrationDedicatedKpi("Avg OP Temp", if (avgOpTemp == 0f) "—" else "%.1f °C".format(avgOpTemp)),
                ConcentrationDedicatedKpi("Avg PM Vacuum", if (avgPmVac == 0f) "—" else "%.1f".format(avgPmVac)),
                ConcentrationDedicatedKpi("Jaggery Total", "%,.0f kg".format(totalJaggeryKg)),
                ConcentrationDedicatedKpi("OP Available", openPanAvailableCount.toString()),
                ConcentrationDedicatedKpi("PM Available", powderMakerAvailableCount.toString())
            )

            val dashboardUpdated = _state.value.dashboard.copy(
                userName = userName,
                userRole = userRole,
                batchId = batchId,
                startTime = startTime,
                sectionStatus = sectionStatus,
                openPans = openPans,
                powderMakers = powderMakers,
                kpis = kpis
            )

            _state.update {
                it.copy(
                    dashboard = dashboardUpdated,
                    steamPressure = steamPressure,
                    steamFlow = steamFlow,
                    totalJaggeryKg = totalJaggeryKg,
                    openPanAvailableCount = openPanAvailableCount,
                    powderMakerAvailableCount = powderMakerAvailableCount,
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
        sseClient.dispatcher.executorService.shutdown()
    }
}