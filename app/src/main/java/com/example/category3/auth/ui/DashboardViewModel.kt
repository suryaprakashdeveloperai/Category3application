package com.example.category3.auth.ui

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

data class AlertData(
    val id: String,
    val stage: String,
    val message: String,
    val priority: String,
    val type: String,
    val description: String,
    val sourceRoute: String = "workflow_dashboard",
    val timestamp: Long = System.currentTimeMillis(),
    val targetSection: String? = null,
    val targetAlertId: String? = null,
    val acknowledged: Boolean = false
)

data class RecommendationData(
    val id: String,
    val issue: String,
    val action: String,
    val priority: String,
    val stage: String
)

data class LiveStageData(
    val id: String,
    val name: String,
    val status: String,
    val tempC: Double,
    val actualFlow: Float,
    val targetFlow: Float,
    val pressureBar: Float,
    val efficiency: Int,
    val activeAlerts: Int,
    val recommendations: List<RecommendationData>,
    val color: Color,
    val energyKw: Float,
    val vibrationHz: Float,
    val aiProjection: Float,
    val riskScore: Float,
    val tankFillPercent: Int = 0,
    val tankEdgeColor: Color = Color.Gray
)

/** Exposed timing snapshots */
data class OpanTimerSnapshot(
    val pan: String,              // OPan1..OPan4
    val running: Boolean,
    val elapsedMinutes: Double
)

data class PmTimerSnapshot(
    val pm: String,               // PM1..PM4
    val active: Boolean,
    val phase: String,            // MIX / CRYST / POWDER / DROP_R1 / DROP_F1 / DROP_R2 / IDLE
    val elapsedMinutes: Double,
    val remainingMinutes: Double,
    val totalCycleMinutes: Double
)

class DashboardViewModel : ViewModel() {

    private val PREFIX_PROCESS = "A_"
    private val PREFIX_ENERGY = "E_"
    private val PREFIX_PROD = "P_"

    private val _globalOee = MutableStateFlow(87)
    val globalOee = _globalOee.asStateFlow()

    private val _globalEnergy = MutableStateFlow(217f)
    val globalEnergy = _globalEnergy.asStateFlow()

    private val _globalThroughput = MutableStateFlow(495)
    val globalThroughput = _globalThroughput.asStateFlow()

    private val _stages = MutableStateFlow(getInitialStages())
    val stages = _stages.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<AlertData>>(emptyList())
    val activeAlerts = _activeAlerts.asStateFlow()

    /** NEW: batch + timing flows */
    private val _activeBatchNumber = MutableStateFlow(0)
    val activeBatchNumber = _activeBatchNumber.asStateFlow()

    private val _opanTimers = MutableStateFlow<List<OpanTimerSnapshot>>(emptyList())
    val opanTimers = _opanTimers.asStateFlow()

    private val _opanMaxElapsedMinutes = MutableStateFlow(0.0)
    val opanMaxElapsedMinutes = _opanMaxElapsedMinutes.asStateFlow()

    private val _pmTimers = MutableStateFlow<List<PmTimerSnapshot>>(emptyList())
    val pmTimers = _pmTimers.asStateFlow()

    private val _pmMaxElapsedMinutes = MutableStateFlow(0.0)
    val pmMaxElapsedMinutes = _pmMaxElapsedMinutes.asStateFlow()

    private val _pmTotalCycleMinutes = MutableStateFlow(0.0)
    val pmTotalCycleMinutes = _pmTotalCycleMinutes.asStateFlow()

    private val processAlertsMap = ConcurrentHashMap<String, AlertData>()
    private val energyAlertsMap = ConcurrentHashMap<String, AlertData>()
    private val prodAlertsMap = ConcurrentHashMap<String, AlertData>()

    private val telemetryQueue = Channel<String>(Channel.BUFFERED)

    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val gson = Gson()

    private val sseClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val SSE_URL = "https://associate-supplier-alternatives-millennium.trycloudflare.com/stream"

    /** OPan inferred timers (no PLC timer tags available) */
    private val opanStartMillis = mutableMapOf<Int, Long>()

    /** Batch tracking */
    private var lastTotalBatchesCompleted = -1

    /** Tuning */
    private val OPAN_COUNT = 4
    private val PM_COUNT = 4
    private val OPAN_FLOW_THRESHOLD = 50.0  // adjust to your plant scale

    init {
        startTelemetryProcessor()
        startSseStream()
    }

