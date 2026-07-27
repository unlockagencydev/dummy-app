package com.dosemate.app

import android.app.Application
import com.dosemate.app.data.alarm.AlarmScheduler
import com.dosemate.app.data.alarm.NotificationHelper
import com.dosemate.app.data.local.DoseMateDatabase
import com.dosemate.app.data.repository.MedicationRepository
import com.dosemate.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DoseMateApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: DoseMateDatabase
        private set

    lateinit var repository: MedicationRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)

        database = DoseMateDatabase.getInstance(this)
        alarmScheduler = AlarmScheduler(this)
        settingsRepository = SettingsRepository(this)
        repository = MedicationRepository(
            medicationDao = database.medicationDao(),
            inventoryDao = database.inventoryDao(),
            scheduleDao = database.doseScheduleDao(),
            doseLogDao = database.doseLogDao(),
            alarmScheduler = alarmScheduler
        )

        applicationScope.launch {
            repository.rescheduleAllAlarms()
        }
    }
}
