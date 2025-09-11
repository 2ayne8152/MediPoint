package com.example.medipoint.Viewmodels

import android.app.Application
import androidx.compose.animation.core.copy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.MediPointDatabase
import com.example.medipoint.Data.MedicalInfoEntity
import com.example.medipoint.Repository.MedicalInfoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    // Add medical information fields
    val bloodType: String? = null,
    val insurance: String? = null,
    val allergies: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
    // Add other fields like birthDate, address, etc.
)
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val medicalInfoDao = MediPointDatabase.getDatabase(application).medicalInfoDao()
    private val medicalInfoRepository = MedicalInfoRepository(medicalInfoDao, firestoreDb)

    // For basic user profile from Firestore (name, email, phone)
    private val _userProfileFirestore = MutableStateFlow<UserProfile?>(null) // Holds data from Firestore user doc
    // val userProfileFirestore: StateFlow<UserProfile?> = _userProfileFirestore.asStateFlow()

    // For medical info from Room (which is synced with Firestore)
    private val _medicalInfo = MutableStateFlow<MedicalInfoEntity?>(null)
    // val medicalInfo: StateFlow<MedicalInfoEntity?> = _medicalInfo.asStateFlow()

    // Combined StateFlow for the UI
    private val _uiUserProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _uiUserProfile.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null) // For messages like "Saved", "Error"
    val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()

    private var userId: String? = null

    init {
        auth.currentUser?.uid?.let { currentUserId ->
            userId = currentUserId
            loadUserProfileData(currentUserId) // Load basic profile and medical info
        }

        // Combine flows for UI
        viewModelScope.launch {
            combine(_userProfileFirestore, _medicalInfo) { profileFromFirestore, medicalFromRoom ->
                profileFromFirestore?.copy(
                    // Update UserProfile with fields from MedicalInfoEntity
                    bloodType = medicalFromRoom?.bloodType,
                    insurance = medicalFromRoom?.insuranceProvider,
                    allergies = medicalFromRoom?.allergies,
                    emergencyContactName = medicalFromRoom?.emergencyContactName,
                    emergencyContactPhone = medicalFromRoom?.emergencyContactPhone
                )
            }.collectLatest { combinedProfile ->
                _uiUserProfile.value = combinedProfile
            }
        }
    }
    private fun loadUserProfileData(currentUserId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Fetch basic user profile (name, email, phone) from Firestore users/{userId}
            try {
                val userDocument = firestoreDb.collection("users").document(currentUserId).get().await()
                if (userDocument.exists()) {
                    _userProfileFirestore.value = UserProfile(
                        uid = currentUserId,
                        displayName = userDocument.getString("displayName") ?: auth.currentUser?.displayName,
                        email = userDocument.getString("email") ?: auth.currentUser?.email,
                        phoneNumber = userDocument.getString("phoneNumber")
                        // Medical info fields will be populated by the other flow
                    )
                } else {
                    // Basic profile from Auth if Firestore doc doesn't exist
                    _userProfileFirestore.value = UserProfile(
                        uid = currentUserId,
                        displayName = auth.currentUser?.displayName,
                        email = auth.currentUser?.email
                    )
                }
            } catch (e: Exception) {
                // Handle error fetching basic profile
                _saveStatus.value = "Error loading profile: ${e.message}"
            }

            // 2. Observe Medical Info from Room
            medicalInfoRepository.getLocalMedicalInfo(currentUserId).collectLatest { medicalInfoFromRoom ->
                _medicalInfo.value = medicalInfoFromRoom
            }
        }

        // 3. Refresh medical info from Firestore in the background and update Room
        viewModelScope.launch {
            medicalInfoRepository.refreshMedicalInfoFromFirestore(currentUserId)
            // No direct UI update here, the Room Flow will emit changes
            _isLoading.value = false // Set loading to false after refresh attempt
        }
    }

    fun updateDisplayName(newName: String) {
        val currentUid = userId ?: return
        viewModelScope.launch {
            try {
                firestoreDb.collection("users").document(currentUid)
                    .update("displayName", newName).await()
                // Update local state if needed, or rely on listener if you have one for userProfileFirestore
                _userProfileFirestore.value = _userProfileFirestore.value?.copy(displayName = newName)
                _saveStatus.value = "Display name updated."
            } catch (e: Exception) {
                _saveStatus.value = "Error updating display name: ${e.message}"
            }
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        val currentUid = userId ?: return
        viewModelScope.launch {
            try {
                firestoreDb.collection("users").document(currentUid)
                    .update("phoneNumber", newPhoneNumber).await()
                _userProfileFirestore.value = _userProfileFirestore.value?.copy(phoneNumber = newPhoneNumber)
                _saveStatus.value = "Phone number updated."
            } catch (e: Exception) {
                _saveStatus.value = "Error updating phone number: ${e.message}"
            }
        }
    }

    fun updateMedicalInfo(updatedMedicalInfo: MedicalInfoEntity) {
        val currentUid = userId ?: return
        // Ensure the medicalInfo object has the correct userId
        val infoToSave = updatedMedicalInfo.copy(userId = currentUid)

        viewModelScope.launch {
            _isLoading.value = true
            val result = medicalInfoRepository.saveMedicalInfo(infoToSave)
            if (result.isSuccess) {
                _saveStatus.value = "Medical information updated."
                // The Room Flow will automatically update _medicalInfo.value
            } else {
                _saveStatus.value = "Error updating medical info: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    // Call this on sign out
    fun onSignOutClearData() {
        viewModelScope.launch {
            userId?.let { medicalInfoRepository.clearLocalMedicalInfo(it) }
            _userProfileFirestore.value = null
            _medicalInfo.value = null
            _uiUserProfile.value = null
            userId = null
        }
    }
}