package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.databinding.ItemCourseBinding

class CourseAdapter(
    private var courses: List<Course>,
    private val onCourseClick: (Course) -> Unit,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.tvCourseName.text = course.courseName
            binding.tvCourseCode.text = course.courseCode
            binding.tvInstructor.text = "Instructor: ${course.instructorName}"

            // Full card click
            binding.root.setOnClickListener { onCourseClick(course) }

            // Edit click
            binding.ivEdit.setOnClickListener { onEditClick(course) }

            // Delete click
            binding.ivDelete.setOnClickListener { onDeleteClick(course) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    fun updateData(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}
