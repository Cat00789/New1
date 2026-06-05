package com.example.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DailyHealthLog
import com.example.data.database.LocalUser
import com.example.data.database.Medication
import com.example.data.database.MedicationLog
import com.example.data.database.UserProfile
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    // --- Profile State ---
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .combine(MutableStateFlow(UserProfile())) { profile, default ->
            profile ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    // --- Today's Health Log State ---
    val todayHealthLog: StateFlow<DailyHealthLog> = repository.getTodayHealthLog()
        .combine(MutableStateFlow(DailyHealthLog(date = repository.getTodayDateString()))) { log, default ->
            log ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyHealthLog(date = repository.getTodayDateString())
        )

    // --- All Logs State (For Trend Analysis/Reports) ---
    val allLogs: StateFlow<List<DailyHealthLog>> = repository.allHealthLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Medications State & Logs ---
    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayMedicationLogs: StateFlow<List<MedicationLog>> = repository.getTodayMedicationLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Simulated Sensors & Walking Activity Tracker ---
    private val _isWorkoutSimulating = MutableStateFlow(false)
    val isWorkoutSimulating: StateFlow<Boolean> = _isWorkoutSimulating.asStateFlow()

    private val _simulatedWorkoutSteps = MutableStateFlow(0)
    val simulatedWorkoutSteps: StateFlow<Int> = _simulatedWorkoutSteps.asStateFlow()

    private val _simulatedWorkoutDistance = MutableStateFlow(0f)
    val simulatedWorkoutDistance: StateFlow<Float> = _simulatedWorkoutDistance.asStateFlow()

    private val _simulatedWorkoutCalories = MutableStateFlow(0)
    val simulatedWorkoutCalories: StateFlow<Int> = _simulatedWorkoutCalories.asStateFlow()

    private val _simulatedHeartRate = MutableStateFlow(72)
    val simulatedHeartRate: StateFlow<Int> = _simulatedHeartRate.asStateFlow()

    private var workoutJob: Job? = null

    // --- Simulated GPS SOS Emergency State ---
    private val _sosState = MutableStateFlow<SosState>(SosState.Idle)
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    sealed interface SosState {
        object Idle : SosState
        data class Sending(val countdownSec: Int) : SosState
        data class Active(val lat: Double, val lng: Double, val message: String) : SosState
    }

    private var sosJob: Job? = null

    // --- Real Local Authentication Engine ---
    data class AuthState(
        val isLoggedIn: Boolean = false,
        val email: String = "",
        val isOtpVerified: Boolean = true, // We bypass OTP now for a cleaner, full local credentials flow
        val error: String? = null
    )

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun performLogin(email: String, passwordHash: String) {
        viewModelScope.launch {
            if (!email.contains("@") || email.length < 5) {
                _authState.value = AuthState(error = "Please enter a valid email address.")
                return@launch
            }
            if (passwordHash.isEmpty()) {
                _authState.value = AuthState(error = "Please enter your password.")
                return@launch
            }
            val user = repository.getLocalUser(email)
            if (user == null) {
                _authState.value = AuthState(error = "Account not found. Please register first.")
            } else if (user.passwordHash != passwordHash) {
                _authState.value = AuthState(error = "Incorrect password. If you forgot, use '1234' on the demo account.")
            } else {
                _authState.value = AuthState(isLoggedIn = true, email = email, error = null)
                // Synchronize profile name from local user account details
                val currentProfile = userProfile.value
                repository.saveUserProfile(currentProfile.copy(name = user.name))
            }
        }
    }

    fun performRegister(email: String, passwordHash: String, name: String) {
        viewModelScope.launch {
            if (!email.contains("@") || email.length < 5) {
                _authState.value = AuthState(error = "Please enter a valid email address.")
                return@launch
            }
            if (passwordHash.length < 4) {
                _authState.value = AuthState(error = "Password must be at least 4 characters/digits.")
                return@launch
            }
            if (name.isBlank() || name.length < 2) {
                _authState.value = AuthState(error = "Please enter a valid display name (min 2 chars).")
                return@launch
            }
            val registerSuccess = repository.registerLocalUser(email, passwordHash, name)
            if (registerSuccess) {
                _authState.value = AuthState(isLoggedIn = true, email = email, error = null)
                // Initialize default profile details with registration name
                val currentProfile = userProfile.value
                repository.saveUserProfile(currentProfile.copy(name = name))
            } else {
                _authState.value = AuthState(error = "Account with this email already exists. Try logging in.")
            }
        }
    }

    fun logout() {
        _authState.value = AuthState(isLoggedIn = false)
    }

    fun clearAuthError() {
        _authState.value = _authState.value.copy(error = null)
    }

    // --- Actions ---

    fun updateProfile(
        name: String,
        age: Int,
        height: Float,
        weight: Float,
        gender: String,
        bloodGroup: String,
        medicalConditions: String,
        allergies: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
        stepsGoal: Int,
        waterGoal: Int,
        sleepGoalMin: Int
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                age = age,
                heightCm = height,
                weightKg = weight,
                gender = gender,
                bloodGroup = bloodGroup,
                medicalConditions = medicalConditions,
                allergies = allergies,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                stepsGoal = stepsGoal,
                waterGoalMl = waterGoal,
                sleepGoalMinutes = sleepGoalMin
            )
            repository.saveUserProfile(updated)
            
            // Sync current weight to today's log too
            repository.updateWeight(weight)
        }
    }

    fun logWater(ml: Int) {
        viewModelScope.launch {
            repository.logWater(ml)
        }
    }

    fun addManualSteps(steps: Int) {
        viewModelScope.launch {
            repository.recordSteps(steps)
        }
    }

    fun logSleep(minutes: Int) {
        viewModelScope.launch {
            repository.logSleep(minutes)
        }
    }

    fun updateProfileWeight(kg: Float) {
        viewModelScope.launch {
            repository.updateWeight(kg)
        }
    }

    fun updateHeartRateBpm(bpm: Int) {
        viewModelScope.launch {
            _simulatedHeartRate.value = bpm
            repository.updateHeartRate(bpm)
        }
    }

    fun logAllCustomMetrics(steps: Int, water: Int, sleepMin: Int, heartRate: Int, weight: Float) {
        viewModelScope.launch {
            repository.logCustomMetric(steps, water, sleepMin, heartRate, weight)
        }
    }

    // --- Medications ---
    fun addNewMedication(name: String, dosage: String, time: String) {
        viewModelScope.launch {
            repository.addMedication(name, dosage, time)
        }
    }

    fun deleteMed(medicine: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medicine)
        }
    }

    fun recordMedIntake(medicine: Medication, isTaken: Boolean) {
        viewModelScope.launch {
            repository.recordMedicationTaken(medicine, isTaken)
        }
    }

    // --- Workouts Simulator (Accelerometer simulation) ---
    fun startWorkoutSimulation() {
        if (_isWorkoutSimulating.value) return
        _isWorkoutSimulating.value = true
        _simulatedWorkoutSteps.value = 0
        _simulatedWorkoutDistance.value = 0f
        _simulatedWorkoutCalories.value = 0
        
        workoutJob = viewModelScope.launch {
            var stepAccumulator = 0
            while (_isWorkoutSimulating.value) {
                delay(1200) // update roughly status every 1.2 seconds, simulating footsteps
                val strideSteps = (8..15).random()
                stepAccumulator += strideSteps
                
                _simulatedWorkoutSteps.value = stepAccumulator
                _simulatedWorkoutDistance.value = stepAccumulator * 0.00076f
                _simulatedWorkoutCalories.value = (stepAccumulator * 0.04f).toInt()
                
                // Heart beat increases while walking
                val heartRateBpm = (105..135).random()
                _simulatedHeartRate.value = heartRateBpm
                
                // Write incremental steps immediately to today's DB log so the user sees steps go up in real-time!
                repository.recordSteps(strideSteps)
                repository.updateHeartRate(heartRateBpm)
            }
        }
    }

    fun stopWorkoutSimulation() {
        _isWorkoutSimulating.value = false
        workoutJob?.cancel()
        workoutJob = null
    }

    // --- SOS Emergency Dispatch Simulator ---
    fun triggerSosCountdown() {
        cancelSos()
        sosJob = viewModelScope.launch {
            var countdown = 3
            while (countdown > 0) {
                _sosState.value = SosState.Sending(countdown)
                delay(1000)
                countdown--
            }
            // Trigger GPS simulation!
            // Standard simulated coordinates of a central clean location
            val lat = 37.7749 + (Math.random() - 0.5) * 0.01
            val lng = -122.4194 + (Math.random() - 0.5) * 0.01
            _sosState.value = SosState.Active(
                lat = lat,
                lng = lng,
                message = "CRITICAL COORD PINGED! Live Location, Medical Details with Blood Type: ${userProfile.value.bloodGroup} pushed to emergency contact: ${userProfile.value.emergencyContactName} (${userProfile.value.emergencyContactPhone})"
            )
        }
    }

    fun cancelSos() {
        sosJob?.cancel()
        sosJob = null
        _sosState.value = SosState.Idle
    }

    // --- Database Reset ---
    fun resetAllLocalData() {
        viewModelScope.launch {
            // Delete and update profile
            repository.saveUserProfile(UserProfile())
            // Clear current logs with default values or dummy
            repository.logWater(-1000000000)
            repository.logSleep(-1000000000)
            val todayDate = repository.getTodayDateString()
            repository.logCustomMetric(
                steps = -todayHealthLog.value.steps,
                waterMl = -todayHealthLog.value.waterMl,
                sleepMin = -todayHealthLog.value.sleepMinutes,
                heartRate = 72,
                weightKg = 70f
            )
        }
    }
}
