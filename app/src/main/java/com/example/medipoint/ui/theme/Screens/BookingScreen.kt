package com.example.medipoint.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen() {
    // === Dropdown States ===
    var selectedDoctor by remember { mutableStateOf("") }
    var expandedDoctor by remember { mutableStateOf(false) }
    val doctorList = listOf("Dr. Johnson", "Dr. Lee", "Dr. Smith")

    var appointmentType by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    val appointmentTypes = listOf("General Checkup", "Consultation", "Follow-up")

    var preferredTime by remember { mutableStateOf("") }
    var expandedTime by remember { mutableStateOf(false) }
    val timeSlots = listOf("09:00 AM", "10:30 AM", "02:00 PM", "04:00 PM")

    // === Date Picker ===
    var selectedDate by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // === Additional Notes ===
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Appointment Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Please select your preferred time and doctor",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // === Select Doctor Dropdown ===
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
            ExposedDropdownMenu(
                expanded = expandedDoctor,
                onDismissRequest = { expandedDoctor = false }
            ) {
                doctorList.forEach { doctor ->
                    DropdownMenuItem(
                        text = { Text(doctor) },
                        onClick = {
                            selectedDoctor = doctor
                            expandedDoctor = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Appointment Type Dropdown ===
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
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                appointmentTypes.forEach { type ->
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

        Spacer(modifier = Modifier.height(16.dp))

        // === Preferred Date Picker ===
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Preferred Date *") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // === Preferred Time Dropdown ===
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
            ExposedDropdownMenu(
                expanded = expandedTime,
                onDismissRequest = { expandedTime = false }
            ) {
                timeSlots.forEach { time ->
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

        Spacer(modifier = Modifier.height(16.dp))

        // === Additional Notes TextField ===
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Describe your symptoms or concerns...") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // === Request Appointment Button ===
        Button(
            onClick = {
                // TODO: Handle form submission
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00001A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Request Appointment", color = Color.White)
        }
    }
}
