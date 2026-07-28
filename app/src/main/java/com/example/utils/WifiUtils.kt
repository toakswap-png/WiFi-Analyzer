package com.example.utils

import com.example.model.ChannelRating
import com.example.model.WifiBand
import com.example.model.WifiNetwork
import com.example.model.WifiStandard
import com.example.ui.theme.SpectrumColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.net.InetAddress
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object WifiUtils {

    fun getFrequencyBand(frequencyMhz: Int): WifiBand {
        return when {
            frequencyMhz in 2400..2500 -> WifiBand.BAND_2_4GHZ
            frequencyMhz in 4900..5899 -> WifiBand.BAND_5GHZ
            frequencyMhz in 5925..7125 -> WifiBand.BAND_6GHZ
            else -> WifiBand.BAND_2_4GHZ
        }
    }

    fun convertFrequencyToChannel(frequencyMhz: Int): Int {
        return when {
            frequencyMhz == 2484 -> 14
            frequencyMhz in 2412..2472 -> (frequencyMhz - 2407) / 5
            frequencyMhz in 5170..5825 -> (frequencyMhz - 5000) / 5
            frequencyMhz in 5955..7115 -> (frequencyMhz - 5950) / 5
            else -> 1
        }
    }

    fun parseSecurityType(capabilities: String, standard: WifiStandard? = null, frequencyMhz: Int = 0): String {
        val caps = capabilities.uppercase()
        val is6GhzOrAbove = frequencyMhz in 5925..7125 || standard == WifiStandard.WIFI_6E || standard == WifiStandard.WIFI_7

        return when {
            // Enhanced Open (OWE / OWE Transition)
            caps.contains("OWE") || caps.contains("ENHANCED OPEN") -> "Enhanced Open (OWE)"

            // WPA3 Personal (SAE)
            caps.contains("SAE") || caps.contains("WPA3-SAE") -> "WPA3-Personal (SAE)"
            caps.contains("WPA3") && !caps.contains("EAP") -> "WPA3-Personal (SAE)"

            // WPA3 Enterprise
            caps.contains("WPA3") && caps.contains("EAP") || caps.contains("SUITE-B") || caps.contains("EAP-SHA256") || caps.contains("EAP-SHA384") -> "WPA3-Enterprise"

            // WPA2 Enterprise
            caps.contains("WPA2") && caps.contains("EAP") || caps.contains("RSN-EAP") -> "WPA2-Enterprise"

            // WPA2 Personal
            caps.contains("WPA2") || caps.contains("RSN-PSK") || caps.contains("RSN") -> "WPA2-PSK"

            // WPA Legacy
            caps.contains("WPA") && caps.contains("PSK") -> "WPA-PSK"
            caps.contains("WEP") -> "WEP (Insecure)"

            // True Open or missing flags
            else -> {
                if (is6GhzOrAbove) {
                    "Enhanced Open (OWE)"
                } else if (caps.contains("ESS") || caps.isEmpty()) {
                    "Open (None)"
                } else {
                    "WPA2/WPA3"
                }
            }
        }
    }

    fun calculateSubnetMask(ipAddress: String): String {
        if (ipAddress.isEmpty() || ipAddress == "0.0.0.0") return "255.255.255.0"
        val parts = ipAddress.split(".")
        if (parts.size == 4) {
            val firstOctet = parts[0].toIntOrNull() ?: 192
            return when {
                firstOctet in 1..127 -> "255.0.0.0"
                firstOctet in 128..191 -> "255.255.0.0"
                else -> "255.255.255.0"
            }
        }
        return "255.255.255.0"
    }

    fun parseWifiStandard(standardInt: Int, frequencyMhz: Int, capabilities: String): WifiStandard {
        val caps = capabilities.uppercase()
        val band = getFrequencyBand(frequencyMhz)

        return when {
            standardInt == 8 || caps.contains("EHT") || caps.contains("11BE") -> WifiStandard.WIFI_7
            (standardInt == 6 || caps.contains("HE") || caps.contains("11AX")) && band == WifiBand.BAND_6GHZ -> WifiStandard.WIFI_6E
            standardInt == 6 || caps.contains("HE") || caps.contains("11AX") -> WifiStandard.WIFI_6
            standardInt == 5 || caps.contains("VHT") || caps.contains("11AC") -> WifiStandard.WIFI_5
            standardInt == 4 || caps.contains("HT") || caps.contains("11N") -> WifiStandard.WIFI_4
            band == WifiBand.BAND_6GHZ -> WifiStandard.WIFI_6E
            band == WifiBand.BAND_5GHZ -> WifiStandard.WIFI_5
            else -> WifiStandard.WIFI_4
        }
    }

    fun calculateSignalPercentage(rssi: Int): Int {
        // Mapping -100 dBm (0%) to -30 dBm (100%)
        val clamped = max(-100, min(-30, rssi))
        return ((clamped + 100) * 100) / 70
    }

    fun getSignalQualityLabel(rssi: Int): String {
        return when {
            rssi >= -50 -> "Excellent"
            rssi >= -65 -> "Good"
            rssi >= -75 -> "Fair"
            rssi >= -85 -> "Weak"
            else -> "Very Poor"
        }
    }

    fun getSignalColor(rssi: Int): Long {
        return when {
            rssi > -55 -> 0xFF4ADE80  // Light Green (> -55 dBm)
            rssi >= -65 -> 0xFFA3E635 // Yellow-Green (-55 to -65 dBm)
            rssi >= -74 -> 0xFFFACC15 // Yellow (-65 to -74 dBm)
            rssi >= -83 -> 0xFFFB923C // Orange (-74 to -83 dBm)
            else -> 0xFFF87171        // Red (< -83 dBm)
        }
    }

    fun getComposeSignalColor(rssi: Int): Color {
        return Color(getSignalColor(rssi))
    }

    fun getColorForIndex(index: Int): Long {
        val color = SpectrumColors[abs(index) % SpectrumColors.size]
        return (color.toArgb().toLong() and 0xFFFFFFFFL)
    }

    fun getColorForBssid(bssid: String, fallbackIndex: Int = 0): Long {
        val hash = abs(bssid.hashCode())
        val color = SpectrumColors[(hash + fallbackIndex) % SpectrumColors.size]
        return (color.toArgb().toLong() and 0xFFFFFFFFL)
    }

    fun getComposeColorForIndex(index: Int): Color {
        return SpectrumColors[abs(index) % SpectrumColors.size]
    }

    fun calculateChannelRatings(band: WifiBand, networks: List<WifiNetwork>): List<ChannelRating> {
        val bandNetworks = networks.filter { it.band == band }
        val channelList = when (band) {
            WifiBand.BAND_2_4GHZ -> listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            WifiBand.BAND_5GHZ -> listOf(36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 149, 153, 157, 161, 165)
            WifiBand.BAND_6GHZ -> listOf(1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93)
        }

        val primaryChannels2_4G = listOf(1, 6, 11)

        return channelList.map { ch ->
            val coChannelNets = bandNetworks.filter { it.channel == ch }
            val adjChannelNets = bandNetworks.filter { abs(it.channel - ch) == 1 }

            val coChannelCount = coChannelNets.size
            val adjacentCount = adjChannelNets.size

            // Penalty formula based on AP count and RSSI strength
            var penalty = 0.0f
            coChannelNets.forEach { net ->
                val weight = (100 + net.rssi).toFloat() / 70.0f // stronger signal = higher interference penalty
                penalty += 1.2f * weight
            }
            adjChannelNets.forEach { net ->
                val weight = (100 + net.rssi).toFloat() / 70.0f
                penalty += 0.8f * weight
            }

            var rating = max(0.0f, min(5.0f, 5.0f - penalty))

            // Extra preference for non-overlapping channels in 2.4GHz
            val isRec = if (band == WifiBand.BAND_2_4GHZ) {
                primaryChannels2_4G.contains(ch) && rating >= 3.0f
            } else {
                rating >= 3.5f
            }

            val overlappingSsids = (coChannelNets + adjChannelNets).map { it.ssid }.distinct()

            val freq = when (band) {
                WifiBand.BAND_2_4GHZ -> if (ch == 14) 2484 else 2407 + (ch * 5)
                WifiBand.BAND_5GHZ -> 5000 + (ch * 5)
                WifiBand.BAND_6GHZ -> 5950 + (ch * 5)
            }

            ChannelRating(
                band = band,
                channel = ch,
                frequencyMhz = freq,
                ratingStars = (Math.round(rating * 10) / 10.0f),
                coChannelCount = coChannelCount,
                adjacentChannelCount = adjacentCount,
                overlappingSsids = overlappingSsids,
                isRecommended = isRec
            )
        }
    }

    fun formatIpAddress(ipInt: Int): String {
        return try {
            val bytes = byteArrayOf(
                (ipInt and 0xFF).toByte(),
                (ipInt shr 8 and 0xFF).toByte(),
                (ipInt shr 16 and 0xFF).toByte(),
                (ipInt shr 24 and 0xFF).toByte()
            )
            InetAddress.getByAddress(bytes).hostAddress ?: "192.168.1.100"
        } catch (e: Exception) {
            "192.168.1.100"
        }
    }
}
