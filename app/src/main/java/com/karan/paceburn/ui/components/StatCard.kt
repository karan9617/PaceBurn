package com.karan.paceburn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StatsGrid(
    calories: Float,
    distanceKm: Float,
    activeTimeSeconds: Long,
    steps: Int,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val mins = activeTimeSeconds / 60
    val formattedTime = if (mins >= 60) {
        val hrs = mins / 60
        val remainingMins = mins % 60
        "${hrs}h ${remainingMins}m"
    } else {
        "${mins}m ${activeTimeSeconds % 60}s"
    }

    val paceParen = if (mins > 0) (steps / mins.coerceAtLeast(1)) else 0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Calories",
                value = String.format(locale, "%.1f", calories),
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                iconContainerColor = Color(0xFFFFECEB),
                iconTintColor = Color(0xFFE53935),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Distance",
                value = String.format(locale, "%.2f", distanceKm),
                unit = "km",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconContainerColor = Color(0xFFE8F5E9),
                iconTintColor = Color(0xFF43A047),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Active Time",
                value = formattedTime,
                unit = "",
                icon = Icons.Default.Timer,
                iconContainerColor = Color(0xFFE3F2FD),
                iconTintColor = Color(0xFF1E88E5),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Pace",
                value = paceParen.toString(),
                unit = "steps/m",
                icon = Icons.Default.Speed,
                iconContainerColor = Color(0xFFFFF8E1),
                iconTintColor = Color(0xFFFFB300),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
