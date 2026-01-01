package com.example.unihub.ui.focus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.Pair as AndroidPair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.unihub.ThehubApplication
import com.example.unihub.databinding.FragmentFocusBinding
import com.example.unihub.util.UserManager
import com.example.unihub.viewmodel.FocusViewModel
import com.example.unihub.viewmodel.FocusViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusFragment : Fragment() {

    private var _binding: FragmentFocusBinding? = null
    private val binding get() = _binding!!

    private val focusViewModel: FocusViewModel by viewModels {
        FocusViewModelFactory((requireActivity().application as ThehubApplication).focusRepository)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var timeElapsedInMillis: Long = 0
    private var timerRunning: Boolean = false

    private var selectedStartDate: Long = 0
    private var selectedEndDate: Long = 0

    private val timerRunnable = object : Runnable {
        override fun run() {
            timeElapsedInMillis += 1000
            updateTimerText()
            updateProgressBar()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        focusViewModel.setUserId(UserManager.getUserId(requireContext()))

        binding.btnStartPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }

        binding.btnFinish.setOnClickListener {
            finishFocus()
        }

        binding.btnSelectRange.setOnClickListener {
            showDatePicker()
        }

        binding.btnExport.setOnClickListener {
            exportReport()
        }

        focusViewModel.totalCountToday.observe(viewLifecycleOwner) { count ->
            if (_binding != null) {
                binding.tvTotalCount.text = (count ?: 0).toString()
            }
        }

        focusViewModel.totalDurationToday.observe(viewLifecycleOwner) { duration ->
            if (_binding != null) {
                binding.tvTotalTime.text = (duration ?: 0).toString()
            }
        }

        focusViewModel.exportStats.observe(viewLifecycleOwner) { stats ->
            if (_binding != null && stats != null) {
                showExportDialog(stats.first, stats.second)
            }
        }

        updateTimerText()
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("选择导出日期范围")
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { range ->
            val startUtc = range.first
            val endUtc = range.second
            
            if (startUtc != null && endUtc != null) {
                // 将 UTC 时间戳转换为本地日期范围
                val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startUtc }
                val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endUtc }
                
                selectedStartDate = Calendar.getInstance().apply {
                    set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                selectedEndDate = Calendar.getInstance().apply {
                    set(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                binding.btnSelectRange.text = "${sdf.format(Date(selectedStartDate))} - ${sdf.format(Date(selectedEndDate))}"
            }
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun exportReport() {
        if (selectedStartDate == 0L || selectedEndDate == 0L) {
            Toast.makeText(context, "请先选择日期范围", Toast.LENGTH_SHORT).show()
            return
        }
        focusViewModel.fetchStatsForExport(selectedStartDate, selectedEndDate)
    }

    private fun showExportDialog(count: Int, duration: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val report = """
            专注报告
            周期: ${sdf.format(Date(selectedStartDate))} 至 ${sdf.format(Date(selectedEndDate))}
            ----------------------
            专注次数: $count 次
            总计时长: $duration 秒
            ----------------------
            生成的报告已准备好。
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("统计报告")
            .setMessage(report)
            .setPositiveButton("复制到剪贴板") { _, _ ->
                val clipboard = androidx.core.content.ContextCompat.getSystemService(requireContext(), android.content.ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("Focus Report", report)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "报告已复制", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun startTimer() {
        if (!timerRunning) {
            handler.postDelayed(timerRunnable, 1000)
            timerRunning = true
            binding.btnStartPause.text = "暂停"
            binding.btnFinish.visibility = View.VISIBLE
        }
    }

    private fun pauseTimer() {
        if (timerRunning) {
            handler.removeCallbacks(timerRunnable)
            timerRunning = false
            binding.btnStartPause.text = "继续"
        }
    }

    private fun resetTimer() {
        handler.removeCallbacks(timerRunnable)
        timerRunning = false
        timeElapsedInMillis = 0
        updateTimerText()
        updateProgressBar()
        binding.btnStartPause.text = "开始专注"
        binding.btnFinish.visibility = View.GONE
    }

    private fun finishFocus() {
        val durationSeconds = (timeElapsedInMillis / 1000).toInt()
        // 只要点击了“完成”，无论时长多少（即使是0秒），都计入一次专注
        focusViewModel.addFocusRecord(durationSeconds)
        resetTimer()
    }

    private fun updateTimerText() {
        val minutes = (timeElapsedInMillis / 1000) / 60
        val seconds = (timeElapsedInMillis / 1000) % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.tvTimer.text = timeFormatted
    }

    private fun updateProgressBar() {
        // For count-up, we can make the progress bar cycle every minute or hour
        // Let's make it cycle every 60 seconds for visual feedback
        val seconds = (timeElapsedInMillis / 1000) % 60
        val progress = (seconds.toFloat() / 60 * 100).toInt()
        binding.progressTimer.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
        _binding = null
    }
}
