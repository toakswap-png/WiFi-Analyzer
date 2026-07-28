package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChannelRating
import com.example.model.WifiBand
import com.example.ui.theme.*

@Composable
fun ChannelRatingTab(
    ratings: List<ChannelRating>,
    selectedBand: WifiBand,
    onSelectBand: (WifiBand) -> Unit,
    modifier: Modifier = Modifier
) {
    val bestChannels = remember(ratings) {
        ratings.filter { it.isRecommended || it.ratingStars >= 4.0f }
            .sortedByDescending { it.ratingStars }
            .take(3)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Band Filter Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            WifiBand.values().forEach { band ->
                Surface(
                    color = if (selectedBand == band) PrimaryCyan else DarkNavyCard,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectBand(band) }
                        .testTag("rating_band_${band.shortName}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = band.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedBand == band) Color.White else TextSecondaryDark
                        )
                    }
                }
            }
        }

        // Router Channel Recommendation Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("best_channels_recommendation")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Surface(
                    color = NeonGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Recommended",
                        tint = NeonGreen,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RECOMMENDED ROUTER CHANNELS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = if (bestChannels.isNotEmpty()) {
                            "Best channels: " + bestChannels.joinToString { "CH ${it.channel}" }
                        } else "Scan in progress...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Set your Wi-Fi router to one of these channels for lowest interference.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        // Channel Ratings List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(ratings, key = { it.channel }) { rating ->
                ChannelRatingCard(rating = rating)
            }
        }
    }
}

@Composable
fun ChannelRatingCard(rating: ChannelRating) {
    var isExpanded by remember { mutableStateOf(false) }

    val ratingColor = when {
        rating.ratingStars >= 4.0f -> NeonGreen
        rating.ratingStars >= 2.5f -> NeonAmber
        else -> NeonRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("rating_card_ch_${rating.channel}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Channel & Frequency
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = ratingColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "CH ${rating.channel}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ratingColor
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "${rating.frequencyMhz} MHz",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Co-channel: ${rating.coChannelCount} • Adjacent: ${rating.adjacentChannelCount}",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                // Right: Star Bar & Value
                Column(horizontalAlignment = Alignment.End) {
                    StarRatingBar(ratingStars = rating.ratingStars)
                    Text(
                        text = "${rating.ratingStars} / 5.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ratingColor
                    )
                }
            }

            // Expanded Overlapping Networks list
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (rating.overlappingSsids.isEmpty()) {
                        Text(
                            text = "✓ No interfering access points on this channel",
                            fontSize = 11.sp,
                            color = NeonGreen
                        )
                    } else {
                        Text(
                            text = "Interfering Networks (${rating.overlappingSsids.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        rating.overlappingSsids.forEach { ssid ->
                            Text(
                                text = "• $ssid",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(ratingStars: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val icon = when {
                ratingStars >= i -> Icons.Default.Star
                ratingStars >= i - 0.5f -> Icons.Default.StarHalf
                else -> Icons.Default.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (ratingStars >= i - 0.5f) NeonAmber else TextMutedDark,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
