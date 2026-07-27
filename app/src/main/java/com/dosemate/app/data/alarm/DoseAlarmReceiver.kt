package com.dosemate.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dosemate.app.DoseMateApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DOSE_REMINDER) return

        val doseLogId = intent.getLongExtra(EXTRA_DOSE_LOG_ID, -1L)
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (doseLogId < 0 || medicationId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as DoseMateApp
                val medication = app.database.medicationDao().getMedication(medicationId)
                val name = medication?.name ?: "Medication"
                val dosage = medication?.dosage ?: ""
                NotificationHelper.showDoseReminder(context, doseLogId, name, dosage)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DOSE_REMINDER = "com.dosemate.app.ACTION_DOSE_REMINDER"
        const val EXTRA_DOSE_LOG_ID = "extra_dose_log_id"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
    }
}
