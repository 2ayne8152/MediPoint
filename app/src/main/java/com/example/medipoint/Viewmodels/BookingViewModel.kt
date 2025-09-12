package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.Alerts
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.AlertsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    // State flows for appointments and save status
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _saveStatus = MutableStateFlow<Result<Appointment>?>(null)
    val saveStatus: StateFlow<Result<Appointment>?> = _saveStatus

    // Start listening for appointments in real-time
    fun startAppointmentsListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        appointmentRepository.listenAppointments(
            userId = userId,
            onDataChange = { list -> _appointments.value = list },
            onError = { _appointments.value = emptyList() }
        )
    }

    // Save a new appointment and create an alert if the save is successful
    fun saveAppointment(
        doctorName: String,
        appointmentType: String,
        date: String,
        time: String,
        notes: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _saveStatus.value = Result.failure(Exception("User not logged in"))
            return
        }

        val newAppointment = Appointment(
            doctorName = doctorName,
            appointmentType = appointmentType,
            date = date,
            time = time,
            status = "Scheduled",
            notes = notes,
            userId = userId
        )

        viewModelScope.launch {
            try {
                // Attempt to save the appointment
                val result: Result<Appointment> = appointmentRepository.addAppointment(newAppointment)
                _saveStatus.value = result

                // If appointment was successfully saved, create an alert
                if (result.isSuccess) {
                    val appointmentId = result.getOrNull()?.id ?: return@launch
                    createAlertFromAppointment(
                        appointmentId = appointmentId,
                        doctorName = doctorName,
                        appointmentDate = date,
                        userId = userId
                    )
                }
            } catch (e: Exception) {
                // Handle errors here
                _saveStatus.value = Result.failure(e)
            }
        }
    }

    // Create an alert for the appointment
    private fun createAlertFromAppointment(
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

        saveAlertToFirestore(newAlert)
    }

    // Save the alert to Firestore
    private fun saveAlertToFirestore(alert: Alerts) {
        viewModelScope.launch {
            try {
                val result = alertsRepository.addAlertToFirestore(alert)
                if (result.isSuccess) {
                    println("Alert saved successfully!")
                } else {
                    println("Failed to save alert: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("Error saving alert: ${e.message}")
            }
        }
    }
}
