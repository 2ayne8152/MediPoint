package com.example.medipoint.ui.theme.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.MainActivity
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegistration: () -> Unit 
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoadingLogin by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current // For showing Toasts

    // Observe password reset states from ViewModel
    val passwordResetStatus by authViewModel.passwordResetStatus.collectAsState()
    val isLoadingPasswordReset by authViewModel.isLoadingPasswordReset.collectAsState()

    // Effect to show Toast for password reset status and clear it
    // Effect to show Toast for password reset status and clear it
    LaunchedEffect(passwordResetStatus) {
        passwordResetStatus?.let { status ->
            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            authViewModel.clearPasswordResetStatus() // Clear status after showing
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MediPoint Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Login Error message
        if (loginErrorMessage.isNotBlank()) {
            Text(loginErrorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Forgot Password Button
        TextButton(
            onClick = {
                // Ensure email is not empty before attempting reset
                if (email.isNotBlank()) {
                    authViewModel.sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(context, "Please enter your email address first.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoadingLogin && !isLoadingPasswordReset // Disable if any loading is happening
        ) {
            Text(if (isLoadingPasswordReset) "Sending..." else "Forgot Password?")
        }
        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                isLoadingLogin = true
                loginErrorMessage = ""
                authViewModel.signIn(email, password) { success, message ->
                    isLoadingLogin = false
                    if (success) {
                        onLoginSuccess()
                    } else {
                        loginErrorMessage = message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingLogin && email.isNotBlank() && password.isNotBlank() && !isLoadingPasswordReset
        ) {
            Text(if (isLoadingLogin) "Signing in..." else "Sign In")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToRegistration,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingLogin && !isLoadingPasswordReset
        ) {
            Text("Create New Account")
        }
    }
}