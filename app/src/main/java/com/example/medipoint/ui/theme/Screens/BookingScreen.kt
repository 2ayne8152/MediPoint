package com.example.medipoint.ui.theme.Screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Viewmodels.BookingViewModel
import com.example.medipoint.Viewmodels.BookingViewModelFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel = viewModel(
        factory = BookingViewModelFactory(
            appointmentRepository = AppointmentRepository(FirestoreAppointmentDao()),
            alertsRepository = AlertsRepository()
        )
    )
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // State holders
    var selectedDoctor by remember { mutableStateOf("") }
    var expandedDoctor by remember { mutableStateOf(false) }

    var appointmentType by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }

    var preferredTime by remember { mutableStateOf("") }
    var expandedTime by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val appointments by viewModel.appointments.collectAsState()

    val allTimeSlots = listOf("09:00 AM", "10:30 AM", "02:00 PM", "04:00 PM")

    val availableTimeSlots = remember(selectedDate, appointments) {
        allTimeSlots.filter { time ->
            appointments.none { it.date == selectedDate && it.time == time && (it.status == "Scheduled" || it.status == "Confirmed") }
        }
    }

    val isFormValid = selectedDoctor.isNotBlank() &&
            appointmentType.isNotBlank() &&
            selectedDate.isNotBlank() &&
            preferredTime.isNotBlank()

    val saveStatus by viewModel.saveStatus.collectAsState()

    LaunchedEffect(saveStatus) {
        saveStatus?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Appointment requested successfully!", Toast.LENGTH_SHORT).show()

                selectedDoctor = ""
                appointmentType = ""
                selectedDate = ""
                preferredTime = ""
                notes = ""
            }
            result.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Appointment Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Doctor dropdown
        ExposedDropdownMenuBox(
            expanded = expandedDoctor,
            onExpandedChange = { expandedDoctor = !expandedDoctor }
        ) {
            TextField(
                value = selectedDoctor,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Doctor *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDoctor) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedDoctor, onDismissRequest = { expandedDoctor = false }) {
                listOf("Dr. Johnson", "Dr. Lee", "Dr. Smith").forEach { doc ->
                    DropdownMenuItem(
                        text = { Text(doc) },
                        onClick = {
                            selectedDoctor = doc
                            expandedDoctor = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Appointment type dropdown
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType }
        ) {
            TextField(
                value = appointmentType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Appointment Type *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                listOf("General Checkup", "Consultation", "Follow-up").forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            appointmentType = type
                            expandedType = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Date picker field
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text("Preferred Date *") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showAndroidDatePicker(context, calendar) { date -> selectedDate = date }
                },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Pick a date",
                    modifier = Modifier.clickable {
                        showAndroidDatePicker(context, calendar) { date -> selectedDate = date }
                    }
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // Time dropdown
        ExposedDropdownMenuBox(
            expanded = expandedTime,
            onExpandedChange = { expandedTime = !expandedTime }
        ) {
            TextField(
                value = preferredTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Preferred Time *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTime) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                if (availableTimeSlots.isEmpty()) {
                    DropdownMenuItem(text = { Text("No available slots") }, onClick = {})
                } else {
                    availableTimeSlots.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time) },
                            onClick = {
                                preferredTime = time
                                expandedTime = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Notes field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                viewModel.saveAppointment(
                    doctorName = selectedDoctor,
                    appointmentType = appointmentType,
                    date = selectedDate,
                    time = preferredTime,
                    notes = notes
                )
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Request Appointment",
                color = if (isFormValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun showAndroidDatePicker(
    context: Context,
    calendar: Calendar,
    onDateSelected: (String) -> Unit
) {
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected("$dayOfMonth/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Prevent selecting past dates
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000 //2 days

    datePickerDialog.show()
}
