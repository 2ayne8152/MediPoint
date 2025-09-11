package com.example.medipoint.Data

data class Appointment(
    // Core Appointment Info
    val id: String = "", // Appointment Id
    val userId: String = "",
    val doctorName: String = "",
    val appointmentType: String = "", // e.g., "Consultation", "Follow-up", "Telehealth"
    val date: String = "", // Consider storing as a Timestamp or ISO 8601 string for better sorting/querying
    val time: String = "", // Consider storing as part of a Timestamp with the date
    val status: String = "Scheduled", // e.g., "Completed", "Cancelled", "Rescheduled"
    val notes: String = "",           // General notes about the appointment itself is
    val checkInRecord: CheckInRecord? = null // Embeds the latest check-in for this appointment
)