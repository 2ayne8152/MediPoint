package com.example.medipoint.ui.theme.Viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Keep existing isLoggedIn and currentUser logic
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null) // Keep private
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow() // Expose as StateFlow

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // --- State for Password Reset ---
    private val _passwordResetStatus = MutableStateFlow<String?>(null)
    val passwordResetStatus: StateFlow<String?> = _passwordResetStatus.asStateFlow()

    private val _isLoadingPasswordReset = MutableStateFlow(false)
    val isLoadingPasswordReset: StateFlow<Boolean> = _isLoadingPasswordReset.asStateFlow()
    // ---

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _isLoggedIn.value = firebaseAuth.currentUser != null
        }
    }

    fun signOut() {
        auth.signOut()
        // _isLoggedIn.value will be updated by the AuthStateListener
    }
    fun createAccount(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        // Your existing createAccount logic here
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address")
            return
        }

        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Account created successfully!")
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        is FirebaseAuthUserCollisionException -> "Account already exists"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    onResult(false, errorMessage)
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful!")
                } else {
                    onResult(false, "Login failed: ${task.exception?.message}")
                }
            }
    }
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _passwordResetStatus.value = "Please enter a valid email address to reset password."
            return
        }

        _isLoadingPasswordReset.value = true
        _passwordResetStatus.value = null // Clear previous status

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoadingPasswordReset.value = false
                if (task.isSuccessful) {
                    _passwordResetStatus.value = "Password reset email sent to $email. Please check your inbox (and spam folder)."
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            _passwordResetStatus.value = "No account found with this email address."
                        }
                        is FirebaseNetworkException -> {
                            _passwordResetStatus.value = "Network error. Please check your connection."
                        }
                        // Add more specific error handling if needed
                        else -> {
                            _passwordResetStatus.value = "Failed to send password reset email. Please try again. (${exception?.message ?: "Unknown error"})"
                        }
                    }
                }
            }
    }

    fun clearPasswordResetStatus() {
        _passwordResetStatus.value = null
    }
    // --- End of New Function ---
}