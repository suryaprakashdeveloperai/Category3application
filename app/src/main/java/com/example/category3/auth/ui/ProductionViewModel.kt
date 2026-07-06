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

// If you already have this class elsewhere, DELETE this one.


data class ProductionKpi(
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

data class ProductionAlertEvent(
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

class ProductionViewModel : ViewModel() {

    private val ackTimestamps = ConcurrentHashMap<String, Long>()
    private val raisedAlertIds = ConcurrentHashMap.newKeySet<String>()
    private val REALERT_INTERVAL_MS = 10 * 60 * 1000L

    private val _kpis = MutableStateFlow<List<ProductionKpi>>(emptyList())
    val kpis = _kpis.asStateFlow()

    private val _activeProductionAlerts = MutableStateFlow<List<ProductionAlertEvent>>(emptyList())
    val activeProductionAlerts = _activeProductionAlerts.asStateFlow()

    private val _connectionStatus = MutableStateFlow("CONNECTING")
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _productionAlerts = MutableSharedFlow<ProductionAlertEvent>(extraBufferCapacity = 20)
    val productionAlerts = _productionAlerts.asSharedFlow()

    private var eventSource: EventSource? = null
    private var reconnectJob: Job? = null
    private val gson = Gson()

    private val SSE_URL = "https://sit-sims-cloudy-encourages.trycloudflare.com/stream"
    private val sseClient = createInsecureSseClient()

    init { startStream() }

    private fun createInsecureSseClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ssl = SSLContext.getInstance("TLS")
        ssl.init(null, trustAll, SecureRandom())
        return OkHttpClient.Builder()
            .sslSocketFactory(ssl.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
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
            override fun onOpen(source: EventSource, response: Response) { _connectionStatus.value = "CONNECTED" }
            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                viewModelScope.launch(Dispatchers.Default) { processStream(data) }
            }
            override fun onClosed(source: EventSource) { _connectionStatus.value = "DISCONNECTED"; scheduleReconnect() }
            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) { _connectionStatus.value = "RECONNECTING"; scheduleReconnect() }
        }
        eventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch { delay(5_000); startStream() }
    }

    fun acknowledgeAlert(alertId: String) {
        ackTimestamps[alertId] = System.currentTimeMillis()
        _activeProductionAlerts.update { list ->
            list.map { if (it.id == alertId) it.copy(acknowledged = true) else it }
        }
    }

    private fun processStream(rawData: String) {
        try {
            val json = rawData.trimStart().removePrefix("data:").trim()
            if (json.isEmpty() || json == ":") return
            val payload = gson.fromJson(json, IndustrialTelemetryRaw::class.java) ?: return
            val tags = payload.tags ?: return

            val processor = TelemetryFrameProcessor(tags)
            processor.processAll()

            updateAlerts(processor.candidateAlerts)
            _kpis.value = processor.kpiList
        } catch (e: Exception) {
            Log.e("PROD_SSE", "Parse error: ${e.message}", e)
        }
    }

    private fun updateAlerts(candidateAlerts: List<Pair<ProductionAlertEvent, Boolean>>) {
        val newlyRaised = mutableListOf<ProductionAlertEvent>()
        val now = System.currentTimeMillis()

        _activeProductionAlerts.update { currentList ->
            val map = currentList.associateBy { it.id }.toMutableMap()

            candidateAlerts.forEach { (incoming, shouldRaise) ->
                val id = incoming.id
                val existing = map[id]

                if (shouldRaise) {
                    if (existing == null) {
                        raisedAlertIds.add(id); map[id] = incoming; newlyRaised.add(incoming)
                    } else {
                        if (!existing.acknowledged) {
                            map[id] = existing.copy(currentValue = incoming.currentValue, thresholdValue = incoming.thresholdValue)
                        } else {
                            val ackTime = ackTimestamps[id] ?: 0L
                            if (now - ackTime >= REALERT_INTERVAL_MS) {
                                ackTimestamps.remove(id)
                                val re = incoming.copy(acknowledged = false, timestamp = now)
                                map[id] = re; newlyRaised.add(re)
                            } else {
                                map[id] = existing.copy(currentValue = incoming.currentValue, thresholdValue = incoming.thresholdValue)
                            }
                        }
                        raisedAlertIds.add(id)
                    }
                } else {
                    if (raisedAlertIds.remove(id)) map.remove(id)
                    ackTimestamps.remove(id)
                }
            }
            map.values.toList().sortedByDescending { it.timestamp }
        }

        newlyRaised.forEach { _productionAlerts.tryEmit(it) }
    }

    private fun shouldTriggerAlert(id: String, current: Float, threshold: Float, type: ThresholdType): Boolean {
        val buffer = threshold * 0.05f
        val raised = raisedAlertIds.contains(id)
        return when (type) {
            ThresholdType.ABOVE -> if (raised) current > (threshold - buffer) else current > threshold
            ThresholdType.BELOW -> if (raised) current < (threshold + buffer) else current < threshold
        }
    }

    private inner class TelemetryFrameProcessor(private val tags: Map<String, Double>) {
        val kpiList = mutableListOf<ProductionKpi>()
        val candidateAlerts = mutableListOf<Pair<ProductionAlertEvent, Boolean>>()

        private fun f(key: String): Float = tags[key]?.toFloat() ?: Float.NaN
        private fun fmt(v: Float, format: String) = if (v.isNaN()) "--" else String.format(format, v)
        private fun addKpi(k: ProductionKpi) = kpiList.add(k)

        private fun addAlert(id: String, stage: String, msg: String, prio: String, type: String, desc: String, cur: Float, thr: Float, tt: ThresholdType) {
            if (cur.isNaN()) return
            val raise = shouldTriggerAlert(id, cur, thr, tt)
            candidateAlerts.add(ProductionAlertEvent(id, stage, msg, prio, type, desc, cur, thr) to raise)
        }

        fun processAll() {
            processHeader(); processMilling(); processClarification()
            processEvaporation(); processConcentration(); processProduction()
        }

        private fun processHeader() {
            val kgHr = f("MillingThroughput_KG_HR")
            val tph = if (kgHr.isNaN()) Float.NaN else kgHr / 1000f
            val mtDay = if (tph.isNaN()) Float.NaN else tph * 24f
            val yieldEff = f("Yield_Efficiency_Percent")
            val steam = f("Blr_SteamPressure")
            val fceVac = f("FCE_Vaccum")
            val pans = listOf(f("OPan1_Status"), f("OPan2_Status"), f("OPan3_Status"), f("OPan4_Status")).count { !it.isNaN() && it >= 0.5f }

            val risk = when {
                !steam.isNaN() && steam < ThresholdManager.get("STEAM_PRESSURE_MIN") -> "High"
                !fceVac.isNaN() && fceVac < ThresholdManager.get("FCE_VACUUM_MIN") -> "Medium"
                pans < ThresholdManager.get("MIN_PANS_RUNNING").toInt() -> "Medium"
                else -> "Low"
            }

            addKpi(ProductionKpi("global_throughput", "Global Throughput", mtDay, fmt(mtDay, "%,.0f"), "MT/Day", "Header", "factory"))
            addKpi(ProductionKpi("target_achieved", "Target Achieved", yieldEff, fmt(yieldEff, "%.1f"), "%", "Header", "monitor_heart"))
            addKpi(ProductionKpi("overall_yield", "Overall Yield", yieldEff, fmt(yieldEff, "%.1f"), "%", "Header", "monitor_heart"))
            addKpi(ProductionKpi("bottleneck_risk", "Bottleneck Risk", Float.NaN, risk, "", "Header", "factory"))
        }

        private fun processMilling() {
            val stock = f("CaneStock")
            val kgHr = f("MillingThroughput_KG_HR")
            val tph = if (kgHr.isNaN()) Float.NaN else kgHr / 1000f
            val tMin = ThresholdManager.get("MILLING_TPH_MIN")

            addKpi(ProductionKpi("cane_stock", "Cane Stock", stock, fmt(stock, "%.1f"), "%", "Milling", "factory"))
            addKpi(ProductionKpi("milling_throughput", "Cane Crushed", tph, fmt(tph, "%.2f"), "T/hr", "Milling", "factory",
                status = if (!tph.isNaN() && tph < tMin) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = tMin, thresholdType = ThresholdType.BELOW))
            addAlert("MILLING_LOW", "Milling", "Low Throughput: ${fmt(tph, "%.2f")} T/hr", "WARNING", "FLOW", "Below $tMin T/hr", tph, tMin, ThresholdType.BELOW)
        }

        private fun processClarification() {
            val rawKgHr = f("RawJuiceFlow")
            val rawTph = if (rawKgHr.isNaN()) Float.NaN else rawKgHr / 1000f
            val rawTemp = f("RawJuiceFlow_Temp")
            val ph = f("Deficator_pH")
            val cj = f("Clear Juice Tank Level")

            val phMin = ThresholdManager.get("DEFECATOR_PH_MIN")
            val phMax = ThresholdManager.get("DEFECATOR_PH_MAX")
            val rawTempMax = ThresholdManager.get("RAW_JUICE_TEMP_MAX")

            addKpi(ProductionKpi("raw_juice_flow", "Raw Juice Flow", rawTph, fmt(rawTph, "%.2f"), "T/hr", "Clarification", "water_drop"))
            addKpi(ProductionKpi("raw_juice_temp", "Raw Juice Temp", rawTemp, fmt(rawTemp, "%.1f"), "°C", "Clarification", "thermostat",
                status = if (!rawTemp.isNaN() && rawTemp > rawTempMax) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = rawTempMax, thresholdType = ThresholdType.ABOVE))
            addKpi(ProductionKpi("defecator_ph", "Defecator pH", ph, fmt(ph, "%.2f"), "pH", "Clarification", "monitor_heart",
                status = if (!ph.isNaN() && (ph < phMin || ph > phMax)) KpiStatus.WARNING else KpiStatus.NORMAL))
            addKpi(ProductionKpi("clear_juice_tank_level", "Clear Juice Tank", cj, fmt(cj, "%.1f"), "%", "Clarification", "water_drop"))

            addAlert("PH_LOW", "Clarification", "Low pH: ${fmt(ph, "%.2f")}", "WARNING", "pH", "Below $phMin", ph, phMin, ThresholdType.BELOW)
            addAlert("PH_HIGH", "Clarification", "High pH: ${fmt(ph, "%.2f")}", "WARNING", "pH", "Above $phMax", ph, phMax, ThresholdType.ABOVE)
        }

        private fun processEvaporation() {
            val b1 = f("Evap_Body1_Temp")
            val b4 = f("Evap_Body4_Vaccum")
            val b5 = f("Evap_Body5_Vaccum")
            val b5b = f("Evap_Body5_Brix")

            val tempMax = ThresholdManager.get("EVAP_BODY1_TEMP_MAX")
            val vacMin = ThresholdManager.get("EVAP_VACUUM_MIN")
            val brixMin = ThresholdManager.get("EVAP_BODY5_BRIX_MIN")

            addKpi(ProductionKpi("evap_body1_temp", "Evap Body1 Temp", b1, fmt(b1, "%.1f"), "°C", "Evaporation", "thermostat",
                status = if (!b1.isNaN() && b1 > tempMax) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                threshold = tempMax, thresholdType = ThresholdType.ABOVE))
            addKpi(ProductionKpi("evap_b4_vac", "Evap Vacuum B4", b4, fmt(b4, "%.0f"), "mmHg", "Evaporation", "speed",
                status = if (!b4.isNaN() && b4 < vacMin) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = vacMin, thresholdType = ThresholdType.BELOW))
            addKpi(ProductionKpi("evap_b5_vac", "Evap Vacuum B5", b5, fmt(b5, "%.0f"), "mmHg", "Evaporation", "speed",
                status = if (!b5.isNaN() && b5 < vacMin) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = vacMin, thresholdType = ThresholdType.BELOW))
            addKpi(ProductionKpi("evap_b5_brix", "Evap Body5 Brix", b5b, fmt(b5b, "%.1f"), "Bx", "Evaporation", "monitor_heart",
                status = if (!b5b.isNaN() && b5b < brixMin) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = brixMin, thresholdType = ThresholdType.BELOW))

            addAlert("EVAP_TEMP_HIGH", "Evaporation", "Evap Overtemp: ${fmt(b1, "%.1f")}°C", "CRITICAL", "TEMP", "Above $tempMax°C", b1, tempMax, ThresholdType.ABOVE)
        }

        private fun processConcentration() {
            val fceBrix = f("FCE_Brix")
            val fceVac = f("FCE_Vaccum")
            val vacMin = ThresholdManager.get("FCE_VACUUM_MIN")
            val brixMin = ThresholdManager.get("FCE_BRIX_MIN")
            val brixMax = ThresholdManager.get("FCE_BRIX_MAX")
            val panTempMax = ThresholdManager.get("OPAN_TEMP_MAX")

            addKpi(ProductionKpi("fce_brix", "FCE Brix", fceBrix, fmt(fceBrix, "%.1f"), "Bx", "Concentration", "monitor_heart",
                status = if (!fceBrix.isNaN() && (fceBrix < brixMin || fceBrix > brixMax)) KpiStatus.WARNING else KpiStatus.NORMAL))
            addKpi(ProductionKpi("fce_vacuum", "FCE Vacuum", fceVac, fmt(fceVac, "%.0f"), "mmHg", "Concentration", "speed",
                status = if (!fceVac.isNaN() && fceVac < vacMin) KpiStatus.CRITICAL else KpiStatus.NORMAL,
                threshold = vacMin, thresholdType = ThresholdType.BELOW))
            addAlert("FCE_VAC_DROP", "Concentration", "FCE Vacuum Low: ${fmt(fceVac, "%.0f")} mmHg", "CRITICAL", "VACUUM", "Below $vacMin", fceVac, vacMin, ThresholdType.BELOW)

            val statuses = listOf(f("OPan1_Status"), f("OPan2_Status"), f("OPan3_Status"), f("OPan4_Status"))
            val running = statuses.count { !it.isNaN() && it >= 0.5f }
            val temps = listOf(f("OPan1_Temp"), f("OPan2_Temp"), f("OPan3_Temp"), f("OPan4_Temp")).filter { !it.isNaN() }
            val avgTemp = if (temps.isEmpty()) Float.NaN else temps.average().toFloat()

            addKpi(ProductionKpi("opan_running", "Open Pans Running", running.toFloat(), running.toString(), "Nos", "Crystallization", "factory"))
            addKpi(ProductionKpi("opan_avg_temp", "Open Pan Avg Temp", avgTemp, fmt(avgTemp, "%.1f"), "°C", "Crystallization", "thermostat",
                status = if (!avgTemp.isNaN() && avgTemp > panTempMax) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = panTempMax, thresholdType = ThresholdType.ABOVE))
        }

        private fun processProduction() {
            val total = f("Total_Jaggery_Produced_KG")
            val batches = f("Total_Batches_Completed")
            val yieldEff = f("Yield_Efficiency_Percent")
            val shift = f("Current_Shift")
            val maint = f("In_Maintenance")
            val yieldMin = ThresholdManager.get("YIELD_EFFICIENCY_MIN")

            addKpi(ProductionKpi("total_produced", "Total Produced", total, fmt(total, "%,.0f"), "kg", "Production", "factory"))
            addKpi(ProductionKpi("batches_completed", "Batches", batches, fmt(batches, "%,.0f"), "", "Production", "factory"))
            addKpi(ProductionKpi("yield_efficiency", "Yield Efficiency", yieldEff, fmt(yieldEff, "%.1f"), "%", "Production", "monitor_heart",
                status = if (!yieldEff.isNaN() && yieldEff < yieldMin) KpiStatus.WARNING else KpiStatus.NORMAL,
                threshold = yieldMin, thresholdType = ThresholdType.BELOW))
            addKpi(ProductionKpi("shift", "Current Shift", shift, if (shift.isNaN()) "--" else shift.toInt().toString(), "", "Production", "factory"))
            addKpi(ProductionKpi("maintenance", "In Maintenance", maint,
                if (maint.isNaN()) "--" else if (maint >= 0.5f) "Yes" else "No", "", "Production", "factory"))

            addAlert("YIELD_LOW", "Production", "Low Yield: ${fmt(yieldEff, "%.1f")}%", "WARNING", "YIELD", "Below $yieldMin%", yieldEff, yieldMin, ThresholdType.BELOW)
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
        reconnectJob?.cancel()
        sseClient.dispatcher.executorService.shutdown()
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ProductionViewModel() as T
        }
    }
}