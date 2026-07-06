package com.example.category3.auth.ui

import com.google.gson.annotations.SerializedName

// ============================================================================
// SHARED SSE DATA MODEL
// ============================================================================

data class IndustrialTelemetryRaw(
    @SerializedName("timestamp") val timestamp: Double = 0.0,
    @SerializedName("date") val date: String = "",
    @SerializedName("tags") val tags: Map<String, Double>? = null
)