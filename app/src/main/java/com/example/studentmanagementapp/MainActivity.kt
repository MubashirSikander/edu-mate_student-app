package com.example.studentmanagementapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagementapp.databinding.ActivityMainBinding
import com.example.studentmanagementapp.ui.attendance.MarkAttendanceActivity
import com.example.studentmanagementapp.ui.attendance.ViewAttendanceActivity
import com.example.studentmanagementapp.ui.course.AddCourseActivity
import com.example.studentmanagementapp.ui.course.ViewCoursesActivity
import com.example.studentmanagementapp.ui.student.AddStudentActivity
import com.example.studentmanagementapp.ui.student.ViewStudentsActivity
import com.example.studentmanagementapp.utils.DialogUtils
import com.example.studentmanagementapp.utils.NetworkUtils
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val studentViewModel: StudentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NetworkUtils.observeNetwork(this) { isOnline ->
            runOnUiThread {
                if (isOnline) {
                    binding.chipNetwork.text = "Online"
                    binding.chipNetwork.chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                } else {
                    binding.chipNetwork.text = "Offline"
                    binding.chipNetwork.chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor("#F44336"))
                }
            }
        }

        binding.btnAddStudent.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        binding.btnAddCourse.setOnClickListener {
            startActivity(Intent(this, AddCourseActivity::class.java))
        }

        binding.btnViewStudents.setOnClickListener {
            startActivity(Intent(this, ViewStudentsActivity::class.java))
        }

        binding.btnViewCourses.setOnClickListener {
            startActivity(Intent(this, ViewCoursesActivity::class.java))
        }

        binding.btnMarkAttendance.setOnClickListener {
            startActivity(Intent(this, MarkAttendanceActivity::class.java))
        }

        binding.btnViewAttendance.setOnClickListener {
            startActivity(Intent(this, ViewAttendanceActivity::class.java))
        }

        binding.btnSyncData.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                DialogUtils.showNoInternetDialog(this)
                return@setOnClickListener
            }
            lifecycleScope.launch {
                binding.btnSyncData.isEnabled = false
                studentViewModel.syncNow { success ->
                    val message =
                        if (success) getString(R.string.sync_success) else getString(R.string.sync_failed)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    binding.btnSyncData.isEnabled = true
                }
            }
        }
    }
}
