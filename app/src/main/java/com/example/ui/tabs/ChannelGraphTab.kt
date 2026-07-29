package com.example.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.RectF
import com.example.model.WifiBand
import com.example.model.WifiNetwork
import com.example.ui.theme.*
import com.example.utils.WifiUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class GraphBandSection(
    val displayName: String,
    val shortDisplayName: String,
    val band: WifiBand,
    val channelStart: Int,
    val channelEnd: Int
) {
    BAND_2_4G("2.4 GHz", "2.4 GHz", WifiBand.BAND_2_4GHZ, 1, 14),
    BAND_5G_LOW("5 GHz (36-64)", "5G Low", WifiBand.BAND_5GHZ, 36, 64),
    BAND_5G_HIGH("5 GHz (100-165)", "5G High", WifiBand.BAND_5GHZ, 100, 165),
    BAND_6G("6 GHz (1-93)", "6 GHz", WifiBand.BAND_6GHZ, 1, 93)
}

@Composable
fun ChannelGraphTab(
    networks: List<WifiNetwork>,
    selectedBand: WifiBand,
    onSelectBand: (WifiBand) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(GraphBandSection.BAND_2_4G) }
    var isAutoFitEnabled by remember { mutableStateOf(true) }

    val activeNetworks = remember(networks, selectedSection) {
        networks.filter { net ->
            when (selectedSection) {
                GraphBandSection.BAND_2_4G -> net.band == WifiBand.BAND_2_4GHZ
                GraphBandSection.BAND_5G_LOW -> net.band == WifiBand.BAND_5GHZ && net.channel <= 80
                GraphBandSection.BAND_5G_HIGH -> net.band == WifiBand.BAND_5GHZ && net.channel > 80
                GraphBandSection.BAND_6G -> net.band == WifiBand.BAND_6GHZ
            }
        }
    }

    // Dynamic auto-fit range calculation based on network channel bandwidths & overlap spread
    val (startCh, endCh) = remember(activeNetworks, selectedSection, isAutoFitEnabled) {
        if (!isAutoFitEnabled || activeNetworks.isEmpty()) {
            Pair(selectedSection.channelStart, selectedSection.channelEnd)
        } else {
            var minCh = Float.MAX_VALUE
            var maxCh = Float.MIN_VALUE

            activeNetworks.forEach { net ->
                val halfWidth = when (net.channelWidth) {
                    "40 MHz" -> 4f
                    "80 MHz" -> 8f
                    "160 MHz" -> 16f
                    "320 MHz" -> 32f
                    else -> 2f // 20 MHz
                }
                val left = net.channel - halfWidth
                val right = net.channel + halfWidth
                if (left < minCh) minCh = left
                if (right > maxCh) maxCh = right
            }

            // Padding of 2 channels to fit complete parabolic curve wings cleanly
            val calcStart = max(1, (minCh - 2f).toInt())
            val calcEnd = (maxCh + 2f).toInt()

            // Ensure minimum span so single AP is not stretched awkwardly
            val span = calcEnd - calcStart
            if (span < 10) {
                val mid = (calcStart + calcEnd) / 2
                Pair(max(1, mid - 5), mid + 5)
            } else {
                Pair(calcStart, calcEnd)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .pointerInput(selectedSection) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        val sections = GraphBandSection.values()
                        val currentIndex = sections.indexOf(selectedSection)
                        if (totalDrag < -50f && currentIndex < sections.size - 1) {
                            val nextSec = sections[currentIndex + 1]
                            selectedSection = nextSec
                            onSelectBand(nextSec.band)
                        } else if (totalDrag > 50f && currentIndex > 0) {
                            val prevSec = sections[currentIndex - 1]
                            selectedSection = prevSec
                            onSelectBand(prevSec.band)
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        // Top Header Row: Section Tabs & Auto-Fit Switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(3.dp)
                ) {
                    GraphBandSection.values().forEach { sec ->
                        val isSelected = selectedSection == sec
                        Surface(
                            color = if (isSelected) Color(0xFF0284C7) else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    selectedSection = sec
                                    onSelectBand(sec.band)
                                }
                                .testTag("graph_section_${sec.name}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = sec.shortDisplayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Auto-Fit Toggle Chip
            Surface(
                color = if (isAutoFitEnabled) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAutoFitEnabled) NeonCyan else Color(0xFF334155)
                ),
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isAutoFitEnabled = !isAutoFitEnabled }
                    .testTag("autofit_toggle_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Auto-Fit",
                        tint = if (isAutoFitEnabled) NeonCyan else TextMutedDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isAutoFitEnabled) "Auto-Fit" else "Full",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAutoFitEnabled) NeonCyan else TextMutedDark
                    )
                }
            }
        }

        // Active Overlap Span Banner
        if (activeNetworks.isNotEmpty()) {
            Surface(
                color = GlassIndigoCard,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassIndigoBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Spectrum View: Channels $startCh to $endCh (${endCh - startCh + 1} Ch Span)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${activeNetworks.size} APs Overlapping",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }
        }

        // Main Parabolic Canvas Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassIndigoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("channel_spectrum_canvas")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val leftPadding = 80f
                    val rightPadding = 20f
                    val topPadding = 40f
                    val bottomPadding = 60f

                    val graphWidth = width - leftPadding - rightPadding
                    val graphHeight = height - topPadding - bottomPadding

                    // Draw RSSI Y-Axis Grid Lines (-30 to -100 dBm)
                    val dbmLevels = listOf(-30, -40, -50, -60, -70, -80, -90, -100)
                    dbmLevels.forEach { dbm ->
                        val ratio = (dbm - (-100)) / 70f
                        val y = topPadding + graphHeight * (1f - ratio)

                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(leftPadding, y),
                            end = Offset(width - rightPadding, y),
                            strokeWidth = 1f
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "$dbm",
                            leftPadding - 15f,
                            y + 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#94A3B8")
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                        )
                    }

                    // X-Axis Channel ticks
                    val channelCount = max(1, endCh - startCh)

                    val chStep = when {
                        channelCount > 60 -> 8
                        channelCount > 30 -> 4
                        channelCount > 15 -> 2
                        else -> 1
                    }

                    val channelsToDraw = (startCh..endCh step chStep).toList()

                    channelsToDraw.forEach { ch ->
                        val fraction = (ch - startCh).toFloat() / channelCount.toFloat()
                        val x = leftPadding + fraction * graphWidth

                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(x, topPadding),
                            end = Offset(x, topPadding + graphHeight),
                            strokeWidth = 1f
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "$ch",
                            x,
                            height - 15f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#38BDF8")
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    // 1. Draw Parabolic Spectrum Curves for each Network
                    activeNetworks.forEach { net ->
                        val netColor = Color(net.colorHex)
                        val channelXFraction = (net.channel - startCh).toFloat() / channelCount.toFloat()
                        val centerX = leftPadding + channelXFraction * graphWidth

                        val clampedRssi = max(-100, min(-30, net.rssi))
                        val rssiRatio = (clampedRssi - (-100)) / 70f
                        val peakY = topPadding + graphHeight * (1f - rssiRatio)
                        val baselineY = topPadding + graphHeight

                        val halfWidthInChannels = when (net.channelWidth) {
                            "40 MHz" -> 4f
                            "80 MHz" -> 8f
                            "160 MHz" -> 16f
                            "320 MHz" -> 32f
                            else -> 2f // 20 MHz
                        }

                        val halfWidthPx = (halfWidthInChannels / channelCount.toFloat()) * graphWidth

                        val path = Path().apply {
                            moveTo(centerX - halfWidthPx, baselineY)
                            cubicTo(
                                centerX - halfWidthPx * 0.5f, peakY,
                                centerX + halfWidthPx * 0.5f, peakY,
                                centerX + halfWidthPx, baselineY
                            )
                            close()
                        }

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(netColor.copy(alpha = 0.35f), netColor.copy(alpha = 0.05f)),
                                startY = peakY,
                                endY = baselineY
                            )
                        )

                        drawPath(
                            path = path,
                            color = netColor,
                            style = Stroke(width = 4f)
                        )
                    }

                    // 2. Compute Non-Overlapping Badge Positions for SSIDs
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.LEFT
                    }

                    val placedBadges = mutableListOf<RectF>()
                    data class BadgeInfo(
                        val rect: RectF,
                        val labelText: String,
                        val net: WifiNetwork
                    )
                    val badgesToDraw = mutableListOf<BadgeInfo>()

                    val sortedForBadges = activeNetworks.sortedByDescending { it.rssi }

                    sortedForBadges.forEach { net ->
                        val channelXFraction = (net.channel - startCh).toFloat() / channelCount.toFloat()
                        val centerX = leftPadding + channelXFraction * graphWidth

                        val clampedRssi = max(-100, min(-30, net.rssi))
                        val rssiRatio = (clampedRssi - (-100)) / 70f
                        val peakY = topPadding + graphHeight * (1f - rssiRatio)

                        val labelText = if (net.ssid.length > 18) net.ssid.take(16) + ".." else net.ssid
                        val textWidth = textPaint.measureText(labelText)

                        val badgeWidth = textWidth + 38f
                        val badgeHeight = 44f
                        val spacing = 6f

                        val initialLeft = (centerX - badgeWidth / 2f).coerceIn(leftPadding + 4f, width - rightPadding - badgeWidth - 4f)

                        val candidateYList = listOf(
                            peakY - 14f - badgeHeight,
                            peakY - 14f - 2 * badgeHeight - spacing,
                            peakY - 14f - 3 * badgeHeight - 2 * spacing,
                            peakY + 14f,
                            peakY + 14f + badgeHeight + spacing,
                            peakY + 14f + 2 * badgeHeight + 2 * spacing
                        )

                        var chosenRect: RectF? = null

                        for (candY in candidateYList) {
                            val boundedY = candY.coerceIn(topPadding + 2f, height - bottomPadding - badgeHeight - 2f)

                            val xOffsets = listOf(0f, -25f, 25f, -50f, 50f)
                            for (xOff in xOffsets) {
                                val candLeft = (initialLeft + xOff).coerceIn(leftPadding + 4f, width - rightPadding - badgeWidth - 4f)
                                val candidateRect = RectF(
                                    candLeft,
                                    boundedY,
                                    candLeft + badgeWidth,
                                    boundedY + badgeHeight
                                )

                                var overlaps = false
                                for (existing in placedBadges) {
                                    val expandedExisting = RectF(
                                        existing.left - spacing,
                                        existing.top - spacing,
                                        existing.right + spacing,
                                        existing.bottom + spacing
                                    )
                                    if (RectF.intersects(expandedExisting, candidateRect)) {
                                        overlaps = true
                                        break
                                    }
                                }

                                if (!overlaps) {
                                    chosenRect = candidateRect
                                    break
                                }
                            }
                            if (chosenRect != null) break
                        }

                        val finalRect = chosenRect ?: RectF(
                            initialLeft,
                            (peakY - 14f - badgeHeight).coerceIn(topPadding + 2f, height - bottomPadding - badgeHeight - 2f),
                            initialLeft + badgeWidth,
                            (peakY - 14f).coerceIn(topPadding + 2f + badgeHeight, height - bottomPadding - 2f)
                        )

                        placedBadges.add(finalRect)
                        badgesToDraw.add(BadgeInfo(finalRect, labelText, net))
                    }

                    // 3. Render High-Contrast Badge Cards
                    badgesToDraw.forEach { badge ->
                        val rect = badge.rect
                        val net = badge.net

                        // Drop Shadow
                        val shadowPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#80000000")
                            style = android.graphics.Paint.Style.FILL
                        }
                        val shadowRect = RectF(rect.left + 2f, rect.top + 2f, rect.right + 2f, rect.bottom + 2f)
                        drawContext.canvas.nativeCanvas.drawRoundRect(shadowRect, 10f, 10f, shadowPaint)

                        // Dark Card Background
                        val bgPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#0F172A")
                            style = android.graphics.Paint.Style.FILL
                        }
                        drawContext.canvas.nativeCanvas.drawRoundRect(rect, 10f, 10f, bgPaint)

                        // Colored Border
                        val borderPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor(
                                String.format("#%06X", 0xFFFFFF and net.colorHex.toInt())
                            )
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 3.5f
                        }
                        drawContext.canvas.nativeCanvas.drawRoundRect(rect, 10f, 10f, borderPaint)

                        // Colored Dot Accent
                        val dotX = rect.left + 14f
                        val dotY = rect.top + rect.height() / 2f
                        val dotPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor(
                                String.format("#%06X", 0xFFFFFF and net.colorHex.toInt())
                            )
                            style = android.graphics.Paint.Style.FILL
                        }
                        drawContext.canvas.nativeCanvas.drawCircle(dotX, dotY, 6f, dotPaint)

                        // White Bold Text
                        val textX = rect.left + 26f
                        val textY = rect.top + (rect.height() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                        drawContext.canvas.nativeCanvas.drawText(badge.labelText, textX, textY, textPaint)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend of networks in this graph
        if (activeNetworks.isNotEmpty()) {
            Text(
                text = "DISCOVERED NETWORKS IN BAND (${activeNetworks.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMutedDark,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeNetworks) { net ->
                    Surface(
                        color = DarkNavyCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(net.colorHex))
                            )
                            Text(
                                text = "${net.ssid} (CH ${net.channel} • ${net.channelWidth})",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${net.rssi} dBm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(WifiUtils.getSignalColor(net.rssi))
                            )
                        }
                    }
                }
            }
        }
    }
}
