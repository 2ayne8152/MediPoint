package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.MedicalInfoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _saveStatus = MutableStateFlow<String?>(null) // For messages like "Saved" or "Error"
    val saveStatus: StateFlow<String?> = _saveStatus

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        auth.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val userDocRef = db.collection("users").document(firebaseUser.uid)
                    val snapshot = userDocRef.get().await()
                    if (snapshot.exists()) {
                        val fetchedProfile = snapshot.toObject(UserProfile::class.java)?.copy(
                            displayName = firebaseUser.displayName,
                            email = firebaseUser.email
                        )
                        _userProfile.value = fetchedProfile
                    } else {
                        // User document doesn't exist yet in Firestore, create it.
                        val newProfile = UserProfile(
                            uid = firebaseUser.uid,
                            displayName = firebaseUser.displayName,
                            email = firebaseUser.email,
                            bloodType = null,
                            insurance = null,
                            allergies = null,
                            emergencyContactName = null,
                            emergencyContactPhone = null
                        )
                        userDocRef.set(newProfile).await()
                        _userProfile.value = newProfile
                    }
                } catch (e: Exception) {
                    _saveStatus.value = "Error loading profile: ${e.message}"
                }
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val currentUser = auth.currentUser ?: return
        val currentProfile = _userProfile.value // Don't return if currentProfile is null yet, we might be creating it

        if (currentUser == null) {
            _saveStatus.value = "User not available for update."
            return
        }

        viewModelScope.launch {
            try {
                // Update in Firebase Auth
                val authProfileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                currentUser.updateProfile(authProfileUpdates).await()

                // Update/Create in Firestore
                val userDocRef = db.collection("users").document(currentUser.uid)
                // Create a map of the data you want to set/merge
                val userData = mapOf(
                    "uid" to currentUser.uid, // Good to have uid in the document
                    "displayName" to newName,
                    // If currentProfile is null, it means we are likely creating the document
                    // You might want to include email from auth here as well
                    "email" to (currentProfile?.email ?: currentUser.email)
                    // Add other fields you want to ensure are present
                )
                userDocRef.set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                // Update local state
                _userProfile.value = (_userProfile.value ?: UserProfile(uid = currentUser.uid, email = currentUser.email)).copy(displayName = newName)
                _saveStatus.value = "Display name updated successfully!"
            } catch (e: Exception) {
                _saveStatus.value = "Failed to update display name: ${e.message}"
            }
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        val currentUser = auth.currentUser
        val currentProfile = _userProfile.value

        if (currentUser == null) {
            _saveStatus.value = "User not available for update."
            return
        }

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                val phoneData = mapOf(
                    "uid" to currentUser.uid, // Good practice
                    "phoneNumber" to newPhoneNumber,
                    "email" to (currentProfile?.email ?: currentUser.email), // Include other key fields
                    "displayName" to (currentProfile?.displayName ?: currentUser.displayName)
                )
                userDocRef.set(phoneData, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _userProfile.value = (_userProfile.value ?: UserProfile(uid = currentUser.uid, email = currentUser.email, displayName = currentUser.displayName)).copy(phoneNumber = newPhoneNumber)
                _saveStatus.value = "Phone number updated successfully!"
            } catch (e: Exception) {
                _saveStatus.value = "Failed to update phone number: ${e.message}"
            }
        }
    }
    fun updateMedicalInfo(newMedicalInfo: MedicalInfoEntity) {
        val currentUser = auth.currentUser
        val currentProfile = _userProfile.value

        if (currentUser == null) {
            _saveStatus.value = "User not available for update."
            return
        }

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                val medicalData = mapOf(
                    "uid" to currentUser.uid,
                    "bloodType" to newMedicalInfo.bloodType,
                    "insuranceProvider" to newMedicalInfo.insuranceProvider,
                    "allergies" to newMedicalInfo.allergies,
                    "emergencyContactName" to newMedicalInfo.emergencyContactName,
                    "emergencyContactPhone" to newMedicalInfo.emergencyContactPhone,
                    // Preserve existing data
                    "email" to (currentProfile?.email ?: currentUser.email),
                    "displayName" to (currentProfile?.displayName ?: currentUser.displayName),
                    "phoneNumber" to (currentProfile?.phoneNumber ?: "")
                )

                userDocRef.set(medicalData, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                // Update local state
                _userProfile.value = (_userProfile.value ?: UserProfile(
                    uid = currentUser.uid,
                    email = currentUser.email,
                    displayName = currentUser.displayName
                )).copy(
                    bloodType = newMedicalInfo.bloodType,
                    insurance = newMedicalInfo.insuranceProvider,
                    allergies = newMedicalInfo.allergies,
                    emergencyContactName = newMedicalInfo.emergencyContactName,
                    emergencyContactPhone = newMedicalInfo.emergencyContactPhone
                )

                _saveStatus.value = "Medical information updated successfully!"
            } catch (e: Exception) {
                _saveStatus.value = "Failed to update medical information: ${e.message}"
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}