package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DailyHealthLog
import com.example.data.database.Medication
import com.example.data.database.UserProfile
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    onNavigateToMetric: (String) -> Unit, // "activity", "water", "sleep", "medication", "profile", "sos"
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val todayLog by viewModel.todayHealthLog.collectAsState()
    val medicationsList by viewModel.medications.collectAsState()
    val medLogsToday by viewModel.todayMedicationLogs.collectAsState()

    val isWorkoutSimulating by viewModel.isWorkoutSimulating.collectAsState()
    val simulatedSteps by viewModel.simulatedWorkoutSteps.collectAsState()
    val simulatedBpm by viewModel.simulatedHeartRate.collectAsState()

    val sosState by viewModel.sosState.collectAsState()

    val scrollState = rememberScrollState()

    // Heartbeat Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 80.dp) // Space for navigation
        ) {
            // --- HEADER GREETING ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hello, ${profile.name}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Let's crushed your goals today!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Quick SOS Red Alert Indicator Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(HeartRateRed.copy(alpha = 0.15f))
                        .clickable { onNavigateToMetric("sos") }
                        .testTag("sos_direct_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Trigger Emergency SOS",
                        tint = HeartRateRed,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(if (sosState is HealthViewModel.SosState.Active) pulseScale else 1f)
                    )
                }
            }

            // --- SOS EMERGENCY BANNER (If Active) ---
            if (sosState !is HealthViewModel.SosState.Idle) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = HeartRateRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "SOS Siren",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .scale(pulseScale)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EMERGENCY SOS BROADCAST",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            val statusText = when (val state = sosState) {
                                is HealthViewModel.SosState.Sending -> "Sending coordinate ping in ${state.countdownSec}s..."
                                is HealthViewModel.SosState.Active -> "Live location broadcast active!"
                                else -> ""
                            }
                            Text(
                                text = statusText,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.cancelSos() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CANCEL", color = HeartRateRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- SIMULATED LIVE SENSOR BANNER (If Active) ---
            if (isWorkoutSimulating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = TealDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = "Active Workout",
                                tint = TealAccent,
                                modifier = Modifier
                                    .size(36.dp)
                                    .scale(pulseScale)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Simulating Walking", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("+$simulatedSteps Steps ($simulatedBpm BPM)", color = TealAccent, fontSize = 13.sp)
                            }
                        }
                        Button(
                            onClick = { viewModel.stopWorkoutSimulation() },
                            colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Stop", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- MAIN STEPS HERO DIAL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { onNavigateToMetric("activity") }
                    .testTag("steps_hud_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TODAY'S STEP PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(190.dp)
                    ) {
                        val progressFraction = if (profile.stepsGoal > 0) {
                            (todayLog.steps.toFloat() / profile.stepsGoal.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Canvas(modifier = Modifier.size(170.dp)) {
                            // Dark background arc
                            drawArc(
                                color = Color.White.copy(alpha = 0.05f),
                                startAngle = -220f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Filled accent arc
                            drawArc(
                                color = ActivityGreen,
                                startAngle = -220f,
                                sweepAngle = 260f * progressFraction,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = "Footsteps",
                                tint = ActivityGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = todayLog.steps.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Goal: ${profile.stepsGoal}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Miniature stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Calories", color = Color.Gray, fontSize = 11.sp)
                            Text("${todayLog.calories} kcal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Distance", color = Color.Gray, fontSize = 11.sp)
                            Text(String.format("%.2f km", todayLog.distanceKm), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- TWO COLUMN GRID WIDGETS ---
            ContextWidgetsGrid(
                todayLog = todayLog,
                profile = profile,
                pulseScale = pulseScale,
                onNavigate = onNavigateToMetric,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- TODAY'S MEDICATION REMINDER QUICK-VIEW ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { onNavigateToMetric("medication") }
                    .testTag("medications_hud_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Medications",
                                tint = TealAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY MEDS REMINDER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        Text(
                            text = "${medLogsToday.size}/${medicationsList.size} taken",
                            fontSize = 12.sp,
                            color = TealAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (medicationsList.isEmpty()) {
                        Text(
                            text = "No medications scheduled. Touch to add reminders.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        medicationsList.take(3).forEach { med ->
                            val isTaken = medLogsToday.any { it.medicineId == med.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Task status",
                                        tint = if (isTaken) ActivityGreen else Color.Gray,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { viewModel.recordMedIntake(med, !isTaken) }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = med.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isTaken) Color.White.copy(alpha = 0.5f) else Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${med.dosage} • ${med.time}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                        if (medicationsList.size > 3) {
                            Text(
                                text = "+ ${medicationsList.size - 3} more reminders",
                                color = TealAccent,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- SIMULATED WALK FLOATING TRIGGER CONTROLS ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp) // Adjusted padding to sit above main navigation bar
        ) {
            FloatingActionButton(
                onClick = {
                    if (isWorkoutSimulating) {
                        viewModel.stopWorkoutSimulation()
                    } else {
                        viewModel.startWorkoutSimulation()
                    }
                },
                containerColor = if (isWorkoutSimulating) HeartRateRed else TealAccent,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .testTag("workout_simulator_fab")
            ) {
                Icon(
                    imageVector = if (isWorkoutSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Simulate Accelerated Step Exercise",
                    tint = if (isWorkoutSimulating) Color.White else Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContextWidgetsGrid(
    todayLog: DailyHealthLog,
    profile: UserProfile,
    pulseScale: Float,
    onNavigate: (String) -> Unit,
    viewModel: HealthViewModel
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val widgetWidth = remember { mutableStateOf(0.dp) }

        // --- HYDRATION WIDGET ---
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigate("water") }
                .testTag("hydration_widget_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water",
                        tint = HydrationBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "+250ml",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydrationBlue,
                        modifier = Modifier
                            .background(HydrationBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.logWater(250) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Hydration", color = Color.Gray, fontSize = 12.sp)
                Text("${todayLog.waterMl} ml", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = if (profile.waterGoalMl > 0) (todayLog.waterMl.toFloat() / profile.waterGoalMl).coerceIn(0f, 1f) else 0f,
                    color = HydrationBlue,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }

        // --- SLEEP WIDGET ---
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigate("sleep") }
                .testTag("sleep_widget_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Sleep",
                    tint = SleepLilac,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tonight's Sleep", color = Color.Gray, fontSize = 12.sp)
                val hours = todayLog.sleepMinutes / 60
                val minutesLeft = todayLog.sleepMinutes % 60
                Text(
                    text = if (hours > 0) "${hours}h ${minutesLeft}m" else "${todayLog.sleepMinutes}m",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = if (profile.sleepGoalMinutes > 0) (todayLog.sleepMinutes.toFloat() / profile.sleepGoalMinutes).coerceIn(0f, 1f) else 0f,
                    color = SleepLilac,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }

        // --- HEART RATE WIDGET ---
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigate("activity") }, // Activity has deep health stats
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Pulse",
                        tint = HeartRateRed,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(pulseScale)
                    )
                    Text(
                        text = "Realtime",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Heart Rate", color = Color.Gray, fontSize = 12.sp)
                Text("${todayLog.heartRate} bpm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Resting rate: 68bpm", color = Color.Gray, fontSize = 11.sp)
            }
        }

        // --- WEIGHT WIDGET ---
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigate("profile") }, // Profile edits demographic weights
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = "Weight",
                    tint = WeightOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Weight Log", color = Color.Gray, fontSize = 12.sp)
                Text("${todayLog.weightKg} kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                // BMI indicator
                val heightM = profile.heightCm / 100f
                val bmi = if (heightM > 0) todayLog.weightKg / (heightM * heightM) else 0f
                Text(
                    text = String.format("BMI: %.1f", bmi),
                    color = if (bmi in 18.5f..24.9f) ActivityGreen else WeightOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
