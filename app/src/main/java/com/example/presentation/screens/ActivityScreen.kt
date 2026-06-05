package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayHealthLog.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isWorkoutSimulating by viewModel.isWorkoutSimulating.collectAsState()

    var showManualAddDialog by remember { mutableStateOf(false) }
    var stepsInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }

    // Heartbeat BPM simulated value
    val liveBpm by viewModel.simulatedHeartRate.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Activity Tracker",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Monitor steps, GPS exercise & cardio logs",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { showManualAddDialog = true },
                        modifier = Modifier
                            .background(TealAccent.copy(alpha = 0.15f), CircleShape)
                            .testTag("add_manual_activity_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Activity", tint = TealAccent)
                    }
                }
            }

            // --- GPS WORKOUT SIMULATION DASHBOARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GPS ROUTE EXERCISE HUD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealAccent,
                                letterSpacing = 1.sp
                            )
                            Badge(
                                containerColor = if (isWorkoutSimulating) ActivityGreen else Color.DarkGray
                            ) {
                                Text(
                                    text = if (isWorkoutSimulating) "LIVE" else "STANDBY",
                                    color = if (isWorkoutSimulating) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // GPS Map Dashboard Sim
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isWorkoutSimulating) Icons.Default.Map else Icons.Default.LocationOff,
                                    contentDescription = "Simulated Map GPS",
                                    tint = if (isWorkoutSimulating) TealAccent else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (isWorkoutSimulating) {
                                    Text(
                                        text = "Simulating GPS Trail: San Francisco Park, CA",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Lat: 37.7749 • Lng: -122.4194",
                                        color = TealAccent,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                } else {
                                    Text(
                                        text = "GPS Simulation and accelerometer inactive",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Start exercise using simulation button",
                                        color = Color.DarkGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Workout HUD dials
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cardio Rate", color = Color.Gray, fontSize = 11.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Pulse", tint = HeartRateRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$liveBpm BPM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sim Speed", color = Color.Gray, fontSize = 11.sp)
                                Text(if (isWorkoutSimulating) "5.4 km/h" else "0.0 km/h", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pace Ratio", color = Color.Gray, fontSize = 11.sp)
                                Text(if (isWorkoutSimulating) "11'10\" /km" else "0'00\"", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (isWorkoutSimulating) {
                                    viewModel.stopWorkoutSimulation()
                                } else {
                                    viewModel.startWorkoutSimulation()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWorkoutSimulating) HeartRateRed else TealAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isWorkoutSimulating) Icons.Default.DirectionsRun else Icons.Default.PlayArrow,
                                contentDescription = "Trigger simulation button",
                                tint = if (isWorkoutSimulating) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isWorkoutSimulating) "Finish Walk Sim" else "Start Exercise Sim",
                                color = if (isWorkoutSimulating) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- METRICS SUMMARY ---
            item {
                Text(
                    text = "TODAY'S CARDIO STATS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = "Steps icon", tint = ActivityGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Steps", color = Color.Gray, fontSize = 12.sp)
                            Text("${todayLog.steps}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories icon", tint = HeartRateRed)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Est. Calories", color = Color.Gray, fontSize = 12.sp)
                            Text("${todayLog.calories} kcal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }

            // --- RECENT EXERCISE LOGS ---
            item {
                Text(
                    text = "HISTORIAL LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (allLogs.isEmpty()) {
                item {
                    Text(
                        text = "No history recorded. Complete exercises to populate logs.",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                items(allLogs.take(5)) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ActivityGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active Log", tint = ActivityGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = log.date, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(text = "${log.steps} Steps • ${log.calories} kcal", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = String.format("%.2f km", log.distanceKm),
                                color = TealAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // --- MANUAL METRIC INPUT DIALOG ---
        if (showManualAddDialog) {
            AlertDialog(
                onDismissRequest = { showManualAddDialog = false },
                title = { Text("Log Activity Manually", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add custom exercise metrics to today's dashboard", color = Color.Gray, fontSize = 13.sp)
                        
                        OutlinedTextField(
                            value = stepsInput,
                            onValueChange = { stepsInput = it },
                            label = { Text("Steps to add", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = { Text("e.g. 5000", color = Color.DarkGray) },
                            modifier = Modifier.fillMaxWidth().testTag("manual_steps_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        onClick = {
                            val stepVal = stepsInput.toIntOrNull() ?: 0
                            if (stepVal > 0) {
                                viewModel.addManualSteps(stepVal)
                            }
                            stepsInput = ""
                            showManualAddDialog = false
                        },
                        modifier = Modifier.testTag("manual_steps_add_confirm")
                    ) {
                        Text("Add Metrics", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualAddDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}
