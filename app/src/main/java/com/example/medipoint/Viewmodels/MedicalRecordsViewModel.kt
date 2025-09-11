package com.example.medipoint.Viewmodels // Or your preferred ViewModel package

import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope // Not strictly needed for the changes here but good to have
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao // Assuming still used by Repository
import com.example.medipoint.Data.MedicalRecordDetails // Import the new data class
import com.example.medipoint.Data.PrescribedMedication
import com.example.medipoint.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// import kotlinx.coroutines.flow.collectLatest // Not directly used in the changed parts but fine to keep
// import kotlinx.coroutines.launch // Not directly used in the changed parts but fine to keep
import kotlin.random.Random

class MedicalRecordsViewModel(
    private val repository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao())
) : ViewModel() {

    private val _medicalRecords = MutableStateFlow<List<Appointment>>(emptyList())

    // --- Statistics StateFlows ---
    private val _appointmentTypeStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appointmentTypeStats: StateFlow<Map<String, Int>> = _appointmentTypeStats.asStateFlow()

    private val _statusStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statusStats: StateFlow<Map<String, Int>> = _statusStats.asStateFlow()

    private val _frequentMedications = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val frequentMedications: StateFlow<List<Pair<String, Int>>> = _frequentMedications.asStateFlow()

    private val _uniqueMedications = MutableStateFlow<Set<String>>(emptySet())
    val uniqueMedications: StateFlow<Set<String>> = _uniqueMedications.asStateFlow()

    private val _typesPerDoctorStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val typesPerDoctorStats: StateFlow<Map<String, Map<String, Int>>> = _typesPerDoctorStats.asStateFlow()

    private val _statusPerTypeStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val statusPerTypeStats: StateFlow<Map<String, Map<String, Int>>> = _statusPerTypeStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var appointmentsListenerRegistration: ListenerRegistration? = null

    private val mockMedicationsList = listOf(
        PrescribedMedication("Amoxicillin", "250mg", "Thrice daily"),
        PrescribedMedication("Ibuprofen", "400mg", "As needed for pain"),
        PrescribedMedication("Lisinopril", "10mg", "Once daily"),
        PrescribedMedication("Metformin", "500mg", "Twice daily"),
        PrescribedMedication("Atorvastatin", "20mg", "Once daily at night"),
        PrescribedMedication("Omeprazole", "20mg", "Once daily before breakfast"),
        PrescribedMedication("Cetirizine", "10mg", "Once daily for allergies"),
        PrescribedMedication("Ventolin Inhaler", "2 puffs", "As needed for wheezing"),
        PrescribedMedication("Losartan", "50mg", "Once daily")
    )

    init {
        loadMedicalRecordsAndProcess()
    }

    private fun loadMedicalRecordsAndProcess() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not logged in."
            _appointmentTypeStats.value = emptyMap()
            _statusStats.value = emptyMap()
            _frequentMedications.value = emptyList()
            _uniqueMedications.value = emptySet()
            _typesPerDoctorStats.value = emptyMap()
            _statusPerTypeStats.value = emptyMap()
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _appointmentTypeStats.value = emptyMap()
        _statusStats.value = emptyMap()
        _frequentMedications.value = emptyList()
        _uniqueMedications.value = emptySet()
        _typesPerDoctorStats.value = emptyMap()
        _statusPerTypeStats.value = emptyMap()

        appointmentsListenerRegistration?.remove()

        appointmentsListenerRegistration = repository.listenAppointments(
            userId = userId,
            onDataChange = { appointments ->
                _medicalRecords.value = appointments

                // **MODIFIED: Call the updated simulation function**
                val appointmentsWithSimulatedDetails = simulateMedicalDetails(appointments)

                calculateAppointmentTypeStats(appointmentsWithSimulatedDetails)
                calculateStatusStats(appointmentsWithSimulatedDetails)
                // **MODIFIED: Pass the updated list to this function too**
                calculateMedicationInsights(appointmentsWithSimulatedDetails)
                calculateTypesPerDoctorStats(appointmentsWithSimulatedDetails)
                calculateStatusPerTypeStats(appointmentsWithSimulatedDetails)

                _isLoading.value = false
            },
            onError = { exception ->
                _errorMessage.value = "Error fetching records: ${exception.message}"
                _appointmentTypeStats.value = emptyMap()
                _statusStats.value = emptyMap()
                _frequentMedications.value = emptyList()
                _uniqueMedications.value = emptySet()
                _typesPerDoctorStats.value = emptyMap()
                _statusPerTypeStats.value = emptyMap()
                _isLoading.value = false
            }
        )
    }

    // **MODIFIED: Renamed and updated simulation logic**
    private fun simulateMedicalDetails(appointments: List<Appointment>): List<Appointment> {
        return appointments.map { appointment ->
            // Decide if this appointment should have simulated medical details
            if (Random.nextFloat() < 0.7) { // 70% of appointments get some simulated details

                // Start with existing medicalDetails or create a new one
                var currentMedicalDetails = appointment.medicalDetails ?: MedicalRecordDetails(appointmentId = appointment.id)

                // Simulate prescriptions (only if this appointment gets details)
                if (Random.nextFloat() < 0.8) { // 80% of those with details get prescriptions
                    val numberOfMeds = Random.nextInt(1, 3)
                    val selectedMeds = mockMedicationsList.shuffled().take(numberOfMeds)
                    currentMedicalDetails = currentMedicalDetails.copy(prescribedMedications = selectedMeds)
                }

                // Optionally, simulate other fields like diagnosis or reasonForVisit if needed for stats
                // For example:
                if (currentMedicalDetails.reasonForVisit.isBlank() && Random.nextFloat() < 0.5) {
                    currentMedicalDetails = currentMedicalDetails.copy(reasonForVisit = "Simulated Visit Reason")
                }
                if (currentMedicalDetails.diagnosis.isBlank() && Random.nextFloat() < 0.6) {
                    currentMedicalDetails = currentMedicalDetails.copy(diagnosis = "Simulated Diagnosis")
                }
                // Not simulating checkInRecord for simplicity in this statistics view,
                // unless you have a specific statistic related to it.

                appointment.copy(medicalDetails = currentMedicalDetails)
            } else {
                // No simulated medical details for this appointment, return as is
                // (medicalDetails will remain whatever it was, likely null or its original state)
                appointment
            }
        }
    }

    private fun calculateAppointmentTypeStats(appointments: List<Appointment>) {
        _appointmentTypeStats.value = appointments
            .groupBy { it.appointmentType.ifBlank { "Unspecified" } }
            .mapValues { it.value.size }
    }

    private fun calculateStatusStats(appointments: List<Appointment>) {
        _statusStats.value = appointments
            .groupBy { it.status.trim().ifBlank { "Unknown" } }
            .mapValues { it.value.size }
    }

    // **MODIFIED: Updated to access medications via medicalDetails**
    private fun calculateMedicationInsights(appointments: List<Appointment>) {
        val allPrescribedMedsList = appointments
            .mapNotNull { it.medicalDetails } // Get only appointments that have medicalDetails
            .flatMap { it.prescribedMedications } // Then get the prescribedMedications from those details

        _frequentMedications.value = allPrescribedMedsList
            .groupBy { it.name } // PrescribedMedication data class should have a 'name' field
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        _uniqueMedications.value = allPrescribedMedsList
            .map { it.name } // PrescribedMedication data class should have a 'name' field
            .toSet()
    }

    private fun calculateTypesPerDoctorStats(appointments: List<Appointment>) {
        _typesPerDoctorStats.value = appointments
            .groupBy { it.doctorName.ifBlank { "Unknown Doctor" } }
            .mapValues { doctorEntry ->
                doctorEntry.value.groupBy { it.appointmentType.ifBlank { "Unspecified Type" } }
                    .mapValues { it.value.size }
            }
    }

    private fun calculateStatusPerTypeStats(appointments: List<Appointment>) {
        _statusPerTypeStats.value = appointments
            .groupBy { it.appointmentType.ifBlank { "Unspecified Type" } }
            .mapValues { typeEntry ->
                typeEntry.value.groupBy { it.status.trim().ifBlank { "Unknown Status" } }
                    .mapValues { it.value.size }
            }
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListenerRegistration?.remove()
    }
}
