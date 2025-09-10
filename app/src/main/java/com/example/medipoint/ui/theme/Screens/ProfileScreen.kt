package com.example.medipoint.ui.theme.Screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.medipoint.Data.MedicalInfoEntity
import com.example.medipoint.Viewmodels.ProfileViewModel
import com.example.medipoint.Viewmodels.UserProfile

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val saveStatus by profileViewModel.saveStatus.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State to control the visibility of dialogs
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditMedicalInfoDialog by remember { mutableStateOf(false) }

    // Show save status messages (Toast)
    LaunchedEffect(saveStatus) {
        saveStatus?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            profileViewModel.clearSaveStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Avatar Section
        UserAvatarSection(userProfile)

        Spacer(modifier = Modifier.height(16.dp))

        userProfile?.let { profile ->
            ProfileCard(
                profile = profile,
                onUsernameChange = { newName ->
                    coroutineScope.launch {
                        profileViewModel.updateDisplayName(newName)
                    }
                }
            )

            // Edit Profile Dialog
            if (showEditProfileDialog) {
                EditProfileDialog(
                    currentPhoneNumber = profile.phoneNumber ?: "",
                    onDismissRequest = { showEditProfileDialog = false },
                    onApplyChanges = { newPhoneNumber ->
                        coroutineScope.launch {
                            profileViewModel.updatePhoneNumber(newPhoneNumber)
                        }
                        showEditProfileDialog = false
                    }
                )
            }

            // Edit Medical Info Dialog
            if (showEditMedicalInfoDialog) {
                EditMedicalInfoDialog(
                    currentMedicalInfo = MedicalInfoEntity(
                        userId = profile.uid,
                        bloodType = profile.bloodType,
                        insuranceProvider = profile.insurance,
                        allergies = profile.allergies,
                        emergencyContactName = profile.emergencyContactName,
                        emergencyContactPhone = profile.emergencyContactPhone
                    ),
                    onDismissRequest = { showEditMedicalInfoDialog = false },
                    onApplyChanges = { newMedicalInfo ->
                        coroutineScope.launch {
                            profileViewModel.updateMedicalInfo(newMedicalInfo)
                        }
                        showEditMedicalInfoDialog = false
                    }
                )
            }

        } ?: Text("Loading user profile...", modifier = Modifier.padding(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Medical Info Card
        userProfile?.let { profile ->
            MedicalInfoCard(
                medicalInfo = MedicalInfoEntity(
                    userId = profile.uid,
                    bloodType = profile.bloodType,
                    insuranceProvider = profile.insurance,
                    allergies = profile.allergies,
                    emergencyContactName = profile.emergencyContactName,
                    emergencyContactPhone = profile.emergencyContactPhone
                ),
                onEditClicked = { showEditMedicalInfoDialog = true }
            )
        } ?: Text("No medical information available", modifier = Modifier.padding(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        OptionsSection(
            onSignOut = onSignOut,
            onEditProfileClicked = { showEditProfileDialog = true },
            onEditMedicalInfoClicked = { showEditMedicalInfoDialog = true }
        )
    }
}
@Composable
fun UserAvatarSection(userProfile: UserProfile?) {
    // Simple user avatar with initial
    val initials = userProfile?.displayName?.take(2)?.uppercase() ?: "U"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Avatar circle with initials
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.size(80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User name
        Text(
            text = userProfile?.displayName ?: "User",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // User email
        Text(
            text = userProfile?.email ?: "",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}


@Composable
fun ProfileCard(
    profile: UserProfile,
    onUsernameChange: (String) -> Unit
) {
    var isEditingUsername by remember { mutableStateOf(false) }
    var editableUsername by remember { mutableStateOf(profile.displayName ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Username Row
            UsernameEditingRow(
                isEditing = isEditingUsername,
                currentName = profile.displayName ?: "",
                editableName = editableUsername,
                onEditChange = { editableUsername = it },
                onEditToggle = {
                    isEditingUsername = it
                    if (!it) editableUsername = profile.displayName ?: ""
                },
                onSave = {
                    onUsernameChange(editableUsername)
                    isEditingUsername = false
                }
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Email (read-only)
            ProfileInfoItem(
                icon = Icons.Filled.Email,
                label = "Email",
                value = profile.email ?: "No email provided",
                isEditable = false
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Phone Number (read-only, edited via dialog)
            ProfileInfoItem(
                icon = Icons.Filled.Call,
                label = "Phone",
                value = profile.phoneNumber ?: "No phone number",
                isEditable = false
            )
        }
    }
}

@Composable
fun UsernameEditingRow(
    isEditing: Boolean,
    currentName: String,
    editableName: String,
    onEditChange: (String) -> Unit,
    onEditToggle: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = editableName,
                onValueChange = onEditChange,
                label = { Text("Username") },
                placeholder = { Text("Enter your username") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onSave()
                    focusManager.clearFocus()
                })
            )
            Row {
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Save Username",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onEditToggle(false) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Cancel Username Edit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentName.ifEmpty { "Set your username" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (currentName.isNotEmpty())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (currentName.isEmpty()) {
                    Text(
                        text = "Tap to set your username",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(
                onClick = {
                    onEditChange(currentName)
                    onEditToggle(true)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit Username",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditable: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label icon",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (isEditable && onEditClick != null) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MedicalInfoCard(
    medicalInfo: MedicalInfoEntity,
    onEditClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Medical Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onEditClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Medical Information",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MedicalInfoItem(
                title = "Blood Type",
                value = medicalInfo.bloodType ?: "Not specified"
            )

            Spacer(modifier = Modifier.height(8.dp))

            MedicalInfoItem(
                title = "Insurance Provider",
                value = medicalInfo.insuranceProvider ?: "Not specified"
            )

            Spacer(modifier = Modifier.height(8.dp))

            MedicalInfoItem(
                title = "Allergies",
                value = medicalInfo.allergies ?: "None specified"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Emergency Contact - combined name and phone
            val emergencyContact = if (medicalInfo.emergencyContactName != null && medicalInfo.emergencyContactPhone != null) {
                "${medicalInfo.emergencyContactName} - ${medicalInfo.emergencyContactPhone}"
            } else if (medicalInfo.emergencyContactName != null) {
                medicalInfo.emergencyContactName
            } else if (medicalInfo.emergencyContactPhone != null) {
                medicalInfo.emergencyContactPhone
            } else {
                "Not specified"
            }

            MedicalInfoItem(
                title = "Emergency Contact",
                value = emergencyContact
            )
        }
    }
}


@Composable
fun MedicalInfoItem(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun EditMedicalInfoDialog(
    currentMedicalInfo: MedicalInfoEntity,
    onDismissRequest: () -> Unit,
    onApplyChanges: (MedicalInfoEntity) -> Unit
) {
    var bloodType by remember { mutableStateOf(currentMedicalInfo.bloodType ?: "") }
    var insuranceProvider by remember { mutableStateOf(currentMedicalInfo.insuranceProvider ?: "") }
    var allergies by remember { mutableStateOf(currentMedicalInfo.allergies ?: "") }
    var emergencyContactName by remember { mutableStateOf(currentMedicalInfo.emergencyContactName ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(currentMedicalInfo.emergencyContactPhone ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Edit Medical Information") },
        text = {
            Column {
                OutlinedTextField(
                    value = bloodType,
                    onValueChange = { bloodType = it },
                    label = { Text("Blood Type (e.g., O+, A-, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = insuranceProvider,
                    onValueChange = { insuranceProvider = it },
                    label = { Text("Insurance Provider") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text("Emergency Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = { Text("Emergency Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApplyChanges(
                        MedicalInfoEntity(
                            userId = currentMedicalInfo.userId,
                            bloodType = bloodType.ifEmpty { null },
                            insuranceProvider = insuranceProvider.ifEmpty { null },
                            allergies = allergies.ifEmpty { null },
                            emergencyContactName = emergencyContactName.ifEmpty { null },
                            emergencyContactPhone = emergencyContactPhone.ifEmpty { null }
                        )
                    )
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OptionsSection(
    onSignOut: () -> Unit,
    onEditProfileClicked: () -> Unit,
    onEditMedicalInfoClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProfileButtons(
            text = "Edit Profile",
            vector = Icons.Filled.Create,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp
            ),
            onClick = onEditProfileClicked
        )
        ProfileButtons(
            text = "Edit Medical Info",
            vector = Icons.Filled.Edit,
            shape = RoundedCornerShape(0.dp),
            onClick = onEditMedicalInfoClicked
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
    tint: Color = MaterialTheme.colorScheme.onSurface,
    shape: RoundedCornerShape,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = vector,
                tint = tint,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = color,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentPhoneNumber: String,
    onDismissRequest: () -> Unit,
    onApplyChanges: (newPhoneNumber: String) -> Unit
) {
    var phoneNumberInput by remember { mutableStateOf(currentPhoneNumber) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Edit Profile") },
        text = {
            Column {
                Text("Update your phone number:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = { phoneNumberInput = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApplyChanges(phoneNumberInput)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}