package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HistoricalTrendEntity
import com.example.ui.theme.SophisticatedIceBluePrimary

@Composable
fun ClimateTrendChart(
    trends: List<HistoricalTrendEntity>,
    locationName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("climate_trend_card"),
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
            Text(
                text = "Climate Trends & Monsoon Deviation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rainfall (mm) & Temperature History for $locationName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                if (trends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No historical climate data available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bottomY = canvasHeight - 24f
                        val topY = 20f
                        val availableHeight = bottomY - topY

                        val maxRainfall = trends.maxOfOrNull { maxOf(it.rainfallActualMm, it.rainfallNormalMm) } ?: 300f
                        val stepX = canvasWidth / (trends.size + 0.5f)

                        trends.forEachIndexed { index, item ->
                            val x = (index + 0.5f) * stepX

                            // Bar for Normal Rainfall
                            val normalBarHeight = (item.rainfallNormalMm / maxRainfall) * availableHeight
                            val normalBarY = bottomY - normalBarHeight
                            drawRoundRect(
                                color = Color(0xFF4A5568).copy(alpha = 0.6f),
                                topLeft = Offset(x - 14f, normalBarY),
                                size = Size(12f, normalBarHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Bar for Actual Rainfall
                            val actualBarHeight = (item.rainfallActualMm / maxRainfall) * availableHeight
                            val actualBarY = bottomY - actualBarHeight
                            drawRoundRect(
                                color = SophisticatedIceBluePrimary,
                                topLeft = Offset(x + 2f, actualBarY),
                                size = Size(12f, actualBarHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Draw text month label
                            drawContext.canvas.nativeCanvas.drawText(
                                item.monthName,
                                x - 4f,
                                canvasHeight - 4f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.LTGRAY
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SophisticatedIceBluePrimary))
                    Text("Actual Rainfall (mm)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4A5568)))
                    Text("Normal Baseline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Highlight Cards for Monsoon Deviation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trends.takeLast(2).forEach { trend ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${trend.monthName}: ${trend.rainfallActualMm.toInt()} mm (Norm: ${trend.rainfallNormalMm.toInt()} mm)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = trend.anomalyDescription,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

