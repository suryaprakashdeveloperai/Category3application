package com.example.category3.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class EnergyKpi(
    val id: String,
    val title: String,
    val value: Float,
    val displayValue: String,
    val unit: String,
    val section: String,
    val iconName: String,
    val status: KpiStatus = KpiStatus.NORMAL,
    val threshold: Float? = null,
    val thresholdType: ThresholdType = ThresholdType.ABOVE
)

enum class KpiStatus { NORMAL, WARNING, CRITICAL }
enum class ThresholdType { ABOVE, BELOW }

data class EnergyAlertEvent(
    val id: String,
    val stage: String,
    val message: String,
    val priority: String,
    val type: String,
    val description: String,
    val currentValue: Float,
    val thresholdValue: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val acknowledged: Boolean = false
)

class EnergyViewModel : ViewModel() {
    private val ackTimestamps = ConcurrentHashMap<String, Long>()
    private val REALERT_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
    private val _kpis = MutableStateFlow<List<EnergyKpi>>(emptyList())
    val kpis = _kpis.asStateFlow()

    private val _totalPowerKw = MutableStateFlow(0f)
    val totalPowerKw = _totalPowerKw.asStateFlow()

    private val _peakDemandKw = MutableStateFlow(0f)
    val peakDemandKw = _peakDemandKw.asStateFlow()

    private val _minLoadKw = MutableStateFlow(Float.MAX_VALUE)
    val minLoadKw = _minLoadKw.asStateFlow()

    private val _efficiencyTrend = MutableStateFlow(0f)
    val efficiencyTrend = _efficiencyTrend.asStateFlow()

    private val _activeEnergyAlerts = MutableStateFlow<List<EnergyAlertEvent>>(emptyList())
    val activeEnergyAlerts = _activeEnergyAlerts.asStateFlow()

    private val _connectionStatus = MutableStateFlow("CONNECTING")
    val connectionStatus = _connectionStatus.asStateFlow()

    // Emits newly raised alerts (one-time events)
    private val _energyAlerts = MutableSharedFlow<EnergyAlertEvent>(extraBufferCapacity = 20)
    val energyAlerts = _energyAlerts.asSharedFlow()

    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val gson = Gson()
    private var previousTotalPower = 0f

    private val raisedAlertIds = ConcurrentHashMap.newKeySet<String>()

    private val SSE_URL = "https://dawn-officers-gas-growth.trycloudflare.com/stream"
    private val sseClient = createInsecureSseClient()

    init {
        startStream()
    }

