package com.example.medipoint.Data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_info")
data class MedicalInfoEntity(
    @PrimaryKey
    val userId: String,
    val bloodType: String? = null,
    val insuranceProvider: String? = null,
    val allergies: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
)