package com.example.unihub.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import com.example.unihub.databinding.ViewNumericKeyboardBinding

class NumericKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private val binding: ViewNumericKeyboardBinding

    var onNumberClick: ((String) -> Unit)? = null
    var onDeleteClick: (() -> Unit)? = null
    var onClearClick: (() -> Unit)? = null
    var onDoneClick: (() -> Unit)? = null

    init {
        binding = ViewNumericKeyboardBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        val buttons = listOf(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."
        )

        // 因为 view_numeric_keyboard.xml 的根节点是 GridLayout，
        // 而 NumericKeyboard 也继承自 GridLayout，
        // inflate(..., this, true) 会把 XML 里的 GridLayout 作为子 View 添加进来。
        val container = binding.root as ViewGroup
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is Button) {
                val text = child.text.toString()
                when (text) {
                    "DEL" -> child.setOnClickListener { onDeleteClick?.invoke() }
                    "C" -> child.setOnClickListener { onClearClick?.invoke() }
                    "OK" -> child.setOnClickListener { onDoneClick?.invoke() }
                    in buttons -> child.setOnClickListener { onNumberClick?.invoke(text) }
                }
            }
        }
    }
}
