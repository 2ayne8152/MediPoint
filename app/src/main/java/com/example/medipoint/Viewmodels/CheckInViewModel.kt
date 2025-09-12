package com.example.medipoint.Viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location // Import for distanceBetween
import androidx.core.net.ParseException // Ensure this import is correct if you use it
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
import kotlinx.coroutines.flow.asStateFlow // Added for asStateFlow()
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat // Keep this for SimpleDateFormat
// import java.text.ParseException // This is from java.text, not androidx.core.net
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Sealed class for banner UI states
sealed class DistanceBannerUiState {
    data object Hidden : DistanceBannerUiState()
    data class ShowBanner(
        val message: String,
        val actionText: String?,
        val appointmentId: String,
        val appointmentDetails: String,
        val isError: Boolean = false,
        val isLoadingAction: Boolean = false
    ) : DistanceBannerUiState()
}

class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val hospitalLat = 37.4219983
    private val hospitalLng = -122.084
    private val checkInRadius = 200

    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _checkInRecord = MutableStateFlow<CheckInRecord?>(null)
    val checkInRecord: StateFlow<CheckInRecord?> = _checkInRecord.asStateFlow() // Use asStateFlow

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment.asStateFlow() // Use asStateFlow

    private val _appointmentDateTime = MutableStateFlow<Long?>(null)
    val appointmentDateTime: StateFlow<Long?> = _appointmentDateTime.asStateFlow() // Use asStateFlow

    // --- THIS WAS THE MISSING DECLARATION ---
    private val _distanceBannerState = MutableStateFlow<DistanceBannerUiState>(DistanceBannerUiState.Hidden)
    val distanceBannerState: StateFlow<DistanceBannerUiState> = _distanceBannerState.asStateFlow()
    // ---

    private val appointmentDao = FirestoreAppointmentDao()
    private val appointmentRepository = AppointmentRepository(appointmentDao)

    private val checkInDao = FirestoreCheckInDao()
    private val checkInRepository = CheckInRepository(checkInDao)

    init {
        startAutoUpdateMissedAppointments()
    }

    private fun parseAppointmentDateTime(dateStr: String, timeStr: String): Date? {
        val format = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
        return try {
            format.parse("$dateStr $timeStr")
        } catch (e: java.text.ParseException) { // Corrected to java.text.ParseException
            e.printStackTrace()
            null
        }
    }

    fun prepareDistanceCheckBannerForNextAppointment(hoursWindow: Int = 48) {
        val currentUser = auth.currentUser ?: run {
            _distanceBannerState.value = DistanceBannerUiState.Hidden
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
                        dateTime.after(now.time)
                    }
                    .minByOrNull { (_, dateTime) -> dateTime.time }

                if (nextUpcomingAppointment != null) {
                    val (appointment, appointmentDateTime) = nextUpcomingAppointment
                    val timeDifferenceMillis = appointmentDateTime.time - now.timeInMillis
                    val timeDifferenceHours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)

                    if (timeDifferenceHours in 0 until hoursWindow) {
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

    @SuppressLint("MissingPermission")
    fun performDistanceCheckAndCancelIfNeeded(appointmentId: String, cancellationRadiusMeters: Double) {
        val currentUser = auth.currentUser ?: run {
            (_distanceBannerState.value as? DistanceBannerUiState.ShowBanner)?.copy(
                message = "User not logged in. Cannot check distance.",
                actionText = null,
                isError = true,
                isLoadingAction = false
            )?.let { _distanceBannerState.value = it }
            return
        }

        val currentBannerState = _distanceBannerState.value
        if (currentBannerState !is DistanceBannerUiState.ShowBanner || currentBannerState.appointmentId != appointmentId) {
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
                        actionText = "Check Distance",
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
                    _distanceBannerState.value = currentBannerState.copy(
                        message = "You are approximately ${"%.0f".format(distanceInMeters)}m away. Cancelling appointment...",
                        actionText = null,
                        isLoadingAction = true
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
                            message = "You are too far (${"%.0f".format(distanceInMeters)}m). Failed to cancel: ${cancellationResult.exceptionOrNull()?.message}",
                            actionText = null,
                            isError = true,
                            isLoadingAction = false
                        )
                    }
                } else {
                    _distanceBannerState.value = currentBannerState.copy(
                        message = "You are within range (${"%.0f".format(distanceInMeters)}m) for your appointment ${currentBannerState.appointmentDetails}.",
                        actionText = null,
                        isError = false,
                        isLoadingAction = false
                    )
                }

            } catch (e: SecurityException) {
                e.printStackTrace()
                _distanceBannerState.value = currentBannerState.copy(
                    message = "Location permission denied. Cannot check distance.",
                    actionText = "Check Distance",
                    isError = true,
                    isLoadingAction = false
                )
            }
            catch (e: Exception) {
                e.printStackTrace()
                _distanceBannerState.value = currentBannerState.copy(
                    message = "An error occurred: ${e.message}",
                    actionText = "Check Distance",
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
                    Location.distanceBetween(
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
            // If you want the banner to react to general cancellations too,
            // you might want to update _distanceBannerState here if the cancelled
            // appointmentId matches the one currently in the banner.
            // For example:
            // if ((_distanceBannerState.value as? DistanceBannerUiState.ShowBanner)?.appointmentId == appointmentId) {
            //     _distanceBannerState.value = DistanceBannerUiState.Hidden // Or a "cancelled" message
            // }
        }
    }

    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val result = appointmentRepository.getAppointments(currentUser.uid)
            if (result.isSuccess) {
                val appt = result.getOrNull()?.find { it.id == appointmentId }
                _appointment.value = appt
                _appointmentDateTime.value = appt?.let {
                    parseAppointmentDateTime(it.date, it.time)?.time
                }
                appt?.id?.let { evaluateAppointmentStatus(it) } // Check appt.id before calling
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
            val appt = _appointment.value
            val appointmentTime = _appointmentDateTime.value

            if (appt == null || appt.id != appointmentId || appointmentTime == null) return@launch

            val now = System.currentTimeMillis()
            if (now > appointmentTime + 10 * 60 * 1000) {
                if (_checkInRecord.value?.checkedIn == true && appt.status != "Completed") {
                    appointmentRepository.updateAppointmentStatus(appt.id, "Completed")
                } else if (_checkInRecord.value?.checkedIn != true && appt.status !in listOf("Completed", "Missed", "Cancelled")) {
                    appointmentRepository.updateAppointmentStatus(appt.id, "Missed")
                }
            }
        }
    }

    private fun startAutoUpdateMissedAppointments() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                auth.currentUser?.uid?.let { userId ->
                    val result = appointmentRepository.getAppointments(userId)
                    val appointments = result.getOrNull() ?: emptyList()
                    val now = System.currentTimeMillis()

                    appointments.forEach { appt ->
                        parseAppointmentDateTime(appt.date, appt.time)?.let { apptDateTime ->
                            if (now > apptDateTime.time + 10 * 60 * 1000) {
                                if (appt.status !in listOf("Checked-In", "Completed", "Missed", "Cancelled")) {
                                    appointmentRepository.updateAppointmentStatus(appt.id, "Missed")
                                }
                            }
                        }
                    }
                }
                delay(TimeUnit.MINUTES.toMillis(1)) // More readable delay
            }
        }
    }
}
