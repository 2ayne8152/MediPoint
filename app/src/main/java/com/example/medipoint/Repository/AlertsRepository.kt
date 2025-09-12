package com.example.medipoint.Repository

import com.example.medipoint.Data.Alerts
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AlertsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val alertsCollection = firestore.collection("alerts")

    // Add an alert to Firestore safely
    suspend fun addAlertToFirestore(alert: Alerts): Result<Alerts> {
        return try {
            alertsCollection.document(alert.id).set(alert).await()
            Result.success(alert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch alerts for a user
    suspend fun getAlertsForUser(userId: String): Result<List<Alerts>> {
        return try {
            val snapshot = alertsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val alerts = snapshot.documents.mapNotNull { it.toObject(Alerts::class.java) }
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Generate a unique alert ID
    fun generateAlertId(): String = UUID.randomUUID().toString()
}
