package com.example.medipoint.Viewmodels // Or your preferred ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // For reacting to medicalRecords changes
import kotlinx.coroutines.launch

class MedicalRecordsViewModel(
    private val repository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao())
) : ViewModel() {

    // Raw list of all medical records for the user
    private val _medicalRecords = MutableStateFlow<List<Appointment>>(emptyList())
    // val medicalRecords: StateFlow<List<Appointment>> = _medicalRecords.asStateFlow() // Keep if needed elsewhere

    // Statistics: Map of AppointmentType (String) to Count (Int)
    private val _appointmentStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appointmentStats: StateFlow<Map<String, Int>> = _appointmentStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var appointmentsListenerRegistration: ListenerRegistration? = null

    // Define the known appointment types if you want to ensure all are shown, even with 0 count
    // Or, you can derive them purely from the fetched data.
    // For simplicity now, we'll derive from data, but you could pre-populate keys.
    // private val knownAppointmentTypes = listOf("General Checkup", "Consultation", "Follow-up")

    init {
        loadMedicalRecordsAndCalculateStats()
    }

    private fun loadMedicalRecordsAndCalculateStats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not logged in."
            _medicalRecords.value = emptyList()
            _appointmentStats.value = emptyMap() // Clear stats too
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _appointmentStats.value = emptyMap() // Clear stats while loading

        appointmentsListenerRegistration?.remove() // Stop previous listener

        appointmentsListenerRegistration = repository.listenAppointments(
            userId = userId,
            onDataChange = { appointments ->
                _medicalRecords.value = appointments // Update raw list
                calculateAppointmentStats(appointments) // Calculate stats from the new list
                _isLoading.value = false
            },
            onError = { exception ->
                _errorMessage.value = "Error fetching medical records: ${exception.message}"
                _medicalRecords.value = emptyList()
                _appointmentStats.value = emptyMap() // Clear stats on error
                _isLoading.value = false
            }
        )
    }

    private fun calculateAppointmentStats(appointments: List<Appointment>) {
        // Group by appointmentType and count occurrences
        val stats = appointments
            .groupBy { it.appointmentType } // Groups into Map<String, List<Appointment>>
            .mapValues { entry -> entry.value.size } // Transforms to Map<String, Int>

        // If you want to ensure all known types are present, even if count is 0:
        // val completeStats = knownAppointmentTypes.associateWith { type ->
        //    stats[type] ?: 0
        // }
        // _appointmentStats.value = completeStats

        _appointmentStats.value = stats
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListenerRegistration?.remove()
    }
}
