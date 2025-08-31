package com.example.medipoint.ui.theme.Viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    val isLoggedIn = mutableStateOf(auth.currentUser != null)

    fun signOut() {
        auth.signOut()
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    init {
        // Listen for authentication state changes
        auth.addAuthStateListener { firebaseAuth ->
            isLoggedIn.value = firebaseAuth.currentUser != null
        }
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
}