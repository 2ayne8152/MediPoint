package com.example.medipoint.utils // Or your preferred utility package

fun isValidPhoneNumber(phoneNumber: String): Pair<Boolean, String?> {
    // 1. Check for non-digit characters
    if (!phoneNumber.matches(Regex("^\\d+\$"))) {
        return Pair(false, "Phone number can only contain digits.")
    }

    // 2. Check length
    if (phoneNumber.length !in 10..11) {
        return Pair(false, "Phone number must be 10 or 11 digits long.")
    }

    return Pair(true, null) // Valid, no error message
}

fun isValidEmail(email: String): Boolean { // Example of another validation function
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}