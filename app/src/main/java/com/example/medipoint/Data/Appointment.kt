package com.example.medipoint.Data

data class Appointment(
    val id: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val appointmentType: String = "",
    val date: String = "",
    val time: String = "",
    val doctorPhone: String = "",
    val status: String = "Scheduled", // Confirmed, Cancelled, Completed
    val location: String = "",
    val room: String = "",
    val notes: String = ""
)