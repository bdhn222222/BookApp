package com.example.bookapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import java.security.AuthProvider

class DashboardAdminActivity : AppCompatActivity() {

    //khai báo biến này để dễ thao tác với các thành phần giao diện
    private lateinit var binding: ActivityDashboardAdminBinding

    //khai báo biến này để tương tác với Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    //đây là hàm được gọi khi activity được khởi tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //thiết lập đăng xuất acc cho nút logoutBtn
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this,CategoryAddActivity::class.java))
        }

    }


    //lấy thông tin người dùng đăng nhập bằng Firebase
    private fun checkUser(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //nếu không có người dùng đăng nhập -->đóng activity hiện tại
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            //ngược lại, nếu có người dùng đăng nhập sẽ hiển thị email lên thanh toolbar
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }

}