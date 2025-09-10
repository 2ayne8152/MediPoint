package com.example.medipoint.Data

import com.google.firebase.firestore.ListenerRegistration

interface AppointmentDao {
    suspend fun addAppointment(appointment: Appointment): Result<Unit>
    suspend fun getAppointments(userId: String): Result<List<Appointment>>
    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration
}
