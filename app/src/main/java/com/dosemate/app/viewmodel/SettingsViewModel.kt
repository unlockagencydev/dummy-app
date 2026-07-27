package com.dosemate.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dosemate.app.data.repository.AppSettings
import com.dosemate.app.data.repository.MedicationRepository
import com.dosemate.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun setGeneralAlarms(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGeneralAlarms(enabled) }
    }

    fun setRefillAlerts(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRefillAlerts(enabled) }
    }

    fun cycleThemeMode() {
        viewModelScope.launch {
            val next = when (settings.value.themeMode) {
                "Light" -> "Dark"
                "Dark" -> "System"
                else -> "Light"
            }
            settingsRepository.setThemeMode(next)
        }
    }

    fun cycleTextSize() {
        viewModelScope.launch {
            val next = when (settings.value.textSize) {
                "Small" -> "Medium"
                "Medium" -> "Large"
                else -> "Small"
            }
            settingsRepository.setTextSize(next)
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            medicationRepository.clearAllData()
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            medicationRepository: MedicationRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsRepository, medicationRepository) as T
                }
            }
    }
}
