package com.example.category3.data

// ============================================================================
// PLC AUTOMATION REGISTER & MANUAL OVERRIDE JOURNAL STATE
// ============================================================================
data class MillTelemetryState(
    // Automatic SCADA/PLC Sensor Ingestions
    val juiceFlow: String = "0.0",          // Tag Ref: FIT_101_JUICE
    val caneCarrierRpm: String = "0.0",     // Tag Ref: SIT_102_CARRIER
    val rake1Rpm: String = "0.0",           // Tag Ref: SIT_103_RAKE1
    val rake2Rpm: String = "0.0",           // Tag Ref: SIT_104_RAKE2

    // Conduit Drive 01 Core Matrix Block
    val m1Rpm: String = "0",
    val m1Amps: String = "0",
    val m1Pressure: String = "0.0",

    // Conduit Drive 02 Core Matrix Block
    val m2Rpm: String = "0",
    val m2Amps: String = "0",
    val m2Pressure: String = "0.0",

    // Conduit Drive 03 Core Matrix Block
    val m3Rpm: String = "0",
    val m3Amps: String = "0",
    val m3Pressure: String = "0.0",

    // Hardware Interlock Coil Status Loops (True = Nominal / False = Tripped Fault)
    val pump1Status: Boolean = true,        // Coil: DI_201_P1
    val pump2Status: Boolean = true,        // Coil: DI_202_P2
    val vibroscreenStatus: Boolean = true,   // Coil: DI_203_VIB
    val rotaryRunning: Boolean = true,       // Coil: DI_204_RTR_RUN
    val rotaryStatus: Boolean = true,        // Coil: DI_205_RTR_STA

    // Operator Manual Annotations
    val remarks: String = "",
    val isSubmitted: Boolean = false
) {
    // Dynamic calculation evaluating if any core hardware bus loop has tripped
    val requiresMaintenance: Boolean
        get() = !pump1Status || !pump2Status || !vibroscreenStatus || !rotaryRunning || !rotaryStatus
}