package com.example.medipoint.ui.theme.Screens

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.animation.core.copy
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.google.firebase.Firebase
/*import androidx.privacysandbox.tools.core.generator.build*/
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.tasks.await


@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    var firebaseUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(FirebaseAuth.getInstance().currentUser) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
    }

    fun updateFirebaseUserInMemory(updatedUser: FirebaseUser?) {
        firebaseUser = updatedUser
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        firebaseUser?.let { user ->
            ProfileCard(
                user = user,
                onUsernameChange = { newUsername ->
                    coroutineScope.launch {
                        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateFirebaseUserInMemory(FirebaseAuth.getInstance().currentUser)
                            }
                        }
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
fun ProfileCard(user: FirebaseUser, onUsernameChange: (String) -> Unit) {
    var isEditingUsername by remember { mutableStateOf(false) }
    // Initialize editableUsername with displayName or an empty string if null
    var editableUsername by remember(user.displayName) { mutableStateOf(user.displayName ?: "") }

    var isEditingPhoneNumber by remember { mutableStateOf(false) }
    var editablePhoneNumber by remember { mutableStateOf("") }


    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user.uid) {
        try {
            val userDoc = Firebase.firestore.collection("users").document(user.uid).get().await()
            editablePhoneNumber = userDoc.getString("phoneNumber") ?: ""
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load phone number", Toast.LENGTH_SHORT).show()
        }
    }

    // Determine the text to display based on whether a name exists and if we are editing
    val displayNameText = user.displayName.takeIf { !it.isNullOrBlank() }

    fun saveUsername() {
        if (editableUsername.isBlank()) {
            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        // No need to check if it's the same as currentUserName if currentUserName could be null/empty
        // The check against the actual user.displayName will suffice if no change

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(editableUsername)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
                    onUsernameChange(editableUsername)
                    isEditingUsername = false
                    focusManager.clearFocus()
                } else {
                    Toast.makeText(context, "Failed to update username: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    @SuppressLint("CoroutineCreationDuringComposition")
    fun savePhoneNumber() {
        if (editablePhoneNumber.isBlank()) { // Add more validation as needed (e.g., phone format)
            Toast.makeText(context, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

         val userId = user.uid
         val userDocRef = Firebase.firestore.collection("users").document(userId)
         coroutineScope.launch {
         try {
         userDocRef.update("phoneNumber", editablePhoneNumber).await() // or .set with merge
         Toast.makeText(context, "Phone number updated", Toast.LENGTH_SHORT).show()
         isEditingPhoneNumber = false
         focusManager.clearFocus()
         // Potentially call a lambda to notify ProfileScreen to refresh its Firestore data
         } catch (e: Exception) {
         Toast.makeText(context, "Failed to update phone: ${e.message}", Toast.LENGTH_LONG).show()
         }
         }


        // For UI demonstration purposes without full Firestore integration yet:
        Toast.makeText(context, "Phone number save (UI Demo): $editablePhoneNumber", Toast.LENGTH_SHORT).show()
        isEditingPhoneNumber = false
        focusManager.clearFocus()
        // Here you would also update the state in ProfileScreen that holds the phone number from Firestore.
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Username Row (as before)
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
                        keyboardActions = KeyboardActions(onDone = { saveUsername() })
                    )
                    Row {
                        IconButton(onClick = { saveUsername() }) {
                            Icon(Icons.Filled.Check, contentDescription = "Save Username")
                        }
                        IconButton(onClick = {
                            isEditingUsername = false
                            editableUsername = user.displayName ?: ""
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel Username Edit")
                        }
                    }
                } else {
                    Text(
                        text = displayNameText ?: "Edit Username",
                        fontSize = 20.sp,
                        fontWeight = if (displayNameText != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (displayNameText != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = displayNameText == null) {
                                if (displayNameText == null) {
                                    isEditingUsername = true
                                }
                            }
                    )
                    IconButton(onClick = {
                        editableUsername = user.displayName ?: ""
                        isEditingUsername = true
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Username")
                    }
                }
            }

            // Email (as before)
            Text(
                text = user.email ?: "No email provided",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            // Member Since (as before)
            val creationTimestamp = user.metadata?.creationTimestamp
            val memberSinceText = if (creationTimestamp != null) {
                "Member since ${android.text.format.DateFormat.format("MMMM yyyy", creationTimestamp)}"
            } else {
                "Member since N/A"
            }
            Text(memberSinceText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            // --- Phone Number Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Call, // Or Icons.Filled.Phone
                    contentDescription = "Contact Number Icon",
                    modifier = Modifier.padding(end = 6.dp) // Give some space before the text/field
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
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        keyboardActions = KeyboardActions(onDone = { savePhoneNumber() }),
                    )
                    IconButton(onClick = { savePhoneNumber() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Phone Number")
                    }
                    IconButton(onClick = {
                        isEditingPhoneNumber = false
                        // Reset to original (which should be from Firestore)
                        // For now, using user.phoneNumber, but this needs to be the Firestore value
                        editablePhoneNumber = user.phoneNumber ?: ""
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel Phone Edit")
                    }
                } else {
                    Text(
                        // IMPORTANT: This text should display the phone number fetched from Firestore
                        // For now, it uses user.phoneNumber if available, or a placeholder.
                        text = if (editablePhoneNumber.isNotBlank()) editablePhoneNumber else "Add phone number",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp, top = 12.dp, bottom = 12.dp, end = 12.dp) // Adjust padding to align with TextField
                            .clickable { // Make the text itself clickable to start editing
                                // Ensure editablePhoneNumber is up-to-date before editing
                                // (ideally from your ViewModel/Firestore state)
                                editablePhoneNumber = user.phoneNumber ?: "" // Or your Firestore fetched value
                                isEditingPhoneNumber = true
                            },
                        color = if (editablePhoneNumber.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = {
                        // Ensure editablePhoneNumber is up-to-date before editing
                        editablePhoneNumber = user.phoneNumber ?: "" // Or your Firestore fetched value
                        isEditingPhoneNumber = true
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Phone Number")
                    }
                }
            }

            // Other ProfileInfo (as before, these would also come from Firestore ideally)
            ProfileInfo(text = "Born March 15, 1985", vector = Icons.Filled.DateRange)
            ProfileInfo(text = "123 Main Street, Springfield, IL 62701", vector = Icons.Filled.Place)
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
