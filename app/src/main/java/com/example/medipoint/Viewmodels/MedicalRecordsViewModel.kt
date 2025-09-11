package com.example.medipoint.Viewmodels // Or your preferred ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Data.PrescribedMedication
import com.example.medipoint.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // For reacting to medicalRecords changes
import kotlinx.coroutines.launch
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

    // 5. Prescribed Medication Insights
    private val _frequentMedications = MutableStateFlow<List<Pair<String, Int>>>(emptyList()) // Pair of (MedName, Count)
    val frequentMedications: StateFlow<List<Pair<String, Int>>> = _frequentMedications.asStateFlow()

    private val _uniqueMedications = MutableStateFlow<Set<String>>(emptySet())
    val uniqueMedications: StateFlow<Set<String>> = _uniqueMedications.asStateFlow()

    // 6. Combined Statistics
    // Types of appointments per doctor: Map<DoctorName, Map<AppointmentType, Count>>
    private val _typesPerDoctorStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val typesPerDoctorStats: StateFlow<Map<String, Map<String, Int>>> = _typesPerDoctorStats.asStateFlow()

    // Status of appointments per type: Map<AppointmentType, Map<Status, Count>>
    private val _statusPerTypeStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val statusPerTypeStats: StateFlow<Map<String, Map<String, Int>>> = _statusPerTypeStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var appointmentsListenerRegistration: ListenerRegistration? = null

    // Define the known appointment types if you want to ensure all are shown, even with 0 count
    // Or, you can derive them purely from the fetched data.
    // For simplicity now, we'll derive from data, but you could pre-populate keys.
    // private val knownAppointmentTypes = listOf("General Checkup", "Consultation", "Follow-up")
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
            // Clear all stats
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
        // Clear all stats while loading
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
                _medicalRecords.value = appointments // Keep the raw list if needed

                // Simulate prescriptions for demo purposes - THIS DOES NOT SAVE TO FIRESTORE
                val appointmentsWithSimulatedMeds = simulatePrescriptions(appointments)

                calculateAppointmentTypeStats(appointmentsWithSimulatedMeds)
                calculateStatusStats(appointmentsWithSimulatedMeds)
                calculateMedicationInsights(appointmentsWithSimulatedMeds) // Process simulated meds
                calculateTypesPerDoctorStats(appointmentsWithSimulatedMeds)
                calculateStatusPerTypeStats(appointmentsWithSimulatedMeds)

                _isLoading.value = false
            },
            onError = { exception ->
                _errorMessage.value = "Error fetching records: ${exception.message}"
                // Clear all stats on error
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
    private fun simulatePrescriptions(appointments: List<Appointment>): List<Appointment> {
        return appointments.map { appointment ->
            // For about 60% of appointments, assign some meds
            if (Random.nextFloat() < 0.6) {
                val numberOfMeds = Random.nextInt(1, 3) // 1 or 2 medications
                val selectedMeds = mockMedicationsList.shuffled().take(numberOfMeds)
                appointment.copy(prescribedMedications = selectedMeds)
            } else {
                appointment.copy(prescribedMedications = emptyList())
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

    private fun calculateMedicationInsights(appointments: List<Appointment>) {
        val allPrescribedMedsList = appointments.flatMap { it.prescribedMedications }

        // Most frequent medications
        _frequentMedications.value = allPrescribedMedsList
            .groupBy { it.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second } // Sort by count, descending
            .take(5) // Take top 5 for example

        // Unique medications
        _uniqueMedications.value = allPrescribedMedsList
            .map { it.name }
            .toSet()
    }

    private fun calculateTypesPerDoctorStats(appointments: List<Appointment>) {
        _typesPerDoctorStats.value = appointments
            .groupBy { it.doctorName.ifBlank { "Unknown Doctor" } } // Group by Doctor
            .mapValues { doctorEntry ->
                doctorEntry.value.groupBy { it.appointmentType.ifBlank { "Unspecified Type" } } // Then group by Type
                    .mapValues { it.value.size } // Count for each type
            }
    }

    private fun calculateStatusPerTypeStats(appointments: List<Appointment>) {
        _statusPerTypeStats.value = appointments
            .groupBy { it.appointmentType.ifBlank { "Unspecified Type" } } // Group by Type
            .mapValues { typeEntry ->
                typeEntry.value.groupBy { it.status.trim().ifBlank { "Unknown Status" } } // Then group by Status
                    .mapValues { it.value.size } // Count for each status
            }
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListenerRegistration?.remove()
    }
}

