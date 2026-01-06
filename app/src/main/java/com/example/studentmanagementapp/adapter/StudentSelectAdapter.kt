package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.data.entity.Student

class StudentSelectAdapter(
    private val students: List<Student>
) : RecyclerView.Adapter<StudentSelectAdapter.VH>() {

    private val selectedStudents = mutableSetOf<Student>()

    fun getSelectedStudents(): List<Student> = selectedStudents.toList()

    inner class VH(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val cb = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_checkbox, parent, false) as CheckBox
        return VH(cb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val student = students[position]
        holder.checkBox.text = student.name

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedStudents.contains(student)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedStudents.add(student)
            else selectedStudents.remove(student)
        }
    }

    override fun getItemCount() = students.size
}
