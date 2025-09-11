package com.example.medipoint.Data // Or your DAO package

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

class FirestoreMedicalRecordDao(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MedicalRecordDao {

    // Using Option 1: Subcollection under user for these examples
    private fun userMedicalRecordsCollection(userId: String) =
        db.collection("users").document(userId).collection("medical_records")

    override suspend fun addMedicalRecord(
        userId: String,
        record: MedicalRecord
    ): Result<MedicalRecord> {
        return try {
            val docRef = userMedicalRecordsCollection(userId).add(record).await()
            // Important: The 'record' passed in doesn't have the Firestore ID yet.
            // We get the ID from docRef.id and return a new copy of the record with this ID.
            Result.success(record.copy(id = docRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedicalRecords(userId: String): Result<List<MedicalRecord>> {
        return try {
            val snapshot = userMedicalRecordsCollection(userId).get().await()
            // Firestore ktx .toObjects<MedicalRecord>() should automatically map document IDs
            // if your MedicalRecord data class has @DocumentId on the 'id' field.
            val records = snapshot.toObjects<MedicalRecord>()
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedicalRecordById(
        userId: String,
        recordId: String
    ): Result<MedicalRecord?> {
        return try {
            val documentSnapshot =
                userMedicalRecordsCollection(userId).document(recordId).get().await()
            if (documentSnapshot.exists()) {
                // .toObject<MedicalRecord>() should also map the document ID
                val record = documentSnapshot.toObject<MedicalRecord>()
                Result.success(record)
            } else {
                Result.success(null) // Record not found
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMedicalRecord(userId: String, record: MedicalRecord): Result<Unit> {
        return try {
            // Ensure the record has an ID, as we need it to specify which document to update.
            if (record.id.isBlank()) {
                return Result.failure(IllegalArgumentException("MedicalRecord ID cannot be blank for update."))
            }
            userMedicalRecordsCollection(userId).document(record.id).set(record)
                .await() // 'set' will overwrite or create
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedicalRecord(userId: String, recordId: String): Result<Unit> {
        return try {
            if (recordId.isBlank()) {
                return Result.failure(IllegalArgumentException("MedicalRecord ID cannot be blank for delete."))
            }
            userMedicalRecordsCollection(userId).document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
