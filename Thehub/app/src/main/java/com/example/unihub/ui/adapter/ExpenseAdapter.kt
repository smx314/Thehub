package com.example.unihub.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.R
import com.example.unihub.data.model.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class ExpenseViewHolder(
        itemView: View,
        private val onDeleteClick: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_item_category)
        private val tvNote: TextView = itemView.findViewById(R.id.tv_item_note)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_item_amount)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_item_date)
        private val viewIndicator: View = itemView.findViewById(R.id.view_type_indicator)

        fun bind(expense: Expense) {
            tvCategory.text = expense.category
            if (expense.note.isNotEmpty()) {
                tvNote.text = expense.note
                tvNote.visibility = View.VISIBLE
            } else {
                tvNote.visibility = View.GONE
            }

            val prefix = if (expense.type == 1) "+" else "-"
            // 互换颜色：收入(type=1)用 error (红色)，支出(type=0)用 success (绿色)
            val colorAttr = if (expense.type == 1) R.color.error else R.color.success
            tvAmount.text = "$prefix ￥${String.format("%.2f", expense.amount)}"
            tvAmount.setTextColor(itemView.context.getColor(colorAttr))
            viewIndicator.setBackgroundColor(itemView.context.getColor(colorAttr))

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            tvDate.text = sdf.format(Date(expense.timestamp))

            itemView.setOnClickListener {
                // 可以添加点击查看详情或编辑的功能，目前保持简单
            }

            itemView.setOnLongClickListener {
                onDeleteClick(expense)
                true
            }
        }
    }

    class ExpenseComparator : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
