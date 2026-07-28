package com.example.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectedInfo
import com.example.model.PingResult
import com.example.model.SignalHistoryPoint
import com.example.ui.theme.*
import com.example.utils.WifiUtils
import kotlin.math.max
import kotlin.math.min

@Composable
fun ConnectedDetailsTab(
    connectedInfo: ConnectedInfo,
    signalHistory: List<SignalHistoryPoint>,
    pingResults: List<PingResult>,
    onRunPing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pingHostInput by remember { mutableStateOf(connectedInfo.gatewayIp.ifEmpty { "192.168.1.1" }) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Connected Wi-Fi Primary Details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connected_specs_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = PrimaryCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = "Router Specs",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = connectedInfo.ssid,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = connectedInfo.bssid,
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark
                                )
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${connectedInfo.wifiStandard.label} (${connectedInfo.wifiStandard.techCode})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpecBox("Link Speed", "${connectedInfo.linkSpeedMbps} Mbps", NeonGreen, Modifier.weight(1f))
                        SpecBox("Signal Strength", "${connectedInfo.rssi} dBm", NeonCyan, Modifier.weight(1f))
                        SpecBox("Band / Channel", "${connectedInfo.band.shortName} • Ch ${connectedInfo.channel}", NeonPurple, Modifier.weight(1f))
                    }
                }
            }
        }

        // IP Network Configuration Grid Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NETWORK & SECURITY CONFIGURATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ConfigRow("Wi-Fi Standard", "${connectedInfo.wifiStandard.label} (${connectedInfo.wifiStandard.techCode})", valueColor = NeonCyan)
                        ConfigRow("Security Protocol", connectedInfo.security, valueColor = NeonGreen)
                        ConfigRow("Local Device IP", connectedInfo.ipAddress)
                        ConfigRow("Gateway Router IP", connectedInfo.gatewayIp)
                        ConfigRow("Subnet Mask", connectedInfo.subnetMask)
                        ConfigRow("DNS 1 Server", connectedInfo.dns1)
                        ConfigRow("DNS 2 Server", connectedInfo.dns2)
                        ConfigRow("Device MAC", connectedInfo.macAddress)
                    }
                }
            }
        }

        // Live RSSI History Line Chart Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("signal_history_chart")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "History Chart",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "REAL-TIME SIGNAL HISTORY (LAST 30S)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${connectedInfo.rssi} dBm",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(WifiUtils.getSignalColor(connectedInfo.rssi))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        val width = size.width
                        val height = size.height

                        val leftPad = 50f
                        val bottomPad = 30f
                        val chartW = width - leftPad
                        val chartH = height - bottomPad

                        // Grid lines -30 to -100
                        val gridDbm = listOf(-30, -60, -90)
                        gridDbm.forEach { dbm ->
                            val ratio = (dbm - (-100)) / 70f
                            val y = chartH * (1f - ratio)
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(leftPad, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "$dbm",
                                leftPad - 10f,
                                y + 8f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#94A3B8")
                                    textSize = 22f
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                }
                            )
                        }

                        if (signalHistory.size > 1) {
                            val path = Path()
                            signalHistory.forEachIndexed { idx, pt ->
                                val x = leftPad + (idx.toFloat() / (signalHistory.size - 1)) * chartW
                                val clampedRssi = max(-100, min(-30, pt.rssi))
                                val ratio = (clampedRssi - (-100)) / 70f
                                val y = chartH * (1f - ratio)

                                if (idx == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }

                                drawCircle(
                                    color = Color(0xFF38BDF8),
                                    radius = 4f,
                                    center = Offset(x, y)
                                )
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFF0EA5E9),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }
            }
        }

        // Live Ping Diagnostics Tool Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ping_tool_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "LATENCY & PING DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = pingHostInput,
                            onValueChange = { pingHostInput = it },
                            label = { Text("Target IP / Domain", fontSize = 11.sp, color = TextMutedDark) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ping_host_input")
                        )

                        Button(
                            onClick = { onRunPing(pingHostInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("run_ping_button")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PING", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pingResults.isNotEmpty()) {
                        Text(
                            text = "Recent Latency History:",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        pingResults.take(4).forEach { res ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = res.host,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (res.isSuccessful) "${res.latencyMs} ms" else "Timed out",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isSuccessful) NeonGreen else NeonRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = TextSecondaryDark, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ConfigRow(label: String, value: String, valueColor: Color = Color.White) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                fontWeight = FontWeight.Medium
            )
            SelectionContainer {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
        }
    }
}
