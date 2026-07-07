package com.example.data

data class TelemetryData(
    val voltage: Float = 0.0f,
    val current: Float = 0.0f,
    val temperature: Float = 0.0f,
    val power: Float = 0.0f,
    val chargePercentage: Int = 0,
    val avgCellVoltage: Float = 0.0f,
    val maxTemp: Float = 0.0f,
    val limitCurrent: Float = 0.0f,
    val consPower: Float = 0.0f
)
