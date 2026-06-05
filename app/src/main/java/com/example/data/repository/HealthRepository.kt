package com.example.data.repository

import com.example.data.database.DailyHealthLog
import com.example.data.database.HealthDao
import com.example.data.database.LocalUser
import com.example.data.database.Medication
import com.example.data.database.MedicationLog
import com.example.data.database.UserProfile
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRepository(private val healthDao: HealthDao) {

    // Helper to get today's date format "yyyy-MM-dd"
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = healthDao.getUserProfileFlow()

    suspend fun saveUserProfile(profile: UserProfile) {
        healthDao.insertUserProfile(profile)
    }

    suspend fun initDefaultProfileIfNeeded() {
        val currentProfile = healthDao.getUserProfile()
        if (currentProfile == null) {
            healthDao.insertUserProfile(UserProfile())
        }
    }

    // --- Local Users ---
    suspend fun getLocalUser(email: String): LocalUser? {
        return healthDao.getLocalUser(email)
    }

    suspend fun registerLocalUser(email: String, passwordHash: String, name: String): Boolean {
        if (healthDao.getLocalUser(email) != null) return false
        val user = LocalUser(email = email, passwordHash = passwordHash, name = name)
        healthDao.insertLocalUser(user)
        return true
    }

    suspend fun initDefaultUsersIfNeeded() {
        if (healthDao.getLocalUserCount() == 0) {
            val defaultUser = LocalUser(email = "user@gmail.com", passwordHash = "1234", name = "Demo User")
            healthDao.insertLocalUser(defaultUser)
        }
    }

    // --- Health Log ---
    val allHealthLogs: Flow<List<DailyHealthLog>> = healthDao.getAllHealthLogs()

    fun getTodayHealthLog(): Flow<DailyHealthLog?> {
        return healthDao.getHealthLogByDateFlow(getTodayDateString())
    }

    suspend fun recordSteps(stepsDelta: Int) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        
        val newSteps = currentLog.steps + stepsDelta
        // 1 step is ~0.04 calories burned and ~0.00076 kilometers
        val caloriesDelta = (stepsDelta * 0.04f).toInt()
        val distanceDelta = stepsDelta * 0.00076f

        val updatedLog = currentLog.copy(
            steps = newSteps,
            calories = currentLog.calories + caloriesDelta,
            distanceKm = currentLog.distanceKm + distanceDelta
        )
        healthDao.insertHealthLog(updatedLog)
    }

    suspend fun logWater(waterDeltaMl: Int) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        val updatedLog = currentLog.copy(
            waterMl = (currentLog.waterMl + waterDeltaMl).coerceAtLeast(0)
        )
        healthDao.insertHealthLog(updatedLog)
    }

    suspend fun logSleep(sleepDurationMinutes: Int) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        val updatedLog = currentLog.copy(
            sleepMinutes = (currentLog.sleepMinutes + sleepDurationMinutes).coerceAtLeast(0)
        )
        healthDao.insertHealthLog(updatedLog)
    }

    suspend fun updateWeight(weightKg: Float) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        val updatedLog = currentLog.copy(weightKg = weightKg)
        healthDao.insertHealthLog(updatedLog)
        
        // Also update profile weight as default
        val profile = healthDao.getUserProfile() ?: UserProfile()
        healthDao.insertUserProfile(profile.copy(weightKg = weightKg))
    }

    suspend fun updateHeartRate(bpm: Int) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        val updatedLog = currentLog.copy(heartRate = bpm)
        healthDao.insertHealthLog(updatedLog)
    }

    suspend fun logCustomMetric(steps: Int, waterMl: Int, sleepMin: Int, heartRate: Int, weightKg: Float) {
        val today = getTodayDateString()
        val currentLog = healthDao.getHealthLogByDate(today) ?: DailyHealthLog(date = today)
        
        val totalSteps = currentLog.steps + steps
        val calories = currentLog.calories + (steps * 0.04f).toInt()
        val mileage = currentLog.distanceKm + (steps * 0.00076f)
        
        val updatedLog = currentLog.copy(
            steps = totalSteps,
            calories = calories,
            distanceKm = mileage,
            waterMl = (currentLog.waterMl + waterMl).coerceAtLeast(0),
            sleepMinutes = (currentLog.sleepMinutes + sleepMin).coerceAtLeast(0),
            heartRate = if (heartRate > 0) heartRate else currentLog.heartRate,
            weightKg = if (weightKg > 0) weightKg else currentLog.weightKg
        )
        healthDao.insertHealthLog(updatedLog)
    }

    // --- Medications ---
    val allMedications: Flow<List<Medication>> = healthDao.getAllMedications()
    val activeMedications: Flow<List<Medication>> = healthDao.getActiveMedications()

    suspend fun addMedication(name: String, dosage: String, time: String) {
        val med = Medication(name = name, dosage = dosage, time = time)
        healthDao.insertMedication(med)
    }

    suspend fun toggleMedicationActive(medication: Medication) {
        healthDao.updateMedication(medication.copy(isActive = !medication.isActive))
    }

    suspend fun deleteMedication(medication: Medication) {
        healthDao.deleteMedication(medication)
    }

    // --- Medication Active Log for Today ---
    fun getTodayMedicationLogs(): Flow<List<MedicationLog>> {
        return healthDao.getMedicationLogsByDate(getTodayDateString())
    }

    suspend fun recordMedicationTaken(medication: Medication, isTaken: Boolean) {
        val today = getTodayDateString()
        if (isTaken) {
            val timeLogged = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val log = MedicationLog(
                medicineId = medication.id,
                medicineName = medication.name,
                dosage = medication.dosage,
                date = today,
                timeLogged = timeLogged,
                status = "TAKEN"
            )
            healthDao.insertMedicationLog(log)
        } else {
            healthDao.deleteMedicationLogForDate(medication.id, today)
        }
    }
}
