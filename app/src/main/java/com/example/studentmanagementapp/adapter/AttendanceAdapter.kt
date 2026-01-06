package com.example.studentmanagementapp.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.data.entity.Student

data class AttendanceItem(val student: Student, var isPresent: Boolean)
class AttendanceAdapter(
    private var items: List<AttendanceItem>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private var isEditable = true // Editable by default

    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card = itemView.findViewById<CardView>(R.id.item_student_card)
        val container = itemView.findViewById<LinearLayout>(R.id.outer_linear_layout)
        val name = itemView.findViewById<TextView>(R.id.tvStudentName)
        val registration = itemView.findViewById<TextView>(R.id.tvRegistration)
        val imgRepeater = itemView.findViewById<ImageView>(R.id.imgRepeater)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_student, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.student.name
        holder.registration.text = item.student.registrationNumber
        holder.imgRepeater.visibility = if (item.student.isRepeater) View.VISIBLE else View.GONE

        // Assign background drawable only once
        if (holder.container.background !is GradientDrawable) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.student_card_border)!!.mutate() as GradientDrawable
            holder.container.background = drawable
        }

        // Set color based on current attendance state
        updateBackground(holder.container, context, item.isPresent)

        // Click listener to toggle attendance
        holder.card.setOnClickListener {
            if (!isEditable) return@setOnClickListener

            // Toggle attendance
            item.isPresent = !item.isPresent
            updateBackground(holder.container, context, item.isPresent)

            // Show toast message
            val message = if (item.isPresent) {
                "${item.student.name} marked Present"
            } else {
                "${item.student.name} marked Absent"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBackground(container: LinearLayout, context: Context, isSelected: Boolean) {
        val drawable = container.background as? GradientDrawable ?: return
        if (isSelected) {
            drawable.setColor(ContextCompat.getColor(context, R.color.primaryColor))
            drawable.setStroke(2, ContextCompat.getColor(context, R.color.primaryColor))
        } else {
            drawable.setColor(ContextCompat.getColor(context, android.R.color.white))
            drawable.setStroke(2, ContextCompat.getColor(context, R.color.golden_dark_orange))
        }
    }

    fun setEditable(editable: Boolean) {
        isEditable = editable
        notifyDataSetChanged()
    }

    fun getAttendance(): List<AttendanceItem> = items

    fun updateData(newItems: List<AttendanceItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
