package com.example.medipoint.Repository

import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.AppointmentDao
import com.google.firebase.firestore.ListenerRegistration

class AppointmentRepository(private val dao: AppointmentDao) {

    suspend fun addAppointment(appointment: Appointment): Result<Appointment> {
        return dao.addAppointment(appointment)
    }

    suspend fun getAppointments(userId: String): Result<List<Appointment>> {
        return dao.getAppointments(userId)
    }

    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return dao.listenAppointments(userId, onDataChange, onError)
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return dao.updateAppointmentStatus(appointmentId, status)
    }

    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return dao.cancelAppointment(appointmentId)
    }

    // New: get appointments by date & time
    suspend fun getAppointmentsByDateTime(date: String, time: String): List<Appointment> {
        return dao.getAppointmentsByDateTime(date, time)
    }
}
