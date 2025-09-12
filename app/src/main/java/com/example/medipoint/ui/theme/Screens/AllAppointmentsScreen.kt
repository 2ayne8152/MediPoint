package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Viewmodels.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AllAppointmentsScreen(
    bookingViewModel: BookingViewModel = viewModel(),
    onDetailClick: (String) -> Unit = {}
) {
    val appointments by bookingViewModel.appointments.collectAsState()

    LaunchedEffect(Unit) {
        bookingViewModel.startAppointmentsListener()
    }

    val (upcoming, previous) = filterAppointments(appointments)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Upcoming Section
        Text(
            "Upcoming Appointments",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (upcoming.isEmpty()) {
            Text(
                "No upcoming appointments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            upcoming.forEach { appt ->
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

        Spacer(modifier = Modifier.height(24.dp))

        // Previous Section
        Text(
            "Previous Appointments",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (previous.isEmpty()) {
            Text(
                "No previous appointments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            previous.forEach { appt ->
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

private fun filterAppointments(appointments: List<Appointment>): Pair<List<Appointment>, List<Appointment>> {
    val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
    val now = System.currentTimeMillis()

    val upcoming = mutableListOf<Appointment>()
    val previous = mutableListOf<Appointment>()

    for (appt in appointments) {
        val millis = try {
            sdf.parse("${appt.date} ${appt.time}")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        if (appt.status == "Scheduled" && millis >= now) {
            upcoming.add(appt)
        } else {
            previous.add(appt)
        }
    }

    upcoming.sortBy { sdf.parse("${it.date} ${it.time}")?.time ?: 0L }
    previous.sortByDescending { sdf.parse("${it.date} ${it.time}")?.time ?: 0L }

    return Pair(upcoming, previous)
}
