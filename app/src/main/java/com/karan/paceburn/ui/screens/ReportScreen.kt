package com.karan.paceburn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karan.paceburn.data.DailyRecord
import com.karan.paceburn.ui.components.ChartColorTheme
import com.karan.paceburn.ui.components.ChartMetric
import com.karan.paceburn.ui.components.ChartType
import com.karan.paceburn.ui.components.CustomizableChart
import com.karan.paceburn.ui.components.MonthCalendarCard
import java.text.NumberFormat

@Composable
fun ReportScreen(
    history: List<DailyRecord>,
    stepGoal: Int,
    modifier: Modifier = Modifier,
    todaySteps: Int = 0,
    todayCalories: Float = 0f,
    todayDistanceKm: Float = 0f
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.STEPS) }
    var selectedTimeframeDays by remember { mutableIntStateOf(7) }
    var chartType by remember { mutableStateOf(ChartType.BAR) }
    var colorTheme by remember { mutableStateOf(ChartColorTheme.BURN_ORANGE) }
    var showGoalLine by remember { mutableStateOf(true) }

    val locale = LocalConfiguration.current.locales[0]

    val filteredHistory = remember(history, selectedTimeframeDays) {
        history.take(selectedTimeframeDays)
    }

    // Analytics summary calculations
    val totalSteps = filteredHistory.sumOf { it.steps }
    val totalCalories = filteredHistory.fold(0f) { acc, r -> acc + r.calories }
    val totalDistance = filteredHistory.fold(0f) { acc, r -> acc + r.distanceKm }

    val count = filteredHistory.size.coerceAtLeast(1)
    val avgMetricValue = when (selectedMetric) {
        ChartMetric.STEPS -> totalSteps / count
        ChartMetric.CALORIES -> totalCalories / count
        ChartMetric.DISTANCE -> totalDistance / count
    }

    val bestRecord = filteredHistory.maxByOrNull {
        when (selectedMetric) {
            ChartMetric.STEPS -> it.steps.toFloat()
            ChartMetric.CALORIES -> it.calories
            ChartMetric.DISTANCE -> it.distanceKm
        }
    }

    val goalHitCount = filteredHistory.count { it.steps >= stepGoal }
    val goalSuccessPercent = ((goalHitCount.toFloat() / count) * 100).toInt()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Activity Reports & Trends",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your burn progress and walking statistics over time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Metric Selector (Steps, Calories, Distance)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartMetric.entries.forEach { metric ->
                    val isSelected = selectedMetric == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMetric = metric },
                        label = {
                            Text(
                                text = metric.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            val icon = when (metric) {
                                ChartMetric.STEPS -> Icons.AutoMirrored.Filled.DirectionsWalk
                                ChartMetric.CALORIES -> Icons.Default.LocalFireDepartment
                                ChartMetric.DISTANCE -> Icons.Default.Straighten
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Timeframe Selector (7 Days / 30 Days) & Chart Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timeframe",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = selectedTimeframeDays == 7,
                                onClick = { selectedTimeframeDays = 7 },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("7 Days")
                            }

                            SegmentedButton(
                                selected = selectedTimeframeDays == 30,
                                onClick = { selectedTimeframeDays = 30 },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("30 Days")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Customization Options: Bar vs Line & Goal Line Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = chartType == ChartType.BAR,
                                onClick = { chartType = ChartType.BAR },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            ) {
                                Text("Bar")
                            }

                            SegmentedButton(
                                selected = chartType == ChartType.LINE,
                                onClick = { chartType = ChartType.LINE },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            ) {
                                Text("Line")
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Goal Line",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = showGoalLine,
                                onCheckedChange = { showGoalLine = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Theme Selector Palette
                    Text(
                        text = "Chart Color Palette",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChartColorTheme.entries.forEach { theme ->
                            val isSelected = colorTheme == theme
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                                    .clickable { colorTheme = theme },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Main Interactive Chart Display Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedMetric.label} Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap on any day to inspect details",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    CustomizableChart(
                        history = filteredHistory,
                        metric = selectedMetric,
                        chartType = chartType,
                        colorTheme = colorTheme,
                        showGoalLine = showGoalLine,
                        stepGoal = stepGoal
                    )
                }
            }
        }

        // Monthly Activity Calendar View
        item {
            MonthCalendarCard(
                history = history,
                stepGoal = stepGoal,
                todaySteps = todaySteps,
                todayCalories = todayCalories,
                todayDistanceKm = todayDistanceKm
            )
        }

        // Analytics Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Summary & Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Average Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = colorTheme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Avg",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val formattedAvg = when (selectedMetric) {
                                ChartMetric.STEPS -> NumberFormat.getNumberInstance(locale).format((avgMetricValue as Number).toInt())
                                ChartMetric.CALORIES -> String.format(locale, "%.1f", (avgMetricValue as Number).toFloat())
                                ChartMetric.DISTANCE -> String.format(locale, "%.2f", (avgMetricValue as Number).toFloat())
                            }
                            Text(
                                text = formattedAvg,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedMetric.unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Best Day Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Best Record",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val bestValText = if (bestRecord != null) {
                                when (selectedMetric) {
                                    ChartMetric.STEPS -> NumberFormat.getNumberInstance(locale).format(bestRecord.steps)
                                    ChartMetric.CALORIES -> String.format(locale, "%.0f", bestRecord.calories)
                                    ChartMetric.DISTANCE -> String.format(locale, "%.2f", bestRecord.distanceKm)
                                }
                            } else "0"

                            Text(
                                text = bestValText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bestRecord?.let { "${it.dayOfWeek} (${it.date})" } ?: "N/A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Goal Completion Rate Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Goal Completion Rate",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "$goalSuccessPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { goalSuccessPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$goalHitCount out of ${filteredHistory.size} days met your $stepGoal step goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
