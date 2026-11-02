package com.example.paceburn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    currentGoal: Int,
    currentWeight: Float,
    currentStepLength: Float,
    onDismiss: () -> Unit,
    onSave: (newGoal: Int, newWeight: Float, newStepLength: Float) -> Unit
) {
    var goalState by remember { mutableFloatStateOf(currentGoal.toFloat()) }
    var weightText by remember { mutableStateOf(currentWeight.toString()) }
    var stepLengthText by remember { mutableStateOf(currentStepLength.toString()) }

    val presetGoals = listOf(5000, 8000, 10000, 12000, 15000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "PaceBurn Goal & Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Daily Step Goal: ${goalState.toInt()} steps",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = goalState,
                    onValueChange = { goalState = it },
                    valueRange = 2000f..30000f,
                    steps = 27
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetGoals.forEach { preset ->
                        FilterChip(
                            selected = goalState.toInt() == preset,
                            onClick = { goalState = preset.toFloat() },
                            label = { Text(text = "${preset / 1000}k") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Body Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = stepLengthText,
                    onValueChange = { stepLengthText = it },
                    label = { Text("Step Length (cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalWeight = weightText.toFloatOrNull() ?: currentWeight
                    val finalStepLength = stepLengthText.toFloatOrNull() ?: currentStepLength
                    onSave(goalState.toInt(), finalWeight, finalStepLength)
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
