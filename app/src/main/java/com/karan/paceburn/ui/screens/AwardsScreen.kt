package com.karan.paceburn.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karan.paceburn.R
import com.karan.paceburn.data.Award
import com.karan.paceburn.data.AwardCategory
import com.karan.paceburn.data.AwardManager
import com.karan.paceburn.data.PreferencesManager
import java.text.NumberFormat

@Composable
fun AwardsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val awardManager = remember { AwardManager(preferencesManager) }

    val awards = remember { awardManager.calculateAwards() }
    val userLevelInfo = remember(awards) { awardManager.calculateUserLevel(awards) }

    var selectedCategory by remember { mutableStateOf(AwardCategory.ALL) }
    var selectedAwardForDialog by remember { mutableStateOf<Award?>(null) }

    val filteredAwards = remember(awards, selectedCategory) {
        if (selectedCategory == AwardCategory.ALL) awards
        else awards.filter { it.category == selectedCategory }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header - Level & XP Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.paceburn_legend),
                                    contentDescription = "PaceBurn Legend",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "LEVEL ${userLevelInfo.level}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = userLevelInfo.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${userLevelInfo.unlockedBadgeCount} / ${userLevelInfo.totalBadgeCount} Unlocked",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${userLevelInfo.currentLevelXp} / ${userLevelInfo.xpForNextLevel} XP (${userLevelInfo.totalXp} Total XP)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { userLevelInfo.currentLevelXp.toFloat() / userLevelInfo.xpForNextLevel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Category Filter Row
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AwardCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
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

        // Badges Grid Items
        items(filteredAwards) { award ->
            BadgeCard(
                award = award,
                onClick = { selectedAwardForDialog = award }
            )
        }
    }

    // Badge Detail Modal Dialog
    selectedAwardForDialog?.let { award ->
        BadgeDetailDialog(
            award = award,
            onDismiss = { selectedAwardForDialog = null }
        )
    }
}

@Composable
fun BadgeCard(
    award: Award,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (award.isUnlocked) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (award.isUnlocked) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Circle Icon / Drawable Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (award.isUnlocked) {
                            Brush.linearGradient(
                                listOf(Color(0xFFFF8008), Color(0xFFFFC837))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (award.drawableResId != null) {
                    Image(
                        painter = painterResource(id = award.drawableResId),
                        contentDescription = award.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (!award.isUnlocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) }) else null
                    )
                    if (!award.isUnlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    if (award.isUnlocked) {
                        Icon(
                            imageVector = getBadgeIcon(award.iconName),
                            contentDescription = award.title,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF616161),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = award.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                color = if (award.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (award.isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+${award.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { award.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(
    award: Award,
    onDismiss: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (award.isUnlocked) {
                                Brush.linearGradient(listOf(Color(0xFFFF8008), Color(0xFFFFC837)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (award.drawableResId != null) {
                        Image(
                            painter = painterResource(id = award.drawableResId),
                            contentDescription = award.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            colorFilter = if (!award.isUnlocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) }) else null
                        )
                        if (!award.isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = if (award.isUnlocked) getBadgeIcon(award.iconName) else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = award.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (award.isUnlocked) "UNLOCKED BADGE (+${award.xpReward} XP)" else "LOCKED MILESTONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (award.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = award.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (award.isUnlocked) {
                    Text(
                        text = "Earned on ${award.unlockDate ?: "Today"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    val formattedCurr = NumberFormat.getNumberInstance(locale).format(award.currentValue.toInt())
                    val formattedReq = NumberFormat.getNumberInstance(locale).format(award.requiredValue.toInt())

                    Text(
                        text = "Progress: $formattedCurr / $formattedReq (${(award.progressFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { award.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

fun getBadgeIcon(iconName: String): ImageVector {
    return when (iconName) {
        "DirectionsWalk" -> Icons.AutoMirrored.Filled.DirectionsRun
        "FlashOn" -> Icons.Default.FlashOn
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "MilitaryTech" -> Icons.Default.MilitaryTech
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "WorkspacePremium" -> Icons.Default.WorkspacePremium
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "Whatshot" -> Icons.Default.Whatshot
        "Verified" -> Icons.Default.Verified
        "Thunderstorm" -> Icons.Default.Thunderstorm
        "Map" -> Icons.Default.Map
        "DirectionsRun" -> Icons.AutoMirrored.Filled.DirectionsRun
        "LocationOn" -> Icons.Default.LocationOn
        "Star" -> Icons.Default.Star
        else -> Icons.Default.EmojiEvents
    }
}
