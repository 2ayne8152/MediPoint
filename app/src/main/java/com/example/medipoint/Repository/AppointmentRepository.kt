package com.example.medipoint.Repository

import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.AppointmentDao

class AppointmentRepository(private val dao: AppointmentDao) {

    suspend fun addAppointment(appointment: Appointment): Result<Unit> =
        dao.addAppointment(appointment)

    suspend fun getAppointments(userId: String): Result<List<Appointment>> =
        dao.getAppointments(userId)

    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ) = dao.listenAppointments(userId, onDataChange, onError)
}
