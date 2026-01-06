package com.example.studentmanagementapp.ui.student

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.adapter.StudentAdapter
import com.example.studentmanagementapp.databinding.ActivityViewStudentsBinding
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.google.android.material.snackbar.Snackbar
import android.animation.ObjectAnimator

class ViewStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewStudentsBinding
    private val viewModel: StudentViewModel by viewModels()

    private val adapter by lazy {
        StudentAdapter(
            emptyList(),
            onEditClick = { student ->
                val intent = Intent(this, AddStudentActivity::class.java)
                intent.putExtra("MODE", "EDIT")
                intent.putExtra("STUDENT_ID", student.studentId)
                startActivity(intent)
            },
            onDeleteClick = { student ->
                viewModel.deleteStudent(student.registrationNumber) {
                    Snackbar.make(binding.root, "Student deleted", Snackbar.LENGTH_SHORT).show()
                }
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
        binding.rvStudents.setHasFixedSize(true)

        // Initially hide RecyclerView & Empty view
        binding.rvStudents.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        // Animate ProgressBar from 0 to 100 over 1 second
        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = 1000 // 1 second
        animator.start()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.progressBar.visibility = View.GONE
                binding.rvStudents.visibility = View.VISIBLE

                // Observe students after loading
                viewModel.students.observe(this@ViewStudentsActivity) { list ->
                    adapter.updateData(list)
                    binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvStudents.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        })

        // Add button
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }
    }
}
