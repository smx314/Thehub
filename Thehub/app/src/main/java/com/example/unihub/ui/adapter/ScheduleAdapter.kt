package com.example.unihub.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.R
import com.example.unihub.data.model.Schedule
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAdapter(private val onItemClick: (Schedule) -> Unit) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class ScheduleViewHolder(itemView: View, private val onItemClick: (Schedule) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_item_location)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_item_time)
        private var currentSchedule: Schedule? = null

        init {
            itemView.setOnClickListener {
                currentSchedule?.let { onItemClick(it) }
            }
        }

        fun bind(schedule: Schedule) {
            currentSchedule = schedule
            tvTitle.text = schedule.title
            tvLocation.text = schedule.location
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(schedule.startTime))
        }
    }

    class ScheduleComparator : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }
}
