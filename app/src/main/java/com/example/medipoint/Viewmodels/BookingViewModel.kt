package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

class BookingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun saveAppointment(
        doctorName: String,
        appointmentType: String,
        date: String,
        time: String,
        notes: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val appointment = Appointment(
            id = UUID.randomUUID().toString(),
            doctorName = doctorName,
            appointmentType = appointmentType,
            date = date,
            time = time,
            notes = notes
        )

        viewModelScope.launch {
            db.collection("appointments")
                .document(appointment.id)
                .set(appointment)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }
}