    private fun startTelemetryProcessor() {
        viewModelScope.launch(Dispatchers.Default) {
            telemetryQueue.consumeAsFlow().collect { raw ->
                processIncomingStream(raw)
            }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            when {
                alertId.startsWith(PREFIX_PROCESS) ->
                    processAlertsMap[alertId]?.let { processAlertsMap[alertId] = it.copy(acknowledged = true) }
                alertId.startsWith(PREFIX_ENERGY) ->
                    energyAlertsMap[alertId]?.let { energyAlertsMap[alertId] = it.copy(acknowledged = true) }
                alertId.startsWith(PREFIX_PROD) ->
                    prodAlertsMap[alertId]?.let { prodAlertsMap[alertId] = it.copy(acknowledged = true) }
            }
            updateCombinedAlertsFlow()
        }
    }

    fun injectEnergyAlert(alert: AlertData) {
        viewModelScope.launch(Dispatchers.Default) {
            val id = if (alert.id.startsWith(PREFIX_ENERGY)) alert.id else "$PREFIX_ENERGY${alert.id}"
            energyAlertsMap[id] = alert.copy(
                id = id,
                sourceRoute = "energy_tab",
                targetSection = alert.targetSection ?: alert.stage,
                targetAlertId = alert.targetAlertId ?: id
            )
            updateCombinedAlertsFlow()
        }
    }

