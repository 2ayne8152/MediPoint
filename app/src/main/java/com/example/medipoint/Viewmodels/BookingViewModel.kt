package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BookingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun saveAppointment(
        doctor: String,
        type: String,
        date: String,
        time: String,
        notes: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val appointmentRef = db.collection("appointments").document()
        val appointmentId = appointmentRef.id

        val appointment = hashMapOf(
            "id" to appointmentId,
            "doctor" to doctor,
            "type" to type,
            "date" to date,
            "time" to time,
            "notes" to notes
        )

        // Create a check-in record inside a "checkin" subcollection
        val checkInRef = appointmentRef.collection("checkin").document()
        val checkInRecord = hashMapOf(
            "id" to checkInRef.id,
            "checkedIn" to false,
            "checkInTime" to null
        )

        db.runBatch { batch ->
            batch.set(appointmentRef, appointment)
            batch.set(checkInRef, checkInRecord)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }
}