package com.example.ui.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WifiNetwork
import com.example.ui.theme.*
import com.example.utils.WifiUtils
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalMeterTab(
    allNetworks: List<WifiNetwork>,
    selectedTarget: WifiNetwork?,
    isAudioBeepEnabled: Boolean,
    onSelectTarget: (WifiNetwork) -> Unit,
    onToggleAudioBeep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val targetNetwork = selectedTarget ?: allNetworks.firstOrNull()

    val currentRssi = targetNetwork?.rssi ?: -100
    val targetPercent = WifiUtils.calculateSignalPercentage(currentRssi)
    val qualityLabel = WifiUtils.getSignalQualityLabel(currentRssi)
    val signalColor = Color(WifiUtils.getSignalColor(currentRssi))

    // Smooth needle angle animation
    val clampedRssi = max(-100, min(-30, currentRssi))
    val targetAngleFraction = (clampedRssi - (-100)) / 70f
    val needleAngleDegrees by animateFloatAsState(
        targetValue = 220f + (targetAngleFraction * 160f), // 220° to 380°
        animationSpec = spring(stiffness = 200f),
        label = "needle_angle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Target Network Selector Dropdown Box
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = DarkNavyCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { dropdownExpanded = true }
                    .testTag("network_selector_dropdown")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "TARGET NETWORK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark
                        )
                        Text(
                            text = targetNetwork?.ssid ?: "No Network Selected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        targetNetwork?.let { net ->
                            Text(
                                text = "${net.bssid} • CH ${net.channel} (${net.band.displayName})",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier
                    .background(DarkNavySurface)
                    .fillMaxWidth(0.9f)
            ) {
                allNetworks.forEach { net ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = net.ssid,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${net.rssi} dBm",
                                    fontSize = 12.sp,
                                    color = Color(WifiUtils.getSignalColor(net.rssi))
                                )
                            }
                        },
                        onClick = {
                            onSelectTarget(net)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speedometer Canvas Gauge Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("signal_gauge_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Analog Meter Gauge Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        val centerX = width / 2f
                        val centerY = height * 0.72f
                        val radius = min(width * 0.42f, height * 0.55f)

                        val strokeWidth = 24f

                        // Zone 1: Poor Red (-100 to -85)
                        drawArc(
                            color = Color(0xFFF87171),
                            startAngle = 220f,
                            sweepAngle = 160f * (15f / 70f),
                            useCenter = false,
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Zone 2: Fair Yellow (-85 to -70)
                        drawArc(
                            color = Color(0xFFFACC15),
                            startAngle = 220f + 160f * (15f / 70f),
                            sweepAngle = 160f * (15f / 70f),
                            useCenter = false,
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Zone 3: Excellent Green (-70 to -30)
                        drawArc(
                            color = Color(0xFF4ADE80),
                            startAngle = 220f + 160f * (30f / 70f),
                            sweepAngle = 160f * (40f / 70f),
                            useCenter = false,
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )

                        // Draw Gauge Scale Tick Marks & Labels
                        val ticks = listOf(-100, -90, -80, -70, -60, -50, -40, -30)
                        ticks.forEach { tick ->
                            val frac = (tick - (-100)) / 70f
                            val angleRad = Math.toRadians((220f + frac * 160f).toDouble())

                            val outerX = centerX + (radius + 20f) * cos(angleRad).toFloat()
                            val outerY = centerY + (radius + 20f) * sin(angleRad).toFloat()

                            val innerX = centerX + (radius - 15f) * cos(angleRad).toFloat()
                            val innerY = centerY + (radius - 15f) * sin(angleRad).toFloat()

                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(innerX, innerY),
                                end = Offset(outerX, outerY),
                                strokeWidth = 3f
                            )

                            val textX = centerX + (radius - 40f) * cos(angleRad).toFloat()
                            val textY = centerY + (radius - 40f) * sin(angleRad).toFloat()

                            drawContext.canvas.nativeCanvas.drawText(
                                "$tick",
                                textX,
                                textY + 8f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#94A3B8")
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }

                        // Draw Needle Line
                        val needleAngleRad = Math.toRadians(needleAngleDegrees.toDouble())
                        val needleLength = radius * 0.82f

                        val needleEndX = centerX + needleLength * cos(needleAngleRad).toFloat()
                        val needleEndY = centerY + needleLength * sin(needleAngleRad).toFloat()

                        drawLine(
                            color = Color.Black.copy(alpha = 0.4f),
                            start = Offset(centerX + 3f, centerY + 3f),
                            end = Offset(needleEndX + 3f, needleEndY + 3f),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(centerX, centerY),
                            end = Offset(needleEndX, needleEndY),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = Color(0xFF0EA5E9),
                            radius = 16f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                // Digital Signal Readout Panel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$currentRssi dBm",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = signalColor
                    )

                    Surface(
                        color = signalColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "$qualityLabel Signal ($targetPercent%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = signalColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sound Feedback Control Bar
        Surface(
            color = DarkNavyCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isAudioBeepEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Audio Beep",
                        tint = if (isAudioBeepEnabled) NeonCyan else TextMutedDark
                    )

                    Column {
                        Text(
                            text = "Signal Audio Beeper",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAudioBeepEnabled) "Audible pitch feedback active" else "Sound muted",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Switch(
                    checked = isAudioBeepEnabled,
                    onCheckedChange = { onToggleAudioBeep() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryCyan,
                        uncheckedThumbColor = TextMutedDark,
                        uncheckedTrackColor = DarkNavySurface
                    ),
                    modifier = Modifier.testTag("audio_beep_switch")
                )
            }
        }
    }
}
