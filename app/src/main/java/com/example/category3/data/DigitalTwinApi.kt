package com.example.category3.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

// --- API Models ---
data class ApiPlant(
    @SerializedName("plant_status") val plantStatus: String = "UNKNOWN",
    @SerializedName("global_oee") val globalOee: Double = 0.0,
    @SerializedName("total_throughput") val totalThroughput: Int = 0,
    @SerializedName("total_energy_kw") val totalEnergyKw: Float = 0f
)

data class ApiEquipment(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("status") val status: String = "OFFLINE",
    @SerializedName("temp_c") val tempC: Float? = 30f,
    @SerializedName("vibration_hz") val vibrationHz: Float? = 0f,
    @SerializedName("pressure_bar") val pressureBar: Float? = 1.0f,
    @SerializedName("flow_rate") val flowRate: Float? = 0f,
    @SerializedName("power_kw") val powerKw: Float? = 0f,
    @SerializedName("downtime_mins") val downtimeMins: Int? = 0
)

data class ApiAlarm(
    @SerializedName("id") val id: String = "",
    @SerializedName("equipment_id") val equipmentId: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("severity") val severity: String = "INFO", // "CRITICAL", "WARNING", "INFO"
    @SerializedName("details") val details: String? = ""
)

// --- Retrofit Interface ---
interface DigitalTwinApi {
    @GET("plant")
    suspend fun getPlantStatus(): ApiPlant

    @GET("equipment")
    suspend fun getAllEquipment(): List<ApiEquipment>

    @GET("alarms")
    suspend fun getActiveAlarms(): List<ApiAlarm>

    @GET("graph/impact/{equip_id}")
    suspend fun getDownstreamImpact(@Path("equip_id") equipId: String): Any
}