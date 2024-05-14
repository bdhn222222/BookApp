package com.example.bookapp

import android.app.ProgressDialog
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    //view Binding
    private lateinit var binding:ActivityRegisterBinding

    //
    private  lateinit var firebaseAuth: FirebaseAuth
    //xac thuc nguoi dung = Firebase Authentication

    private  lateinit var progressDialog: ProgressDialog
    //hien thi 1 hop thoai tien trinh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        //tham chieu den hoat dong hien tai
        progressDialog.setTitle("Please wait")
        //Tieu de cho hop thoai
        progressDialog.setCanceledOnTouchOutside(false)
        //khong cho nguoi dung cham vao ben ngoai cua hop thoai, hop thoai se khong bi tat

        /*
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }*/
    }
}