package com.example.medipoint.Repository

import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.AppointmentDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class AppointmentRepository(private val dao: AppointmentDao) {

    private val db = FirebaseFirestore.getInstance()
    /**
     * Add a new appointment via DAO
     */
    suspend fun addAppointment(appointment: Appointment): Result<Appointment> {
        return dao.addAppointment(appointment)
    }

    /**
     * Get all appointments for a specific user
     */
    suspend fun getAppointments(userId: String): Result<List<Appointment>> {
        return dao.getAppointments(userId)
    }

    /**
     * Listen for real-time updates to a user's appointments
     * Returns ListenerRegistration so caller can remove the listener if needed
     */
    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return dao.listenAppointments(userId, onDataChange, onError)
    }

    /**
     * Update the status of a specific appointment
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return dao.updateAppointmentStatus(appointmentId, status)
    }

    /**
     * Cancel a specific appointment (sets status to "Cancelled")
     */
    suspend fun cancelAppointment(appointmentId: String) {
        try {
            val docRef = db.collection("appointments").document(appointmentId)
            docRef.update("status", "Canceled").await()  // Updates the status of the appointment
        } catch (e: Exception) {
            // Handle exception (log, notify user, etc.)
            e.printStackTrace()
        }
    }


}
