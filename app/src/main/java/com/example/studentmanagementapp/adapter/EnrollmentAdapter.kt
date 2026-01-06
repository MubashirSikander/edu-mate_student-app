package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.data.entity.Student

class EnrollmentAdapter(private var students: List<Student>) :
    RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder>() {

    class EnrollmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvEnrollmentName)
        val reg: TextView = itemView.findViewById(R.id.tvEnrollmentReg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_enrollment, parent, false)
        return EnrollmentViewHolder(view)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        val student = students[position]
        holder.name.text = student.name
        holder.reg.text = student.registrationNumber
    }

    fun updateData(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }
}
