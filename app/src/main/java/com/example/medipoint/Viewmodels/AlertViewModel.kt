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

    // Cancel alert by ID
    fun cancelAlert(alertId: String) {
        viewModelScope.launch {
            try {
                val result = repository.cancelAlert(alertId)
                if (result.isSuccess) {
                    // Successfully deleted the alert, remove it from the local list
                    _alerts.value = _alerts.value.filterNot { it.id == alertId }
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Mark alert as read
    fun onMarkAsRead(alertId: String) {
        viewModelScope.launch {
            try {
                // Update the alert status locally first
                val updatedAlerts = _alerts.value.map {
                    if (it.id == alertId) {
                        it.copy(isRead = true) // Mark the alert as read
                    } else {
                        it
                    }
                }
                _alerts.value = updatedAlerts // Update the local state

                // Also update the alert's status in Firestore
                val result = repository.updateAlertStatus(alertId, true) // Persist in Firestore
                if (result.isFailure) {
                    // If Firestore update fails, reset the local state or show an error message
                    _error.value = result.exceptionOrNull()?.message
                }

            } catch (e: Exception) {
                _error.value = e.message // Handle exceptions
            }
        }
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
