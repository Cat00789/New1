package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only single local profile
    val name: String = "User",
    val age: Int = 28,
    val gender: String = "Not Specified",
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val bloodGroup: String = "O+",
    val medicalConditions: String = "",
    val allergies: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val stepsGoal: Int = 10000,
    val waterGoalMl: Int = 2500,
    val sleepGoalMinutes: Int = 480 // 8 hours
)

@Entity(tableName = "daily_health_log")
data class DailyHealthLog(
    @PrimaryKey val date: String, // format "yyyy-MM-dd"
    val steps: Int = 0,
    val calories: Int = 0,
    val distanceKm: Float = 0f,
    val waterMl: Int = 0,
    val sleepMinutes: Int = 0,
    val heartRate: Int = 72,
    val weightKg: Float = 70f
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val time: String, // "HH:mm" format
    val isRecurring: Boolean = true,
    val isActive: Boolean = true
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val dosage: String,
    val date: String, // "yyyy-MM-dd"
    val timeLogged: String, // "HH:mm"
    val status: String // "TAKEN" or "MISSED"
)

@Entity(tableName = "local_users")
data class LocalUser(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val name: String = "User"
)
