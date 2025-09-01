package com.example.medipoint.ui.theme.Screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.example.medipoint.Viewmodels.BookingViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(viewModel: BookingViewModel = viewModel()) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Appointment Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Doctor dropdown
        ExposedDropdownMenuBox(expanded = expandedDoctor, onExpandedChange = { expandedDoctor = !expandedDoctor }) {
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
                    DropdownMenuItem(text = { Text(doc) }, onClick = { selectedDoctor = doc; expandedDoctor = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Appointment type dropdown
        ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
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
                    DropdownMenuItem(text = { Text(type) }, onClick = { appointmentType = type; expandedType = false })
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
                .clickable { showAndroidDatePicker(context, calendar) { date -> selectedDate = date } }
        )

        Spacer(Modifier.height(16.dp))

        // Time dropdown
        ExposedDropdownMenuBox(expanded = expandedTime, onExpandedChange = { expandedTime = !expandedTime }) {
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
                listOf("09:00 AM", "10:30 AM", "02:00 PM", "04:00 PM").forEach { time ->
                    DropdownMenuItem(text = { Text(time) }, onClick = { preferredTime = time; expandedTime = false })
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

        Button(
            onClick = {
                viewModel.saveAppointment(
                    doctorName = selectedDoctor,
                    appointmentType = appointmentType,
                    date = selectedDate,
                    time = preferredTime,
                    notes = notes,
                    onSuccess = {
                        // TODO: maybe navigate to Home or AppointmentDetailScreen
                    },
                    onFailure = { e ->
                        // TODO: show error with Snackbar/Toast
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00001A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Request Appointment", color = Color.White)
        }
    }
}

private fun showAndroidDatePicker(context: Context, calendar: Calendar, onDateSelected: (String) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onDateSelected("$day/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
