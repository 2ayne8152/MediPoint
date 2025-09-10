package com.example.medipoint.Data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

            // Add appointment
            val map = hashMapOf(
                "doctorName" to appointment.doctorName,
                "appointmentType" to appointment.appointmentType,
                "date" to appointment.date,
                "time" to appointment.time,
                "status" to appointment.status,
                "notes" to appointment.notes,
                "userId" to appointment.userId
            )

            // Save and grab Firestore document ID
            val docRef = db.collection("appointments").add(map).await()

            // Return a **new copy** with Firestore id
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
    ) {
        db.collection("appointments")
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

