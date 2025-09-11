package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.R
import com.example.medipoint.Viewmodels.BookingViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    onBookAppointmentClick: () -> Unit,
    onDetailClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    // Collect appointments from the ViewModel
    val appointments by bookingViewModel.appointments.collectAsState()

    // Start listening once when the screen enters
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "TEST_USER"
        bookingViewModel.startAppointmentsListener()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Greeting
        Text(
            text = "Good morning, John",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "You have ${appointments.size} upcoming appointment${if (appointments.size == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Quick Actions
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                ),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionButton(
                    imageVector = Icons.Filled.Add,
                    title = stringResource(R.string.book_appointment),
                    subtitle = stringResource(R.string.schedule_a_new_visit),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    onButtonClicked = onBookAppointmentClick
                )
                ActionButton(
                    imageVector = Icons.Filled.Place,
                    title = stringResource(R.string.find_my_doctor),
                    subtitle = stringResource(R.string.get_directions),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    onButtonClicked = {}
                )
            }
        }

        // Upcoming Appointments
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                "Upcoming Appointments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding()
            )
            Spacer(
                modifier = Modifier.padding(6.dp)
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    onViewAllClick()
                }
            )
        }

        // Render appointments from Firestore
        if (appointments.isEmpty()) {
            Text(
                "No upcoming appointments.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            appointments.forEach { appt ->
                if (appt.status == "Scheduled") {
                    AppointmentCard(
                        doctor = appt.doctorName,
                        specialty = appt.appointmentType,
                              date = appt.date,
                        time = appt.time,
                        modifier = Modifier.padding(top = 8.dp),
                        onDetailClick = { onDetailClick(appt.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    imageVector: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onButtonClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onButtonClicked
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier.width(16.dp)
            )
            Icon(
                imageVector = imageVector,
                tint = Color(0xFF0A0A1A),
                contentDescription = null
            )
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AppointmentCard(
    doctor: String,
    specialty: String,
    date: String,
    time: String,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor, style = MaterialTheme.typography.bodyLarge)
                Text(specialty, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Text(
                    "$date • $time",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Details")
            }
        }
    }
}
