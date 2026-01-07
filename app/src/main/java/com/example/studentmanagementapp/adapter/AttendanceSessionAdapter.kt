package com.example.studentmanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.google.android.material.card.MaterialCardView

data class AttendanceSessionItem(
    val sessionDateMs: Long,
    val title: String,
    val subtitle: String
)

class AttendanceSessionAdapter(
    private var items: List<AttendanceSessionItem>,
    private val onClick: (AttendanceSessionItem) -> Unit
) : RecyclerView.Adapter<AttendanceSessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardSession)
        val title: TextView = itemView.findViewById(R.id.tvSessionTitle)
        val subtitle: TextView = itemView.findViewById(R.id.tvSessionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.card.setOnClickListener { onClick(item) }
    }

    fun submitList(newItems: List<AttendanceSessionItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
