package com.example.medipoint.Data

enum class CheckInStatus { PENDING, CHECKED_IN, MISSED }

data class CheckInRecord(
    val id: String = "",   // <-- Added this
    val status: CheckInStatus = CheckInStatus.PENDING,
    val checkedInAt: Long? = null,
    val checkedInLat: Double? = null,
    val checkedInLng: Double? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "status" to status.name, // Store as String
            "checkedInAt" to checkedInAt,
            "checkedInLat" to checkedInLat,
            "checkedInLng" to checkedInLng
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): CheckInRecord {
            return CheckInRecord(
                id = map["id"] as? String ?: "",
                status = CheckInStatus.valueOf(map["status"] as String),
                checkedInAt = map["checkedInAt"] as? Long,
                checkedInLat = map["checkedInLat"] as? Double,
                checkedInLng = map["checkedInLng"] as? Double
            )
        }
    }
}
