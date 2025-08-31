package com.example.medipoint.ui.theme.Screens

import com.example.medipoint.Viewmodels.ProfileViewModel
import com.example.medipoint.Viewmodels.UserProfile
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
@Composable
fun ProfileScreen(onSignOut: () -> Unit,
                  profileViewModel: ProfileViewModel = viewModel()) {
    val viewModel: ProfileViewModel = viewModel()
    val userProfile: UserProfile? by profileViewModel.userProfile.collectAsState()
    val saveStatus: String? by profileViewModel.saveStatus.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Show save status messages
    LaunchedEffect(saveStatus) {
        saveStatus?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        userProfile?.let { profile ->
            ProfileCard(
                profile = profile,
                onUsernameChange = { newName ->
                    coroutineScope.launch {
                        viewModel.updateDisplayName(newName)
                    }
                },
                onPhoneNumberChange = { newPhone ->
                    coroutineScope.launch {
                        viewModel.updatePhoneNumber(newPhone)
                    }
                }
            )
        } ?: Text("Loading user profile...")

        Spacer(modifier = Modifier.height(16.dp))
        MedicalInfoCard()
        Spacer(modifier = Modifier.height(16.dp))
        OptionsSection(onSignOut = onSignOut)
    }
}

@Composable
fun ProfileCard(
    profile: UserProfile,
    onUsernameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit
) {
    var isEditingUsername by remember { mutableStateOf(false) }
    var editableUsername by remember { mutableStateOf(profile.displayName ?: "") }

    var isEditingPhoneNumber by remember { mutableStateOf(false) }
    var editablePhoneNumber by remember { mutableStateOf(profile.phoneNumber ?: "") }

    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Username Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditingUsername) {
                    OutlinedTextField(
                        value = editableUsername,
                        onValueChange = { editableUsername = it },
                        label = { Text("Username") },
                        placeholder = { Text("Enter your username") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onUsernameChange(editableUsername)
                            isEditingUsername = false
                            focusManager.clearFocus()
                        })
                    )
                    Row {
                        IconButton(onClick = {
                            onUsernameChange(editableUsername)
                            isEditingUsername = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Save Username")
                        }
                        IconButton(onClick = {
                            isEditingUsername = false
                            editableUsername = profile.displayName ?: ""
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel Username Edit")
                        }
                    }
                } else {
                    Text(
                        text = profile.displayName ?: "Edit Username",
                        fontSize = 20.sp,
                        fontWeight = if (profile.displayName != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (profile.displayName != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = profile.displayName == null) {
                                if (profile.displayName == null) {
                                    isEditingUsername = true
                                }
                            }
                    )
                    IconButton(onClick = {
                        editableUsername = profile.displayName ?: ""
                        isEditingUsername = true
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Username")
                    }
                }
            }

            // Email
            Text(
                text = profile.email ?: "No email provided",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Phone Number Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Contact Number Icon",
                    modifier = Modifier.padding(end = 6.dp)
                )
                if (isEditingPhoneNumber) {
                    OutlinedTextField(
                        value = editablePhoneNumber,
                        onValueChange = { editablePhoneNumber = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Phone
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            onPhoneNumberChange(editablePhoneNumber)
                            isEditingPhoneNumber = false
                            focusManager.clearFocus()
                        }),
                    )
                    IconButton(onClick = {
                        onPhoneNumberChange(editablePhoneNumber)
                        isEditingPhoneNumber = false
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Phone Number")
                    }
                    IconButton(onClick = {
                        isEditingPhoneNumber = false
                        editablePhoneNumber = profile.phoneNumber ?: ""
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel Phone Edit")
                    }
                } else {
                    Text(
                        text = if (profile.phoneNumber.isNullOrBlank()) "Add phone number" else profile.phoneNumber!!,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
                            .clickable {
                                editablePhoneNumber = profile.phoneNumber ?: ""
                                isEditingPhoneNumber = true
                            },
                        color = if (profile.phoneNumber.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = {
                        editablePhoneNumber = profile.phoneNumber ?: ""
                        isEditingPhoneNumber = true
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Phone Number")
                    }
                }
            }

        }
    }
}

@Composable
fun MedicalInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Medical Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicalInfo(
                    title = "BloodType: ",
                    subtitle = "O+"
                )
                Spacer(modifier = Modifier.width(8.dp))
                MedicalInfo(
                    title = "Insurance: ",
                    subtitle = "AIA"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            MedicalInfo(
                title = "Allergies: ",
                subtitle = "Peanut, Penicillin"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MedicalInfo(
                title = "Emergency Contact: ",
                subtitle = "Jane Smith +(555) 123-4567"
            )
        }
    }
}

@Composable
fun OptionsSection(onSignOut : () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProfileButtons(
            text = "Edit Profile",
            vector = Icons.Filled.Create,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp
            )
        )
        ProfileButtons(
            text = "Medical Records",
            vector = Icons.Filled.DateRange,
            shape = RoundedCornerShape(0.dp)
        )
        ProfileButtons(
            text = "Settings",
            vector = Icons.Filled.Settings,
            shape = RoundedCornerShape(0.dp)
        )
        ProfileButtons(
            text = "Sign Out",
            tint = MaterialTheme.colorScheme.error,
            vector = Icons.AutoMirrored.Filled.ExitToApp,
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomEnd = 12.dp,
                bottomStart = 12.dp
            ),
            color = MaterialTheme.colorScheme.error,
            onClick = onSignOut
        )
    }
}

@Composable
fun ProfileButtons(
    text: String,
    vector: ImageVector,
    tint: Color = Color.Black,
    shape: RoundedCornerShape,
    color: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = shape
            )
            .clickable(
                onClick = onClick
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = vector,
                tint = tint,
                contentDescription = null
            )
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = color,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ProfileInfo(text: String, vector: ImageVector, color: Color = Color.Black) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = vector,
            contentDescription = null
        )
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = color,
            fontSize = 16.sp
        )
    }
}


@Composable
fun MedicalInfo(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
