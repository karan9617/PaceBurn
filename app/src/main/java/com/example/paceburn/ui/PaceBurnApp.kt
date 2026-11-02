package com.example.paceburn.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paceburn.ui.components.CircularProgressRing
import com.example.paceburn.ui.components.PermissionBanner
import com.example.paceburn.ui.components.SettingsDialog
import com.example.paceburn.ui.components.StatsGrid
import com.example.paceburn.ui.components.WeeklyHistoryCard
import com.example.paceburn.ui.screens.AwardsScreen
import com.example.paceburn.ui.screens.ReportScreen
import com.example.paceburn.ui.screens.WorkoutMapScreen

enum class AppTab(val label: String) {
    TODAY("Today"),
    MAP("Workout"),
    REPORTS("Reports"),
    AWARDS("Awards")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaceBurnApp(
    viewModel: StepViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.TODAY) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        } else {
            true
        }
        viewModel.setPermissionGranted(activityGranted)
    }

    fun launchPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !uiState.hasPermission) {
            launchPermissions()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PaceBurn",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.isTracking) "Live Fitness Tracker" else "Tracking Paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isTracking) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.TODAY,
                    onClick = { selectedTab = AppTab.TODAY },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                    label = { Text("Today") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.MAP,
                    onClick = { selectedTab = AppTab.MAP },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Workout Map") },
                    label = { Text("Workout") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.REPORTS,
                    onClick = { selectedTab = AppTab.REPORTS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.AWARDS,
                    onClick = { selectedTab = AppTab.AWARDS },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Awards") },
                    label = { Text("Awards") }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                AppTab.TODAY -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Permission Warning Banner if missing
                        if (!uiState.hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            item {
                                PermissionBanner(
                                    onRequestPermission = {
                                        launchPermissions()
                                    }
                                )
                            }
                        }

                        // Main Circular Step Counter Progress Ring
                        item {
                            CircularProgressRing(
                                steps = uiState.steps,
                                goal = uiState.goal
                            )
                        }

                        // Action Controls (Pause/Resume, Reset, Quick Add)
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleTracking() },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (uiState.isTracking) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (uiState.isTracking) "Pause" else "Start",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.resetTodaySteps() },
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Step Simulator Chips (ideal for testing or emulator)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Simulate:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    AssistChip(
                                        onClick = { viewModel.addSteps(100) },
                                        label = { Text("+100") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    AssistChip(
                                        onClick = { viewModel.addSteps(500) },
                                        label = { Text("+500") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    AssistChip(
                                        onClick = { viewModel.addSteps(1000) },
                                        label = { Text("+1k") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // Detailed Statistics Grid
                        item {
                            StatsGrid(
                                calories = uiState.calories,
                                distanceKm = uiState.distanceKm,
                                activeTimeSeconds = uiState.activeTimeSeconds,
                                steps = uiState.steps
                            )
                        }

                        // Recent Activity History Section
                        if (uiState.history.isNotEmpty()) {
                            item {
                                WeeklyHistoryCard(
                                    history = uiState.history,
                                    goal = uiState.goal
                                )
                            }
                        }
                    }
                }

                AppTab.MAP -> {
                    WorkoutMapScreen(
                        userWeightKg = uiState.userWeightKg
                    )
                }

                AppTab.REPORTS -> {
                    ReportScreen(
                        history = uiState.history,
                        stepGoal = uiState.goal,
                        todaySteps = uiState.steps,
                        todayCalories = uiState.calories,
                        todayDistanceKm = uiState.distanceKm
                    )
                }

                AppTab.AWARDS -> {
                    AwardsScreen()
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentGoal = uiState.goal,
            currentWeight = uiState.userWeightKg,
            currentStepLength = uiState.stepLengthCm,
            onDismiss = { showSettingsDialog = false },
            onSave = { newGoal, newWeight, newStepLength ->
                viewModel.updateStepGoal(newGoal)
                viewModel.updateUserProfile(newWeight, newStepLength)
                showSettingsDialog = false
            }
        )
    }
}
