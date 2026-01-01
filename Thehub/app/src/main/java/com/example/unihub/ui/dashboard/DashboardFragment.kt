package com.example.unihub.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.unihub.ThehubApplication
import com.example.unihub.databinding.FragmentDashboardBinding
import com.example.unihub.util.UserManager
import com.example.unihub.viewmodel.ScheduleViewModel
import com.example.unihub.viewmodel.ScheduleViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.unihub.databinding.DialogAddScheduleBinding
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import com.example.unihub.data.model.Schedule

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: ScheduleViewModel by viewModels {
        ScheduleViewModelFactory((requireActivity().application as ThehubApplication).scheduleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        dashboardViewModel.setUserId(UserManager.getUserId(requireContext()))
        
        setupViewPager()
        
        dashboardViewModel.selectedYear.observe(viewLifecycleOwner) { year ->
            if (_binding != null) {
                val tab = binding.tabLayout.getTabAt(2)
                tab?.text = year.toString()
            }
        }
        
        binding.fabAdd.setOnClickListener {
            showAddScheduleDialog()
        }

        return binding.root
    }

    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        val tabTitles = listOf("今天", "我的一周", dashboardViewModel.selectedYear.value?.toString() ?: "2025")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun showAddScheduleDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val calendar = Calendar.getInstance()
        val startCalendar = calendar.clone() as Calendar
        startCalendar.set(Calendar.MINUTE, 0)
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.HOUR_OF_DAY, 1)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
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
                dashboardViewModel.insert(schedule)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class DashboardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TodayFragment()
                1 -> WeekFragment()
                2 -> YearFragment()
                else -> TodayFragment()
            }
        }
    }
}
