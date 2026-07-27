package com.dosemate.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dosemate.app.data.local.dao.DoseLogDao
import com.dosemate.app.data.local.dao.DoseScheduleDao
import com.dosemate.app.data.local.dao.InventoryDao
import com.dosemate.app.data.local.dao.MedicationDao
import com.dosemate.app.data.local.entity.DoseLog
import com.dosemate.app.data.local.entity.DoseSchedule
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.Inventory
import com.dosemate.app.data.local.entity.Medication
import com.dosemate.app.data.local.entity.MedicationIcon
import com.dosemate.app.data.local.entity.TimeOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Database(
    entities = [
        Medication::class,
        Inventory::class,
        DoseSchedule::class,
        DoseLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DoseMateDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun doseScheduleDao(): DoseScheduleDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        @Volatile
        private var INSTANCE: DoseMateDatabase? = null

        fun getInstance(context: Context): DoseMateDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): DoseMateDatabase {
            return Room.databaseBuilder(
                context,
                DoseMateDatabase::class.java,
                "dosemate.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedSampleData()
                        }
                    }
                })
                .build()
        }
    }

    suspend fun seedSampleData() {
        val medDao = medicationDao()
        val invDao = inventoryDao()
        val scheduleDao = doseScheduleDao()
        val logDao = doseLogDao()

        data class Seed(
            val med: Medication,
            val inventory: Inventory,
            val schedules: List<DoseSchedule>
        )

        val seeds = listOf(
            Seed(
                Medication(name = "Metformin", dosage = "500mg", form = "Tablet", icon = MedicationIcon.KIT),
                Inventory(medicationId = 0, currentCount = 25, maxCount = 30, unit = "pills", lowStockThreshold = 8),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.MORNING,
                        hour = 8,
                        minute = 0,
                        quantityLabel = "1 Pill"
                    )
                )
            ),
            Seed(
                Medication(name = "Vitamin B12", dosage = "1000mcg", form = "Injection", icon = MedicationIcon.SYRINGE),
                Inventory(medicationId = 0, currentCount = 12, maxCount = 20, unit = "doses", lowStockThreshold = 5),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.MORNING,
                        hour = 8,
                        minute = 30,
                        quantityLabel = "1 Dose"
                    )
                )
            ),
            Seed(
                Medication(name = "Lisinopril", dosage = "10mg", form = "Tablet", icon = MedicationIcon.PILL),
                Inventory(medicationId = 0, currentCount = 6, maxCount = 30, unit = "pills", lowStockThreshold = 10),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.AFTERNOON,
                        hour = 13,
                        minute = 0,
                        quantityLabel = "1 Pill"
                    )
                )
            ),
            Seed(
                Medication(name = "Omega-3", dosage = "1000mg", form = "Softgel", icon = MedicationIcon.BOTTLE),
                Inventory(medicationId = 0, currentCount = 40, maxCount = 60, unit = "pills", lowStockThreshold = 10),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.AFTERNOON,
                        hour = 14,
                        minute = 0,
                        quantityLabel = "1 Softgel"
                    )
                )
            ),
            Seed(
                Medication(name = "Melatonin", dosage = "5mg", form = "Tablet", icon = MedicationIcon.PILL),
                Inventory(medicationId = 0, currentCount = 18, maxCount = 30, unit = "pills", lowStockThreshold = 8),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.EVENING,
                        hour = 21,
                        minute = 0,
                        quantityLabel = "1 Pill"
                    )
                )
            ),
            Seed(
                Medication(name = "Vitamin D3", dosage = "1000 IU", form = "Liquid Drops", icon = MedicationIcon.SYRINGE),
                Inventory(medicationId = 0, currentCount = 18, maxCount = 30, unit = "ml", lowStockThreshold = 8),
                emptyList()
            ),
            Seed(
                Medication(name = "Advil", dosage = "200mg", form = "Capsules", icon = MedicationIcon.CLIPBOARD),
                Inventory(medicationId = 0, currentCount = 2, maxCount = 24, unit = "pills", lowStockThreshold = 6),
                emptyList()
            ),
            Seed(
                Medication(name = "Amoxicillin", dosage = "500mg", form = "Capsule", icon = MedicationIcon.BLISTER),
                Inventory(medicationId = 0, currentCount = 10, maxCount = 20, unit = "doses", lowStockThreshold = 5),
                listOf(
                    DoseSchedule(
                        medicationId = 0,
                        timeOfDay = TimeOfDay.EVENING,
                        hour = 20,
                        minute = 0,
                        quantityLabel = "1 Capsule"
                    )
                )
            )
        )

        val today = LocalDate.now().toEpochDay()
        val now = LocalTime.now()

        seeds.forEach { seed ->
            val medId = medDao.insertMedication(seed.med)
            invDao.insert(seed.inventory.copy(medicationId = medId))
            seed.schedules.forEach { schedule ->
                val scheduleId = scheduleDao.insert(schedule.copy(medicationId = medId))
                val scheduledTime = LocalTime.of(schedule.hour, schedule.minute)
                val status = if (scheduledTime.isBefore(now.minusMinutes(30))) {
                    DoseStatus.PENDING
                } else {
                    DoseStatus.PENDING
                }
                logDao.insert(
                    DoseLog(
                        medicationId = medId,
                        scheduleId = scheduleId,
                        scheduledEpochDay = today,
                        scheduledHour = schedule.hour,
                        scheduledMinute = schedule.minute,
                        status = status
                    )
                )
                // Also seed tomorrow so date selector has content when browsing
                logDao.insert(
                    DoseLog(
                        medicationId = medId,
                        scheduleId = scheduleId,
                        scheduledEpochDay = today + 1,
                        scheduledHour = schedule.hour,
                        scheduledMinute = schedule.minute,
                        status = DoseStatus.PENDING
                    )
                )
                // Yesterday
                logDao.insert(
                    DoseLog(
                        medicationId = medId,
                        scheduleId = scheduleId,
                        scheduledEpochDay = today - 1,
                        scheduledHour = schedule.hour,
                        scheduledMinute = schedule.minute,
                        status = DoseStatus.TAKEN,
                        takenAt = System.currentTimeMillis() - 86_400_000L
                    )
                )
            }
        }
    }
}
