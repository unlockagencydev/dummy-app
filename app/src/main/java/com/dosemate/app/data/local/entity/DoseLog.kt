package com.dosemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DoseStatus {
    PENDING,
    TAKEN,
    SNOOZED,
    SKIPPED
}

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DoseSchedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("medicationId"),
        Index("scheduleId"),
        Index(value = ["scheduleId", "scheduledEpochDay"], unique = true)
    ]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledEpochDay: Long,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val status: DoseStatus = DoseStatus.PENDING,
    val takenAt: Long? = null,
    val snoozeUntil: Long? = null
)