    private fun createInsecureSseClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Failed to create insecure OkHttpClient", e)
        }
    }

    private fun startStream() {
        eventSource?.cancel()
        reconnectJob?.cancel()

        val request = Request.Builder()
            .url(SSE_URL)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(source: EventSource, response: Response) {
                _connectionStatus.value = "CONNECTED"
            }

            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                viewModelScope.launch(Dispatchers.Default) {
                    processStream(data)
                }
            }

            override fun onClosed(source: EventSource) {
                _connectionStatus.value = "DISCONNECTED"
                scheduleReconnect()
            }

            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                _connectionStatus.value = "RECONNECTING"
                scheduleReconnect()
            }
        }

        eventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            delay(5_000)
            startStream()
        }
    }

    fun acknowledgeAlert(alertId: String) {
        val now = System.currentTimeMillis()
        ackTimestamps[alertId] = now

        _activeEnergyAlerts.update { alerts ->
            alerts.map {
                if (it.id == alertId) it.copy(acknowledged = true) else it
            }
        }
    }

    private suspend fun processStream(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return

            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            val processor = TelemetryFrameProcessor(tags)
            processor.processAll()

            updateAlerts(processor.candidateAlerts)

            val trend = if (previousTotalPower > 0f) {
                ((processor.totalPower - previousTotalPower) / previousTotalPower * 100f)
            } else 0f

            previousTotalPower = processor.totalPower

            _kpis.value = processor.kpiList
            _totalPowerKw.value = processor.totalPower
            _efficiencyTrend.value = trend

            if (processor.totalPower > _peakDemandKw.value) {
                _peakDemandKw.value = processor.totalPower
            }
            if (processor.totalPower < _minLoadKw.value) {
                _minLoadKw.value = processor.totalPower
            }
        } catch (e: Exception) {
            Log.e("ENERGY_SSE", "💥 Parse error: ${e.message}", e)
        }
    }

    private fun updateAlerts(candidateAlerts: List<Pair<EnergyAlertEvent, Boolean>>) {
        val newlyRaised = mutableListOf<EnergyAlertEvent>()
        val now = System.currentTimeMillis()

        _activeEnergyAlerts.update { currentActiveList ->
            val currentMap = currentActiveList.associateBy { it.id }.toMutableMap()

            candidateAlerts.forEach { (incomingAlert, shouldRaise) ->
                val id = incomingAlert.id
                val existing = currentMap[id]

                if (shouldRaise) {
                    // Condition is currently violating

                    if (existing == null) {
                        // Brand new violation
                        raisedAlertIds.add(id)
                        currentMap[id] = incomingAlert
                        newlyRaised.add(incomingAlert)
                    } else {
                        // Already known alert
                        if (!existing.acknowledged) {
                            // Not acknowledged yet, just update values and keep active
                            currentMap[id] = existing.copy(
                                currentValue = incomingAlert.currentValue,
                                thresholdValue = incomingAlert.thresholdValue
                            )
                        } else {
                            // Was acknowledged – check if we should re-alert
                            val ackTime = ackTimestamps[id] ?: 0L
                            if (now - ackTime >= REALERT_INTERVAL_MS) {
                                // Re-alert: clear ack and emit as new alert
                                ackTimestamps.remove(id)
                                val reAlert = incomingAlert.copy(
                                    acknowledged = false,
                                    timestamp = now
                                )
                                currentMap[id] = reAlert
                                newlyRaised.add(reAlert)
                            } else {
                                // Still within quiet period, keep it acknowledged but update values
                                currentMap[id] = existing.copy(
                                    currentValue = incomingAlert.currentValue,
                                    thresholdValue = incomingAlert.thresholdValue
                                )
                            }
                        }

                        // Ensure we remember it's currently raised
                        raisedAlertIds.add(id)
                    }
                } else {
                    // Condition resolved, clear all state for this id
                    if (raisedAlertIds.remove(id)) {
                        currentMap.remove(id)
                    }
                    ackTimestamps.remove(id)
                }
            }

            currentMap.values.toList().sortedByDescending { it.timestamp }
        }

        // Emit newly raised/re-raised alerts to the one-time event stream
        newlyRaised.forEach { _energyAlerts.tryEmit(it) }
    }

    private fun shouldTriggerAlert(
        alertId: String,
        current: Float,
        threshold: Float,
        type: ThresholdType
    ): Boolean {
        val buffer = threshold * 0.05f // 5% deadband
        val isCurrentlyRaised = raisedAlertIds.contains(alertId)
        return when (type) {
            ThresholdType.ABOVE ->
                if (isCurrentlyRaised) current > (threshold - buffer) else current > threshold

            ThresholdType.BELOW ->
                if (isCurrentlyRaised) current < (threshold + buffer) else current < threshold
        }
    }

    private inner class TelemetryFrameProcessor(private val tags: Map<String, Double>) {
        val kpiList = mutableListOf<EnergyKpi>()
        val candidateAlerts = mutableListOf<Pair<EnergyAlertEvent, Boolean>>()
        var totalPower = 0f
            private set

        private fun tagFloat(key: String): Float = tags[key]?.toFloat() ?: 0f
        private fun calcKw(volts: Float, amps: Float) = (1.732f * volts * amps * 0.8f) / 1000f

        fun processAll() {
            processBoiler()
            processMilling()
            processEvaporation()
            processConcentration()
            processPumps()
            processPowerQuality()
            processProduction()
            calculateTotals()
        }

        private fun addKpi(kpi: EnergyKpi) {
            kpiList.add(kpi)
        }

        private fun addAlert(
            id: String,
            stage: String,
            message: String,
            priority: String,
            type: String,
            desc: String,
            current: Float,
            threshold: Float,
            threshType: ThresholdType
        ) {
            val shouldRaise = shouldTriggerAlert(id, current, threshold, threshType)
            candidateAlerts.add(
                EnergyAlertEvent(
                    id,
                    stage,
                    message,
                    priority,
                    type,
                    desc,
                    current,
                    threshold
                ) to shouldRaise
            )
        }

        private fun processBoiler() {
            val pressure = tagFloat("Blr_SteamPressure")
            val flow = tagFloat("Blr_SteamFlow")
            val condenserTemp = tagFloat("Wtr_CondenserTemp")

            val tPressMin = ThresholdManager.get("STEAM_PRESSURE_MIN")
            val tPressMax = ThresholdManager.get("STEAM_PRESSURE_MAX")
            val tFlowMax = ThresholdManager.get("STEAM_FLOW_MAX")
            val tCondTemp = ThresholdManager.get("CONDENSER_TEMP_MAX")

            addKpi(
                EnergyKpi(
                    "steam_pressure",
                    "Steam Pressure",
                    pressure,
                    String.format("%.1f", pressure),
                    "Bar",
                    "Boiler",
                    "speed",
                    status = when {
                        pressure < tPressMin -> KpiStatus.CRITICAL
                        pressure > tPressMax -> KpiStatus.WARNING
                        else -> KpiStatus.NORMAL
                    },
                    threshold = tPressMin,
                    thresholdType = ThresholdType.BELOW
                )
            )
            addAlert(
                "E_STEAM_LOW",
                "Boiler",
                "Low Steam Pressure: ${String.format("%.1f", pressure)} Bar",
                "CRITICAL",
                "PRESSURE",
                "Steam pressure below $tPressMin Bar.",
                pressure,
                tPressMin,
                ThresholdType.BELOW
            )

            addKpi(
                EnergyKpi(
                    "steam_flow",
                    "Steam Flow",
                    flow,
                    String.format("%,.0f", flow),
                    "kg/hr",
                    "Boiler",
                    "air",
                    status = if (flow > tFlowMax) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tFlowMax,
                    thresholdType = ThresholdType.ABOVE
                )
            )
            addAlert(
                "E_STEAM_FLOW_HIGH",
                "Boiler",
                "High Steam Flow: ${String.format("%,.0f", flow)} kg/hr",
                "WARNING",
                "FLOW",
                "Steam flow exceeds $tFlowMax kg/hr.",
                flow,
                tFlowMax,
                ThresholdType.ABOVE
            )

            addKpi(
                EnergyKpi(
                    "condenser_temp",
                    "Condenser Temp",
                    condenserTemp,
                    String.format("%.1f", condenserTemp),
                    "°C",
                    "Boiler",
                    "thermostat",
                    status = if (condenserTemp > tCondTemp) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tCondTemp
                )
            )

            val returnCondFlow = tagFloat("ReturnCondensateFlow")
            addKpi(
                EnergyKpi(
                    "return_cond_flow",
                    "Return Condensate",
                    returnCondFlow,
                    String.format("%.1f", returnCondFlow),
                    "m³/hr",
                    "Boiler",
                    "water_drop"
                )
            )

            val etpIn = tagFloat("Blr_ETPwaterIN_VolumetricFlow")
            addKpi(
                EnergyKpi(
                    "etp_water_in",
                    "ETP Water In",
                    etpIn,
                    String.format("%.2f", etpIn),
                    "m³/hr",
                    "Boiler",
                    "water_drop"
                )
            )
        }

        private fun processMilling() {
            val cutterAmps = tagFloat("Motor_CaneCutter_A")
            val fiberizerAmps = tagFloat("MotorFiberizor_A")
            val tCutter = ThresholdManager.get("CANE_CUTTER_AMPS_MAX")
            val tFiber = ThresholdManager.get("FIBERIZOR_AMPS_MAX")
            val tMill = ThresholdManager.get("MILL_MOTOR_AMPS_MAX")

            val cutterKw = calcKw(tagFloat("Motor_CaneCutter_V"), cutterAmps)
            val fiberKw = calcKw(tagFloat("MotorFiberizor_V"), fiberizerAmps)

            addKpi(
                EnergyKpi(
                    "cane_cutter_power",
                    "Cane Cutter",
                    cutterKw,
                    String.format("%.0f", cutterKw),
                    "kW",
                    "Milling",
                    "factory",
                    status = if (cutterAmps > tCutter) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                    threshold = tCutter
                )
            )
            addAlert(
                "E_CUTTER_OVERLOAD",
                "Milling",
                "Cane Cutter Overload",
                "CRITICAL",
                "MOTOR",
                "Exceeds ${tCutter}A.",
                cutterAmps,
                tCutter,
                ThresholdType.ABOVE
            )

            addKpi(
                EnergyKpi(
                    "fiberizer_power",
                    "Fiberizer",
                    fiberKw,
                    String.format("%.0f", fiberKw),
                    "kW",
                    "Milling",
                    "factory",
                    status = if (fiberizerAmps > tFiber) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                    threshold = tFiber
                )
            )

            for (i in 1..4) {
                val amps = tagFloat("MotorMill${i}_A")
                val kw = calcKw(tagFloat("MotorMill${i}_V"), amps)
                addKpi(
                    EnergyKpi(
                        "mill_${i}_power",
                        "Mill $i Motor",
                        kw,
                        String.format("%.0f", kw),
                        "kW",
                        "Milling",
                        "factory",
                        status = if (amps > tMill) KpiStatus.WARNING else KpiStatus.NORMAL,
                        threshold = tMill
                    )
                )
                addAlert(
                    "E_MILL${i}_OVERLOAD",
                    "Milling",
                    "Mill $i Overload",
                    "WARNING",
                    "MOTOR",
                    "Exceeds ${tMill}A.",
                    amps,
                    tMill,
                    ThresholdType.ABOVE
                )
            }
        }

        private fun processEvaporation() {
            val evapB1Temp = tagFloat("Evap_Body1_Temp")
            val evapB4Vac = tagFloat("Evap_Body4_Vaccum")
            val evapB5Vac = tagFloat("Evap_Body5_Vaccum")
            val tTemp = ThresholdManager.get("EVAP_BODY1_TEMP_MAX")
            val tVac = ThresholdManager.get("EVAP_VACUUM_MIN")

            addKpi(
                EnergyKpi(
                    "evap_body1_temp",
                    "Evap Body 1 Temp",
                    evapB1Temp,
                    String.format("%.1f", evapB1Temp),
                    "°C",
                    "Evaporation",
                    "thermostat",
                    status = if (evapB1Temp > tTemp) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                    threshold = tTemp
                )
            )
            addAlert(
                "E_EVAP_TEMP_HIGH",
                "Evaporation",
                "Evap Body 1 Overtemp",
                "CRITICAL",
                "TEMP",
                "Exceeds ${tTemp}°C.",
                evapB1Temp,
                tTemp,
                ThresholdType.ABOVE
            )

            addKpi(
                EnergyKpi(
                    "evap_vacuum_b4",
                    "Evap Vacuum B4",
                    evapB4Vac,
                    String.format("%.0f", evapB4Vac),
                    "mmHg",
                    "Evaporation",
                    "speed",
                    status = if (evapB4Vac < tVac) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tVac,
                    thresholdType = ThresholdType.BELOW
                )
            )
            addKpi(
                EnergyKpi(
                    "evap_vacuum_b5",
                    "Evap Vacuum B5",
                    evapB5Vac,
                    String.format("%.0f", evapB5Vac),
                    "mmHg",
                    "Evaporation",
                    "speed",
                    status = if (evapB5Vac < tVac) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tVac,
                    thresholdType = ThresholdType.BELOW
                )
            )

            val evapFlow = tagFloat("EvapMOND_Volumetricflow")
            addKpi(
                EnergyKpi(
                    "evap_flow",
                    "Evap Flow",
                    evapFlow,
                    String.format("%.1f", evapFlow),
                    "m³/hr",
                    "Evaporation",
                    "water_drop"
                )
            )
        }

        private fun processConcentration() {
            val fceVacuum = tagFloat("FCE_Vaccum")
            val tVacFce = ThresholdManager.get("FCE_VACUUM_MIN")
            val tPanAmps = ThresholdManager.get("OPAN_AMPS_MAX")

            addKpi(
                EnergyKpi(
                    "fce_vacuum",
                    "FCE Vacuum",
                    fceVacuum,
                    String.format("%.0f", fceVacuum),
                    "mmHg",
                    "Concentration",
                    "speed",
                    status = if (fceVacuum < tVacFce) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                    threshold = tVacFce,
                    thresholdType = ThresholdType.BELOW
                )
            )
            addAlert(
                "E_FCE_VACUUM_DROP",
                "Concentration",
                "FCE Vacuum Drop",
                "CRITICAL",
                "PRESSURE",
                "Below ${tVacFce} mmHg.",
                fceVacuum,
                tVacFce,
                ThresholdType.BELOW
            )

            val fceBrix = tagFloat("FCE_Brix")
            addKpi(
                EnergyKpi(
                    "fce_brix",
                    "FCE Brix",
                    fceBrix,
                    String.format("%.1f", fceBrix),
                    "°Bx",
                    "Concentration",
                    "monitor_heart"
                )
            )

            for (i in 1..4) {
                val amps = tagFloat("OPan${i}_A")
                val kw = calcKw(tagFloat("OPan${i}_V"), amps)
                addKpi(
                    EnergyKpi(
                        "opan_${i}_power",
                        "Open Pan $i",
                        kw,
                        String.format("%.1f", kw),
                        "kW",
                        "Concentration",
                        "factory",
                        status = if (amps > tPanAmps) KpiStatus.WARNING else KpiStatus.NORMAL,
                        threshold = tPanAmps
                    )
                )
            }
        }

        private fun processPumps() {
            val feedAmps = tagFloat("Motor_FeedWaterPump_A")
            val condenserAmps = tagFloat("Motor_CondenserPump_A")
            val vacuumAmps = tagFloat("Motor_VacuumPump_A")

            val tFeed = ThresholdManager.get("FEED_WATER_PUMP_MAX")
            val tCond = ThresholdManager.get("CONDENSER_PUMP_MAX")
            val tVac = ThresholdManager.get("VACUUM_PUMP_MAX")

            val feedKw = calcKw(tagFloat("Motor_FeedWaterPump_V"), feedAmps)
            val condKw = calcKw(tagFloat("Motor_CondenserPump_V"), condenserAmps)
            val vacKw = calcKw(tagFloat("Motor_VacuumPump_V"), vacuumAmps)

            addKpi(
                EnergyKpi(
                    "feed_water_pump",
                    "Feed Water Pump",
                    feedKw,
                    String.format("%.1f", feedKw),
                    "kW",
                    "Pumps",
                    "water_drop",
                    status = if (feedAmps > tFeed) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tFeed
                )
            )
            addKpi(
                EnergyKpi(
                    "condenser_pump",
                    "Condenser Pump",
                    condKw,
                    String.format("%.1f", condKw),
                    "kW",
                    "Pumps",
                    "water_drop",
                    status = if (condenserAmps > tCond) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tCond
                )
            )
            addKpi(
                EnergyKpi(
                    "vacuum_pump",
                    "Vacuum Pump",
                    vacKw,
                    String.format("%.1f", vacKw),
                    "kW",
                    "Pumps",
                    "autorenew",
                    status = if (vacuumAmps > tVac) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tVac
                )
            )
        }

        private fun processPowerQuality() {
            val lvList = listOf(
                tagFloat("Motor_CaneCutter_V"),
                tagFloat("MotorFiberizor_V"),
                tagFloat("Motor_CaneCarrier_V")
            )
            val hvList = listOf(
                tagFloat("MotorMill1_V"),
                tagFloat("MotorMill2_V"),
                tagFloat("MotorMill3_V"),
                tagFloat("MotorMill4_V")
            )

            val avgLv = if (lvList.isNotEmpty()) lvList.average().toFloat() else 0f
            val avgHv = if (hvList.isNotEmpty()) hvList.average().toFloat() else 0f

            val tLvMin = ThresholdManager.get("LV_VOLTAGE_MIN")
            val tLvMax = ThresholdManager.get("LV_VOLTAGE_MAX")
            val tHvMin = ThresholdManager.get("HV_VOLTAGE_MIN")

            addKpi(
                EnergyKpi(
                    "lv_voltage",
                    "LV Bus Voltage",
                    avgLv,
                    String.format("%.0f", avgLv),
                    "V",
                    "Power Quality",
                    "electric_bolt",
                    status = when {
                        avgLv < tLvMin -> KpiStatus.CRITICAL
                        avgLv > tLvMax -> KpiStatus.WARNING
                        else -> KpiStatus.NORMAL
                    }
                )
            )
            addAlert(
                "E_LV_UNDERVOLT",
                "Power Quality",
                "LV Undervoltage",
                "CRITICAL",
                "VOLTAGE",
                "Below ${tLvMin}V.",
                avgLv,
                tLvMin,
                ThresholdType.BELOW
            )

            addKpi(
                EnergyKpi(
                    "hv_voltage",
                    "HV Bus Voltage",
                    avgHv,
                    String.format("%,.0f", avgHv),
                    "V",
                    "Power Quality",
                    "electric_bolt",
                    status = if (avgHv < tHvMin) KpiStatus.CRITICAL else KpiStatus.NORMAL
                )
            )
        }

        private fun processProduction() {
            val totalJaggery = tagFloat("Total_Jaggery_Produced_KG")
            val yieldEfficiency = tagFloat("Yield_Efficiency_Percent")
            val tYield = ThresholdManager.get("YIELD_EFFICIENCY_MIN")

            addKpi(
                EnergyKpi(
                    "total_production",
                    "Total Produced",
                    totalJaggery,
                    String.format("%,.0f", totalJaggery),
                    "kg",
                    "Production",
                    "factory"
                )
            )

            addKpi(
                EnergyKpi(
                    "yield_efficiency",
                    "Yield Efficiency",
                    yieldEfficiency,
                    String.format("%.1f", yieldEfficiency),
                    "%",
                    "Production",
                    "monitor_heart",
                    status = if (yieldEfficiency < tYield) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tYield,
                    thresholdType = ThresholdType.BELOW
                )
            )
        }

        private fun calculateTotals() {
            totalPower = kpiList.filter { it.unit == "kW" }
                .sumOf { it.value.toDouble() }
                .toFloat()

            val tEnergy = ThresholdManager.get("TOTAL_ENERGY_MAX")
            val tPf = ThresholdManager.get("POWER_FACTOR_MIN")

            addKpi(
                EnergyKpi(
                    "total_plant_power",
                    "Total Plant Load",
                    totalPower,
                    String.format("%,.0f", totalPower),
                    "kW",
                    "Power Quality",
                    "electric_bolt",
                    status = if (totalPower > tEnergy) KpiStatus.WARNING else KpiStatus.NORMAL,
                    threshold = tEnergy
                )
            )

            addAlert(
                "E_TOTAL_POWER_HIGH",
                "Power Quality",
                "High Plant Load",
                "WARNING",
                "POWER",
                "Exceeds ${tEnergy} kW.",
                totalPower,
                tEnergy,
                ThresholdType.ABOVE
            )

            val pfCurrent = 0.8f // placeholder, until you have telemetry
            addKpi(
                EnergyKpi(
                    "power_factor",
                    "Power Factor",
                    pfCurrent,
                    String.format("%.2f", pfCurrent),
                    "pf",
                    "Power Quality",
                    "monitor_heart",
                    status = if (pfCurrent < tPf) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                    threshold = tPf,
                    thresholdType = ThresholdType.BELOW
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
        reconnectJob?.cancel()
        sseClient.dispatcher.executorService.shutdown()
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EnergyViewModel() as T
            }
    }
}