package com.example.medipoint.Data

data class Appointment(
    // Core Appointment Info
    val id: String = "", // Firestore document ID, should be populated after fetching
    val userId: String = "", // ID of the user this appointment belongs to

    // Doctor & Appointment Type
    val doctorName: String = "",
    val doctorSpecialization: String = "", // e.g., "Cardiology", "General Practice"
    val appointmentType: String = "", // e.g., "Consultation", "Follow-up", "Telehealth"

    // Date & Time
    val date: String = "", // Consider storing as a Timestamp or ISO 8601 string for better sorting/querying
    val time: String = "", // Consider storing as part of a Timestamp with the date

    // Status & General Notes
    val status: String = "Scheduled", // e.g., "Completed", "Cancelled", "Rescheduled"
    val notes: String = "",           // General notes about the appointment itself

    val medicalDetails: MedicalRecordDetails? = null
)
