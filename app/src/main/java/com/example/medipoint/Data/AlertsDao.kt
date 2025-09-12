package com.example.medipoint.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface AlertsDao {

    // Insert a list of alerts
    @Insert
    suspend fun insertAlerts(alerts: List<Alerts>)

    // Get all alerts
    @Query("SELECT * FROM alerts")
    suspend fun getAllAlerts(): List<Alerts>

    // Delete a single alert
    @Delete
    suspend fun delete(alert: Alerts)

    // Delete all alerts (Optional: If you need to delete all alerts)
    @Query("DELETE FROM alerts")
    suspend fun deleteAllAlerts()
}