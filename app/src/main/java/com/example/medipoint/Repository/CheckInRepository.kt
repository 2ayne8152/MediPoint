package com.example.medipoint.Repository

import com.example.medipoint.Data.CheckInDao
import com.example.medipoint.Data.CheckInRecord

class CheckInRepository(private val dao: CheckInDao) {
    suspend fun addCheckInRecord(appointmentId: String, record: CheckInRecord) = dao.addCheckInRecord(appointmentId, record)
    suspend fun getCheckInRecord(appointmentId: String, userId: String) = dao.getCheckInRecord(appointmentId, userId)
}
