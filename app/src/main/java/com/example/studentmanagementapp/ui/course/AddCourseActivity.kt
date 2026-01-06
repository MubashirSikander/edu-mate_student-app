package com.example.studentmanagementapp.ui.course

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.databinding.ActivityAddCourseBinding
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import androidx.lifecycle.observe


class AddCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCourseBinding
    private val viewModel: CourseViewModel by viewModels()
    private val COURSE_CODE_REGEX = Regex("^[A-Z]{4}-\\d{4}$")
    private val NAME_REGEX = Regex("^[A-Za-z ]+$") // letters and spaces only
    private var editingCourseId: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Course Code Validation (keep as-is)
        binding.etCourseCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val upper = text.uppercase()

                if (text != upper) {
                    binding.etCourseCode.setText(upper)
                    binding.etCourseCode.setSelection(upper.length)
                    return
                }

                binding.etCourseCode.error =
                    if (upper.isNotEmpty() && !COURSE_CODE_REGEX.matches(upper)) {
                        "Format must be AAAA-0000"
                    } else null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
// Check if editing
        editingCourseId = intent.getLongExtra("courseId", -1).takeIf { it != -1L }

        if (editingCourseId != null) {
            title = "Edit Course"
            // 🔒 Disable course code editing
            binding.etCourseCode.isEnabled = false
            binding.etCourseCode.isFocusable = false
            binding.etCourseCode.isFocusableInTouchMode = false
            viewModel.loadCourse(editingCourseId!!)

            viewModel.selectedCourse.observe(this) { course ->
                course?.let {
                    binding.etCourseName.setText(it.courseName)
                    binding.etCourseCode.setText(it.courseCode)
                    binding.etCreditHours.setText(it.creditHours.toString())
                    binding.etInstructor.setText(it.instructorName)
                    binding.etSemester.setText(it.semesterNumber.toString())
                }
            }
        }

        binding.btnSaveCourse.setOnClickListener {

            val name = binding.etCourseName.text.toString().trim()
            val code = binding.etCourseCode.text.toString().trim()
            val creditHours = binding.etCreditHours.text.toString().toIntOrNull() ?: 0
            val instructor = binding.etInstructor.text.toString().trim()
            val semester = binding.etSemester.text.toString().toIntOrNull() ?: 0

            var isValid = true

            // Validate Course Name
            if (name.isEmpty() || !NAME_REGEX.matches(name)) {
                binding.etCourseName.error = "Enter valid course name (letters only)"
                isValid = false
            } else {
                binding.etCourseName.error = null
            }

            // Validate Credit Hours
            if (creditHours <= 0) {
                binding.etCreditHours.error = "Credit hours must be greater than 0"
                isValid = false
            } else {
                binding.etCreditHours.error = null
            }

            // Validate Instructor Name
            if (instructor.isEmpty() || !NAME_REGEX.matches(instructor)) {
                binding.etInstructor.error = "Enter valid instructor name (letters only)"
                isValid = false
            } else {
                binding.etInstructor.error = null
            }

            // Validate Semester
            if (semester <= 0 || semester >= 9) {
                binding.etSemester.error = "Enter valid semester"
                isValid = false
            } else {
                binding.etSemester.error = null
            }

            // Validate Course Code ONLY when adding new course
            if (!code.matches(COURSE_CODE_REGEX)) {
                binding.etCourseCode.error = "Invalid Format! Use: AAAA-0000"
                isValid = false
            } else {
                binding.etCourseCode.error = null
            }


            // If any validation failed, do not proceed
            if (!isValid) return@setOnClickListener

            // Add course
            if(editingCourseId == null) {
                viewModel.addCourse(name, code, creditHours, instructor, semester) { result ->
                    runOnUiThread {
                        when (result) {
                            "VALID_DETAILS" -> Toast.makeText(
                                this,
                                "Please enter valid course details",
                                Toast.LENGTH_SHORT
                            ).show()
                            "DUPLICATE_CODE" -> Toast.makeText(
                                this,
                                "Course code already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> {
                                Toast.makeText(
                                    this,
                                    "Course saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            } else {
                viewModel.updateCourse(editingCourseId!!, name, code, creditHours, instructor, semester) { result ->
                    runOnUiThread {
                        when (result) {
                            "VALID_DETAILS" -> Toast.makeText(
                                this,
                                "Please enter valid course details",
                                Toast.LENGTH_SHORT
                            ).show()
                            "DUPLICATE_CODE" -> Toast.makeText(
                                this,
                                "Course code already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> {
                                Toast.makeText(
                                    this,
                                    "Course saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }

    }
}