    fun syncEnergyAlerts(activeIds: Set<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            val formatted = activeIds.map { if (it.startsWith(PREFIX_ENERGY)) it else "$PREFIX_ENERGY$it" }.toSet()
            val altered = energyAlertsMap.keys.removeAll { it !in formatted }
            if (altered) updateCombinedAlertsFlow()
        }
    }

    fun injectProductionAlert(alert: AlertData) {
        viewModelScope.launch(Dispatchers.Default) {
            val id = if (alert.id.startsWith(PREFIX_PROD)) alert.id else "$PREFIX_PROD${alert.id}"
            prodAlertsMap[id] = alert.copy(
                id = id,
                sourceRoute = "production_tab",
                targetSection = alert.targetSection ?: alert.stage,
                targetAlertId = alert.targetAlertId ?: id
            )
            updateCombinedAlertsFlow()
        }
    }

    fun syncProductionAlerts(activeIds: Set<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            val formatted = activeIds.map { if (it.startsWith(PREFIX_PROD)) it else "$PREFIX_PROD$it" }.toSet()
            val altered = prodAlertsMap.keys.removeAll { it !in formatted }
            if (altered) updateCombinedAlertsFlow()
        }
    }

    private fun updateCombinedAlertsFlow() {
        _activeAlerts.value = (processAlertsMap.values + energyAlertsMap.values + prodAlertsMap.values)
            .sortedWith(compareBy({ it.acknowledged }, { -it.timestamp }))
    }

    private fun startSseStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val request = Request.Builder()
            .url(SSE_URL)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {}

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                telemetryQueue.trySend(data)
            }

            override fun onClosed(eventSource: EventSource) {
                scheduleReconnect()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                scheduleReconnect()
            }
        }

        eventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            delay(5_000.milliseconds)
            startSseStream()
        }
    }

    private fun processIncomingStream(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            val nowMillis = System.currentTimeMillis()
            val processor = TelemetryProcessor(tags, nowMillis)

            _globalOee.value = processor.calculateOee()
            _globalThroughput.value = processor.calculateThroughput()
            _globalEnergy.value = processor.calculateEnergy()
            _stages.value = processor.mapStages(_stages.value)

            processor.updateBatchNumber()
            processor.updateOpanTimers()
            processor.updatePmTimers()

            processor.evaluateProcessAlerts()

        } catch (e: Exception) {
            Log.e("SSE_PARSE", "Stream processor fault", e)
        }
    }

    private inner class TelemetryProcessor(
        private val tags: Map<String, Double>,
        private val nowMillis: Long
    ) {
        fun tagDouble(key: String, default: Double = 0.0) = tags[key] ?: default
        fun tagFloat(key: String, default: Float = 0f) = tagDouble(key, default.toDouble()).toFloat()
        fun tagInt(key: String, default: Int = 0) = tagDouble(key, default.toDouble()).toInt()

        fun calculateOee(): Int = tagInt("Yield_Efficiency_Percent", 87)
        fun calculateThroughput(): Int =
            (tagDouble("MillingThroughput_KG_HR") / 1000.0 * 24.0).toInt()

        fun calculateEnergy(): Float {
            val totalAmps = tagDouble("MotorMill1_A") + tagDouble("MotorMill2_A") +
                    tagDouble("MotorMill3_A") + tagDouble("MotorMill4_A") +
                    tagDouble("Motor_CaneCutter_A") + tagDouble("MotorFiberizor_A")
            return (1.732 * 415 * totalAmps * 0.8 / 1000).toFloat()
        }

        fun mapStages(current: List<LiveStageData>): List<LiveStageData> = current.map { stage ->
            when (stage.id) {
                "01" -> stage.copy(
                    actualFlow = tagFloat("MillingThroughput_KG_HR"),
                    tempC = tagDouble("RawJuiceFlow_Temp", stage.tempC),
                    energyKw = tagFloat("Motor_CaneCutter_A"),
                    vibrationHz = tagFloat("MotorFiberizor_RPM")
                )
                "02" -> stage.copy(
                    actualFlow = tagFloat("RawJuiceFlow"),
                    tempC = tagDouble("DJHeater3_Temp_PV", stage.tempC),
                    pressureBar = tagFloat("Deficator_pH", 7.0f),
                    vibrationHz = tagFloat("DJTank_Pump(Active)_amps")
                )
                "03" -> stage.copy(
                    actualFlow = tagFloat("EvapMOND_Volumetricflow"),
                    tempC = tagDouble("Evap_Body1_Temp", stage.tempC),
                    pressureBar = tagFloat("Evap_Body1_Pressure"),
                    efficiency = tagInt("Evap_Body5_Brix")
                )
                "04" -> stage.copy(
                    actualFlow = tagFloat("CJ_JuiceFlow"),
                    tempC = tagDouble("CJ_Heater_inlet_temp", stage.tempC),
                    vibrationHz = tagFloat("FC1(VFD_Motor)")
                )
                "05" -> stage.copy(
                    actualFlow = tagFloat("OP_FCMOND_VolumetricFlow"),
                    tempC = tagDouble("OPan1_Temp", stage.tempC),
                    pressureBar = tagFloat("FCE_Pressure"),
                    energyKw = tagFloat("OPan1_A"),
                    vibrationHz = tagFloat("OPan1_RPM")
                )
                else -> stage
            }
        }

        fun updateBatchNumber() {
            val completed = tagInt("Total_Batches_Completed", 0)
            if (completed != lastTotalBatchesCompleted) {
                lastTotalBatchesCompleted = completed
            }
            _activeBatchNumber.value = completed + 1
        }

        /** OPan has no remaining timer tags => infer elapsed using state transitions */
        fun updateOpanTimers() {
            val flow = tagDouble("OP_FCMOND_VolumetricFlow", 0.0)
            val opanSnapshots = ArrayList<OpanTimerSnapshot>(OPAN_COUNT)

            for (i in 1..OPAN_COUNT) {
                val statusKey = "OPan$i" + "_Status"
                val valveKey = "OPan$i" + "_InletValve(ON/OFF)"   // exact key in your payload

                val running = tagInt(statusKey) == 1 &&
                        tagInt(valveKey) == 1 &&
                        flow > OPAN_FLOW_THRESHOLD

                if (running) {
                    if (opanStartMillis[i] == null) opanStartMillis[i] = nowMillis
                } else {
                    opanStartMillis.remove(i)
                }

                val elapsedMin = opanStartMillis[i]
                    ?.let { (nowMillis - it) / 60_000.0 }
                    ?: 0.0

                opanSnapshots += OpanTimerSnapshot(
                    pan = "OPan$i",
                    running = running,
                    elapsedMinutes = elapsedMin
                )
            }

            _opanTimers.value = opanSnapshots
            _opanMaxElapsedMinutes.value = opanSnapshots.maxOfOrNull { it.elapsedMinutes } ?: 0.0
        }

        fun updatePmTimers() {
            val pmSnapshots = ArrayList<PmTimerSnapshot>(PM_COUNT)

            for (i in 1..PM_COUNT) {
                val pm = "PM$i"

                // --- Setpoints (seconds)
                val mixSp = tagDouble("${pm}_MIXING_TIME_SP")
                val crySp = tagDouble("${pm}_CRYSTALISATION_TIME_SP")
                val powSp = tagDouble("${pm}_POWDER_MAKING_TIME_SP")
                val dropR1Sp = tagDouble("${pm}_DROPING_TIME_R1_SP")
                val dropF1Sp = tagDouble("${pm}_DROPING_TIME_F1_SP")

                // Exists in your payload (not suffixed _SP). Treat as configured duration (seconds).
                val dropR2Sp = tagDouble("${pm}_DROPING_TIME_R2")

                val totalSec = mixSp + crySp + powSp + dropR1Sp + dropF1Sp + dropR2Sp
                val totalMin = if (totalSec > 0) totalSec / 60.0 else 0.0

                // --- Remaining timers (seconds) from your payload
                val remMix = tagDouble("${pm}_REMAINING_MIXING_TIME")
                val remCry = tagDouble("${pm}_REMAINING_CRYSTALISATION_TIME")

                // IMPORTANT: You do NOT have REMAINING_POWDER_MAKING_TIME.
                // You DO have REMAINING_PM_TIME; use it as powder-making remaining.
                val remPow = tagDouble("${pm}_REMAINING_PM_TIME")

                val remDropR1 = tagDouble("${pm}_REMAINING_DROPING_TIME_R1")
                val remDropF1 = tagDouble("${pm}_REMAINING_DROPING_TIME_F1")
                val remDropR2 = tagDouble("${pm}_REMAINING_DROPING_TIME_R2")

                // activity gating (prevents showing elapsed from stale values when totally idle)
                val runFb = tagInt("${pm}_RUN_FB")
                val vfdSpeed = tagDouble("${pm}_VFD_SPEED_FB")
                val feedOpen = tagInt("${pm}_FEED_VALVE_OPEN_FB")
                val auto = tagInt("${pm}_CYCLE_AUTO")

                val anyRemaining = (remMix + remCry + remPow + remDropR1 + remDropF1 + remDropR2) > 0.01
                val active = (auto == 1) && (runFb == 1 || vfdSpeed > 0.1 || feedOpen == 1 || anyRemaining)

                // Determine phase by remaining timers (your PLC structure)
                val (phase, elapsedSec, remainingTotalSec) = computePmPhaseAndTimes(
                    mixSp, crySp, powSp, dropR1Sp, dropF1Sp, dropR2Sp,
                    remMix, remCry, remPow, remDropR1, remDropF1, remDropR2
                )

                pmSnapshots += PmTimerSnapshot(
                    pm = pm,
                    active = active && phase != "IDLE",
                    phase = if (active) phase else "IDLE",
                    elapsedMinutes = if (active) (elapsedSec / 60.0) else 0.0,
                    remainingMinutes = if (active) (remainingTotalSec / 60.0) else 0.0,
                    totalCycleMinutes = totalMin
                )
            }

            _pmTimers.value = pmSnapshots
            _pmMaxElapsedMinutes.value = pmSnapshots.maxOfOrNull { it.elapsedMinutes } ?: 0.0

            // publish a representative "total cycle minutes"
            _pmTotalCycleMinutes.value = pmSnapshots.firstOrNull { it.totalCycleMinutes > 0 }?.totalCycleMinutes ?: 0.0
        }

        private fun computePmPhaseAndTimes(
            mixSp: Double, crySp: Double, powSp: Double,
            dropR1Sp: Double, dropF1Sp: Double, dropR2Sp: Double,
            remMix: Double, remCry: Double, remPow: Double,
            remDropR1: Double, remDropF1: Double, remDropR2: Double
        ): Triple<String, Double, Double> {
            // Phase priority: MIX -> CRYST -> POWDER -> DROP segments
            return when {
                remMix > 0.0 -> {
                    val elapsed = (mixSp - remMix).coerceAtLeast(0.0)
                    val remainingTotal = remMix + crySp + powSp + dropR1Sp + dropF1Sp + dropR2Sp
                    Triple("MIX", elapsed, remainingTotal)
                }

                remCry > 0.0 -> {
                    val elapsed = mixSp + (crySp - remCry).coerceAtLeast(0.0)
                    val remainingTotal = remCry + powSp + dropR1Sp + dropF1Sp + dropR2Sp
                    Triple("CRYST", elapsed, remainingTotal)
                }

                remPow > 0.0 -> {
                    val elapsed = mixSp + crySp + (powSp - remPow).coerceAtLeast(0.0)
                    val remainingTotal = remPow + dropR1Sp + dropF1Sp + dropR2Sp
                    Triple("POWDER", elapsed, remainingTotal)
                }

                remDropR1 > 0.0 -> {
                    val elapsed = mixSp + crySp + powSp + (dropR1Sp - remDropR1).coerceAtLeast(0.0)
                    val remainingTotal = remDropR1 + dropF1Sp + dropR2Sp
                    Triple("DROP_R1", elapsed, remainingTotal)
                }

                remDropF1 > 0.0 -> {
                    val elapsed = mixSp + crySp + powSp + dropR1Sp + (dropF1Sp - remDropF1).coerceAtLeast(0.0)
                    val remainingTotal = remDropF1 + dropR2Sp
                    Triple("DROP_F1", elapsed, remainingTotal)
                }

                remDropR2 > 0.0 -> {
                    val elapsed = mixSp + crySp + powSp + dropR1Sp + dropF1Sp + (dropR2Sp - remDropR2).coerceAtLeast(0.0)
                    val remainingTotal = remDropR2
                    Triple("DROP_R2", elapsed, remainingTotal)
                }

                else -> Triple("IDLE", 0.0, 0.0)
            }
        }

        fun evaluateProcessAlerts() {
            val evapTemp = tagDouble("Evap_Body1_Temp")
            val defecatorPh = tagDouble("Deficator_pH", 7.0)
            val fceVacuum = tagDouble("FCE_Vaccum")
            val boilerSteam = tagDouble("Blr_SteamPressure", 12.0)

            val incoming = mutableSetOf<String>()

            fun evalAlert(id: String, cond: Boolean, builder: () -> AlertData) {
                val aid = PREFIX_PROCESS + id
                if (cond) {
                    incoming.add(aid)
                    if (!processAlertsMap.containsKey(aid)) processAlertsMap[aid] = builder()
                }
            }

            evalAlert("EVAP_TEMP", evapTemp > ThresholdManager.get("EVAP_BODY1_TEMP_MAX")) {
                AlertData("${PREFIX_PROCESS}EVAP_TEMP", "EVAPORATION", "High Evap Temp: ${evapTemp.toInt()} °C", "CRITICAL", "TEMP", "Immediate cooling required.")
            }
            evalAlert(
                "DEFEC_PH",
                defecatorPh < ThresholdManager.get("DEFECATOR_PH_MIN") || defecatorPh > ThresholdManager.get("DEFECATOR_PH_MAX")
            ) {
                AlertData("${PREFIX_PROCESS}DEFEC_PH", "DEFECATION", "Abnormal pH: ${"%.2f".format(defecatorPh)}", "WARNING", "pH", "Adjust lime stabilizer.")
            }
            evalAlert("VACUUM_DROP", fceVacuum < ThresholdManager.get("FCE_VACUUM_MIN") && tagDouble("OPan1_Status") == 1.0) {
                AlertData("${PREFIX_PROCESS}VACUUM_DROP", "CONCENTRATION", "Vacuum Drop: ${fceVacuum.toInt()} mmHg", "WARNING", "PRESSURE", "Check vacuum pumps.")
            }
            evalAlert("BOILER_STM", boilerSteam < ThresholdManager.get("STEAM_PRESSURE_MIN")) {
                AlertData("${PREFIX_PROCESS}BOILER_STM", "PLANT UTILITY", "Low Steam Pressure: ${boilerSteam.toInt()} Bar", "CRITICAL", "PRESSURE", "Check boiler fuel feed.")
            }

            val altered = processAlertsMap.keys.removeAll { it !in incoming }
            if (incoming.isNotEmpty() || altered) updateCombinedAlertsFlow()
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
        reconnectJob?.cancel()
        telemetryQueue.close()
    }

    private fun getInitialStages(): List<LiveStageData> = listOf(
        LiveStageData("01", "MILL", "RUN", 25.0, 25f, 30f, 47f, 110, 15, listOf(), Color(0xFF47B3E2), 25f, 64f, 124f, 2.5f, 52, Color(0xFF47B3E2)),
        LiveStageData("02", "DEFECATION", "RUN", 45.0, 47f, 45f, 26f, 95, 5, listOf(), Color(0xFF11CFC9), 39f, 82f, 43f, 1.2f, 17, Color(0xFFF68420)),
        LiveStageData("03", "EVAPORATION", "BLOCK", 85.0, 86f, 80f, 64f, 80, 45, listOf(), Color(0xFFF68420), 70f, 100f, 51f, 3.8f, 50, Color(0xFF47B3E2)),
        LiveStageData("04", "CLARIFICATION", "RUN", 50.0, 50f, 60f, 34f, 105, 12, listOf(), Color(0xFF496D89), 34f, 75f, 64f, 1.8f, 29, Color(0xFFD68A51)),
        LiveStageData("05", "CONCENTRATION", "RUN", 60.0, 61f, 75f, 45f, 105, 8, listOf(), Color(0xFFD68A51), 71f, 59f, 80f, 0.4f, 40, Color(0xFFD68A51))
    )

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel() as T
        }
    }
}