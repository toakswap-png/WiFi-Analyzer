package com.example.ui.tabs

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WifiBand
import com.example.model.WifiNetwork
import com.example.model.WifiStandard
import com.example.ui.theme.*
import com.example.utils.WifiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPointsTab(
    networks: List<WifiNetwork>,
    isWifiEnabled: Boolean = true,
    selectedBand: WifiBand?,
    searchQuery: String,
    onSelectBand: (WifiBand?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectTargetForMeter: (WifiNetwork) -> Unit,
    modifier: Modifier = Modifier
) {
    var detailNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    val context = LocalContext.current
    val bandList: List<WifiBand?> = remember { listOf(null) + WifiBand.values().toList() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .pointerInput(selectedBand) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        val currentIndex = bandList.indexOf(selectedBand)
                        if (totalDrag < -50f && currentIndex < bandList.size - 1 && currentIndex != -1) {
                            onSelectBand(bandList[currentIndex + 1])
                        } else if (totalDrag > 50f && currentIndex > 0) {
                            onSelectBand(bandList[currentIndex - 1])
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        // Band Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isAllSelected = selectedBand == null
                Surface(
                    color = if (isAllSelected) Color(0xFF0284C7) else Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAllSelected) Color(0xFF0284C7) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectBand(null) }
                        .testTag("filter_all")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = "All Bands (${networks.size})",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            items(WifiBand.values()) { band ->
                val isBandSelected = selectedBand == band
                val count = networks.count { it.band == band }
                Surface(
                    color = if (isBandSelected) Color(0xFF0284C7) else Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBandSelected) Color(0xFF0284C7) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectBand(band) }
                        .testTag("filter_${band.shortName}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = "${band.displayName} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isBandSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBandSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isWifiEnabled) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Wi-Fi Off",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Please Turn On WiFi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Wi-Fi is currently turned off on your device. Turn on Wi-Fi to scan nearby access points.",
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Turn On Wi-Fi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (networks.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No networks",
                        tint = TextMutedDark,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Wi-Fi networks found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "Try pulling down or tapping Refresh above",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Connected Network Header Banner
                val connectedNet = networks.firstOrNull { it.isConnected }
                if (connectedNet != null) {
                    item {
                        Surface(
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Connected",
                                    tint = Color(0xFF84CC16),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Connected to: ${connectedNet.ssid}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        WifiStandardBadge(standard = connectedNet.standard)

                                        Text(
                                            text = "CH ${connectedNet.channel}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF84CC16)
                                        )

                                        Text(
                                            text = "•",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        Text(
                                            text = "BW: ${connectedNet.channelWidth}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1)
                                        )

                                        Text(
                                            text = "•",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        val speedText = if (connectedNet.linkSpeedMbps > 0) "${connectedNet.linkSpeedMbps} Mbps" else "150 Mbps"
                                        Text(
                                            text = "Speed: $speedText",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(networks, key = { it.bssid + it.frequencyMhz }) { net ->
                    NetworkCardItem(
                        network = net,
                        allNetworks = networks,
                        onCardClick = { detailNetwork = net },
                        onSelectForMeter = { onSelectTargetForMeter(net) }
                    )
                    HorizontalDivider(
                        color = Color(0xFF334155),
                        thickness = 1.dp
                    )
                }
            }
        }
    }

    // Network Detail Dialog
    detailNetwork?.let { net ->
        NetworkDetailModal(
            network = net,
            onDismiss = { detailNetwork = null },
            onOpenMeter = {
                onSelectTargetForMeter(net)
                detailNetwork = null
            }
        )
    }
}

@Composable
fun NetworkCardItem(
    network: WifiNetwork,
    allNetworks: List<WifiNetwork>,
    onCardClick: () -> Unit,
    onSelectForMeter: () -> Unit
) {
    val signalPercent = WifiUtils.calculateSignalPercentage(network.rssi)
    val signalColor = WifiUtils.getComposeSignalColor(network.rssi)

    val multiBandText = remember(network, allNetworks) {
        if (network.ssid.isBlank() || network.ssid == "<Hidden Network>") {
            null
        } else {
            val matching = allNetworks.filter { it.ssid == network.ssid }
            val bands = matching.map { it.band }.distinct()
            if (bands.size > 1) {
                val order = listOf(WifiBand.BAND_2_4GHZ, WifiBand.BAND_5GHZ, WifiBand.BAND_6GHZ)
                order.filter { bands.contains(it) }.joinToString(" / ") { it.shortName }
            } else {
                null
            }
        }
    }

    Surface(
        color = Color(0xFF0F1117),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("ap_card_${network.bssid}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Line 1: SSID (BSSID) Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(network.colorHex))
                )

                Text(
                    text = "${network.ssid} (${network.bssid})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (multiBandText != null) {
                    Surface(
                        color = NeonPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = multiBandText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Line 2: Signal Icon + CH <num> <freq> MHz + Wi-Fi Standard Badge | Horizontal Green Signal Bar with dBm
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WifiSignalIcon(
                        rssi = network.rssi,
                        isLocked = network.capabilities.contains("WPA") || network.capabilities.contains("WEP") || network.capabilities.contains("EAP")
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = "CH",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        text = "${network.channel}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = signalColor
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${network.frequencyMhz} MHz",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    WifiStandardBadge(standard = network.standard)
                }

                // Signal Progress Bar with dBm text
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(130.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
                ) {
                    // Filled Portion
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = signalPercent / 100f)
                            .align(Alignment.CenterStart)
                            .background(signalColor)
                    )

                    // Text inside bar with high-contrast text color
                    val textOnBarColor = if (network.rssi > -74) Color(0xFF090D16) else Color.White
                    Text(
                        text = "${network.rssi} dBm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textOnBarColor,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = if (network.rssi > -74) Color(0x30FFFFFF) else Color(0xEE000000),
                                offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Line 3: Capabilities String
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (network.capabilities.startsWith("[")) network.capabilities else "[${network.capabilities}]",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onSelectForMeter,
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("gauge_icon_${network.bssid}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Open Signal Meter",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkDetailModal(
    network: WifiNetwork,
    onDismiss: () -> Unit,
    onOpenMeter: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavySurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi Icon",
                    tint = NeonCyan
                )
                Text(
                    text = network.ssid,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailRow("BSSID (MAC)", network.bssid)
                DetailRow("Signal Strength", "${network.rssi} dBm (${WifiUtils.calculateSignalPercentage(network.rssi)}%)")
                DetailRow("Quality", WifiUtils.getSignalQualityLabel(network.rssi))
                DetailRow("Band & Frequency", "${network.band.displayName} (${network.frequencyMhz} MHz)")
                DetailRow("Channel & Width", "Channel ${network.channel} • ${network.channelWidth}")
                DetailRow("Wi-Fi Standard", "${network.standard.label} (${network.standard.techCode})")
                DetailRow("Security", network.securityType)
                DetailRow("Capabilities", network.capabilities)
                if (network.linkSpeedMbps > 0) {
                    DetailRow("Link Speed", "${network.linkSpeedMbps} Mbps")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenMeter,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Signal Meter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondaryDark)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WifiSignalIcon(
    rssi: Int,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val level = when {
        rssi > -55 -> 5
        rssi >= -65 -> 4
        rssi >= -74 -> 3
        rssi >= -83 -> 2
        else -> 1
    }
    val signalColor = WifiUtils.getComposeSignalColor(rssi)

    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = 1.8f.dp.toPx()

            val centerX = width / 2f
            val centerY = height * 0.90f

            for (i in 1..5) {
                val radius = (height * 0.165f) * i
                val isActive = i <= level
                val color = if (isActive) signalColor else Color(0x35FFFFFF)

                if (i == 1) {
                    drawCircle(
                        color = color,
                        radius = radius * 0.55f,
                        center = Offset(centerX, centerY - radius * 0.2f)
                    )
                } else {
                    val rectSize = radius * 2f
                    drawArc(
                        color = color,
                        startAngle = 220f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(rectSize, rectSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secured",
                tint = Color(0xFFFACC15),
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun WifiStandardBadge(standard: WifiStandard) {
    val (badgeBg, badgeBorder, badgeText) = when (standard) {
        WifiStandard.WIFI_7 -> Triple(Color(0xFF831843), Color(0xFFF472B6), Color(0xFFF472B6))
        WifiStandard.WIFI_6E -> Triple(Color(0xFF3B0764), Color(0xFFC084FC), Color(0xFFE9D5FF))
        WifiStandard.WIFI_6 -> Triple(Color(0xFF1E1B4B), Color(0xFF818CF8), Color(0xFFC7D2FE))
        WifiStandard.WIFI_5 -> Triple(Color(0xFF064E3B), Color(0xFF34D399), Color(0xFFA7F3D0))
        WifiStandard.WIFI_4 -> Triple(Color(0xFF451A03), Color(0xFFF97316), Color(0xFFFFEDD5))
        WifiStandard.LEGACY -> Triple(Color(0xFF1F2937), Color(0xFF9CA3AF), Color(0xFFE5E7EB))
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
    ) {
        Text(
            text = standard.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = badgeText,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
        )
    }
}
