package com.example.medipoint.Viewmodels // Or your preferred ViewModel package

import androidx.activity.result.launch
import androidx.compose.foundation.layout.size
import androidx.core.util.remove
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import androidx.lifecycle.viewModelScope // Not strictly needed for the changes here but good to have
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao // Assuming still used by Repository
import com.example.medipoint.Data.FirestoreMedicalRecordDao
import com.example.medipoint.Data.MedicalRecord
import com.example.medipoint.Data.MedicalRecordDetails // Import the new data class
import com.example.medipoint.Data.PrescribedMedication
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.MedicalRecordRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.type.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import kotlinx.coroutines.flow.collectLatest // Not directly used in the changed parts but fine to keep
// import kotlinx.coroutines.launch // Not directly used in the changed parts but fine to keep
import kotlin.random.Random
import com.google.firebase.Timestamp

class MedicalRecordsViewModel(
    // Inject both repositories
    private val appointmentRepository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao()),
    private val medicalRecordRepository: MedicalRecordRepository = MedicalRecordRepository(
        FirestoreMedicalRecordDao()
    )
) : ViewModel() {

    // State for fetched data
    private val _rawAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _rawMedicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())

    // --- Statistics StateFlows ---
    // These might change based on what data they are derived from now

    // Statistics derived from Appointments
    private val _appointmentTypeStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appointmentTypeStats: StateFlow<Map<String, Int>> = _appointmentTypeStats.asStateFlow()

    private val _statusStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val statusStats: StateFlow<Map<String, Int>> = _statusStats.asStateFlow()

    private val _typesPerDoctorStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val typesPerDoctorStats: StateFlow<Map<String, Map<String, Int>>> = _typesPerDoctorStats.asStateFlow()

    private val _statusPerTypeStats = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val statusPerTypeStats: StateFlow<Map<String, Map<String, Int>>> = _statusPerTypeStats.asStateFlow()

    // Statistics derived from MedicalRecords (new)
    private val _frequentMedications = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val frequentMedications: StateFlow<List<Pair<String, Int>>> = _frequentMedications.asStateFlow()

    private val _uniqueMedicationsCount = MutableStateFlow(0) // Simplified from Set to just count
    val uniqueMedicationsCount: StateFlow<Int> = _uniqueMedicationsCount.asStateFlow()

    private val _recordTypeStats = MutableStateFlow<Map<String, Int>>(emptyMap()) // New stat
    val recordTypeStats: StateFlow<Map<String, Int>> = _recordTypeStats.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var appointmentsListenerReg: ListenerRegistration? = null

    private val predefinedMockMedications = listOf(
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

    private val mockRecordTypes = listOf("General Check-up", "Follow-up", "Consultation")
    private val mockDiagnoses = listOf("Common Cold", "Hypertension", "Type 2 Diabetes", "Allergic Rhinitis", "Bronchitis")


    init {
        loadAllDataAndProcess()
    }

    private fun resetStats() {
        _appointmentTypeStats.value = emptyMap()
        _statusStats.value = emptyMap()
        _typesPerDoctorStats.value = emptyMap()
        _statusPerTypeStats.value = emptyMap()
        _frequentMedications.value = emptyList()
        _uniqueMedicationsCount.value = 0
        _recordTypeStats.value = emptyMap()
    }
    private fun generateSimulatedMedicalRecords(userId: String, count: Int): List<MedicalRecord> {
        val records = mutableListOf<MedicalRecord>()
        repeat(count) {
            val numberOfMeds = Random.nextInt(1, 4)
            // Now it can access predefinedMockMedications because it's a member of the same class
            val selectedMeds = predefinedMockMedications.shuffled().take(numberOfMeds)

            records.add(
                MedicalRecord(
                    userId = userId,
                    recordTitle = "Simulated Record #${it + 1}",
                    recordType = mockRecordTypes.random(),
                    diagnosis = mockDiagnoses.random(),
                    prescribedMedications = selectedMeds,
                    reasonForVisit = "Simulated reason",
                    treatmentPlan = "Simulated plan",
                    issuingOrganization = "MediPoint Simulation Clinic"
                )
            )
        }
        return records
    }


    private fun loadAllDataAndProcess() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not logged in."
            resetStats()
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        resetStats()

        appointmentsListenerReg?.remove()
        // medicalRecordsListenerReg?.remove()

        appointmentsListenerReg = appointmentRepository.listenAppointments(
            userId = userId,
            onDataChange = { appointments ->
                _rawAppointments.value = appointments
                calculateAppointmentTypeStats(appointments)
                calculateStatusStats(appointments)
                calculateTypesPerDoctorStats(appointments)
                calculateStatusPerTypeStats(appointments)
            },
            onError = { exception ->
                _errorMessage.value = "Error fetching appointments: ${exception.message}"
                _isLoading.value = false
            }
        )

        val simulatedMedicalRecordsForMeds = generateSimulatedMedicalRecords(userId, 15) // Generate e.g., 15 simulated records
        // These are NOT saved to the DAO. They exist only in memory for this ViewModel instance.
        calculateMedicationInsights(simulatedMedicalRecordsForMeds)



        viewModelScope.launch {
            val medicalRecordsResult = medicalRecordRepository.getMedicalRecords(userId)
            medicalRecordsResult.onSuccess { records ->
                _rawMedicalRecords.value = records
                calculateMedicationInsights(records)
                calculateRecordTypeStats(records)
            }.onFailure { exception ->
                _errorMessage.value = (_errorMessage.value ?: "") + "\nError fetching medical records: ${exception.message}"
            }
            _isLoading.value = false // Simplified loading state
        }
    }

    // --- Statistics Calculation Functions ---
    // Make sure these are INSIDE the class
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

    private fun calculateMedicationInsights(medicalRecords: List<MedicalRecord>) {
        val allPrescribedMedsList = medicalRecords
            .flatMap { it.prescribedMedications }

        _frequentMedications.value = allPrescribedMedsList
            .groupBy { it.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        _uniqueMedicationsCount.value = allPrescribedMedsList
            .map { it.name }
            .distinct()
            .size
    }

    private fun calculateRecordTypeStats(medicalRecords: List<MedicalRecord>) {
        _recordTypeStats.value = medicalRecords
            .groupBy { it.recordType.ifBlank { "Uncategorized" } }
            .mapValues { it.value.size }
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListenerReg?.remove()
        // medicalRecordsListenerReg?.remove()
    }

}