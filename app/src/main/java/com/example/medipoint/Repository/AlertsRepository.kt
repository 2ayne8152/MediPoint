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

    suspend fun addAlertToFirestore(alert: Alerts): Boolean {
        return try {
            val alertData = hashMapOf(
                "title" to alert.title,
                "message" to alert.message,
                "date" to alert.date,
                "userId" to alert.userId
            )

            val docRef = firestore.collection("alerts").add(alertData).await()

            // Insert the same alert into Room (for local persistence)
            val alertWithId = alert.copy(id = docRef.id)
            alertsDao.insertAlerts(listOf(alertWithId))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
