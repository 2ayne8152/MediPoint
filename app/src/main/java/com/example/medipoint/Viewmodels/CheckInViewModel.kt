package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Data.FirestoreCheckInDao
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.CheckInRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val hospitalLat = 37.4219983
    private val hospitalLng = -122.084
    private val checkInRadius = 200 // meters

    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow<CheckInRecord?>(null)
    val checkInRecord: StateFlow<CheckInRecord?> = _checkInRecord

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    private val _appointmentDateTime = MutableStateFlow<Long?>(null)
    val appointmentDateTime: StateFlow<Long?> = _appointmentDateTime

    private val appointmentDao = FirestoreAppointmentDao()
    private val appointmentRepository = AppointmentRepository(appointmentDao)

    private val checkInDao = FirestoreCheckInDao()
    private val checkInRepository = CheckInRepository(checkInDao)

    @SuppressLint("MissingPermission")
    fun attemptCheckIn(appointmentId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                val checkedIn = location?.let {
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        it.latitude, it.longitude,
                        hospitalLat, hospitalLng,
                        distance
                    )
                    distance[0] <= checkInRadius
                } ?: false

                val record = CheckInRecord(
                    checkedIn = checkedIn,
                    checkInTime = if (checkedIn) System.currentTimeMillis() else null,
                    checkInLat = if (checkedIn) location?.latitude else null,
                    checkInLng = if (checkedIn) location?.longitude else null,
                    userId = currentUser.uid,
                    appointmentId = appointmentId
                )

                _checkInRecord.value = record

                if (checkedIn) {
                    appointmentRepository.updateAppointmentStatus(appointmentId, "Checked-In")
                    checkInRepository.addCheckInRecord(appointmentId, record)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _checkInRecord.value = CheckInRecord(
                    checkedIn = false,
                    userId = currentUser.uid,
                    appointmentId = appointmentId
                )
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.cancelAppointment(appointmentId)
        }
    }

    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val result = appointmentRepository.getAppointments(currentUser.uid)
            if (result.isSuccess) {
                val appt = result.getOrNull()?.find { it.id == appointmentId }
                _appointment.value = appt

                val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
                _appointmentDateTime.value = appt?.let {
                    try { sdf.parse("${it.date} ${it.time}")?.time } catch (e: Exception){ null }
                }
            } else {
                _appointment.value = null
                _appointmentDateTime.value = null
            }
        }
    }

    fun loadUserCheckInRecord(appointmentId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val result = checkInRepository.getCheckInRecord(appointmentId, currentUser.uid)
            _checkInRecord.value = result.getOrNull()
        }
    }

    fun setCheckInRecord(record: CheckInRecord?) {
        _checkInRecord.value = record
    }
}
