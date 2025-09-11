package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.CheckInRecord
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow<CheckInRecord?>(null)
    val checkInRecord: StateFlow<CheckInRecord?> = _checkInRecord

    // ⚡️ Appointment object from Firestore
    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    // ⚡️ Parsed appointment datetime in millis
    private val _appointmentDateTime = MutableStateFlow<Long?>(null)
    val appointmentDateTime: StateFlow<Long?> = _appointmentDateTime

    /**
     * Try to check in for a specific appointment (only once per user).
     */
    @SuppressLint("MissingPermission")
    fun attemptCheckIn(appointmentId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        location.latitude, location.longitude,
                        hospitalLat, hospitalLng,
                        distance
                    )

                    if (distance[0] <= checkInRadius) {
                        val record = CheckInRecord(
                            checkedIn = true,
                            checkInTime = System.currentTimeMillis(),
                            checkInLat = location.latitude,
                            checkInLng = location.longitude,
                            userId = currentUser.uid,
                            appointmentId = appointmentId
                        )
                        saveCheckInRecord(appointmentId, record)
                        _checkInRecord.value = record
                    } else {
                        _checkInRecord.value = CheckInRecord(checkedIn = false)
                    }
                } else {
                    _checkInRecord.value = CheckInRecord(checkedIn = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _checkInRecord.value = CheckInRecord(checkedIn = false)
            }
        }
    }

    private fun saveCheckInRecord(appointmentId: String, record: CheckInRecord) {
        val checkInRef = db.collection("appointments")
            .document(appointmentId)
            .collection("checkin")
            .document(record.userId)

        checkInRef.set(record)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "Check-in saved!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error saving check-in", e)
            }
    }

    fun loadUserCheckInRecord(appointmentId: String) {
        val currentUser = auth.currentUser ?: return

        db.collection("appointments")
            .document(appointmentId)
            .collection("checkin")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val record = document.toObject(CheckInRecord::class.java)
                    _checkInRecord.value = record
                } else {
                    _checkInRecord.value = null
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error loading check-in", e)
                _checkInRecord.value = null
            }
    }

    /**
     * Load appointment details from Firestore and parse datetime.
     */
    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("appointments")
                    .document(appointmentId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val appt = snapshot.toObject(Appointment::class.java)
                    _appointment.value = appt

                    // ⚡️ Parse date + time into timestamp
                    if (appt?.date != null && appt.time != null) {
                        val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
                        try {
                            val parsed = sdf.parse("${appt.date} ${appt.time}")
                            _appointmentDateTime.value = parsed?.time
                        } catch (e: Exception) {
                            e.printStackTrace()
                            _appointmentDateTime.value = null
                        }
                    } else {
                        _appointmentDateTime.value = null
                    }
                } else {
                    _appointment.value = null
                    _appointmentDateTime.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _appointment.value = null
                _appointmentDateTime.value = null
            }
        }
    }

    fun setCheckInRecord(record: CheckInRecord?) {
        _checkInRecord.value = record
    }
}
