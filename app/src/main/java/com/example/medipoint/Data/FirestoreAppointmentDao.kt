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
            // Check if appointment already exists for same doctor/date/time
            val existing = db.collection("appointments")
                .whereEqualTo("doctorName", appointment.doctorName)
                .whereEqualTo("date", appointment.date)
                .whereEqualTo("time", appointment.time)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("This doctor already has an appointment at that time."))
            }

            // Save appointment and grab Firestore document ID
            val docRef = db.collection("appointments").document()
            val appointmentWithId = appointment.copy(id = docRef.id)

            // Save appointment
            docRef.set(appointmentWithId).await()

            // 🔹 Create default CheckInRecord
            val checkInRecord = CheckInRecord(
                checkedIn = false,
                userId = appointment.userId,
                appointmentId = docRef.id
            )
            docRef.collection("checkin")
                .document(appointment.userId)
                .set(checkInRecord)
                .await()

            // Return success
            Result.success(appointmentWithId)
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

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("appointments")
                .document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
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
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { it.toAppointment() } ?: emptyList()
                onDataChange(appointments)
            }
    }
}

private fun DocumentSnapshot.toAppointment(): Appointment? {
    return try {
        val appointment = this.toObject(Appointment::class.java)
        appointment?.copy(id = this.id) // override with Firestore doc id
    } catch (e: Exception) {
        null
    }
}
