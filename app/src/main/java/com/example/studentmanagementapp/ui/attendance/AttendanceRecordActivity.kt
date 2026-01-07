package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.AttendanceDisplayItem
import com.example.studentmanagementapp.adapter.AttendanceRecordAdapter
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.databinding.ActivityAttendanceRecordBinding
import com.example.studentmanagementapp.utils.PdfUtils
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceRecordBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: AttendanceRecordAdapter
    private var courseId: Long = -1L
    private var sessionDate: Long = -1L
    private var course: Course? = null
    private var fallbackCourse: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1)
        sessionDate = intent.getLongExtra(EXTRA_SESSION_DATE, -1)
        val courseName = intent.getStringExtra(EXTRA_COURSE_NAME).orEmpty()
        val courseCode = intent.getStringExtra(EXTRA_COURSE_CODE).orEmpty()
        val instructorName = intent.getStringExtra(EXTRA_INSTRUCTOR).orEmpty()
        fallbackCourse = Course(
            courseId = courseId,
            courseName = courseName,
            courseCode = courseCode,
            instructorName = instructorName,
            creditHours = 0,
            semesterNumber = 0
        )

        if (courseId == -1L || sessionDate == -1L) {
            Toast.makeText(this, getString(R.string.select_course_prompt), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        adapter = AttendanceRecordAdapter(emptyList())
        binding.rvRecords.layoutManager = LinearLayoutManager(this)
        binding.rvRecords.adapter = adapter

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.attendance_session_title)

        binding.tvCourseName.text = if (courseName.isNotBlank()) {
            "$courseName ($courseCode)"
        } else {
            ""
        }
        binding.tvDate.text = formatSessionDate(sessionDate)
        supportActionBar?.subtitle = instructorName

        loadCourseAndAttendance()

//        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    private fun loadCourseAndAttendance() {
        lifecycleScope.launch {
            course = attendanceViewModel.getCourse(courseId) ?: fallbackCourse
            val records = attendanceViewModel.getAttendanceForDate(courseId, sessionDate)
            if (records.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                adapter.submitList(emptyList())
                return@launch
            }

            val students = attendanceViewModel.getStudentsByIds(records.map { it.studentOwnerId }.distinct())
            val studentMap = students.associateBy { it.studentId }
            val formatter = SimpleDateFormat("MMM dd, yyyy h:mma", Locale.getDefault())
            val items = records.map { attendance ->
                val student = studentMap[attendance.studentOwnerId]
                AttendanceDisplayItem(
                    id = attendance.attendanceId,
                    studentName = student?.name ?: getString(R.string.unknown_student),
                    registration = student?.registrationNumber ?: getString(R.string.not_available),
                    statusLabel = if (attendance.isPresent) getString(R.string.present) else getString(R.string.absent),
                    dateLabel = formatter.format(attendance.date),
                    isPresent = attendance.isPresent
                )
            }
            binding.tvEmptyState.visibility = android.view.View.GONE
            adapter.submitList(items)
        }
    }

    private fun exportPdf() {
        val localCourse = course ?: fallbackCourse ?: return
        lifecycleScope.launch {
            val attendanceRecords = attendanceViewModel.getAttendanceForDate(courseId, sessionDate)
            val students = attendanceViewModel.getStudentsByIds(attendanceRecords.map { it.studentOwnerId }.distinct())
            val file = PdfUtils.generateAttendanceSessionPdf(
                this@AttendanceRecordActivity,
                localCourse,
                sessionDate,
                students,
                attendanceRecords
            )
            if (file != null) {
                Toast.makeText(
                    this@AttendanceRecordActivity,
                    getString(R.string.pdf_saved_at, file.absolutePath),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun formatSessionDate(dateMs: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(dateMs)
    }

    companion object {
        const val EXTRA_COURSE_ID = "course_id"
        const val EXTRA_SESSION_DATE = "session_date"
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_COURSE_CODE = "course_code"
        const val EXTRA_INSTRUCTOR = "instructor_name"
    }
}
