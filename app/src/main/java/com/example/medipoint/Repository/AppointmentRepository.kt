package com.example.medipoint.Repository

import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.AppointmentDao
import com.google.firebase.firestore.ListenerRegistration

sealed class AppointmentResult {
    data class Success(val appointment: Appointment) : AppointmentResult()
    data class Error(val message: String) : AppointmentResult()
}

class AppointmentRepository(private val dao: AppointmentDao) {

    suspend fun addAppointment(appointment: Appointment): AppointmentResult {
        val result = dao.addAppointment(appointment)

        return if (result.isSuccess) {
            AppointmentResult.Success(result.getOrThrow())
        } else {
            val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
            AppointmentResult.Error(errorMessage)
        }
    }

    suspend fun getAppointments(userId: String): Result<List<Appointment>> =
        dao.getAppointments(userId)

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return dao.updateAppointmentStatus(appointmentId, status)
    }

    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = dao.listenAppointments(userId, onDataChange, onError)
}
