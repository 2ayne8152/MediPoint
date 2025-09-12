package com.example.medipoint.Repository

import android.content.Context
import com.example.medipoint.Data.Alerts
import com.example.medipoint.Data.MediPointDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AlertsRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val alertsDao = MediPointDatabase.getDatabase(context).alertsDao()

    private val db = FirebaseFirestore.getInstance()
    suspend fun fetchAlertsFromFirestore(userId: String) {
        val alertsRef = firestore.collection("alerts")
            .whereEqualTo("userId", userId)

        try {
            val snapshot = alertsRef.get().await()
            val alertsList = snapshot.documents.mapNotNull { document ->
                Alerts(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    message = document.getString("message") ?: "",
                    date = document.getString("date") ?: "",
                    userId = document.getString("userId") ?: ""
                )
            }
            // Insert fetched alerts into Room
            withContext(Dispatchers.IO) {
                alertsDao.insertAlerts(alertsList)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getAllAlertsFromRoom(): List<Alerts> {
        return withContext(Dispatchers.IO) {
            alertsDao.getAllAlerts()
        }
    }

    suspend fun insertAlertLocally(alert: Alerts) {
        withContext(Dispatchers.IO) {
            alertsDao.insertAlerts(listOf(alert))
        }
    }

    suspend fun deleteAllAlertsFromRoom() {
        withContext(Dispatchers.IO) {
            alertsDao.deleteAllAlerts()
        }
    }

    // In AlertsRepository.kt
    suspend fun addAlertToFirestore(alert: Alerts): Result<Boolean> {
        return try {
            db.collection("alerts")
                .document(alert.id)
                .set(alert)
                .await()  // Await the Firestore operation to complete
            Result.success(true)  // If the operation succeeds, return success
        } catch (e: Exception) {
            Result.failure(e)  // If the operation fails, return failure
        }
    }

}
