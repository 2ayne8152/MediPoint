package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegistrationSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Success message
        if (successMessage.isNotBlank()) {
            Text(successMessage, color = Color.Green)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Input Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CREATE ACCOUNT Button
        Button(
            onClick = {
                // Validate inputs
                if (password != confirmPassword) {
                    errorMessage = "Passwords don't match"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                isLoading = true
                errorMessage = ""
                authViewModel.createAccount(email, password) { success, message ->
                    isLoading = false
                    if (success) {
                        successMessage = "Account created successfully! Please login."
                        // Clear form
                        email = ""
                        password = ""
                        confirmPassword = ""
                    } else {
                        errorMessage = message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            Text(if (isLoading) "Creating Account..." else "Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BACK TO LOGIN Button
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Back to Login")
        }
    }
}