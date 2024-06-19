package com.example.bookappyt

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.bookappyt.databinding.ActivityCategoryAddBinding
import com.example.bookappyt.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseRegistrar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class CategoryAddActivity : AppCompatActivity() {

    // view
    private lateinit var binding: ActivityCategoryAddBinding

    //  xác thực firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // hộp thoại
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // khởi tạo firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // hộp thoại
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // nút Back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // nút submit
        binding.submitBtn.setOnClickListener {
            validateData() //xác thực dữ liệu đầu vào
        }
    }

    private var category = ""
    private fun validateData() {
        /*Trước khi thêm dữ liệu*/
        // lấy dữ liệu
        category = binding.categoryEt.text.toString().trim()

        // nếu trống
        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter category...!", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        // show progress
        progressDialog.setMessage("Adding category...")
        progressDialog.show()

        // lấy mốc thời gian
        val timestamp = System.currentTimeMillis()

        val hashMap: HashMap<String, Any?> = HashMap()
        // tạo ra 1 hashMap chứ các cặp Key : Value
        hashMap["id"] = "$timestamp"
        hashMap["category"] = "$category"
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        // Tham chiếu đến nút Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener { //thêm dữ liệu
            //nếu add được
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Category added successfully...",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e ->
            // nếu fail failed
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed to add dui to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()

        }
    }
}