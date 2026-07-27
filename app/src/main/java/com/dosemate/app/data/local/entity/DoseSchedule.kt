package com.dosemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING
}

@Entity(
    tableName = "dose_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val timeOfDay: TimeOfDay,
    val hour: Int,
    val minute: Int,
    val quantityLabel: String = "1 Pill",
    val enabled: Boolean = true
)
