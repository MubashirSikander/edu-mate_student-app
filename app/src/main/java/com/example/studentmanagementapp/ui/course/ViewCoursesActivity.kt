package com.example.studentmanagementapp.ui.course

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.adapter.CourseAdapter
import com.example.studentmanagementapp.adapter.StudentAdapter
import com.example.studentmanagementapp.databinding.ActivityViewCoursesBinding
import com.example.studentmanagementapp.viewmodel.CourseViewModel

class ViewCoursesActivity : AppCompatActivity() {
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var binding: ActivityViewCoursesBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var adapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CourseAdapter(
            emptyList(),
            onCourseClick = { course ->
                val intent = Intent(this, CourseDetailActivity::class.java)
                intent.putExtra("courseId", course.courseId)
                startActivity(intent)
            },
            onEditClick = { course ->
                val intent = Intent(this, AddCourseActivity::class.java)
                intent.putExtra("courseId", course.courseId)
                startActivity(intent)
            },
            onDeleteClick = { course ->
                viewModel.deleteCourseByCode(course.courseCode)
            }
        )

        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = adapter

        // Initial state
        binding.progressBar.visibility = View.VISIBLE
        binding.rvCourses.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        // Observe data
        viewModel.courses.observe(this) { list ->
            binding.progressBar.visibility = View.GONE

            if (list.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvCourses.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvCourses.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }

        // Add new course
        binding.btnAddCourse.setOnClickListener {
            startActivity(Intent(this, AddCourseActivity::class.java))
        }
    }
}
