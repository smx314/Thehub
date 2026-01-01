package com.example.unihub.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.unihub.R
import com.example.unihub.databinding.FragmentProfileBinding
import com.example.unihub.ui.auth.LoginActivity
import com.example.unihub.util.UserManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = UserManager.getUserId(requireContext())
        binding.tvNickname.text = userId
        binding.tvEmail.text = "$userId@unihub.com"

        setupOptions()

        binding.btnLogout.setOnClickListener {
            // 清除用户标识
            UserManager.clearUserId(requireContext())
            
            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun setupOptions() {
        binding.itemSettings.apply {
            tvTitle.text = "通用设置"
            ivIcon.setImageResource(android.R.drawable.ic_menu_preferences)
            root.setOnClickListener { 
                findNavController().navigate(R.id.navigation_settings)
            }
        }

        binding.itemAbout.apply {
            tvTitle.text = "关于 Thehub"
            ivIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            root.setOnClickListener { 
                findNavController().navigate(R.id.navigation_about)
            }
        }

        binding.itemFeedback.apply {
            tvTitle.text = "意见反馈"
            ivIcon.setImageResource(android.R.drawable.ic_menu_send)
            root.setOnClickListener { 
                findNavController().navigate(R.id.navigation_feedback)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
