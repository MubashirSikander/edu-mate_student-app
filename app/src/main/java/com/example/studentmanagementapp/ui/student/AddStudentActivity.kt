package com.example.studentmanagementapp.ui.student

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.databinding.ActivityAddStudentBinding
import com.example.studentmanagementapp.viewmodel.StudentViewModel

class AddStudentActivity : AppCompatActivity() {

    private var isEditMode = false
    private var studentId: Long = -1

    private lateinit var binding: ActivityAddStudentBinding
    private val viewModel: StudentViewModel by viewModels()
    private val registrationPattern = "^[A-Z]{4}\\d{9}$".toRegex()
    private val contactPattern = "^03\\d{9}$".toRegex()
    private val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getStringExtra("MODE") == "EDIT"
        studentId = intent.getLongExtra("STUDENT_ID", -1)

        if (isEditMode) {
            binding.btnSaveStudent.text = "Update Student"
            binding.titleText.text = "Edit Student"
            loadStudentData()
        }

        // Make registration uppercase and validate
        binding.etRegistration.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val upper = text.uppercase()
                if (text != upper) {
                    binding.etRegistration.setText(upper)
                    binding.etRegistration.setSelection(upper.length)
                    return
                }
                binding.etRegistration.error =
                    if (upper.isNotEmpty() && !registrationPattern.matches(upper)) {
                        "Format must be AAAA000000000"
                    } else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Show/hide password toggle
        binding.etPassword.setOnTouchListener { _, _ ->
            false // allow default behavior
        }


        binding.btnSaveStudent.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val contact = binding.etContact.text.toString().trim()
            val registration = binding.etRegistration.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val isRepeater = binding.switchRepeater.isChecked
            val isCR = binding.switchCR.isChecked

            var isValid = true

            // Validate Name
            if (name.isEmpty()) {
                binding.etName.error = "Enter student name"
                isValid = false
            } else binding.etName.error = null

            // Validate Contact
            if (!contact.matches(contactPattern)) {
                binding.etContact.error = "Invalid contact number"
                isValid = false
            } else binding.etContact.error = null

            // Validate Registration
            if (!registration.matches(registrationPattern)) {
                binding.etRegistration.error = "Invalid format: AAAA000000000"
                isValid = false
            } else binding.etRegistration.error = null

            // Validate Email
            if (!email.matches(emailPattern)) {
                binding.etEmail.error = "Invalid email"
                isValid = false
            } else binding.etEmail.error = null

            // Validate Password only in add mode
            if (!isEditMode && password.isEmpty()) {
                binding.etPassword.error = "Enter password"
                isValid = false
            } else binding.etPassword.error = null

            if (!isValid) return@setOnClickListener

            val student = Student(
                studentId = if (isEditMode) studentId else 0,
                name = name,
                contactNumber = contact,
                registrationNumber = registration,
                email = email,
                password = password,
                isRepeater = isRepeater,
                isCR = isCR
            )

            if (isEditMode) {
                viewModel.updateStudent(student) {
                    Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                viewModel.addStudent(
                    student.name,
                    student.contactNumber,
                    student.registrationNumber,
                    student.email,
                    student.password,
                    student.isRepeater,
                    student.isCR
                ) {
                    Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun loadStudentData() {
        viewModel.getStudentById(studentId) { student ->
            student ?: return@getStudentById

            binding.etName.setText(student.name)
            binding.etContact.setText(student.contactNumber)
            binding.etRegistration.setText(student.registrationNumber)
            binding.etEmail.setText(student.email)
            binding.switchCR.isChecked = student.isCR
            binding.switchRepeater.isChecked = student.isRepeater

            // Hide password field in edit mode
            binding.tilPassword.visibility = View.GONE

            // Make registration number non-editable
            binding.etRegistration.isEnabled = false
        }
    }
}
