package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Patterns
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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


        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
        binding.registerBtn.setOnClickListener{
            /*4 buoc:
            B1: Nhap du lieu dau vao
            B2: Xac thuc du lieu
            B3: Tao Account -Firebase Auth
            B4: Luu thong tin User - Firebase Realtime Database
            * */
        }
        var name = ""
        var email = ""
        var password = ""

        fun updateUserInfo() {
            //4) Save User Info - Firebase Realtime Database
            progressDialog.setMessage("Saving user info..")

            //timestamp : lưu trữ thời điểm cập nhập thông tin người dùng
            var timestamp = System.currentTimeMillis()

            //get current user uid -> user id, since user is registerd so we can get it now
            val uid = firebaseAuth.uid
            /*lấy giá trị uid (User ID) của người dùng hiện tại từ đối tượng firebaseAuth.
            firebaseAuth được giả định là một tham chiếu đến đối tượng FirebaseAuth,
            được sử dụng để xác thực và quản lý người dùng.*/

            //set up data to add in db
            val hashMap:HashMap<String, Any?> = HashMap()
            /*tạo một đối tượng HashMap với kiểu dữ liệu <String, Any?>,
            trong đó String là kiểu dữ liệu cho khóa (key)
            và Any? là kiểu dữ liệu cho giá trị (value).
            Đối tượng HashMap này sẽ chứa thông tin người dùng để được lưu trữ trong cơ sở dữ liệu.*/
            hashMap["uid"] = uid
            hashMap["email"] = email
            hashMap["name"] = name
            hashMap["profileImage"] = uid
            hashMap["userType"] = "user"
            hashMap["timestamp"] = timestamp

            //set data on db
            val ref = FirebaseDatabase.getInstance().getReference("Users")
                //tham chieu den nut Users trong co so du lieu Firebase Realtime Database
                //getInstance : trả về thể hiện của lớp FBDatabase
                //getReference: tham chiếu ddến nút Users trong cơ sở dữ liệu
            ref.child(uid!!)
                //tham chiếu ref để tạo ra 1 nút con tên là giá trị uid (UserID) và gán giá trị hashMap vào nút con đó
                .setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                    //chuyển sang màn hình DashboardUserActivity
                    finish()
                }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed saving user info due to ${e.message} ", Toast.LENGTH_SHORT).show()
                }

        }

        fun createUserAccount() {
            //3) Tạo tài khoản mới
            progressDialog.setMessage("Create Account...")
            progressDialog.show()

            //create user in firebase auth
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    //account created
                    updateUserInfo()
                }
                .addOnFailureListener { e->
                    //failed created account
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed creating account due to ${e.message} ", Toast.LENGTH_SHORT).show()
                }
        }

        //xac thuc du lieu
        fun validateDate(){
            //1- Du lieu dau vao
            name = binding.nameEt.toString().trim() //ham trim() -- xoa vo cac khoang trang (space)
            email = binding.emailEt.toString().trim()
            password = binding.passwordEt.toString().trim()
            val cPassword = binding.cPasswordEt.toString().trim()

            //2 Xac thuc du lieu dau vao
            if(name.isEmpty()){
                //empty name
                Toast.makeText(this, "Enter your name..", Toast.LENGTH_SHORT).show()
                //Toast dùng để hiển thị thông báo ngắn gọn lên màn hình
                //makeText -> tạo ra 1 thông báo mới, nhậm các tham số cần thiết để tạo nên thông báo
                // tham số 1 this-> đại diện cho context hiện tại -> Activity hoặc Fragment
                //tham số 2 -> chuỗi văn bản muốn hiển thị thông báo
                //tham số 3 -> thời gian hin thị thông báo -> Length_short -> 2s
                //.show() -> hiển thị lên màn hình
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                //empty name
                Toast.makeText(this, "Invalid Email Pattern..", Toast.LENGTH_SHORT).show()
            }
            else if(password.isEmpty()){
                //empty name
                Toast.makeText(this, "Enter your password..", Toast.LENGTH_SHORT).show()
            }
            else if(cPassword.isEmpty()){
                //empty name
                Toast.makeText(this, "Confirm Password..", Toast.LENGTH_SHORT).show()
            }
            else if(password != cPassword){
                //empty name
                Toast.makeText(this, "Password doesn't match..", Toast.LENGTH_SHORT).show()
            }
            else {
                createUserAccount()
            }
        }
    }
}