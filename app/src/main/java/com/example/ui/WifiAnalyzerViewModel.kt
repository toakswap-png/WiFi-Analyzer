package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChannelRating
import com.example.model.ConnectedInfo
import com.example.model.PingResult
import com.example.model.SignalHistoryPoint
import com.example.model.WifiBand
import com.example.model.WifiNetwork
import com.example.repository.WifiScannerRepository
import com.example.utils.AudioBeeper
import com.example.utils.WifiUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class WifiAnalyzerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WifiScannerRepository(application)
    private val audioBeeper = AudioBeeper()

    private val _networks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val networks: StateFlow<List<WifiNetwork>> = _networks.asStateFlow()

    private val _connectedInfo = MutableStateFlow(ConnectedInfo())
    val connectedInfo: StateFlow<ConnectedInfo> = _connectedInfo.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    val isSimulationMode: StateFlow<Boolean> = repository.isSimulationMode

    private val _selectedBandFilter = MutableStateFlow<WifiBand?>(null) // null = All Bands
    val selectedBandFilter: StateFlow<WifiBand?> = _selectedBandFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGraphBand = MutableStateFlow(WifiBand.BAND_2_4GHZ)
    val selectedGraphBand: StateFlow<WifiBand> = _selectedGraphBand.asStateFlow()

    private val _selectedRatingBand = MutableStateFlow(WifiBand.BAND_2_4GHZ)
    val selectedRatingBand: StateFlow<WifiBand> = _selectedRatingBand.asStateFlow()

    private val _selectedTargetNetwork = MutableStateFlow<WifiNetwork?>(null)
    val selectedTargetNetwork: StateFlow<WifiNetwork?> = _selectedTargetNetwork.asStateFlow()

    private val _isAudioBeepEnabled = MutableStateFlow(false)
    val isAudioBeepEnabled: StateFlow<Boolean> = _isAudioBeepEnabled.asStateFlow()

    private val _signalHistory = MutableStateFlow<List<SignalHistoryPoint>>(emptyList())
    val signalHistory: StateFlow<List<SignalHistoryPoint>> = _signalHistory.asStateFlow()

    private val _pingResults = MutableStateFlow<List<PingResult>>(emptyList())
    val pingResults: StateFlow<List<PingResult>> = _pingResults.asStateFlow()

    private var autoScanJob: Job? = null
    private var beepLoopJob: Job? = null
    private var scanIntervalSeconds = 5

    // Filtered Access Points flow
    val filteredNetworks = combine(networks, selectedBandFilter, searchQuery) { list, band, query ->
        list.filter { net ->
            (band == null || net.band == band) &&
                    (query.isEmpty() || net.ssid.contains(query, ignoreCase = true) || net.bssid.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Channel Ratings flow for selected rating band
    val channelRatings = combine(networks, selectedRatingBand) { list, band ->
        WifiUtils.calculateChannelRatings(band, list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startAutoScan()
    }

    fun startAutoScan() {
        autoScanJob?.cancel()
        autoScanJob = viewModelScope.launch {
            _isScanning.value = true
            while (true) {
                performRefresh()
                delay(scanIntervalSeconds * 1000L)
            }
        }
    }

    fun stopAutoScan() {
        autoScanJob?.cancel()
        _isScanning.value = false
    }

    fun toggleAutoScan() {
        if (_isScanning.value) {
            stopAutoScan()
        } else {
            startAutoScan()
        }
    }

    fun refreshScan() {
        viewModelScope.launch {
            performRefresh()
        }
    }

    private suspend fun performRefresh() {
        _isWifiEnabled.value = repository.isWifiEnabled()
        val scanned = repository.performScan()
        val conn = repository.getConnectedInfo()

        _networks.value = scanned
        _connectedInfo.value = conn

        // Set default target network for Signal Meter if none selected
        if (_selectedTargetNetwork.value == null && scanned.isNotEmpty()) {
            val connNet = scanned.find { it.isConnected } ?: scanned.first()
            _selectedTargetNetwork.value = connNet
        } else if (_selectedTargetNetwork.value != null) {
            // Update active target network with latest RSSI
            val updatedTarget = scanned.find { it.bssid == _selectedTargetNetwork.value?.bssid }
            if (updatedTarget != null) {
                _selectedTargetNetwork.value = updatedTarget
            }
        }

        // Append to signal history
        val targetRssi = _selectedTargetNetwork.value?.rssi ?: conn.rssi
        val targetSsid = _selectedTargetNetwork.value?.ssid ?: conn.ssid
        val currentHistory = _signalHistory.value.toMutableList()
        currentHistory.add(SignalHistoryPoint(System.currentTimeMillis(), targetRssi, targetSsid))
        // Keep last 30 points
        if (currentHistory.size > 30) {
            currentHistory.removeAt(0)
        }
        _signalHistory.value = currentHistory
    }

    fun toggleSimulationMode() {
        repository.setSimulationMode(!repository.isSimulationMode.value)
        refreshScan()
    }

    fun setBandFilter(band: WifiBand?) {
        _selectedBandFilter.value = band
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGraphBand(band: WifiBand) {
        _selectedGraphBand.value = band
    }

    fun setRatingBand(band: WifiBand) {
        _selectedRatingBand.value = band
    }

    fun selectTargetNetwork(network: WifiNetwork) {
        _selectedTargetNetwork.value = network
    }

    fun toggleAudioBeep() {
        val newState = !_isAudioBeepEnabled.value
        _isAudioBeepEnabled.value = newState
        audioBeeper.setMuted(!newState)
        if (newState) {
            startBeepLoop()
        } else {
            beepLoopJob?.cancel()
            beepLoopJob = null
        }
    }

    private fun startBeepLoop() {
        beepLoopJob?.cancel()
        beepLoopJob = viewModelScope.launch {
            while (_isAudioBeepEnabled.value) {
                val currentRssi = _selectedTargetNetwork.value?.rssi ?: _connectedInfo.value.rssi
                audioBeeper.playBeepForSignal(currentRssi)
                val delayMs = calculateBeepInterval(currentRssi)
                delay(delayMs)
            }
        }
    }

    private fun calculateBeepInterval(rssi: Int): Long {
        val clampedRssi = rssi.coerceIn(-95, -35)
        val fraction = (clampedRssi - (-95)) / 60f
        val delayMs = 1800L - (fraction * 1680L).toLong()
        return delayMs.coerceIn(120L, 2000L)
    }

    fun runPingTest(targetHost: String = "192.168.1.1") {
        viewModelScope.launch {
            val latency = repository.executePing(targetHost)
            val newResult = PingResult(
                host = targetHost,
                isSuccessful = latency >= 0,
                latencyMs = if (latency >= 0) latency else 0
            )
            val currentList = _pingResults.value.toMutableList()
            currentList.add(0, newResult)
            if (currentList.size > 10) currentList.removeAt(currentList.lastIndex)
            _pingResults.value = currentList
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioBeeper.release()
    }
}
