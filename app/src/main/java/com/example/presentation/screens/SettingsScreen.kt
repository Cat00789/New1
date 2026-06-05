package com.example.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isWearableSyncEnabled by remember { mutableStateOf(true) }
    var unitSystemMetric by remember { mutableStateOf(true) }
    var inAppRemindersEnabled by remember { mutableStateOf(true) }
    var showResetVerifyDialog by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column {
                    Text(
                        text = "App Settings",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Customize devices, systems & notification alerts",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- SMART WEARABLE INTEGRATION SECTION ---
            Text("DEVICE INTEGRATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TealAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Watch, contentDescription = "Pulse check", tint = TealAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Smartwatch Sync", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Synchronize steps & heartrate with Wear OS, Fitbit & Garmin", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isWearableSyncEnabled,
                            onCheckedChange = { isWearableSyncEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealAccent, checkedTrackColor = TealDark),
                            modifier = Modifier.testTag("wear_sync_toggle")
                        )
                    }

                    if (isWearableSyncEnabled) {
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Last Synchronized", color = Color.White, fontSize = 13.sp)
                            Text("Just Now (Google Health Connect)", color = ActivityGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- LOCAL MEASUREMENT SYSTEM ---
            Text("LOCALIZATION SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(WeightOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Scale, contentDescription = "Units check", tint = WeightOrange, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Metric System Units", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Toggle metric (kg/cm) and imperial (lbs/feet)", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = unitSystemMetric,
                            onCheckedChange = { unitSystemMetric = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = WeightOrange, checkedTrackColor = WeightOrange.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // --- IN-APP NOTIFICATIONS ALERTS ---
            Text("PRESTIGE HEALTH NOTIFICATION SERVICE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(HydrationBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alarms check", tint = HydrationBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Pill / Water Reminders", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Receive daily notifications for logging health values", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = inAppRemindersEnabled,
                            onCheckedChange = { inAppRemindersEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = HydrationBlue, checkedTrackColor = HydrationBlue.copy(alpha = 0.4f))
                        )
                    }

                    if (inAppRemindersEnabled) {
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Button(
                            onClick = {
                                // Simulate sending standard system channel alert diagnostic ping
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SystemUpdateAlt, contentDescription = "Test notifications", tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Alarm Notifications System", color = Color.LightGray)
                        }
                    }
                }
            }

            // --- RED ZONE CLEAR ZONE ---
            Text("DANGER RED ZONE PROTECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Resetting local caches will permanently clear steps, target histories, medical profiles, and medications logging.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_database_button"),
                        onClick = { showResetVerifyDialog = true }
                    ) {
                        Icon(Icons.Default.Dangerous, contentDescription = "Dangerous deletion action", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset database", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- VERIFY DATABASE ERASING ---
        if (showResetVerifyDialog) {
            AlertDialog(
                onDismissRequest = { showResetVerifyDialog = false },
                title = { Text("Are you absolutely sure?", color = Color.White) },
                text = {
                    Text("This will wipe all SQLite databases in HealthSync. This is irreversible.", color = Color.Gray)
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = HeartRateRed),
                        onClick = {
                            viewModel.resetAllLocalData()
                            showResetVerifyDialog = false
                        },
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text("Erase database", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetVerifyDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}
