package com.example.medipoint.Data

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val doctorName: String = "",
    val appointmentType: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Scheduled",
    val notes: String = "",
    val checkInRecord: CheckInRecord? = null // optional: embed latest check-in
)