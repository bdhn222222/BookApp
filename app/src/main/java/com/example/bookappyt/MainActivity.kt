package com.example.bookappyt

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookappyt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    // hàm khởi tạo nhưng vẫn lưu lại các trang thái trước đó nếu như Activity bị hủy (back/ xoay ngang), lưu thông tin đó vào Bundle
    override fun onCreate(savedInstanceState: Bundle?) {
        // đảm bảo rằng các logic khởi tạo cơ bản của Activity được thực hiện đúng cách.
        super.onCreate(savedInstanceState)
        //truy cập các view trong layout của activity này
        binding = ActivityMainBinding.inflate(layoutInflater)
        // TODO check documentation on view binding on android docs
        setContentView(binding.root) //thiết lập nội dung của Act bằng layout cuả Act đó
        //biding.root đại diện cho gốc layout

        // Thiết lập sự kiện cho nút Login Button
        binding.loginBtn.setOnClickListener {
            // Khởi chạy 1 Intent để chuyển sang màn hình Login
            //Intent này có chứa 1 số dữ liệu và giúp giao tiếp giữa các thành phần ( vd: Act này với Act khác)
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Thiết lập sự kiện cho nút Skip Button
        binding.skipBtn.setOnClickListener {
            // khởi tạo Intent và chuyển sang 1 màn hình User trang chủ
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }
    }
}