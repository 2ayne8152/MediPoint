package com.example.medipoint.Data


data class MedicalInfoEntity(
    val userId: String, // Links to the current user
    val bloodType: String? = null,
    val insuranceProvider: String? = null,
    val allergies: String? = null, // e.g., "Peanut, Penicillin"
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
)