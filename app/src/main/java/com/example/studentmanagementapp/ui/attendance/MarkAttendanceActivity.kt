package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.AttendanceAdapter
import com.example.studentmanagementapp.adapter.AttendanceItem
import com.example.studentmanagementapp.adapter.CourseSelectAdapter
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.repository.StudentRepository.AttendanceMarkPayload
import com.example.studentmanagementapp.databinding.ActivityMarkAttendanceBinding
import com.example.studentmanagementapp.utils.PdfUtils
import com.example.studentmanagementapp.utils.NetworkUtils
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAttendanceBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private var selectedCourse: Course? = null
    private var adapter = AttendanceAdapter(emptyList())
    private var enrollmentsLiveData: LiveData<List<Enrollment>>? = null
    private lateinit var toolbar: Toolbar
    private var attendanceTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // optional back button

        binding.root.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        // Observe courses
        courseViewModel.courses.observe(this) { courses ->
            if (courses.isNotEmpty() && selectedCourse == null) {
                showCourseSelectorBottomSheet(courses)
            }
        }

        // Observe students
        studentViewModel.students.observe(this) {
            if (selectedCourse != null) loadEnrollments()
        }

        binding.btnSaveAttendance.setOnClickListener { saveAttendance() }
        binding.btnGeneratePdf.setOnClickListener { generatePdf() }
        binding.btnChangeCourse.setOnClickListener {
            courseViewModel.courses.value?.let { showCourseSelectorBottomSheet(it) }
        }
    }

    private fun showCourseSelectorBottomSheet(courses: List<Course>) {
        val dialog = BottomSheetDialog(this, R.style.FullScreenBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_select_course, null)
        view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        dialog.setContentView(view)

        val rvCourses = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(this)

        val courseAdapter = CourseSelectAdapter(courses) { course ->
            selectedCourse = course
            binding.tvSelectedCourse.text = "${course.courseName} (${course.courseCode})"
            adapter.updateData(emptyList())

            // Update toolbar title immediately
            updateToolbarTitle(course.courseName)

            // Set initial subtitle as current date & time (attendance marking start time)
            attendanceTimestamp = System.currentTimeMillis()
            updateToolbarSubtitle(attendanceTimestamp)

            loadEnrollments()
            dialog.dismiss()
        }

        rvCourses.adapter = courseAdapter
        dialog.show()
    }

    private fun loadEnrollments() {
        val courseId = selectedCourse?.courseId ?: return
        enrollmentsLiveData?.removeObservers(this)
        enrollmentsLiveData = attendanceViewModel.getEnrollments(courseId)
        enrollmentsLiveData?.observe(this) { enrollments ->
            attendanceViewModel.loadEnrolledStudents(enrollments) { students ->
                val items = enrollments.mapNotNull { enrollment ->
                    students.find { it.studentId == enrollment.studentOwnerId }
                }.map { AttendanceItem(it, false) }

                adapter.updateData(items)

                if (items.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_enrolled_students), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAttendance() {
        val courseId = selectedCourse?.courseId
        if (courseId == null) {
            Toast.makeText(this, getString(R.string.select_course_prompt), Toast.LENGTH_SHORT).show()
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.network_required), Toast.LENGTH_SHORT).show()
            return
        }
        val attendanceItems = adapter.getAttendance()
        if (attendanceItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_students_to_mark), Toast.LENGTH_SHORT).show()
            return
        }

        attendanceTimestamp = System.currentTimeMillis() // save exact save time
        binding.btnSaveAttendance.isEnabled = false

        lifecycleScope.launch {
            val payloads = attendanceItems.map {
                AttendanceMarkPayload(studentId = it.student.studentId, isPresent = it.isPresent)
            }

            val result = attendanceViewModel.saveAttendance(courseId, payloads, attendanceTimestamp)

            binding.btnSaveAttendance.isEnabled = true

            if (result.failures.isEmpty()) {
                updateToolbarSubtitle(attendanceTimestamp)
                Toast.makeText(
                    this@MarkAttendanceActivity,
                    getString(R.string.attendance_saved_success, result.successCount),
                    Toast.LENGTH_SHORT
                ).show()
                resetSelections()
                com.example.studentmanagementapp.utils.AdManager.showInterstitial(this@MarkAttendanceActivity) {
                    finish()
                }
            } else {
                if (result.successCount > 0) {
                    updateToolbarSubtitle(attendanceTimestamp)
                }
                Toast.makeText(
                    this@MarkAttendanceActivity,
                    getString(R.string.attendance_saved_partial, result.successCount, result.failures.size),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun updateToolbarSubtitle(timestamp: Long) {
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mma", Locale.getDefault())
        supportActionBar?.subtitle = sdf.format(Date(timestamp))
    }

    private fun generatePdf() {
        val course = selectedCourse ?: return
        lifecycleScope.launch {
            val students = studentViewModel.students.value ?: emptyList()
            val attendanceRecords = attendanceViewModel.getAttendanceSnapshot(course.courseId)
            val file = PdfUtils.generateAttendancePdf(this@MarkAttendanceActivity, course, students, attendanceRecords)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun resetSelections() {
        val cleared = adapter.getAttendance().map { AttendanceItem(it.student, false) }
        adapter.updateData(cleared)
        supportActionBar?.subtitle = SimpleDateFormat("MMM dd, yyyy h:mma", Locale.getDefault()).format(Date())
    }
}
