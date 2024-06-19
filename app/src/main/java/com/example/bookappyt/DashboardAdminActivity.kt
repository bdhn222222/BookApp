package com.example.bookappyt

import android.R.layout.simple_spinner_item
import android.R.layout.simple_spinner_dropdown_item
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappyt.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardAdminActivity : AppCompatActivity() {
    // view binding - dùng để tương tác với các view trong layout
    private lateinit var binding: ActivityDashboardAdminBinding

    // xác thực firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // mảng để lưu trữ các thể loại
    private lateinit var categoryArraylist: ArrayList<ModelCategory>

    // biến chuyển đổi
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        // tương tự với Main Activity
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()
        loadCategories()

        // xử lý các sự kiện khi ấn vào nút Seảrch
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            // phương thức này sẽ được gọi mỗi khi nội dung của EditText thay đổi
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*
                s: CharSequence?: Đây là văn bản mới được nhập vào EditText.
                start: Int: Vị trí bắt đầu của văn bản thay đổi.
                before: Int: Số ký tự đã bị xóa trước khi văn bản thay đổi.
                count: Int: Số ký tự mới được thêm vào.
                */
                try {
                    adapterCategory.filter.filter(s) //cố gọi phương thức filer và truyền vào giá trị s
                } catch (e: Exception) {

                }
            }
            // trước khi thay đổi
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            //sau khi
            override fun afterTextChanged(s: Editable?) {

            }
        })


        // xử lý các sự kiện khi nhấn vào nút Log Out
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        // xử lý các sự kiện khi nhấn vào nút Add và chuyển sang CategoryAdd
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }


        // tương tự như trên
        binding.addPdfFab.setOnClickListener{
            startActivity(Intent(this,PdfAddActivity::class.java))

        }

        // click -> open profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this@DashboardAdminActivity, ProfileActivity::class.java))
        }
    }

    private fun loadCategories() {
        // khởi tạo arraylist
        categoryArraylist = ArrayList()

        //lấy hết các dữ liệu từ nút 'Categories' trong FB
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        //lắng nghe thay đổi trong dữ liệu
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list cũ trước khi bắt đầu thêm data vào
                categoryArraylist.clear()
                for (ds in snapshot.children) {
                    // chạy vòng lặp qua mỗi snapshot con và chuyển nó thành các đối tượng ModelCategory
                    val model = ds.getValue(ModelCategory::class.java)
                    // thêm các đối tượng ModelCategory vào List đó
                    categoryArraylist.add(model!!)
                }

                // một AdapterCategory mới được khởi tạo với categoryArraylist làm dữ liệu nguồn.
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArraylist)

                // hiển thị danh sách các thể loại
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        /// get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // không login thì chuyển đến màn hình Main Act
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //nếu có đăng nhập
            val email = firebaseUser.email
            // hiển thị email của Admin lên trên thanh toolbar
            binding.subTitleTv.text = email

        }
    }
}