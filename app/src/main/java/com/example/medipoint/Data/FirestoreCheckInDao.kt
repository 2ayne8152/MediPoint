package com.example.medipoint.Data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCheckInDao(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CheckInDao {
    override suspend fun addCheckInRecord(appointmentId: String, record: CheckInRecord): Result<Unit> {
        return try {
            db.collection("appointments")
                .document(appointmentId)
                .collection("checkin")
                .document(record.userId)
                .set(record)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCheckInRecord(appointmentId: String, userId: String): Result<CheckInRecord?> {
        return try {
            val doc = db.collection("appointments")
                .document(appointmentId)
                .collection("checkin")
                .document(userId)
                .get()
                .await()
            Result.success(doc.toObject(CheckInRecord::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
