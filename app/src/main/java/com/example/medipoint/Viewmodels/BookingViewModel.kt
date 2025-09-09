package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    /**
     * Start listening for realtime updates to this user's appointments.
     */
    fun startAppointmentsListener(userId: String) {
        repository.listenAppointments(
            userId = userId,
            onDataChange = { list ->
                _appointments.value = list
            },
            onError = {
                _appointments.value = emptyList()
            }
        )
    }

    /**
     * Save a new appointment.
     */
    fun saveAppointment(
        doctorName: String,
        appointmentType: String,
        date: String,
        time: String,
        notes: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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
            val result = repository.addAppointment(newAppointment)
            result.onSuccess { onSuccess() }
                .onFailure { e -> onFailure(e) }
        }
    }
}
