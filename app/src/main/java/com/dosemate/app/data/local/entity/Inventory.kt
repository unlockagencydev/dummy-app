package com.dosemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"], unique = true)]
)
data class Inventory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val currentCount: Int,
    val maxCount: Int,
    val unit: String = "pills",
    val lowStockThreshold: Int = 10
) {
    val remainingFraction: Float
        get() = if (maxCount <= 0) 0f else currentCount.toFloat() / maxCount.toFloat()

    val percentRemaining: Int
        get() = (remainingFraction * 100).toInt().coerceIn(0, 100)

    val isLowStock: Boolean
        get() = currentCount <= lowStockThreshold

    val isCritical: Boolean
        get() = remainingFraction <= 0.15f
}
