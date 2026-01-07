package com.example.studentmanagementapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.MainActivity
import com.example.studentmanagementapp.databinding.ActivitySignupBinding
import com.example.studentmanagementapp.viewmodel.AuthResult
import com.example.studentmanagementapp.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val NAME_REGEX = Regex("^[A-Za-z ]+$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim().orEmpty()
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            when {
                name.isEmpty() -> {
                    showToast(getString(com.example.studentmanagementapp.R.string.name_required))
                    return@setOnClickListener
                }

                !NAME_REGEX.matches(name) -> {
                    showToast(getString(com.example.studentmanagementapp.R.string.name_letters_only))
                    return@setOnClickListener
                }

                email.isEmpty() -> {
                    showToast(getString(com.example.studentmanagementapp.R.string.email_required))
                    return@setOnClickListener
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast(getString(com.example.studentmanagementapp.R.string.invalid_email))
                    return@setOnClickListener
                }

                password.isEmpty() -> {
                    showToast(getString(com.example.studentmanagementapp.R.string.password_required))
                    return@setOnClickListener
                }
            }

            // ✅ Validation passed
            binding.btnSignup.isEnabled = false
            authViewModel.signup(name, email, password) { result ->
                binding.btnSignup.isEnabled = true
                when (result) {
                    is AuthResult.Success -> navigateHome()
                    is AuthResult.NetworkUnavailable ->
                        showToast(getString(com.example.studentmanagementapp.R.string.network_required))
                    is AuthResult.Error ->
                        showToast(result.message)
                }
            }
        }


        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateHome() {
        com.example.studentmanagementapp.utils.AdManager.showInterstitial(this) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
