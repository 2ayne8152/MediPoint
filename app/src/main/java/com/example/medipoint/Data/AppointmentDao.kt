package com.example.medipoint.Data

import com.google.firebase.firestore.ListenerRegistration

interface AppointmentDao {

    suspend fun addAppointment(appointment: Appointment): Result<Appointment>

    suspend fun getAppointments(userId: String): Result<List<Appointment>>

    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit>

    suspend fun cancelAppointment(appointmentId: String): Result<Unit>

    suspend fun getAppointmentsByDateTime(date: String, time: String): List<Appointment>
}
