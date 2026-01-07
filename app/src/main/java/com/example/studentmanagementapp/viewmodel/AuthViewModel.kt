package com.example.studentmanagementapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.repository.AuthRepository
import com.example.studentmanagementapp.utils.AuthValidator
import com.example.studentmanagementapp.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val message: String = "") : AuthResult()
    data class Error(val message: String) : AuthResult()
    object NetworkUnavailable : AuthResult()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()

    fun isLoggedIn(): Boolean = repository.currentUser() != null

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val context = getApplication<Application>()
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onResult(AuthResult.NetworkUnavailable)
            return
        }
        if (!AuthValidator.isValidEmail(email)) {
            onResult(AuthResult.Error("Please enter a valid email address."))
            return
        }
        if (password.isBlank()) {
            onResult(AuthResult.Error("Password cannot be empty."))
            return
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repository.login(email.trim(), password) }
            result.onSuccess { onResult(AuthResult.Success()) }
                .onFailure { onResult(AuthResult.Error(it.localizedMessage ?: "Login failed")) }
        }
    }

    fun signup(name: String, email: String, password: String, onResult: (AuthResult) -> Unit) {
        val context = getApplication<Application>()
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onResult(AuthResult.NetworkUnavailable)
            return
        }
        if (!AuthValidator.isValidName(name)) {
            onResult(AuthResult.Error("Name should contain only alphabets."))
            return
        }
        if (!AuthValidator.isValidEmail(email)) {
            onResult(AuthResult.Error("Please enter a valid email address."))
            return
        }
        if (password.isBlank()) {
            onResult(AuthResult.Error("Password cannot be empty."))
            return
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.signup(name.trim(), email.trim(), password)
            }
            result.onSuccess { onResult(AuthResult.Success()) }
                .onFailure { onResult(AuthResult.Error(it.localizedMessage ?: "Signup failed")) }
        }
    }

    fun logout() {
        repository.logout()
    }
}
