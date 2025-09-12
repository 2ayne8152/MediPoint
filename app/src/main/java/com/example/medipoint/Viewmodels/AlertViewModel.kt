package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Alerts
import com.example.medipoint.Repository.AlertsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertViewModel(
    private val repository: AlertsRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alerts>>(emptyList())
    val alerts: StateFlow<List<Alerts>> get() = _alerts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun fetchAlerts(userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getAlertsForUser(userId)
                if (result.isSuccess) {
                    _alerts.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveAlert(alert: Alerts) {
        viewModelScope.launch {
            try {
                val result = repository.addAlertToFirestore(alert)
                if (result.isSuccess) {
                    _alerts.value = _alerts.value + alert
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createAlertFromAppointment(
        appointmentId: String,
        doctorName: String,
        appointmentDate: String,
        userId: String
    ) {
        val alertId = if (appointmentId.isNotEmpty()) appointmentId else repository.generateAlertId()
        val alert = Alerts(
            id = alertId,
            title = "Appointment Reminder",
            message = "You have an appointment with Dr. $doctorName on $appointmentDate.",
            date = appointmentDate,
            userId = userId
        )
        saveAlert(alert)
    }
}

class AlertViewModelFactory(
    private val repository: AlertsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
