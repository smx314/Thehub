package com.example.unihub.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unihub.ThehubApplication
import com.example.unihub.databinding.FragmentTodayBinding
import com.example.unihub.ui.adapter.ScheduleAdapter
import com.example.unihub.viewmodel.ScheduleViewModel
import com.example.unihub.viewmodel.ScheduleViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.unihub.databinding.DialogAddScheduleBinding
import android.widget.Toast
import com.example.unihub.data.model.Schedule
import com.example.unihub.util.UserManager

import java.util.Calendar
import java.util.Date

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale
import java.text.SimpleDateFormat

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels({ requireParentFragment() }) {
        ScheduleViewModelFactory((requireActivity().application as ThehubApplication).scheduleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        
        setupRecyclerViews()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerViews() {
        binding.rvSchedules.layoutManager = LinearLayoutManager(context)
        binding.rvSchedules.adapter = ScheduleAdapter { schedule ->
            showEditDialog(schedule)
        }

        binding.rvCountdowns.layoutManager = LinearLayoutManager(context)
        binding.rvCountdowns.adapter = ScheduleAdapter { schedule ->
            showEditDialog(schedule)
        }
    }

    private fun showEditDialog(schedule: Schedule) {
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

        // 修改保存按钮为“更新”
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
                    endTime = endCalendar.timeInMillis,
                    type = 0,
                    isCountdown = false
                )
                viewModel.insert(updatedSchedule) // Room handles update with REPLACE strategy
                dialog.dismiss()
                Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请输入标题", Toast.LENGTH_SHORT).show()
            }
        }

        // 修改取消按钮逻辑，或者增加一个删除按钮？
        // 现有的 dialog_add_schedule.xml 只有取消和保存。
        // 为了方便用户，我们可以把取消按钮改为删除按钮，或者在对话框中长按？
        // 既然用户要求“修改或删除”，我可以在编辑对话框加一个删除逻辑。
        
        dialogBinding.btnCancel.text = "删除"
        dialogBinding.btnCancel.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        dialogBinding.btnCancel.setOnClickListener {
            viewModel.delete(schedule)
            dialog.dismiss()
            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.allSchedules.observe(viewLifecycleOwner) { schedules ->
            if (schedules == null || _binding == null) return@observe
            
            val now = Calendar.getInstance()
            
            // 计算今天的开始和结束时间
            val todayStart = now.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val todayEnd = now.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            // 计算明天的开始和结束时间
            val tomorrowStart = todayEnd + 1
            val tomorrowEnd = tomorrowStart + (24 * 60 * 60 * 1000) - 1

            // 过滤今天的日程
            val dailySchedules = schedules.filter { 
                it.startTime in todayStart..todayEnd 
            }
            (binding.rvSchedules.adapter as ScheduleAdapter).submitList(dailySchedules)
            binding.tvEmptySchedules.visibility = if (dailySchedules.isEmpty()) View.VISIBLE else View.GONE

            // 过滤明天的日程
            val tomorrowSchedules = schedules.filter {
                it.startTime in tomorrowStart..tomorrowEnd
            }
            (binding.rvCountdowns.adapter as ScheduleAdapter).submitList(tomorrowSchedules)
            binding.tvEmptyCountdowns.visibility = if (tomorrowSchedules.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
