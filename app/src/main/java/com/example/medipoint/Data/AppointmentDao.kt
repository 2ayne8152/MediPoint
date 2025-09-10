package com.example.medipoint.Data

interface AppointmentDao {
    suspend fun addAppointment(appointment: Appointment): Result<Appointment>
    suspend fun getAppointments(userId: String): Result<List<Appointment>>
    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    )
}
