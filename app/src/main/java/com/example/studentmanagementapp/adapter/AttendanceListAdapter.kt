package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.data.entity.Attendance
import java.text.SimpleDateFormat
import java.util.*

class AttendanceListAdapter(
    private var items: List<Attendance>,
    private val onItemClick: (Attendance) -> Unit
) : RecyclerView.Adapter<AttendanceListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvAttendanceTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mma", Locale.getDefault())
        holder.title.text = sdf.format(record.date)

        holder.itemView.setOnClickListener { onItemClick(record) }
    }

    fun updateData(newItems: List<Attendance>) {
        items = newItems
        notifyDataSetChanged()
    }
}
