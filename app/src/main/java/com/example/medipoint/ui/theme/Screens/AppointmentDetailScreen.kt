@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medipoint.R

@Preview(showBackground = true)
@Composable
fun AppointmentDetailScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Appointment Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Dr. Johnson", style = MaterialTheme.typography.titleLarge)
                Text(
                    "General Checkup",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppointmentInfoRow(Icons.Default.DateRange, "Monday, July 28, 2025")
                AppointmentInfoRow(Icons.Filled.Settings, "09:00 AM")
                AppointmentInfoRow(Icons.Default.Call, "+1 (555) 123-4567")

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(Color(0xFF00C853), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Confirmed",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Location & Directions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.location_directions),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Main Building", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "2nd Floor\nRoom 201",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.get_directions),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                DirectionButton("Open Google Map")
            }
        }
    }
}


@Composable
fun AppointmentInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0A0A1A))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
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


