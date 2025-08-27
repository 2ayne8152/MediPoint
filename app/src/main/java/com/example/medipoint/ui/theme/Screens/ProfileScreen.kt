package com.example.medipoint.ui.theme.Screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Preview(showBackground = true)
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileCard()
        Spacer(modifier = Modifier.height(16.dp))
        MedicalInfoCard()
        Spacer(modifier = Modifier.height(16.dp))
        OptionsSection()
    }
}

@Composable
fun ProfileCard() {
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
            Text("John Smith", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "john.smith@email.com",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "Member since January 2023",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfo(
                text = "+1 (555) 123-4567",
                vector = Icons.Filled.Call
            )
            ProfileInfo(
                text = "Born March 15, 1985",
                vector = Icons.Filled.DateRange
            )
            ProfileInfo(
                text = "123 Main Street, Springfield, IL 62701",
                vector = Icons.Filled.Place
            )
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
fun OptionsSection() {
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
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ProfileButtons(
    text: String,
    vector: ImageVector,
    tint: Color = Color.Black,
    shape: RoundedCornerShape,
    color: Color = Color.Black
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
                onClick = { }
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
