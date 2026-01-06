package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.google.android.material.card.MaterialCardView

data class AttendanceDisplayItem(
    val id: Long,
    val studentName: String,
    val registration: String,
    val statusLabel: String,
    val dateLabel: String,
    val isPresent: Boolean
)

class AttendanceRecordAdapter(
    private var items: List<AttendanceDisplayItem>
) : RecyclerView.Adapter<AttendanceRecordAdapter.AttendanceRecordViewHolder>() {

    class AttendanceRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardAttendance)
        val name: TextView = itemView.findViewById(R.id.tvStudentName)
        val registration: TextView = itemView.findViewById(R.id.tvRegistration)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val date: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return AttendanceRecordViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AttendanceRecordViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.studentName
        holder.registration.text = item.registration
        holder.status.text = item.statusLabel
        holder.date.text = item.dateLabel

        val statusColor = if (item.isPresent) {
            ContextCompat.getColor(context, R.color.green_500)
        } else {
            ContextCompat.getColor(context, R.color.red_400)
        }
        holder.status.setTextColor(statusColor)
        holder.card.strokeColor = statusColor
    }

    fun submitList(newItems: List<AttendanceDisplayItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
