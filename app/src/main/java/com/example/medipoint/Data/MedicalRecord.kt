package com.example.medipoint.Data // Or your relevant data package

import com.google.firebase.firestore.DocumentId // For Firestore document ID
import com.google.firebase.firestore.ServerTimestamp // For server-generated timestamps
import java.util.Date // For date fields

data class MedicalRecord(
    @DocumentId // This will be auto-populated with the Firestore document ID when fetched
    val id: String = "",

    val userId: String = "",           // ID of the user this medical record belongs to (essential)
    val appointmentId: String? = null, // Optional: Links to an Appointment made via your app

    // === Core Record Metadata ===
    val recordTitle: String = "",      // e.g., "Annual Check-up Summary", "Lab Results - Blood Panel"
    val dateOfService: Date? = null,   // When the medical service/event occurred. Use Date or Timestamp.
    // Consider storing as Firestore Timestamp for better querying.
    val recordType: String = "",       // User-defined or predefined: "Doctor's Note", "Lab Report",
    // "Imaging Result", "Prescription", "Vaccination Record", "Discharge Summary"
    val issuingOrganization: String? = null, // e.g., "City General Hospital", "Dr. Smith's Clinic"
    val creatingDoctorName: String? = null, // Name of the doctor/provider associated with this record

    @ServerTimestamp // Automatically set by Firestore on creation
    val dateCreated: Date? = null,     // When this record was added to your app
    @ServerTimestamp // Automatically set by Firestore on creation or update (if configured in rules/functions)
    val dateLastUpdated: Date? = null, // When this record was last modified in your app

    // === Details (similar to your MedicalRecordDetails) ===
    val reasonForVisit: String? = null,      // User's description or chief complaint
    val diagnosis: String? = null,           // Doctor’s primary diagnosis
    val secondaryDiagnoses: List<String> = emptyList(), // Any other diagnoses
    val prescribedMedications: List<PrescribedMedication> = emptyList(), // From your existing PrescribedMedication data class
    val treatmentPlan: String? = null,       // Detailed treatment plan
    val followUpInstructions: String? = null,// e.g., "Come back in 2 weeks", "Perform X test"
    val generalNotes: String? = null,        // Any other relevant notes for this record

    // === Attachments ===
    // Store URLs to files in Cloud Storage
    val attachmentUrls: List<String> = emptyList(), // e.g., PDFs of lab reports, images

    // === Original Check-In (if applicable and from your app) ===
    // If this record was directly tied to an appointment with a check-in through your app,
    // you might want to embed or link the CheckInRecord.
    // However, if CheckInRecord is ONLY for appointments made via your app,
    // and this MedicalRecord could be from an external source, embedding it directly
    // might not always make sense.
    // Consider if `checkInRecord` from your old `MedicalRecordDetails` should be here
    // or if the link via `appointmentId` (which might have its own check-in) is sufficient.
    // For now, I'll keep it as it was in your previous structure:
    val checkInRecord: CheckInRecord? = null // From your existing CheckInRecord data class
    // Make this nullable, as external records won't have it.
)

// You would still have your PrescribedMedication.kt and CheckInRecord.kt as separate data classes:
// data class PrescribedMedication(val name: String = "", val dosage: String = "", val frequency: String = "")
// data class CheckInRecord(val id: String = "", /* ... other fields ... */)

