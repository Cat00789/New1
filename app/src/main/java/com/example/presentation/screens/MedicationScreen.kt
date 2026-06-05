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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Medication
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val medicationsList by viewModel.medications.collectAsState()
    val medLogsToday by viewModel.todayMedicationLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("1 Pill") }
    var medTimeHours by remember { mutableStateOf("") }
    var medTimeMinutes by remember { mutableStateOf("") }

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
                            text = "Medications",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Schedules & pill alarm reminder logs",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .background(TealAccent.copy(alpha = 0.15f), CircleShape)
                            .testTag("add_medication_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Pill Reminder", tint = TealAccent)
                    }
                }
            }

            // --- ALARM DISPATCH SIMULATION BANNER ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HeartRateRed.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HeartRateRed.copy(alpha = 0.3f))),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Simulated alarm push",
                            tint = HeartRateRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SIMULATED ALARM REMINDERS IS ON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeartRateRed,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You will receive virtual in-app push banners at scheduled hours to avoid missed doses.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // --- SChedule LIST TITLE ---
            item {
                Text(
                    text = "TODAY'S SCHEDULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (medicationsList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "No medications",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No medications programmed",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap the plus button above to add pill reminders",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(medicationsList) { med ->
                    val isTaken = medLogsToday.any { it.medicineId == med.id }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTaken) DarkSurface.copy(alpha = 0.5f) else DarkSurface
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Toggle status checkbox
                                Icon(
                                    imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Mark medication status",
                                    tint = if (isTaken) ActivityGreen else Color.Gray,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.recordMedIntake(med, !isTaken) }
                                        .testTag("med_checkbox_${med.id}")
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = med.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTaken) Color.White.copy(alpha = 0.4f) else Color.White,
                                        fontSize = 16.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = "Time",
                                            tint = TealAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${med.time} • ${med.dosage}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteMed(med) },
                                modifier = Modifier.testTag("delete_med_${med.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete medication",
                                    tint = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ADD MEDICATION DIALOG MODAL ---
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Add New Reminder",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("Pill Name", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = { Text("e.g. Lipitor, Vitamin D", color = Color.DarkGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("med_name_input")
                        )

                        OutlinedTextField(
                            value = medDosage,
                            onValueChange = { medDosage = it },
                            label = { Text("Dosage size", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = { Text("e.g. 1 capsule, 10ml", color = Color.DarkGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("med_dosage_input")
                        )

                        // Alarm Time Picker (Hours and minutes)
                        Column {
                            Text("Time Schedule (24h format)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = medTimeHours,
                                    onValueChange = { if (it.length <= 2) medTimeHours = it },
                                    label = { Text("HH", color = Color.Gray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TealAccent,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("med_time_hour_input")
                                )
                                Text(":", color = Color.White, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
                                OutlinedTextField(
                                    value = medTimeMinutes,
                                    onValueChange = { if (it.length <= 2) medTimeMinutes = it },
                                    label = { Text("MM", color = Color.Gray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TealAccent,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("med_time_minute_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                onClick = {
                                    val hr = medTimeHours.padStart(2, '0')
                                    val min = medTimeMinutes.padStart(2, '0')
                                    val formatTime = "$hr:$min"
                                    if (medName.isNotBlank() && medDosage.isNotBlank() && medTimeHours.isNotBlank() && medTimeMinutes.isNotBlank()) {
                                        viewModel.addNewMedication(medName, medDosage, formatTime)
                                    }
                                    medName = ""
                                    medDosage = "1 Pill"
                                    medTimeHours = ""
                                    medTimeMinutes = ""
                                    showAddDialog = false
                                },
                                modifier = Modifier.testTag("med_add_confirm_button")
                            ) {
                                Text("Add Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
