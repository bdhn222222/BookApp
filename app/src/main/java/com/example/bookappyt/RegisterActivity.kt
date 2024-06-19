package com.example.bookappyt

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookappyt.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        // tượng tự với Main Activity
        // lưu các giá trị đang bị hủy ? - khởi tạo các logic đúng để chạy Activity
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // xử lý sự kiện khi gặp Back button
        binding.backBtn.setOnClickListener {
            onBackPressed() // quay lại màn hình trước
        }

        // xử lý sự kiện khi ấn nút tạo Account
        binding.registerBtn.setOnClickListener {
            /*Steps
            * Dữ liệu đầu vào
            * Xác thực đầu vào
            * Tạo Acc - FireBase
            * Lưu dữ liệu người dùng - firebase realtime database*/
            validateData() // khởi tạo hàm
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        // 2) Xác thực dữ liệu người dùng
        if (name.isEmpty())
            Toast.makeText(this, "Enter your name ...", Toast.LENGTH_SHORT).show()
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern ...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password ...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Confirm password ...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Password doesn't match ...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        // sử dụng FA để tạo 1 tài khoản mới với email và password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // nếu tạo thành công thì khởi tạo hàm này
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        // Lưu thông tin của người dùng - Firebase realtime databse
        progressDialog.setMessage("Saving user info...")
        val timestamp = System.currentTimeMillis()

        // lấy thông tin người dùng
        val uid = firebaseAuth.uid // lấy id của người dùng
        val hashMap: HashMap<String, Any?> = HashMap() // tạo 1 hasmMap chứa thông tin ngừoi dùng
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user" // thông thường sẽ là user, nếu muốn đổi sang user/admin thì sẽ chuyển trong db
        hashMap["timestamp"] = timestamp

        // lưu  thông tin người dùng vào FRD
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        // tham chiếu vào nút Users

        // lưu thông tin người dùng vào nút Users và uid là khóa chính
        ref.child(uid!!).setValue(hashMap).addOnSuccessListener {
            //nếu lưu thông tin thành công thì sẽ có hộp thoại hiện ra
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Account created...",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
            finish()

        }.addOnFailureListener { e -> // nếu tạo không thành công
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed saving user information due to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()

        }


    }
}