package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val repository: ThemeRepository) : ViewModel() {

    val isDarkMode = repository.themeSetting
        .map { it?.isDarkMode ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.saveTheme(isDark) // saves to Room
        }
    }
}