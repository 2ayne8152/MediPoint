package com.example.medipoint.Data

enum class CheckInStatus { PENDING, CHECKED_IN, MISSED }

data class CheckInRecord(
    val status: CheckInStatus = CheckInStatus.PENDING,
    val checkedInAt: Long? = null,
    val checkedInLat: Double? = null,
    val checkedInLng: Double? = null,
)