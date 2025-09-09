package com.example.medipoint.Data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreAppointmentDao(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AppointmentDao {

    override suspend fun addAppointment(appointment: Appointment): Result<Unit> {
        return try {
            val map = hashMapOf(
                "doctorName" to appointment.doctorName,
                "appointmentType" to appointment.appointmentType,
                "date" to appointment.date,
                "time" to appointment.time,
                "status" to appointment.status,
                "notes" to appointment.notes,
                "userId" to appointment.userId // 🔑 make user-specific
            )
            db.collection("appointments").add(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppointments(userId: String): Result<List<Appointment>> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val appointments = snapshot.documents.map { it.toAppointment() }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.map { it.toAppointment() } ?: emptyList()
                onDataChange(appointments)
            }
    }
}
