package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.CheckInRecord
import com.google.android.gms.location.LocationServices
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
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow(CheckInRecord())
    val checkInRecord: StateFlow<CheckInRecord> = _checkInRecord

    /**
     * Attempt to check in for a specific appointment
     */
    @SuppressLint("MissingPermission")
    fun attemptCheckIn(appointmentId: String) {
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
                            checkInLng = location.longitude
                        )
                        _checkInRecord.value = record
                        saveCheckInRecord(appointmentId, record)
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
     * Save record under the appointment's subcollection
     */
    private fun saveCheckInRecord(appointmentId: String, record: CheckInRecord) {
        val checkInRef = db.collection("appointments")
            .document(appointmentId)
            .collection("checkin")
            .document()

        val recordWithId = record.copy(id = checkInRef.id)

        checkInRef.set(recordWithId)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "Check-in saved!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error saving check-in", e)
            }
    }

    /**
     * Load all check-ins for an appointment
     */
    fun loadCheckInRecords(appointmentId: String, onResult: (List<CheckInRecord>) -> Unit) {
        db.collection("appointments")
            .document(appointmentId)
            .collection("checkin")
            .get()
            .addOnSuccessListener { result ->
                val records = result.mapNotNull { it.toObject(CheckInRecord::class.java) }
                onResult(records)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error loading check-ins", e)
                onResult(emptyList())
            }
    }
}
