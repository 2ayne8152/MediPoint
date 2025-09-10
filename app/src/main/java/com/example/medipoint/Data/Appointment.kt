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

    // --- Medical Record Specific Fields ---

    // Reason for Visit / Symptoms
    val reasonForVisit: String = "",      // User's description of symptoms or concern

    // Outcome / Doctor's Notes
    val diagnosis: String = "",           // Doctor’s primary diagnosis
    val secondaryDiagnoses: List<String> = emptyList(), // Any other diagnoses

    // Using a more structured PrescribedMedication data class is often better
    // val prescribedMedications: List<String> = emptyList(), // Simple list of medication names
    val prescribedMedications: List<PrescribedMedication> = emptyList(), // More structured

    val treatmentPlan: String = "",       // Detailed treatment plan
    val followUpInstructions: String = "",// e.g., "Come back in 2 weeks", "Perform X test"

//    // Attachments - These would typically be URLs pointing to files in Firebase Storage or another CDN
//    val prescriptionAttachmentUrls: List<String> = emptyList(), // URLs to scanned/digital prescriptions
//    val labResultAttachmentUrls: List<String> = emptyList(),    // URLs to lab result documents/images
//    val medicalImageAttachmentUrls: List<String> = emptyList(), // URLs to X-rays, MRIs, etc.

//    // Billing Info (Optional - ensure you handle privacy and security appropriately if storing sensitive financial data)
//    val consultationFee: Double? = null,
//    val insuranceProvider: String? = null, // Could be same as user's main insurance
//    val insurancePolicyNumber: String? = null,
//    val amountCoveredByInsurance: Double? = null,
//    val outOfPocketCost: Double? = null,
//    val paymentStatus: String? = null, // e.g., "Paid", "Pending", "Due"
//    val paymentMethod: String? = null,
//    val transactionId: String? = null,

    // Check-In Record (as previously defined)
    val checkInRecord: CheckInRecord? = null // Embeds the latest check-in for this appointment
)