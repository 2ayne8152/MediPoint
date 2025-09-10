package com.example.medipoint.Viewmodels // Or your preferred ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.FirestoreAppointmentDao // Assuming this is your implementation
import com.example.medipoint.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration // For managing the listener

class MedicalRecordsViewModel(
    // You might want to use dependency injection (e.g., Hilt) here
    private val repository: AppointmentRepository = AppointmentRepository(FirestoreAppointmentDao())
) : ViewModel() {

    private val _medicalRecords = MutableStateFlow<List<Appointment>>(emptyList())
    val medicalRecords: StateFlow<List<Appointment>> = _medicalRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var appointmentsListenerRegistration: ListenerRegistration? = null

    init {
        loadMedicalRecords()
    }

    fun loadMedicalRecords() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not logged in."
            _medicalRecords.value = emptyList()
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        // Stop any previous listener to avoid multiple listeners
        appointmentsListenerRegistration?.remove()

        // Using listenAppointments for real-time updates,
        // or you can use repository.getAppointments(userId) for a one-time fetch.
        // For medical records, a listener might be good if records can be updated elsewhere.
        appointmentsListenerRegistration = repository.listenAppointments(
            userId = userId,
            onDataChange = { appointments ->
                // Sort appointments by date and time, most recent first for a timeline
                _medicalRecords.value = appointments.sortedWith(
                    compareByDescending<Appointment> { it.date }
                        .thenByDescending { it.time }
                )
                _isLoading.value = false
            },
            onError = { exception ->
                _errorMessage.value = "Error fetching medical records: ${exception.message}"
                _medicalRecords.value = emptyList()
                _isLoading.value = false
            }
        )
    }

    // Call this when the ViewModel is cleared to remove the listener
    override fun onCleared() {
        super.onCleared()
        appointmentsListenerRegistration?.remove()
    }
}
