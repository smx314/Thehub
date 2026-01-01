package com.example.unihub.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDoRegister.setOnClickListener {
            val username = binding.etRegUsername.text.toString()
            val password = binding.etRegPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBackLogin.setOnClickListener {
            finish()
        }
    }
}
