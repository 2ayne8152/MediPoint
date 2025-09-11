package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Repository.AppointmentRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class CheckInViewModel(
    application: Application,
    private val repository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao())
) : AndroidViewModel(application) {

    // 👉 Move hospital location to constants (replace with your real hospital coords)
    private val hospitalLat = 37.4219983
    private val hospitalLng = -122.084
    private val checkInRadius = 200 // meters

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow<CheckInRecord?>(null)
    val checkInRecord: StateFlow<CheckInRecord?> = _checkInRecord

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

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

                        // 🔥 Use repository instead of Firestore direct call
                        repository.updateAppointmentStatus(appointmentId, "Completed")

                        _checkInRecord.value = record
                    } else {
                        _checkInRecord.value =
                            CheckInRecord(checkedIn = false, userId = currentUser.uid, appointmentId = appointmentId)
                    }
                } else {
                    // Better handling when location is null
                    android.util.Log.e("Location", "Unable to get location. Try again with GPS enabled.")
                    _checkInRecord.value =
                        CheckInRecord(checkedIn = false, userId = currentUser.uid, appointmentId = appointmentId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _checkInRecord.value =
                    CheckInRecord(checkedIn = false, userId = currentUser.uid, appointmentId = appointmentId)
            }
        }
    }

    fun autoUpdateAppointmentStatus(appointmentId: String, appointmentTime: Long, durationMinutes: Int = 30) {
        val now = System.currentTimeMillis()
        val appointmentEnd = appointmentTime + (durationMinutes * 60 * 1000)

        viewModelScope.launch {
            if (now > appointmentEnd) {
                repository.updateAppointmentStatus(appointmentId, "Completed")
            }
        }
    }

    private fun saveCheckInRecord(appointmentId: String, record: CheckInRecord) {
        viewModelScope.launch {
            try {
                db.collection("appointments")
                    .document(appointmentId)
                    .collection("checkin")
                    .document(record.userId)
                    .set(record)
                    .await()
                android.util.Log.d("Firestore", "Check-in saved!")
            } catch (e: Exception) {
                android.util.Log.e("Firestore", "Error saving check-in", e)
            }
        }
    }

    fun loadUserCheckInRecord(appointmentId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val document = db.collection("appointments")
                    .document(appointmentId)
                    .collection("checkin")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (document.exists()) {
                    val record = document.toObject(CheckInRecord::class.java)
                    _checkInRecord.value = record
                } else {
                    _checkInRecord.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("Firestore", "Error loading check-in", e)
                _checkInRecord.value = null
            }
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

                    if (appt?.date != null && appt.time != null) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
                        try {
                            val parsed = sdf.parse("${appt.date} ${appt.time}")
                            val apptTime = parsed?.time
                            _appointmentDateTime.value = apptTime

                            if (apptTime != null) {
                                // Mark as missed if past and still scheduled
                                if (appt.status == "Scheduled" && System.currentTimeMillis() > apptTime) {
                                    repository.updateAppointmentStatus(appointmentId, "Missed")
                                }

                                // Also check if it should be completed
                                autoUpdateAppointmentStatus(appointmentId, apptTime)
                            }
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
