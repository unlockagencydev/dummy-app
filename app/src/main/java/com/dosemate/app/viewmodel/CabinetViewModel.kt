package com.dosemate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dosemate.app.data.local.entity.MedicationIcon
import com.dosemate.app.data.local.entity.MedicationWithInventory
import com.dosemate.app.data.local.entity.TimeOfDay
import com.dosemate.app.data.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CabinetCardUi(
    val medicationId: Long,
    val name: String,
    val formLine: String,
    val currentCount: Int,
    val maxCount: Int,
    val unit: String,
    val percent: Int,
    val isLowStock: Boolean,
    val isCritical: Boolean,
    val icon: MedicationIcon,
    val isWide: Boolean = false
)

data class CabinetUiState(
    val medicationCount: Int = 0,
    val cards: List<CabinetCardUi> = emptyList(),
    val showAddSheet: Boolean = false
)

class CabinetViewModel(
    private val repository: MedicationRepository
) : ViewModel() {

    private val showAddSheet = kotlinx.coroutines.flow.MutableStateFlow(false)

    val uiState: StateFlow<CabinetUiState> = combine(
        repository.observeMedicationsWithInventory(),
        repository.observeMedicationCount(),
        showAddSheet
    ) { meds, count, addSheet ->
        CabinetUiState(
            medicationCount = count,
            cards = meds.map { it.toCardUi() },
            showAddSheet = addSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CabinetUiState()
    )

    fun openAddSheet() {
        showAddSheet.value = true
    }

    fun dismissAddSheet() {
        showAddSheet.value = false
    }

    fun addMedication(
        name: String,
        dosage: String,
        form: String,
        current: Int,
        max: Int,
        hour: Int,
        minute: Int,
        timeOfDay: TimeOfDay
    ) {
        viewModelScope.launch {
            repository.addMedication(
                name = name.trim(),
                dosage = dosage.trim(),
                form = form.trim(),
                currentCount = current,
                maxCount = max,
                unit = "pills",
                timeOfDay = timeOfDay,
                hour = hour,
                minute = minute,
                quantityLabel = "1 Pill"
            )
            showAddSheet.value = false
        }
    }

    fun adjustStock(medicationId: Long, newCount: Int) {
        viewModelScope.launch { repository.adjustInventory(medicationId, newCount) }
    }

    private fun MedicationWithInventory.toCardUi(): CabinetCardUi {
        val inv = inventory
        val current = inv?.currentCount ?: 0
        val max = inv?.maxCount ?: 0
        return CabinetCardUi(
            medicationId = medication.id,
            name = medication.name,
            formLine = "${medication.dosage} ${medication.form}",
            currentCount = current,
            maxCount = max,
            unit = inv?.unit ?: "pills",
            percent = inv?.percentRemaining ?: 0,
            isLowStock = inv?.isLowStock == true,
            isCritical = inv?.isCritical == true,
            icon = medication.icon,
            isWide = medication.icon == MedicationIcon.BLISTER
        )
    }

    companion object {
        fun factory(repository: MedicationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CabinetViewModel(repository) as T
                }
            }
    }
}
