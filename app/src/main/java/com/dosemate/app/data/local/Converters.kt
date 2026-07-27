package com.dosemate.app.data.local

import androidx.room.TypeConverter
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.MedicationIcon
import com.dosemate.app.data.local.entity.TimeOfDay

class Converters {
    @TypeConverter
    fun fromIcon(value: MedicationIcon): String = value.name

    @TypeConverter
    fun toIcon(value: String): MedicationIcon = MedicationIcon.valueOf(value)

    @TypeConverter
    fun fromTimeOfDay(value: TimeOfDay): String = value.name

    @TypeConverter
    fun toTimeOfDay(value: String): TimeOfDay = TimeOfDay.valueOf(value)

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus = DoseStatus.valueOf(value)
}
