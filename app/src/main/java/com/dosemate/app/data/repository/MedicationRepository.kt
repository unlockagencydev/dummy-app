package com.dosemate.app.data.repository

import com.dosemate.app.data.alarm.AlarmScheduler
import com.dosemate.app.data.local.dao.DoseLogDao
import com.dosemate.app.data.local.dao.DoseScheduleDao
import com.dosemate.app.data.local.dao.InventoryDao
import com.dosemate.app.data.local.dao.MedicationDao
import com.dosemate.app.data.local.entity.DoseLog
import com.dosemate.app.data.local.entity.DoseLogWithMedication
import com.dosemate.app.data.local.entity.DoseSchedule
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.Inventory
import com.dosemate.app.data.local.entity.Medication
import com.dosemate.app.data.local.entity.MedicationWithInventory
import com.dosemate.app.data.local.entity.TimeOfDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val inventoryDao: InventoryDao,
    private val scheduleDao: DoseScheduleDao,
    private val doseLogDao: DoseLogDao,
    private val alarmScheduler: AlarmScheduler
) {

    fun observeMedicationsWithInventory(): Flow<List<MedicationWithInventory>> =
        medicationDao.observeMedicationsWithInventory()

    fun observeMedicationCount(): Flow<Int> = medicationDao.observeMedicationCount()

    fun observeDosesForDay(epochDay: Long): Flow<List<DoseLogWithMedication>> =
        doseLogDao.observeLogsForDay(epochDay)

    suspend fun ensureDoseLogsForDay(epochDay: Long) {
        val schedules = scheduleDao.getEnabledSchedules()
        val existing = doseLogDao.getLogsForDay(epochDay).map { it.doseLog.scheduleId }.toSet()
        schedules
            .filter { it.id !in existing }
            .forEach { schedule ->
                val logId = doseLogDao.insert(
                    DoseLog(
                        medicationId = schedule.medicationId,
                        scheduleId = schedule.id,
                        scheduledEpochDay = epochDay,
                        scheduledHour = schedule.hour,
                        scheduledMinute = schedule.minute
                    )
                )
                if (logId > 0 && epochDay == LocalDate.now().toEpochDay()) {
                    alarmScheduler.scheduleDose(
                        doseLogId = logId,
                        medicationId = schedule.medicationId,
                        epochDay = epochDay,
                        hour = schedule.hour,
                        minute = schedule.minute
                    )
                }
            }
    }

    suspend fun takeDose(doseLogId: Long) {
        val log = doseLogDao.getById(doseLogId) ?: return
        if (log.status == DoseStatus.TAKEN) return
        doseLogDao.markStatus(doseLogId, DoseStatus.TAKEN, System.currentTimeMillis())
        val inventory = inventoryDao.getForMedication(log.medicationId) ?: return
        inventoryDao.update(
            inventory.copy(currentCount = (inventory.currentCount - 1).coerceAtLeast(0))
        )
        alarmScheduler.cancelDose(doseLogId)
    }

    suspend fun snoozeDose(doseLogId: Long, minutes: Int = 15) {
        val snoozeUntil = System.currentTimeMillis() + minutes * 60_000L
        doseLogDao.snooze(doseLogId, DoseStatus.SNOOZED, snoozeUntil)
        val log = doseLogDao.getById(doseLogId) ?: return
        alarmScheduler.scheduleSnooze(
            doseLogId = doseLogId,
            medicationId = log.medicationId,
            triggerAtMillis = snoozeUntil
        )
    }

    suspend fun addMedication(
        name: String,
        dosage: String,
        form: String,
        currentCount: Int,
        maxCount: Int,
        unit: String,
        timeOfDay: TimeOfDay,
        hour: Int,
        minute: Int,
        quantityLabel: String
    ): Long {
        val medId = medicationDao.insertMedication(
            Medication(name = name, dosage = dosage, form = form)
        )
        inventoryDao.insert(
            Inventory(
                medicationId = medId,
                currentCount = currentCount.coerceAtMost(maxCount),
                maxCount = maxCount,
                unit = unit,
                lowStockThreshold = (maxCount * 0.25f).toInt().coerceAtLeast(1)
            )
        )
        val scheduleId = scheduleDao.insert(
            DoseSchedule(
                medicationId = medId,
                timeOfDay = timeOfDay,
                hour = hour,
                minute = minute,
                quantityLabel = quantityLabel
            )
        )
        val today = LocalDate.now().toEpochDay()
        val logId = doseLogDao.insert(
            DoseLog(
                medicationId = medId,
                scheduleId = scheduleId,
                scheduledEpochDay = today,
                scheduledHour = hour,
                scheduledMinute = minute
            )
        )
        if (logId > 0) {
            alarmScheduler.scheduleDose(
                doseLogId = logId,
                medicationId = medId,
                epochDay = today,
                hour = hour,
                minute = minute
            )
        }
        return medId
    }

    suspend fun refillMedication(medicationId: Long, amount: Int) {
        val inventory = inventoryDao.getForMedication(medicationId) ?: return
        inventoryDao.update(
            inventory.copy(
                currentCount = (inventory.currentCount + amount).coerceAtMost(inventory.maxCount)
            )
        )
    }

    suspend fun adjustInventory(medicationId: Long, newCount: Int) {
        val inventory = inventoryDao.getForMedication(medicationId) ?: return
        inventoryDao.update(inventory.copy(currentCount = newCount.coerceIn(0, inventory.maxCount)))
    }

    suspend fun rescheduleAllAlarms() {
        alarmScheduler.cancelAll()
        val today = LocalDate.now().toEpochDay()
        ensureDoseLogsForDay(today)
        doseLogDao.getLogsForDay(today)
            .filter { it.doseLog.status == DoseStatus.PENDING || it.doseLog.status == DoseStatus.SNOOZED }
            .forEach { item ->
                val log = item.doseLog
                if (log.status == DoseStatus.SNOOZED && log.snoozeUntil != null) {
                    alarmScheduler.scheduleSnooze(log.id, log.medicationId, log.snoozeUntil)
                } else {
                    val trigger = LocalDateTime.of(
                        LocalDate.ofEpochDay(log.scheduledEpochDay),
                        LocalTime.of(log.scheduledHour, log.scheduledMinute)
                    ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    if (trigger > System.currentTimeMillis()) {
                        alarmScheduler.scheduleDose(
                            doseLogId = log.id,
                            medicationId = log.medicationId,
                            epochDay = log.scheduledEpochDay,
                            hour = log.scheduledHour,
                            minute = log.scheduledMinute
                        )
                    }
                }
            }
    }

    suspend fun clearAllData() {
        alarmScheduler.cancelAll()
        val meds = medicationDao.getMedicationsWithInventory()
        meds.forEach { medicationDao.deleteMedication(it.medication.id) }
    }
}
