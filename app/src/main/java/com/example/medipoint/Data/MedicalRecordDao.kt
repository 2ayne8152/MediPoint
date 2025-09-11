package com.example.medipoint.Data // Or your DAO package

import com.example.medipoint.Data.MedicalRecord

interface MedicalRecordDao {
    // Changed: `addMedicalRecord` now includes userId
    suspend fun addMedicalRecord(userId: String, record: MedicalRecord): Result<MedicalRecord>

    suspend fun getMedicalRecords(userId: String): Result<List<MedicalRecord>>

    // Changed: `getMedicalRecordById` now includes userId (important for path/security)
    suspend fun getMedicalRecordById(userId: String, recordId: String): Result<MedicalRecord?>

    // Changed: `updateMedicalRecord` now includes userId
    suspend fun updateMedicalRecord(userId: String, record: MedicalRecord): Result<Unit>

    // Changed: `deleteMedicalRecord` now includes userId
    suspend fun deleteMedicalRecord(userId: String, recordId: String): Result<Unit>

    // Optional: Listener function also needs userId
    // fun listenMedicalRecords(
    //     userId: String,
    //     onDataChange: (List<MedicalRecord>) -> Unit,
    //     onError: (Exception) -> Unit
    // ): Any // Return type for Firestore listener
}
