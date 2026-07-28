package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectedInfo
import com.example.ui.theme.*

@Composable
fun TopBarHeader(
    connectedInfo: ConnectedInfo,
    isScanning: Boolean,
    isSimMode: Boolean,
    onToggleScan: () -> Unit,
    onRefreshScan: () -> Unit,
    onToggleSimMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkNavySurface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main App Bar Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryCyan.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "WiFi Analyzer Logo",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "WiFi Analyzer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Spectrum & Signal Diagnostics",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sim Mode Badge
                    Surface(
                        color = if (isSimMode) NeonAmber.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleSimMode() }
                            .testTag("sim_mode_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSimMode) NeonAmber else NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSimMode) "SIMULATOR" else "HARDWARE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimMode) NeonAmber else NeonGreen
                            )
                        }
                    }

                    // Scan Pause / Play Button
                    IconButton(
                        onClick = onToggleScan,
                        modifier = Modifier.testTag("toggle_scan_button")
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Toggle Scan",
                            tint = if (isScanning) NeonGreen else TextSecondaryDark
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefreshScan,
                        modifier = Modifier.testTag("refresh_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Scan",
                            tint = NeonCyan
                        )
                    }
                }
            }
        }
    }
}
