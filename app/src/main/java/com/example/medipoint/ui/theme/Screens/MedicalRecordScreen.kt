package com.example.medipoint.ui.theme.Screens // Or your preferred UI package

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medipoint.Data.Appointment // Ensure this is your expanded Appointment class
import com.example.medipoint.Viewmodels.MedicalRecordsViewModel // Adjust import if needed
import androidx.lifecycle.viewmodel.compose.viewModel // For viewModel()


@Composable
fun MedicalRecordScreen(
    viewModel: MedicalRecordsViewModel = viewModel() // Get instance of ViewModel
) {
    val medicalRecords by viewModel.medicalRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
            } else if (medicalRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No medical records found.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(medicalRecords, key = { it.id }) { appointment -> // Use appointment.id as key
                        MedicalRecordItem(appointment = appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalRecordItem(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Basic Appointment Info
            Text(
                "${appointment.date} at ${appointment.time}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Doctor: ${appointment.doctorName}" + if (appointment.doctorSpecialization.isNotBlank()) " (${appointment.doctorSpecialization})" else "",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Type: ${appointment.appointmentType}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (appointment.status.isNotBlank()) {
                Text("Status: ${appointment.status}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Purpose/Reason for Visit
            if (appointment.reasonForVisit.isNotBlank()) {
                SectionTitle("Reason for Visit")
                Text(appointment.reasonForVisit, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Outcome / Notes
            if (appointment.diagnosis.isNotBlank() || !appointment.prescribedMedications.isNullOrEmpty() || appointment.treatmentPlan.isNotBlank() || appointment.followUpInstructions.isNotBlank() || appointment.notes.isNotBlank()) {
                SectionTitle("Outcome / Notes")
                if (appointment.diagnosis.isNotBlank()) {
                    SubSectionTitle("Diagnosis")
                    Text(appointment.diagnosis, style = MaterialTheme.typography.bodyMedium)
                }
                if (!appointment.prescribedMedications.isNullOrEmpty()) {
                    SubSectionTitle("Prescribed Medications")
                    appointment.prescribedMedications.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                }
                if (appointment.treatmentPlan.isNotBlank()) {
                    SubSectionTitle("Treatment Plan")
                    Text(appointment.treatmentPlan, style = MaterialTheme.typography.bodyMedium)
                }
                if (appointment.followUpInstructions.isNotBlank()) {
                    SubSectionTitle("Follow-up Instructions")
                    Text(appointment.followUpInstructions, style = MaterialTheme.typography.bodyMedium)
                }
                if (appointment.notes.isNotBlank() && appointment.notes != appointment.reasonForVisit) { // Avoid duplicate if general notes are same as reason
                    SubSectionTitle("Additional Notes")
                    Text(appointment.notes, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


//            // Attachments
//            if (!appointment.prescriptionAttachmentUrls.isNullOrEmpty() || !appointment.labResultUrls.isNullOrEmpty() || !appointment.medicalImageUrls.isNullOrEmpty()) {
//                SectionTitle("Attachments")
//                // You'd typically make these clickable to open/download
//                appointment.prescriptionAttachmentUrls?.forEach { url -> Text("Prescription: View", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { /* TODO: Handle click */ }) }
//                appointment.labResultUrls?.forEach { url -> Text("Lab Result: View", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { /* TODO: Handle click */ }) }
//                appointment.medicalImageUrls?.forEach { url -> Text("Medical Image: View", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { /* TODO: Handle click */ }) }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//
//            // Billing Info (optional)
//            if (appointment.consultationFee != null || !appointment.insuranceCoverage.isNullOrBlank() || !appointment.paymentStatus.isNullOrBlank()) {
//                SectionTitle("Billing Info (Optional)")
//                appointment.consultationFee?.let { Text("Fee: $$it", style = MaterialTheme.typography.bodyMedium) }
//                appointment.insuranceCoverage?.takeIf { it.isNotBlank() }?.let { Text("Insurance: $it", style = MaterialTheme.typography.bodyMedium) }
//                appointment.paymentStatus?.takeIf { it.isNotBlank() }?.let { Text("Payment: $it", style = MaterialTheme.typography.bodyMedium) }
//            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SubSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}
