package com.example.medipoint.Repository

import com.example.medipoint.Data.MedicalRecord
import com.example.medipoint.Data.MedicalRecordDao
// Import ListenerRegistration if you add the listener function
// import com.google.firebase.firestore.ListenerRegistration

class MedicalRecordRepository(private val medicalRecordDao: MedicalRecordDao) {

    suspend fun addMedicalRecord(userId: String, record: MedicalRecord): Result<MedicalRecord> {
        return medicalRecordDao.addMedicalRecord(userId, record)
    }

    suspend fun getMedicalRecords(userId: String): Result<List<MedicalRecord>> {
        return medicalRecordDao.getMedicalRecords(userId)
    }

    suspend fun getMedicalRecordById(userId: String, recordId: String): Result<MedicalRecord?> {
        return medicalRecordDao.getMedicalRecordById(userId, recordId)
    }

    suspend fun updateMedicalRecord(userId: String, record: MedicalRecord): Result<Unit> {
        return medicalRecordDao.updateMedicalRecord(userId, record)
    }

    suspend fun deleteMedicalRecord(userId: String, recordId: String): Result<Unit> {
        return medicalRecordDao.deleteMedicalRecord(userId, recordId)
    }

    // Optional: If you implement the listener in your DAO
    /*
    fun listenMedicalRecords(
        userId: String,
        onDataChange: (List<MedicalRecord>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration { // Or 'Any' if you want to keep DAO abstract from Firestore
        return medicalRecordDao.listenMedicalRecords(userId, onDataChange, onError)
    }
    */
}
