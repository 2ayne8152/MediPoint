package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.core.net.ParseException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Data.CheckInRecord
import com.example.medipoint.Data.FirestoreAppointmentDao
import com.example.medipoint.Data.FirestoreCheckInDao
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.CheckInRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class DistanceBannerUiState {
    data object Hidden : DistanceBannerUiState() // Banner is not shown
    data class ShowBanner(
        val message: String,
        val actionText: String?, // e.g., "Check Distance"
        val appointmentId: String,
        val appointmentDetails: String, // e.g., "Dr. Smith at 10:00 AM"
        val isError: Boolean = false,
        val isLoadingAction: Boolean = false // If the banner action triggers a loading state
    ) : DistanceBannerUiState()
}
class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    // These are where the emulator is located at
    private val hospitalLat = 37.4219983
    private val hospitalLng = -122.084
    private val checkInRadius = 200

    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow<CheckInRecord?>(null)
    val checkInRecord: StateFlow<CheckInRecord?> = _checkInRecord

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    private val _appointmentDateTime = MutableStateFlow<Long?>(null)
    val appointmentDateTime: StateFlow<Long?> = _appointmentDateTime

    private val appointmentDao = FirestoreAppointmentDao()
    private val appointmentRepository = AppointmentRepository(appointmentDao)

    private val checkInDao = FirestoreCheckInDao()
    private val checkInRepository = CheckInRepository(checkInDao)

    init {
        // Start periodic status updater
        startAutoUpdateMissedAppointments()
    }

    // Helper to parse date and time string to a Date object
    private fun parseAppointmentDateTime(dateStr: String, timeStr: String): Date? {
        val format = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
        return try {
            format.parse("$dateStr $timeStr")
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun prepareDistanceCheckBannerForNextAppointment(hoursWindow: Int = 48) {
        val currentUser = auth.currentUser ?: run {
            _distanceBannerState.value = DistanceBannerUiState.Hidden // No user, no banner
            return
        }

        viewModelScope.launch {
            val result = appointmentRepository.getAppointments(currentUser.uid)
            if (result.isSuccess) {
                val appointments = result.getOrNull() ?: emptyList()
                val now = Calendar.getInstance()

                val nextUpcomingAppointment = appointments
                    .filter { it.status == "Scheduled" }
                    .mapNotNull { appt ->
                        parseAppointmentDateTime(appt.date, appt.time)?.let { dateTime ->
                            Pair(appt, dateTime)
                        }
                    }
                    .filter { (_, dateTime) ->
                        dateTime.after(now.time) // Only future appointments
                    }
                    .minByOrNull { (_, dateTime) -> dateTime.time } // Find the soonest

                if (nextUpcomingAppointment != null) {
                    val (appointment, appointmentDateTime) = nextUpcomingAppointment
                    val timeDifferenceMillis = appointmentDateTime.time - now.timeInMillis
                    val timeDifferenceHours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)

                    if (timeDifferenceHours in 0 until hoursWindow) { // Within the specified window (e.g., next 48 hours)
                        // Check if this appointment was already cancelled by this feature before
                        // (This requires knowing if _distanceBannerState was previously resolved for this apptId)
                        // For simplicity now, we'll just show it if it's scheduled.
                        // A more robust solution might involve storing a flag or checking banner history.

                        _distanceBannerState.value = DistanceBannerUiState.ShowBanner(
                            message = "Your appointment with ${appointment.doctorName} on ${appointment.date} is approaching.",
                            actionText = "Check Distance",
                            appointmentId = appointment.id,
                            appointmentDetails = "with ${appointment.doctorName} at ${appointment.time} on ${appointment.date}",
                            isError = false,
                            isLoadingAction = false
                        )
                    } else {
                        _distanceBannerState.value = DistanceBannerUiState.Hidden
                    }
                } else {
                    _distanceBannerState.value = DistanceBannerUiState.Hidden
                }
            } else {
                // Handle error fetching appointments - maybe show a generic error banner or log
                _distanceBannerState.value = DistanceBannerUiState.ShowBanner(
                    message = "Could not load appointment details to check for distance alerts.",
                    actionText = null,
                    appointmentId = "",
                    appointmentDetails = "",
                    isError = true
                )
            }
        }
    }

    @SuppressLint("MissingPermission") // Permissions should be checked by the UI before calling
    fun performDistanceCheckAndCancelIfNeeded(appointmentId: String, cancellationRadiusMeters: Double) {
        val currentUser = auth.currentUser ?: run {
            // Update banner to an error state if user gets logged out mid-process
            (_distanceBannerState.value as? DistanceBannerUiState.ShowBanner)?.copy(
                message = "User not logged in. Cannot check distance.",
                actionText = null,
                isError = true,
                isLoadingAction = false
            )?.let { _distanceBannerState.value = it }
            return
        }

        // Ensure we have appointment details for the banner messages
        val currentBannerState = _distanceBannerState.value
        if (currentBannerState !is DistanceBannerUiState.ShowBanner || currentBannerState.appointmentId != appointmentId) {
            // This shouldn't happen if called correctly from the banner, but good to check
            _distanceBannerState.value = DistanceBannerUiState.ShowBanner(
                message = "Error: Mismatch in appointment data for distance check.",
                actionText = null, appointmentId = appointmentId, appointmentDetails = "Unknown appointment", isError = true
            )
            return
        }

        _distanceBannerState.value = currentBannerState.copy(isLoadingAction = true, message = "Checking your location...")

        viewModelScope.launch {
            try {
                val userLocation = fusedLocationClient.lastLocation.await()

                if (userLocation == null) {
                    _distanceBannerState.value = currentBannerState.copy(
                        message = "Could not get your current location. Please ensure location services are enabled and try again.",
                        actionText = "Check Distance", // Allow retry
                        isError = true,
                        isLoadingAction = false
                    )
                    return@launch
                }

                val distanceResults = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    hospitalLat, hospitalLng,
                    distanceResults
                )
                val distanceInMeters = distanceResults[0]

                if (distanceInMeters > cancellationRadiusMeters) {
                    // Too far, attempt to cancel
                    _distanceBannerState.value = currentBannerState.copy(
                        message = "You are approximately ${"%.0f".format(distanceInMeters)}m away. Cancelling appointment...",
                        actionText = null, // No action after this point from banner
                        isLoadingAction = true // Still loading as we cancel
                    )
                    val cancellationResult = appointmentRepository.cancelAppointment(appointmentId)
                    if (cancellationResult.isSuccess) {
                        _distanceBannerState.value = currentBannerState.copy(
                            message = "Appointment ${currentBannerState.appointmentDetails} has been cancelled as you are too far (${"%.0f".format(distanceInMeters)}m).",
                            actionText = null,
                            isError = false,
                            isLoadingAction = false
                        )
                    } else {
                        _distanceBannerState.value = currentBannerState.copy(
                            message = "You are too far (${"%.0f".format(distanceInMeters)}m). Failed to cancel appointment: ${cancellationResult.exceptionOrNull()?.message}",
                            actionText = null, // Or maybe a "Retry Cancellation" if that makes sense
                            isError = true,
                            isLoadingAction = false
                        )
                    }
                } else {
                    // Within range
                    _distanceBannerState.value = currentBannerState.copy(
                        message = "You are within range (${"%.0f".format(distanceInMeters)}m) for your appointment ${currentBannerState.appointmentDetails}.",
                        actionText = null, // Or "Dismiss"
                        isError = false,
                        isLoadingAction = false
                    )
                }

            } catch (e: SecurityException) { // Should be caught by permission check in UI, but defensive
                e.printStackTrace()
                _distanceBannerState.value = currentBannerState.copy(
                    message = "Location permission denied. Cannot check distance.",
                    actionText = "Check Distance", // Allow retry after granting permission
                    isError = true,
                    isLoadingAction = false
                )
            }
            catch (e: Exception) {
                e.printStackTrace()
                _distanceBannerState.value = currentBannerState.copy(
                    message = "An error occurred while checking distance: ${e.message}",
                    actionText = "Check Distance", // Allow retry
                    isError = true,
                    isLoadingAction = false
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun attemptCheckIn(appointmentId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                val checkedIn = location?.let {
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        it.latitude, it.longitude,
                        hospitalLat, hospitalLng,
                        distance
                    )
                    distance[0] <= checkInRadius
                } ?: false

                val record = CheckInRecord(
                    checkedIn = checkedIn,
                    checkInTime = if (checkedIn) System.currentTimeMillis() else null,
                    checkInLat = if (checkedIn) location?.latitude else null,
                    checkInLng = if (checkedIn) location?.longitude else null,
                    userId = currentUser.uid,
                    appointmentId = appointmentId
                )

                _checkInRecord.value = record

                if (checkedIn) {
                    appointmentRepository.updateAppointmentStatus(appointmentId, "Checked-In")
                    checkInRepository.addCheckInRecord(appointmentId, record)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _checkInRecord.value = CheckInRecord(
                    checkedIn = false,
                    userId = currentUser.uid,
                    appointmentId = appointmentId
                )
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.cancelAppointment(appointmentId)
        }
    }

    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val result = appointmentRepository.getAppointments(currentUser.uid)
            if (result.isSuccess) {
                val appt = result.getOrNull()?.find { it.id == appointmentId }
                _appointment.value = appt

                val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
                _appointmentDateTime.value = appt?.let {
                    try { sdf.parse("${it.date} ${it.time}")?.time } catch (e: Exception){ null }
                }

                // Evaluate appointment status after loading details
                evaluateAppointmentStatus(appointmentId)

            } else {
                _appointment.value = null
                _appointmentDateTime.value = null
            }
        }
    }

    fun loadUserCheckInRecord(appointmentId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val result = checkInRepository.getCheckInRecord(appointmentId, currentUser.uid)
            _checkInRecord.value = result.getOrNull()
        }
    }

    fun setCheckInRecord(record: CheckInRecord?) {
        _checkInRecord.value = record
    }

    private fun evaluateAppointmentStatus(appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appt = appointment.value ?: return@launch
            val appointmentTime = appointmentDateTime.value ?: return@launch
            val now = System.currentTimeMillis()

            // give 10 min grace period
            if (now > appointmentTime + 10 * 60 * 1000) {
                if (_checkInRecord.value?.checkedIn == true) {
                    appointmentRepository.updateAppointmentStatus(appointmentId, "Completed")
                } else {
                    appointmentRepository.updateAppointmentStatus(appointmentId, "Missed")
                }
            }
        }
    }

    private fun startAutoUpdateMissedAppointments() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val currentUser = auth.currentUser ?: break
                val result = appointmentRepository.getAppointments(currentUser.uid)
                val appointments = result.getOrNull() ?: emptyList()

                appointments.forEach { appt ->
                    val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
                    val apptTime = try { sdf.parse("${appt.date} ${appt.time}")?.time } catch (e: Exception){ null }
                    if (apptTime != null && System.currentTimeMillis() > apptTime + 10 * 60 * 1000) {
                        if (appt.status != "Checked-In" && appt.status != "Completed" && appt.status != "Missed") {
                            appointmentRepository.updateAppointmentStatus(appt.id, "Missed")
                        }
                    }
                }
                delay(60 * 1000) // Check every 1 minute
            }
        }
    }
}
