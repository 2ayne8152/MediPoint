package com.example.medipoint.Data

interface CheckInDao {
    suspend fun addCheckInRecord(appointmentId: String, record: CheckInRecord): Result<Unit>
    suspend fun getCheckInRecord(appointmentId: String, userId: String): Result<CheckInRecord?>
}