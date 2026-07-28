package com.example.model

import androidx.compose.ui.graphics.Color

enum class WifiBand(val displayName: String, val shortName: String) {
    BAND_2_4GHZ("2.4 GHz", "2.4G"),
    BAND_5GHZ("5 GHz", "5G"),
    BAND_6GHZ("6 GHz", "6G")
}

enum class WifiStandard(val label: String, val techCode: String) {
    WIFI_7("Wi-Fi 7", "802.11be"),
    WIFI_6E("Wi-Fi 6E", "802.11ax"),
    WIFI_6("Wi-Fi 6", "802.11ax"),
    WIFI_5("Wi-Fi 5", "802.11ac"),
    WIFI_4("Wi-Fi 4", "802.11n"),
    LEGACY("Legacy", "802.11a/b/g")
}

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int, // Signal strength in dBm (-100 to -30)
    val frequencyMhz: Int,
    val channel: Int,
    val channelWidth: String = "20 MHz",
    val band: WifiBand,
    val standard: WifiStandard,
    val capabilities: String,
    val securityType: String,
    val isConnected: Boolean = false,
    val linkSpeedMbps: Int = 0,
    val rxLinkSpeedMbps: Int = 0,
    val txLinkSpeedMbps: Int = 0,
    val ipAddress: String? = null,
    val colorHex: Long = 0xFF0EA5E9
)

data class ChannelRating(
    val band: WifiBand,
    val channel: Int,
    val frequencyMhz: Int,
    val ratingStars: Float, // 0.0 to 5.0
    val coChannelCount: Int,
    val adjacentChannelCount: Int,
    val overlappingSsids: List<String> = emptyList(),
    val isRecommended: Boolean = false
)

data class SignalHistoryPoint(
    val timestampMs: Long,
    val rssi: Int,
    val ssid: String
)

data class ConnectedInfo(
    val ssid: String = "Not Connected",
    val bssid: String = "--:--:--:--:--:--",
    val rssi: Int = -100,
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val ipAddress: String = "0.0.0.0",
    val gatewayIp: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val dns1: String = "8.8.8.8",
    val dns2: String = "8.8.4.4",
    val macAddress: String = "02:00:00:00:00:00",
    val wifiStandard: WifiStandard = WifiStandard.WIFI_5,
    val security: String = "WPA2-PSK",
    val band: WifiBand = WifiBand.BAND_2_4GHZ,
    val channel: Int = 1
)

data class PingResult(
    val host: String,
    val isSuccessful: Boolean,
    val latencyMs: Long,
    val timestampMs: Long = System.currentTimeMillis()
)
