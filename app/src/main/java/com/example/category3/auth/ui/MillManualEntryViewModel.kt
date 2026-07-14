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

// ─────────────────────────────────────────────────────────────────────────────
// ISOLATED DATA MODELS (Prefixed with MillManual to prevent clashes)
// ─────────────────────────────────────────────────────────────────────────────

enum class MillManualEquipStatus { RUNNING, STANDBY, FAULT, OFFLINE }

data class MillManualMotorData(
    val name: String = "",
    val healthValue: String = "",
    val statusText: String = ""
)

data class MillManualDashboardState(
    val motors: List<MillManualMotorData> = emptyList()
)

data class MillManualRawJuiceData(
    val volumeFlowM3hr: Float = 0f,
    val pumpStatus: MillManualEquipStatus = MillManualEquipStatus.STANDBY
)

data class MillManualLiveState(
    val dashboard: MillManualDashboardState,
    val rawJuice: MillManualRawJuiceData,
    val rjPumpFault: Boolean,
    val connectionStatus: String,
    val lastUpdated: Long
)

// Re-added the missing Raw Payload Class for SSE Parsing
data class MillManualSsePayload(
    val timestamp: Double? = null,
    val date: String? = null,
    val tags: Map<String, String>? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

class MillManualEntryViewModel : ViewModel() {

    companion object {
        private const val TAG = "MILL_MANUAL_SSE"
        private const val SSE_URL = "https://warner-regional-gay-donors.trycloudflare.com/stream"
        private const val RECONNECT_DELAY_MS = 5_000L

        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MillManualEntryViewModel() as T
        }
    }

    private val _state = MutableStateFlow(
        MillManualLiveState(
            dashboard = MillManualDashboardState(
                motors = listOf(
                    MillManualMotorData("Mill 01", "0.0 A", "Stable · 0 RPM"),
                    MillManualMotorData("Mill 02", "0.0 A", "Stable · 0 RPM"),
                    MillManualMotorData("Mill 03", "0.0 A", "Stable · 0 RPM")
                )
            ),
            rawJuice = MillManualRawJuiceData(
                volumeFlowM3hr = 0.0f,
                pumpStatus = MillManualEquipStatus.STANDBY
            ),
            rjPumpFault = false,
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
                    Log.d(TAG, "SSE connected for Manual Entry")
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

            val payload = gson.fromJson(json, MillManualSsePayload::class.java) ?: return
            val tags = payload.tags ?: return

            fun tag(key: String): Float = tags[key]?.toFloatOrNull() ?: 0f

            val m1Amps = String.format("%.1f", tag("MotorMill1_A"))
            val m1Rpm = tag("MotorMill1_RPM").toInt()

            val m2Amps = String.format("%.1f", tag("MotorMill2_A"))
            val m2Rpm = tag("MotorMill2_RPM").toInt()

            val m3Amps = String.format("%.1f", tag("MotorMill3_A"))
            val m3Rpm = tag("MotorMill3_RPM").toInt()

            val liveMotors = listOf(
                MillManualMotorData("Mill 01", "$m1Amps A", "Stable · $m1Rpm RPM"),
                MillManualMotorData("Mill 02", "$m2Amps A", "Stable · $m2Rpm RPM"),
                MillManualMotorData("Mill 03", "$m3Amps A", "Stable · $m3Rpm RPM")
            )

            val flowM3 = tag("RawJuiceFlow_Volume")
            val pumpAmps = tag("RJTank_Pump(Active)_amps")
            val isPumpRunning = flowM3 > 0.5f || pumpAmps > 0.005f
            val isPumpFault = false

            _state.update { currentState ->
                currentState.copy(
                    dashboard = MillManualDashboardState(motors = liveMotors),
                    rawJuice = MillManualRawJuiceData(
                        volumeFlowM3hr = flowM3,
                        pumpStatus = if (isPumpRunning && !isPumpFault) MillManualEquipStatus.RUNNING else MillManualEquipStatus.STANDBY
                    ),
                    rjPumpFault = isPumpFault,
                    connectionStatus = "CONNECTED",
                    lastUpdated = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
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