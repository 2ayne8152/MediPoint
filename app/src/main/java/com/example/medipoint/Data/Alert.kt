package com.example.medipoint.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alerts(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val date: String,      // user-friendly date string
    val appointmentId: Int? = null,
    val userId: String = ""
)