package com.example.medipoint.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Repository.AppointmentRepository

class BookingViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val alertsRepository: AlertsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(appointmentRepository, alertsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
