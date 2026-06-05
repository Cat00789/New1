package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@Composable
fun SosScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val sosState by viewModel.sosState.collectAsState()
    val scrollState = rememberScrollState()

    // Pulse animation for the panic button
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsePanic"
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column {
                    Text(
                        text = "SOS Emergency System",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Instant panic dispatch and digital medical ID card",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- PANIC DISPATCH ACTION AREA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE PANIC DISPATCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    when (val state = sosState) {
                        is HealthViewModel.SosState.Idle -> {
                            // Big clickable SOS trigger button
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .scale(scalePulse)
                                    .clip(CircleShape)
                                    .background(HeartRateRed)
                                    .border(6.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable { viewModel.triggerSosCountdown() }
                                    .testTag("panic_trigger_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SOS",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Press and hold for 3 seconds to broadcast live GPS location and health records to emergency services.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        is HealthViewModel.SosState.Sending -> {
                            // Countdown indicator
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape)
                                    .background(WeightOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.countdownSec.toString(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "TRANSMITTING IN ${state.countdownSec}s ...",
                                fontWeight = FontWeight.Bold,
                                color = WeightOrange,
                                fontSize = 15.sp
                            )
                            TextButton(
                                onClick = { viewModel.cancelSos() },
                                modifier = Modifier.testTag("abort_sos_button")
                            ) {
                                Text("ABORT PANIC TRANSMISSION", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        is HealthViewModel.SosState.Active -> {
                            // Active beacon screen
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .scale(scalePulse)
                                    .clip(CircleShape)
                                    .background(ActivityGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "Beacon active",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "GPS COORDINATES SENT",
                                fontWeight = FontWeight.Bold,
                                color = ActivityGreen,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", state.lat)} • Lng: ${String.format("%.4f", state.lng)}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.cancelSos() },
                                colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                                modifier = Modifier.testTag("close_sos_button")
                            ) {
                                Text("DISMISS ALARM BEACON", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- DIGITAL EMERGENCY MEDICAL CARD ---
            Text(
                text = "DIGITAL ADMITTANCE HEALTH CARD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Start)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Blood Group and Info Hero Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Age: ${profile.age} • Gender: ${profile.gender}", color = Color.Gray, fontSize = 13.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(HeartRateRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BLOOD", color = HeartRateRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(profile.bloodGroup.ifBlank { "N/A" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Allergies Block
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = "Allergy icon", tint = WeightOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("ALLERGIES & DRUG HYPERSENSITIVITY", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = profile.allergies.ifBlank { "NONE CURRENTLY REGISTERED" },
                                color = if (profile.allergies.isNotBlank()) Color.White else Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Medical Conditions Block
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.MedicalServices, contentDescription = "Conditions icon", tint = TealAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("DIAGNOSED CHRONIC CONDITIONS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = profile.medicalConditions.ifBlank { "NO CONDITIONS REPORTED" },
                                color = if (profile.medicalConditions.isNotBlank()) Color.White else Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Emergency Contact
                    Text("EMERGENCY CONCIERGE RELATIONSHIP", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = profile.emergencyContactName.ifBlank { "No Contact Added" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = profile.emergencyContactPhone.ifBlank { "Register contact phone in settings" },
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.Phone, contentDescription = "Phone alert", tint = TealAccent)
                        }
                    }
                }
            }
        }
    }
}
