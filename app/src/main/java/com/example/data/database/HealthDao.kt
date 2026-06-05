package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // --- Daily Health Log ---
    @Query("SELECT * FROM daily_health_log ORDER BY date DESC")
    fun getAllHealthLogs(): Flow<List<DailyHealthLog>>

    @Query("SELECT * FROM daily_health_log WHERE date = :date")
    fun getHealthLogByDateFlow(date: String): Flow<DailyHealthLog?>

    @Query("SELECT * FROM daily_health_log WHERE date = :date")
    suspend fun getHealthLogByDate(date: String): DailyHealthLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthLog(log: DailyHealthLog)

    // --- Medications ---
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY time ASC")
    fun getActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY time ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    // --- Medication Logs ---
    @Query("SELECT * FROM medication_logs WHERE date = :date")
    fun getMedicationLogsByDate(date: String): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    @Query("DELETE FROM medication_logs WHERE medicineId = :medicineId AND date = :date")
    suspend fun deleteMedicationLogForDate(medicineId: Long, date: String)

    // --- Local Users ---
    @Query("SELECT * FROM local_users WHERE email = :email")
    suspend fun getLocalUser(email: String): LocalUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalUser(user: LocalUser)

    @Query("SELECT COUNT(*) FROM local_users")
    suspend fun getLocalUserCount(): Int
}
