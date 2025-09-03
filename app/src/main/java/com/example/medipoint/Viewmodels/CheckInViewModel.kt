package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.CheckInRecord
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                            userId = currentUser.uid,         // ✅ store userId
                            appointmentId = appointmentId     // ✅ store appointmentId
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

    /**
     * Save record under the appointment’s subcollection.
     * One record per user per appointment.
     */
    private fun saveCheckInRecord(appointmentId: String, record: CheckInRecord) {
        val checkInRef = db.collection("appointments")
            .document(appointmentId)
            .collection("checkin")
            .document(record.userId) // ✅ use userId as the doc ID so it's unique per user

        checkInRef.set(record)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "Check-in saved!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error saving check-in", e)
            }
    }

    /**
     * Load the current user’s check-in for an appointment.
     */
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
     * Allows UI to set the record manually (optional).
     */
    fun setCheckInRecord(record: CheckInRecord?) {
        _checkInRecord.value = record
    }
}
