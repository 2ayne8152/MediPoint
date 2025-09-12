package com.example.medipoint.Repository

import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.AppointmentDao
import com.google.firebase.firestore.ListenerRegistration // ** ADD THIS IMPORT **

class AppointmentRepository(private val dao: AppointmentDao) {

    suspend fun addAppointment(appointment: Appointment): Result<Appointment> =
        dao.addAppointment(appointment)

    suspend fun getAppointments(userId: String): Result<List<Appointment>> =
        dao.getAppointments(userId)

    fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = dao.listenAppointments(userId, onDataChange, onError)
    // ^----------------------- ADD RETURN TYPE HERE
    // The single expression body `dao.listenAppointments(...)` will now correctly
    // be expected to return a ListenerRegistration, which it does (from the updated DAO).
}