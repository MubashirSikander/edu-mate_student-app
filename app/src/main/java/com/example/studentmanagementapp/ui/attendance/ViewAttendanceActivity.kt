package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.AttendanceDisplayItem
import com.example.studentmanagementapp.adapter.AttendanceRecordAdapter
import com.example.studentmanagementapp.adapter.CourseSelectAdapter
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.databinding.ActivityViewAttendanceBinding
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAttendanceBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private var selectedCourse: Course? = null
    private var attendanceLiveData: LiveData<List<com.example.studentmanagementapp.data.entity.Attendance>>? = null
    private lateinit var adapter: AttendanceRecordAdapter
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.view_attendance)

        adapter = AttendanceRecordAdapter(emptyList())
        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        courseViewModel.courses.observe(this) { courses ->
            if (courses.isNotEmpty() && selectedCourse == null) {
                showCourseSelectorBottomSheet(courses)
            }
        }

        studentViewModel.students.observe(this) {
            if (selectedCourse != null) observeAttendance()
        }

        binding.btnSelectCourse.setOnClickListener {
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

        val adapter = CourseSelectAdapter(courses) { course ->
            selectedCourse = course
            binding.tvSelectedCourse.text = "${course.courseName} (${course.courseCode})"
            supportActionBar?.subtitle = course.instructorName
            observeAttendance()
            dialog.dismiss()
        }

        rvCourses.adapter = adapter
        dialog.show()
    }

    private fun observeAttendance() {
        val courseId = selectedCourse?.courseId ?: return
        attendanceLiveData?.removeObservers(this)
        attendanceLiveData = attendanceViewModel.getAttendance(courseId)
        attendanceLiveData?.observe(this) { records ->
            if (records.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                adapter.submitList(emptyList())
                return@observe
            }

            binding.tvEmptyState.visibility = View.GONE
            lifecycleScope.launch {
                // Resolve student info for the attendance records so UI stays readable.
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
                adapter.submitList(items)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
