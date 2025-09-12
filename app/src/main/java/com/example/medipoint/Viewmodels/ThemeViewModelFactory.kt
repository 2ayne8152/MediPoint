package com.example.medipoint.Viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medipoint.Data.AppDatabase
import com.example.medipoint.Repository.ThemeRepository

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = AppDatabase.getDatabase(context).themeSettingDao()
        val repository = ThemeRepository(dao)
        @Suppress("UNCHECKED_CAST")
        return ThemeViewModel(repository) as T
    }
}
