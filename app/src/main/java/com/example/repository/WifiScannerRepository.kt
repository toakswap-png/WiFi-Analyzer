package com.example.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.model.ConnectedInfo
import com.example.model.WifiBand
import com.example.model.WifiNetwork
import com.example.model.WifiStandard
import com.example.ui.theme.SpectrumColors
import com.example.utils.WifiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.InetAddress
import kotlin.random.Random

class WifiScannerRepository(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode = _isSimulationMode.asStateFlow()

    private var simulatedNetworks = mutableListOf<WifiNetwork>()
    private var lastScannedList: List<WifiNetwork> = emptyList()

    init {
        initSimulatedDataset()
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
    }

    fun isWifiEnabled(): Boolean {
        if (_isSimulationMode.value) return true
        return wifiManager?.isWifiEnabled == true
    }

    private fun initSimulatedDataset() {
        simulatedNetworks = mutableListOf(
            WifiNetwork(
                ssid = "MyHome_Fiber",
                bssid = "00:22:B0:77:7F:6A",
                rssi = -48,
                frequencyMhz = 5745,
                channel = 149,
                channelWidth = "80 MHz",
                band = WifiBand.BAND_5GHZ,
                standard = WifiStandard.WIFI_6,
                capabilities = "[WPA3-SAE-CCMP][WPA2-PSK-CCMP][ESS][WPS]",
                securityType = "WPA3-Personal (SAE)",
                isConnected = true,
                linkSpeedMbps = 1200,
                rxLinkSpeedMbps = 1200,
                txLinkSpeedMbps = 1080,
                ipAddress = "192.168.1.104",
                colorHex = 0xFF4ADE80
            ),
            WifiNetwork(
                ssid = "MyHome_Fiber",
                bssid = "00:22:B0:77:7F:6B",
                rssi = -52,
                frequencyMhz = 2462,
                channel = 11,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.WIFI_4,
                capabilities = "[WPA2-PSK-CCMP][ESS]",
                securityType = "WPA2-PSK",
                isConnected = false,
                linkSpeedMbps = 300,
                colorHex = 0xFF38BDF8
            ),
            WifiNetwork(
                ssid = "Netcore_GigaRouter",
                bssid = "08:11:76:27:26:3C",
                rssi = -58,
                frequencyMhz = 2437,
                channel = 6,
                channelWidth = "40 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.WIFI_4,
                capabilities = "[WPA2-PSK-CCMP][WPA-PSK-CCMP][ESS]",
                securityType = "WPA2-PSK",
                isConnected = false,
                colorHex = 0xFF60A5FA
            ),
            WifiNetwork(
                ssid = "TP-LINK_44C950",
                bssid = "40:16:9F:4C:95:00",
                rssi = -60,
                frequencyMhz = 2437,
                channel = 6,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.WIFI_4,
                capabilities = "[WPA2-PSK-CCMP][WPS][ESS]",
                securityType = "WPA2-PSK",
                isConnected = false,
                colorHex = 0xFFFACC15
            ),
            WifiNetwork(
                ssid = "Tenda_597638",
                bssid = "C8:3A:35:59:76:38",
                rssi = -79,
                frequencyMhz = 2427,
                channel = 4,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.LEGACY,
                capabilities = "[WPA-PSK-CCMP][ESS]",
                securityType = "WPA-PSK",
                isConnected = false,
                colorHex = 0xFFFB923C
            ),
            WifiNetwork(
                ssid = "TP-LINK_5F476C",
                bssid = "E4:83:45:5F:47:6C",
                rssi = -81,
                frequencyMhz = 2412,
                channel = 1,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.WIFI_4,
                capabilities = "[WPA2-PSK-CCMP][WPS][ESS]",
                securityType = "WPA2-PSK",
                isConnected = false,
                colorHex = 0xFFF87171
            ),
            WifiNetwork(
                ssid = "Airtel_Fiber_Ultra",
                bssid = "AC:12:34:56:78:90",
                rssi = -42,
                frequencyMhz = 6105,
                channel = 33,
                channelWidth = "160 MHz",
                band = WifiBand.BAND_6GHZ,
                standard = WifiStandard.WIFI_6E,
                capabilities = "[WPA3-SAE][ESS]",
                securityType = "WPA3-Personal (SAE)",
                isConnected = false,
                linkSpeedMbps = 2400,
                colorHex = 0xFFA78BFA
            ),
            WifiNetwork(
                ssid = "Airtel_Fiber_Ultra",
                bssid = "AC:12:34:56:78:91",
                rssi = -46,
                frequencyMhz = 5220,
                channel = 44,
                channelWidth = "80 MHz",
                band = WifiBand.BAND_5GHZ,
                standard = WifiStandard.WIFI_6,
                capabilities = "[WPA3-SAE][WPA2-PSK][ESS]",
                securityType = "WPA3-Personal (SAE)",
                isConnected = false,
                linkSpeedMbps = 1200,
                colorHex = 0xFF818CF8
            ),
            WifiNetwork(
                ssid = "Airtel_Fiber_Ultra",
                bssid = "AC:12:34:56:78:92",
                rssi = -50,
                frequencyMhz = 2437,
                channel = 6,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.WIFI_4,
                capabilities = "[WPA2-PSK][ESS]",
                securityType = "WPA2-PSK",
                isConnected = false,
                linkSpeedMbps = 300,
                colorHex = 0xFF34D399
            ),
            WifiNetwork(
                ssid = "Asus_ROG_Gaming_7",
                bssid = "F4:2A:7D:11:22:33",
                rssi = -38,
                frequencyMhz = 6425,
                channel = 97,
                channelWidth = "320 MHz",
                band = WifiBand.BAND_6GHZ,
                standard = WifiStandard.WIFI_7,
                capabilities = "[WPA3-SAE-CCMP][WPA3-EAP][ESS]",
                securityType = "Wi-Fi 7 Ultra-Security",
                isConnected = false,
                linkSpeedMbps = 4800,
                colorHex = 0xFFF472B6
            ),
            WifiNetwork(
                ssid = "Office_Corporate_AP1",
                bssid = "34:20:E3:88:99:AA",
                rssi = -64,
                frequencyMhz = 5180,
                channel = 36,
                channelWidth = "80 MHz",
                band = WifiBand.BAND_5GHZ,
                standard = WifiStandard.WIFI_5,
                capabilities = "[WPA2-EAP-CCMP][ESS]",
                securityType = "WPA2-Enterprise",
                isConnected = false,
                colorHex = 0xFF818CF8
            ),
            WifiNetwork(
                ssid = "Guest_Free_WiFi",
                bssid = "12:34:56:78:90:AB",
                rssi = -86,
                frequencyMhz = 2437,
                channel = 6,
                channelWidth = "20 MHz",
                band = WifiBand.BAND_2_4GHZ,
                standard = WifiStandard.LEGACY,
                capabilities = "[ESS]",
                securityType = "Open (None)",
                isConnected = false,
                colorHex = 0xFF34D399
            )
        )
    }

    suspend fun performScan(): List<WifiNetwork> = withContext(Dispatchers.IO) {
        if (_isSimulationMode.value) {
            return@withContext getSimulatedScanResults()
        }

        try {
            // Request real system Wi-Fi scan
            try { wifiManager?.startScan() } catch (e: Exception) { Log.e("WifiScannerRepository", "startScan error", e) }

            @Suppress("DEPRECATION")
            val scanResults = try { wifiManager?.scanResults } catch (e: Exception) { null }

            val connectedWifiInfo = try { wifiManager?.connectionInfo } catch (e: Exception) { null }
            val connectedBssid = connectedWifiInfo?.bssid
            val connectedSsid = connectedWifiInfo?.ssid?.replace("\"", "")?.takeIf { it.isNotEmpty() && it != "<unknown ssid>" }

            val realNetworks = mutableListOf<WifiNetwork>()

            if (!scanResults.isNullOrEmpty()) {
                scanResults.forEachIndexed { index, result ->
                    val freq = result.frequency
                    val band = WifiUtils.getFrequencyBand(freq)
                    val channel = WifiUtils.convertFrequencyToChannel(freq)
                    val isConnectedNet = connectedBssid != null && connectedBssid.equals(result.BSSID, ignoreCase = true)
                    val std = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WifiUtils.parseWifiStandard(result.wifiStandard, freq, result.capabilities)
                    } else {
                        WifiUtils.parseWifiStandard(0, freq, result.capabilities)
                    }

                    realNetworks.add(
                        WifiNetwork(
                            ssid = if (result.SSID.isNullOrEmpty()) "<Hidden Network>" else result.SSID,
                            bssid = result.BSSID ?: "00:00:00:00:00:00",
                            rssi = result.level,
                            frequencyMhz = freq,
                            channel = channel,
                            channelWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                when (result.channelWidth) {
                                    ScanResultWidth.WIDTH_40 -> "40 MHz"
                                    ScanResultWidth.WIDTH_80 -> "80 MHz"
                                    ScanResultWidth.WIDTH_160 -> "160 MHz"
                                    ScanResultWidth.WIDTH_80_80 -> "80+80 MHz"
                                    ScanResultWidth.WIDTH_320 -> "320 MHz"
                                    else -> "20 MHz"
                                }
                            } else "20 MHz",
                            band = band,
                            standard = std,
                            capabilities = result.capabilities ?: "",
                            securityType = WifiUtils.parseSecurityType(result.capabilities ?: "", std, freq),
                            isConnected = isConnectedNet,
                            linkSpeedMbps = if (isConnectedNet) connectedWifiInfo?.linkSpeed ?: 0 else 0,
                            rxLinkSpeedMbps = if (isConnectedNet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) connectedWifiInfo?.rxLinkSpeedMbps ?: 0 else 0,
                            txLinkSpeedMbps = if (isConnectedNet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) connectedWifiInfo?.txLinkSpeedMbps ?: 0 else 0,
                            ipAddress = if (isConnectedNet) WifiUtils.formatIpAddress(connectedWifiInfo?.ipAddress ?: 0) else null,
                            colorHex = WifiUtils.getColorForBssid(result.BSSID ?: "", index)
                        )
                    )
                }
            }

            // Include current active connected Wi-Fi network if not already listed in scan results
            if (connectedSsid != null && connectedWifiInfo != null) {
                val alreadyAdded = realNetworks.any { it.bssid.equals(connectedBssid, ignoreCase = true) || it.ssid == connectedSsid }
                if (!alreadyAdded) {
                    val freq = connectedWifiInfo.frequency.coerceAtLeast(2412)
                    val band = WifiUtils.getFrequencyBand(freq)
                    val channel = WifiUtils.convertFrequencyToChannel(freq)
                    val ipStr = WifiUtils.formatIpAddress(connectedWifiInfo.ipAddress)

                    realNetworks.add(
                        0,
                        WifiNetwork(
                            ssid = connectedSsid,
                            bssid = connectedWifiInfo.bssid ?: "02:00:00:00:00:00",
                            rssi = connectedWifiInfo.rssi,
                            frequencyMhz = freq,
                            channel = channel,
                            channelWidth = "20/40 MHz",
                            band = band,
                            standard = WifiUtils.parseWifiStandard(0, freq, ""),
                            capabilities = "[CONNECTED]",
                            securityType = WifiUtils.parseSecurityType("", WifiUtils.parseWifiStandard(0, freq, ""), freq),
                            isConnected = true,
                            linkSpeedMbps = connectedWifiInfo.linkSpeed,
                            ipAddress = ipStr,
                            colorHex = WifiUtils.getColorForIndex(0)
                        )
                    )
                }
            }

            val sorted = realNetworks.sortedByDescending { it.rssi }
            lastScannedList = sorted
            return@withContext sorted
        } catch (e: Exception) {
            Log.e("WifiScannerRepository", "Error scanning real Wi-Fi: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    private fun getSimulatedScanResults(): List<WifiNetwork> {
        // Apply jitter fluctuation to simulated networks for live real-time feel
        return simulatedNetworks.mapIndexed { index, net ->
            val jitter = Random.nextInt(-2, 3)
            val updatedRssi = (net.rssi + jitter).coerceIn(-100, -30)
            net.copy(
                rssi = updatedRssi,
                colorHex = WifiUtils.getColorForIndex(index)
            )
        }.sortedByDescending { it.rssi }
    }

    suspend fun getConnectedInfo(): ConnectedInfo = withContext(Dispatchers.IO) {
        if (_isSimulationMode.value || wifiManager == null) {
            val connectedSim = simulatedNetworks.find { it.isConnected } ?: simulatedNetworks.first()
            return@withContext ConnectedInfo(
                ssid = connectedSim.ssid,
                bssid = connectedSim.bssid,
                rssi = connectedSim.rssi,
                linkSpeedMbps = 1200,
                frequencyMhz = connectedSim.frequencyMhz,
                ipAddress = connectedSim.ipAddress ?: "192.168.1.104",
                gatewayIp = "192.168.1.1",
                subnetMask = "255.255.255.0",
                dns1 = "8.8.8.8",
                dns2 = "1.1.1.1",
                macAddress = "A4:C3:F0:12:34:56",
                wifiStandard = connectedSim.standard,
                security = connectedSim.securityType,
                band = connectedSim.band,
                channel = connectedSim.channel
            )
        }

        try {
            val wifiInfo = wifiManager.connectionInfo
            val dhcpInfo = wifiManager.dhcpInfo

            val rawSsid = wifiInfo?.ssid?.replace("\"", "") ?: "Connected Network"
            val ssid = if (rawSsid == "<unknown ssid>") "Connected Wi-Fi" else rawSsid
            val bssid = wifiInfo?.bssid ?: "00:00:00:00:00:00"
            val rssi = wifiInfo?.rssi ?: -60
            val speed = wifiInfo?.linkSpeed ?: 0
            val freq = (wifiInfo?.frequency ?: 2437).coerceAtLeast(2412)

            val ip = WifiUtils.formatIpAddress(wifiInfo?.ipAddress ?: 0)

            // Calculate Subnet Mask
            val rawSubnet = WifiUtils.formatIpAddress(dhcpInfo?.netmask ?: 0)
            val subnet = if (rawSubnet == "0.0.0.0" || dhcpInfo?.netmask == 0) {
                WifiUtils.calculateSubnetMask(ip)
            } else {
                rawSubnet
            }

            // Calculate Gateway
            val rawGateway = WifiUtils.formatIpAddress(dhcpInfo?.gateway ?: 0)
            val gateway = if (rawGateway == "0.0.0.0" || dhcpInfo?.gateway == 0) {
                if (ip.contains(".")) {
                    ip.substringBeforeLast(".") + ".1"
                } else "192.168.1.1"
            } else {
                rawGateway
            }

            // Calculate DNS
            val rawDns1 = WifiUtils.formatIpAddress(dhcpInfo?.dns1 ?: 0)
            val dns1 = if (rawDns1 == "0.0.0.0" || dhcpInfo?.dns1 == 0) "8.8.8.8" else rawDns1

            val rawDns2 = WifiUtils.formatIpAddress(dhcpInfo?.dns2 ?: 0)
            val dns2 = if (rawDns2 == "0.0.0.0" || dhcpInfo?.dns2 == 0) "1.1.1.1" else rawDns2

            // Cross reference with scanned list to get precise capabilities & standard
            val matchedScan = lastScannedList.find {
                it.bssid.equals(bssid, ignoreCase = true) || (it.ssid == ssid && ssid != "Connected Wi-Fi")
            }

            val wifiStandard = matchedScan?.standard ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && wifiInfo != null) {
                WifiUtils.parseWifiStandard(wifiInfo.wifiStandard, freq, "")
            } else {
                WifiUtils.parseWifiStandard(0, freq, "")
            }

            val security = matchedScan?.securityType ?: WifiUtils.parseSecurityType(
                capabilities = "",
                standard = wifiStandard,
                frequencyMhz = freq
            )

            ConnectedInfo(
                ssid = ssid,
                bssid = bssid,
                rssi = rssi,
                linkSpeedMbps = speed,
                frequencyMhz = freq,
                ipAddress = ip,
                gatewayIp = gateway,
                subnetMask = subnet,
                dns1 = dns1,
                dns2 = dns2,
                macAddress = wifiInfo?.macAddress ?: "02:00:00:00:00:00",
                wifiStandard = wifiStandard,
                security = security,
                band = WifiUtils.getFrequencyBand(freq),
                channel = WifiUtils.convertFrequencyToChannel(freq)
            )
        } catch (e: Exception) {
            ConnectedInfo()
        }
    }

    suspend fun executePing(host: String): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val inet = InetAddress.getByName(host)
            val reachable = inet.isReachable(2000)
            val elapsed = System.currentTimeMillis() - startTime
            if (reachable) elapsed else -1L
        } catch (e: Exception) {
            -1L
        }
    }
}

object ScanResultWidth {
    const val WIDTH_20 = 0
    const val WIDTH_40 = 1
    const val WIDTH_80 = 2
    const val WIDTH_160 = 3
    const val WIDTH_80_80 = 4
    const val WIDTH_320 = 5
}
