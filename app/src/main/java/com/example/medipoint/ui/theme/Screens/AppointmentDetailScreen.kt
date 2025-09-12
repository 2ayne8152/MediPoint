@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.example.medipoint.ui.theme.Screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.R
import com.example.medipoint.Viewmodels.CheckInViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    viewModel: CheckInViewModel = viewModel()
) {
    val checkInRecord by viewModel.checkInRecord.collectAsState()
    val appointment by viewModel.appointment.collectAsState()
    val appointmentDateTime by viewModel.appointmentDateTime.collectAsState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(appointmentId) {
        viewModel.loadUserCheckInRecord(appointmentId)
        viewModel.loadAppointmentDetails(appointmentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    appointment?.doctorName ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    appointment?.appointmentType ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppointmentInfoRow(Icons.Default.DateRange, "Date:", appointment?.date ?: "")
                AppointmentInfoRow(Icons.Filled.Settings, "Time:", appointment?.time ?: "")
                AppointmentInfoRow(Icons.Filled.Phone, "Contact Us: ", "012-345678")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(
                            if (appointment?.status == "Confirmed") Color(0xFF00C853) else Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        appointment?.status ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ✅ Check-in Card (Dynamic window)
        CheckInCard(
            checkInRecord = checkInRecord ?: CheckInRecord(),
            appointmentId = appointmentId,
            viewModel = viewModel,
            appointmentDateTime = appointmentDateTime
        )

        // Location & Directions Card (unchanged)
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.location_directions),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text("Main Building", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "2nd Floor\nRoom 201",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.get_directions),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                DirectionButton("Open Google Map")
            }
        }

        // Preparation Tips (unchanged)
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Text(
                    "Preparation Tips",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    "• Arrive 15 minutes early for check-in\n\n" +
                            "• Bring your insurance card and ID\n\n" +
                            "• List of current medications\n\n" +
                            "• Any questions or concerns to discuss",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
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
        Icon(icon, contentDescription = null, tint = Color(0xFF0A0A1A))
        Spacer(modifier = Modifier.width(8.dp))
        Text( text = "$text1 $text2", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.check_in),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (checkInRecord.checkedIn) {
                Text("Checked in successfully!")

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Tap to Check In")
                }

                if (!checkInAvailable) {
                    Text(
                        text = "Check-in opens 30 minutes before your appointment",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun DirectionButton(label: String) {
    Button(
        onClick = { /* Handle directions */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ) {
        Text(label)
    }
}
