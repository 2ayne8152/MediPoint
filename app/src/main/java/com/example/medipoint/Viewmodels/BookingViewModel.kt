package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Alerts
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _saveStatus = MutableStateFlow<Result<Appointment>?>(null)
    val saveStatus: StateFlow<Result<Appointment>?> = _saveStatus

    fun startAppointmentsListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        appointmentRepository.listenAppointments(
            userId = userId,
            onDataChange = { list -> _appointments.value = list },
            onError = { _appointments.value = emptyList() }
        )
    }

    fun saveAppointment(
        doctorName: String,
        appointmentType: String,
        date: String,
        time: String,
        notes: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
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
                val result = appointmentRepository.addAppointment(newAppointment)
                _saveStatus.value = result

                // Only create alert if appointment saved successfully
                result.getOrNull()?.id?.let { appointmentId ->
                    val alert = Alerts(
                        id = appointmentId.ifEmpty { alertsRepository.generateAlertId() },
                        title = "Appointment Reminder",
                        message = "You have an appointment with $doctorName on $date.",
                        userId = userId
                    )
                    alertsRepository.addAlertToFirestore(alert)
                }
            } catch (e: Exception) {
                _saveStatus.value = Result.failure(e)
            }
        }
    }
}
