package com.example.medipoint.ui.theme.Screens // Or your correct package

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Viewmodels.MedicalRecordsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordScreen(
    viewModel: MedicalRecordsViewModel = viewModel()
) {
    val appointmentTypeStats by viewModel.appointmentTypeStats.collectAsState()
    val statusStats by viewModel.statusStats.collectAsState()
    val frequentMedications by viewModel.frequentMedications.collectAsState()
    val uniqueMedicationsCount by viewModel.uniqueMedicationsCount.collectAsState() // New line
    val typesPerDoctorStats by viewModel.typesPerDoctorStats.collectAsState()
    val statusPerTypeStats by viewModel.statusPerTypeStats.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Insights & Statistics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp), // Additional horizontal padding
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp)) // Space from TopAppBar
            }

            when {
                isLoading -> item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> item {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    // Section 1: General Appointment Statistics
                    item {
                        StatisticSectionCard(title = "Appointment Types") {
                            if (appointmentTypeStats.isEmpty()) {
                                Text("No appointment type data.")
                            } else {
                                appointmentTypeStats.forEach { (type, count) ->
                                    StatisticRow(label = type, value = count.toString())
                                }
                            }
                        }
                    }

                    item {
                        StatisticSectionCard(title = "Appointment Statuses") {
                            if (statusStats.isEmpty()) {
                                Text("No status data.")
                            } else {
                                statusStats.forEach { (status, count) ->
                                    StatisticRow(label = status, value = count.toString())
                                }
                            }
                        }
                    }

                    // Section 2: Prescribed Medication Insights
                    item {
                        StatisticSectionCard(title = "Medication Insights") {
                            Text("Top Prescribed (Simulated):", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                            if (frequentMedications.isEmpty()) {
                                Text("No medication data.")
                            } else {
                                frequentMedications.forEach { (name, count) ->
                                    StatisticRow(label = name, value = "$count times")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Unique Medications Prescribed: $uniqueMedicationsCount", // Directly use the count
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }


                    // Section 3: Combined Statistics
                    item {
                        StatisticSectionCard(title = "Appointments per Doctor") {
                            if (typesPerDoctorStats.isEmpty()){
                                Text("No data.")
                            } else {
                                typesPerDoctorStats.forEach { (doctor, typeMap) ->
                                    Text(doctor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        typeMap.forEach { (type, count) ->
                                            StatisticRow(label = type, value = count.toString())
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    item {
                        StatisticSectionCard(title = "Status per Appointment Type") {
                            if (statusPerTypeStats.isEmpty()){
                                Text("No data.")
                            } else {
                                statusPerTypeStats.forEach { (type, statusMap) ->
                                    Text(type, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                    Column(modifier = Modifier.padding(start = 16.dp)) {
                                        statusMap.forEach { (status, count) ->
                                            StatisticRow(label = status, value = count.toString())
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) } // Bottom padding
                }
            }
        }
    }
}

@Composable
fun StatisticSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.ifBlank { "N/A" },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

