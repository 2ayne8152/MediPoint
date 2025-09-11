package com.example.medipoint.Data

data class MedicalRecordDetails(
    val appointmentId: String = "", // Foreign key linking back to the Appointment's ID

    // Reason for Visit / Symptoms
    val reasonForVisit: String = "",      // User's description of symptoms or concern

    // Outcome / Doctor's Notes
    val diagnosis: String = "",           // Doctor’s primary diagnosis
    val secondaryDiagnoses: List<String> = emptyList(), // Any other diagnoses
    val prescribedMedications: List<PrescribedMedication> = emptyList(), // More structured
    val treatmentPlan: String = "",       // Detailed treatment plan
    val followUpInstructions: String = "",// e.g., "Come back in 2 weeks", "Perform X test"

    // Check-In Record
    val checkInRecord: CheckInRecord? = null // Embeds the latest check-in for this appointment related medical details
    // You could also store attachments URLs here if they are specific to the medical outcome
    // val prescriptionAttachmentUrls: List<String> = emptyList(),
    // val labResultAttachmentUrls: List<String> = emptyList(),
    // val medicalImageAttachmentUrls: List<String> = emptyList(),
)
