package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils

data class ChartPoint(
    val label: String,
    val value: Float,
    val timestamp: Long = 0L
)

@Composable
fun VolumeTrendChart(
    points: List<ChartPoint>,
    unitLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Volume Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (points.isNotEmpty()) {
                    val maxVal = points.maxOf { it.value }
                    Text(
                        text = "Peak: ${maxVal.toInt()} $unitLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (points.isEmpty()) "Complete workouts to view your volume progression chart."
                        else "Need at least 2 workouts to plot a trend line.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 20f
                    val chartHeight = height - paddingBottom

                    val maxVal = points.maxOf { it.value }.coerceAtLeast(1f)
                    val minVal = points.minOf { it.value }.coerceAtLeast(0f)
                    val range = (maxVal - minVal).coerceAtLeast(1f)

                    // Draw grid lines
                    drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, chartHeight / 2), Offset(width, chartHeight / 2), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, chartHeight), Offset(width, chartHeight), strokeWidth = 1.dp.toPx())

                    val stepX = width / (points.size - 1)
                    val path = Path()

                    val offsets = points.mapIndexed { index, point ->
                        val x = index * stepX
                        val normalized = (point.value - minVal) / range
                        val y = chartHeight - (normalized * (chartHeight - 16f)) - 8f
                        Offset(x, y)
                    }

                    offsets.forEachIndexed { i, offset ->
                        if (i == 0) path.moveTo(offset.x, offset.y)
                        else path.lineTo(offset.x, offset.y)
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw point dots
                    offsets.forEach { offset ->
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                    }
                }

                // First and last date labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = points.first().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = points.last().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
