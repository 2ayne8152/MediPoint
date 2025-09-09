package com.example.medipoint.Data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreAppointmentDao(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AppointmentDao {

    override suspend fun addAppointment(appointment: Appointment): Result<Unit> {
        return try {
            db.collection("appointments")
                .add(appointment)
                .await()
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
        this.toObject(Appointment::class.java)
    } catch (e: Exception) {
        null
    }
}
