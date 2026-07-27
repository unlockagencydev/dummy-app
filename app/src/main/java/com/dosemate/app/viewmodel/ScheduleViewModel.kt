package com.dosemate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dosemate.app.data.local.entity.DoseLogWithMedication
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.TimeOfDay
import com.dosemate.app.data.repository.MedicationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class DateChip(
    val date: LocalDate,
    val dayLabel: String,
    val dayNumber: String,
    val isSelected: Boolean
)

data class DoseCardUi(
    val doseLogId: Long,
    val medicationName: String,
    val dosageLine: String,
    val timeLabel: String,
    val timeOfDay: TimeOfDay,
    val status: DoseStatus,
    val isTooEarly: Boolean,
    val iconName: String
)

data class ScheduleSection(
    val timeOfDay: TimeOfDay,
    val title: String,
    val doses: List<DoseCardUi>
)

data class ScheduleUiState(
    val weekDates: List<DateChip> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val sections: List<ScheduleSection> = emptyList(),
    val isLoading: Boolean = true
)

class ScheduleViewModel(
    private val repository: MedicationRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScheduleUiState> = selectedDate
        .flatMapLatest { date ->
            flow {
                repository.ensureDoseLogsForDay(date.toEpochDay())
                emitAll(repository.observeDosesForDay(date.toEpochDay()))
            }.combine(selectedDate) { logs, selected ->
                ScheduleUiState(
                    weekDates = buildWeek(selected),
                    selectedDate = selected,
                    sections = buildSections(logs),
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScheduleUiState(
                weekDates = buildWeek(LocalDate.now()),
                selectedDate = LocalDate.now()
            )
        )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun takeDose(doseLogId: Long) {
        viewModelScope.launch { repository.takeDose(doseLogId) }
    }

    fun snoozeDose(doseLogId: Long) {
        viewModelScope.launch { repository.snoozeDose(doseLogId) }
    }

    private fun buildWeek(selected: LocalDate): List<DateChip> {
        val start = selected.with(java.time.DayOfWeek.MONDAY)
        return (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            DateChip(
                date = date,
                dayLabel = date.dayOfWeek.name.take(3).lowercase()
                    .replaceFirstChar { it.uppercase() },
                dayNumber = date.dayOfMonth.toString(),
                isSelected = date == selected
            )
        }
    }

    private fun buildSections(logs: List<DoseLogWithMedication>): List<ScheduleSection> {
        val now = LocalTime.now()
        val cards = logs.map { item ->
            val schedule = item.schedule
            val scheduled = LocalTime.of(item.doseLog.scheduledHour, item.doseLog.scheduledMinute)
            val tooEarly = item.doseLog.status == DoseStatus.PENDING &&
                scheduled.isAfter(now.plusMinutes(30)) &&
                item.doseLog.scheduledEpochDay == LocalDate.now().toEpochDay()
            DoseCardUi(
                doseLogId = item.doseLog.id,
                medicationName = item.medication.name,
                dosageLine = "${item.medication.dosage} — ${schedule.quantityLabel}",
                timeLabel = scheduled.format(timeFormatter).uppercase(),
                timeOfDay = schedule.timeOfDay,
                status = item.doseLog.status,
                isTooEarly = tooEarly,
                iconName = item.medication.icon.name
            )
        }

        return TimeOfDay.entries.mapNotNull { tod ->
            val doses = cards.filter { it.timeOfDay == tod }
            if (doses.isEmpty()) null
            else ScheduleSection(
                timeOfDay = tod,
                title = tod.name.lowercase().replaceFirstChar { it.uppercase() },
                doses = doses
            )
        }
    }

    companion object {
        fun factory(repository: MedicationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduleViewModel(repository) as T
                }
            }
    }
}
