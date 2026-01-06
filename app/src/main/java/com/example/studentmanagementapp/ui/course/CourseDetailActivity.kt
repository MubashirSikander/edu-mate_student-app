package com.example.studentmanagementapp.ui.course

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.StudentAdapter
import com.example.studentmanagementapp.adapter.StudentSelectAdapter
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.databinding.ActivityCourseDetailBinding
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private var latestEnrollments: List<Enrollment> = emptyList()

    private var enrolledStudentIds: Set<Long> = emptySet()
    private val courseViewModel: CourseViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter
    private var courseId: Long = -1
    private var allStudents: List<Student> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getLongExtra("courseId", -1)
        if (courseId == -1L) {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        studentAdapter = StudentAdapter(
            students = emptyList(),
            onEditClick = null, // No edit in course detail
            onDeleteClick = { student ->
                courseViewModel.unenrollStudent(student.studentId, courseId)
                Toast.makeText(this, "${student.name} removed from course", Toast.LENGTH_SHORT).show()
            }
        )


        binding.rvEnrolledStudents.layoutManager = LinearLayoutManager(this)
        binding.rvEnrolledStudents.adapter = studentAdapter

        binding.btnEnroll.setOnClickListener { showEnrollBottomSheet() }
//        binding.btnViewAttendance.setOnClickListener {
//            val intent = Intent(this, CourseAttendanceListActivity::class.java)
//            intent.putExtra("courseId", courseId)
//            startActivity(intent)
//        }

        // Load course details
        lifecycleScope.launch {
            courseViewModel.getCourse(courseId)?.let { course ->
                binding.tvCourseTitle.text = "${course.courseName} (${course.courseCode})"
                binding.tvInstructor.text = "Instructor: ${course.instructorName}"
                binding.tvSemester.text = "Semester ${course.semesterNumber}"
            }
        }

        // Observe students
        studentViewModel.students.observe(this) { students ->
            allStudents = students
            refreshEnrolledStudents()
        }

        courseViewModel.getEnrollments(courseId).observe(this) { enrollments ->
            latestEnrollments = enrollments
            refreshEnrolledStudents()
        }

    }

    private fun refreshEnrolledStudents() {
        binding.progressBar.visibility = View.GONE

        if (allStudents.isEmpty() || latestEnrollments.isEmpty()) {
            studentAdapter.updateData(emptyList())
            binding.tvEmptyEnrolled.visibility = View.VISIBLE
            return
        }

        enrolledStudentIds = latestEnrollments.map { it.studentOwnerId }.toSet()

        val studentList = latestEnrollments.mapNotNull { enrollment ->
            allStudents.find { it.studentId == enrollment.studentOwnerId }
        }

        studentAdapter.updateData(studentList)
        binding.tvEmptyEnrolled.visibility =
            if (studentList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEnrollBottomSheet() {
        val notEnrolledStudents = allStudents.filter { it.studentId !in enrolledStudentIds }

        if (notEnrolledStudents.isEmpty()) {
            Toast.makeText(this, "All students are already enrolled", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_enroll_students, null)
        dialog.setContentView(view)

        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvStudents)
        val btn = view.findViewById<MaterialButton>(R.id.btnConfirmEnroll)

        val adapter = StudentSelectAdapter(notEnrolledStudents)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btn.setOnClickListener {
            val selected = adapter.getSelectedStudents()
            if (selected.isEmpty()) {
                Toast.makeText(this, "Select at least one student", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selected.forEach { student ->
                courseViewModel.enrollStudent(student.studentId, courseId)
            }

            Toast.makeText(this, "Students enrolled successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEnrolledStudentsWithLoader(enrollments: List<Enrollment>) {
        binding.progressBar.visibility = View.GONE
        enrolledStudentIds = enrollments.map { it.studentOwnerId }.toSet()
        val studentList = enrollments.mapNotNull { enrollment ->
            allStudents.find { it.studentId == enrollment.studentOwnerId }
        }
        studentAdapter.updateData(studentList)
        binding.tvEmptyEnrolled.visibility = if (studentList.isEmpty()) View.VISIBLE else View.GONE


    }
}
