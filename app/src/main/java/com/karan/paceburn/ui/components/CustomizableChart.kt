package com.karan.paceburn.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karan.paceburn.data.DailyRecord
import java.text.NumberFormat

enum class ChartMetric(val label: String, val unit: String) {
    STEPS("Steps", "steps"),
    CALORIES("Calories", "kcal"),
    DISTANCE("Distance", "km")
}

enum class ChartType(val label: String) {
    BAR("Bar Chart"),
    LINE("Line Chart")
}

enum class ChartColorTheme(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    OCEAN_FLAME("Ocean Flame", Color(0xFF0083B0), Color(0xFF00B4DB)),
    BURN_ORANGE("Burn Orange", Color(0xFFFF512F), Color(0xFFF09819)),
    EMERALD("Emerald", Color(0xFF11998E), Color(0xFF38EF7D)),
    NEON_PURPLE("Neon Violet", Color(0xFF8E2DE2), Color(0xFF4A00E0))
}

@Composable
fun CustomizableChart(
    history: List<DailyRecord>,
    metric: ChartMetric,
    chartType: ChartType,
    colorTheme: ChartColorTheme,
    showGoalLine: Boolean,
    stepGoal: Int,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    val displayList = remember(history) { history.reversed() }
    var selectedIndex by remember { mutableIntStateOf(displayList.lastIndex) }

    val locale = LocalConfiguration.current.locales[0]

    // Determine values according to selected metric
    val values = remember(displayList, metric) {
        displayList.map { record ->
            when (metric) {
                ChartMetric.STEPS -> record.steps.toFloat()
                ChartMetric.CALORIES -> record.calories
                ChartMetric.DISTANCE -> record.distanceKm
            }
        }
    }

    val maxValue = remember(values, metric, stepGoal) {
        val maxRecorded = values.maxOrNull() ?: 100f
        val goalValue = when (metric) {
            ChartMetric.STEPS -> stepGoal.toFloat()
            ChartMetric.CALORIES -> stepGoal * 0.04f
            ChartMetric.DISTANCE -> (stepGoal * 75) / 100000.0f
        }
        (maxOf(maxRecorded, goalValue) * 1.15f).coerceAtLeast(10f)
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val goalLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected Date Tooltip Card
        val selectedRecord = displayList.getOrNull(selectedIndex)
        if (selectedRecord != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedRecord.dayOfWeek}, ${selectedRecord.date}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selected Day Summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val formattedVal = when (metric) {
                            ChartMetric.STEPS -> NumberFormat.getNumberInstance(locale).format(selectedRecord.steps)
                            ChartMetric.CALORIES -> String.format(locale, "%.1f", selectedRecord.calories)
                            ChartMetric.DISTANCE -> String.format(locale, "%.2f", selectedRecord.distanceKm)
                        }
                        Text(
                            text = "$formattedVal ${metric.unit}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorTheme.primaryColor
                        )
                    }
                }
            }
        }

        // Custom Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayList) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val itemCount = displayList.size
                            if (itemCount > 0) {
                                val itemWidth = width / itemCount
                                val index = (tapOffset.x / itemWidth)
                                    .toInt()
                                    .coerceIn(0, itemCount - 1)
                                selectedIndex = index
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val count = displayList.size
                if (count == 0) return@Canvas

                val bottomY = canvasHeight - 24.dp.toPx()
                val topY = 10.dp.toPx()
                val usableHeight = bottomY - topY

                // Draw horizontal grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = topY + (usableHeight * i / gridLines)
                    drawLine(
                        color = surfaceVariant.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Goal Line if enabled
                if (showGoalLine) {
                    val goalValue = when (metric) {
                        ChartMetric.STEPS -> stepGoal.toFloat()
                        ChartMetric.CALORIES -> stepGoal * 0.04f
                        ChartMetric.DISTANCE -> (stepGoal * 75) / 100000.0f
                    }
                    val goalY = bottomY - (usableHeight * (goalValue / maxValue))
                    if (goalY in topY..bottomY) {
                        drawLine(
                            color = goalLineColor,
                            start = Offset(0f, goalY),
                            end = Offset(canvasWidth, goalY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }

                val colWidth = canvasWidth / count

                if (chartType == ChartType.BAR) {
                    val barWidth = (colWidth * 0.55f).coerceAtLeast(8.dp.toPx())

                    values.forEachIndexed { i, valItem ->
                        val xCenter = i * colWidth + (colWidth / 2)
                        val barHeight = (usableHeight * (valItem / maxValue)) * animProgress
                        val barTop = bottomY - barHeight
                        val left = xCenter - (barWidth / 2)

                        val isSelected = i == selectedIndex
                        val barBrush = Brush.verticalGradient(
                            colors = if (isSelected) listOf(colorTheme.secondaryColor, colorTheme.primaryColor)
                            else listOf(colorTheme.primaryColor.copy(alpha = 0.8f), colorTheme.primaryColor.copy(alpha = 0.4f))
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, barTop),
                            size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                } else {
                    // LINE CHART MODE
                    val path = Path()
                    val areaPath = Path()

                    val points = values.mapIndexed { i, valItem ->
                        val x = i * colWidth + (colWidth / 2)
                        val y = bottomY - ((usableHeight * (valItem / maxValue)) * animProgress)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        path.moveTo(points.first().x, points.first().y)
                        areaPath.moveTo(points.first().x, bottomY)
                        areaPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val control1 = Offset((p1.x + p2.x) / 2, p1.y)
                            val control2 = Offset((p1.x + p2.x) / 2, p2.y)
                            path.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                            areaPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                        }

                        areaPath.lineTo(points.last().x, bottomY)
                        areaPath.close()

                        // Draw Gradient Area under Line
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorTheme.primaryColor.copy(alpha = 0.35f),
                                    colorTheme.primaryColor.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Draw Curved Line
                        drawPath(
                            path = path,
                            color = colorTheme.primaryColor,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Node Points
                        points.forEachIndexed { i, pt ->
                            val isSelected = i == selectedIndex
                            drawCircle(
                                color = if (isSelected) colorTheme.secondaryColor else colorTheme.primaryColor,
                                radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Labels (Days)
        val displayCount = displayList.size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            displayList.forEachIndexed { index, record ->
                // Show label for every item if <= 7, else skip some labels for clarity
                val showLabel = displayCount <= 7 || index % (displayCount / 6).coerceAtLeast(1) == 0 || index == displayList.lastIndex
                if (showLabel) {
                    Text(
                        text = record.dayOfWeek,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == selectedIndex) colorTheme.primaryColor else onSurfaceVariant
                    )
                }
            }
        }
    }
}
