@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.medipoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.ui.theme.MediPointTheme
import com.example.medipoint.ui.theme.Screens.LoginScreen
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MediPointTheme {
                val authViewModel: AuthViewModel = viewModel()
                // Observe the authentication state
                if (authViewModel.isLoggedIn.value) {
                    MediPointApp(
                        onSignOut = {
                            authViewModel.signOut()
                        }
                    )
                } else {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                        }
                    )
                }
            }
        }
    }
}

