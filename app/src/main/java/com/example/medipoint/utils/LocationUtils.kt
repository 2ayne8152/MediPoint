package com.example.medipoint.utils

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

// Function to calculate the distance between two locations
fun getDistanceBetweenLocations(
    currentLat: Double,
    currentLon: Double,
    hospitalLat: Double,
    hospitalLon: Double
): Float {
    val location1 = Location("point A")
    location1.latitude = currentLat
    location1.longitude = currentLon

    val location2 = Location("point B")
    location2.latitude = hospitalLat
    location2.longitude = hospitalLon

    return location1.distanceTo(location2) // Returns distance in meters
}

// Function to check if the user is within 500 meters of the hospital
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
fun checkIfWithin500Meters(context: Context, hospitalLat: Double, hospitalLon: Double, onLocationChecked: (Boolean) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnCompleteListener { task ->
        val location: Location = task.result
        if (location != null) {
            val distance = getDistanceBetweenLocations(
                location.latitude,
                location.longitude,
                hospitalLat,
                hospitalLon
            )

            // Check if the user is within 500 meters
            onLocationChecked(distance <= 500)
        }
    }
}
