package com.example.category3.data // 💡 Make sure this matches your project package folder structure

/**
 * Represents a discrete maintenance task assigned to a field engineer
 * for a specific machine asset on the plant floor.
 */
data class MaintenanceJobCard(
    val id: String,
    val title: String,
    val assetTag: String,
    val criticalLevel: String,      // CRITICAL, HIGH, ROUTINE
    val instructions: String,      // Steps for the operator to follow
    val initialStatus: String      // PENDING, IN_PROGRESS, COMPLETED
)