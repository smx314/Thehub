package com.example.unihub.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.R
import com.example.unihub.ThehubApplication
import com.example.unihub.data.model.Schedule
import com.example.unihub.util.UserManager
import com.example.unihub.databinding.DialogAddScheduleBinding
import com.example.unihub.databinding.FragmentWeekBinding
import com.example.unihub.viewmodel.ScheduleViewModel
import com.example.unihub.viewmodel.ScheduleViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar
import java.util.Locale

import com.example.unihub.databinding.DialogDaySchedulesBinding
import com.example.unihub.ui.adapter.ScheduleAdapter

class WeekFragment : Fragment() {

    private var _binding: FragmentWeekBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels({ requireParentFragment() }) {
        ScheduleViewModelFactory((requireActivity().application as ThehubApplication).scheduleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeekBinding.inflate(inflater, container, false)
        
        binding.rvWeekDays.layoutManager = LinearLayoutManager(context)
        binding.rvWeekDays.adapter = WeekAdapter(getWeekDays())
        
        return binding.root
    }

    private fun getWeekDays(): List<Triple<String, String, Long>> {
        val days = mutableListOf<Triple<String, String, Long>>()
        val calendar = Calendar.getInstance()
        // 设置到本周一
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        
        val dayFormat = SimpleDateFormat("EEEE", Locale.CHINESE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 0 until 7) {
            days.add(Triple(dayFormat.format(calendar.time), dateFormat.format(calendar.time), calendar.timeInMillis))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }

    private fun showDaySchedulesDialog(dayName: String, dateStr: String, timestamp: Long) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogDaySchedulesBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvDialogTitle.text = "$dayName ($dateStr) 的计划"

        val adapter = ScheduleAdapter { schedule ->
            showEditScheduleDialog(schedule)
        }
        dialogBinding.rvDaySchedules.layoutManager = LinearLayoutManager(context)
        dialogBinding.rvDaySchedules.adapter = adapter

        // 计算该天的范围
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        viewModel.allSchedules.observe(viewLifecycleOwner) { schedules ->
            if (_binding == null || schedules == null) return@observe
            val dailySchedules = schedules.filter { 
                it.startTime in startOfDay..endOfDay 
            }
            adapter.submitList(dailySchedules)
            dialogBinding.tvEmptyState.visibility = if (dailySchedules.isEmpty()) View.VISIBLE else View.GONE
        }

        dialogBinding.btnAddPlan.setOnClickListener {
            showAddScheduleDialog(dayName, dateStr, timestamp)
        }

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddScheduleDialog(dayName: String, dateStr: String, timestamp: Long) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val startCalendar = calendar.clone() as Calendar
        startCalendar.set(Calendar.HOUR_OF_DAY, 8)
        startCalendar.set(Calendar.MINUTE, 0)
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.set(Calendar.HOUR_OF_DAY, 9)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        dialogBinding.tvDialogTitle.text = "添加 $dayName 的计划"
        dialogBinding.tvStartTime.text = timeFormat.format(startCalendar.time)
        dialogBinding.tvEndTime.text = timeFormat.format(endCalendar.time)

        dialogBinding.llStartTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(startCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(startCalendar.get(Calendar.MINUTE))
                .setTitleText("选择开始时间")
                .build()

            picker.addOnPositiveButtonClickListener {
                startCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                startCalendar.set(Calendar.MINUTE, picker.minute)
                dialogBinding.tvStartTime.text = timeFormat.format(startCalendar.time)
                
                if (endCalendar.before(startCalendar)) {
                    endCalendar.timeInMillis = startCalendar.timeInMillis
                    endCalendar.add(Calendar.HOUR_OF_DAY, 1)
                    dialogBinding.tvEndTime.text = timeFormat.format(endCalendar.time)
                }
            }
            picker.show(childFragmentManager, "START_TIME_PICKER")
        }

        dialogBinding.llEndTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(endCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(endCalendar.get(Calendar.MINUTE))
                .setTitleText("选择结束时间")
                .build()

            picker.addOnPositiveButtonClickListener {
                endCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                endCalendar.set(Calendar.MINUTE, picker.minute)
                dialogBinding.tvEndTime.text = timeFormat.format(endCalendar.time)
            }
            picker.show(childFragmentManager, "END_TIME_PICKER")
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString()
            val location = dialogBinding.etLocation.text.toString()

            if (title.isNotEmpty()) {
                val userId = UserManager.getUserId(requireContext())
                val schedule = Schedule(
                    userId = userId,
                    title = title,
                    location = location,
                    startTime = startCalendar.timeInMillis,
                    endTime = endCalendar.timeInMillis,
                    type = 0,
                    isCountdown = false
                )
                viewModel.insert(schedule)
                dialog.dismiss()
                Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请输入标题", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditScheduleDialog(schedule: Schedule) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val startCalendar = Calendar.getInstance().apply { timeInMillis = schedule.startTime }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = schedule.endTime }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        dialogBinding.tvDialogTitle.text = "编辑计划"
        dialogBinding.etTitle.setText(schedule.title)
        dialogBinding.etLocation.setText(schedule.location)
        dialogBinding.tvStartTime.text = timeFormat.format(startCalendar.time)
        dialogBinding.tvEndTime.text = timeFormat.format(endCalendar.time)

        dialogBinding.llStartTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(startCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(startCalendar.get(Calendar.MINUTE))
                .setTitleText("选择开始时间")
                .build()

            picker.addOnPositiveButtonClickListener {
                startCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                startCalendar.set(Calendar.MINUTE, picker.minute)
                dialogBinding.tvStartTime.text = timeFormat.format(startCalendar.time)
            }
            picker.show(childFragmentManager, "START_TIME_PICKER")
        }

        dialogBinding.llEndTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(endCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(endCalendar.get(Calendar.MINUTE))
                .setTitleText("选择结束时间")
                .build()

            picker.addOnPositiveButtonClickListener {
                endCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                endCalendar.set(Calendar.MINUTE, picker.minute)
                dialogBinding.tvEndTime.text = timeFormat.format(endCalendar.time)
            }
            picker.show(childFragmentManager, "END_TIME_PICKER")
        }

        dialogBinding.btnSave.text = "更新"
        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString()
            val location = dialogBinding.etLocation.text.toString()

            if (title.isNotEmpty()) {
                val userId = UserManager.getUserId(requireContext())
                val updatedSchedule = schedule.copy(
                    userId = userId,
                    title = title,
                    location = location,
                    startTime = startCalendar.timeInMillis,
                    endTime = endCalendar.timeInMillis
                )
                viewModel.insert(updatedSchedule)
                dialog.dismiss()
                Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请输入标题", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnCancel.text = "删除"
        dialogBinding.btnCancel.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        dialogBinding.btnCancel.setOnClickListener {
            viewModel.delete(schedule)
            dialog.dismiss()
            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    inner class WeekAdapter(private val days: List<Triple<String, String, Long>>) : 
        RecyclerView.Adapter<WeekAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (day, date, timestamp) = days[position]
            holder.text1.text = day
            holder.text1.setTextColor(resources.getColor(R.color.primary_color))
            holder.text2.text = date
            holder.text2.setTextColor(resources.getColor(R.color.text_secondary))
            
            holder.itemView.setOnClickListener {
                showDaySchedulesDialog(day, date, timestamp)
            }
        }


        override fun getItemCount() = days.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text1: TextView = view.findViewById(android.R.id.text1)
            val text2: TextView = view.findViewById(android.R.id.text2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
