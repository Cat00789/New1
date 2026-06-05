package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    // Form inputs initialized with profile values
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var genderSelection by remember { mutableStateOf("") }
    var bloodGroupInput by remember { mutableStateOf("") }
    var conditionsInput by remember { mutableStateOf("") }
    var allergiesInput by remember { mutableStateOf("") }
    var emergencyNameInput by remember { mutableStateOf("") }
    var emergencyPhoneInput by remember { mutableStateOf("") }
    
    // Goals
    var stepsGoalInput by remember { mutableStateOf("") }
    var waterGoalInput by remember { mutableStateOf("") }
    var sleepGoalInput by remember { mutableStateOf("") }

    // Sync state when profile is loaded from DB
    LaunchedEffect(profile) {
        nameInput = profile.name
        ageInput = profile.age.toString()
        heightInput = profile.heightCm.toString()
        weightInput = profile.weightKg.toString()
        genderSelection = profile.gender
        bloodGroupInput = profile.bloodGroup
        conditionsInput = profile.medicalConditions
        allergiesInput = profile.allergies
        emergencyNameInput = profile.emergencyContactName
        emergencyPhoneInput = profile.emergencyContactPhone
        stepsGoalInput = profile.stepsGoal.toString()
        waterGoalInput = profile.waterGoalMl.toString()
        sleepGoalInput = (profile.sleepGoalMinutes / 60).toString()
    }

    // BMI calculation
    val heightM = (heightInput.toFloatOrNull() ?: 175f) / 100f
    val weightKg = weightInput.toFloatOrNull() ?: 70f
    val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f

    val (bmiCategory, bmiColor) = when {
        bmi <= 0f -> "N/A" to Color.Gray
        bmi < 18.5f -> "Underweight" to WeightOrange
        bmi < 25f -> "Healthy Weight" to ActivityGreen
        bmi < 30f -> "Overweight" to WeightOrange
        else -> "Obese Category" to HeartRateRed
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TealAccent,
        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manage demographics & goal calculators",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        val age = ageInput.toIntOrNull() ?: 28
                        val height = heightInput.toFloatOrNull() ?: 175f
                        val weight = weightInput.toFloatOrNull() ?: 70f
                        val stepsGoal = stepsGoalInput.toIntOrNull() ?: 10000
                        val waterGoal = waterGoalInput.toIntOrNull() ?: 2500
                        val sleepMin = (sleepGoalInput.toIntOrNull() ?: 8) * 60

                        viewModel.updateProfile(
                            nameInput, age, height, weight, genderSelection, bloodGroupInput,
                            conditionsInput, allergiesInput, emergencyNameInput, emergencyPhoneInput,
                            stepsGoal, waterGoal, sleepMin
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // --- BMI DASHBOARD CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BODY MASS INDEX (BMI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = String.format("%.1f", bmi),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bmiCategory,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = bmiColor,
                        modifier = Modifier
                            .background(bmiColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // --- PERSONAL DETAILS FORM ---
            Text("BIOMETRICS & DEMOGRAPHICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name", color = Color.Gray) },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageInput,
                            onValueChange = { ageInput = it },
                            label = { Text("Age (yrs)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bloodGroupInput,
                            onValueChange = { bloodGroupInput = it },
                            label = { Text("Blood Type", color = Color.Gray) },
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height (cm)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = genderSelection,
                        onValueChange = { genderSelection = it },
                        label = { Text("Gender", color = Color.Gray) },
                        colors = textFieldColors,
                        placeholder = { Text("e.g. Male / Female / Other", color = Color.DarkGray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // --- HEALTH GOALS CONFIG ---
            Text("DAILY TARGET GOALS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = stepsGoalInput,
                        onValueChange = { stepsGoalInput = it },
                        label = { Text("Daily Steps Goal", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = waterGoalInput,
                            onValueChange = { waterGoalInput = it },
                            label = { Text("Water (ml)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sleepGoalInput,
                            onValueChange = { sleepGoalInput = it },
                            label = { Text("Sleep (hrs)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- MEDICAL SAFETY CARD & EMERGENCY CONTACTS ---
            Text("MEDICAL EMERGENCY CARD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = conditionsInput,
                        onValueChange = { conditionsInput = it },
                        label = { Text("Diagnosed Medical Conditions", color = Color.Gray) },
                        placeholder = { Text("e.g. Hypertension, Diabetes Type 2", color = Color.DarkGray) },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = allergiesInput,
                        onValueChange = { allergiesInput = it },
                        label = { Text("Known Allergies", color = Color.Gray) },
                        placeholder = { Text("e.g. Penicillin, Peanuts, Latex", color = Color.DarkGray) },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                    Text("Emergency Contact", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                    OutlinedTextField(
                        value = emergencyNameInput,
                        onValueChange = { emergencyNameInput = it },
                        label = { Text("Contact Name", color = Color.Gray) },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emergencyPhoneInput,
                        onValueChange = { emergencyPhoneInput = it },
                        label = { Text("Contact Phone", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
