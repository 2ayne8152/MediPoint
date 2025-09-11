package com.example.medipoint.Data

data class PrescribedMedication(
    val name: String = "",
    val dosage: String = "", // e.g., "10mg", "1 tablet"
    val frequency: String = "" // e.g., "Once daily", "Twice a day"
    // You could add more fields like "duration", "notes", etc.
)