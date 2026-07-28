package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WifiAnalyzerViewModel
import com.example.ui.components.TopBarHeader
import com.example.ui.tabs.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: WifiAnalyzerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                WifiAnalyzerApp(viewModel = viewModel)
            }
        }
    }
}

data class AnalyzerTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAnalyzerApp(viewModel: WifiAnalyzerViewModel) {
    val context = LocalContext.current

    val networks by viewModel.filteredNetworks.collectAsStateWithLifecycle()
    val allNetworks by viewModel.networks.collectAsStateWithLifecycle()
    val connectedInfo by viewModel.connectedInfo.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isWifiEnabled by viewModel.isWifiEnabled.collectAsStateWithLifecycle()
    val isSimMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val selectedBandFilter by viewModel.selectedBandFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGraphBand by viewModel.selectedGraphBand.collectAsStateWithLifecycle()
    val selectedRatingBand by viewModel.selectedRatingBand.collectAsStateWithLifecycle()
    val selectedTarget by viewModel.selectedTargetNetwork.collectAsStateWithLifecycle()
    val isAudioBeepEnabled by viewModel.isAudioBeepEnabled.collectAsStateWithLifecycle()
    val signalHistory by viewModel.signalHistory.collectAsStateWithLifecycle()
    val channelRatings by viewModel.channelRatings.collectAsStateWithLifecycle()
    val pingResults by viewModel.pingResults.collectAsStateWithLifecycle()

    var activeTabIndex by remember { mutableStateOf(0) }

    // Request Runtime Permissions Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshScan()
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    val tabItems = listOf(
        AnalyzerTabItem("Networks", Icons.Default.FormatListNumbered, "tab_networks"),
        AnalyzerTabItem("Spectrum", Icons.Default.ShowChart, "tab_spectrum"),
        AnalyzerTabItem("Meter", Icons.Default.Speed, "tab_meter"),
        AnalyzerTabItem("Ratings", Icons.Default.Star, "tab_ratings"),
        AnalyzerTabItem("Connected", Icons.Default.Router, "tab_connected")
    )

    Scaffold(
        topBar = {
            TopBarHeader(
                connectedInfo = connectedInfo,
                isScanning = isScanning,
                isSimMode = isSimMode,
                onToggleScan = { viewModel.toggleAutoScan() },
                onRefreshScan = { viewModel.refreshScan() },
                onToggleSimMode = { viewModel.toggleSimulationMode() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Surface(
                color = DarkNavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                TabRow(
                    selectedTabIndex = activeTabIndex,
                    containerColor = DarkNavySurface,
                    contentColor = PrimaryCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabIndex]),
                            height = 3.dp,
                            color = PrimaryCyan
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = activeTabIndex == index,
                            onClick = { activeTabIndex = index },
                            text = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (activeTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTabIndex == index) NeonCyan else TextSecondaryDark
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (activeTabIndex == index) NeonCyan else TextMutedDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        containerColor = DarkNavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTabIndex) {
                0 -> AccessPointsTab(
                    networks = networks,
                    isWifiEnabled = isWifiEnabled,
                    selectedBand = selectedBandFilter,
                    searchQuery = searchQuery,
                    onSelectBand = { viewModel.setBandFilter(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSelectTargetForMeter = { net ->
                        viewModel.selectTargetNetwork(net)
                        activeTabIndex = 2 // Switch to Meter tab
                    }
                )

                1 -> ChannelGraphTab(
                    networks = allNetworks,
                    selectedBand = selectedGraphBand,
                    onSelectBand = { viewModel.setGraphBand(it) }
                )

                2 -> SignalMeterTab(
                    allNetworks = allNetworks,
                    selectedTarget = selectedTarget,
                    isAudioBeepEnabled = isAudioBeepEnabled,
                    onSelectTarget = { viewModel.selectTargetNetwork(it) },
                    onToggleAudioBeep = { viewModel.toggleAudioBeep() }
                )

                3 -> ChannelRatingTab(
                    ratings = channelRatings,
                    selectedBand = selectedRatingBand,
                    onSelectBand = { viewModel.setRatingBand(it) }
                )

                4 -> ConnectedDetailsTab(
                    connectedInfo = connectedInfo,
                    signalHistory = signalHistory,
                    pingResults = pingResults,
                    onRunPing = { viewModel.runPingTest(it) }
                )
            }
        }
    }
}
