package com.example.unihub.ui.finance

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Pair as AndroidPair
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.example.unihub.R
import com.example.unihub.ThehubApplication
import com.example.unihub.data.model.Expense
import com.example.unihub.databinding.DialogAddExpenseBinding
import com.example.unihub.databinding.FragmentFinanceBinding
import com.example.unihub.ui.adapter.ExpenseAdapter
import com.example.unihub.util.UserManager
import com.example.unihub.viewmodel.ExpenseViewModel
import com.example.unihub.viewmodel.ExpenseViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val financeViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((requireActivity().application as ThehubApplication).expenseRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        financeViewModel.setUserId(UserManager.getUserId(requireContext()))

        val adapter = ExpenseAdapter { expense ->
            showDeleteConfirmDialog(expense)
        }
        binding.rvExpenses.adapter = adapter
        binding.rvExpenses.layoutManager = LinearLayoutManager(context)

        financeViewModel.dateRange.observe(viewLifecycleOwner) { range ->
            val startStr = dateFormat.format(Date(range.first))
            val endStr = dateFormat.format(Date(range.second))
            binding.tvDateRange.text = "$startStr 至 $endStr"
        }

        binding.cardDatePicker.setOnClickListener {
            showDateRangePicker()
        }

        financeViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses == null) return@observe
            
            adapter.submitList(expenses)
            
            binding.llEmptyState.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            binding.rvExpenses.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
            
            val totalIncome = expenses.filter { it.type == 1 }.sumOf { it.amount }
            val totalExpense = expenses.filter { it.type == 0 }.sumOf { it.amount }
            val balance = totalIncome - totalExpense
            
            binding.tvTotalIncome.text = "￥${String.format("%.2f", totalIncome)}"
            binding.tvTotalExpense.text = "￥${String.format("%.2f", totalExpense)}"
            binding.tvBalance.text = "￥${String.format("%.2f", balance)}"
        }

        binding.btnAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        return root
    }

    private fun showDeleteConfirmDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除账单")
            .setMessage("确定要删除这条账单吗？")
            .setPositiveButton("删除") { _, _ ->
                financeViewModel.delete(expense)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("选择查询范围")
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { range ->
            val startUtc = range.first
            val endUtc = range.second
            if (startUtc != null && endUtc != null) {
                // 将 UTC 时间戳转换为本地日期零点和深夜
                val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startUtc }
                val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endUtc }
                
                val localStart = Calendar.getInstance().apply {
                    set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val localEnd = Calendar.getInstance().apply {
                    set(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                financeViewModel.setDateRange(localStart, localEnd)
            }
        }
        picker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun showAddExpenseDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        var currentAmount = ""
        var currentType = 0 // 默认支出
        
        // 默认选择今天本地零点的时间戳
        var selectedDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        dialogBinding.btnSelectDate.setOnClickListener {
            // 将本地时间戳转换为 UTC 零点供 MaterialDatePicker 显示
            val localCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH))
            }
            
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择日期")
                .setSelection(utcCal.timeInMillis)
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                // 将返回的 UTC 时间戳转换回本地零点
                val selectionCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = selection }
                val newLocalCal = Calendar.getInstance().apply {
                    set(selectionCal.get(Calendar.YEAR), selectionCal.get(Calendar.MONTH), selectionCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDate = newLocalCal.timeInMillis
                dialogBinding.btnSelectDate.text = dateFormat.format(Date(selectedDate))
            }
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        dialogBinding.toggleType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                currentType = if (checkedId == dialogBinding.btnIncome.id) 1 else 0
                // 互换颜色：收入(1)用 error (红色)，支出(0)用 success (绿色)
                val colorAttr = if (currentType == 1) R.color.error else R.color.success
                dialogBinding.etDialogAmount.setTextColor(resources.getColor(colorAttr, null))
            }
        }

        dialogBinding.numericKeyboard.onNumberClick = { num ->
            currentAmount += num
            dialogBinding.etDialogAmount.setText(currentAmount)
        }

        dialogBinding.numericKeyboard.onDeleteClick = {
            if (currentAmount.isNotEmpty()) {
                currentAmount = currentAmount.dropLast(1)
                dialogBinding.etDialogAmount.setText(currentAmount)
            }
        }

        dialogBinding.numericKeyboard.onClearClick = {
            currentAmount = ""
            dialogBinding.etDialogAmount.setText("")
        }

        dialogBinding.numericKeyboard.onDoneClick = {
            val amount = currentAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val selectedChipId = dialogBinding.cgCategories.checkedChipId
                val category = dialogBinding.root.findViewById<Chip>(selectedChipId)?.text?.toString() ?: "其他"
                val note = dialogBinding.etDialogNote.text.toString()
                val userId = UserManager.getUserId(requireContext())
                financeViewModel.insert(Expense(userId = userId, amount = amount, category = category, note = note, type = currentType, timestamp = selectedDate))
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
