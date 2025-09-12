package com.example.medipoint.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_settings")
data class ThemeSetting(
    @PrimaryKey val id: Int = 1, // Only one row
    val isDarkMode: Boolean
)
