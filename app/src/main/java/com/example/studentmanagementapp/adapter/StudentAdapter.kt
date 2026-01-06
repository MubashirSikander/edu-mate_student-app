package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.databinding.ItemStudentBinding
class StudentAdapter(
    var students: List<Student>,
    private val onEditClick: ((Student) -> Unit)? = null,
    private val onDeleteClick: (Student) -> Unit   // Add delete callback
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        holder.binding.tvStudentName.text = student.name
        holder.binding.tvRegistration.text = student.registrationNumber
        holder.binding.imgRepeater.visibility =
            if (student.isRepeater) View.VISIBLE else View.GONE

        holder.binding.btnEdit.visibility =
            if (onEditClick != null) View.VISIBLE else View.GONE

        holder.binding.btnEdit.setOnClickListener {
            onEditClick?.invoke(student)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(student)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(student)
        }
    }
    fun updateData(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }
}
