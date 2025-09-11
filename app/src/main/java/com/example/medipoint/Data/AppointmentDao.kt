package com.example.medipoint.Data

import com.google.firebase.firestore.ListenerRegistration

interface AppointmentDao {
    suspend fun addAppointment(appointment: Appointment): Result<Appointment>
    suspend fun getAppointments(userId: String): Result<List<Appointment>>
    suspend fun updateAppointmentStatus(id: String, newStatus: String): Result<Unit>
    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration
}
