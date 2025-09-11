package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.AppointmentResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(
    private val repository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao())
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _saveStatus = MutableStateFlow<AppointmentResult?>(null)
    val saveStatus = _saveStatus.asStateFlow()

    /**
     * Start listening for realtime updates to this user's appointments.
     */
    fun startAppointmentsListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        repository.listenAppointments(
            userId = userId,
            onDataChange = { list -> _appointments.value = list },
            onError = { _appointments.value = emptyList() }
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
        notes: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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
            _saveStatus.value = result
        }
    }

}

