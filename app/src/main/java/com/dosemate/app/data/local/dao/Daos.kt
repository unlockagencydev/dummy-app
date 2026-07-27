package com.dosemate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dosemate.app.data.local.entity.DoseLog
import com.dosemate.app.data.local.entity.DoseLogWithMedication
import com.dosemate.app.data.local.entity.DoseSchedule
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.Inventory
import com.dosemate.app.data.local.entity.Medication
import com.dosemate.app.data.local.entity.MedicationWithInventory
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun observeMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedication(id: Long): Medication?

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun observeMedicationsWithInventory(): Flow<List<MedicationWithInventory>>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    suspend fun getMedicationsWithInventory(): List<MedicationWithInventory>

    @Query("SELECT COUNT(*) FROM medications")
    fun observeMedicationCount(): Flow<Int>
}

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: Inventory): Long

    @Update
    suspend fun update(inventory: Inventory)

    @Query("SELECT * FROM inventory WHERE medicationId = :medicationId LIMIT 1")
    suspend fun getForMedication(medicationId: Long): Inventory?

    @Query("SELECT * FROM inventory")
    suspend fun getAll(): List<Inventory>
}

@Dao
interface DoseScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: DoseSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<DoseSchedule>): List<Long>

    @Update
    suspend fun update(schedule: DoseSchedule)

    @Query("SELECT * FROM dose_schedules WHERE enabled = 1")
    suspend fun getEnabledSchedules(): List<DoseSchedule>

    @Query("SELECT * FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun getForMedication(medicationId: Long): List<DoseSchedule>

    @Query("SELECT * FROM dose_schedules WHERE id = :id")
    suspend fun getById(id: Long): DoseSchedule?

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteForMedication(medicationId: Long)
}

@Dao
interface DoseLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: DoseLog): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(logs: List<DoseLog>): List<Long>

    @Update
    suspend fun update(log: DoseLog)

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getById(id: Long): DoseLog?

    @Transaction
    @Query(
        """
        SELECT * FROM dose_logs
        WHERE scheduledEpochDay = :epochDay
        ORDER BY scheduledHour ASC, scheduledMinute ASC
        """
    )
    fun observeLogsForDay(epochDay: Long): Flow<List<DoseLogWithMedication>>

    @Transaction
    @Query(
        """
        SELECT * FROM dose_logs
        WHERE scheduledEpochDay = :epochDay
        ORDER BY scheduledHour ASC, scheduledMinute ASC
        """
    )
    suspend fun getLogsForDay(epochDay: Long): List<DoseLogWithMedication>

    @Query(
        """
        UPDATE dose_logs
        SET status = :status, takenAt = :takenAt, snoozeUntil = NULL
        WHERE id = :id
        """
    )
    suspend fun markStatus(id: Long, status: DoseStatus, takenAt: Long?)

    @Query(
        """
        UPDATE dose_logs
        SET status = :status, snoozeUntil = :snoozeUntil
        WHERE id = :id
        """
    )
    suspend fun snooze(id: Long, status: DoseStatus, snoozeUntil: Long)

    @Query("SELECT COUNT(*) FROM dose_logs")
    suspend fun count(): Int
}
