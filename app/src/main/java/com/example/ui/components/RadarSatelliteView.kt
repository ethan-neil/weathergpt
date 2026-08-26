package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class MapLayerType(val label: String, val iconEmoji: String) {
    DOPPLER_RADAR("Doppler Radar (dBZ)", "📡"),
    SATELLITE_CLOUDS("INSAT Satellite Cloud", "🛰️"),
    PRECIPITATION_MAP("Rainfall Intensity", "🌧️")
}

@Composable
fun RadarSatelliteView(
    locationName: String,
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(MapLayerType.DOPPLER_RADAR) }
    var timeIndex by remember { mutableStateOf(3) } // 0: -6h, 1: -4h, 2: -2h, 3: Now, 4: +2h
    var isPlaying by remember { mutableStateOf(true) }

    val timeLabels = listOf("-6h", "-4h", "-2h", "Now", "+2h")

    // Automatic playback animation loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1200)
            timeIndex = (timeIndex + 1) % timeLabels.size
        }
    }

    // Infinite radar sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radar_satellite_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header & Layer Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Radar & Satellite Products",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Area: $locationName • Mode: ${selectedLayer.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = {
                        val nextOrdinal = (selectedLayer.ordinal + 1) % MapLayerType.values().size
                        selectedLayer = MapLayerType.values()[nextOrdinal]
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("layer_toggle_button")
                ) {
                    Text(
                        text = selectedLayer.iconEmoji,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070B0E))
                    .border(1.dp, Color(0xFF1E2D3D), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                    val maxRadius = minOf(canvasWidth, canvasHeight) * 0.46f

                    // Draw concentric radar range rings
                    for (i in 1..4) {
                        val r = maxRadius * (i / 4f)
                        drawCircle(
                            color = Color(0xFF162736),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.5f)
                        )
                    }

                    // Draw crosshairs
                    drawLine(
                        color = Color(0xFF162736),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0xFF162736),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.5f
                    )

                    // Draw simulated weather layers based on time offset
                    val timeOffsetFactor = (timeIndex - 2) * 25f

                    when (selectedLayer) {
                        MapLayerType.DOPPLER_RADAR -> {
                            val stormCenter1 = Offset(center.x - 50f + timeOffsetFactor, center.y - 40f + timeOffsetFactor * 0.5f)
                            val stormCenter2 = Offset(center.x + 80f + timeOffsetFactor * 0.8f, center.y + 30f)

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(RadarRed.copy(alpha = 0.85f), RadarYellow.copy(alpha = 0.65f), RadarGreen.copy(alpha = 0.4f), Color.Transparent),
                                    center = stormCenter1,
                                    radius = 75f
                                ),
                                center = stormCenter1,
                                radius = 75f
                            )

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(RadarYellow.copy(alpha = 0.7f), RadarGreen.copy(alpha = 0.45f), RadarCyan.copy(alpha = 0.2f), Color.Transparent),
                                    center = stormCenter2,
                                    radius = 60f
                                ),
                                center = stormCenter2,
                                radius = 60f
                            )

                            // Radar Sweep line
                            val rad = Math.toRadians(sweepAngle.toDouble())
                            val sweepEnd = Offset(
                                (center.x + maxRadius * Math.cos(rad)).toFloat(),
                                (center.y + maxRadius * Math.sin(rad)).toFloat()
                            )
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, SophisticatedIceBluePrimary.copy(alpha = 0.7f)),
                                    start = center,
                                    end = sweepEnd
                                ),
                                start = center,
                                end = sweepEnd,
                                strokeWidth = 2.5f
                            )
                        }

                        MapLayerType.SATELLITE_CLOUDS -> {
                            val cloudBand1 = Offset(center.x - 70f + timeOffsetFactor * 1.2f, center.y - 20f)
                            val cloudBand2 = Offset(center.x + 40f + timeOffsetFactor * 1.1f, center.y - 60f)

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.75f), Color(0xFFB0BEC5).copy(alpha = 0.5f), Color.Transparent),
                                    center = cloudBand1,
                                    radius = 95f
                                ),
                                center = cloudBand1,
                                radius = 95f
                            )

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFECEFF1).copy(alpha = 0.6f), Color(0xFF90A4AE).copy(alpha = 0.35f), Color.Transparent),
                                    center = cloudBand2,
                                    radius = 80f
                                ),
                                center = cloudBand2,
                                radius = 80f
                            )
                        }

                        MapLayerType.PRECIPITATION_MAP -> {
                            val rainZone = Offset(center.x - 30f + timeOffsetFactor, center.y + 20f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(RadarMagenta.copy(alpha = 0.8f), RadarRed.copy(alpha = 0.6f), RadarYellow.copy(alpha = 0.4f), Color.Transparent),
                                    center = rainZone,
                                    radius = 85f
                                ),
                                center = rainZone,
                                radius = 85f
                            )
                        }
                    }

                    // Radar Center Marker
                    drawCircle(
                        color = Color.White,
                        radius = 4.5f,
                        center = center
                    )
                    drawCircle(
                        color = SophisticatedIceBluePrimary,
                        radius = 8f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                // Top Floating Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Color(0xFF3F474F)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "📡 Frame: ${timeLabels[timeIndex]} (${if (timeIndex == 3) "Live" else "NWP Composite"})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedIceBluePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Range Scale Indicator
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "150 km Radius",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Scrubber & Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("radar_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause Animation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Time step chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    timeLabels.forEachIndexed { idx, label ->
                        val isSelected = timeIndex == idx
                        Surface(
                            onClick = {
                                timeIndex = idx
                                isPlaying = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("time_chip_$idx")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reflectivity dBZ Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reflectivity (dBZ):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LegendItem(color = RadarCyan, label = "Light")
                    LegendItem(color = RadarGreen, label = "Mod")
                    LegendItem(color = RadarYellow, label = "Hvy")
                    LegendItem(color = RadarRed, label = "Int")
                    LegendItem(color = RadarMagenta, label = "Sev")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

