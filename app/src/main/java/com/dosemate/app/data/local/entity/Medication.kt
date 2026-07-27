package com.dosemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicationIcon {
    PILL,
    KIT,
    SYRINGE,
    CLIPBOARD,
    BOTTLE,
    BLISTER
}

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: String,
    val icon: MedicationIcon = MedicationIcon.PILL,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
