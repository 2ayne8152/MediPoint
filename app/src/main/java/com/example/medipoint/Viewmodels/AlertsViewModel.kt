package com.example.medipoint.Viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Alerts
import com.example.medipoint.Repository.AlertsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlertViewModel(
    private val repository: AlertsRepository) : ViewModel() {

    // State holder for alerts in UI (StateFlow for Compose)
    private val _alerts = MutableStateFlow<List<Alerts>>(emptyList())
    val alerts: StateFlow<List<Alerts>> get() = _alerts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    // Fetch alerts from Firestore and Room
    fun fetchAlerts(userId: String) {
        viewModelScope.launch {
            try {
                // Fetch alerts from Firestore and store them in Room
                repository.fetchAlertsFromFirestore(userId)
                // Fetch alerts from Room (for local persistence)
                _alerts.value = repository.getAllAlertsFromRoom()  // Refresh UI after fetching
            } catch (e: Exception) {
                _error.value = e.message // Handle error
            }
        }
    }

    // Insert an alert locally (for in-app generated notifications)
    fun insertAlert(alert: Alerts) {
        viewModelScope.launch {
            repository.insertAlertLocally(alert)
            _alerts.value = repository.getAllAlertsFromRoom()  // Refresh UI after insertion
        }
    }

    // Delete all alerts from Room (for clearing notifications)
    fun deleteAllAlerts() {
        viewModelScope.launch {
            repository.deleteAllAlertsFromRoom()
            _alerts.value = emptyList()  // Refresh UI after deletion
        }
    }

    // Generate and save an alert when a new appointment is made
    fun createAlertFromAppointment(
        appointmentId: String,
        doctorName: String,
        appointmentDate: String,
        userId: String
    ) {
        val alertTitle = "Appointment Reminder"
        val alertMessage = "You have an appointment with Dr. $doctorName on $appointmentDate."
        val alertDate = appointmentDate  // Use the appointment date for the alert

        val newAlert = Alerts(
            id = appointmentId,
            title = alertTitle,
            message = alertMessage,
            date = alertDate,
            userId = userId
        )

        // Save the alert to Firestore
        saveAlertToFirestore(newAlert)
    }

    private fun saveAlertToFirestore(alert: Alerts) {
        val db = FirebaseFirestore.getInstance()

        val alertData = hashMapOf(
            "title" to alert.title,
            "message" to alert.message,
            "date" to alert.date,
            "userId" to alert.userId
        )

        db.collection("alerts")
            .add(alertData)
            .addOnSuccessListener { documentReference ->
                println("Alert added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding alert: $e")
            }
    }

}

class AlertViewModelFactory(
    private val repository: AlertsRepository  // Pass repository here if required
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertViewModel(repository) as T  // Return AlertViewModel instance
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}