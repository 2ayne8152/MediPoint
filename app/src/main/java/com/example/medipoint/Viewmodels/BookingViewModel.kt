package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import com.example.medipoint.Data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private var listener: ListenerRegistration? = null

    /**
     * Start a realtime listener for appointments.
     * Optional: pass a userId if you store it in your docs and want to filter.
     */
    fun startAppointmentsListener(userId: String? = null) {
        stopAppointmentsListener()

        val currentUserId = auth.currentUser?.uid ?: return

        var query = db.collection("appointments")
            .whereEqualTo("userId", currentUserId)
        // If you later add userId in each appointment document, uncomment:
        // if (userId != null) query = query.whereEqualTo("userId", userId)

        listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                e.printStackTrace()
                _appointments.value = emptyList()
                return@addSnapshotListener
            }

            val loaded = snapshot?.documents.orEmpty().map { it.toAppointment() }
            _appointments.value = loaded
        }
    }

    fun stopAppointmentsListener() {
        listener?.remove()
        listener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAppointmentsListener()
    }

    fun saveAppointment(
        doctorName: String,
        appointmentType: String,
        date: String,
        time: String,
        notes: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        val newAppointment = hashMapOf(
            "userId" to currentUserId,
            "doctorName" to doctorName,
            "appointmentType" to appointmentType,
            "date" to date,
            "time" to time,
            "status" to "Scheduled",   // default
            "notes" to notes
        )

        db.collection("appointments")
            .add(newAppointment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

/**
 * Safely map Firestore -> Appointment, handling both your **current** field names
 * (doctorName, appointmentType) and older ones you may have saved earlier
 * (doctor, type).
 */
private fun DocumentSnapshot.toAppointment(): Appointment {
    val doctorName = getString("doctorName") ?: getString("doctor") ?: ""
    val appointmentType = getString("appointmentType") ?: getString("type") ?: ""
    val date = getString("date") ?: ""
    val time = getString("time") ?: ""
    val status = getString("status") ?: "Scheduled"
    val notes = getString("notes") ?: ""

    // If you later embed a checkInRecord map in the appointment doc,
    // you can parse it here and pass it in.
    return Appointment(
        id = id,
        doctorName = doctorName,
        appointmentType = appointmentType,
        date = date,
        time = time,
        status = status,
        notes = notes,
        checkInRecord = null
    )
}
