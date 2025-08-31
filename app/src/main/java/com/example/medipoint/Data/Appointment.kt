package com.example.medipoint.Data

data class Appointment(
    val id: String = "",
    val doctorName: String = "",
    val appointmentType: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Scheduled",
    val location: String = "",
    val notes: String = "",
    val checkInRecord: CheckInRecord? = null
)