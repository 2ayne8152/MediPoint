package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.Data.CheckInStatus
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val hospitalLat = 37.4219983
    private val hospitalLng = -122.084
    private val checkInRadius = 200 // meters

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // ✅ Backing StateFlow
    private val _checkInRecord = MutableStateFlow(CheckInRecord(status = CheckInStatus.PENDING))
    val checkInRecord: StateFlow<CheckInRecord> = _checkInRecord

    @SuppressLint("MissingPermission")
    fun attemptCheckIn() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        location.latitude, location.longitude,
                        hospitalLat, hospitalLng,
                        distance
                    )
                    if (distance[0] <= checkInRadius) {
                        _checkInRecord.value = CheckInRecord(
                            status = CheckInStatus.CHECKED_IN,
                            checkedInAt = System.currentTimeMillis(),
                            checkedInLat = location.latitude,
                            checkedInLng = location.longitude
                        )
                    } else {
                        _checkInRecord.value = CheckInRecord(status = CheckInStatus.MISSED)
                    }
                } else {
                    _checkInRecord.value = CheckInRecord(status = CheckInStatus.MISSED)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _checkInRecord.value = CheckInRecord(status = CheckInStatus.MISSED)
            }
        }
    }
}
