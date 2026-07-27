package com.dosemate.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MedicationWithInventory(
    @Embedded val medication: Medication,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val inventory: Inventory?
)

data class DoseLogWithMedication(
    @Embedded val doseLog: DoseLog,
    @Relation(
        parentColumn = "medicationId",
        entityColumn = "id"
    )
    val medication: Medication,
    @Relation(
        parentColumn = "scheduleId",
        entityColumn = "id"
    )
    val schedule: DoseSchedule
)
