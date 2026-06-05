package com.example.presentation.screens

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@Composable
fun SleepScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayHealthLog.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    var showLogDialog by remember { mutableStateOf(false) }
    var selectedSleepHours by remember { mutableFloatStateOf(7.5f) }
    var selectedQuality by remember { mutableIntStateOf(4) } // 1..5 stars

    val totalHoursToday = todayLog.sleepMinutes / 60f
    val goalHours = profile.sleepGoalMinutes / 60f

    // Pulse animation for the moon
    val infiniteTransition = rememberInfiniteTransition(label = "sleep")
    val moonGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonGlow"
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
                .padding(bottom = 80.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column {
                    Text(
                        text = "Sleep Tracking",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Log and analyze sleep patterns & nocturnal recovery",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- NIGHT VISUAL CARD WITH GLOW MOON ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0D0B2E),
                                    DarkSurface
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SLEEP RECOVERY HUD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Moon Glow drawing
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .scale(moonGlowScale)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .shadow(24.dp, shape = CircleShape, ambientColor = SleepLilac, spotColor = SleepLilac),
                            shape = CircleShape,
                            color = Color(0xFF1E1A54).copy(alpha = 0.4f)
                        ) {}

                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Cosmic Rest",
                            tint = SleepLilac,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format("%.1f hrs", totalHoursToday),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    val sleepRatio = if (goalHours > 0) (totalHoursToday / goalHours).coerceIn(0f, 1f) else 0f
                    Text(
                        text = String.format("Goal: %.1f hrs (%d%% complete)", goalHours, (sleepRatio * 100f).toInt()),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showLogDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleepLilac),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_sleep_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bedtime, contentDescription = "Settle down Log", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Sleep Session", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- DEEP RECOVERY RECOMMENDATIONS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Sleep hygiene",
                        tint = SleepLilac,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SMART SLEEP HYGIENE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleepLilac,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (totalHoursToday < 6.0f) {
                                "You slept less than 6 hours. Studies show sleeping 7-8 hours improves logical thinking by 24% and balances immune cytokine release. Settle down 30 minutes earlier tonight!"
                            } else {
                                "Excellent recovery index! Your REM and light sleep cycles are well balanced. Maintain consistent bedtime alarms for continued heart-rate wellness."
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- INSIGHT SLEEP BAROMETER METRIC ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SLEEP METRICS BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val deepSleep = (todayLog.sleepMinutes * 0.35f).toInt()
                    val lightSleep = (todayLog.sleepMinutes * 0.50f).toInt()
                    val awakeTime = (todayLog.sleepMinutes * 0.15f).toInt()

                    SleepBarIndicator(label = "Deep Sleep (35%)", minutes = deepSleep, color = SleepLilac)
                    Spacer(modifier = Modifier.height(10.dp))
                    SleepBarIndicator(label = "Light Sleep (50%)", minutes = lightSleep, color = TealAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    SleepBarIndicator(label = "Rem / Awake Period (15%)", minutes = awakeTime, color = WeightOrange)
                }
            }
        }

        // --- SLEEP LOG DIALOG ---
        if (showLogDialog) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                title = { Text("Log Sleep Time & Quality", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Drag the slider to enter hours slept last night:", color = Color.Gray, fontSize = 13.sp)

                        // Hours selector
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration:", color = Color.White, fontSize = 14.sp)
                                Text(String.format("%.1f hrs", selectedSleepHours), color = SleepLilac, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Slider(
                                value = selectedSleepHours,
                                onValueChange = { selectedSleepHours = it },
                                valueRange = 1f..12f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = SleepLilac,
                                    activeTrackColor = SleepLilac,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                )
                            )
                        }

                        // Quality stars rating selector
                        Column {
                            Text("Rest Quality:", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                for (star in 1..5) {
                                    IconButton(
                                        onClick = { selectedQuality = star },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Rating $star",
                                            tint = if (star <= selectedQuality) WeightOrange else Color.DarkGray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = SleepLilac),
                        onClick = {
                            val durationMin = (selectedSleepHours * 60f).toInt()
                            if (durationMin > 0) {
                                viewModel.logSleep(durationMin)
                            }
                            showLogDialog = false
                        },
                        modifier = Modifier.testTag("sleep_log_confirm")
                    ) {
                        Text("Save Sleep", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
fun SleepBarIndicator(
    label: String,
    minutes: Int,
    color: Color
) {
    val hrs = minutes / 60
    val mins = minutes % 60
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text(
                text = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m",
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (minutes / 480f).coerceIn(0f, 1f),
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}
