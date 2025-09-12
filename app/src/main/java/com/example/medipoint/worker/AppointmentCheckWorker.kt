package com.example.medipoint.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Repository.AppointmentRepository
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.utils.checkIfWithin500Meters
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class AppointmentCheckWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun doWork(): Result {
        // Get the hospital coordinates (replace with actual coordinates)
        val hospitalLat = 12.9716  // Example latitude
        val hospitalLon = 77.5946  // Example longitude

        // Get the appointmentId passed from the input data
        val appointmentId = inputData.getString("appointmentId") ?: return Result.failure()

        // Check if the patient is within 500 meters
        checkIfWithin500Meters(applicationContext, hospitalLat, hospitalLon) { isWithin500Meters ->
            if (!isWithin500Meters) {
                // Patient is not within 500 meters, cancel appointment and notify next patient
                cancelAppointmentAndNotifyNextPatient(appointmentId)
            }
        }

        return Result.success()
    }

    private fun cancelAppointmentAndNotifyNextPatient(appointmentId: String) {
        val appointmentRepository = AppointmentRepository()
        val alertsRepository = AlertsRepository(applicationContext)

        // Fetch the appointment details using the appointmentId
        val appointment = appointmentRepository.getAppointmentById(appointmentId)

        if (appointment != null) {
            // Cancel the appointment
            appointmentRepository.cancelAppointment(appointmentId)

            // Fetch next available appointment and notify the patient
            val nextAppointment = fetchNextAvailableAppointment()
            sendNotificationToNextPatient(nextAppointment)
        }
    }

    private fun fetchNextAvailableAppointment(): Appointment {
        // Fetch the next available appointment from your data source
        // This should return the next appointment object, with details like deviceToken
        // Placeholder for an appointment object
        return Appointment(
            deviceToken = "next_patient_device_token" // Replace with actual device token
        )
    }

    private fun sendNotificationToNextPatient(appointment: Appointment) {
        // Use Firebase Cloud Messaging (FCM) to notify the next patient
        val notificationTitle = "Appointment Slot Available"
        val notificationMessage = "An appointment slot has become available. Do you want to take it?"

        val data = mapOf(
            "title" to notificationTitle,
            "message" to notificationMessage
        )

        // Send FCM notification
        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder("${appointment.deviceToken}@fcm.googleapis.com")
                .setMessageId("id_${System.currentTimeMillis()}")
                .setData(data)
                .build()
        )
    }

}
