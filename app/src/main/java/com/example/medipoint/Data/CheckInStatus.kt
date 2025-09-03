package com.example.medipoint.Data

data class CheckInRecord(
    val id: String = "",
    val appointmentId: String = "",
    val userId: String = "",
    val checkedIn: Boolean = false,
    val checkInTime: Long? = null,
    val checkInLat: Double? = null,
    val checkInLng: Double? = null
)