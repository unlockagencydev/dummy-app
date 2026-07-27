package com.dosemate.app.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDose(
        doseLogId: Long,
        medicationId: Long,
        epochDay: Long,
        hour: Int,
        minute: Int
    ) {
        val triggerAt = LocalDateTime.of(
            LocalDate.ofEpochDay(epochDay),
            LocalTime.of(hour, minute)
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerAt <= System.currentTimeMillis()) return
        scheduleExact(doseLogId, medicationId, triggerAt)
    }

    fun scheduleSnooze(doseLogId: Long, medicationId: Long, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        scheduleExact(doseLogId, medicationId, triggerAtMillis)
    }

    private fun scheduleExact(doseLogId: Long, medicationId: Long, triggerAtMillis: Long) {
        val pendingIntent = pendingIntentFor(doseLogId, medicationId)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelDose(doseLogId: Long) {
        val intent = Intent(context, DoseAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseLogId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun cancelAll() {
        // Individual cancels happen per dose; nothing global required for sample data
    }

    private fun pendingIntentFor(doseLogId: Long, medicationId: Long): PendingIntent {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            action = DoseAlarmReceiver.ACTION_DOSE_REMINDER
            putExtra(DoseAlarmReceiver.EXTRA_DOSE_LOG_ID, doseLogId)
            putExtra(DoseAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
        }
        return PendingIntent.getBroadcast(
            context,
            doseLogId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
