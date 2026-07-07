package com.example.data

data class BmsDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnected: Boolean = false
)
