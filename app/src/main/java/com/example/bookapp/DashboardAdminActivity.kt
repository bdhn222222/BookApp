package com.example.bookapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.example.bookappyt.AdapterCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.AuthProvider

class DashboardAdminActivity : AppCompatActivity() {

    //khai báo biến này để dễ thao tác với các thành phần giao diện
    private lateinit var binding: ActivityDashboardAdminBinding

    //khai báo biến này để tương tác với Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory

    //đây là hàm được gọi khi activity được khởi tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)

                } catch (_: Exception) {

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        // handle click, log out
        binding.logoutBtn.setOnClickListener {

            firebaseAuth.signOut()
            checkUser()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }
        binding.addPdfFad.setOnClickListener{
            startActivity(Intent(this,PdfAddActivity::class.java))

        }

    }

    private fun loadCategories() {
        // Khởi tạo danh sách các danh mục
        categoryArrayList = ArrayList()


        // Tham chiếu đến node "Categories" trong Firebase Database
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        // Thêm sự kiện lắng nghe cho dữ liệu thay đổi trên node "Categories"
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                // Xóa danh sách trước khi thêm dữ liệu mới vào
                categoryArrayList.clear()

                // Duyệt qua từng nút con trong snapshot
                for (ds in snapshot.children) {
                    // Lấy dữ liệu từ snapshot và chuyển đổi thành model
                    val model = ds.getValue(ModelCategory::class.java)

                    // Kiểm tra và thêm model vào danh sách nếu không null

                        categoryArrayList.add(model!!)
                    }


                // Thiết lập AdapterCategory với danh sách các danh mục mới
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,categoryArrayList)

                // Đặt AdapterCategory cho RecyclerView để hiển thị danh sách danh mục
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình truy cập dữ liệu từ Firebase Database
                // Ở đây, chưa cần xử lý gì cụ thể, chỉ để TODO để làm sau
                TODO("Not yet implemented")
            }
        })
    }



    //lấy thông tin người dùng đăng nhập bằng Firebase
    private fun checkUser() {
        /// get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // logged in, get and show user info
            val email = firebaseUser.email

            // set to textview of toolbar
            binding.subTitleTv.text = email

        }
    }

}