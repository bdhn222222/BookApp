package com.example.bookapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // sử dụng AMB để liên kết với layout acitivity_main.xml với biến binding
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener{
            //later
        }
        binding.skipBtn.setOnClickListener{

        }

    }
}