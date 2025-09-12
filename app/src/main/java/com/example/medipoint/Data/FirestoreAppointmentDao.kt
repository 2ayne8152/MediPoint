package com.example.medipoint.Data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirestoreAppointmentDao(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AppointmentDao {

    override suspend fun addAppointment(appointment: Appointment): Result<Appointment> {
        return try {
            val existing = db.collection("appointments")
                .whereEqualTo("doctorName", appointment.doctorName)
                .whereEqualTo("date", appointment.date)
                .whereEqualTo("time", appointment.time)
                .get()
                .await()

            if (!existing.isEmpty) return Result.failure(Exception("Doctor already has appointment at this time"))

            val docRef = db.collection("appointments").add(appointment).await()
            Result.success(appointment.copy(id = docRef.id))
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

            val appointments = snapshot.documents.mapNotNull { it.toAppointment() }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenAppointments(
        userId: String,
        onDataChange: (List<Appointment>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { onError(e); return@addSnapshotListener }
                val appointments = snapshot?.documents?.mapNotNull { it.toAppointment() } ?: emptyList()
                onDataChange(appointments)
            }
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("status", "Cancelled")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun DocumentSnapshot.toAppointment(): Appointment? {
    return try {
        this.toObject(Appointment::class.java)?.copy(id = this.id)
    } catch (e: Exception) {
        null
    }
}

