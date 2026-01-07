package com.example.studentmanagementapp.utils

object AuthValidator {
    private val emailRegex =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    private val nameRegex = "^[A-Za-z ]+$".toRegex()

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidName(name: String): Boolean = nameRegex.matches(name.trim())
}
