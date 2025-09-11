package com.example.medipoint.Repository

import android.util.Log
import com.example.medipoint.Data.MedicalInfoDao
import com.example.medipoint.Data.MedicalInfoEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MedicalInfoRepository(
    private val medicalInfoDao: MedicalInfoDao,
    private val firestoreDb: FirebaseFirestore
) {

    // Observe local Room data
    fun getLocalMedicalInfo(userId: String): Flow<MedicalInfoEntity?> {
        return medicalInfoDao.getMedicalInfoByUserId(userId).flowOn(Dispatchers.IO)
    }

    // Fetch from Firestore and update Room
    suspend fun refreshMedicalInfoFromFirestore(userId: String): Result<Unit> {
        return try {
            val documentSnapshot = firestoreDb.collection("users") // Or your path
                .document(userId)
                .collection("medicalInfo") // Assuming subcollection
                .document("info") // Assuming a single doc for medical info
                .get()
                .await()

            if (documentSnapshot.exists()) {
                val firestoreInfo = documentSnapshot.toObject(MedicalInfoEntity::class.java)
                firestoreInfo?.let {
                    // It's crucial that MedicalInfoEntity from Firestore has the userId
                    // If not, you need to construct it:
                    val infoToCache = it.copy(userId = userId) // Ensure userId is set
                    medicalInfoDao.insertOrUpdateMedicalInfo(infoToCache)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Failed to parse medical info from Firestore."))
            } else {
                // User might not have medical info set up yet, clear local cache if it exists
                medicalInfoDao.deleteMedicalInfoByUserId(userId)
                Result.success(Unit) // Success, just no data
            }
        } catch (e: Exception) {
            Log.e("MedicalInfoRepo", "Error refreshing medical info from Firestore", e)
            Result.failure(e)
        }
    }

    // Save to Firestore and then update Room
    suspend fun saveMedicalInfo(medicalInfo: MedicalInfoEntity): Result<Unit> {
        return try {
            // Firestore expects a map or a POJO (without the userId if it's the document ID)
            // Assuming medicalInfo document ID is 'info' under a subcollection 'medicalInfo'
            firestoreDb.collection("users")
                .document(medicalInfo.userId)
                .collection("medicalInfo")
                .document("info") // Or use medicalInfo.userId if medical_info is a top-level collection keyed by userId
                .set(medicalInfo) // Firestore can serialize MedicalInfoEntity directly
                .await()

            // Update local cache
            medicalInfoDao.insertOrUpdateMedicalInfo(medicalInfo)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MedicalInfoRepo", "Error saving medical info", e)
            Result.failure(e)
        }
    }

    suspend fun clearLocalMedicalInfo(userId: String) {
        withContext(Dispatchers.IO) {
            medicalInfoDao.deleteMedicalInfoByUserId(userId)
        }
    }
}
