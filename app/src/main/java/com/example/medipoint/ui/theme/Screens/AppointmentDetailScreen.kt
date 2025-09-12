@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.example.medipoint.ui.theme.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.R
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Viewmodels.AlertViewModel
import com.example.medipoint.Viewmodels.AlertViewModelFactory
import com.example.medipoint.Viewmodels.CheckInViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    checkInViewModel: CheckInViewModel = viewModel(),
    alertViewModelFactory: AlertViewModelFactory = AlertViewModelFactory(AlertsRepository()),
    navController: NavController
) {
    val alertViewModel: AlertViewModel = viewModel(factory = alertViewModelFactory)
    val checkInRecord by checkInViewModel.checkInRecord.collectAsState()
    val appointment by checkInViewModel.appointment.collectAsState()
    val appointmentDateTime by checkInViewModel.appointmentDateTime.collectAsState()
    val context = LocalContext.current

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(appointmentId) {
        checkInViewModel.loadUserCheckInRecord(appointmentId)
        checkInViewModel.loadAppointmentDetails(appointmentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Appointment Details Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    appointment?.doctorName ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    appointment?.appointmentType ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppointmentInfoRow(Icons.Default.DateRange, "Date:", appointment?.date ?: "")
                AppointmentInfoRow(Icons.Default.Settings, "Time:", appointment?.time ?: "")
                AppointmentInfoRowPhone(Icons.Default.Phone, "Contact Us:", "012-3456789")
                Spacer(modifier = Modifier.height(8.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (appointment?.status == "Confirmed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            appointment?.status ?: "",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Check-In Card
        CheckInCard(
            checkInRecord = checkInRecord ?: CheckInRecord(),
            appointmentId = appointmentId,
            viewModel = checkInViewModel,
            appointmentDateTime = appointmentDateTime
        )

        // Location & Directions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Location & Directions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Main Building", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "2nd Floor\nRoom 201",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                DirectionButton("Open Google Map", context)
            }
        }

        // Preparation Tips Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Preparation Tips",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Arrive 15 minutes early for check-in\n" +
                            "• Bring your insurance card and ID\n" +
                            "• List of current medications\n" +
                            "• Any questions or concerns to discuss",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cancel Appointment Button
        if (checkInRecord?.checkedIn != true && appointment?.status == "Scheduled") {
            Button(
                onClick = {
                    checkInViewModel.cancelAppointment(appointmentId)
                    alertViewModel.cancelAlert(appointmentId)
                    Toast.makeText(context, "Appointment cancelled!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack(route = "home", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel Appointment", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun AppointmentInfoRow(icon: ImageVector, text1: String, text2: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text( text = "$text1 $text2", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun AppointmentInfoRowPhone(
    icon: ImageVector,
    label: String,
    phoneNumber: String
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                context.startActivity(intent)
            }
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "$label $phoneNumber")
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckInCard(
    checkInRecord: CheckInRecord,
    appointmentId: String,
    viewModel: CheckInViewModel = viewModel(),
    appointmentDateTime: Long?,
    locationPermission: PermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val checkInAvailable = appointmentDateTime?.let {
        now in (it - 30 * 60 * 1000)..(it + 10 * 60 * 1000)
    } ?: false

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.check_in),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (checkInRecord.checkedIn) {
                Text(
                    "Checked in successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )

                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Checked in successfully!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Button(
                    onClick = {
                        if (locationPermission.status.isGranted) {
                            viewModel.attemptCheckIn(appointmentId)
                        } else {
                            locationPermission.launchPermissionRequest()
                        }
                    },
                    enabled = checkInAvailable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Tap to Check In")
                }

                if (!checkInAvailable) {
                    Text(
                        text = "Check-in opens 30 minutes before your appointment",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionButton(label: String, context: Context) {
    Button(
        onClick = {
            val address = "Tunku+Abdul+Rahman+University,+Penang"
            val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(label, color = MaterialTheme.colorScheme.onPrimary)
    }
}
